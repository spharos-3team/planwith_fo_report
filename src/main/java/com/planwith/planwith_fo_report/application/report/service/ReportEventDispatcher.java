package com.planwith.planwith_fo_report.application.report.service;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_report.application.report.port.out.ModerationOutboxPort;
import com.planwith.planwith_fo_report.application.report.port.out.ReportEventPublisherPort;
import com.planwith.planwith_fo_report.domain.report.Report;
import com.planwith.planwith_fo_report.domain.report.event.DomainEvent;
import com.planwith.planwith_fo_report.domain.report.event.ModerationActionRequired;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class ReportEventDispatcher {

	private final ReportEventPublisherPort reportEventPublisherPort;
	private final ModerationOutboxPort moderationOutboxPort;

	void dispatch(Report report) {
		for (DomainEvent event : report.pullDomainEvents()) {
			if (event instanceof ModerationActionRequired moderationEvent) {
				log.info("ReportEventDispatcher : dispatch : Moderation 이벤트 Outbox 적재 - reportUuid={}",
						moderationEvent.reportUuid());
				moderationOutboxPort.save(moderationEvent);
				continue;
			}

			log.debug("ReportEventDispatcher : dispatch : 신고 이벤트 발행 - eventType={}, report 관련 이벤트",
					event.eventType());
			reportEventPublisherPort.publish(event);
		}
	}
}
