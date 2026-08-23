package com.planwith.planwith_fo_report.adapter.out.persistence.commentreport;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.port.out.StoryCommentReportRepository;
import com.planwith.planwith_fo_report.domain.report.ReportType;
import com.planwith.planwith_fo_report.domain.report.StoryCommentReport;
import com.planwith.planwith_fo_report.domain.report.exception.DuplicateCommentReportException;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class StoryCommentReportConcurrentDuplicateTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MEMBER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Autowired
	private StoryCommentReportRepository storyCommentReportRepository;

	@Test
	void dbUniqueBlocksConcurrentDuplicateInsert() {
		storyCommentReportRepository.save(
				StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, ReportType.SPAM)
		);

		assertThatThrownBy(() -> storyCommentReportRepository.save(
				StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, ReportType.HATE)
		)).isInstanceOf(DuplicateCommentReportException.class);
	}
}
