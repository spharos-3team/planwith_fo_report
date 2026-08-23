package com.planwith.planwith_fo_report.domain.report.event;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.ReportStatus;

public record ReportReviewed(
		UUID reportUuid,
		ReportStatus status,
		UUID reviewerUuid,
		String reviewComment,
		Instant occurredAt
) implements DomainEvent {

	@Override
	public String eventType() {
		return "ReportReviewed";
	}
}
