package com.planwith.planwith_fo_report.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_report.application.report.command.ValidateCommentReportTargetCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "댓글 신고 대상 검증 요청")
public record ValidateCommentReportTargetRequest(
		@Schema(description = "신고 대상 댓글 UUID", example = "22222222-2222-2222-2222-222222222222")
		@NotNull(message = "댓글 UUID는 필수입니다.")
		UUID commentUuid,

		@Schema(description = "신고 회원 UUID", example = "11111111-1111-1111-1111-111111111111")
		@NotNull(message = "회원 UUID는 필수입니다.")
		UUID memberUuid
) {

	public ValidateCommentReportTargetCommand toCommand() {
		return new ValidateCommentReportTargetCommand(commentUuid, memberUuid);
	}
}
