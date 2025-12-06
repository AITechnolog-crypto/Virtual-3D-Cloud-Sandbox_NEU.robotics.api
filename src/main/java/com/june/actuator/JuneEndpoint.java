package com.june.actuator;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom actuator endpoint that summarizes Spring beans for application packages.
 *
 * GET /actuator/june/beans
 */
@Component
@Endpoint(id = "june")
public class JuneEndpoint {
  private final ApplicationContext ctx;
  private static final List<String> PREFIXES = List.of("com.june", "de.nursystem", "tradeguard");

  public JuneEndpoint(ApplicationContext ctx) {
    this.ctx = ctx;
  }

  @ReadOperation
  public Map<String, Object> beans() {
    String[] names = ctx.getBeanDefinitionNames();
    Arrays.sort(names);

    List<Map<String, String>> list = new ArrayList<>();
    for (String name : names) {
      BeanDefinition bd;
      try {
        bd = ctx.getAutowireCapableBeanFactory().getBeanDefinition(name);
      } catch (Exception ex) {
        continue; // skip non-standard defs
      }
      String type = ctx.getType(name) != null ? ctx.getType(name).getName() : (bd.getBeanClassName());
      if (type == null) continue;
      boolean match = PREFIXES.stream().anyMatch(type::startsWith);
      if (!match) continue;
      String scope = (bd.getScope() == null || bd.getScope().isBlank()) ? "singleton" : bd.getScope();
      Map<String, String> m = new LinkedHashMap<>();
      m.put("name", name);
      m.put("type", type);
      m.put("scope", scope);
      list.add(m);
    }
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("count", list.size());
    out.put("beans", list);
    return out;
  }
}
