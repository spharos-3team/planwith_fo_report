package com.planwith.planwith_fo_report.domain.report.exception;

public class DuplicateCommentReportException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DuplicateCommentReportException() {
		super("동일 회원이 같은 댓글을 이미 신고했습니다.");
	}
}
