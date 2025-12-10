package com.lshdainty.porest.service;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.permission.domain.Permission;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.type.ActionType;
import com.lshdainty.porest.permission.type.ResourceType;
import com.lshdainty.porest.security.service.CustomUserDetailsService;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService 테스트")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadUserByUsername {
        @Test
        @DisplayName("성공 - 유저를 반환한다")
        void loadUserByUsernameSuccess() {
            // given
            String username = "user1";
            User user = User.createUser(username, "password", "테스트유저", "test@test.com",
                    LocalDate.of(1990, 1, 1), OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findByIdWithRolesAndPermissions(username)).willReturn(Optional.of(user));

            // when
            UserDetails result = customUserDetailsService.loadUserByUsername(username);

            // then
            assertThat(result.getUsername()).isEqualTo(username);
            assertThat(result.getPassword()).isEqualTo("password");
            assertThat(result.isAccountNonExpired()).isTrue();
            assertThat(result.isAccountNonLocked()).isTrue();
            assertThat(result.isCredentialsNonExpired()).isTrue();
            assertThat(result.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("성공 - 역할과 권한이 포함된 유저를 반환한다")
        void loadUserByUsernameWithRolesAndPermissions() {
            // given
            String username = "user1";
            User user = User.createUser(username, "password", "테스트유저", "test@test.com",
                    LocalDate.of(1990, 1, 1), OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);

            // 역할 및 권한 설정
            Permission permission = Permission.createPermission("USER:READ", "사용자 조회", "desc", ResourceType.USER, ActionType.READ);
            Role role = Role.createRoleWithPermissions("ADMIN", "관리자", "관리자 역할", List.of(permission));
            user.addRole(role);

            given(userRepository.findByIdWithRolesAndPermissions(username)).willReturn(Optional.of(user));

            // when
            UserDetails result = customUserDetailsService.loadUserByUsername(username);

            // then
            assertThat(result.getUsername()).isEqualTo(username);
            List<String> authorities = result.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            assertThat(authorities).contains("ADMIN");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저면 예외가 발생한다")
        void loadUserByUsernameFailNotFound() {
            // given
            String username = "nonexistent";
            given(userRepository.findByIdWithRolesAndPermissions(username)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(username))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining(username);
        }
    }

    @Nested
    @DisplayName("CustomUserDetails")
    class CustomUserDetailsTest {
        @Test
        @DisplayName("성공 - UserDetails 메서드들이 올바르게 동작한다")
        void customUserDetailsMethodsWork() {
            // given
            String username = "user1";
            User user = User.createUser(username, "testPassword", "테스트유저", "test@test.com",
                    LocalDate.of(1990, 1, 1), OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findByIdWithRolesAndPermissions(username)).willReturn(Optional.of(user));

            // when
            UserDetails result = customUserDetailsService.loadUserByUsername(username);

            // then
            assertThat(result.getUsername()).isEqualTo(username);
            assertThat(result.getPassword()).isEqualTo("testPassword");
            assertThat(result.isAccountNonExpired()).isTrue();
            assertThat(result.isAccountNonLocked()).isTrue();
            assertThat(result.isCredentialsNonExpired()).isTrue();
            assertThat(result.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("성공 - getUser 메서드가 User 객체를 반환한다")
        void customUserDetailsGetUser() {
            // given
            String username = "user1";
            User user = User.createUser(username, "password", "테스트유저", "test@test.com",
                    LocalDate.of(1990, 1, 1), OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findByIdWithRolesAndPermissions(username)).willReturn(Optional.of(user));

            // when
            CustomUserDetailsService.CustomUserDetails result =
                    (CustomUserDetailsService.CustomUserDetails) customUserDetailsService.loadUserByUsername(username);

            // then
            assertThat(result.getUser()).isEqualTo(user);
            assertThat(result.getUser().getName()).isEqualTo("테스트유저");
        }

        @Test
        @DisplayName("성공 - 권한이 없는 유저는 빈 authorities를 반환한다")
        void customUserDetailsEmptyAuthorities() {
            // given
            String username = "user1";
            User user = User.createUser(username, "password", "테스트유저", "test@test.com",
                    LocalDate.of(1990, 1, 1), OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findByIdWithRolesAndPermissions(username)).willReturn(Optional.of(user));

            // when
            UserDetails result = customUserDetailsService.loadUserByUsername(username);

            // then
            assertThat(result.getAuthorities()).isEmpty();
        }
    }
}
