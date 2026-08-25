package com.planwith.planwith_fo_report.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.planwith.planwith_fo_report.adapter.out.persistence.outbox.OutboxEventJpaRepository;
import com.planwith.planwith_fo_report.adapter.out.persistence.outbox.OutboxStatus;
import com.planwith.planwith_fo_report.application.report.port.out.StoryCommentReportRepository;
import com.planwith.planwith_fo_report.domain.report.ReportType;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

@ActiveProfiles("test")
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:report_full_flow_testdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"app.kafka.enabled=true",
		"app.outbox.relay-enabled=true",
		"app.outbox.relay-interval-ms=3600000"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(
		partitions = 1,
		topics = CommentReportFullFlowIntegrationTests.COMMENT_HIDE_TOPIC
)
@Import(CommentReportFullFlowIntegrationTests.KafkaIntegrationTestConfiguration.class)
class CommentReportFullFlowIntegrationTests {

	static final String COMMENT_HIDE_TOPIC = "planwith.report.comment-report-threshold-reached";

	private static final String REPORT_PATH = "/api/planwith-fo-report/reports/comments/";
	private static final String MEMBER_UUID_HEADER = "X-Auth-User-Id";
	private static final UUID AUTHOR_UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private static final UUID MEMBER_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID MEMBER_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MEMBER_C = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final UUID MEMBER_D = UUID.fromString("44444444-4444-4444-4444-444444444444");
	private static final TestCommentService COMMENT_SERVICE = TestCommentService.start();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StoryCommentReportRepository storyCommentReportRepository;

	@Autowired
	private OutboxEventJpaRepository outboxEventJpaRepository;

	@Autowired
	private OutboxEventRelay outboxEventRelay;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@DynamicPropertySource
	static void registerCommentServiceProperties(DynamicPropertyRegistry registry) {
		registry.add("app.comment-service.base-url", COMMENT_SERVICE::baseUrl);
		registry.add(
				"app.comment-service.report-context-path",
				() -> "/internal/comments/{commentUuid}/report-context"
		);
	}

	@BeforeEach
	void setUp() {
		jdbcTemplate.update("DELETE FROM report_outbox_event");
		jdbcTemplate.update("DELETE FROM story_comment_report");
		COMMENT_SERVICE.reset();
	}

	@AfterAll
	static void stopCommentService() {
		COMMENT_SERVICE.stop();
	}

	@ParameterizedTest
	@EnumSource(ReportType.class)
	void acceptsEverySupportedReportReason(ReportType reportType) throws Exception {
		UUID commentUuid = UUID.randomUUID();
		COMMENT_SERVICE.register(commentUuid, AUTHOR_UUID, true);

		assertSuccessfulReport(commentUuid, MEMBER_A, reportType, 1, false)
				.andExpect(jsonPath("$.reportType").value(reportType.name()));

		assertThat(storyCommentReportRepository.countByCommentUuid(commentUuid)).isEqualTo(1L);
		assertThat(storyCommentReportRepository.existsByCommentUuidAndMemberUuid(commentUuid, MEMBER_A)).isTrue();
	}

	@Test
	void rejectsDuplicateReportFromSameMember() throws Exception {
		UUID commentUuid = UUID.randomUUID();
		COMMENT_SERVICE.register(commentUuid, AUTHOR_UUID, true);
		assertSuccessfulReport(commentUuid, MEMBER_A, ReportType.SPAM, 1, false);

		performReport(commentUuid, MEMBER_A, ReportType.ABUSE)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DUPLICATE_COMMENT_REPORT"));

		assertThat(storyCommentReportRepository.countByCommentUuid(commentUuid)).isEqualTo(1L);
	}

	@Test
	void rejectsSelfReport() throws Exception {
		UUID commentUuid = UUID.randomUUID();
		COMMENT_SERVICE.register(commentUuid, AUTHOR_UUID, true);

		performReport(commentUuid, AUTHOR_UUID, ReportType.HATE)
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("SELF_REPORT_NOT_ALLOWED"));

