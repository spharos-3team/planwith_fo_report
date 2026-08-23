package com.planwith.planwith_fo_report.domain.report.exception;

import java.util.UUID;

public class ReportNotFoundException extends RuntimeException {

	public ReportNotFoundException(UUID reportUuid) {
		super("신고를 찾을 수 없습니다. reportUuid=" + reportUuid);
	}
}
