package com.planwith.planwith_fo_report.application.report.port.in;

import com.planwith.planwith_fo_report.application.report.command.ValidateCommentReportTargetCommand;
import com.planwith.planwith_fo_report.application.report.result.CommentReportTargetResult;

public interface ValidateCommentReportTargetUseCase {

	CommentReportTargetResult validate(ValidateCommentReportTargetCommand command);
}
