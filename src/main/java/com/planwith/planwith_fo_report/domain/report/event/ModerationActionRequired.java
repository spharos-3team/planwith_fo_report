package com.planwith.planwith_fo_report.domain.report.event;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.TargetType;

public record ModerationActionRequired(
		UUID reportUuid,
		TargetType targetType,
		UUID targetUuid,
		UUID reviewerUuid,
		Instant occurredAt
) implements DomainEvent {

	@Override
	public String eventType() {
		return "ModerationActionRequired";
	}
}
