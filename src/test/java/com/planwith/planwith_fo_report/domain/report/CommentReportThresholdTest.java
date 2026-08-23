package com.planwith.planwith_fo_report.domain.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;

class CommentReportThresholdTest {

	@Test
	void defaultHideThresholdIsThree() {
		CommentReportThreshold threshold = CommentReportThreshold.defaultThreshold();

		assertThat(threshold.hideThreshold()).isEqualTo(3);
		assertThat(threshold.isReached(1)).isFalse();
		assertThat(threshold.isReached(2)).isFalse();
		assertThat(threshold.isReached(3)).isTrue();
		assertThat(threshold.isReached(4)).isTrue();
	}

	@Test
	void usesConfiguredHideThreshold() {
		CommentReportThreshold threshold = CommentReportThreshold.of(5);

		assertThat(threshold.isReached(4)).isFalse();
		assertThat(threshold.isReached(5)).isTrue();
	}

	@Test
	void rejectsNonPositiveHideThreshold() {
		assertThatThrownBy(() -> CommentReportThreshold.of(0))
				.isInstanceOf(InvalidReportException.class)
				.hasMessage("신고 숨김 임계치는 1 이상이어야 합니다.");
	}
}
