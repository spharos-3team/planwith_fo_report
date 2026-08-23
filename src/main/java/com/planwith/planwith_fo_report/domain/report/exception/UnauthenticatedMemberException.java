package com.planwith.planwith_fo_report.domain.report.exception;

public class UnauthenticatedMemberException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public UnauthenticatedMemberException() {
		super("로그인 회원 정보가 없습니다.");
	}
}
