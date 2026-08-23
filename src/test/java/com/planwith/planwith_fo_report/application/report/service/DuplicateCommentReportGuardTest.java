package com.planwith.planwith_fo_report.application.report.service;

import static org.assertj.core.api.Assertions.assertThatCode;
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
import com.planwith.planwith_fo_report.domain.report.exception.DuplicateReportException;

@ExtendWith(MockitoExtension.class)
class DuplicateCommentReportGuardTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MEMBER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Mock
	private StoryCommentReportRepository storyCommentReportRepository;

	private DuplicateCommentReportGuard duplicateCommentReportGuard;

	@BeforeEach
	void setUp() {
		duplicateCommentReportGuard = new DuplicateCommentReportGuard(storyCommentReportRepository);
	}

	@Test
	void allowsWhenSameMemberHasNotReportedComment() {
		given(storyCommentReportRepository.existsByCommentUuidAndMemberUuid(COMMENT_UUID, MEMBER_UUID))
				.willReturn(false);

		assertThatCode(() -> duplicateCommentReportGuard.assertNotDuplicated(COMMENT_UUID, MEMBER_UUID))
				.doesNotThrowAnyException();

		verify(storyCommentReportRepository).existsByCommentUuidAndMemberUuid(COMMENT_UUID, MEMBER_UUID);
	}

	@Test
	void rejectsWhenSameMemberAlreadyReportedComment() {
		given(storyCommentReportRepository.existsByCommentUuidAndMemberUuid(COMMENT_UUID, MEMBER_UUID))
				.willReturn(true);

		assertThatThrownBy(() -> duplicateCommentReportGuard.assertNotDuplicated(COMMENT_UUID, MEMBER_UUID))
				.isInstanceOf(DuplicateReportException.class);

		verify(storyCommentReportRepository).existsByCommentUuidAndMemberUuid(COMMENT_UUID, MEMBER_UUID);
	}
}
