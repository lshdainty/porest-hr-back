package com.lshdainty.porest.domain;

import com.lshdainty.porest.type.VacationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ToString
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "vacation")
public class Vacation extends AuditingFields {
    @Id @GeneratedValue
    @Column(name = "vacation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Setter
    private User user;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "vacation", cascade = CascadeType.ALL)
    private List<VacationHistory> historys =  new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "vacation_type")
    private VacationType type;

    @Column(name = "remain_time", precision = 7, scale = 4)
    private BigDecimal remainTime;

    @Column(name = "occur_date")
    private LocalDateTime occurDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    // user 추가 연관관계 편의 메소드
    public void addUser(User user) {
        this.user = user;
        user.getVacations().add(this);
    }

    /**
     * 휴가 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 생성할 것
     *
     * @return Vacation
     */
    public static Vacation createVacation(User user, VacationType type, BigDecimal grantTime, LocalDateTime occurDate, LocalDateTime expiryDate, String crtUserId, String clientIP) {
        Vacation vacation = new Vacation();
        vacation.addUser(user);
        vacation.type = type;
        vacation.remainTime = grantTime;
        vacation.occurDate = occurDate;
        vacation.expiryDate = expiryDate;
        vacation.setCreated(crtUserId, clientIP);
        vacation.setModified(crtUserId, clientIP);
        return vacation;
    }

    /* 비즈니스 편의 메소드 */
    /**
     * 휴가 추가 메소드<br>
     * remainTime(잔여시간)에 grantTime(추가시간)을 더함
     */
    public void addVacation(BigDecimal grantTime, String mdfUserId, String clientIP) {
        this.remainTime =  getRemainTime().add(grantTime);
        this.setModified(LocalDateTime.now(), mdfUserId, clientIP);
    }

    /**
     * 휴가 차감 메소드<br>
     * remainTime(잔여시간)에서 deductTime을(사용시간)을 뺌
     */
    public void deductedVacation(BigDecimal deductTime, String mdfUserId, String clientIP) {
        this.remainTime =  getRemainTime().subtract(deductTime);
        this.setModified(LocalDateTime.now(), mdfUserId, clientIP);
    }

    /**
     * occurDate, expireDate를 비교하여<br>
     * 발생일자가 만료일자 이전인지 확인
     *
     * @return true, false
     */
    public boolean isBeforeOccur() {
        return !getOccurDate().isBefore(getExpiryDate());
    }
}
