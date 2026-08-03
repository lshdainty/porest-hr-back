package com.porest.hr.common.time;

import com.porest.core.time.ServiceClock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * core 시각 유틸 빈 등록.
 *
 * <p>core 클래스에는 Spring 어노테이션이 없다(컴포넌트 스캔 범위에 {@code com.porest.core} 가
 * 들어오는지에 의존하지 않기 위해). 그래서 사용하는 쪽에서 명시 등록한다.
 *
 * <p>hr 의 날짜 판정은 {@link CompanyClock}(회사 소재지 기준)을 쓴다. 여기 등록하는
 * {@link ServiceClock} 은 회사 타임존을 못 읽었을 때의 폴백이자 배치 기준이다.
 */
@Configuration
public class ClockConfig {

    @Bean
    public ServiceClock serviceClock(@Value("${app.scheduler.zone:Asia/Seoul}") String zone) {
        return new ServiceClock(zone);
    }
}
