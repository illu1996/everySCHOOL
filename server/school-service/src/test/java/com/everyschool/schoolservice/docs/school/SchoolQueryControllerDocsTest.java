package com.everyschool.schoolservice.docs.school;

import com.everyschool.schoolservice.api.controller.school.SchoolQueryController;
import com.everyschool.schoolservice.api.controller.school.response.SchoolResponse;
import com.everyschool.schoolservice.api.service.school.SchoolQueryService;
import com.everyschool.schoolservice.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SchoolQueryControllerDocsTest extends RestDocsSupport {

    private final SchoolQueryService schoolQueryService = mock(SchoolQueryService.class);

    @Override
    protected Object initController() {
        return new SchoolQueryController(schoolQueryService);
    }

    @DisplayName("학교 리스트 조회 API")
    @Test
    void searchSchools() throws Exception {
        SchoolResponse response1 = SchoolResponse.builder()
            .schoolId(1L)
            .name("수완고등학교")
            .address("광주광역시 광산구 장덕로 155")
            .build();

        SchoolResponse response2 = SchoolResponse.builder()
            .schoolId(2L)
            .name("수완중학교")
            .address("광주광역시 광산구 수완로 19")
            .build();

        SchoolResponse response3 = SchoolResponse.builder()
            .schoolId(3L)
            .name("수완초등학교")
            .address("광주광역시 광산구 장덕로 143")
            .build();

        SchoolResponse response4 = SchoolResponse.builder()
            .schoolId(4L)
            .name("수완하나중학교")
            .address("광주광역시 광산구 수완로105번길 47")
            .build();

        List<SchoolResponse> responses = List.of(response1, response2, response3, response4);

        given(schoolQueryService.searchSchools("수완"))
            .willReturn(responses);

        mockMvc.perform(
                get("/school-service/v1/schools")
                    .param("query", "수완")
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("search-list-school",
                preprocessResponse(prettyPrint()),
                requestParameters(
                    parameterWithName("query")
                        .optional()
                        .description("조회할 학교명")
                ),
                responseFields(
                    fieldWithPath("code").type(JsonFieldType.NUMBER)
                        .description("코드"),
                    fieldWithPath("status").type(JsonFieldType.STRING)
                        .description("상태"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("메시지"),
                    fieldWithPath("data").type(JsonFieldType.ARRAY)
                        .description("응답 데이터"),
                    fieldWithPath("data[].schoolId").type(JsonFieldType.NUMBER)
                        .description("학교 Id"),
                    fieldWithPath("data[].name").type(JsonFieldType.STRING)
                        .description("학교명"),
                    fieldWithPath("data[].address").type(JsonFieldType.STRING)
                        .description("학교 주소")
                )
            ));
    }

    @DisplayName("학교 코드로 학교 단건 조회 API")
    @Test
    void searchOneSchool() throws Exception {

        SchoolResponse response = SchoolResponse.builder()
            .schoolId(Long.parseLong("12345"))
            .name("경기수완중학교")
            .address("경기도")
            .build();

        given(schoolQueryService.searchOneSchool(Long.parseLong("12345")))
            .willReturn(response);

        mockMvc.perform(
                get("/school-service/v1/school/{schoolId}", Long.parseLong("12345"))
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("search-one-school",
                preprocessResponse(prettyPrint()),
                responseFields(
                    fieldWithPath("code").type(JsonFieldType.NUMBER)
                        .description("코드"),
                    fieldWithPath("status").type(JsonFieldType.STRING)
                        .description("상태"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("메시지"),
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("응답 데이터"),
                    fieldWithPath("data.id").type(JsonFieldType.NUMBER)
                        .description("학교 ID"),
                    fieldWithPath("data.name").type(JsonFieldType.STRING)
                        .description("학교 이름"),
                    fieldWithPath("data.address").type(JsonFieldType.STRING)
                        .description("학교 주소"),
                    fieldWithPath("data.url").type(JsonFieldType.STRING)
                        .description("학교 홈페이지 주소"),
                    fieldWithPath("data.tel").type(JsonFieldType.STRING)
                        .description("확교 전화번호")
                )
            ));
    }

    @DisplayName("내 학교 등록 현황 조회 API")
    @Test
    void searchMyEnroll() throws Exception {
        String userKey = UUID.randomUUID().toString();

        mockMvc.perform(
                get("/enroll/{userKey}", userKey)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("search-my-enroll",
                preprocessResponse(prettyPrint()),
                responseFields(
                    fieldWithPath("code").type(JsonFieldType.NUMBER)
                        .description("코드"),
                    fieldWithPath("status").type(JsonFieldType.STRING)
                        .description("상태"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("메시지"),
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("응답 데이터"),
                    fieldWithPath("data.schoolYear").type(JsonFieldType.NUMBER)
                        .description("학년도"),
                    fieldWithPath("data.schoolName").type(JsonFieldType.STRING)
                        .description("학교 이름"),
                    fieldWithPath("data.grade").type(JsonFieldType.NUMBER)
                        .description("학년"),
                    fieldWithPath("data.classNum").type(JsonFieldType.NUMBER)
                        .description("반"),
                    fieldWithPath("data.name").type(JsonFieldType.STRING)
                        .description("이름"),
                    fieldWithPath("data.isApproved").type(JsonFieldType.BOOLEAN)
                        .description("승인 여부"),
                    fieldWithPath("data.rejectedReason").type(JsonFieldType.STRING)
                        .description("거절 사유")
                )
            ));
    }

    @DisplayName("내 학급 조회 API")
    @Test
    void searchMyClassroom() throws Exception {
        String userKey = UUID.randomUUID().toString();

        mockMvc.perform(
                get("/classroom/{userKey}", userKey)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("search-my-classroom",
                preprocessResponse(prettyPrint()),
                responseFields(
                    fieldWithPath("code").type(JsonFieldType.NUMBER)
                        .description("코드"),
                    fieldWithPath("status").type(JsonFieldType.STRING)
                        .description("상태"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("메시지"),
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("응답 데이터"),
                    fieldWithPath("data.teacherName").type(JsonFieldType.STRING)
                        .description("교사 이름"),
                    fieldWithPath("data.schoolYear").type(JsonFieldType.NUMBER)
                        .description("년도"),
                    fieldWithPath("data.grade").type(JsonFieldType.NUMBER)
                        .description("학년"),
                    fieldWithPath("data.name").type(JsonFieldType.STRING)
                        .description("학급 이름")
                )
            ));
    }

    @DisplayName("(교사용) 등록 요청 리스트 조회 API")
    @Test
    void searchEnrolls() throws Exception {
        Long schoolClassId = Long.parseLong("123123123123");

        mockMvc.perform(
                get("/enroll/teacher/{schoolClassId}", schoolClassId)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("search-enroll-list",
                preprocessResponse(prettyPrint()),
                responseFields(
                    fieldWithPath("code").type(JsonFieldType.NUMBER)
                        .description("코드"),
                    fieldWithPath("status").type(JsonFieldType.STRING)
                        .description("상태"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("메시지"),
                    fieldWithPath("data").type(JsonFieldType.ARRAY)
                        .description("응답 데이터"),
                    fieldWithPath("data[].type").type(JsonFieldType.STRING)
                        .description("요청 주체(P: 학부모 신청, C: 학생 신청)"),
                    fieldWithPath("data[].childName").type(JsonFieldType.STRING)
                        .description("학생 이름"),
                    fieldWithPath("data[].grade").type(JsonFieldType.NUMBER)
                        .description("학년"),
                    fieldWithPath("data[].classNum").type(JsonFieldType.NUMBER)
                        .description("반"),
                    fieldWithPath("data[].studentNum").type(JsonFieldType.NUMBER)
                        .description("학생 번호"),
                    fieldWithPath("data[].appliedDate").type(JsonFieldType.ARRAY)
                        .description("신청 일자")
                )
            ));
    }
}
