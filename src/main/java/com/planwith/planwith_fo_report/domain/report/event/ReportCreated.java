package com.planwith.planwith_fo_report.domain.report.event;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.ReportReason;
import com.planwith.planwith_fo_report.domain.report.TargetType;

public record ReportCreated(
		UUID reportUuid,
		UUID reporterUuid,
		TargetType targetType,
		UUID targetUuid,
		ReportReason reason,
		Instant occurredAt
) implements DomainEvent {

	@Override
	public String eventType() {
		return "ReportCreated";
	}
}
