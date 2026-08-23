package com.planwith.planwith_fo_report.adapter.out.persistence.commentreport;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.planwith.planwith_fo_report.domain.report.exception.DuplicateReportException;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class StoryCommentReportRepositoryTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MEMBER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Autowired
	private StoryCommentReportRepository storyCommentReportRepository;

	@Test
	void saveAndFindByCommentReportId() {
		StoryCommentReport saved = storyCommentReportRepository.save(
				StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, ReportType.PRIVACY)
		);

		assertThat(saved.getCommentReportId()).isNotNull();

		StoryCommentReport found = storyCommentReportRepository.findByCommentReportId(saved.getCommentReportId())
				.orElseThrow();

		assertThat(found.getCommentReportId()).isEqualTo(saved.getCommentReportId());
		assertThat(found.getCommentReportUuid()).isEqualTo(saved.getCommentReportUuid());
		assertThat(found.getCommentUuid()).isEqualTo(COMMENT_UUID);
		assertThat(found.getMemberUuid()).isEqualTo(MEMBER_UUID);
		assertThat(found.getReportType()).isEqualTo(ReportType.PRIVACY);
		assertThat(found.getCreatedAt()).isEqualTo(saved.getCreatedAt());
	}

	@Test
	void saveAndFindByCommentReportUuid() {
		StoryCommentReport saved = storyCommentReportRepository.save(
				StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, ReportType.SEXUAL)
		);

		StoryCommentReport found = storyCommentReportRepository.findByCommentReportUuid(saved.getCommentReportUuid())
				.orElseThrow();

		assertThat(found.getCommentReportId()).isEqualTo(saved.getCommentReportId());
		assertThat(found.getReportType()).isEqualTo(ReportType.SEXUAL);
	}

	@Test
	void existsByCommentUuidAndMemberUuid() {
		storyCommentReportRepository.save(
				StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, ReportType.OTHER)
		);

		assertThat(storyCommentReportRepository.existsByCommentUuidAndMemberUuid(COMMENT_UUID, MEMBER_UUID)).isTrue();
		assertThat(storyCommentReportRepository.existsByCommentUuidAndMemberUuid(
				UUID.fromString("33333333-3333-3333-3333-333333333333"),
				MEMBER_UUID
		)).isFalse();
	}

	@Test
	void uniqueConstraintRejectsSameMemberAndComment() {
		storyCommentReportRepository.save(
				StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, ReportType.SPAM)
		);

		assertThatThrownBy(() -> storyCommentReportRepository.save(
				StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, ReportType.HATE)
		)).isInstanceOf(DuplicateReportException.class);
	}

	@Test
	void allowsSameCommentFromDifferentMembers() {
		UUID otherMemberUuid = UUID.fromString("44444444-4444-4444-4444-444444444444");

		storyCommentReportRepository.save(
				StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, ReportType.SPAM)
		);
		StoryCommentReport otherMemberReport = storyCommentReportRepository.save(
				StoryCommentReport.create(COMMENT_UUID, otherMemberUuid, ReportType.HATE)
		);

		assertThat(otherMemberReport.getCommentReportId()).isNotNull();
		assertThat(storyCommentReportRepository.existsByCommentUuidAndMemberUuid(COMMENT_UUID, MEMBER_UUID)).isTrue();
		assertThat(storyCommentReportRepository.existsByCommentUuidAndMemberUuid(COMMENT_UUID, otherMemberUuid)).isTrue();
	}
}
