package com.planwith.planwith_fo_report.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_report.application.report.result.CreateCommentReportResult;
import com.planwith.planwith_fo_report.domain.report.ReportType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 신고 생성 응답")
public record CreateCommentReportResponse(
		UUID commentReportUuid,
		UUID commentUuid,
		ReportType reportType,
		Instant createdAt,
		long reportCount,
		boolean thresholdReached,
		String message
) {

	private static final String SUCCESS_MESSAGE = "댓글을 신고했다";

	public static CreateCommentReportResponse from(CreateCommentReportResult result) {
		return new CreateCommentReportResponse(
				result.commentReportUuid(),
				result.commentUuid(),
				result.reportType(),
				result.createdAt(),
				result.reportCount(),
				result.thresholdReached(),
				SUCCESS_MESSAGE
		);
	}
}
