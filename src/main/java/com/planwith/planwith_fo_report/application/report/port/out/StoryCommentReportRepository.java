package com.planwith.planwith_fo_report.application.report.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_report.domain.report.StoryCommentReport;

public interface StoryCommentReportRepository {

	StoryCommentReport save(StoryCommentReport storyCommentReport);

	Optional<StoryCommentReport> findByCommentReportId(Long commentReportId);

	Optional<StoryCommentReport> findByCommentReportUuid(UUID commentReportUuid);

	boolean existsByCommentUuidAndMemberUuid(UUID commentUuid, UUID memberUuid);
}
