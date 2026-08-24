package com.planwith.planwith_fo_report.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_report.application.report.command.ReportWorkflowAction;
import com.planwith.planwith_fo_report.application.report.command.ReviewReportCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "신고 워크플로우 처리 요청")
public record ReviewReportRequest(
		@Schema(description = "워크플로우 액션", example = "START_REVIEW")
		@NotNull(message = "워크플로우 액션은 필수입니다.")
		ReportWorkflowAction action,

		@Schema(description = "호환성 유지용 검토자 UUID. 실제 검토자는 인증 헤더를 사용한다.", example = "33333333-3333-3333-3333-333333333333")
		UUID reviewerUuid,

		@Schema(description = "검토 의견", example = "커뮤니티 가이드 위반으로 확인")
		@Size(max = 1000, message = "검토 의견은 1000자를 초과할 수 없습니다.")
		String reviewComment
) {

	public ReviewReportCommand toCommand(UUID reportUuid, UUID authenticatedReviewerUuid) {
		return new ReviewReportCommand(reportUuid, action, authenticatedReviewerUuid, reviewComment);
	}
}
