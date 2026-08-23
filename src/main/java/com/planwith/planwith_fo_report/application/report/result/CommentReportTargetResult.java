package com.planwith.planwith_fo_report.application.report.result;

import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.CommentReportContext;

public record CommentReportTargetResult(
		UUID commentUuid,
		UUID authorMemberUuid,
		boolean reportable
) {

	public static CommentReportTargetResult from(CommentReportContext context) {
		return new CommentReportTargetResult(
				context.getCommentUuid(),
				context.getAuthorMemberUuid(),
				context.isReportable()
		);
	}
}
