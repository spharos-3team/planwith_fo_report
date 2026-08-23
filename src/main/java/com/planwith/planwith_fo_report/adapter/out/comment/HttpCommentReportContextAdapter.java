package com.planwith.planwith_fo_report.adapter.out.comment;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.planwith.planwith_fo_report.application.report.port.out.CommentReportContextPort;
import com.planwith.planwith_fo_report.config.CommentServiceProperties;
import com.planwith.planwith_fo_report.domain.report.CommentReportContext;
import com.planwith.planwith_fo_report.domain.report.exception.CommentServiceUnavailableException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
class HttpCommentReportContextAdapter implements CommentReportContextPort {

	private final RestClient restClient;
	private final CommentServiceProperties commentServiceProperties;

	HttpCommentReportContextAdapter(
			RestClient.Builder restClientBuilder,
			CommentServiceProperties commentServiceProperties
	) {
		this.restClient = restClientBuilder
				.baseUrl(commentServiceProperties.baseUrl())
				.build();
		this.commentServiceProperties = commentServiceProperties;
	}

	@Override
	public Optional<CommentReportContext> findByCommentUuid(UUID commentUuid) {
		log.info("HttpCommentReportContextAdapter : findByCommentUuid : Comment Service 댓글 확인 시작");
		log.debug(
				"HttpCommentReportContextAdapter : findByCommentUuid : 댓글 확인 요청 - commentUuid={}",
				commentUuid
		);

		try {
			CommentReportContextResponse response = restClient.get()
					.uri(commentServiceProperties.reportContextPath(), commentUuid)
					.retrieve()
					.onStatus(statusCode -> statusCode != null && statusCode.is4xxClientError(), (request, httpResponse) -> {
						if (httpResponse.getStatusCode().value() == 404) {
							throw new CommentContextNotFoundException();
						}
						throw new CommentServiceUnavailableException();
					})
					.body(CommentReportContextResponse.class);

			if (response == null || response.commentUuid() == null) {
				log.warn("HttpCommentReportContextAdapter : findByCommentUuid : Comment Service 응답이 비어 있음 - commentUuid={}",
						commentUuid);
				throw new CommentServiceUnavailableException();
			}

			boolean reportable = Boolean.TRUE.equals(response.reportable());
			CommentReportContext context = CommentReportContext.of(
					response.commentUuid(),
					response.authorMemberUuid(),
					reportable
			);
			log.info("HttpCommentReportContextAdapter : findByCommentUuid : Comment Service 댓글 확인 완료 - commentUuid={}, reportable={}",
					context.getCommentUuid(),
					context.isReportable());
			return Optional.of(context);
		} catch (CommentContextNotFoundException exception) {
			log.warn("HttpCommentReportContextAdapter : findByCommentUuid : 댓글이 존재하지 않음 - commentUuid={}", commentUuid);
			return Optional.empty();
		} catch (CommentServiceUnavailableException exception) {
			throw exception;
		} catch (RestClientResponseException exception) {
			log.error("HttpCommentReportContextAdapter : findByCommentUuid : Comment Service 응답 오류 - commentUuid={}, status={}",
					commentUuid,
					exception.getStatusCode().value(),
					exception);
			throw new CommentServiceUnavailableException("댓글 서비스 확인에 실패했습니다.", exception);
		} catch (RestClientException exception) {
			log.error("HttpCommentReportContextAdapter : findByCommentUuid : Comment Service 연동 실패 - commentUuid={}",
					commentUuid,
					exception);
			throw new CommentServiceUnavailableException("댓글 서비스 확인에 실패했습니다.", exception);
		}
	}

	private static class CommentContextNotFoundException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
}
