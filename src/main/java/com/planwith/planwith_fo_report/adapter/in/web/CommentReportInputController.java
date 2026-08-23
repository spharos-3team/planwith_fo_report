package com.planwith.planwith_fo_report.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_report.adapter.in.web.dto.CommentReportInputResponse;
import com.planwith.planwith_fo_report.adapter.in.web.dto.CreateCommentReportRequest;
import com.planwith.planwith_fo_report.application.report.port.in.ValidateCommentReportInputUseCase;
import com.planwith.planwith_fo_report.domain.report.exception.UnauthenticatedMemberException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/planwith-fo-report/comment-reports")
@Tag(name = "comment-report-input", description = "댓글 신고 입력 검증 API")
public class CommentReportInputController {

	static final String MEMBER_UUID_HEADER = "X-Member-Uuid";

	private final ValidateCommentReportInputUseCase validateCommentReportInputUseCase;

	// 댓글 신고 입력 검증
	@PostMapping("/input-validation")
	@Operation(
			summary = "댓글 신고 입력 검증",
			description = "commentUuid, reportType을 검증하고 로그인 회원은 X-Member-Uuid에서 획득한 뒤 신고 대상 확인으로 전달한다."
	)
	public ResponseEntity<CommentReportInputResponse> validateInput(
			@RequestHeader(value = MEMBER_UUID_HEADER, required = false) UUID memberUuid,
			@Valid @RequestBody CreateCommentReportRequest request
	) {
		log.info("CommentReportInputController : POST validateInput : 댓글 신고 입력 검증 요청");
		if (memberUuid == null) {
			throw new UnauthenticatedMemberException();
		}
		CommentReportInputResponse response = CommentReportInputResponse.from(
				validateCommentReportInputUseCase.validate(request.toCommand(memberUuid))
		);
		return ResponseEntity.ok(response);
	}
}
