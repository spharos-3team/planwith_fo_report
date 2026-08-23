package com.planwith.planwith_fo_report.application.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_report.application.report.command.CreateCommentReportCommand;
import com.planwith.planwith_fo_report.application.report.command.ValidateCommentReportTargetCommand;
import com.planwith.planwith_fo_report.application.report.port.in.ValidateCommentReportTargetUseCase;
import com.planwith.planwith_fo_report.application.report.result.CommentReportInputResult;
import com.planwith.planwith_fo_report.application.report.result.CommentReportTargetResult;
import com.planwith.planwith_fo_report.domain.report.ReportType;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;
import com.planwith.planwith_fo_report.domain.report.exception.UnauthenticatedMemberException;

@ExtendWith(MockitoExtension.class)
class ValidateCommentReportInputServiceTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MEMBER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID AUTHOR_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Mock
	private ValidateCommentReportTargetUseCase validateCommentReportTargetUseCase;

	private ValidateCommentReportInputService validateCommentReportInputService;

	@BeforeEach
	void setUp() {
		validateCommentReportInputService = new ValidateCommentReportInputService(validateCommentReportTargetUseCase);
	}

	@ParameterizedTest
	@EnumSource(ReportType.class)
	void allowsSixReportTypesAndEntersTargetValidation(ReportType reportType) {
		given(validateCommentReportTargetUseCase.validate(any(ValidateCommentReportTargetCommand.class)))
				.willReturn(new CommentReportTargetResult(COMMENT_UUID, AUTHOR_UUID, true));

		CommentReportInputResult result = validateCommentReportInputService.validate(
				new CreateCommentReportCommand(COMMENT_UUID, reportType, MEMBER_UUID)
		);

		assertThat(result.reportType()).isEqualTo(reportType);
		assertThat(result.commentUuid()).isEqualTo(COMMENT_UUID);
		assertThat(result.memberUuid()).isEqualTo(MEMBER_UUID);
		assertThat(result.reportable()).isTrue();
		verify(validateCommentReportTargetUseCase).validate(any(ValidateCommentReportTargetCommand.class));
	}

	@Test
	void rejectsMissingCommentUuidBeforeTargetValidation() {
		assertThatThrownBy(() -> validateCommentReportInputService.validate(
				new CreateCommentReportCommand(null, ReportType.SPAM, MEMBER_UUID)
		)).isInstanceOf(InvalidReportException.class)
				.hasMessage("댓글 UUID는 필수입니다.");

		verify(validateCommentReportTargetUseCase, never()).validate(any());
	}

	@Test
	void rejectsMissingReportTypeBeforeTargetValidation() {
		assertThatThrownBy(() -> validateCommentReportInputService.validate(
				new CreateCommentReportCommand(COMMENT_UUID, null, MEMBER_UUID)
		)).isInstanceOf(InvalidReportException.class)
				.hasMessage("허용되지 않은 신고 사유입니다.");

		verify(validateCommentReportTargetUseCase, never()).validate(any());
	}

	@Test
	void rejectsMissingMemberBeforeTargetValidation() {
		assertThatThrownBy(() -> validateCommentReportInputService.validate(
				new CreateCommentReportCommand(COMMENT_UUID, ReportType.HATE, null)
		)).isInstanceOf(UnauthenticatedMemberException.class);

		verify(validateCommentReportTargetUseCase, never()).validate(any());
	}
}
