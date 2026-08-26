package com.porest.hr.common.config;

import com.github.gavlyukovskiy.boot.jdbc.decorator.DataSourceDecoratorProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * p6spy SQL 로깅 설정이 <b>라이브러리가 실제로 읽는 자리</b>에 있는지 고정한다.
 *
 * <p><b>왜 이 테스트가 있나</b> — 설정이 {@code spring.datasource.p6spy.*} 에 적혀 있었다.
 * 그 prefix 를 읽는 {@code @ConfigurationProperties} 가 없어서
 * ({@link DataSourceDecoratorProperties} 는 {@code decorator.datasource} 다) 네 줄 전부가
 * 죽은 설정이었고, {@code P6SPY_ENABLED} 를 어떤 값으로 두든 p6spy 는 라이브러리 기본값
 * {@code enableLogging=true} 로 돌았다. 스프링은 모르는 키를 조용히 무시하므로 기동도
 * 테스트도 초록불이었다 — 사원 이름·이메일·생년월일이 값째로 박힌 SQL 이 dev 로그에
 * 계속 남는 동안.
 *
 * <p>그래서 문자열 비교가 아니라 <b>라이브러리의 프로퍼티 클래스에 실제로 바인딩</b>해서 본다.
 * 라이브러리가 prefix 를 바꾸면 이 테스트가 먼저 깨진다.
 */
class P6SpyLoggingConfigTest {

    /** {@code @ConfigurationProperties} 가 없어 아무도 읽지 않는, 예전에 쓰던 자리. */
    private static final String DEAD_PREFIX = "spring.datasource.p6spy";

    /** 라이브러리가 읽는 자리. */
    private static final String LIVE_PREFIX = "decorator.datasource";

    /**
     * 클래스패스가 아니라 소스 트리의 파일을 직접 읽는다.
     *
     * <p>{@code ClassPathResource("application.yml")} 로 잡으면 테스트 리소스에 같은 이름이
     * 생기는 순간 그쪽이 이겨서, 정작 배포되는 파일은 안 보고도 초록불이 뜬다
     * <b>이 레포에는 실제로 {@code src/test/resources/application.yml} 이 있다</b> — 확인해 보면
     * {@code ClassPathResource} 는 {@code build/resources/test/application.yml} 을 잡는다.
     * 배포되는 파일은 {@code build/resources/main/} 쪽인데 클래스패스에서 뒤에 온다.
     * 이 테스트가 막으려는 게 바로 "엉뚱한 자리를 보고 통과하는" 실패 모드라,
     * 같은 함정을 테스트가 다시 밟게 두지 않는다.
     */
    private static final String MAIN_YAML = "src/main/resources/application.yml";

    @Test
    @DisplayName("환경변수를 주지 않으면 p6spy 로깅은 꺼진 채로 바인딩된다")
    void disabledByDefault() throws IOException {
        assertThat(bind(load()).getP6spy().isEnableLogging())
                .as("기본값이 켜짐이면 dev·운영 로그에 값이 박힌 SQL 이 남는다")
                .isFalse();
    }

    @Test
    @DisplayName("P6SPY_LOG_SQL_VALUES=true 로만 켜진다 — 새 이름이 실제로 물려 있는지 본다")
    void enabledOnlyByNewEnvName() throws IOException {
        MutablePropertySources sources = load();
        sources.addFirst(new MapPropertySource(
                "env", Map.of("P6SPY_LOG_SQL_VALUES", (Object) "true")));

        assertThat(bind(sources).getP6spy().isEnableLogging()).isTrue();
    }

    @Test
    @DisplayName("옛 이름 P6SPY_ENABLED 로는 켜지지 않는다 — 서버 .env 에 남은 줄이 되살아나면 안 된다")
    void oldEnvNameNoLongerTurnsItOn() throws IOException {
        MutablePropertySources sources = load();
        sources.addFirst(new MapPropertySource(
                "env", Map.of("P6SPY_ENABLED", (Object) "true")));

        assertThat(bind(sources).getP6spy().isEnableLogging()).isFalse();
    }

    @Test
    @DisplayName("죽은 prefix 로 되돌아가지 않는다 — 스프링이 조용히 무시해 아무도 못 알아챈다")
    void noKeysUnderDeadPrefix() throws IOException {
        assertThat(keysOf(load())).noneMatch(key -> key.startsWith(DEAD_PREFIX));
    }

    private static DataSourceDecoratorProperties bind(MutablePropertySources sources) {
        return new Binder(
                ConfigurationPropertySources.from(sources),
                new PropertySourcesPlaceholdersResolver(sources))
                .bind(LIVE_PREFIX, DataSourceDecoratorProperties.class)
                .orElseThrow(() -> new AssertionError(
                        LIVE_PREFIX + " 아래에 설정이 없다 — 라이브러리가 읽는 prefix 가 여기다"));
    }

    private static MutablePropertySources load() throws IOException {
        Resource resource = new FileSystemResource(MAIN_YAML);
        assertThat(resource.exists())
                .as("%s 를 못 찾았다 — 테스트 작업 디렉토리가 프로젝트 루트가 아니다", MAIN_YAML)
                .isTrue();

        MutablePropertySources sources = new MutablePropertySources();
        new YamlPropertySourceLoader().load(MAIN_YAML, resource).forEach(sources::addLast);
        return sources;
    }

    private static List<String> keysOf(MutablePropertySources sources) {
        return sources.stream()
                .filter(EnumerablePropertySource.class::isInstance)
                .map(EnumerablePropertySource.class::cast)
                .map(EnumerablePropertySource::getPropertyNames)
                .flatMap(Arrays::stream)
                .map(String.class::cast)
                .toList();
    }
}
