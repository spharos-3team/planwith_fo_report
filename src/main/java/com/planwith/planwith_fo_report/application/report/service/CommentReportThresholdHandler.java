package com.planwith.planwith_fo_report.application.report.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_report.application.report.port.out.CommentHideRequestPort;
import com.planwith.planwith_fo_report.domain.report.CommentReportThreshold;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentReportThresholdHandler {

	private final CommentReportThreshold commentReportThreshold;
	private final CommentHideRequestPort commentHideRequestPort;

	public boolean handle(UUID commentUuid, UUID commentReportUuid, long reportCount) {
		if (!commentReportThreshold.isReached(reportCount)) {
			log.debug(
					"CommentReportThresholdHandler : handle : 신고 임계치 미달 - commentUuid={}, reportCount={}, hideThreshold={}",
					commentUuid,
					reportCount,
					commentReportThreshold.hideThreshold()
			);
			return false;
		}

		log.info(
				"CommentReportThresholdHandler : handle : 신고 임계치 도달, 댓글 숨김 처리 요청 - commentUuid={}, commentReportUuid={}, reportCount={}",
				commentUuid,
				commentReportUuid,
				reportCount
		);
		commentHideRequestPort.requestHide(commentUuid, commentReportUuid, reportCount);
		return true;
	}
}
