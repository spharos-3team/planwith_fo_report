package com.planwith.planwith_fo_report.domain.report.exception;

import java.util.UUID;

public class CommentNotReportableException extends RuntimeException {

	public CommentNotReportableException(UUID commentUuid) {
		super("삭제된 댓글은 신고할 수 없습니다. commentUuid=" + commentUuid);
	}
}
