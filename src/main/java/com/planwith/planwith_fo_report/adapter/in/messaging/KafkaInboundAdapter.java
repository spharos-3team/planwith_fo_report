package com.planwith.planwith_fo_report.adapter.in.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 외부 서비스 이벤트 구독용 Kafka inbound 확장 지점.
 * 자동 판정 알고리즘은 이번 범위에서 구현하지 않는다.
 */
@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class KafkaInboundAdapter {
}
