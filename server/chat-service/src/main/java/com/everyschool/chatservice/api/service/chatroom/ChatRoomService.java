package com.everyschool.chatservice.api.service.chatroom;

import com.everyschool.chatservice.api.client.SchoolServiceClient;
import com.everyschool.chatservice.api.client.UserServiceClient;
import com.everyschool.chatservice.api.client.response.SchoolClassInfo;
import com.everyschool.chatservice.api.client.response.UserInfo;
import com.everyschool.chatservice.api.controller.chatroom.response.CreateChatRoomResponse;
import com.everyschool.chatservice.api.service.chatroom.dto.CreateChatRoomDto;
import com.everyschool.chatservice.api.service.util.RedisUtils;
import com.everyschool.chatservice.domain.chatroom.ChatRoom;
import com.everyschool.chatservice.domain.chatroom.repository.ChatRoomRepository;
import com.everyschool.chatservice.domain.chatroomuser.ChatRoomUser;
import com.everyschool.chatservice.domain.chatroomuser.repository.ChatRoomUserQueryRepository;
import com.everyschool.chatservice.domain.chatroomuser.repository.ChatRoomUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatRoomUserQueryRepository chatRoomUserQueryRepository;

    private final UserServiceClient userServiceClient;
    private final SchoolServiceClient schoolServiceClient;

    private final RedisUtils redisUtil;

    /**
     * 채팅방 생성
     *
     * @param dto
     * @return 채팅방 Id, 채팅방 이름, 상대 유저 이름
     */
    public CreateChatRoomResponse createChatRoom(CreateChatRoomDto dto) {

        // 로그인 한 회원 정보 요청
        UserInfo loginUser = userServiceClient.searchUserInfo(dto.getLoginUserToken());
        log.debug("[채팅방 생성 Service] 로그인 유저 = {}", loginUser.getUserName());
        // 상대 유저키로 정보 요청
        UserInfo opponentUser = userServiceClient.searchUserInfoByUserKey(dto.getOpponentUserKey());
        log.debug("[채팅방 생성 Service] 상대방 유저 = {}", opponentUser.getUserName());

        Optional<Long> findRoom = chatRoomUserQueryRepository.findChatRoomIdByTwoUserId(loginUser.getUserId(), opponentUser.getUserId());
        if (findRoom.isPresent()) {
            return createChatRoomResponse(dto, opponentUser, findRoom);
        }

        // 채팅방 생성
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder().build());
        // 채팅방 유저 생성
        ChatRoomUser opponentRoom = createChatRoomUser(loginUser.getUserName(), loginUser, dto.getSchoolClassId(), opponentUser.getUserId(), chatRoom);
        ChatRoomUser loginUserRoom = createChatRoomUser(opponentUser.getUserName(), opponentUser, dto.getSchoolClassId(), loginUser.getUserId(), chatRoom);

        chatRoomUserRepository.save(loginUserRoom);
        chatRoomUserRepository.save(opponentRoom);

        return CreateChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .opponentUserName(opponentUser.getUserName())
                .opponentUserType(opponentUser.getUserType())
                .opponentUsersChildName(opponentRoom.getChildName())
                .build();
    }

    private CreateChatRoomResponse createChatRoomResponse(CreateChatRoomDto dto, UserInfo opponentUser, Optional<Long> findRoom) {
        Long findRoomId = findRoom.get();
        String childName = "";
        if (opponentUser.getUserType() == 'M' || opponentUser.getUserType() == 'F') {
            // 부모와 학급키로 학급에 다니는 자녀 이름 요청
            childName = userServiceClient.searchUsername(dto.getSchoolClassId(), opponentUser.getUserId());
        }
        return CreateChatRoomResponse.builder()
                .roomId(findRoomId)
                .opponentUserName(opponentUser.getUserName())
                .opponentUserType(opponentUser.getUserType())
                .opponentUsersChildName(childName)
                .build();
    }

    /**
     * 채팅방 입장시 채팅방에 있는 인원 증가
     *
     * @param chatRoomId
     * @param userId
     */
    public void connectChatRoom(Long chatRoomId, Long userId) {
        log.debug("[소켓 연결] 레디스. 채팅방 번호 = {}", chatRoomId);
        String chatRoomUserCountKey = "CHAT_ROOM_USER_COUNT_" + chatRoomId;
        String roomUserCount = redisUtil.getString(chatRoomUserCountKey);
        if (roomUserCount == null) {
            roomUserCount = String.valueOf(0);
        }
        int count = Integer.parseInt(roomUserCount) + 1;
        redisUtil.insertString(chatRoomUserCountKey, String.valueOf(count));

        //채팅 읽음 처리
        ChatRoomUser chatRoomUser = chatRoomUserQueryRepository.findChatRoomUserByRoomIdAndUserId(chatRoomId, userId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));
        chatRoomUser.read();
    }

    /**
     * 채팅방 유저 Entity 생성
     *
     * @param title
     * @param opponentUser
     * @param schoolClassId
     * @param userId
     * @param chatRoom
     * @return
     */
    private ChatRoomUser createChatRoomUser(String title, UserInfo opponentUser, Long schoolClassId, Long userId, ChatRoom chatRoom) {
        String childName = "";
        if (opponentUser.getUserType() == 'M' || opponentUser.getUserType() == 'F') {
            // 부모와 학급키로 학급에 다니는 자녀 이름 요청
            childName = userServiceClient.searchUsername(schoolClassId, opponentUser.getUserId());
        }
        return ChatRoomUser.builder()
                .chatRoomTitle(generateChatRoomTitle(opponentUser, schoolClassId))
                .childName(childName)
                .userId(userId)
                .opponentUserType(String.valueOf(opponentUser.getUserType()))
                .isAlarm(true)
                .unreadCount(0)
                .chatRoom(chatRoom)
                .build();
    }

    private String generateChatRoomTitle(UserInfo opponentUser, Long schoolClassId) {
        StringBuilder sb = new StringBuilder();
        SchoolClassInfo schoolClassInfo = schoolServiceClient.searchSchoolClassInfo(schoolClassId);

        char opponentUserUserType = opponentUser.getUserType();
        if (opponentUserUserType == 'M' || opponentUserUserType == 'F') {
            // 부모
            String childName = userServiceClient.searchUsername(schoolClassId, opponentUser.getUserId());
            sb.append(schoolClassInfo.getClassName())
                    .append(childName);
            if (opponentUserUserType == 'M') {
                sb.append(" 아버지");
            } else {
                sb.append(" 어머니");
            }
        } else if (opponentUserUserType == 'S') {
            // 학생
            sb.append(schoolClassInfo.getClassName())
                    .append(opponentUser.getUserName())
                    .append(" 학생");
        } else {
            // 교사
            sb.append(schoolClassInfo.getClassName())
                    .append(opponentUser.getUserName())
                    .append(" 선생님");
        }
        return sb.toString();
    }

    public void disconnect(Long chatRoomId) {
        log.debug("[소켓] 인원수 감소. ChatRoomService, disconnect. 채팅방 Id = {}", chatRoomId);
        String chatRoomUserCountKey = "CHAT_ROOM_USER_COUNT_" + chatRoomId;
        String roomUserCount = redisUtil.getString(chatRoomUserCountKey);
        log.debug("[소켓] 지금 채팅방 인원 수 = {}", roomUserCount);
        if (roomUserCount == null) {
            roomUserCount = String.valueOf(0);
        }
        int count = Integer.parseInt(roomUserCount) - 1;
        redisUtil.insertString(chatRoomUserCountKey, String.valueOf(count));
        log.debug("[소켓] 감소 후 채팅방 인원 수 = {}", redisUtil.getString(chatRoomUserCountKey));
    }
}
