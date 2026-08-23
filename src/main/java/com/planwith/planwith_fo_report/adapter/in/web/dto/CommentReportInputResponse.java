package com.planwith.planwith_fo_report.adapter.in.web.dto;

import java.util.UUID;

import com.planwith.planwith_fo_report.application.report.result.CommentReportInputResult;
import com.planwith.planwith_fo_report.domain.report.ReportType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 신고 입력 검증 응답")
public record CommentReportInputResponse(
		UUID commentUuid,
		ReportType reportType,
		UUID authorMemberUuid,
		boolean reportable
) {

	public static CommentReportInputResponse from(CommentReportInputResult result) {
		return new CommentReportInputResponse(
				result.commentUuid(),
				result.reportType(),
				result.authorMemberUuid(),
				result.reportable()
		);
	}
}
