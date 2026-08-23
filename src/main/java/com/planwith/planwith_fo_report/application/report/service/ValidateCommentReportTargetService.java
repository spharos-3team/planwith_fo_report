package com.planwith.planwith_fo_report.application.report.service;

import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_report.application.report.command.ValidateCommentReportTargetCommand;
import com.planwith.planwith_fo_report.application.report.port.in.ValidateCommentReportTargetUseCase;
import com.planwith.planwith_fo_report.application.report.port.out.CommentReportContextPort;
import com.planwith.planwith_fo_report.application.report.result.CommentReportTargetResult;
import com.planwith.planwith_fo_report.domain.report.CommentReportContext;
import com.planwith.planwith_fo_report.domain.report.exception.CommentNotFoundException;
import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateCommentReportTargetService implements ValidateCommentReportTargetUseCase {

	private final CommentReportContextPort commentReportContextPort;

	@Override
	public CommentReportTargetResult validate(ValidateCommentReportTargetCommand command) {
		if (command == null || command.commentUuid() == null) {
			throw new InvalidReportException("댓글 UUID는 필수입니다.");
		}
		if (command.memberUuid() == null) {
			throw new InvalidReportException("회원 UUID는 필수입니다.");
		}

		log.info("ValidateCommentReportTargetService : validate : 신고 대상 댓글 검증 시작");
		log.debug(
				"ValidateCommentReportTargetService : validate : 검증 요청 확인 - commentUuid={}, memberUuid={}",
				command.commentUuid(),
				command.memberUuid()
		);

		CommentReportContext context = commentReportContextPort.findByCommentUuid(command.commentUuid())
				.orElseThrow(() -> new CommentNotFoundException(command.commentUuid()));
		context.assertCanBeReportedBy(command.memberUuid());

		log.info(
				"ValidateCommentReportTargetService : validate : 신고 대상 댓글 검증 완료 - commentUuid={}",
				context.getCommentUuid()
		);
		return CommentReportTargetResult.from(context);
	}
}
