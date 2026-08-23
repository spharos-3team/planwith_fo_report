package com.planwith.planwith_fo_report.adapter.out.persistence.commentreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.planwith.planwith_fo_report.domain.report.ReportType;
import com.planwith.planwith_fo_report.domain.report.StoryCommentReport;
import com.planwith.planwith_fo_report.domain.report.exception.DuplicateCommentReportException;

@ExtendWith(MockitoExtension.class)
class StoryCommentReportPersistenceAdapterTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MEMBER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Mock
	private StoryCommentReportJpaRepository storyCommentReportJpaRepository;

	private StoryCommentReportPersistenceAdapter storyCommentReportPersistenceAdapter;

	@BeforeEach
	void setUp() {
		storyCommentReportPersistenceAdapter = new StoryCommentReportPersistenceAdapter(storyCommentReportJpaRepository);
	}

	@Test
	void mapsMemberCommentUniqueViolationToDuplicateReport() {
		given(storyCommentReportJpaRepository.save(any(StoryCommentReportJpaEntity.class)))
				.willThrow(new DataIntegrityViolationException(
						"Unique index or primary key violation: " + StoryCommentReportJpaEntity.MEMBER_COMMENT_UNIQUE
				));

		assertThatThrownBy(() -> storyCommentReportPersistenceAdapter.save(
				StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, ReportType.SPAM)
		)).isInstanceOf(DuplicateCommentReportException.class);
	}

	@Test
	void rethrowsOtherIntegrityViolations() {
		DataIntegrityViolationException integrityViolation = new DataIntegrityViolationException(
				"Unique index or primary key violation: uk_comment_report_uuid"
		);
		given(storyCommentReportJpaRepository.save(any(StoryCommentReportJpaEntity.class)))
				.willThrow(integrityViolation);

		assertThatThrownBy(() -> storyCommentReportPersistenceAdapter.save(
				StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, ReportType.SPAM)
		)).isSameAs(integrityViolation);
	}

	@Test
	void countByCommentUuidDelegatesToJpaRepository() {
		given(storyCommentReportJpaRepository.countByCommentUuid(COMMENT_UUID.toString())).willReturn(3L);

		assertThat(storyCommentReportPersistenceAdapter.countByCommentUuid(COMMENT_UUID)).isEqualTo(3L);

		verify(storyCommentReportJpaRepository).countByCommentUuid(COMMENT_UUID.toString());
	}
}
