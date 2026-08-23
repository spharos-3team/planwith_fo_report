package com.planwith.planwith_fo_report.adapter.in.web;

import java.time.Instant;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.planwith.planwith_fo_report.adapter.in.web.auth.InvalidCredentialsException;
import com.planwith.planwith_fo_report.adapter.in.web.dto.ApiErrorResponse;
import com.planwith.planwith_fo_report.domain.report.exception.DuplicateReportException;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportStatusTransitionException;
import com.planwith.planwith_fo_report.domain.report.exception.ReportNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
		return createErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", exception.getMessage());
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
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청값 형식이 올바르지 않습니다.");
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
