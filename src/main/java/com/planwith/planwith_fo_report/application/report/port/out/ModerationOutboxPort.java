package com.planwith.planwith_fo_report.application.report.port.out;

import com.planwith.planwith_fo_report.domain.report.event.ModerationActionRequired;

public interface ModerationOutboxPort {

	void save(ModerationActionRequired event);
}
