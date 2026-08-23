package com.planwith.planwith_fo_report.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "app.outbox", name = "relay-enabled", havingValue = "true")
public class OutboxRelayConfig {
}
