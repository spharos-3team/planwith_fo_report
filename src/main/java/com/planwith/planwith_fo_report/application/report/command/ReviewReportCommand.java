package com.planwith.planwith_fo_report.application.report.command;

import java.util.UUID;

public record ReviewReportCommand(
		UUID reportUuid,
		ReportWorkflowAction action,
		UUID reviewerUuid,
		String reviewComment
) {
}
