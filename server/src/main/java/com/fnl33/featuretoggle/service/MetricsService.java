package com.fnl33.featuretoggle.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter toggleEvaluationCounter;
    private final Counter toggleEvaluationCacheHitCounter;
    private final Counter toggleEvaluationCacheMissCounter;
    private final Counter toggleCreatedCounter;
    private final Counter toggleUpdatedCounter;
    private final Counter toggleDeletedCounter;
    private final Counter attributeCreatedCounter;
    private final Counter attributeUpdatedCounter;
    private final Counter attributeDeletedCounter;
    private final Counter clientRegisteredCounter;
    private final Counter clientUnregisteredCounter;

    // Timers
    private final Timer toggleEvaluationTimer;
    private final Timer toggleServiceTimer;
    private final Timer attributeServiceTimer;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize Counters
        this.toggleEvaluationCounter = Counter.builder("toggle.evaluation.total")
                .description("Total number of toggle evaluations")
                .register(meterRegistry);

        this.toggleEvaluationCacheHitCounter = Counter.builder("toggle.evaluation.cache.hits")
                .description("Number of toggle evaluation cache hits")
                .register(meterRegistry);

        this.toggleEvaluationCacheMissCounter = Counter.builder("toggle.evaluation.cache.misses")
                .description("Number of toggle evaluation cache misses")
                .register(meterRegistry);

        this.toggleCreatedCounter = Counter.builder("toggle.created.total")
                .description("Total number of toggles created")
                .register(meterRegistry);

        this.toggleUpdatedCounter = Counter.builder("toggle.updated.total")
                .description("Total number of toggles updated")
                .register(meterRegistry);

        this.toggleDeletedCounter = Counter.builder("toggle.deleted.total")
                .description("Total number of toggles deleted")
                .register(meterRegistry);

        this.attributeCreatedCounter = Counter.builder("attribute.created.total")
                .description("Total number of attributes created")
                .register(meterRegistry);

        this.attributeUpdatedCounter = Counter.builder("attribute.updated.total")
                .description("Total number of attributes updated")
                .register(meterRegistry);

        this.attributeDeletedCounter = Counter.builder("attribute.deleted.total")
                .description("Total number of attributes deleted")
                .register(meterRegistry);

        this.clientRegisteredCounter = Counter.builder("client.registered.total")
                .description("Total number of clients registered")
                .register(meterRegistry);

        this.clientUnregisteredCounter = Counter.builder("client.unregistered.total")
                .description("Total number of clients unregistered")
                .register(meterRegistry);

        // Initialize Timers
        this.toggleEvaluationTimer = Timer.builder("toggle.evaluation.duration")
                .description("Time taken to evaluate a toggle")
                .register(meterRegistry);

        this.toggleServiceTimer = Timer.builder("toggle.service.duration")
                .description("Time taken for toggle service operations")
                .register(meterRegistry);

        this.attributeServiceTimer = Timer.builder("attribute.service.duration")
                .description("Time taken for attribute service operations")
                .register(meterRegistry);
    }

    // Counter increments
    public void incrementToggleEvaluation() {
        toggleEvaluationCounter.increment();
    }

    public void incrementToggleEvaluationCacheHit() {
        toggleEvaluationCacheHitCounter.increment();
    }

    public void incrementToggleEvaluationCacheMiss() {
        toggleEvaluationCacheMissCounter.increment();
    }

    public void incrementToggleCreated() {
        toggleCreatedCounter.increment();
    }

    public void incrementToggleUpdated() {
        toggleUpdatedCounter.increment();
    }

    public void incrementToggleDeleted() {
        toggleDeletedCounter.increment();
    }

    public void incrementAttributeCreated() {
        attributeCreatedCounter.increment();
    }

    public void incrementAttributeUpdated() {
        attributeUpdatedCounter.increment();
    }

    public void incrementAttributeDeleted() {
        attributeDeletedCounter.increment();
    }

    public void incrementClientRegistered() {
        clientRegisteredCounter.increment();
    }

    public void incrementClientUnregistered() {
        clientUnregisteredCounter.increment();
    }

    // Timer operations
    public Timer.Sample startToggleEvaluationTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordToggleEvaluationTime(Timer.Sample sample) {
        sample.stop(toggleEvaluationTimer);
    }

    public Timer.Sample startToggleServiceTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordToggleServiceTime(Timer.Sample sample) {
        sample.stop(toggleServiceTimer);
    }

    public Timer.Sample startAttributeServiceTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordAttributeServiceTime(Timer.Sample sample) {
        sample.stop(attributeServiceTimer);
    }

    // Convenience method for executing code with timing
    public void timeToggleEvaluation(Runnable task) {
        toggleEvaluationTimer.record(task);
    }

    public void timeToggleService(Runnable task) {
        toggleServiceTimer.record(task);
    }

    public void timeAttributeService(Runnable task) {
        attributeServiceTimer.record(task);
    }

    public <T> T timeToggleEvaluationCallable(java.util.concurrent.Callable<T> task) throws Exception {
        return toggleEvaluationTimer.recordCallable(task);
    }

    public <T> T timeToggleServiceCallable(java.util.concurrent.Callable<T> task) throws Exception {
        return toggleServiceTimer.recordCallable(task);
    }

    public <T> T timeAttributeServiceCallable(java.util.concurrent.Callable<T> task) throws Exception {
        return attributeServiceTimer.recordCallable(task);
    }
}
