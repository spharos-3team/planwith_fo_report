package com.planwith.planwith_fo_report.application.report.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.port.in.GetReportUseCase;
import com.planwith.planwith_fo_report.application.report.port.out.ReportRepositoryPort;
import com.planwith.planwith_fo_report.application.report.result.ReportResult;
import com.planwith.planwith_fo_report.domain.report.Report;
import com.planwith.planwith_fo_report.domain.report.exception.ReportNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetReportService implements GetReportUseCase {

	private final ReportRepositoryPort reportRepositoryPort;

	@Override
	@Transactional(readOnly = true)
	public ReportResult getReport(UUID reportUuid) {
		log.debug("GetReportService : getReport : 신고 조회 - reportUuid={}", reportUuid);

		Report report = reportRepositoryPort.findByReportUuid(reportUuid)
				.orElseThrow(() -> new ReportNotFoundException(reportUuid));

		return ReportResult.from(report);
	}
}
