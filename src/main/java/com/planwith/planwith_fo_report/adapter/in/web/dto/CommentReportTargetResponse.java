package com.planwith.planwith_fo_report.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_report.application.report.result.CommentReportTargetResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 신고 대상 검증 응답")
public record CommentReportTargetResponse(
		UUID commentUuid,
		UUID authorMemberUuid,
		boolean reportable
) {

	public static CommentReportTargetResponse from(CommentReportTargetResult result) {
		return new CommentReportTargetResponse(
				result.commentUuid(),
				result.authorMemberUuid(),
				result.reportable()
		);
	}
}
