package com.planwith.planwith_fo_report.application.report.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_report.application.report.result.ReportResult;

public interface GetReportUseCase {

	ReportResult getReport(UUID reportUuid);
}
