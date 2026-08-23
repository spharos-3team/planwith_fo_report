package com.planwith.planwith_fo_report.application.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_report.application.report.command.ValidateCommentReportTargetCommand;
import com.planwith.planwith_fo_report.application.report.port.out.CommentReportContextPort;
import com.planwith.planwith_fo_report.application.report.result.CommentReportTargetResult;
import com.planwith.planwith_fo_report.domain.report.CommentReportContext;
import com.planwith.planwith_fo_report.domain.report.exception.CommentNotFoundException;
import com.planwith.planwith_fo_report.domain.report.exception.CommentNotReportableException;
import com.planwith.planwith_fo_report.domain.report.exception.SelfCommentReportException;

@ExtendWith(MockitoExtension.class)
class ValidateCommentReportTargetServiceTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID AUTHOR_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final UUID REPORTER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Mock
	private CommentReportContextPort commentReportContextPort;

	private ValidateCommentReportTargetService validateCommentReportTargetService;

	@BeforeEach
	void setUp() {
		validateCommentReportTargetService = new ValidateCommentReportTargetService(commentReportContextPort);
	}

	@Test
	void passesValidCommentToReportStep() {
		given(commentReportContextPort.findByCommentUuid(COMMENT_UUID))
				.willReturn(Optional.of(CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, true)));

		CommentReportTargetResult result = validateCommentReportTargetService.validate(
				new ValidateCommentReportTargetCommand(COMMENT_UUID, REPORTER_UUID)
		);

		assertThat(result.commentUuid()).isEqualTo(COMMENT_UUID);
		assertThat(result.authorMemberUuid()).isEqualTo(AUTHOR_UUID);
		assertThat(result.reportable()).isTrue();
		verify(commentReportContextPort).findByCommentUuid(COMMENT_UUID);
	}

	@Test
	void rejectsMissingComment() {
		given(commentReportContextPort.findByCommentUuid(COMMENT_UUID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> validateCommentReportTargetService.validate(
				new ValidateCommentReportTargetCommand(COMMENT_UUID, REPORTER_UUID)
		)).isInstanceOf(CommentNotFoundException.class);
	}

	@Test
	void rejectsDeletedComment() {
		given(commentReportContextPort.findByCommentUuid(COMMENT_UUID))
				.willReturn(Optional.of(CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, false)));

		assertThatThrownBy(() -> validateCommentReportTargetService.validate(
				new ValidateCommentReportTargetCommand(COMMENT_UUID, REPORTER_UUID)
		)).isInstanceOf(CommentNotReportableException.class);
	}

	@Test
	void rejectsSelfComment() {
		given(commentReportContextPort.findByCommentUuid(COMMENT_UUID))
				.willReturn(Optional.of(CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, true)));

		assertThatThrownBy(() -> validateCommentReportTargetService.validate(
				new ValidateCommentReportTargetCommand(COMMENT_UUID, AUTHOR_UUID)
		)).isInstanceOf(SelfCommentReportException.class);
	}
}
