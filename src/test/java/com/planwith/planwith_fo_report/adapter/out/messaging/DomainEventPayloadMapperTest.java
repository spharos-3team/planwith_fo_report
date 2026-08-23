package com.planwith.planwith_fo_report.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_report.domain.report.event.CommentReportThresholdReachedEvent;

class DomainEventPayloadMapperTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Test
	void mapsThresholdReachedEventForCommentService() {
		CommentReportThresholdReachedEvent event = CommentReportThresholdReachedEvent.of(COMMENT_UUID, 3L, 3);

		String payload = DomainEventPayloadMapper.toJson(event);

		assertThat(payload).contains("\"eventType\":\"COMMENT_REPORT_THRESHOLD_REACHED\"");
		assertThat(payload).contains("\"eventUuid\":\"" + event.eventUuid() + "\"");
		assertThat(payload).contains("\"commentUuid\":\"" + COMMENT_UUID + "\"");
		assertThat(payload).contains("\"reportCount\":3");
		assertThat(payload).contains("\"threshold\":3");
		assertThat(payload).contains("\"occurredAt\":\"" + event.occurredAt() + "\"");
	}
}
