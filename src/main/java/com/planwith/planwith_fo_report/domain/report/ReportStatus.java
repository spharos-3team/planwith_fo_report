package com.planwith.planwith_fo_report.domain.report;

public enum ReportStatus {
	RECEIVED,
	REVIEWING,
	APPROVED,
	REJECTED,
	ACTIONED;

	public boolean canTransitionTo(ReportStatus nextStatus) {
		if (nextStatus == null) {
			return false;
		}

		return switch (this) {
			case RECEIVED -> nextStatus == REVIEWING;
			case REVIEWING -> nextStatus == APPROVED || nextStatus == REJECTED;
			case APPROVED -> nextStatus == ACTIONED;
			case REJECTED, ACTIONED -> false;
		};
	}
}
