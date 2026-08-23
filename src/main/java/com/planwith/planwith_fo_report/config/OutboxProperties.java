package com.planwith.planwith_fo_report.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.outbox")
public record OutboxProperties(
		boolean relayEnabled,
		long relayIntervalMs,
		int batchSize
) {
}
