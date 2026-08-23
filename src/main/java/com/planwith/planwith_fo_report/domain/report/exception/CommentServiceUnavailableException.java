package com.planwith.planwith_fo_report.domain.report.exception;

public class CommentServiceUnavailableException extends RuntimeException {

	public CommentServiceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommentServiceUnavailableException() {
		super("댓글 서비스 확인에 실패했습니다.");
	}
}
