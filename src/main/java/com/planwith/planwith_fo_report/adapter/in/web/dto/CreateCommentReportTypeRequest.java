package com.planwith.planwith_fo_report.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_report.application.report.command.CreateCommentReportCommand;
import com.planwith.planwith_fo_report.domain.report.ReportType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "댓글 신고 생성 요청. commentUuid는 경로 변수, memberUuid는 X-Auth-User-Id 헤더에서 획득한다.")
public record CreateCommentReportTypeRequest(
		@Schema(description = "신고 사유", example = "HATE", allowableValues = {
				"SPAM", "ABUSE", "HATE", "SEXUAL", "PRIVACY", "OTHER"
		})
		@NotNull(message = "신고 사유는 필수입니다.")
		ReportType reportType
) {

	public CreateCommentReportCommand toCommand(UUID commentUuid, UUID memberUuid) {
		return new CreateCommentReportCommand(commentUuid, reportType, memberUuid);
	}
}
