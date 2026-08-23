package com.planwith.planwith_fo_report.application.report.port.out;

import com.planwith.planwith_fo_report.domain.report.event.DomainEvent;

public interface ReportEventPublisherPort {

	void publish(DomainEvent event);
}
