package com.instana.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootApplication
public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@RestController
	@RequestMapping(value="**")
	public static class Api {

		private final int objectReferencesAllocatedPerRequest;
		private final int objectReferenceAllocationsMax;

		private final Duration delayBeforeMemoryDereferenced;

		private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
		private final AtomicInteger currentObjectsReferenceAllocations = new AtomicInteger();

		Api(
			@Value("${MEMORY_ALLOCATED_PER_REQUEST:2MB}") final String memoryAllocatedPerRequest,
			@Value("${MEMORY_ALLOCATED_MAXIMUM:1GB}") final String memoryAllocatedMaximum,
			@Value("${DELAY_BEFORE_MEMORY_DEREFERENCED:PT1M}") final String delayBeforeMemoryDereferenced
		) {
			final long bytesAllocatedPerRequest = DataSize.parse(memoryAllocatedPerRequest).toBytes();
			this.objectReferencesAllocatedPerRequest = (int) Math.min(Integer.MAX_VALUE, bytesAllocatedPerRequest / 4);

			final long objectReferencesAllocatedMax = (int) Math.min(Integer.MAX_VALUE, DataSize.parse(memoryAllocatedMaximum).toBytes() / 4);
			objectReferenceAllocationsMax = (int) objectReferencesAllocatedMax / objectReferencesAllocatedPerRequest;

			this.delayBeforeMemoryDereferenced = Duration.parse(delayBeforeMemoryDereferenced);

			LOGGER.info("Will allocate {} object references per request up to {} times at any one time, and free the object references allocated for one request after {}",
				objectReferencesAllocatedPerRequest, objectReferenceAllocationsMax, this.delayBeforeMemoryDereferenced.toString());
		}

		@GetMapping(produces = TEXT_PLAIN_VALUE)
		public String helloWorld(@Autowired HttpServletRequest request) {
			/*
			 * We allocate a certain amount of memory with every request, to be released later
			 * in order to trigger major Garbage Collections through memory pressure and the
			 * associated stop-the-world events to add artificial, inconsistent latency.
			 */
			final boolean allocateMoreObjects = objectReferenceAllocationsMax > currentObjectsReferenceAllocations.get();

			if (allocateMoreObjects) {
				final AtomicReference<Object> ref = new AtomicReference<>(new Object[objectReferencesAllocatedPerRequest]);
				currentObjectsReferenceAllocations.incrementAndGet();
	
				executor.schedule(() -> {
					ref.set(null);
					currentObjectsReferenceAllocations.decrementAndGet();
					// Now the allocated array can be garbage collected
				}, delayBeforeMemoryDereferenced.toMillis(), TimeUnit.MILLISECONDS);
			}

			return "Hello World!";
		}

	}

}