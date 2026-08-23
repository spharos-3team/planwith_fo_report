package com.planwith.planwith_fo_report.application.report.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.command.CreateReportCommand;
import com.planwith.planwith_fo_report.application.report.port.in.CreateReportUseCase;
import com.planwith.planwith_fo_report.application.report.port.out.ReportRepositoryPort;
import com.planwith.planwith_fo_report.application.report.result.ReportResult;
import com.planwith.planwith_fo_report.domain.report.Report;
import com.planwith.planwith_fo_report.domain.report.exception.DuplicateReportException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateReportService implements CreateReportUseCase {

	private final ReportRepositoryPort reportRepositoryPort;
	private final ReportEventDispatcher reportEventDispatcher;

	@Override
	@Transactional
	public ReportResult createReport(CreateReportCommand command) {
		log.info("CreateReportService : createReport : 신고 생성 비즈니스 로직 시작");
		log.debug(
				"CreateReportService : createReport : 신고 생성 요청 데이터 확인 - reporterUuid={}, targetType={}, targetUuid={}",
				command.reporterUuid(),
				command.targetType(),
				command.targetUuid()
		);

		if (reportRepositoryPort.existsByReporterAndTarget(
				command.reporterUuid(),
				command.targetType(),
				command.targetUuid()
		)) {
			log.warn(
					"CreateReportService : createReport : 중복 신고 요청 - reporterUuid={}, targetType={}, targetUuid={}",
					command.reporterUuid(),
					command.targetType(),
					command.targetUuid()
			);
			throw new DuplicateReportException();
		}

		Report report = Report.create(
				command.reporterUuid(),
				command.targetType(),
				command.targetUuid(),
				command.reason(),
				command.detail()
		);
		Report saved = reportRepositoryPort.save(report);
		reportEventDispatcher.dispatch(report);

		log.info("CreateReportService : createReport : 신고 생성 완료 - reportUuid={}", saved.getReportUuid());
		return ReportResult.from(saved);
	}
}
