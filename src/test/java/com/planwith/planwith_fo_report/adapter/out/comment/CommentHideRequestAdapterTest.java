package com.planwith.planwith_fo_report.adapter.out.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_report.application.report.port.out.ModerationOutboxPort;
import com.planwith.planwith_fo_report.domain.report.TargetType;
import com.planwith.planwith_fo_report.domain.report.event.ModerationActionRequired;

@ExtendWith(MockitoExtension.class)
class CommentHideRequestAdapterTest {

	private static final UUID COMMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID COMMENT_REPORT_UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

	@Mock
	private ModerationOutboxPort moderationOutboxPort;

	private CommentHideRequestAdapter commentHideRequestAdapter;

	@BeforeEach
	void setUp() {
		commentHideRequestAdapter = new CommentHideRequestAdapter(moderationOutboxPort);
	}

	@Test
	void savesCommentHideRequestToModerationOutbox() {
		commentHideRequestAdapter.requestHide(COMMENT_UUID, COMMENT_REPORT_UUID, 3L);

		ArgumentCaptor<ModerationActionRequired> captor = ArgumentCaptor.forClass(ModerationActionRequired.class);
		verify(moderationOutboxPort).save(captor.capture());
		ModerationActionRequired event = captor.getValue();
		assertThat(event.reportUuid()).isEqualTo(COMMENT_REPORT_UUID);
		assertThat(event.targetType()).isEqualTo(TargetType.COMMENT);
		assertThat(event.targetUuid()).isEqualTo(COMMENT_UUID);
		assertThat(event.reviewerUuid()).isNull();
	}
}
