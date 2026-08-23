package com.planwith.planwith_fo_report.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_report.adapter.in.web.dto.CommentReportTargetResponse;
import com.planwith.planwith_fo_report.adapter.in.web.dto.ValidateCommentReportTargetRequest;
import com.planwith.planwith_fo_report.application.report.port.in.ValidateCommentReportTargetUseCase;

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
@Tag(name = "comment-report-target", description = "댓글 신고 대상 검증 API")
public class CommentReportTargetController {

	private final ValidateCommentReportTargetUseCase validateCommentReportTargetUseCase;

	// 댓글 신고 대상 검증
	@PostMapping("/target-validation")
	@Operation(summary = "댓글 신고 대상 검증", description = "Comment Service로 댓글 존재/삭제/작성자를 확인한 뒤 신고 가능 대상만 통과시킨다.")
	public ResponseEntity<CommentReportTargetResponse> validateTarget(
			@Valid @RequestBody ValidateCommentReportTargetRequest request
	) {
		log.info("CommentReportTargetController : POST validateTarget : 댓글 신고 대상 검증 요청");
		CommentReportTargetResponse response = CommentReportTargetResponse.from(
				validateCommentReportTargetUseCase.validate(request.toCommand())
		);
		return ResponseEntity.ok(response);
	}
}
