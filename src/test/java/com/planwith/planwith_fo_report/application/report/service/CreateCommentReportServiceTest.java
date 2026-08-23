package com.planwith.planwith_fo_report.application.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_report.application.report.command.CreateCommentReportCommand;
import com.planwith.planwith_fo_report.application.report.port.in.CountCommentReportsUseCase;
import com.planwith.planwith_fo_report.application.report.port.in.ValidateCommentReportInputUseCase;
import com.planwith.planwith_fo_report.application.report.port.out.StoryCommentReportRepository;
import com.planwith.planwith_fo_report.application.report.result.CommentReportCountResult;
import com.planwith.planwith_fo_report.application.report.result.CommentReportInputResult;
import com.planwith.planwith_fo_report.application.report.result.CreateCommentReportResult;
import com.planwith.planwith_fo_report.domain.report.ReportType;
import com.planwith.planwith_fo_report.domain.report.StoryCommentReport;
import com.planwith.planwith_fo_report.domain.report.exception.DuplicateCommentReportException;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;

@ExtendWith(MockitoExtension.class)
class CreateCommentReportServiceTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MEMBER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID AUTHOR_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Mock
	private ValidateCommentReportInputUseCase validateCommentReportInputUseCase;

	@Mock
	private DuplicateCommentReportGuard duplicateCommentReportGuard;

	@Mock
	private StoryCommentReportRepository storyCommentReportRepository;

	@Mock
	private CountCommentReportsUseCase countCommentReportsUseCase;

	@Mock
	private CommentReportThresholdHandler commentReportThresholdHandler;

	private CreateCommentReportService createCommentReportService;

	@BeforeEach
	void setUp() {
		createCommentReportService = new CreateCommentReportService(
				validateCommentReportInputUseCase,
				duplicateCommentReportGuard,
				storyCommentReportRepository,
				countCommentReportsUseCase,
				commentReportThresholdHandler
		);
	}

	@Test
	void createsAndSavesStoryCommentReport() {
		CreateCommentReportCommand command = new CreateCommentReportCommand(
				COMMENT_UUID,
				ReportType.HATE,
				MEMBER_UUID
		);
		given(validateCommentReportInputUseCase.validate(command))
				.willReturn(new CommentReportInputResult(
						COMMENT_UUID,
						ReportType.HATE,
						MEMBER_UUID,
						AUTHOR_UUID,
						true
				));
		given(storyCommentReportRepository.save(any(StoryCommentReport.class)))
				.willAnswer(invocation -> invocation.getArgument(0));
		given(countCommentReportsUseCase.count(COMMENT_UUID))
				.willReturn(new CommentReportCountResult(COMMENT_UUID, 1L));
		given(commentReportThresholdHandler.handle(any(UUID.class), any(UUID.class), anyLong()))
				.willReturn(false);

		CreateCommentReportResult result = createCommentReportService.create(command);

		assertThat(result.commentUuid()).isEqualTo(COMMENT_UUID);
		assertThat(result.reportType()).isEqualTo(ReportType.HATE);
		assertThat(result.commentReportUuid()).isNotNull();
		assertThat(result.createdAt()).isNotNull();
		assertThat(result.reportCount()).isEqualTo(1L);
		assertThat(result.thresholdReached()).isFalse();

		verify(duplicateCommentReportGuard).assertNotDuplicated(COMMENT_UUID, MEMBER_UUID);
		verify(countCommentReportsUseCase).count(COMMENT_UUID);
		verify(commentReportThresholdHandler).handle(COMMENT_UUID, result.commentReportUuid(), 1L);
		ArgumentCaptor<StoryCommentReport> captor = ArgumentCaptor.forClass(StoryCommentReport.class);
		verify(storyCommentReportRepository).save(captor.capture());
		assertThat(captor.getValue().getCommentUuid()).isEqualTo(COMMENT_UUID);
		assertThat(captor.getValue().getMemberUuid()).isEqualTo(MEMBER_UUID);
		assertThat(captor.getValue().getReportType()).isEqualTo(ReportType.HATE);
	}

	@Test
	void rejectsDuplicateCommentReport() {
		CreateCommentReportCommand command = new CreateCommentReportCommand(
				COMMENT_UUID,
				ReportType.SPAM,
				MEMBER_UUID
		);
		given(validateCommentReportInputUseCase.validate(command))
				.willReturn(new CommentReportInputResult(
						COMMENT_UUID,
						ReportType.SPAM,
						MEMBER_UUID,
						AUTHOR_UUID,
						true
				));
		willThrow(new DuplicateCommentReportException())
				.given(duplicateCommentReportGuard)
				.assertNotDuplicated(COMMENT_UUID, MEMBER_UUID);

		assertThatThrownBy(() -> createCommentReportService.create(command))
				.isInstanceOf(DuplicateCommentReportException.class);

		verify(duplicateCommentReportGuard).assertNotDuplicated(COMMENT_UUID, MEMBER_UUID);
		verify(storyCommentReportRepository, never()).save(any());
		verify(countCommentReportsUseCase, never()).count(any());
		verify(commentReportThresholdHandler, never()).handle(any(), any(), anyLong());
	}

	@Test
	void requestsHideWhenReportCountReachesThreshold() {
		CreateCommentReportCommand command = new CreateCommentReportCommand(
				COMMENT_UUID,
				ReportType.HATE,
				MEMBER_UUID
		);
		given(validateCommentReportInputUseCase.validate(command))
				.willReturn(new CommentReportInputResult(
						COMMENT_UUID,
						ReportType.HATE,
						MEMBER_UUID,
						AUTHOR_UUID,
						true
				));
		given(storyCommentReportRepository.save(any(StoryCommentReport.class)))
				.willAnswer(invocation -> invocation.getArgument(0));
		given(countCommentReportsUseCase.count(COMMENT_UUID))
				.willReturn(new CommentReportCountResult(COMMENT_UUID, 3L));
		given(commentReportThresholdHandler.handle(any(UUID.class), any(UUID.class), anyLong()))
				.willReturn(true);

		CreateCommentReportResult result = createCommentReportService.create(command);

		assertThat(result.reportCount()).isEqualTo(3L);
		assertThat(result.thresholdReached()).isTrue();
		verify(commentReportThresholdHandler).handle(COMMENT_UUID, result.commentReportUuid(), 3L);
	}

	@Test
	void propagatesDuplicateWhenConcurrentInsertViolatesUniqueConstraint() {
		CreateCommentReportCommand command = new CreateCommentReportCommand(
				COMMENT_UUID,
				ReportType.SPAM,
				MEMBER_UUID
		);
		given(validateCommentReportInputUseCase.validate(command))
				.willReturn(new CommentReportInputResult(
						COMMENT_UUID,
						ReportType.SPAM,
						MEMBER_UUID,
						AUTHOR_UUID,
						true
				));
		given(storyCommentReportRepository.save(any(StoryCommentReport.class)))
				.willThrow(new DuplicateCommentReportException());

		assertThatThrownBy(() -> createCommentReportService.create(command))
				.isInstanceOf(DuplicateCommentReportException.class);

		verify(duplicateCommentReportGuard).assertNotDuplicated(COMMENT_UUID, MEMBER_UUID);
		verify(countCommentReportsUseCase, never()).count(any());
		verify(commentReportThresholdHandler, never()).handle(any(), any(), anyLong());
	}

	@Test
	void doesNotSaveWhenInputValidationFails() {
		CreateCommentReportCommand command = new CreateCommentReportCommand(
				COMMENT_UUID,
				ReportType.ABUSE,
				MEMBER_UUID
		);
		given(validateCommentReportInputUseCase.validate(command))
				.willThrow(new InvalidReportException("댓글 UUID는 필수입니다."));

		assertThatThrownBy(() -> createCommentReportService.create(command))
				.isInstanceOf(InvalidReportException.class);

		verify(duplicateCommentReportGuard, never()).assertNotDuplicated(any(), any());
		verify(storyCommentReportRepository, never()).save(any());
		verify(countCommentReportsUseCase, never()).count(any());
		verify(commentReportThresholdHandler, never()).handle(any(), any(), anyLong());
	}
}
