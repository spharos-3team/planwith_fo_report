package com.planwith.planwith_fo_report.adapter.out.persistence.commentreport;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.port.out.StoryCommentReportRepository;
import com.planwith.planwith_fo_report.domain.report.StoryCommentReport;
import com.planwith.planwith_fo_report.domain.report.exception.DuplicateCommentReportException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class StoryCommentReportPersistenceAdapter implements StoryCommentReportRepository {

	private final StoryCommentReportJpaRepository storyCommentReportJpaRepository;

	@Override
	@Transactional
	public StoryCommentReport save(StoryCommentReport storyCommentReport) {
		try {
			StoryCommentReportJpaEntity saved = storyCommentReportJpaRepository.save(
					StoryCommentReportJpaEntity.fromDomain(storyCommentReport)
			);
			log.info(
					"StoryCommentReportPersistenceAdapter : save : 댓글 신고 저장 완료 - commentReportId={}, commentReportUuid={}",
					saved.getCommentReportId(),
					saved.getCommentReportUuid()
			);
			return saved.toDomain();
		} catch (DataIntegrityViolationException exception) {
			if (isMemberCommentUniqueViolation(exception)) {
				log.warn("StoryCommentReportPersistenceAdapter : save : 댓글 신고 UNIQUE 제약 위반 - uk_comment_report_member");
				throw new DuplicateCommentReportException();
			}
			throw exception;
		}
	}

	private static boolean isMemberCommentUniqueViolation(DataIntegrityViolationException exception) {
		Throwable current = exception;
		while (current != null) {
			String message = current.getMessage();
			if (message != null && message.toLowerCase().contains(StoryCommentReportJpaEntity.MEMBER_COMMENT_UNIQUE)) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<StoryCommentReport> findByCommentReportId(Long commentReportId) {
		return storyCommentReportJpaRepository.findById(commentReportId)
				.map(entity -> entity.toDomain());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<StoryCommentReport> findByCommentReportUuid(UUID commentReportUuid) {
		return storyCommentReportJpaRepository.findByCommentReportUuid(commentReportUuid.toString())
				.map(entity -> entity.toDomain());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByCommentUuidAndMemberUuid(UUID commentUuid, UUID memberUuid) {
		return storyCommentReportJpaRepository.existsByCommentUuidAndMemberUuid(
				commentUuid.toString(),
				memberUuid.toString()
		);
	}

	@Override
	@Transactional(readOnly = true)
	public long countByCommentUuid(UUID commentUuid) {
		return storyCommentReportJpaRepository.countByCommentUuid(commentUuid.toString());
	}
}
