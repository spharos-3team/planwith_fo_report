package com.planwith.planwith_fo_report.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_report.adapter.in.web.dto.CreateCommentReportResponse;
import com.planwith.planwith_fo_report.adapter.in.web.dto.CreateCommentReportTypeRequest;
import com.planwith.planwith_fo_report.application.report.port.in.CreateCommentReportUseCase;
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
@RequestMapping("/api/planwith-fo-report/reports/comments")
@Tag(name = "comment-report-command", description = "댓글 신고 생성 API")
public class CommentReportCommandController {

	private final CreateCommentReportUseCase createCommentReportUseCase;

	// 댓글 신고 생성
	@PostMapping("/{commentUuid}")
	@Operation(summary = "댓글 신고 생성", description = "입력/대상/중복 검증 후 story_comment_report에 댓글 신고를 저장한다.")
	public ResponseEntity<CreateCommentReportResponse> createCommentReport(
			@RequestHeader(value = CommentReportInputController.MEMBER_UUID_HEADER, required = false) UUID memberUuid,
			@PathVariable UUID commentUuid,
			@Valid @RequestBody CreateCommentReportTypeRequest request
	) {
		log.info("CommentReportCommandController : POST createCommentReport : 댓글 신고 생성 요청");
		if (memberUuid == null) {
			throw new UnauthenticatedMemberException();
		}
		CreateCommentReportResponse response = CreateCommentReportResponse.from(
				createCommentReportUseCase.create(request.toCommand(commentUuid, memberUuid))
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
