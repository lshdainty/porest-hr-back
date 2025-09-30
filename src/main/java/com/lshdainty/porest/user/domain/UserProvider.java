package com.lshdainty.porest.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "users_provider")
public class UserProvider {
    @Id @GeneratedValue
    @Column(name = "provider_seq")
    private Long seq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "provider_type")
    private String type;

    @Column(name = "provider_user_id")
    private String id;

    // user 추가 연관관계 편의 메소드
    public void addUser(User user) {
        this.user = user;
        user.getProviders().add(this);
    }

    public static UserProvider createProvider(User user, String type, String id) {
        UserProvider provider = new UserProvider();
        provider.addUser(user);
        provider.type = type;
        provider.id = id;
        return provider;
    }
}
