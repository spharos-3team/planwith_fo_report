package com.planwith.planwith_fo_report.adapter.out.persistence.outbox;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.port.out.ModerationOutboxPort;
import com.planwith.planwith_fo_report.domain.report.event.ModerationActionRequired;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class ModerationOutboxAdapter implements ModerationOutboxPort {

	private static final String AGGREGATE_TYPE = "REPORT";

	private final OutboxEventJpaRepository outboxEventJpaRepository;

	@Override
	@Transactional
	public void save(ModerationActionRequired event) {
		String payload = """
				{
				  "eventType":"%s",
				  "reportUuid":"%s",
				  "targetType":"%s",
				  "targetUuid":"%s",
				  "reviewerUuid":%s,
				  "occurredAt":"%s"
				}
				""".formatted(
				event.eventType(),
				event.reportUuid(),
				event.targetType(),
				event.targetUuid(),
				toJsonUuid(event.reviewerUuid()),
				event.occurredAt()
		).replaceAll("\\s+", " ").trim();

		OutboxEventJpaEntity entity = OutboxEventJpaEntity.pending(
				UUID.randomUUID().toString(),
				event.eventType(),
				AGGREGATE_TYPE,
				event.reportUuid().toString(),
				payload,
				event.occurredAt()
		);
		outboxEventJpaRepository.save(entity);
		log.info("ModerationOutboxAdapter : save : Moderation Outbox 저장 완료 - reportUuid={}", event.reportUuid());
	}

	private static String toJsonUuid(UUID uuid) {
		if (uuid == null) {
			return "null";
		}
		return "\"" + uuid + "\"";
	}
}
