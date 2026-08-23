package com.planwith.planwith_fo_report.application.report.port.in;

import com.planwith.planwith_fo_report.application.report.command.ReviewReportCommand;
import com.planwith.planwith_fo_report.application.report.result.ReportResult;

public interface ReviewReportUseCase {

	ReportResult reviewReport(ReviewReportCommand command);
}
