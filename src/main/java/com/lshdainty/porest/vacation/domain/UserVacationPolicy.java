package com.lshdainty.porest.vacation.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@ToString
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "user_vacation_policy")
public class UserVacationPolicy extends AuditingFields {
    @Id @GeneratedValue
    @Column(name = "user_vacation_policy_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Setter
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_policy_id")
    @Setter
    private VacationPolicy vacationPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn")
    private YNType delYN;                   // 삭제여부

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
        userVacationPolicy.delYN = YNType.N;
        return userVacationPolicy;
    }

    /**
     * 유저 휴가정책 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 휴가정책 삭제할 것
     */
    public void deleteUserVacationPolicy() {
        this.delYN = YNType.Y;
    }
}
