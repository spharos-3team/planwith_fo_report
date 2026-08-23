package com.planwith.planwith_fo_report.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_report.application.report.result.ReportResult;
import com.planwith.planwith_fo_report.domain.report.ReportReason;
import com.planwith.planwith_fo_report.domain.report.ReportStatus;
import com.planwith.planwith_fo_report.domain.report.TargetType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "신고 응답")
public record ReportResponse(
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

	public static ReportResponse from(ReportResult result) {
		return new ReportResponse(
				result.reportUuid(),
				result.reporterUuid(),
				result.targetType(),
				result.targetUuid(),
				result.reason(),
				result.detail(),
				result.status(),
				result.reviewerUuid(),
				result.reviewComment(),
				result.createdAt(),
				result.updatedAt(),
				result.reviewedAt(),
				result.actionedAt()
		);
	}
}
