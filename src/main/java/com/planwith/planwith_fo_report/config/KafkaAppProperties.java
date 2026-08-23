package com.planwith.planwith_fo_report.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaAppProperties(
		boolean enabled,
		Topics topics
) {

	public record Topics(
			String reportCreated,
			String reportReviewed,
			String moderationActionRequired,
			String commentReportThresholdReached
	) {
	}
}
