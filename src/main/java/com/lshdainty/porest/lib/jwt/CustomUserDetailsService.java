//package com.lshdainty.myhr.lib.jwt;
//
//import com.lshdainty.myhr.domain.User;
//import com.lshdainty.myhr.repository.UserRepositoryImpl;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.MessageSource;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.Objects;
//
//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//    private final UserRepositoryImpl userRepositoryImpl;
//    private final MessageSource ms;
//
//    @Override
//    public UserDetails loadUserByUsername(String userNo) throws UsernameNotFoundException {
//        User user = userRepositoryImpl.findById(Long.valueOf(userNo));
//
//        if (Objects.isNull(user)) {
//            throw new UsernameNotFoundException(ms.getMessage("error.notfound.user", null, null));
//        }
//
//        return new CustomUserDetails(user);
//    }
//}
