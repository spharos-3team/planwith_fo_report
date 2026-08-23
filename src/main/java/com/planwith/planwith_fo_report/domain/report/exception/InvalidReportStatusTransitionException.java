package com.planwith.planwith_fo_report.domain.report.exception;

import com.planwith.planwith_fo_report.domain.report.ReportStatus;

public class InvalidReportStatusTransitionException extends RuntimeException {

	public InvalidReportStatusTransitionException(ReportStatus currentStatus, ReportStatus nextStatus) {
		super("신고 상태를 " + currentStatus + "에서 " + nextStatus + "로 변경할 수 없습니다.");
	}
}
