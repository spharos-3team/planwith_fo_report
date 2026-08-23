package com.planwith.planwith_fo_report.adapter.in.web.dto;

import java.time.Instant;

public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String code,
		String message
) {
}
