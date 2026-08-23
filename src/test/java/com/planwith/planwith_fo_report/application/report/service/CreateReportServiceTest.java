package com.planwith.planwith_fo_report.application.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_report.application.report.command.CreateReportCommand;
import com.planwith.planwith_fo_report.application.report.port.out.ModerationOutboxPort;
import com.planwith.planwith_fo_report.application.report.port.out.ReportEventPublisherPort;
import com.planwith.planwith_fo_report.application.report.port.out.ReportRepositoryPort;
import com.planwith.planwith_fo_report.application.report.result.ReportResult;
import com.planwith.planwith_fo_report.domain.report.Report;
import com.planwith.planwith_fo_report.domain.report.ReportReason;
import com.planwith.planwith_fo_report.domain.report.ReportStatus;
import com.planwith.planwith_fo_report.domain.report.TargetType;
import com.planwith.planwith_fo_report.domain.report.event.ReportCreated;
import com.planwith.planwith_fo_report.domain.report.exception.DuplicateReportException;

@ExtendWith(MockitoExtension.class)
class CreateReportServiceTest {

	private static final UUID REPORTER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID TARGET_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Mock
	private ReportRepositoryPort reportRepositoryPort;

	@Mock
	private ReportEventPublisherPort reportEventPublisherPort;

	@Mock
	private ModerationOutboxPort moderationOutboxPort;

	private CreateReportService createReportService;

	@BeforeEach
	void setUp() {
		ReportEventDispatcher dispatcher = new ReportEventDispatcher(reportEventPublisherPort, moderationOutboxPort);
		createReportService = new CreateReportService(reportRepositoryPort, dispatcher);
	}

	@Test
	void createReportSavesReceivedReportAndPublishesCreatedEvent() {
		given(reportRepositoryPort.existsByReporterAndTarget(REPORTER_UUID, TargetType.STORY, TARGET_UUID))
				.willReturn(false);
		given(reportRepositoryPort.save(any(Report.class))).willAnswer(invocation -> invocation.getArgument(0));

		ReportResult result = createReportService.createReport(new CreateReportCommand(
				REPORTER_UUID,
				TargetType.STORY,
				TARGET_UUID,
				ReportReason.SPAM,
				"광고"
		));

		assertThat(result.status()).isEqualTo(ReportStatus.RECEIVED);
		assertThat(result.targetUuid()).isEqualTo(TARGET_UUID);
		verify(reportEventPublisherPort).publish(any(ReportCreated.class));
		verify(moderationOutboxPort, never()).save(any());
	}

	@Test
	void createReportRejectsDuplicateTarget() {
		given(reportRepositoryPort.existsByReporterAndTarget(REPORTER_UUID, TargetType.COMMENT, TARGET_UUID))
				.willReturn(true);

		assertThatThrownBy(() -> createReportService.createReport(new CreateReportCommand(
				REPORTER_UUID,
				TargetType.COMMENT,
				TARGET_UUID,
				ReportReason.HATE,
				null
		))).isInstanceOf(DuplicateReportException.class);

		verify(reportRepositoryPort, never()).save(any());
	}

	@Test
	void createReportDoesNotUseInternalTargetPk() {
		given(reportRepositoryPort.existsByReporterAndTarget(any(), any(), any())).willReturn(false);
		given(reportRepositoryPort.save(any(Report.class))).willAnswer(invocation -> invocation.getArgument(0));

		createReportService.createReport(new CreateReportCommand(
				REPORTER_UUID,
				TargetType.STORY,
				TARGET_UUID,
				ReportReason.ILLEGAL,
				null
		));

		ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
		verify(reportRepositoryPort).save(captor.capture());
		assertThat(captor.getValue().getTargetUuid()).isEqualTo(TARGET_UUID);
	}
}
