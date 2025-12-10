package com.lshdainty.porest.service;

import com.lshdainty.porest.common.config.properties.SecurityProperties;
import com.lshdainty.porest.security.service.IpBlacklistServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("IP 블랙리스트 서비스 테스트")
class IpBlacklistServiceTest {

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private SecurityProperties.IpBlacklist ipBlacklistConfig;

    @InjectMocks
    private IpBlacklistServiceImpl ipBlacklistService;

    @BeforeEach
    void setUp() {
        given(securityProperties.getIpBlacklist()).willReturn(ipBlacklistConfig);
        // 테스트 간 독립성을 위해 런타임 블랙리스트 초기화
        Set<String> runtimeBlockedIps = ConcurrentHashMap.newKeySet();
        ReflectionTestUtils.setField(ipBlacklistService, "runtimeBlockedIps", runtimeBlockedIps);
    }

    @Nested
    @DisplayName("isBlocked")
    class IsBlocked {
        @Test
        @DisplayName("성공 - 비활성화 상태면 false를 반환한다")
        void isBlockedDisabled() {
            // given
            given(ipBlacklistConfig.isEnabled()).willReturn(false);

            // when
            boolean result = ipBlacklistService.isBlocked("192.168.1.100");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("성공 - null IP면 false를 반환한다")
        void isBlockedNullIp() {
            // given
            given(ipBlacklistConfig.isEnabled()).willReturn(true);

            // when
            boolean result = ipBlacklistService.isBlocked(null);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("성공 - 빈 IP면 false를 반환한다")
        void isBlockedEmptyIp() {
            // given
            given(ipBlacklistConfig.isEnabled()).willReturn(true);

            // when
            boolean result = ipBlacklistService.isBlocked("");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("성공 - 런타임 블랙리스트에 있으면 true를 반환한다")
        void isBlockedInRuntimeBlacklist() {
            // given
            given(ipBlacklistConfig.isEnabled()).willReturn(true);
            ipBlacklistService.addToBlacklist("192.168.1.100");

            // when
            boolean result = ipBlacklistService.isBlocked("192.168.1.100");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 블랙리스트에 없으면 false를 반환한다")
        void isBlockedNotInBlacklist() {
            // given
            given(ipBlacklistConfig.isEnabled()).willReturn(true);

            // when
            boolean result = ipBlacklistService.isBlocked("192.168.1.100");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("성공 - IPv6 루프백을 IPv4로 정규화한다 (::1)")
        void isBlockedIPv6Loopback() {
            // given
            given(ipBlacklistConfig.isEnabled()).willReturn(true);
            ipBlacklistService.addToBlacklist("127.0.0.1");

            // when
            boolean result = ipBlacklistService.isBlocked("::1");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - IPv6 루프백을 IPv4로 정규화한다 (0:0:0:0:0:0:0:1)")
        void isBlockedIPv6LoopbackLong() {
            // given
            given(ipBlacklistConfig.isEnabled()).willReturn(true);
            ipBlacklistService.addToBlacklist("127.0.0.1");

            // when
            boolean result = ipBlacklistService.isBlocked("0:0:0:0:0:0:0:1");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - IPv6 매핑된 IPv4를 정규화한다")
        void isBlockedIPv6MappedIPv4() {
            // given
            given(ipBlacklistConfig.isEnabled()).willReturn(true);
            ipBlacklistService.addToBlacklist("192.168.1.100");

            // when
            boolean result = ipBlacklistService.isBlocked("::ffff:192.168.1.100");

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("addToBlacklist")
    class AddToBlacklist {
        @Test
        @DisplayName("성공 - IP가 런타임 블랙리스트에 추가된다")
        void addToBlacklistSuccess() {
            // given
            String ip = "192.168.1.100";
            given(ipBlacklistConfig.isEnabled()).willReturn(true);

            // when
            ipBlacklistService.addToBlacklist(ip);

            // then
            assertThat(ipBlacklistService.isBlocked(ip)).isTrue();
            assertThat(ipBlacklistService.getRuntimeBlacklist()).contains(ip);
        }

        @Test
        @DisplayName("성공 - IPv6 루프백이 정규화되어 추가된다")
        void addToBlacklistNormalized() {
            // given
            given(ipBlacklistConfig.isEnabled()).willReturn(true);

            // when
            ipBlacklistService.addToBlacklist("::1");

            // then
            assertThat(ipBlacklistService.getRuntimeBlacklist()).contains("127.0.0.1");
        }
    }

    @Nested
    @DisplayName("removeFromBlacklist")
    class RemoveFromBlacklist {
        @Test
        @DisplayName("성공 - IP가 런타임 블랙리스트에서 제거된다")
        void removeFromBlacklistSuccess() {
            // given
            String ip = "192.168.1.100";
            given(ipBlacklistConfig.isEnabled()).willReturn(true);
            ipBlacklistService.addToBlacklist(ip);

            // when
            ipBlacklistService.removeFromBlacklist(ip);

            // then
            assertThat(ipBlacklistService.isBlocked(ip)).isFalse();
            assertThat(ipBlacklistService.getRuntimeBlacklist()).doesNotContain(ip);
        }
    }

    @Nested
    @DisplayName("getRuntimeBlacklist")
    class GetRuntimeBlacklist {
        @Test
        @DisplayName("성공 - 런타임 블랙리스트를 반환한다")
        void getRuntimeBlacklistSuccess() {
            // given
            given(ipBlacklistConfig.isEnabled()).willReturn(true);
            ipBlacklistService.addToBlacklist("192.168.1.100");
            ipBlacklistService.addToBlacklist("192.168.1.101");

            // when
            Set<String> result = ipBlacklistService.getRuntimeBlacklist();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).contains("192.168.1.100", "192.168.1.101");
        }

        @Test
        @DisplayName("성공 - 빈 블랙리스트를 반환한다")
        void getRuntimeBlacklistEmpty() {
            // when
            Set<String> result = ipBlacklistService.getRuntimeBlacklist();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("init")
    class Init {
        @Test
        @DisplayName("성공 - 비활성화 상태면 초기화를 건너뛴다")
        void initDisabled() {
            // given
            given(ipBlacklistConfig.isEnabled()).willReturn(false);

            // when
            ipBlacklistService.init();

            // then - 예외가 발생하지 않으면 성공
        }

        @Test
        @DisplayName("성공 - 파일 경로가 없으면 초기화를 건너뛴다")
        void initNoFilePath() {
            // given
            given(ipBlacklistConfig.isEnabled()).willReturn(true);
            given(ipBlacklistConfig.getFilePath()).willReturn(null);

            // when
            ipBlacklistService.init();

            // then - 예외가 발생하지 않으면 성공
        }

        @Test
        @DisplayName("성공 - 파일이 존재하지 않으면 초기화를 건너뛴다")
        void initFileNotExists() {
            // given
            given(ipBlacklistConfig.isEnabled()).willReturn(true);
            given(ipBlacklistConfig.getFilePath()).willReturn("/nonexistent/path/blacklist.txt");

            // when
            ipBlacklistService.init();

            // then - 예외가 발생하지 않으면 성공
        }

        @Test
        @DisplayName("성공 - 파일에서 IP를 로드한다")
        void initLoadFromFile(@TempDir Path tempDir) throws IOException {
            // given
            Path blacklistFile = tempDir.resolve("blacklist.txt");
            Files.write(blacklistFile, List.of(
                    "192.168.1.100",
                    "# This is a comment",
                    "192.168.1.101 # inline comment",
                    "",
                    "10.0.0.0/8"
            ));

            given(ipBlacklistConfig.isEnabled()).willReturn(true);
            given(ipBlacklistConfig.getFilePath()).willReturn(blacklistFile.toString());

            // when
            ipBlacklistService.init();

            // then - 파일 로드 성공 (파일 기반 블랙리스트는 private이므로 직접 테스트 불가)
            // isBlocked 메서드로 간접 확인
            assertThat(ipBlacklistService.isBlocked("192.168.1.100")).isTrue();
            assertThat(ipBlacklistService.isBlocked("192.168.1.101")).isTrue();
        }
    }

    @Nested
    @DisplayName("CIDR 매칭")
    class CidrMatching {
        @Test
        @DisplayName("성공 - CIDR 범위 내 IP를 차단한다")
        void cidrMatchingSuccess(@TempDir Path tempDir) throws IOException {
            // given
            Path blacklistFile = tempDir.resolve("blacklist.txt");
            Files.write(blacklistFile, List.of("192.168.1.0/24"));

            given(ipBlacklistConfig.isEnabled()).willReturn(true);
            given(ipBlacklistConfig.getFilePath()).willReturn(blacklistFile.toString());

            ipBlacklistService.init();

            // when & then
            assertThat(ipBlacklistService.isBlocked("192.168.1.1")).isTrue();
            assertThat(ipBlacklistService.isBlocked("192.168.1.255")).isTrue();
            assertThat(ipBlacklistService.isBlocked("192.168.2.1")).isFalse();
        }

        @Test
        @DisplayName("성공 - /32 CIDR은 단일 IP와 동일하다")
        void cidr32MatchesSingleIp(@TempDir Path tempDir) throws IOException {
            // given
            Path blacklistFile = tempDir.resolve("blacklist.txt");
            Files.write(blacklistFile, List.of("192.168.1.100/32"));

            given(ipBlacklistConfig.isEnabled()).willReturn(true);
            given(ipBlacklistConfig.getFilePath()).willReturn(blacklistFile.toString());

            ipBlacklistService.init();

            // when & then
            assertThat(ipBlacklistService.isBlocked("192.168.1.100")).isTrue();
            assertThat(ipBlacklistService.isBlocked("192.168.1.101")).isFalse();
        }
    }
}
