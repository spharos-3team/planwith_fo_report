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
		int hideThreshold = commentReportThreshold.hideThreshold();
		if (reportCount < hideThreshold) {
			log.debug(
					"CommentReportThresholdHandler : handle : 신고 임계치 미달 - commentUuid={}, reportCount={}, hideThreshold={}",
					commentUuid,
					reportCount,
					hideThreshold
			);
			return false;
		}

		if (reportCount > hideThreshold) {
			log.debug(
					"CommentReportThresholdHandler : handle : 신고 임계치 이미 도달, 숨김 이벤트 재발행 생략 - commentUuid={}, reportCount={}, hideThreshold={}",
					commentUuid,
					reportCount,
					hideThreshold
			);
			return false;
		}

		log.info(
				"CommentReportThresholdHandler : handle : 신고 임계치 최초 도달, 댓글 숨김 처리 요청 - commentUuid={}, commentReportUuid={}, reportCount={}, threshold={}",
				commentUuid,
				commentReportUuid,
				reportCount,
				hideThreshold
		);
		commentHideRequestPort.requestHide(commentUuid, reportCount, hideThreshold);
		return true;
	}
}
