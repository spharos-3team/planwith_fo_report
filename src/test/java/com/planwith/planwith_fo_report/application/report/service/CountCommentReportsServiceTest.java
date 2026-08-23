package com.planwith.planwith_fo_report.application.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_report.application.report.port.out.StoryCommentReportRepository;
import com.planwith.planwith_fo_report.application.report.result.CommentReportCountResult;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;

@ExtendWith(MockitoExtension.class)
class CountCommentReportsServiceTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Mock
	private StoryCommentReportRepository storyCommentReportRepository;

	private CountCommentReportsService countCommentReportsService;

	@BeforeEach
	void setUp() {
		countCommentReportsService = new CountCommentReportsService(storyCommentReportRepository);
	}

	@Test
	void returnsZeroWhenNoReportsExist() {
		given(storyCommentReportRepository.countByCommentUuid(COMMENT_UUID)).willReturn(0L);

		CommentReportCountResult result = countCommentReportsService.count(COMMENT_UUID);

		assertThat(result.commentUuid()).isEqualTo(COMMENT_UUID);
		assertThat(result.reportCount()).isZero();
		verify(storyCommentReportRepository).countByCommentUuid(COMMENT_UUID);
	}

	@Test
	void accumulatesReportsFromDifferentMembers() {
		given(storyCommentReportRepository.countByCommentUuid(COMMENT_UUID)).willReturn(3L);

		CommentReportCountResult result = countCommentReportsService.count(COMMENT_UUID);

		assertThat(result.commentUuid()).isEqualTo(COMMENT_UUID);
		assertThat(result.reportCount()).isEqualTo(3L);
	}

	@Test
	void rejectsMissingCommentUuid() {
		assertThatThrownBy(() -> countCommentReportsService.count(null))
				.isInstanceOf(InvalidReportException.class)
				.hasMessage("댓글 UUID는 필수입니다.");
	}
}
