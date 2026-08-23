package com.planwith.planwith_fo_report.adapter.out.persistence.commentreport;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.ReportType;
import com.planwith.planwith_fo_report.domain.report.StoryCommentReport;

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
		name = "story_comment_report",
		uniqueConstraints = {
				@UniqueConstraint(
						name = StoryCommentReportJpaEntity.MEMBER_COMMENT_UNIQUE,
						columnNames = {"comment_uuid", "member_uuid"}
				)
		},
		indexes = {
				@Index(name = "idx_comment_report_comment", columnList = "comment_uuid")
		}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class StoryCommentReportJpaEntity {

	static final String MEMBER_COMMENT_UNIQUE = "uk_comment_report_member";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_report_id")
	private Long commentReportId;

	@Column(name = "comment_report_uuid", nullable = false, unique = true, length = 36)
	private String commentReportUuid;

	@Column(name = "comment_uuid", nullable = false, length = 36)
	private String commentUuid;

	@Column(name = "member_uuid", nullable = false, length = 36)
	private String memberUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "report_type", nullable = false, length = 20)
	private ReportType reportType;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	static StoryCommentReportJpaEntity fromDomain(StoryCommentReport report) {
		StoryCommentReportJpaEntity entity = new StoryCommentReportJpaEntity();
		entity.commentReportUuid = report.getCommentReportUuid().toString();
		entity.commentUuid = report.getCommentUuid().toString();
		entity.memberUuid = report.getMemberUuid().toString();
		entity.reportType = report.getReportType();
		entity.createdAt = report.getCreatedAt();
		return entity;
	}

	StoryCommentReport toDomain() {
		return StoryCommentReport.restore(
				commentReportId,
				UUID.fromString(commentReportUuid),
				UUID.fromString(commentUuid),
				UUID.fromString(memberUuid),
				reportType,
				createdAt
		);
	}
}
