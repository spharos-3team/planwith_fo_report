package com.planwith.planwith_fo_report.domain.report;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;

public class StoryCommentReport {

	private final Long commentReportId;
	private final UUID commentReportUuid;
	private final UUID commentUuid;
	private final UUID memberUuid;
	private final ReportType reportType;
	private final Instant createdAt;

	private StoryCommentReport(
			Long commentReportId,
			UUID commentReportUuid,
			UUID commentUuid,
			UUID memberUuid,
			ReportType reportType,
			Instant createdAt
	) {
		this.commentReportId = commentReportId;
		this.commentReportUuid = commentReportUuid;
		this.commentUuid = commentUuid;
		this.memberUuid = memberUuid;
		this.reportType = reportType;
		this.createdAt = createdAt;
	}

	public static StoryCommentReport create(UUID commentUuid, UUID memberUuid, ReportType reportType) {
		validate(commentUuid, memberUuid, reportType);
		return new StoryCommentReport(
				null,
				UUID.randomUUID(),
				commentUuid,
				memberUuid,
				reportType,
				Instant.now()
		);
	}

	public static StoryCommentReport restore(
			Long commentReportId,
			UUID commentReportUuid,
			UUID commentUuid,
			UUID memberUuid,
			ReportType reportType,
			Instant createdAt
	) {
		return new StoryCommentReport(
				commentReportId,
				commentReportUuid,
				commentUuid,
				memberUuid,
				reportType,
				createdAt
		);
	}

	private static void validate(UUID commentUuid, UUID memberUuid, ReportType reportType) {
		if (commentUuid == null) {
			throw new InvalidReportException("댓글 UUID는 필수입니다.");
		}
		if (memberUuid == null) {
			throw new InvalidReportException("회원 UUID는 필수입니다.");
		}
		if (reportType == null) {
			throw new InvalidReportException("신고 사유는 필수입니다.");
		}
	}

	public Long getCommentReportId() {
		return commentReportId;
	}

	public UUID getCommentReportUuid() {
		return commentReportUuid;
	}

	public UUID getCommentUuid() {
		return commentUuid;
	}

	public UUID getMemberUuid() {
		return memberUuid;
	}

	public ReportType getReportType() {
		return reportType;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
