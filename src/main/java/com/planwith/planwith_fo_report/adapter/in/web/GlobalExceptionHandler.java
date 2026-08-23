package com.planwith.planwith_fo_report.adapter.in.web;

import java.time.Instant;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.planwith.planwith_fo_report.adapter.in.web.auth.InvalidCredentialsException;
import com.planwith.planwith_fo_report.adapter.in.web.dto.ApiErrorResponse;
import com.planwith.planwith_fo_report.domain.report.exception.CommentNotFoundException;
import com.planwith.planwith_fo_report.domain.report.exception.CommentNotReportableException;
import com.planwith.planwith_fo_report.domain.report.exception.CommentServiceUnavailableException;
import com.planwith.planwith_fo_report.domain.report.exception.DuplicateReportException;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportStatusTransitionException;
import com.planwith.planwith_fo_report.domain.report.exception.ReportNotFoundException;
import com.planwith.planwith_fo_report.domain.report.exception.SelfCommentReportException;
import com.planwith.planwith_fo_report.domain.report.exception.UnauthenticatedMemberException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
		return createErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", exception.getMessage());
	}

	@ExceptionHandler(UnauthenticatedMemberException.class)
	public ResponseEntity<ApiErrorResponse> handleUnauthenticatedMember(UnauthenticatedMemberException exception) {
		log.warn("GlobalExceptionHandler : handleUnauthenticatedMember : 로그인 회원 정보 없음");
		return createErrorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED_MEMBER", exception.getMessage());
	}

	@ExceptionHandler(CommentNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleCommentNotFound(CommentNotFoundException exception) {
		log.warn("GlobalExceptionHandler : handleCommentNotFound : 존재하지 않는 댓글 신고 차단");
		return createErrorResponse(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", exception.getMessage());
	}

	@ExceptionHandler(CommentNotReportableException.class)
	public ResponseEntity<ApiErrorResponse> handleCommentNotReportable(CommentNotReportableException exception) {
		log.warn("GlobalExceptionHandler : handleCommentNotReportable : 삭제된 댓글 신고 차단");
		return createErrorResponse(HttpStatus.CONFLICT, "COMMENT_NOT_REPORTABLE", exception.getMessage());
	}

	@ExceptionHandler(SelfCommentReportException.class)
	public ResponseEntity<ApiErrorResponse> handleSelfCommentReport(SelfCommentReportException exception) {
		log.warn("GlobalExceptionHandler : handleSelfCommentReport : 본인 댓글 신고 차단");
		return createErrorResponse(HttpStatus.FORBIDDEN, "SELF_COMMENT_REPORT", exception.getMessage());
	}

	@ExceptionHandler(CommentServiceUnavailableException.class)
	public ResponseEntity<ApiErrorResponse> handleCommentServiceUnavailable(CommentServiceUnavailableException exception) {
		log.error("GlobalExceptionHandler : handleCommentServiceUnavailable : Comment Service 연동 실패");
		return createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "COMMENT_SERVICE_UNAVAILABLE", exception.getMessage());
	}

	@ExceptionHandler(ReportNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleReportNotFound(ReportNotFoundException exception) {
		log.warn("GlobalExceptionHandler : handleReportNotFound : 신고 조회 실패 - {}", exception.getMessage());
		return createErrorResponse(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", exception.getMessage());
	}

	@ExceptionHandler(DuplicateReportException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicateReport(DuplicateReportException exception) {
		log.warn("GlobalExceptionHandler : handleDuplicateReport : 중복 신고 요청");
		return createErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_REPORT", exception.getMessage());
	}

	@ExceptionHandler(InvalidReportStatusTransitionException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidTransition(InvalidReportStatusTransitionException exception) {
		log.warn("GlobalExceptionHandler : handleInvalidTransition : 잘못된 상태 전이 - {}", exception.getMessage());
		return createErrorResponse(HttpStatus.CONFLICT, "INVALID_REPORT_STATUS_TRANSITION", exception.getMessage());
	}

	@ExceptionHandler(InvalidReportException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidReport(InvalidReportException exception) {
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REPORT", exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.orElse("요청값이 올바르지 않습니다.");

		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		if (CommentReportInputController.MEMBER_UUID_HEADER.equals(exception.getName())) {
			return createErrorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED_MEMBER", "로그인 회원 정보가 올바르지 않습니다.");
		}
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청값 형식이 올바르지 않습니다.");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException exception) {
		String detail = exception.getMostSpecificCause().getMessage();
		if (detail != null && detail.contains("ReportType")) {
			return createErrorResponse(
					HttpStatus.BAD_REQUEST,
					"INVALID_REQUEST",
					"신고 사유는 SPAM, ABUSE, HATE, SEXUAL, PRIVACY, OTHER 중 하나여야 합니다."
			);
		}
		if (detail != null && detail.toLowerCase().contains("uuid")) {
			return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "댓글 UUID 형식이 올바르지 않습니다.");
		}
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청값이 올바르지 않습니다.");
	}

	private ResponseEntity<ApiErrorResponse> createErrorResponse(
			HttpStatus status,
			String code,
			String message
	) {
		ApiErrorResponse response = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				code,
				message
		);
		return ResponseEntity.status(status).body(response);
	}
}
