package com.planwith.planwith_fo_report.application.report.command;

import java.util.UUID;

public record ValidateCommentReportTargetCommand(
		UUID commentUuid,
		UUID memberUuid
) {
}
