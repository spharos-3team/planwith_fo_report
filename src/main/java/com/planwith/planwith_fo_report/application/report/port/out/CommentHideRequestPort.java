package com.planwith.planwith_fo_report.application.report.port.out;

import java.util.UUID;

public interface CommentHideRequestPort {

	void requestHide(UUID commentUuid, long reportCount, int threshold);
}
