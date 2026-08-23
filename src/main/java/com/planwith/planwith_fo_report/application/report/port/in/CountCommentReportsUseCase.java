package com.planwith.planwith_fo_report.application.report.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_report.application.report.result.CommentReportCountResult;

public interface CountCommentReportsUseCase {

	CommentReportCountResult count(UUID commentUuid);
}
