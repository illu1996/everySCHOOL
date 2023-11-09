package com.everyschool.callservice.api.service.usercall;

import com.everyschool.callservice.IntegrationTestSupport;
import com.everyschool.callservice.api.client.UserServiceClient;
import com.everyschool.callservice.api.client.response.UserInfo;
import com.everyschool.callservice.api.controller.usercall.response.UserCallResponse;
import com.everyschool.callservice.api.service.usercall.dto.CreateUserCallDto;
import com.everyschool.callservice.domain.usercall.UserCall;
import com.everyschool.callservice.domain.usercall.repository.UserCallRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class UserCallServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserCallService userCallService;

    @Autowired
    private UserCallRepository userCallRepository;

    @MockBean
    private UserServiceClient userServiceClient;

    @DisplayName("통화 종료후 DB에 저장")
    @Test
    void createCallInfo() {

        // given
        UserInfo teacher = UserInfo.builder()
                .userId(1L)
                .userType('T')
                .userName("신성주")
                .schoolClassId(1L)
                .build();

        UserInfo parent = UserInfo.builder()
                .userId(2L)
                .userType('M')
                .userName("홍경환")
                .schoolClassId(1L)
                .build();

        given(userServiceClient.searchUserInfo("jwt"))
                .willReturn(teacher);
        given(userServiceClient.searchUserInfoByUserKey("otheruserkey"))
                .willReturn(parent);

        CreateUserCallDto dto = CreateUserCallDto.builder()
                .sender("O")
                .startDateTime(LocalDateTime.now().minusHours(2))
                .endDateTime(LocalDateTime.now().minusHours(1))
                .uploadFileName("이예리 폭언 녹음본")
                .storeFileName("이예리 폭언 녹음본")
                .isBad(true)
                .build();

        // when
        UserCallResponse response = userCallService.createCallInfo(dto, "otheruserkey", "jwt");

        // then
        assertThat(response.getSenderName()).isEqualTo("홍경환");
        assertThat(response.getReceiverName()).isEqualTo("신성주");
    }

    private UserCall saveCall() {
        UserCall userCall = UserCall.builder()
                .teacherId(1L)
                .otherUserId(2L)
                .sender("O")
                .startDateTime(LocalDateTime.now().minusHours(2))
                .endDateTime(LocalDateTime.now().minusHours(1))
                .uploadFileName("이예리 폭언 녹음본")
                .storeFileName("이예리 폭언 녹음본")
                .isBad(true)
                .build();
        return userCallRepository.save(userCall);
    }
}