		assertThat(storyCommentReportRepository.countByCommentUuid(commentUuid)).isZero();
	}

	@Test
	void rejectsMissingComment() throws Exception {
		UUID commentUuid = UUID.randomUUID();

		performReport(commentUuid, MEMBER_A, ReportType.PRIVACY)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("COMMENT_NOT_FOUND"));

		assertThat(storyCommentReportRepository.countByCommentUuid(commentUuid)).isZero();
	}

	@Test
	void rejectsDeletedComment() throws Exception {
		UUID commentUuid = UUID.randomUUID();
		COMMENT_SERVICE.register(commentUuid, AUTHOR_UUID, false);

		performReport(commentUuid, MEMBER_A, ReportType.OTHER)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("COMMENT_NOT_REPORTABLE"));

		assertThat(storyCommentReportRepository.countByCommentUuid(commentUuid)).isZero();
	}

	@Test
	void accumulatesReportsPublishesThresholdEventAndHidesComment() throws Exception {
		UUID commentUuid = UUID.randomUUID();
		COMMENT_SERVICE.register(commentUuid, AUTHOR_UUID, true);

		assertSuccessfulReport(commentUuid, MEMBER_A, ReportType.SPAM, 1, false);
		assertThat(COMMENT_SERVICE.isHidden(commentUuid)).isFalse();
		assertThat(outboxEventJpaRepository.findAll()).isEmpty();

		assertSuccessfulReport(commentUuid, MEMBER_B, ReportType.ABUSE, 2, false);
		assertThat(COMMENT_SERVICE.isHidden(commentUuid)).isFalse();
		assertThat(outboxEventJpaRepository.findAll()).isEmpty();

		assertSuccessfulReport(commentUuid, MEMBER_C, ReportType.HATE, 3, true);
		assertThat(storyCommentReportRepository.countByCommentUuid(commentUuid)).isEqualTo(3L);
		assertThat(outboxEventJpaRepository.findAll())
				.singleElement()
				.satisfies(event -> {
					assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
					assertThat(event.getEventType()).isEqualTo("COMMENT_REPORT_THRESHOLD_REACHED");
					assertThat(event.getAggregateUuid()).isEqualTo(commentUuid.toString());
					assertThat(event.getPayload()).contains("\"reportCount\":3", "\"threshold\":3");
				});

		outboxEventRelay.publishPendingEvents();

		String publishedPayload = COMMENT_SERVICE.awaitHideEvent(Duration.ofSeconds(10));
		assertThat(publishedPayload)
				.contains("\"eventType\":\"COMMENT_REPORT_THRESHOLD_REACHED\"")
				.contains("\"commentUuid\":\"" + commentUuid + "\"")
				.contains("\"reportCount\":3")
				.contains("\"threshold\":3");
		assertThat(COMMENT_SERVICE.isHidden(commentUuid)).isTrue();
		assertThat(outboxEventJpaRepository.findAll())
				.singleElement()
				.satisfies(event -> {
					assertThat(event.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
					assertThat(event.getPublishedAt()).isNotNull();
				});

		assertSuccessfulReport(commentUuid, MEMBER_D, ReportType.OTHER, 4, false);
		assertThat(storyCommentReportRepository.countByCommentUuid(commentUuid)).isEqualTo(4L);
		assertThat(outboxEventJpaRepository.findAll()).hasSize(1);

		outboxEventRelay.publishPendingEvents();
		assertThat(COMMENT_SERVICE.awaitHideEvent(Duration.ofMillis(500))).isNull();
	}

	@Test
	void concurrentDuplicateRequestsCreateOnlyOneReport() throws Exception {
		UUID commentUuid = UUID.randomUUID();
		COMMENT_SERVICE.register(commentUuid, AUTHOR_UUID, true);
		int requestCount = 8;
		ExecutorService executor = Executors.newFixedThreadPool(requestCount);
		CountDownLatch ready = new CountDownLatch(requestCount);
		CountDownLatch start = new CountDownLatch(1);

		try {
			List<Future<Integer>> responses = new ArrayList<>();
			for (int index = 0; index < requestCount; index++) {
				responses.add(executor.submit(() -> {
					ready.countDown();
					start.await(5, TimeUnit.SECONDS);
					return performReport(commentUuid, MEMBER_A, ReportType.SPAM)
							.andReturn()
							.getResponse()
							.getStatus();
				}));
			}

			assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
			start.countDown();

			List<Integer> statuses = new ArrayList<>();
			for (Future<Integer> response : responses) {
				statuses.add(response.get(15, TimeUnit.SECONDS));
			}

			assertThat(statuses).filteredOn(status -> status == 201).hasSize(1);
			assertThat(statuses).filteredOn(status -> status == 409).hasSize(requestCount - 1);
			assertThat(storyCommentReportRepository.countByCommentUuid(commentUuid)).isEqualTo(1L);
		} finally {
			start.countDown();
			executor.shutdownNow();
		}
	}

	private ResultActions assertSuccessfulReport(
			UUID commentUuid,
			UUID memberUuid,
			ReportType reportType,
			long expectedCount,
			boolean thresholdReached
	) throws Exception {
		return performReport(commentUuid, memberUuid, reportType)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.commentReportUuid").isNotEmpty())
				.andExpect(jsonPath("$.commentUuid").value(commentUuid.toString()))
				.andExpect(jsonPath("$.reportCount").value(expectedCount))
				.andExpect(jsonPath("$.thresholdReached").value(thresholdReached))
				.andExpect(jsonPath("$.message").value("댓글을 신고했다"));
	}

	private ResultActions performReport(UUID commentUuid, UUID memberUuid, ReportType reportType) throws Exception {
		return mockMvc.perform(post(REPORT_PATH + commentUuid)
				.header(MEMBER_UUID_HEADER, memberUuid)
				.contentType("application/json")
				.content("{\"reportType\":\"" + reportType.name() + "\"}"));
	}

	@EnableKafka
	@TestConfiguration(proxyBeanMethods = false)
	static class KafkaIntegrationTestConfiguration {

		@Bean
		ProducerFactory<String, String> producerFactory(EmbeddedKafkaBroker broker) {
			Map<String, Object> properties = KafkaTestUtils.producerProps(broker);
			properties.put(ProducerConfig.ACKS_CONFIG, "all");
			return new DefaultKafkaProducerFactory<>(properties, new StringSerializer(), new StringSerializer());
		}

		@Bean
		KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
			return new KafkaTemplate<>(producerFactory);
		}

		@Bean
		ConsumerFactory<String, String> consumerFactory(EmbeddedKafkaBroker broker) {
			Map<String, Object> properties = KafkaTestUtils.consumerProps(
					broker,
					"comment-service-full-flow",
					true
			);
			properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
			return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), new StringDeserializer());
		}

		@Bean
		ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
				ConsumerFactory<String, String> consumerFactory
		) {
			ConcurrentKafkaListenerContainerFactory<String, String> factory =
					new ConcurrentKafkaListenerContainerFactory<>();
			factory.setConsumerFactory(consumerFactory);
			return factory;
		}

		@Bean
		CommentHideEventConsumer commentHideEventConsumer() {
			return new CommentHideEventConsumer();
		}
	}

	static class CommentHideEventConsumer {

		@KafkaListener(topics = COMMENT_HIDE_TOPIC, groupId = "comment-service-full-flow")
		void consume(String payload) {
			COMMENT_SERVICE.consumeHideEvent(payload);
		}
	}

	private static final class TestCommentService {

		private static final Pattern COMMENT_UUID_PATTERN =
				Pattern.compile("\\\"commentUuid\\\":\\\"([^\\\"]+)\\\"");
		private static final String REPORT_CONTEXT_PREFIX = "/internal/comments/";
		private static final String REPORT_CONTEXT_SUFFIX = "/report-context";

		private final HttpServer server;
		private final ExecutorService executor;
		private final Map<UUID, TestComment> comments = new ConcurrentHashMap<>();
		private final BlockingQueue<String> hideEvents = new LinkedBlockingQueue<>();

		private TestCommentService(HttpServer server, ExecutorService executor) {
			this.server = server;
			this.executor = executor;
		}

		static TestCommentService start() {
			try {
				HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
				ExecutorService executor = Executors.newCachedThreadPool();
				TestCommentService service = new TestCommentService(server, executor);
				server.createContext(REPORT_CONTEXT_PREFIX, service::handleReportContextRequest);
				server.setExecutor(executor);
				server.start();
				return service;
			} catch (IOException exception) {
				throw new IllegalStateException("통합 테스트용 Comment Service를 시작할 수 없습니다.", exception);
			}
		}

		String baseUrl() {
			return "http://" + server.getAddress().getHostString() + ":" + server.getAddress().getPort();
		}

		void register(UUID commentUuid, UUID authorUuid, boolean reportable) {
			comments.put(commentUuid, new TestComment(authorUuid, reportable));
		}

		void consumeHideEvent(String payload) {
			Matcher matcher = COMMENT_UUID_PATTERN.matcher(payload);
			if (!matcher.find()) {
				throw new IllegalArgumentException("댓글 UUID가 없는 숨김 이벤트입니다.");
			}
			UUID commentUuid = UUID.fromString(matcher.group(1));
			TestComment comment = comments.get(commentUuid);
			if (comment != null) {
				comment.hide();
			}
			hideEvents.add(payload);
		}

		boolean isHidden(UUID commentUuid) {
			TestComment comment = comments.get(commentUuid);
			return comment != null && comment.hidden();
		}

		String awaitHideEvent(Duration timeout) throws InterruptedException {
			return hideEvents.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
		}

		void reset() {
			comments.clear();
			hideEvents.clear();
		}

		void stop() {
			server.stop(0);
			executor.shutdownNow();
		}

		private void handleReportContextRequest(HttpExchange exchange) throws IOException {
			try (exchange) {
				if (!"GET".equals(exchange.getRequestMethod())) {
					writeResponse(exchange, 405, "");
					return;
				}

				UUID commentUuid = extractCommentUuid(exchange.getRequestURI().getPath());
				TestComment comment = comments.get(commentUuid);
				if (comment == null) {
					writeResponse(exchange, 404, "");
					return;
				}

				String response = """
						{
						  "commentUuid":"%s",
						  "authorMemberUuid":"%s",
						  "reportable":%s
						}
						""".formatted(commentUuid, comment.authorUuid(), comment.reportable());
				writeResponse(exchange, 200, response);
			}
		}

		private static UUID extractCommentUuid(String path) {
			if (!path.startsWith(REPORT_CONTEXT_PREFIX) || !path.endsWith(REPORT_CONTEXT_SUFFIX)) {
				throw new IllegalArgumentException("지원하지 않는 Comment Service 경로입니다: " + path);
			}
			String value = path.substring(
					REPORT_CONTEXT_PREFIX.length(),
					path.length() - REPORT_CONTEXT_SUFFIX.length()
			);
			return UUID.fromString(value);
		}

		private static void writeResponse(HttpExchange exchange, int status, String body) throws IOException {
			byte[] response = body.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
			exchange.sendResponseHeaders(status, response.length);
			try (OutputStream output = exchange.getResponseBody()) {
				output.write(response);
			}
		}
	}

	private static final class TestComment {

		private final UUID authorUuid;
		private final boolean reportable;
		private volatile boolean hidden;

		private TestComment(UUID authorUuid, boolean reportable) {
			this.authorUuid = authorUuid;
			this.reportable = reportable;
		}

		UUID authorUuid() {
			return authorUuid;
		}

		boolean reportable() {
			return reportable;
		}

		boolean hidden() {
			return hidden;
		}

		void hide() {
			hidden = true;
		}
	}
}
