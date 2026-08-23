package com.planwith.planwith_fo_report.application.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_report.application.report.command.ReportWorkflowAction;
import com.planwith.planwith_fo_report.application.report.command.ReviewReportCommand;
import com.planwith.planwith_fo_report.application.report.port.out.ModerationOutboxPort;
import com.planwith.planwith_fo_report.application.report.port.out.ReportEventPublisherPort;
import com.planwith.planwith_fo_report.application.report.port.out.ReportRepositoryPort;
import com.planwith.planwith_fo_report.domain.report.Report;
import com.planwith.planwith_fo_report.domain.report.ReportReason;
import com.planwith.planwith_fo_report.domain.report.ReportStatus;
import com.planwith.planwith_fo_report.domain.report.TargetType;
import com.planwith.planwith_fo_report.domain.report.event.ModerationActionRequired;
import com.planwith.planwith_fo_report.domain.report.event.ReportReviewed;

@ExtendWith(MockitoExtension.class)
class ReviewReportServiceTest {

	private static final UUID REPORTER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID TARGET_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID REVIEWER_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Mock
	private ReportRepositoryPort reportRepositoryPort;

	@Mock
	private ReportEventPublisherPort reportEventPublisherPort;

	@Mock
	private ModerationOutboxPort moderationOutboxPort;

	private ReviewReportService reviewReportService;

	@BeforeEach
	void setUp() {
		ReportEventDispatcher dispatcher = new ReportEventDispatcher(reportEventPublisherPort, moderationOutboxPort);
		reviewReportService = new ReviewReportService(reportRepositoryPort, dispatcher);
	}

	@Test
	void approveWritesModerationEventToOutbox() {
		Report report = reviewingReport();
		given(reportRepositoryPort.findByReportUuid(report.getReportUuid())).willReturn(Optional.of(report));
		given(reportRepositoryPort.save(any(Report.class))).willAnswer(invocation -> invocation.getArgument(0));

		reviewReportService.reviewReport(new ReviewReportCommand(
				report.getReportUuid(),
				ReportWorkflowAction.APPROVE,
				REVIEWER_UUID,
				"삭제 필요"
		));

		assertThat(report.getStatus()).isEqualTo(ReportStatus.APPROVED);
		verify(reportEventPublisherPort).publish(any(ReportReviewed.class));
		verify(moderationOutboxPort).save(any(ModerationActionRequired.class));
	}

	@Test
	void rejectDoesNotWriteOutbox() {
		Report report = reviewingReport();
		given(reportRepositoryPort.findByReportUuid(report.getReportUuid())).willReturn(Optional.of(report));
		given(reportRepositoryPort.save(any(Report.class))).willAnswer(invocation -> invocation.getArgument(0));

		reviewReportService.reviewReport(new ReviewReportCommand(
				report.getReportUuid(),
				ReportWorkflowAction.REJECT,
				REVIEWER_UUID,
				"해당 없음"
		));

		assertThat(report.getStatus()).isEqualTo(ReportStatus.REJECTED);
		verify(reportEventPublisherPort).publish(any(ReportReviewed.class));
		verify(moderationOutboxPort, never()).save(any());
	}

	private static Report reviewingReport() {
		Report report = Report.create(REPORTER_UUID, TargetType.STORY, TARGET_UUID, ReportReason.SPAM, null);
		report.pullDomainEvents();
		report.startReview(REVIEWER_UUID);
		return report;
	}
}
