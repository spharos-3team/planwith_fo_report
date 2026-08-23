package com.planwith.planwith_fo_report.domain.report;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_report.domain.report.exception.CommentNotReportableException;
import com.planwith.planwith_fo_report.domain.report.exception.SelfCommentReportException;

class CommentReportContextTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID AUTHOR_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final UUID REPORTER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Test
	void allowsReportWhenCommentIsReportableAndReporterIsNotAuthor() {
		CommentReportContext context = CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, true);

		assertThatCode(() -> context.assertCanBeReportedBy(REPORTER_UUID))
				.doesNotThrowAnyException();
	}

	@Test
	void rejectsDeletedComment() {
		CommentReportContext context = CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, false);

		assertThatThrownBy(() -> context.assertCanBeReportedBy(REPORTER_UUID))
				.isInstanceOf(CommentNotReportableException.class);
	}

	@Test
	void rejectsSelfComment() {
		CommentReportContext context = CommentReportContext.of(COMMENT_UUID, AUTHOR_UUID, true);

		assertThatThrownBy(() -> context.assertCanBeReportedBy(AUTHOR_UUID))
				.isInstanceOf(SelfCommentReportException.class);
	}
}
