package com.planwith.planwith_fo_report.adapter.out.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.planwith.planwith_fo_report.config.CommentServiceProperties;
import com.planwith.planwith_fo_report.domain.report.CommentReportContext;
import com.planwith.planwith_fo_report.domain.report.exception.CommentServiceUnavailableException;

class HttpCommentReportContextAdapterTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID AUTHOR_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final String BASE_URL = "http://comment-service";

	private MockRestServiceServer mockServer;
	private HttpCommentReportContextAdapter adapter;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		mockServer = MockRestServiceServer.bindTo(builder).build();
		adapter = new HttpCommentReportContextAdapter(
				builder,
				new CommentServiceProperties(
						BASE_URL,
						"/internal/comments/{commentUuid}/report-context"
				)
		);
	}

	@Test
	void fetchesReportContextFromCommentService() {
		mockServer.expect(requestTo(BASE_URL + "/internal/comments/" + COMMENT_UUID + "/report-context"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("""
						{
						  "commentUuid": "22222222-2222-2222-2222-222222222222",
						  "authorMemberUuid": "33333333-3333-3333-3333-333333333333",
						  "reportable": true
						}
						""", MediaType.APPLICATION_JSON));

		Optional<CommentReportContext> context = adapter.findByCommentUuid(COMMENT_UUID);

		assertThat(context).isPresent();
		assertThat(context.get().getCommentUuid()).isEqualTo(COMMENT_UUID);
		assertThat(context.get().getAuthorMemberUuid()).isEqualTo(AUTHOR_UUID);
		assertThat(context.get().isReportable()).isTrue();
		mockServer.verify();
	}

	@Test
	void returnsEmptyWhenCommentServiceRespondsNotFound() {
		mockServer.expect(requestTo(BASE_URL + "/internal/comments/" + COMMENT_UUID + "/report-context"))
				.andRespond(withResourceNotFound());

		assertThat(adapter.findByCommentUuid(COMMENT_UUID)).isEmpty();
		mockServer.verify();
	}

	@Test
	void throwsWhenCommentServiceFails() {
		mockServer.expect(requestTo(BASE_URL + "/internal/comments/" + COMMENT_UUID + "/report-context"))
				.andRespond(withServerError());

		assertThatThrownBy(() -> adapter.findByCommentUuid(COMMENT_UUID))
				.isInstanceOf(CommentServiceUnavailableException.class);
		mockServerVerifyQuietly();
	}

	private void mockServerVerifyQuietly() {
		mockServer.verify();
	}
}
