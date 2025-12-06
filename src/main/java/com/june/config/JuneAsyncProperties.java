package com.june.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties to configure the async executor used by the June application.
 *
 * Prefix: june.async
 */
@ConfigurationProperties(prefix = "june.async")
public class JuneAsyncProperties {
  private int corePoolSize = 4;
  private int maxPoolSize = 8;
  private int queueCapacity = 100;

  public int getCorePoolSize() {
    return corePoolSize;
  }

  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  public void setMaxPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }

  public int getQueueCapacity() {
    return queueCapacity;
  }

  public void setQueueCapacity(int queueCapacity) {
    this.queueCapacity = queueCapacity;
  }
}
