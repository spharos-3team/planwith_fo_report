package com.planwith.planwith_fo_report.domain.report.exception;

public class DuplicateReportException extends RuntimeException {

	public DuplicateReportException() {
		super("동일한 대상에 대한 신고가 이미 존재합니다.");
	}
}
