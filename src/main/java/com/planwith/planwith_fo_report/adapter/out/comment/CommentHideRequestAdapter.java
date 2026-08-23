package com.planwith.planwith_fo_report.adapter.out.comment;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.port.out.CommentHideRequestPort;
import com.planwith.planwith_fo_report.application.report.port.out.ModerationOutboxPort;
import com.planwith.planwith_fo_report.domain.report.TargetType;
import com.planwith.planwith_fo_report.domain.report.event.ModerationActionRequired;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class CommentHideRequestAdapter implements CommentHideRequestPort {

	private final ModerationOutboxPort moderationOutboxPort;

	@Override
	@Transactional
	public void requestHide(UUID commentUuid, UUID commentReportUuid, long reportCount) {
		moderationOutboxPort.save(new ModerationActionRequired(
				commentReportUuid,
				TargetType.COMMENT,
				commentUuid,
				null,
				Instant.now()
		));
		log.info(
				"CommentHideRequestAdapter : requestHide : 댓글 숨김 처리 요청 Outbox 적재 - commentUuid={}, commentReportUuid={}, reportCount={}",
				commentUuid,
				commentReportUuid,
				reportCount
		);
	}
}
