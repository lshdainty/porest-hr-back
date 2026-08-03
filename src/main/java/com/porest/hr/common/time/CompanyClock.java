package com.porest.hr.common.time;

import com.porest.core.time.ServiceClock;
import com.porest.hr.company.domain.Company;
import com.porest.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 회사 기준 시각.
 *
 * <p>인력관리는 <b>회사 소재지</b>를 기준으로 날짜를 판단한다 — 근무일·휴가 만료·게시 기간은
 * 직원 개인이 아니라 회사가 정하는 값이다. 소속 직원은 회사 타임존을 그대로 따른다
 * (국내 회사면 {@code Asia/Seoul}, 미국 회사면 그 지역 타임존).
 *
 * <p><b>왜 사용자별이 아닌가</b> — 같은 회사 직원은 타임존이 같으므로 사용자별로 조회하면
 * 결과는 같은데 쿼리만 늘어난다. 게다가 공지 게시 기간·근무 시스템 로그처럼 사용자 컨텍스트가
 * 아예 없는 판정도 많다. 다중 회사를 지원하게 되면 그때 core {@code UserZoneProvider} 로
 * 확장한다({@code company.timezone} 컬럼은 이미 그 형태로 준비돼 있다).
 *
 * <p><b>왜 필요한가</b> — 컨테이너에 TZ 가 없으면 JVM 기본이 UTC 다. 그대로
 * {@code LocalDate.now()} 를 쓰면 한국 기준 오전 9시 전까지 "오늘" 이 하루 전으로 잡혀
 * 휴가가 하루 늦게 만료되고 근무일이 어긋난다.
 *
 * <p>회사 타임존은 바뀌는 일이 드물어 최초 1회 조회 후 캐시한다(변경 시 재기동 필요).
 * 조회 실패·미설정이면 {@link ServiceClock}(프로퍼티 {@code app.scheduler.zone}) 으로 폴백한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CompanyClock {

    private final CompanyRepository companyRepository;
    private final ServiceClock serviceClock;

    private volatile ZoneId cached;

    /** 회사 기준 타임존. */
    public ZoneId zone() {
        ZoneId zone = cached;
        if (zone == null) {
            synchronized (this) {
                zone = cached;
                if (zone == null) {
                    zone = resolve();
                    cached = zone;
                }
            }
        }
        return zone;
    }

    /** 회사 기준 오늘 날짜. */
    public LocalDate today() {
        return LocalDate.now(zone());
    }

    /** 회사 기준 현재 일시. */
    public LocalDateTime now() {
        return LocalDateTime.now(zone());
    }

    private ZoneId resolve() {
        try {
            return companyRepository.find()
                .map(Company::getTimezone)
                .map(this::parseOrNull)
                .filter(java.util.Objects::nonNull)
                .orElseGet(() -> {
                    log.info("회사 타임존 미설정 — 서비스 기준({})으로 동작", serviceClock.zone());
                    return serviceClock.zone();
                });
        } catch (Exception e) {
            // 기동 직후 DB 미준비 등 — 타임존 하나 때문에 요청이 죽으면 안 된다.
            log.warn("회사 타임존 조회 실패 — 서비스 기준({})으로 폴백", serviceClock.zone(), e);
            return serviceClock.zone();
        }
    }

    private ZoneId parseOrNull(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return null;
        }
        try {
            return ZoneId.of(timezone.trim());
        } catch (DateTimeException e) {
            log.warn("알 수 없는 회사 타임존 '{}' — 서비스 기준으로 폴백", timezone);
            return null;
        }
    }
}
