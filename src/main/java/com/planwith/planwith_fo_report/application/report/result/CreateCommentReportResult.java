package com.planwith.planwith_fo_report.application.report.result;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.ReportType;
import com.planwith.planwith_fo_report.domain.report.StoryCommentReport;

public record CreateCommentReportResult(
		UUID commentReportUuid,
		UUID commentUuid,
		ReportType reportType,
		Instant createdAt,
		long reportCount
) {

	public static CreateCommentReportResult from(StoryCommentReport report, long reportCount) {
		return new CreateCommentReportResult(
				report.getCommentReportUuid(),
				report.getCommentUuid(),
				report.getReportType(),
				report.getCreatedAt(),
				reportCount
		);
	}
}
