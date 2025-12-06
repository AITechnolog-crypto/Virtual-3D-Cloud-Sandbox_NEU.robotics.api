package Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

/**
 * Central bean configuration for the June application.
 * - Provides ObjectMapper and RestTemplate beans
 * - Configures the default async executor using properties under 'june.async.*'
 */
@Configuration
@EnableConfigurationProperties(JuneAsyncProperties.class)
public class BeansConfig implements AsyncConfigurer {

  private final JuneAsyncProperties props;

  public BeansConfig(JuneAsyncProperties props) {
    this.props = props;
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean(name = "asyncExecutor")
  public Executor asyncExecutor(JuneAsyncProperties props) {
    ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
    exec.setCorePoolSize(props.getCorePoolSize());
    exec.setMaxPoolSize(props.getMaxPoolSize());
    exec.setQueueCapacity(props.getQueueCapacity());
    exec.setThreadNamePrefix("async-");
    exec.initialize();
    return exec;
  }

  // Alias for Spring's default async executor bean name
  @Bean(name = "taskExecutor")
  public Executor taskExecutor(Executor asyncExecutor) {
    return asyncExecutor;
  }

  @Override
  public Executor getAsyncExecutor() {
    return taskExecutor(asyncExecutor(this.props));
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (ex, method, params) -> {
      // simple log to stderr to avoid extra deps
      System.err.println("[AsyncError] " + method + " -> " + ex.getMessage());
    };
  }
}
