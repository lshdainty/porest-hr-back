package com.lshdainty.porest.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

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

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "vacation_policy", cascade = CascadeType.ALL)
    private List<VacationPolicy> policies =  new ArrayList<>();
}
