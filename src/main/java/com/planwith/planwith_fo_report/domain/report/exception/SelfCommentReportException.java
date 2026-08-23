package com.planwith.planwith_fo_report.domain.report.exception;

public class SelfCommentReportException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SelfCommentReportException() {
		super("본인이 작성한 댓글은 신고할 수 없습니다.");
	}
}
