package com.planwith.planwith_fo_report.application.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_report.application.report.port.out.CommentHideRequestPort;
import com.planwith.planwith_fo_report.domain.report.CommentReportThreshold;

@ExtendWith(MockitoExtension.class)
class CommentReportThresholdHandlerTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID COMMENT_REPORT_UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

	@Mock
	private CommentHideRequestPort commentHideRequestPort;

	private CommentReportThresholdHandler commentReportThresholdHandler;

	@BeforeEach
	void setUp() {
		commentReportThresholdHandler = new CommentReportThresholdHandler(
				CommentReportThreshold.defaultThreshold(),
				commentHideRequestPort
		);
	}

	@Test
	void doesNotRequestHideWhenCountIsBelowThreshold() {
		assertThat(commentReportThresholdHandler.handle(COMMENT_UUID, COMMENT_REPORT_UUID, 1L)).isFalse();
		assertThat(commentReportThresholdHandler.handle(COMMENT_UUID, COMMENT_REPORT_UUID, 2L)).isFalse();

		verify(commentHideRequestPort, never()).requestHide(COMMENT_UUID, 1L, 3);
		verify(commentHideRequestPort, never()).requestHide(COMMENT_UUID, 2L, 3);
	}

	@Test
	void requestsHideWhenCountReachesThreshold() {
		boolean reached = commentReportThresholdHandler.handle(COMMENT_UUID, COMMENT_REPORT_UUID, 3L);

		assertThat(reached).isTrue();
		verify(commentHideRequestPort).requestHide(COMMENT_UUID, 3L, 3);
	}

	@Test
	void doesNotRequestHideWhenCountExceedsThreshold() {
		assertThat(commentReportThresholdHandler.handle(COMMENT_UUID, COMMENT_REPORT_UUID, 4L)).isFalse();

		verify(commentHideRequestPort, never()).requestHide(COMMENT_UUID, 4L, 3);
	}
}
