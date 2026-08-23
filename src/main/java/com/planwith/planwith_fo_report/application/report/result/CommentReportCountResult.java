package com.planwith.planwith_fo_report.application.report.result;

import java.util.UUID;

public record CommentReportCountResult(
		UUID commentUuid,
		long reportCount
) {
}
