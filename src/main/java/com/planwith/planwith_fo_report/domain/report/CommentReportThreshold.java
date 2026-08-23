package com.planwith.planwith_fo_report.domain.report;

import com.planwith.planwith_fo_report.domain.report.exception.InvalidReportException;

public final class CommentReportThreshold {

	public static final int DEFAULT_HIDE_THRESHOLD = 3;

	private final int hideThreshold;

	private CommentReportThreshold(int hideThreshold) {
		if (hideThreshold < 1) {
			throw new InvalidReportException("신고 숨김 임계치는 1 이상이어야 합니다.");
		}
		this.hideThreshold = hideThreshold;
	}

	public static CommentReportThreshold of(int hideThreshold) {
		return new CommentReportThreshold(hideThreshold);
	}

	public static CommentReportThreshold defaultThreshold() {
		return of(DEFAULT_HIDE_THRESHOLD);
	}

	public boolean isReached(long reportCount) {
		return reportCount >= hideThreshold;
	}

	public int hideThreshold() {
		return hideThreshold;
	}
}
