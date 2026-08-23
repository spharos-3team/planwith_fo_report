package com.planwith.planwith_fo_report.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
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

import com.planwith.planwith_fo_report.application.report.port.out.CommentReportContextPort;
import com.planwith.planwith_fo_report.domain.report.CommentReportContext;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class CommentReportTargetControllerIntegrationTests {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID AUTHOR_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CommentReportContextPort commentReportContextPort;

	@Test
	void validateTargetReturnsOkForReportableComment() throws Exception {
		given(commentReportContextPort.findByCommentUuid(COMMENT_UUID))
				.willReturn(Optional.of(CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, true)));

		mockMvc.perform(post("/api/planwith-fo-report/comment-reports/target-validation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentUuid": "22222222-2222-2222-2222-222222222222",
								  "memberUuid": "11111111-1111-1111-1111-111111111111"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.commentUuid").value(COMMENT_UUID.toString()))
				.andExpect(jsonPath("$.authorMemberUuid").value(AUTHOR_UUID.toString()))
				.andExpect(jsonPath("$.reportable").value(true));
	}

	@Test
	void validateTargetReturnsNotFoundWhenCommentMissing() throws Exception {
		given(commentReportContextPort.findByCommentUuid(any())).willReturn(Optional.empty());

		mockMvc.perform(post("/api/planwith-fo-report/comment-reports/target-validation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentUuid": "22222222-2222-2222-2222-222222222222",
								  "memberUuid": "11111111-1111-1111-1111-111111111111"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("COMMENT_NOT_FOUND"));
	}

	@Test
	void validateTargetReturnsConflictWhenCommentDeleted() throws Exception {
		given(commentReportContextPort.findByCommentUuid(COMMENT_UUID))
				.willReturn(Optional.of(CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, false)));

		mockMvc.perform(post("/api/planwith-fo-report/comment-reports/target-validation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentUuid": "22222222-2222-2222-2222-222222222222",
								  "memberUuid": "11111111-1111-1111-1111-111111111111"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("COMMENT_NOT_REPORTABLE"));
	}

	@Test
	void validateTargetReturnsForbiddenWhenSelfReport() throws Exception {
		given(commentReportContextPort.findByCommentUuid(COMMENT_UUID))
				.willReturn(Optional.of(CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, true)));

		mockMvc.perform(post("/api/planwith-fo-report/comment-reports/target-validation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "commentUuid": "22222222-2222-2222-2222-222222222222",
								  "memberUuid": "33333333-3333-3333-3333-333333333333"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("SELF_REPORT_NOT_ALLOWED"));
	}
}
