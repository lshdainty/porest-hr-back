package com.lshdainty.porest.security.service;

import com.lshdainty.porest.common.config.properties.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP 블랙리스트 관리 서비스
 * - 설정 파일 기반 블랙리스트
 * - 런타임 동적 추가/제거
 * - CIDR 표기법 지원
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IpBlacklistService {

    private final SecurityProperties securityProperties;

    /**
     * 런타임에 추가된 블랙리스트 (동적 차단용)
     * ConcurrentHashMap으로 스레드 안전성 보장
     */
    private final Set<String> runtimeBlockedIps = ConcurrentHashMap.newKeySet();

    /**
     * IP가 블랙리스트에 있는지 확인
     *
     * @param ipAddress 확인할 IP 주소
     * @return 차단 대상이면 true
     */
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

        // 2. 설정 파일 블랙리스트 확인 (CIDR 포함)
        for (String blockedIp : securityProperties.getIpBlacklist().getBlockedIps()) {
            if (matchesIpPattern(normalizedIp, blockedIp)) {
                log.debug("IP {} matches blacklist pattern: {}", normalizedIp, blockedIp);
                return true;
            }
        }

        return false;
    }

    /**
     * 런타임에 IP를 블랙리스트에 추가
     *
     * @param ipAddress 차단할 IP
     */
    public void addToBlacklist(String ipAddress) {
        String normalizedIp = normalizeIpAddress(ipAddress);
        runtimeBlockedIps.add(normalizedIp);
        log.warn("⚠️ IP added to runtime blacklist: {}", normalizedIp);
    }

    /**
     * 런타임 블랙리스트에서 IP 제거
     *
     * @param ipAddress 차단 해제할 IP
     */
    public void removeFromBlacklist(String ipAddress) {
        String normalizedIp = normalizeIpAddress(ipAddress);
        runtimeBlockedIps.remove(normalizedIp);
        log.info("IP removed from runtime blacklist: {}", normalizedIp);
    }

    /**
     * 현재 런타임 블랙리스트 조회
     */
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
