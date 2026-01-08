package com.lshdainty.porest.security.service;

import com.lshdainty.porest.common.type.DefaultCompanyType;
import com.lshdainty.porest.security.principal.UserPrincipal;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import com.lshdainty.porest.permission.domain.Role;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 사용자 id를 기반으로 한 유저 권한 정보 및 역할 조회
        User user = userRepository.findByIdWithRolesAndPermissions(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                });

        // SYSTEM 유저는 모니터링 도구(Prometheus 등)이므로 info 로그 생략
        if (!DefaultCompanyType.SYSTEM.equals(user.getCompany())) {
            log.info("Attempting to load user: {}", username);
            log.info("User found: {}, Roles: {}, Authorities: {}",
                    user.getId(),
                    user.getRoles().stream().map(Role::getCode).collect(Collectors.joining(", ")),
                    String.join(", ", user.getAllAuthorities()));
        }

        return new CustomUserDetails(user);
    }

    // Spring Security UserDetails 구현
    public static class CustomUserDetails implements UserDetails, UserPrincipal {
        private final User user;

        public CustomUserDetails(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // User의 모든 권한 조회 (Role 코드 + Permission 코드)
            // 예: ["ADMIN", "USER:READ", "USER:EDIT", "VACATION:REQUEST", ...]
            return user.getAllAuthorities().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        }

        @Override
        public String getPassword() {
            return user.getPwd();
        }

        @Override
        public String getUsername() {
            return user.getId();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        // User 객체 반환 메서드 (UserPrincipal 인터페이스 구현)
        @Override
        public User getUser() {
            return user;
        }
    }
}
