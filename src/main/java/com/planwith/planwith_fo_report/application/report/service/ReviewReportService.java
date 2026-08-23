package com.planwith.planwith_fo_report.application.report.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.command.ReviewReportCommand;
import com.planwith.planwith_fo_report.application.report.port.in.ReviewReportUseCase;
import com.planwith.planwith_fo_report.application.report.port.out.ReportRepositoryPort;
import com.planwith.planwith_fo_report.application.report.result.ReportResult;
import com.planwith.planwith_fo_report.domain.report.Report;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;
import com.planwith.planwith_fo_report.domain.report.exception.ReportNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewReportService implements ReviewReportUseCase {

	private final ReportRepositoryPort reportRepositoryPort;
	private final ReportEventDispatcher reportEventDispatcher;

	@Override
	@Transactional
	public ReportResult reviewReport(ReviewReportCommand command) {
		log.info("ReviewReportService : reviewReport : 신고 워크플로우 처리 시작 - reportUuid={}, action={}",
				command.reportUuid(),
				command.action());

		if (command.action() == null) {
			throw new InvalidReportException("워크플로우 액션은 필수입니다.");
		}

		Report report = reportRepositoryPort.findByReportUuid(command.reportUuid())
				.orElseThrow(() -> new ReportNotFoundException(command.reportUuid()));

		switch (command.action()) {
			case START_REVIEW -> report.startReview(command.reviewerUuid());
			case APPROVE -> report.approve(command.reviewerUuid(), command.reviewComment());
			case REJECT -> report.reject(command.reviewerUuid(), command.reviewComment());
			case MARK_ACTIONED -> report.markActioned();
		}

		Report saved = reportRepositoryPort.save(report);
		reportEventDispatcher.dispatch(report);

		log.info("ReviewReportService : reviewReport : 신고 워크플로우 처리 완료 - reportUuid={}, status={}",
				saved.getReportUuid(),
				saved.getStatus());
		return ReportResult.from(saved);
	}
}
