package com.planwith.planwith_fo_report.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_report.adapter.out.persistence.outbox.OutboxEventJpaEntity;
import com.planwith.planwith_fo_report.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import com.planwith.planwith_fo_report.domain.report.event.CommentReportThresholdReachedEvent;

@ExtendWith(MockitoExtension.class)
class CommentHideRequestAdapterTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Mock
	private OutboxEventJpaRepository outboxEventJpaRepository;

	private CommentHideRequestAdapter commentHideRequestAdapter;

	@BeforeEach
	void setUp() {
		commentHideRequestAdapter = new CommentHideRequestAdapter(outboxEventJpaRepository);
	}

	@Test
	void savesThresholdReachedEventToOutboxWithoutTouchingCommentDb() {
		commentHideRequestAdapter.requestHide(COMMENT_UUID, 3L, 3);

		ArgumentCaptor<OutboxEventJpaEntity> captor = ArgumentCaptor.forClass(OutboxEventJpaEntity.class);
		verify(outboxEventJpaRepository).save(captor.capture());
		OutboxEventJpaEntity saved = captor.getValue();

		assertThat(saved.getEventType()).isEqualTo(CommentReportThresholdReachedEvent.EVENT_TYPE);
		assertThat(saved.getAggregateType()).isEqualTo("COMMENT");
		assertThat(saved.getAggregateUuid()).isEqualTo(COMMENT_UUID.toString());
		assertThat(saved.getPayload()).contains("\"commentUuid\":\"" + COMMENT_UUID + "\"");
		assertThat(saved.getPayload()).contains("\"reportCount\":3");
		assertThat(saved.getPayload()).contains("\"threshold\":3");
		assertThat(saved.getPayload()).contains("\"eventUuid\":\"" + saved.getEventUuid() + "\"");
		assertThat(saved.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
	}
}
