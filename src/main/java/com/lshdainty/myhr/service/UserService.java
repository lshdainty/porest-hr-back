package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.User;
import com.lshdainty.myhr.repository.UserRepositoryImpl;
import com.lshdainty.myhr.service.dto.UserServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    private final MessageSource ms;
    private final UserRepositoryImpl userRepositoryImpl;

    @Transactional
    public String join(UserServiceDto data) {
        User user = User.createUser(
                data.getId(),
                data.getPwd(),
                data.getName(),
                data.getEmail(),
                data.getBirth(),
                data.getEmploy(),
                data.getWorkTime(),
                data.getLunarYN()
        );
        userRepositoryImpl.save(user);
        return user.getId();
    }

    public User findUser(String userId) {
        return checkUserExist(userId);
    }

    public List<User> findUsers() {
        return userRepositoryImpl.findUsers();
    }

    @Transactional
    public void editUser(UserServiceDto data) {
        User user = checkUserExist(data.getId());
        user.updateUser(
                data.getName(),
                data.getEmail(),
                data.getBirth(),
                data.getEmploy(),
                data.getWorkTime(),
                data.getLunarYN(),
                data.getRole()
        );
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = checkUserExist(userId);
        user.deleteUser();
    }

    public User checkUserExist(String userId) {
        Optional<User> findUser = userRepositoryImpl.findById(userId);
        if ((findUser.isEmpty()) || findUser.get().getDelYN().equals("Y")) { throw new IllegalArgumentException(ms.getMessage("error.notfound.user", null, null)); }
        return findUser.get();
    }
}