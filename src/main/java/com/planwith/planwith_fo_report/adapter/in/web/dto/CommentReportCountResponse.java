package com.planwith.planwith_fo_report.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_report.application.report.result.CommentReportCountResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 신고 누적 건수 응답")
public record CommentReportCountResponse(
		UUID commentUuid,
		long reportCount
) {

	public static CommentReportCountResponse from(CommentReportCountResult result) {
		return new CommentReportCountResponse(result.commentUuid(), result.reportCount());
	}
}
