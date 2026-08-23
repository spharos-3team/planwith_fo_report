package com.planwith.planwith_fo_report.adapter.out.persistence.report;

import java.time.Instant;

import com.planwith.planwith_fo_report.domain.report.ReportReason;
import com.planwith.planwith_fo_report.domain.report.ReportStatus;
import com.planwith.planwith_fo_report.domain.report.TargetType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
		name = "report",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_report_reporter_target",
						columnNames = {"reporter_uuid", "target_type", "target_uuid"}
				)
		},
		indexes = {
				@Index(name = "idx_report_target", columnList = "target_type, target_uuid"),
				@Index(name = "idx_report_status", columnList = "status")
		}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ReportJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "report_uuid", nullable = false, unique = true, length = 36)
	private String reportUuid;

	@Column(name = "reporter_uuid", nullable = false, length = 36)
	private String reporterUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_type", nullable = false, length = 20)
	private TargetType targetType;

	@Column(name = "target_uuid", nullable = false, length = 36)
	private String targetUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "reason", nullable = false, length = 30)
	private ReportReason reason;

	@Column(name = "detail", length = 1000)
	private String detail;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ReportStatus status;

	@Column(name = "reviewer_uuid", length = 36)
	private String reviewerUuid;

	@Column(name = "review_comment", length = 1000)
	private String reviewComment;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "reviewed_at")
	private Instant reviewedAt;

	@Column(name = "actioned_at")
	private Instant actionedAt;

	void updateFromDomain(
			ReportStatus status,
			String reviewerUuid,
			String reviewComment,
			Instant updatedAt,
			Instant reviewedAt,
			Instant actionedAt
	) {
		this.status = status;
		this.reviewerUuid = reviewerUuid;
		this.reviewComment = reviewComment;
		this.updatedAt = updatedAt;
		this.reviewedAt = reviewedAt;
		this.actionedAt = actionedAt;
	}

	static ReportJpaEntity fromDomain(com.planwith.planwith_fo_report.domain.report.Report report) {
		ReportJpaEntity entity = new ReportJpaEntity();
		entity.reportUuid = report.getReportUuid().toString();
		entity.reporterUuid = report.getReporterUuid().toString();
		entity.targetType = report.getTargetType();
		entity.targetUuid = report.getTargetUuid().toString();
		entity.reason = report.getReason();
		entity.detail = report.getDetail();
		entity.status = report.getStatus();
		entity.reviewerUuid = report.getReviewerUuid() == null ? null : report.getReviewerUuid().toString();
		entity.reviewComment = report.getReviewComment();
		entity.createdAt = report.getCreatedAt();
		entity.updatedAt = report.getUpdatedAt();
		entity.reviewedAt = report.getReviewedAt();
		entity.actionedAt = report.getActionedAt();
		return entity;
	}
}
