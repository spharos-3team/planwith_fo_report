package com.planwith.planwith_fo_report.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_report.adapter.in.web.dto.CommentReportCountResponse;
import com.planwith.planwith_fo_report.application.report.port.in.CountCommentReportsUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/planwith-fo-report/reports/comments")
@Tag(name = "comment-report-count", description = "댓글 신고 누적 집계 API")
public class CommentReportCountController {

	private final CountCommentReportsUseCase countCommentReportsUseCase;

	// 댓글 신고 누적 건수 조회
	@GetMapping("/{commentUuid}/count")
	@Operation(summary = "댓글 신고 누적 건수 조회", description = "story_comment_report를 원장으로 comment_uuid 기준 COUNT를 반환한다.")
	public ResponseEntity<CommentReportCountResponse> countCommentReports(@PathVariable UUID commentUuid) {
		log.info("CommentReportCountController : GET countCommentReports : 댓글 신고 누적 건수 조회 요청");
		return ResponseEntity.ok(CommentReportCountResponse.from(countCommentReportsUseCase.count(commentUuid)));
	}
}
