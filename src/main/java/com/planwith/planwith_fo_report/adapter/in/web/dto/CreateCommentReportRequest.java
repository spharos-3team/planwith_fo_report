package com.planwith.planwith_fo_report.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_report.application.report.command.CreateCommentReportCommand;
import com.planwith.planwith_fo_report.domain.report.ReportType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "댓글 신고 요청. memberUuid는 요청 Body가 아니라 X-Auth-User-Id 인증 헤더에서 획득한다.")
public record CreateCommentReportRequest(
		@Schema(description = "신고 대상 댓글 UUID", example = "22222222-2222-2222-2222-222222222222")
		@NotNull(message = "댓글 UUID는 필수입니다.")
		UUID commentUuid,

		@Schema(description = "신고 사유", example = "SPAM", allowableValues = {
				"SPAM", "ABUSE", "HATE", "SEXUAL", "PRIVACY", "OTHER"
		})
		@NotNull(message = "신고 사유는 필수입니다.")
		ReportType reportType
) {

	public CreateCommentReportCommand toCommand(UUID memberUuid) {
		return new CreateCommentReportCommand(commentUuid, reportType, memberUuid);
	}
}
