package com.planwith.planwith_fo_report.adapter.out.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_report.application.report.port.out.ReportEventPublisherPort;
import com.planwith.planwith_fo_report.domain.report.event.DomainEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
class NoOpReportEventPublisher implements ReportEventPublisherPort {

	@Override
	public void publish(DomainEvent event) {
		log.debug("NoOpReportEventPublisher : publish : Kafka 비활성화 상태로 이벤트 발행 생략 - eventType={}",
				event.eventType());
	}
}
