package com.planwith.planwith_fo_report.application.report.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_report.application.report.command.CreateCommentReportCommand;
import com.planwith.planwith_fo_report.application.report.port.in.CountCommentReportsUseCase;
import com.planwith.planwith_fo_report.application.report.port.in.CreateCommentReportUseCase;
import com.planwith.planwith_fo_report.application.report.port.in.ValidateCommentReportInputUseCase;
import com.planwith.planwith_fo_report.application.report.port.out.StoryCommentReportRepository;
import com.planwith.planwith_fo_report.application.report.result.CommentReportCountResult;
import com.planwith.planwith_fo_report.application.report.result.CommentReportInputResult;
import com.planwith.planwith_fo_report.application.report.result.CreateCommentReportResult;
import com.planwith.planwith_fo_report.domain.report.StoryCommentReport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCommentReportService implements CreateCommentReportUseCase {

	private final ValidateCommentReportInputUseCase validateCommentReportInputUseCase;
	private final DuplicateCommentReportGuard duplicateCommentReportGuard;
	private final StoryCommentReportRepository storyCommentReportRepository;
	private final CountCommentReportsUseCase countCommentReportsUseCase;

	@Override
	@Transactional
	public CreateCommentReportResult create(CreateCommentReportCommand command) {
		log.info("CreateCommentReportService : create : 댓글 신고 생성 비즈니스 로직 시작");

		CommentReportInputResult validated = validateCommentReportInputUseCase.validate(command);
		duplicateCommentReportGuard.assertNotDuplicated(validated.commentUuid(), validated.memberUuid());

		StoryCommentReport saved = storyCommentReportRepository.save(
				StoryCommentReport.create(
						validated.commentUuid(),
						validated.memberUuid(),
						validated.reportType()
				)
		);
		CommentReportCountResult count = countCommentReportsUseCase.count(saved.getCommentUuid());

		log.info(
				"CreateCommentReportService : create : 댓글 신고 생성 완료 - commentReportUuid={}, commentUuid={}, reportCount={}",
				saved.getCommentReportUuid(),
				saved.getCommentUuid(),
				count.reportCount()
		);
		return CreateCommentReportResult.from(saved, count.reportCount());
	}
}
