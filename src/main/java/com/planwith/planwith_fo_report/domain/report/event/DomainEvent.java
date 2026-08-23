package com.planwith.planwith_fo_report.domain.report.event;

import java.time.Instant;

public interface DomainEvent {

	String eventType();

	Instant occurredAt();
}
