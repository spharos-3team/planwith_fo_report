package com.planwith.planwith_fo_report.application.report.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_report.application.report.port.out.StoryCommentReportRepository;
import com.planwith.planwith_fo_report.domain.report.exception.DuplicateCommentReportException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DuplicateCommentReportGuard {

	private final StoryCommentReportRepository storyCommentReportRepository;

	public void assertNotDuplicated(UUID commentUuid, UUID memberUuid) {
		if (storyCommentReportRepository.existsByCommentUuidAndMemberUuid(commentUuid, memberUuid)) {
			log.warn(
					"DuplicateCommentReportGuard : assertNotDuplicated : 동일 회원 동일 댓글 중복 신고 - commentUuid={}, memberUuid={}",
					commentUuid,
					memberUuid
			);
			throw new DuplicateCommentReportException();
		}
	}
}
