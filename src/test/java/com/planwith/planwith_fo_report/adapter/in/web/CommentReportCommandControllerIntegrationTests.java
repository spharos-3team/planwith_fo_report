package com.planwith.planwith_fo_report.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
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
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.port.out.CommentReportContextPort;
import com.planwith.planwith_fo_report.application.report.port.out.StoryCommentReportRepository;
import com.planwith.planwith_fo_report.domain.report.CommentReportContext;
import com.planwith.planwith_fo_report.domain.report.ReportType;
import com.planwith.planwith_fo_report.domain.report.StoryCommentReport;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentReportCommandControllerIntegrationTests {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MEMBER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID AUTHOR_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StoryCommentReportRepository storyCommentReportRepository;

	@MockitoBean
	private CommentReportContextPort commentReportContextPort;

	@Test
	void createCommentReportSavesAndReturnsMessage() throws Exception {
		given(commentReportContextPort.findByCommentUuid(COMMENT_UUID))
				.willReturn(Optional.of(CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, true)));

		mockMvc.perform(post("/api/planwith-fo-report/reports/comments/" + COMMENT_UUID)
						.header("X-Member-Uuid", MEMBER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reportType": "HATE"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.commentReportUuid").isNotEmpty())
				.andExpect(jsonPath("$.commentUuid").value(COMMENT_UUID.toString()))
				.andExpect(jsonPath("$.reportType").value("HATE"))
				.andExpect(jsonPath("$.createdAt").isNotEmpty())
				.andExpect(jsonPath("$.reportCount").value(1))
				.andExpect(jsonPath("$.message").value("댓글을 신고했다"));

		assertThat(storyCommentReportRepository.existsByCommentUuidAndMemberUuid(COMMENT_UUID, MEMBER_UUID)).isTrue();
	}

	@Test
	void rejectsUnauthenticatedMember() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-report/reports/comments/" + COMMENT_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reportType": "HATE"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED_MEMBER"));
	}

	@Test
	void rejectsMissingReportType() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-report/reports/comments/" + COMMENT_UUID)
						.header("X-Member-Uuid", MEMBER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("신고 사유는 필수입니다."));
	}

	@Test
	void allowsSameCommentFromDifferentMembers() throws Exception {
		UUID otherMemberUuid = UUID.fromString("44444444-4444-4444-4444-444444444444");
		given(commentReportContextPort.findByCommentUuid(COMMENT_UUID))
				.willReturn(Optional.of(CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, true)));
		storyCommentReportRepository.save(
				StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, ReportType.SPAM)
		);

		mockMvc.perform(post("/api/planwith-fo-report/reports/comments/" + COMMENT_UUID)
						.header("X-Member-Uuid", otherMemberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reportType": "HATE"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").value("댓글을 신고했다"))
				.andExpect(jsonPath("$.reportCount").value(2));

		assertThat(storyCommentReportRepository.existsByCommentUuidAndMemberUuid(COMMENT_UUID, MEMBER_UUID)).isTrue();
		assertThat(storyCommentReportRepository.existsByCommentUuidAndMemberUuid(COMMENT_UUID, otherMemberUuid)).isTrue();
	}

	@Test
	void rejectsDuplicateCommentReport() throws Exception {
		given(commentReportContextPort.findByCommentUuid(COMMENT_UUID))
				.willReturn(Optional.of(CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, true)));
		storyCommentReportRepository.save(
				StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, ReportType.SPAM)
		);

		mockMvc.perform(post("/api/planwith-fo-report/reports/comments/" + COMMENT_UUID)
						.header("X-Member-Uuid", MEMBER_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reportType": "HATE"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DUPLICATE_REPORT"));
	}
}
