package com.planwith.planwith_fo_report.adapter.out.messaging;

import java.time.Instant;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.adapter.out.persistence.outbox.OutboxEventJpaEntity;
import com.planwith.planwith_fo_report.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import com.planwith.planwith_fo_report.adapter.out.persistence.outbox.OutboxStatus;
import com.planwith.planwith_fo_report.config.KafkaAppProperties;
import com.planwith.planwith_fo_report.config.OutboxProperties;
import com.planwith.planwith_fo_report.domain.report.event.CommentReportThresholdReachedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.outbox", name = "relay-enabled", havingValue = "true")
class OutboxEventRelay {

	private final OutboxEventJpaRepository outboxEventJpaRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final KafkaAppProperties kafkaAppProperties;
	private final OutboxProperties outboxProperties;

	@Scheduled(fixedDelayString = "${app.outbox.relay-interval-ms:5000}")
	@Transactional
	public void publishPendingEvents() {
		if (!kafkaAppProperties.enabled()) {
			log.warn("OutboxEventRelay : publishPendingEvents : Kafka가 비활성화되어 Outbox Relay를 건너뜀");
			return;
		}

		List<OutboxEventJpaEntity> pendingEvents = outboxEventJpaRepository.findByStatusOrderByCreatedAtAsc(
				OutboxStatus.PENDING,
				PageRequest.of(0, Math.max(outboxProperties.batchSize(), 1))
		);
		if (pendingEvents.isEmpty()) {
			return;
		}

		log.info("OutboxEventRelay : publishPendingEvents : Outbox 대기 이벤트 발행 시작 - count={}", pendingEvents.size());

		for (OutboxEventJpaEntity event : pendingEvents) {
			try {
				String topic = resolveTopic(event.getEventType());
				if (topic == null) {
					log.warn(
							"OutboxEventRelay : publishPendingEvents : 발행 대상이 아닌 Outbox 이벤트 - eventUuid={}, eventType={}",
							event.getEventUuid(),
							event.getEventType()
					);
					continue;
				}
				kafkaTemplate.send(topic, event.getAggregateUuid(), event.getPayload());
				event.markPublished(Instant.now());
				log.info(
						"OutboxEventRelay : publishPendingEvents : Outbox 이벤트 Kafka 발행 완료 - eventUuid={}, eventType={}, topic={}",
						event.getEventUuid(),
						event.getEventType(),
						topic
				);
			} catch (RuntimeException exception) {
				log.error("OutboxEventRelay : publishPendingEvents : Outbox 이벤트 발행 실패 - eventUuid={}",
						event.getEventUuid(),
						exception);
				event.markFailed();
			}
		}
	}

	private String resolveTopic(String eventType) {
		if (CommentReportThresholdReachedEvent.EVENT_TYPE.equals(eventType)) {
			return kafkaAppProperties.topics().commentReportThresholdReached();
		}
		if ("ModerationActionRequired".equals(eventType)) {
			return kafkaAppProperties.topics().moderationActionRequired();
		}
		return null;
	}
}
