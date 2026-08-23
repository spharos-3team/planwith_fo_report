package com.planwith.planwith_fo_report.domain.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_report.domain.report.event.DomainEvent;
import com.planwith.planwith_fo_report.domain.report.event.ModerationActionRequired;
import com.planwith.planwith_fo_report.domain.report.event.ReportCreated;
import com.planwith.planwith_fo_report.domain.report.event.ReportReviewed;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportStatusTransitionException;

class ReportTest {

	private static final UUID REPORTER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID TARGET_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID REVIEWER_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Test
	void createStartsWithReceivedAndReportCreatedEvent() {
		Report report = Report.create(REPORTER_UUID, TargetType.STORY, TARGET_UUID, ReportReason.SPAM, "광고");

		assertThat(report.getStatus()).isEqualTo(ReportStatus.RECEIVED);
		assertThat(report.getTargetType()).isEqualTo(TargetType.STORY);
		assertThat(report.getTargetUuid()).isEqualTo(TARGET_UUID);
		assertThat(report.pullDomainEvents())
				.hasSize(1)
				.first()
				.isInstanceOf(ReportCreated.class);
	}

	@Test
	void workflowFollowsReceivedReviewingApprovedActioned() {
		Report report = Report.create(REPORTER_UUID, TargetType.COMMENT, TARGET_UUID, ReportReason.HARASSMENT, null);

		report.startReview(REVIEWER_UUID);
		assertThat(report.getStatus()).isEqualTo(ReportStatus.REVIEWING);

		report.approve(REVIEWER_UUID, "가이드 위반");
		assertThat(report.getStatus()).isEqualTo(ReportStatus.APPROVED);

		List<DomainEvent> events = report.pullDomainEvents();
		assertThat(events).anyMatch(ReportCreated.class::isInstance);
		assertThat(events).anyMatch(ReportReviewed.class::isInstance);
		assertThat(events).anyMatch(ModerationActionRequired.class::isInstance);

		report.markActioned();
		assertThat(report.getStatus()).isEqualTo(ReportStatus.ACTIONED);
	}

	@Test
	void rejectDoesNotRequireModerationAction() {
		Report report = Report.create(REPORTER_UUID, TargetType.STORY, TARGET_UUID, ReportReason.OTHER, "오해");
		report.pullDomainEvents();
		report.startReview(REVIEWER_UUID);
		report.reject(REVIEWER_UUID, "해당 없음");

		assertThat(report.getStatus()).isEqualTo(ReportStatus.REJECTED);
		assertThat(report.pullDomainEvents())
				.hasSize(1)
				.first()
				.isInstanceOf(ReportReviewed.class);
	}

	@Test
	void cannotSkipReviewing() {
		Report report = Report.create(REPORTER_UUID, TargetType.STORY, TARGET_UUID, ReportReason.SPAM, null);

		assertThatThrownBy(() -> report.approve(REVIEWER_UUID, "바로 승인"))
				.isInstanceOf(InvalidReportStatusTransitionException.class);
	}

	@Test
	void cannotTransitionFromRejected() {
		Report report = Report.create(REPORTER_UUID, TargetType.STORY, TARGET_UUID, ReportReason.SPAM, null);
		report.startReview(REVIEWER_UUID);
		report.reject(REVIEWER_UUID, "기각");

		assertThatThrownBy(report::markActioned)
				.isInstanceOf(InvalidReportStatusTransitionException.class);
	}

	@Test
	void createRequiresTargetUuid() {
		assertThatThrownBy(() -> Report.create(REPORTER_UUID, TargetType.STORY, null, ReportReason.SPAM, null))
				.isInstanceOf(InvalidReportException.class)
				.hasMessage("신고 대상 UUID는 필수입니다.");
	}
}
