package com.planwith.planwith_fo_report.adapter.out.messaging;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.adapter.out.persistence.outbox.OutboxEventJpaEntity;
import com.planwith.planwith_fo_report.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import com.planwith.planwith_fo_report.application.report.port.out.CommentHideRequestPort;
import com.planwith.planwith_fo_report.domain.report.event.CommentReportThresholdReachedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class CommentHideRequestAdapter implements CommentHideRequestPort {

	private static final String AGGREGATE_TYPE = "COMMENT";

	private final OutboxEventJpaRepository outboxEventJpaRepository;

	@Override
	@Transactional
	public void requestHide(UUID commentUuid, long reportCount, int threshold) {
		CommentReportThresholdReachedEvent event = CommentReportThresholdReachedEvent.of(
				commentUuid,
				reportCount,
				threshold
		);
		outboxEventJpaRepository.save(OutboxEventJpaEntity.pending(
				event.eventUuid().toString(),
				event.eventType(),
				AGGREGATE_TYPE,
				event.commentUuid().toString(),
				DomainEventPayloadMapper.toJson(event),
				event.occurredAt()
		));
		log.info(
				"CommentHideRequestAdapter : requestHide : COMMENT_REPORT_THRESHOLD_REACHED Outbox 적재 - eventUuid={}, commentUuid={}, reportCount={}, threshold={}",
				event.eventUuid(),
				commentUuid,
				reportCount,
				threshold
		);
	}
}
