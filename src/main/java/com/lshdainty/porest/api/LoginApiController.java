//package com.lshdainty.myhr.api;
//
//import com.lshdainty.myhr.SessionConst;
//import com.lshdainty.myhr.domain.User;
//import com.lshdainty.myhr.dto.LoginDto;
//import com.lshdainty.myhr.service.UserService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpSession;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Objects;
//
//@RestController
//@RequiredArgsConstructor
//@Slf4j
//public class LoginApiController {
//    private final UserService userService;
//
//    @PostMapping("/login")
//    public ApiResponse login(@RequestBody LoginDto loginDto, HttpServletRequest req) {
//        User user = userService.findUser(loginDto.getId());
//
//        if (Objects.isNull(user)) {
//            throw new IllegalArgumentException("Invalid username or password");
//        }
//
//        req.getSession().setAttribute(SessionConst.LOGIN_MEMBER, user);
//
//        return ApiResponse.success();
//    }
//
//    @PostMapping("/logout")
//    public ApiResponse logout(HttpServletRequest req) {
//        HttpSession session = req.getSession(false);
//        if (Objects.nonNull(session)) { session.invalidate(); }
//        return ApiResponse.success();
//    }
//
//    @PostMapping("/jwt/convert/pw")
//    public ApiResponse jwtConvert(@RequestBody LoginDto loginDto, HttpServletRequest req) {
//        log.info("JWT convert request: {}", loginDto);
//
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        String encodePw = encoder.encode(loginDto.getPw());
//
//        return ApiResponse.success(encodePw);
//    }
//
//}
