//package com.lshdainty.myhr.lib.jwt;
//
//import com.lshdainty.myhr.domain.User;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
//@RequiredArgsConstructor
//public class CustomUserDetails implements UserDetails {
//    // userNo를 통해 조회한 user가 저장
//    private final User user;
//
//    // 권한을 조회하여 반환하는 함수
//    // 권한 기능이 없으면 기본값 return 할 것
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        Collection<GrantedAuthority> authorities = new ArrayList<>();
//
//        authorities.add(new GrantedAuthority() {
//            @Override
//            public String getAuthority() {
////                return user.getRole();    // 추후 권한 관련 기능이 추가되면 해당부분 수정할 것
//                return "";
//            }
//        });
//
//        return authorities;
//    }
//
//    @Override
//    public String getPassword() {
//        return user.getPwd();
//    }
//
//    @Override
//    public String getUsername() {
//        return "";
//    }
//
//    public Long getUserNo() {
//        return user.getId();
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return UserDetails.super.isAccountNonExpired();
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return UserDetails.super.isAccountNonLocked();
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return UserDetails.super.isCredentialsNonExpired();
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return UserDetails.super.isEnabled();
//    }
//}
