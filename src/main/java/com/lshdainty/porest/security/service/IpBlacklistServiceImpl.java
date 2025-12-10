package com.lshdainty.porest.security.service;

import com.lshdainty.porest.common.config.properties.SecurityProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class IpBlacklistServiceImpl implements IpBlacklistService {

    private final SecurityProperties securityProperties;

    /**
     * 런타임에 추가된 블랙리스트 (동적 차단용)
     * ConcurrentHashMap으로 스레드 안전성 보장
     */
    private final Set<String> runtimeBlockedIps = ConcurrentHashMap.newKeySet();

    /**
     * 외부 파일에서 로드된 블랙리스트
     */
    private final List<String> fileBasedBlockedIps = new ArrayList<>();

    /**
     * 애플리케이션 시작 시 외부 파일에서 IP 블랙리스트 로드
     */
    @PostConstruct
    public void init() {
        if (!securityProperties.getIpBlacklist().isEnabled()) {
            log.info("IP blacklist is disabled");
            return;
        }

        loadBlockedIpsFromFile();
        logBlacklistSummary();
    }

    /**
     * 외부 파일에서 IP 블랙리스트 로드
     */
    private void loadBlockedIpsFromFile() {
        String filePath = securityProperties.getIpBlacklist().getFilePath();

        if (!StringUtils.hasText(filePath)) {
            log.debug("No IP blacklist file configured");
            return;
        }

        try {
            Path path = Paths.get(filePath);

            if (!Files.exists(path)) {
                log.warn("⚠️ IP blacklist file not found: {} (continuing without file-based blacklist)", filePath);
                return;
            }

            if (!Files.isReadable(path)) {
                log.error("❌ IP blacklist file is not readable: {}", filePath);
                return;
            }

            int loadedCount = 0;
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String line;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    String ip = parseLine(line);

                    if (ip != null) {
                        fileBasedBlockedIps.add(ip);
                        loadedCount++;
                    }
                }
            }

            log.info("✅ Loaded {} IP addresses from blacklist file: {}", loadedCount, filePath);

        } catch (IOException e) {
            log.error("❌ Failed to read IP blacklist file: {}", filePath, e);
        }
    }

    /**
     * 파일의 한 줄을 파싱하여 유효한 IP 주소 추출
     * - 빈 줄 무시
     * - # 으로 시작하는 주석 무시
     * - 앞뒤 공백 제거
     *
     * @param line 파일의 한 줄
     * @return 유효한 IP 주소 또는 null
     */
    private String parseLine(String line) {
        if (line == null) {
            return null;
        }

        // 주석 제거 (# 이후 모두 제거)
        int commentIndex = line.indexOf('#');
        if (commentIndex >= 0) {
            line = line.substring(0, commentIndex);
        }

        // 앞뒤 공백 제거
        line = line.trim();

        // 빈 줄 무시
        if (line.isEmpty()) {
            return null;
        }

        return line;
    }

    /**
     * 블랙리스트 로딩 결과 요약 로그
     */
    private void logBlacklistSummary() {
        int fileCount = fileBasedBlockedIps.size();

        if (fileCount > 0) {
            log.info("📋 IP Blacklist loaded: {} IPs from file", fileCount);
        } else {
            log.info("📋 IP Blacklist is empty (no IPs configured in file)");
        }
    }

    @Override
    public boolean isBlocked(String ipAddress) {
        if (!securityProperties.getIpBlacklist().isEnabled()) {
            return false;
        }

        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }

        // IPv6 주소를 IPv4로 변환 (::1 -> 127.0.0.1)
        String normalizedIp = normalizeIpAddress(ipAddress);

        // 1. 런타임 블랙리스트 확인 (정확한 매칭)
        if (runtimeBlockedIps.contains(normalizedIp)) {
            log.debug("IP {} is in runtime blacklist", normalizedIp);
            return true;
        }

        // 2. 외부 파일 블랙리스트 확인 (CIDR 포함)
        for (String blockedIp : fileBasedBlockedIps) {
            if (matchesIpPattern(normalizedIp, blockedIp)) {
                log.debug("IP {} matches file-based blacklist pattern: {}", normalizedIp, blockedIp);
                return true;
            }
        }

        return false;
    }

    @Override
    public void addToBlacklist(String ipAddress) {
        String normalizedIp = normalizeIpAddress(ipAddress);
        runtimeBlockedIps.add(normalizedIp);
        log.warn("⚠️ IP added to runtime blacklist: {}", normalizedIp);
    }

    @Override
    public void removeFromBlacklist(String ipAddress) {
        String normalizedIp = normalizeIpAddress(ipAddress);
        runtimeBlockedIps.remove(normalizedIp);
        log.info("IP removed from runtime blacklist: {}", normalizedIp);
    }

    @Override
    public Set<String> getRuntimeBlacklist() {
        return Set.copyOf(runtimeBlockedIps);
    }

    /**
     * IP 주소 정규화
     * - IPv6 루프백(::1)을 IPv4(127.0.0.1)로 변환
     * - IPv6 매핑된 IPv4 주소 처리
     */
    private String normalizeIpAddress(String ipAddress) {
        if (ipAddress == null) {
            return "";
        }

        // IPv6 루프백
        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            return "127.0.0.1";
        }

        // IPv6-mapped IPv4 주소 (::ffff:192.168.1.1 -> 192.168.1.1)
        if (ipAddress.startsWith("::ffff:")) {
            return ipAddress.substring(7);
        }

        return ipAddress;
    }

    /**
     * IP 패턴 매칭 (CIDR 표기법 지원)
     *
     * @param ip      확인할 IP
     * @param pattern 패턴 (예: "192.168.1.100" 또는 "192.168.1.0/24")
     * @return 매칭되면 true
     */
    private boolean matchesIpPattern(String ip, String pattern) {
        // 정확한 매칭
        if (ip.equals(pattern)) {
            return true;
        }

        // CIDR 표기법 처리 (예: 192.168.1.0/24)
        if (pattern.contains("/")) {
            return matchesCidr(ip, pattern);
        }

        return false;
    }

    /**
     * CIDR 표기법 매칭
     *
     * @param ip         확인할 IP (예: "192.168.1.100")
     * @param cidr       CIDR 표기 (예: "192.168.1.0/24")
     * @return 범위 내에 있으면 true
     */
    private boolean matchesCidr(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                return false;
            }

            String networkAddress = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            InetAddress ipAddr = InetAddress.getByName(ip);
            InetAddress networkAddr = InetAddress.getByName(networkAddress);

            byte[] ipBytes = ipAddr.getAddress();
            byte[] networkBytes = networkAddr.getAddress();

            if (ipBytes.length != networkBytes.length) {
                return false; // IPv4와 IPv6 혼용 불가
            }

            int maskBits = prefixLength;
            for (int i = 0; i < ipBytes.length; i++) {
                int mask = (maskBits >= 8) ? 0xFF : (0xFF << (8 - maskBits)) & 0xFF;
                maskBits = Math.max(0, maskBits - 8);

                if ((ipBytes[i] & mask) != (networkBytes[i] & mask)) {
                    return false;
                }
            }

            return true;
        } catch (UnknownHostException | NumberFormatException e) {
            log.error("Invalid CIDR pattern: {}", cidr, e);
            return false;
        }
    }
}
