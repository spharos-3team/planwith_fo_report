package com.planwith.planwith_fo_report.adapter.out.persistence.commentreport;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface StoryCommentReportJpaRepository extends JpaRepository<StoryCommentReportJpaEntity, Long> {

	Optional<StoryCommentReportJpaEntity> findByCommentReportUuid(String commentReportUuid);

	boolean existsByCommentUuidAndMemberUuid(String commentUuid, String memberUuid);
}
