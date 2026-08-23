package com.planwith.planwith_fo_report.application.report.command;

import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.ReportReason;
import com.planwith.planwith_fo_report.domain.report.TargetType;

public record CreateReportCommand(
		UUID reporterUuid,
		TargetType targetType,
		UUID targetUuid,
		ReportReason reason,
		String detail
) {
}
