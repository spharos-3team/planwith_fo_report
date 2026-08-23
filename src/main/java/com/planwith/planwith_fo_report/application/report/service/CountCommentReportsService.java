package com.planwith.planwith_fo_report.application.report.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.port.in.CountCommentReportsUseCase;
import com.planwith.planwith_fo_report.application.report.port.out.StoryCommentReportRepository;
import com.planwith.planwith_fo_report.application.report.result.CommentReportCountResult;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountCommentReportsService implements CountCommentReportsUseCase {

	private final StoryCommentReportRepository storyCommentReportRepository;

	@Override
	@Transactional(readOnly = true)
	public CommentReportCountResult count(UUID commentUuid) {
		if (commentUuid == null) {
			throw new InvalidReportException("댓글 UUID는 필수입니다.");
		}

		log.debug("CountCommentReportsService : count : 댓글 신고 누적 집계 시작 - commentUuid={}", commentUuid);

		long reportCount = storyCommentReportRepository.countByCommentUuid(commentUuid);

		log.info(
				"CountCommentReportsService : count : 댓글 신고 누적 집계 완료 - commentUuid={}, reportCount={}",
				commentUuid,
				reportCount
		);
		return new CommentReportCountResult(commentUuid, reportCount);
	}
}
