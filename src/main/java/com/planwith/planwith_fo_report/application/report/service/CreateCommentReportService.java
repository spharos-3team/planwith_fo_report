package com.planwith.planwith_fo_report.application.report.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.command.CreateCommentReportCommand;
import com.planwith.planwith_fo_report.application.report.port.in.CreateCommentReportUseCase;
import com.planwith.planwith_fo_report.application.report.port.in.ValidateCommentReportInputUseCase;
import com.planwith.planwith_fo_report.application.report.port.out.StoryCommentReportRepository;
import com.planwith.planwith_fo_report.application.report.result.CommentReportInputResult;
import com.planwith.planwith_fo_report.application.report.result.CreateCommentReportResult;
import com.planwith.planwith_fo_report.domain.report.StoryCommentReport;
import com.planwith.planwith_fo_report.domain.report.exception.DuplicateReportException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCommentReportService implements CreateCommentReportUseCase {

	private final ValidateCommentReportInputUseCase validateCommentReportInputUseCase;
	private final StoryCommentReportRepository storyCommentReportRepository;

	@Override
	@Transactional
	public CreateCommentReportResult create(CreateCommentReportCommand command) {
		log.info("CreateCommentReportService : create : 댓글 신고 생성 비즈니스 로직 시작");

		CommentReportInputResult validated = validateCommentReportInputUseCase.validate(command);

		if (storyCommentReportRepository.existsByCommentUuidAndMemberUuid(
				validated.commentUuid(),
				validated.memberUuid()
		)) {
			log.warn(
					"CreateCommentReportService : create : 중복 댓글 신고 요청 - commentUuid={}, memberUuid={}",
					validated.commentUuid(),
					validated.memberUuid()
			);
			throw new DuplicateReportException();
		}

		StoryCommentReport saved = storyCommentReportRepository.save(
				StoryCommentReport.create(
						validated.commentUuid(),
						validated.memberUuid(),
						validated.reportType()
				)
		);

		log.info(
				"CreateCommentReportService : create : 댓글 신고 생성 완료 - commentReportUuid={}, commentUuid={}",
				saved.getCommentReportUuid(),
				saved.getCommentUuid()
		);
		return CreateCommentReportResult.from(saved);
	}
}
