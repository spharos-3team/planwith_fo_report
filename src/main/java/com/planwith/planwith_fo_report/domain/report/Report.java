package com.planwith.planwith_fo_report.domain.report;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.event.DomainEvent;
import com.planwith.planwith_fo_report.domain.report.event.ModerationActionRequired;
import com.planwith.planwith_fo_report.domain.report.event.ReportCreated;
import com.planwith.planwith_fo_report.domain.report.event.ReportReviewed;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportStatusTransitionException;

public class Report {

	private static final int MAX_DETAIL_LENGTH = 1000;
	private static final int MAX_REVIEW_COMMENT_LENGTH = 1000;

	private final UUID reportUuid;
	private final UUID reporterUuid;
	private final TargetType targetType;
	private final UUID targetUuid;
	private final ReportReason reason;
	private final String detail;
	private ReportStatus status;
	private UUID reviewerUuid;
	private String reviewComment;
	private final Instant createdAt;
	private Instant updatedAt;
	private Instant reviewedAt;
	private Instant actionedAt;
	private final List<DomainEvent> domainEvents = new ArrayList<>();

	private Report(
			UUID reportUuid,
			UUID reporterUuid,
			TargetType targetType,
			UUID targetUuid,
			ReportReason reason,
			String detail,
			ReportStatus status,
			UUID reviewerUuid,
			String reviewComment,
			Instant createdAt,
			Instant updatedAt,
			Instant reviewedAt,
			Instant actionedAt
	) {
		this.reportUuid = reportUuid;
		this.reporterUuid = reporterUuid;
		this.targetType = targetType;
		this.targetUuid = targetUuid;
		this.reason = reason;
		this.detail = detail;
		this.status = status;
		this.reviewerUuid = reviewerUuid;
		this.reviewComment = reviewComment;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.reviewedAt = reviewedAt;
		this.actionedAt = actionedAt;
	}

	public static Report create(
			UUID reporterUuid,
			TargetType targetType,
			UUID targetUuid,
			ReportReason reason,
			String detail
	) {
		validateCreate(reporterUuid, targetType, targetUuid, reason, detail);

		Instant now = Instant.now();
		UUID reportUuid = UUID.randomUUID();
		Report report = new Report(
				reportUuid,
				reporterUuid,
				targetType,
				targetUuid,
				reason,
				normalizeBlank(detail),
				ReportStatus.RECEIVED,
				null,
				null,
				now,
				now,
				null,
				null
		);
		report.domainEvents.add(new ReportCreated(
				reportUuid,
				reporterUuid,
				targetType,
				targetUuid,
				reason,
				now
		));
		return report;
	}

	public static Report restore(
			UUID reportUuid,
			UUID reporterUuid,
			TargetType targetType,
			UUID targetUuid,
			ReportReason reason,
			String detail,
			ReportStatus status,
			UUID reviewerUuid,
			String reviewComment,
			Instant createdAt,
			Instant updatedAt,
			Instant reviewedAt,
			Instant actionedAt
	) {
		return new Report(
				reportUuid,
				reporterUuid,
				targetType,
				targetUuid,
				reason,
				detail,
				status,
				reviewerUuid,
				reviewComment,
				createdAt,
				updatedAt,
				reviewedAt,
				actionedAt
		);
	}

	public void startReview(UUID reviewerUuid) {
		validateReviewer(reviewerUuid);
		transitionTo(ReportStatus.REVIEWING);
		this.reviewerUuid = reviewerUuid;
		this.updatedAt = Instant.now();
	}

	public void approve(UUID reviewerUuid, String reviewComment) {
		validateReviewer(reviewerUuid);
		validateReviewComment(reviewComment);
		transitionTo(ReportStatus.APPROVED);

		Instant now = Instant.now();
		this.reviewerUuid = reviewerUuid;
		this.reviewComment = normalizeBlank(reviewComment);
		this.reviewedAt = now;
		this.updatedAt = now;
		this.domainEvents.add(new ReportReviewed(
				reportUuid,
				ReportStatus.APPROVED,
				reviewerUuid,
				this.reviewComment,
				now
		));
		this.domainEvents.add(new ModerationActionRequired(
				reportUuid,
				targetType,
				targetUuid,
				reviewerUuid,
				now
		));
	}

	public void reject(UUID reviewerUuid, String reviewComment) {
		validateReviewer(reviewerUuid);
		validateReviewComment(reviewComment);
		transitionTo(ReportStatus.REJECTED);

		Instant now = Instant.now();
		this.reviewerUuid = reviewerUuid;
		this.reviewComment = normalizeBlank(reviewComment);
		this.reviewedAt = now;
		this.updatedAt = now;
		this.domainEvents.add(new ReportReviewed(
				reportUuid,
				ReportStatus.REJECTED,
				reviewerUuid,
				this.reviewComment,
				now
		));
	}

	public void markActioned() {
		transitionTo(ReportStatus.ACTIONED);
		Instant now = Instant.now();
		this.actionedAt = now;
		this.updatedAt = now;
	}

	public List<DomainEvent> pullDomainEvents() {
		List<DomainEvent> events = List.copyOf(domainEvents);
		domainEvents.clear();
		return events;
	}

	public List<DomainEvent> domainEvents() {
		return Collections.unmodifiableList(domainEvents);
	}

	private void transitionTo(ReportStatus nextStatus) {
		if (!status.canTransitionTo(nextStatus)) {
			throw new InvalidReportStatusTransitionException(status, nextStatus);
		}
		this.status = nextStatus;
	}

	private static void validateCreate(
			UUID reporterUuid,
			TargetType targetType,
			UUID targetUuid,
			ReportReason reason,
			String detail
	) {
		if (reporterUuid == null) {
			throw new InvalidReportException("신고자 UUID는 필수입니다.");
		}
		if (targetType == null) {
			throw new InvalidReportException("신고 대상 유형은 필수입니다.");
		}
		if (targetUuid == null) {
			throw new InvalidReportException("신고 대상 UUID는 필수입니다.");
		}
		if (reason == null) {
			throw new InvalidReportException("신고 사유는 필수입니다.");
		}
		if (detail != null && detail.length() > MAX_DETAIL_LENGTH) {
			throw new InvalidReportException("신고 상세 내용은 " + MAX_DETAIL_LENGTH + "자를 초과할 수 없습니다.");
		}
	}

	private static void validateReviewer(UUID reviewerUuid) {
		if (reviewerUuid == null) {
			throw new InvalidReportException("검토자 UUID는 필수입니다.");
		}
	}

	private static void validateReviewComment(String reviewComment) {
		if (reviewComment != null && reviewComment.length() > MAX_REVIEW_COMMENT_LENGTH) {
			throw new InvalidReportException("검토 의견은 " + MAX_REVIEW_COMMENT_LENGTH + "자를 초과할 수 없습니다.");
		}
	}

	private static String normalizeBlank(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value;
	}

	public UUID getReportUuid() {
		return reportUuid;
	}

	public UUID getReporterUuid() {
		return reporterUuid;
	}

	public TargetType getTargetType() {
		return targetType;
	}

	public UUID getTargetUuid() {
		return targetUuid;
	}

	public ReportReason getReason() {
		return reason;
	}

	public String getDetail() {
		return detail;
	}

	public ReportStatus getStatus() {
		return status;
	}

	public UUID getReviewerUuid() {
		return reviewerUuid;
	}

	public String getReviewComment() {
		return reviewComment;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getReviewedAt() {
		return reviewedAt;
	}

	public Instant getActionedAt() {
		return actionedAt;
	}
}
