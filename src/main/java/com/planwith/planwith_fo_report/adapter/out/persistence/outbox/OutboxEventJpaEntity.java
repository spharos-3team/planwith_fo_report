package com.planwith.planwith_fo_report.adapter.out.persistence.outbox;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
		name = "report_outbox_event",
		indexes = {
				@Index(name = "idx_outbox_status_created", columnList = "status, created_at")
		}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_uuid", nullable = false, unique = true, length = 36)
	private String eventUuid;

	@Column(name = "event_type", nullable = false, length = 80)
	private String eventType;

	@Column(name = "aggregate_type", nullable = false, length = 40)
	private String aggregateType;

	@Column(name = "aggregate_uuid", nullable = false, length = 36)
	private String aggregateUuid;

	@Lob
	@Column(name = "payload", nullable = false)
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private OutboxStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "published_at")
	private Instant publishedAt;

	public static OutboxEventJpaEntity pending(
			String eventUuid,
			String eventType,
			String aggregateType,
			String aggregateUuid,
			String payload,
			Instant createdAt
	) {
		OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
		entity.eventUuid = eventUuid;
		entity.eventType = eventType;
		entity.aggregateType = aggregateType;
		entity.aggregateUuid = aggregateUuid;
		entity.payload = payload;
		entity.status = OutboxStatus.PENDING;
		entity.createdAt = createdAt;
		return entity;
	}

	public void markPublished(Instant publishedAt) {
		this.status = OutboxStatus.PUBLISHED;
		this.publishedAt = publishedAt;
	}

	public void markFailed() {
		this.status = OutboxStatus.FAILED;
	}
}
