package com.planwith.planwith_fo_report.domain.report.exception;

import java.util.UUID;

public class CommentNotFoundException extends RuntimeException {

	public CommentNotFoundException(UUID commentUuid) {
		super("존재하지 않는 댓글은 신고할 수 없습니다. commentUuid=" + commentUuid);
	}
}
