package com.planwith.planwith_fo_report.application.report.result;

import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.ReportType;

public record CommentReportInputResult(
		UUID commentUuid,
		ReportType reportType,
		UUID memberUuid,
		UUID authorMemberUuid,
		boolean reportable
) {
}
