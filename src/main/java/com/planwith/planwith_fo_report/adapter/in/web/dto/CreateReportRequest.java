package com.planwith.planwith_fo_report.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_report.application.report.command.CreateReportCommand;
import com.planwith.planwith_fo_report.domain.report.ReportReason;
import com.planwith.planwith_fo_report.domain.report.TargetType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "신고 생성 요청")
public record CreateReportRequest(
		@Schema(description = "신고자 UUID", example = "11111111-1111-1111-1111-111111111111")
		@NotNull(message = "신고자 UUID는 필수입니다.")
		UUID reporterUuid,

		@Schema(description = "신고 대상 유형", example = "STORY")
		@NotNull(message = "신고 대상 유형은 필수입니다.")
		TargetType targetType,

		@Schema(description = "신고 대상 UUID. 다른 서비스 내부 PK가 아닌 target_uuid를 사용한다.", example = "22222222-2222-2222-2222-222222222222")
		@NotNull(message = "신고 대상 UUID는 필수입니다.")
		UUID targetUuid,

		@Schema(description = "신고 사유", example = "SPAM")
		@NotNull(message = "신고 사유는 필수입니다.")
		ReportReason reason,

		@Schema(description = "신고 상세 내용", example = "광고성 게시글입니다.")
		@Size(max = 1000, message = "신고 상세 내용은 1000자를 초과할 수 없습니다.")
		String detail
) {

	public CreateReportCommand toCommand() {
		return new CreateReportCommand(reporterUuid, targetType, targetUuid, reason, detail);
	}
}
