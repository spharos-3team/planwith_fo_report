package com.planwith.planwith_fo_report.application.report.command;

import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.ReportType;

public record CreateCommentReportCommand(
		UUID commentUuid,
		ReportType reportType,
		UUID memberUuid
) {
}
