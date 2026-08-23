package com.planwith.planwith_fo_report.adapter.in.web.auth;

public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("아이디 또는 비밀번호가 올바르지 않습니다.");
	}
}
