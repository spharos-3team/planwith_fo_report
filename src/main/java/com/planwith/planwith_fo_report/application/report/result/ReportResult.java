package com.planwith.planwith_fo_report.application.report.result;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.Report;
import com.planwith.planwith_fo_report.domain.report.ReportReason;
import com.planwith.planwith_fo_report.domain.report.ReportStatus;
import com.planwith.planwith_fo_report.domain.report.TargetType;

public record ReportResult(
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

	public static ReportResult from(Report report) {
		return new ReportResult(
				report.getReportUuid(),
				report.getReporterUuid(),
				report.getTargetType(),
				report.getTargetUuid(),
				report.getReason(),
				report.getDetail(),
				report.getStatus(),
				report.getReviewerUuid(),
				report.getReviewComment(),
				report.getCreatedAt(),
				report.getUpdatedAt(),
				report.getReviewedAt(),
				report.getActionedAt()
		);
	}
}
