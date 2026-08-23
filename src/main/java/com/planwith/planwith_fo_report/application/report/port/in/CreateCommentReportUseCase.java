package com.planwith.planwith_fo_report.application.report.port.in;

import com.planwith.planwith_fo_report.application.report.command.CreateCommentReportCommand;
import com.planwith.planwith_fo_report.application.report.result.CreateCommentReportResult;

public interface CreateCommentReportUseCase {

	CreateCommentReportResult create(CreateCommentReportCommand command);
}
