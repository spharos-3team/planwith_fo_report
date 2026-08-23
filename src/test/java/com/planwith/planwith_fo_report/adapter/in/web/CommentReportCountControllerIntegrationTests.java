package com.planwith.planwith_fo_report.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.port.out.StoryCommentReportRepository;
import com.planwith.planwith_fo_report.domain.report.ReportType;
import com.planwith.planwith_fo_report.domain.report.StoryCommentReport;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentReportCountControllerIntegrationTests {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID OTHER_COMMENT_UUID = UUID.fromString("55555555-5555-5555-5555-555555555555");
	private static final UUID MEMBER_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID MEMBER_B = UUID.fromString("44444444-4444-4444-4444-444444444444");
	private static final UUID MEMBER_C = UUID.fromString("66666666-6666-6666-6666-666666666666");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StoryCommentReportRepository storyCommentReportRepository;

	@Test
	void returnsZeroWhenCommentHasNoReports() throws Exception {
		mockMvc.perform(get("/api/planwith-fo-report/reports/comments/" + COMMENT_UUID + "/count"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.commentUuid").value(COMMENT_UUID.toString()))
				.andExpect(jsonPath("$.reportCount").value(0));
	}

	@Test
	void accumulatesReportsFromDifferentMembers() throws Exception {
		storyCommentReportRepository.save(StoryCommentReport.create(COMMENT_UUID, MEMBER_A, ReportType.SPAM));
		storyCommentReportRepository.save(StoryCommentReport.create(COMMENT_UUID, MEMBER_B, ReportType.HATE));
		storyCommentReportRepository.save(StoryCommentReport.create(COMMENT_UUID, MEMBER_C, ReportType.ABUSE));
		storyCommentReportRepository.save(
				StoryCommentReport.create(OTHER_COMMENT_UUID, MEMBER_A, ReportType.OTHER)
		);

		mockMvc.perform(get("/api/planwith-fo-report/reports/comments/" + COMMENT_UUID + "/count"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.commentUuid").value(COMMENT_UUID.toString()))
				.andExpect(jsonPath("$.reportCount").value(3));

		mockMvc.perform(get("/api/planwith-fo-report/reports/comments/" + OTHER_COMMENT_UUID + "/count"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.commentUuid").value(OTHER_COMMENT_UUID.toString()))
				.andExpect(jsonPath("$.reportCount").value(1));
	}

	@Test
	void rejectsInvalidCommentUuid() throws Exception {
		mockMvc.perform(get("/api/planwith-fo-report/reports/comments/not-a-uuid/count"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}
}
