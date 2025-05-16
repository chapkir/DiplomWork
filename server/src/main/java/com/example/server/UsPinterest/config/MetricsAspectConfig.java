package com.example.server.UsPinterest.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.server.UsPinterest.repository.PinRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;

@Configuration
public class MetricsAspectConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public Gauge pinsCountGauge(MeterRegistry registry, PinRepository pinRepository) {
        return Gauge.builder("pins_count", pinRepository, PinRepository::count)
                .description("Total number of pins")
                .register(registry);
    }

    @Bean
    public Counter authLoginCounter(MeterRegistry registry) {
        return Counter.builder("auth.login.attempts")
                .description("Number of login attempts")
                .register(registry);
    }

    @Bean
    public Counter authRegisterCounter(MeterRegistry registry) {
        return Counter.builder("auth.register.count")
                .description("Number of registration requests")
                .register(registry);
    }

    @Bean
    public Counter postCreateCounter(MeterRegistry registry) {
        return Counter.builder("post.create.count")
                .description("Number of posts created")
                .register(registry);
    }

    @Bean
    public Counter profileImageUploadCounter(MeterRegistry registry) {
        return Counter.builder("profile.image.upload.count")
                .description("Number of profile image uploads")
                .register(registry);
    }

    @Bean
    public DistributionSummary fileUploadSizeSummary(MeterRegistry registry) {
        return DistributionSummary.builder("file.upload.size")
                .description("Size distribution of uploaded files")
                .baseUnit("bytes")
                .register(registry);
    }
}