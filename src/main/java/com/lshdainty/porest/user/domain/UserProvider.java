package com.lshdainty.porest.user.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "user_provider")
public class UserProvider extends AuditingFields {
    /**
     * provider 시퀀스<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "provider_seq", columnDefinition = "bigint(20) COMMENT 'provider 시퀀스'")
    private Long seq;

    /**
     * 사용자 객체<br>
     * 테이블 컬럼은 user_id<br>
     * 어떤 유저의 OAuth 정보인지 알기 위해 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * provider 타입<br>
     * OAuth 제공자 종류 (예: GOOGLE, KAKAO, NAVER 등)
     */
    @Column(name = "provider_type", nullable = false, length = 10, columnDefinition = "varchar(10) NOT NULL COMMENT 'provider 타입'")
    private String type;

    /**
     * OAuth 제공자 사용자 아이디<br>
     * OAuth 제공자에서 발급한 고유 사용자 식별자
     */
    @Column(name = "provider_user_id", length = 100, columnDefinition = "varchar(100) COMMENT 'OAuth 제공자 사용자 아이디'")
    private String id;

    // user 추가 연관관계 편의 메소드
    public void addUser(User user) {
        this.user = user;
        user.getProviders().add(this);
    }

    /**
     * OAuth 제공자 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 OAuth 제공자 정보 생성할 것
     *
     * @param user 유저 객체
     * @param type OAuth 제공자 타입
     * @param id OAuth 제공자 사용자 아이디
     * @return UserProvider
     */
    public static UserProvider createProvider(User user, String type, String id) {
        UserProvider provider = new UserProvider();
        provider.addUser(user);
        provider.type = type;
        provider.id = id;
        return provider;
    }
}
