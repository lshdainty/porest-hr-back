package com.porest.hr.common.audit;

import com.porest.core.audit.AccessLogEntry;
import com.porest.core.audit.AuditAccessPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 접속기록 저장 포트 구현<br>
 * core 의 {@code AuditAccessAspect} 가 이 구현을 통해 기록을 남긴다
 *
 * <h3>REQUIRES_NEW 를 쓰는 이유</h3>
 * <p>
 * 본 작업 트랜잭션에 얹으면 본 작업이 롤백될 때 접속기록도 함께 사라진다.
 * "개인정보에 접근했다" 는 사실은 그 뒤 처리가 실패했더라도 남아야 한다.
 *
 * <h3>실패를 삼키는 이유</h3>
 * <p>
 * 감사 기록 저장이 실패했다고 사용자 요청까지 깨뜨리면 본말이 전도된다.
 * 대신 warn 으로 남겨 누락을 추적할 수 있게 한다.
 *
 * @see AuditAccessPort core 포트 정의
 * @see AccessLog 저장 엔티티
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessLogPortImpl implements AuditAccessPort {

    private final AccessLogRepository accessLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AccessLogEntry entry) {
        try {
            accessLogRepository.save(AccessLog.from(entry));
        } catch (Exception e) {
            log.warn("접속기록 저장 실패: action={}, targetType={}, targetId={}",
                    entry.action(), entry.targetType(), entry.targetId(), e);
        }
    }
}
