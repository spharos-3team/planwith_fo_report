package com.planwith.planwith_fo_report.domain.report.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;

class CommentReportThresholdReachedEventTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Test
	void createsHideEventForCommentService() {
		CommentReportThresholdReachedEvent event = CommentReportThresholdReachedEvent.of(COMMENT_UUID, 3L, 3);

		assertThat(event.eventType()).isEqualTo("COMMENT_REPORT_THRESHOLD_REACHED");
		assertThat(event.eventUuid()).isNotNull();
		assertThat(event.commentUuid()).isEqualTo(COMMENT_UUID);
		assertThat(event.reportCount()).isEqualTo(3L);
		assertThat(event.threshold()).isEqualTo(3);
		assertThat(event.occurredAt()).isNotNull();
	}

	@Test
	void rejectsMissingCommentUuid() {
		assertThatThrownBy(() -> CommentReportThresholdReachedEvent.of(null, 3L, 3))
				.isInstanceOf(InvalidReportException.class)
				.hasMessage("댓글 UUID는 필수입니다.");
	}
}
