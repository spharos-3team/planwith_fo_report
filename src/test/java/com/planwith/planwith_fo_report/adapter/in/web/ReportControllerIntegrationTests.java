package com.planwith.planwith_fo_report.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createAndReviewReportWorkflow() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/planwith-fo-report/reports")
						.header("X-Member-Uuid", "11111111-1111-1111-1111-111111111111")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reporterUuid": "99999999-9999-9999-9999-999999999999",
								  "targetType": "STORY",
								  "targetUuid": "22222222-2222-2222-2222-222222222222",
								  "reason": "SPAM",
								  "detail": "광고성 게시글"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.reporterUuid").value("11111111-1111-1111-1111-111111111111"))
				.andExpect(jsonPath("$.status").value("RECEIVED"))
				.andExpect(jsonPath("$.targetType").value("STORY"))
				.andExpect(jsonPath("$.targetUuid").value("22222222-2222-2222-2222-222222222222"))
				.andReturn();

		String reportUuid = JsonPath.read(createResult.getResponse().getContentAsString(), "$.reportUuid");

		mockMvc.perform(get("/api/planwith-fo-report/reports/" + reportUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reportUuid").value(reportUuid));

		mockMvc.perform(post("/api/planwith-fo-report/reports/" + reportUuid + "/workflow")
						.header("X-Member-Uuid", "33333333-3333-3333-3333-333333333333")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "action": "START_REVIEW",
								  "reviewerUuid": "33333333-3333-3333-3333-333333333333"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REVIEWING"));

		mockMvc.perform(post("/api/planwith-fo-report/reports/" + reportUuid + "/workflow")
						.header("X-Member-Uuid", "33333333-3333-3333-3333-333333333333")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "action": "APPROVE",
								  "reviewerUuid": "33333333-3333-3333-3333-333333333333",
								  "reviewComment": "가이드 위반"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED"));

		mockMvc.perform(post("/api/planwith-fo-report/reports/" + reportUuid + "/workflow")
						.header("X-Member-Uuid", "33333333-3333-3333-3333-333333333333")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "action": "MARK_ACTIONED"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIONED"));
	}

	@Test
	void duplicateReportReturnsConflict() throws Exception {
		String body = """
				{
				  "reporterUuid": "11111111-1111-1111-1111-111111111111",
				  "targetType": "COMMENT",
				  "targetUuid": "44444444-4444-4444-4444-444444444444",
				  "reason": "HARASSMENT"
				}
				""";

		mockMvc.perform(post("/api/planwith-fo-report/reports")
						.header("X-Member-Uuid", "11111111-1111-1111-1111-111111111111")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/planwith-fo-report/reports")
						.header("X-Member-Uuid", "11111111-1111-1111-1111-111111111111")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DUPLICATE_REPORT"));
	}

	@Test
	void getUnknownReportReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/planwith-fo-report/reports/55555555-5555-5555-5555-555555555555"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("REPORT_NOT_FOUND"));
	}
}
