package com.planwith.planwith_fo_report.domain.report.event;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;

public record CommentReportThresholdReachedEvent(
		UUID eventUuid,
		UUID commentUuid,
		long reportCount,
		int threshold,
		Instant occurredAt
) implements DomainEvent {

	public static final String EVENT_TYPE = "COMMENT_REPORT_THRESHOLD_REACHED";

	public static CommentReportThresholdReachedEvent of(UUID commentUuid, long reportCount, int threshold) {
		if (commentUuid == null) {
			throw new InvalidReportException("댓글 UUID는 필수입니다.");
		}
		return new CommentReportThresholdReachedEvent(
				UUID.randomUUID(),
				commentUuid,
				reportCount,
				threshold,
				Instant.now()
		);
	}

	@Override
	public String eventType() {
		return EVENT_TYPE;
	}
}
