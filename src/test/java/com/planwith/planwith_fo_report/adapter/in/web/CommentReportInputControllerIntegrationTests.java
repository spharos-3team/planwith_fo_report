package com.planwith.planwith_fo_report.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.planwith.planwith_fo_report.application.report.port.out.CommentReportContextPort;
import com.planwith.planwith_fo_report.domain.report.CommentReportContext;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class CommentReportInputControllerIntegrationTests {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MEMBER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID AUTHOR_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CommentReportContextPort commentReportContextPort;

	@Test
	void validInputEntersApplicationAndPassesTargetCheck() throws Exception {
		given(commentReportContextPort.findByCommentUuid(COMMENT_UUID))
				.willReturn(Optional.of(CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, true)));

		mockMvc.perform(post("/api/planwith-fo-report/comment-reports/input-validation")
						.header("X-Member-Uuid", MEMBER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentUuid": "22222222-2222-2222-2222-222222222222",
								  "reportType": "SPAM"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.commentUuid").value(COMMENT_UUID.toString()))
				.andExpect(jsonPath("$.reportType").value("SPAM"))
				.andExpect(jsonPath("$.authorMemberUuid").value(AUTHOR_UUID.toString()))
				.andExpect(jsonPath("$.reportable").value(true));
	}

	@Test
	void rejectsMissingCommentUuidBeforeApplicationService() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-report/comment-reports/input-validation")
						.header("X-Member-Uuid", MEMBER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reportType": "SPAM"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("댓글 UUID는 필수입니다."));

		verify(commentReportContextPort, never()).findByCommentUuid(any());
	}

	@Test
	void rejectsMissingReportTypeBeforeApplicationService() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-report/comment-reports/input-validation")
						.header("X-Member-Uuid", MEMBER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentUuid": "22222222-2222-2222-2222-222222222222"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("신고 사유는 필수입니다."));

		verify(commentReportContextPort, never()).findByCommentUuid(any());
	}

	@Test
	void rejectsUnknownReportTypeBeforeApplicationService() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-report/comment-reports/input-validation")
						.header("X-Member-Uuid", MEMBER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentUuid": "22222222-2222-2222-2222-222222222222",
								  "reportType": "ILLEGAL"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value(
						"신고 사유는 SPAM, ABUSE, HATE, SEXUAL, PRIVACY, OTHER 중 하나여야 합니다."
				));

		verify(commentReportContextPort, never()).findByCommentUuid(any());
	}

	@Test
	void rejectsInvalidCommentUuidFormatBeforeApplicationService() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-report/comment-reports/input-validation")
						.header("X-Member-Uuid", MEMBER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentUuid": "not-a-uuid",
								  "reportType": "SPAM"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("댓글 UUID 형식이 올바르지 않습니다."));

		verify(commentReportContextPort, never()).findByCommentUuid(any());
	}

	@Test
	void rejectsRequestWithoutAuthenticatedMember() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-report/comment-reports/input-validation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentUuid": "22222222-2222-2222-2222-222222222222",
								  "reportType": "OTHER"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED_MEMBER"));

		verify(commentReportContextPort, never()).findByCommentUuid(any());
	}

	@Test
	void rejectsMemberUuidInRequestBody() throws Exception {
		given(commentReportContextPort.findByCommentUuid(COMMENT_UUID))
				.willReturn(Optional.of(CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, true)));

		mockMvc.perform(post("/api/planwith-fo-report/comment-reports/input-validation")
						.header("X-Member-Uuid", MEMBER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentUuid": "22222222-2222-2222-2222-222222222222",
								  "reportType": "ABUSE",
								  "memberUuid": "44444444-4444-4444-4444-444444444444"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reportType").value("ABUSE"));
	}
}
