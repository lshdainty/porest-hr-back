package com.lshdainty.porest.vacation.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "user_vacation_policy")
public class UserVacationPolicy extends AuditingFields {
    /**
     * 사용자-휴가 정책 매핑 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_vacation_policy_id")
    private Long id;

    /**
     * 사용자 객체<br>
     * 테이블 컬럼은 user_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private User user;

    /**
     * 휴가 정책 객체<br>
     * 테이블 컬럼은 vacation_policy_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_policy_id", nullable = false)
    @Setter
    private VacationPolicy vacationPolicy;

    /**
     * 마지막 휴가 부여 일시<br>
     * policy_type이 repeat(반복)인 경우 스케줄러 실행으로<br>
     * 휴가를 부여하는데 중복 부여 방지를 위한 컬럼
     */
    @Column(name = "last_granted_at")
    private LocalDateTime lastGrantedAt;

    /**
     * 다음 휴가 부여 일자<br>
     * 스케줄러 조회 최적화용 컬럼<br>
     * (인덱스 추가 예정)
     */
    @Column(name = "next_grant_date")
    private LocalDate nextGrantDate;

    /**
     * 삭제 여부
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1)
    private YNType isDeleted;

    // user 추가 연관관계 편의 메소드
    public void addUser(User user) {
        this.user = user;
        user.getUserVacationPolicies().add(this);
    }

    // vacationPolicy 추가 연관관계 편의 메소드
    public void addVacationPolicy(VacationPolicy vacationPolicy) {
        this.vacationPolicy = vacationPolicy;
        vacationPolicy.getUserVacationPolicies().add(this);
    }

    /**
     * 유저 휴가정책 추가 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 휴가정책 추가할 것
     *
     * @return UserVacationPolicy
     */
    public static UserVacationPolicy createUserVacationPolicy(User user, VacationPolicy vacationPolicy) {
        UserVacationPolicy userVacationPolicy = new UserVacationPolicy();
        userVacationPolicy.addUser(user);
        userVacationPolicy.addVacationPolicy(vacationPolicy);
        userVacationPolicy.isDeleted = YNType.N;
        return userVacationPolicy;
    }

    /**
     * 유저 휴가정책 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 휴가정책 삭제할 것
     */
    public void deleteUserVacationPolicy() {
        this.isDeleted = YNType.Y;
    }

    /**
     * 휴가 부여 후 부여 이력 업데이트<br>
     * 스케줄러에서 휴가 부여 후 마지막 부여 시점과 다음 부여 예정일을 갱신할 때 사용
     *
     * @param lastGrantedAt 마지막 부여 시점
     * @param nextGrantDate 다음 부여 예정일
     */
    public void updateGrantHistory(LocalDateTime lastGrantedAt, LocalDate nextGrantDate) {
        this.lastGrantedAt = lastGrantedAt;
        this.nextGrantDate = nextGrantDate;
    }
}
