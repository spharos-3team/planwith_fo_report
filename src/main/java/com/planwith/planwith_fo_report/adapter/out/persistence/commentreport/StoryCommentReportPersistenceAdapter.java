package com.planwith.planwith_fo_report.adapter.out.persistence.commentreport;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.port.out.StoryCommentReportRepository;
import com.planwith.planwith_fo_report.domain.report.StoryCommentReport;

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
		StoryCommentReportJpaEntity saved = storyCommentReportJpaRepository.save(
				StoryCommentReportJpaEntity.fromDomain(storyCommentReport)
		);
		log.info(
				"StoryCommentReportPersistenceAdapter : save : 댓글 신고 저장 완료 - commentReportId={}, commentReportUuid={}",
				saved.getCommentReportId(),
				saved.getCommentReportUuid()
		);
		return saved.toDomain();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<StoryCommentReport> findByCommentReportId(Long commentReportId) {
		return storyCommentReportJpaRepository.findById(commentReportId)
				.map(StoryCommentReportJpaEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<StoryCommentReport> findByCommentReportUuid(UUID commentReportUuid) {
		return storyCommentReportJpaRepository.findByCommentReportUuid(commentReportUuid.toString())
				.map(StoryCommentReportJpaEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByCommentUuidAndMemberUuid(UUID commentUuid, UUID memberUuid) {
		return storyCommentReportJpaRepository.existsByCommentUuidAndMemberUuid(
				commentUuid.toString(),
				memberUuid.toString()
		);
	}
}
