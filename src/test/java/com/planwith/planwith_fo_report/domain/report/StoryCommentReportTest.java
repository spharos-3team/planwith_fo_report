package com.planwith.planwith_fo_report.domain.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;

class StoryCommentReportTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MEMBER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Test
	void createAssignsExternalUuidAndCreatedAt() {
		StoryCommentReport report = StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, ReportType.SPAM);

		assertThat(report.getCommentReportId()).isNull();
		assertThat(report.getCommentReportUuid()).isNotNull();
		assertThat(report.getCommentUuid()).isEqualTo(COMMENT_UUID);
		assertThat(report.getMemberUuid()).isEqualTo(MEMBER_UUID);
		assertThat(report.getReportType()).isEqualTo(ReportType.SPAM);
		assertThat(report.getCreatedAt()).isNotNull();
	}

	@Test
	void createRequiresCommentUuid() {
		assertThatThrownBy(() -> StoryCommentReport.create(null, MEMBER_UUID, ReportType.ABUSE))
				.isInstanceOf(InvalidReportException.class)
				.hasMessage("댓글 UUID는 필수입니다.");
	}

	@Test
	void createRequiresMemberUuid() {
		assertThatThrownBy(() -> StoryCommentReport.create(COMMENT_UUID, null, ReportType.HATE))
				.isInstanceOf(InvalidReportException.class)
				.hasMessage("회원 UUID는 필수입니다.");
	}

	@Test
	void createRequiresReportType() {
		assertThatThrownBy(() -> StoryCommentReport.create(COMMENT_UUID, MEMBER_UUID, null))
				.isInstanceOf(InvalidReportException.class)
				.hasMessage("신고 사유는 필수입니다.");
	}

	@Test
	void reportTypeMatchesProvidedSqlEnum() {
		assertThat(ReportType.values()).containsExactly(
				ReportType.SPAM,
				ReportType.ABUSE,
				ReportType.HATE,
				ReportType.SEXUAL,
				ReportType.PRIVACY,
				ReportType.OTHER
		);
	}
}
