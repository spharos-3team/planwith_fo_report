package com.planwith.planwith_fo_report.application.report.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.CommentReportContext;

public interface CommentReportContextPort {

	Optional<CommentReportContext> findByCommentUuid(UUID commentUuid);
}
