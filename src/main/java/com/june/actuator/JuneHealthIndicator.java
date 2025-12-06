package com.june.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class JuneHealthIndicator implements HealthIndicator {

  private final Executor executor;

  public JuneHealthIndicator(Executor executor) {
    this.executor = executor;
  }

  @Override
  public Health health() {
    boolean ready = (executor != null);
    if (ready) {
      return Health.up().withDetail("asyncExecutor", executor.getClass().getName()).build();
    }
    return Health.down().withDetail("asyncExecutor", "missing").build();
  }
}
