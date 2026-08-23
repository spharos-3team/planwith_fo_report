package com.planwith.planwith_fo_report.application.report.service;

import java.util.EnumSet;

import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_report.application.report.command.CreateCommentReportCommand;
import com.planwith.planwith_fo_report.application.report.command.ValidateCommentReportTargetCommand;
import com.planwith.planwith_fo_report.application.report.port.in.ValidateCommentReportInputUseCase;
import com.planwith.planwith_fo_report.application.report.port.in.ValidateCommentReportTargetUseCase;
import com.planwith.planwith_fo_report.application.report.result.CommentReportInputResult;
import com.planwith.planwith_fo_report.application.report.result.CommentReportTargetResult;
import com.planwith.planwith_fo_report.domain.report.ReportType;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;
import com.planwith.planwith_fo_report.domain.report.exception.UnauthenticatedMemberException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateCommentReportInputService implements ValidateCommentReportInputUseCase {

	private static final EnumSet<ReportType> ALLOWED_REPORT_TYPES = EnumSet.allOf(ReportType.class);

	private final ValidateCommentReportTargetUseCase validateCommentReportTargetUseCase;

	@Override
	public CommentReportInputResult validate(CreateCommentReportCommand command) {
		validateRequiredValues(command);

		log.info("ValidateCommentReportInputService : validate : 신고 입력 검증 시작");
		log.debug(
				"ValidateCommentReportInputService : validate : 입력 확인 - commentUuid={}, reportType={}",
				command.commentUuid(),
				command.reportType()
		);

		CommentReportTargetResult target = validateCommentReportTargetUseCase.validate(
				new ValidateCommentReportTargetCommand(command.commentUuid(), command.memberUuid())
		);

		log.info(
				"ValidateCommentReportInputService : validate : 신고 입력 검증 완료 - commentUuid={}, reportType={}",
				command.commentUuid(),
				command.reportType()
		);
		return new CommentReportInputResult(
				command.commentUuid(),
				command.reportType(),
				command.memberUuid(),
				target.authorMemberUuid(),
				target.reportable()
		);
	}

	private void validateRequiredValues(CreateCommentReportCommand command) {
		if (command == null || command.commentUuid() == null) {
			throw new InvalidReportException("댓글 UUID는 필수입니다.");
		}
		if (command.reportType() == null || !ALLOWED_REPORT_TYPES.contains(command.reportType())) {
			throw new InvalidReportException("허용되지 않은 신고 사유입니다.");
		}
		if (command.memberUuid() == null) {
			throw new UnauthenticatedMemberException();
		}
	}
}
