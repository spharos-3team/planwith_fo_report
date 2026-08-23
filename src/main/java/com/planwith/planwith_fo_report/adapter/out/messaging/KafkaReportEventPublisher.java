package com.planwith.planwith_fo_report.adapter.out.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_report.application.report.port.out.ReportEventPublisherPort;
import com.planwith.planwith_fo_report.config.KafkaAppProperties;
import com.planwith.planwith_fo_report.domain.report.event.DomainEvent;
import com.planwith.planwith_fo_report.domain.report.event.ReportCreated;
import com.planwith.planwith_fo_report.domain.report.event.ReportReviewed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
class KafkaReportEventPublisher implements ReportEventPublisherPort {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final KafkaAppProperties kafkaAppProperties;

	@Override
	public void publish(DomainEvent event) {
		String topic = resolveTopic(event);
		if (topic == null) {
			log.debug("KafkaReportEventPublisher : publish : 발행 대상이 아닌 이벤트 - eventType={}", event.eventType());
			return;
		}

		String payload = DomainEventPayloadMapper.toJson(event);
		String key = resolveKey(event);
		kafkaTemplate.send(topic, key, payload);
		log.info("KafkaReportEventPublisher : publish : 신고 이벤트 Kafka 발행 완료 - eventType={}, topic={}",
				event.eventType(),
				topic);
	}

	private String resolveTopic(DomainEvent event) {
		if (event instanceof ReportCreated) {
			return kafkaAppProperties.topics().reportCreated();
		}
		if (event instanceof ReportReviewed) {
			return kafkaAppProperties.topics().reportReviewed();
		}
		return null;
	}

	private static String resolveKey(DomainEvent event) {
		if (event instanceof ReportCreated created) {
			return created.reportUuid().toString();
		}
		if (event instanceof ReportReviewed reviewed) {
			return reviewed.reportUuid().toString();
		}
		return event.eventType();
	}
}
