package com.planwith.planwith_fo_report.adapter.out.messaging;

import com.planwith.planwith_fo_report.domain.report.event.DomainEvent;
import com.planwith.planwith_fo_report.domain.report.event.ModerationActionRequired;
import com.planwith.planwith_fo_report.domain.report.event.ReportCreated;
import com.planwith.planwith_fo_report.domain.report.event.ReportReviewed;

final class DomainEventPayloadMapper {

	private DomainEventPayloadMapper() {
	}

	static String toJson(DomainEvent event) {
		if (event instanceof ReportCreated created) {
			return compact("""
					{
					  "eventType":"%s",
					  "reportUuid":"%s",
					  "reporterUuid":"%s",
					  "targetType":"%s",
					  "targetUuid":"%s",
					  "reason":"%s",
					  "occurredAt":"%s"
					}
					""".formatted(
					created.eventType(),
					created.reportUuid(),
					created.reporterUuid(),
					created.targetType(),
					created.targetUuid(),
					created.reason(),
					created.occurredAt()
			));
		}
		if (event instanceof ReportReviewed reviewed) {
			return compact("""
					{
					  "eventType":"%s",
					  "reportUuid":"%s",
					  "status":"%s",
					  "reviewerUuid":"%s",
					  "reviewComment":%s,
					  "occurredAt":"%s"
					}
					""".formatted(
					reviewed.eventType(),
					reviewed.reportUuid(),
					reviewed.status(),
					reviewed.reviewerUuid(),
					toJsonString(reviewed.reviewComment()),
					reviewed.occurredAt()
			));
		}
		if (event instanceof ModerationActionRequired required) {
			return compact("""
					{
					  "eventType":"%s",
					  "reportUuid":"%s",
					  "targetType":"%s",
					  "targetUuid":"%s",
					  "reviewerUuid":"%s",
					  "occurredAt":"%s"
					}
					""".formatted(
					required.eventType(),
					required.reportUuid(),
					required.targetType(),
					required.targetUuid(),
					required.reviewerUuid(),
					required.occurredAt()
			));
		}
		return compact("""
				{
				  "eventType":"%s",
				  "occurredAt":"%s"
				}
				""".formatted(event.eventType(), event.occurredAt()));
	}

	private static String toJsonString(String value) {
		if (value == null) {
			return "null";
		}
		return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}

	private static String compact(String json) {
		return json.replaceAll("\\s+", " ").trim();
	}
}
