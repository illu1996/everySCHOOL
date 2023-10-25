package com.everyschool.userservice.api.service.user;

import com.everyschool.userservice.IntegrationTestSupport;
import com.everyschool.userservice.api.controller.user.response.UserResponse;
import com.everyschool.userservice.api.controller.user.response.WithdrawalResponse;
import com.everyschool.userservice.api.service.user.dto.CreateUserDto;
import com.everyschool.userservice.api.service.user.exception.DuplicateException;
import com.everyschool.userservice.domain.user.User;
import com.everyschool.userservice.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UserServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @DisplayName("이미 사용 중인 이메일이라면 예외가 발생한다.")
    @Test
    void createUserDuplicateEmail() {
        //given
        User user = saveUser();

        CreateUserDto dto = CreateUserDto.builder()
            .userCodeId(1)
            .email("ssafy@gmail.com")
            .pwd("ssafy5678@")
            .name("이싸피")
            .birth("2002-02-02")
            .build();

        //when //then
        assertThatThrownBy(() -> userService.createUser(dto))
            .isInstanceOf(DuplicateException.class)
            .hasMessage("이미 사용 중인 이메일 입니다.");
    }

    @DisplayName("회원 정보를 입력 받아 회원을 생성한다.")
    @Test
    void createUser() {
        //given
        CreateUserDto dto = CreateUserDto.builder()
            .userCodeId(1)
            .email("ssafy@gmail.com")
            .pwd("ssafy1234@")
            .name("김싸피")
            .birth("2001-01-01")
            .build();

        //when
        UserResponse response = userService.createUser(dto);

        //then
        assertThat(response.getEmail()).isEqualTo("ssafy@gmail.com");
    }

    @DisplayName("비밀번호 변경 시 입력 받은 이메일을 사용하는 회원이 존재하지 않으면 예외가 발생한다.")
    @Test
    void editPwdWithoutUser() {
        //given

        //when //then
        assertThatThrownBy(() -> userService.editPwd("ssafy@gmail.com", "ssafy1234@", "ssafy1111@"))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("이메일을 확인해주세요.");
    }

    @DisplayName("비밀번호 변경 시 입력 받은 현재 비밀번호가 일치하지 않으면 예외가 발생한다.")
    @Test
    void editPwdNotMatchCurrentPwd() {
        //given
        User user = saveUser();

        //when //then
        assertThatThrownBy(() -> userService.editPwd("ssafy@gmail.com", "ssafy5678@", "ssafy1111@"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("현재 비밀번호가 일치하지 않습니다.");
    }

    @DisplayName("이메일, 현재 비밀번호, 새로운 비밀번호를 입력 받아 계정 비밀번호를 수정한다.")
    @Test
    void editPwd() {
        //given
        User user = saveUser();

        //when
        UserResponse response = userService.editPwd("ssafy@gmail.com", "ssafy1234@", "ssafy1111@");

        //then
        assertThat(response.getEmail()).isEqualTo("ssafy@gmail.com");
    }

    @DisplayName("회원 탈퇴 시 입력 받은 이메일을 사용하는 회원이 존재하지 않으면 예외가 발생한다.")
    @Test
    void withdrawalWithoutUser() {
        //given

        //when //then
        assertThatThrownBy(() -> userService.withdrawal("ssafy@gmail.com", "ssafy1234@"))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("이메일을 확인해주세요.");
    }

    @DisplayName("회원 탈퇴 시 입력 받은 비밀번호가 일치하지 않으면 예외가 발생한다.")
    @Test
    void withdrawalNotMatchPwd() {
        //given
        User user = saveUser();

        //when //then
        assertThatThrownBy(() -> userService.withdrawal("ssafy@gmail.com", "ssafy5678@"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("현재 비밀번호가 일치하지 않습니다.");
    }

    @DisplayName("이메일과 비밀번호를 입력 받아 회원 탈퇴를 한다.")
    @Test
    void withdrawal() {
        //given
        User user = saveUser();

        //when
        WithdrawalResponse response = userService.withdrawal("ssafy@gmail.com", "ssafy1234@");

        //then
        Optional<User> findUser = userRepository.findById(user.getId());
        assertThat(findUser).isPresent();
        assertThat(findUser.get().isDeleted()).isTrue();
    }

    private User saveUser() {
        User user = User.builder()
            .email("ssafy@gmail.com")
            .pwd(passwordEncoder.encode("ssafy1234@"))
            .name("김싸피")
            .birth("2001-01-01")
            .userKey(UUID.randomUUID().toString())
            .userCodeId(1)
            .build();
        return userRepository.save(user);
    }
}