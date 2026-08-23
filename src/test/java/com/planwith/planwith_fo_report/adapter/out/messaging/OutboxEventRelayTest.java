package com.planwith.planwith_fo_report.adapter.out.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import com.planwith.planwith_fo_report.adapter.out.persistence.outbox.OutboxEventJpaEntity;
import com.planwith.planwith_fo_report.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import com.planwith.planwith_fo_report.adapter.out.persistence.outbox.OutboxStatus;
import com.planwith.planwith_fo_report.config.KafkaAppProperties;
import com.planwith.planwith_fo_report.config.OutboxProperties;
import com.planwith.planwith_fo_report.domain.report.event.CommentReportThresholdReachedEvent;

@ExtendWith(MockitoExtension.class)
class OutboxEventRelayTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final String HIDE_TOPIC = "planwith.report.comment-report-threshold-reached";
	private static final String MODERATION_TOPIC = "planwith.report.moderation-action-required";

	@Mock
	private OutboxEventJpaRepository outboxEventJpaRepository;

	@Mock
	private KafkaTemplate<String, String> kafkaTemplate;

	private OutboxEventRelay outboxEventRelay;

	@BeforeEach
	void setUp() {
		outboxEventRelay = new OutboxEventRelay(
				outboxEventJpaRepository,
				kafkaTemplate,
				new KafkaAppProperties(
						true,
						new KafkaAppProperties.Topics(
								"planwith.report.created",
								"planwith.report.reviewed",
								MODERATION_TOPIC,
								HIDE_TOPIC
						)
				),
				new OutboxProperties(true, 5000L, 10)
		);
	}

	@Test
	void publishesThresholdReachedEventToCommentHideTopic() {
		OutboxEventJpaEntity event = OutboxEventJpaEntity.pending(
				UUID.randomUUID().toString(),
				CommentReportThresholdReachedEvent.EVENT_TYPE,
				"COMMENT",
				COMMENT_UUID.toString(),
				"{\"commentUuid\":\"" + COMMENT_UUID + "\",\"reportCount\":3,\"threshold\":3}",
				Instant.now()
		);
		given(outboxEventJpaRepository.findByStatusOrderByCreatedAtAsc(eq(OutboxStatus.PENDING), any(Pageable.class)))
				.willReturn(List.of(event));

		outboxEventRelay.publishPendingEvents();

		verify(kafkaTemplate).send(HIDE_TOPIC, COMMENT_UUID.toString(), event.getPayload());
	}

	@Test
	void publishesModerationEventToModerationTopic() {
		OutboxEventJpaEntity event = OutboxEventJpaEntity.pending(
				UUID.randomUUID().toString(),
				"ModerationActionRequired",
				"REPORT",
				"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
				"{\"eventType\":\"ModerationActionRequired\"}",
				Instant.now()
		);
		given(outboxEventJpaRepository.findByStatusOrderByCreatedAtAsc(eq(OutboxStatus.PENDING), any(Pageable.class)))
				.willReturn(List.of(event));

		outboxEventRelay.publishPendingEvents();

		verify(kafkaTemplate).send(MODERATION_TOPIC, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", event.getPayload());
	}

	@Test
	void skipsPublishWhenKafkaDisabled() {
		outboxEventRelay = new OutboxEventRelay(
				outboxEventJpaRepository,
				kafkaTemplate,
				new KafkaAppProperties(
						false,
						new KafkaAppProperties.Topics(
								"planwith.report.created",
								"planwith.report.reviewed",
								MODERATION_TOPIC,
								HIDE_TOPIC
						)
				),
				new OutboxProperties(true, 5000L, 10)
		);

		outboxEventRelay.publishPendingEvents();

		verify(outboxEventJpaRepository, never()).findByStatusOrderByCreatedAtAsc(any(), any());
		verify(kafkaTemplate, never()).send(any(), any(), any());
	}
}
