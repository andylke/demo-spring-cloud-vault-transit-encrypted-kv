package com.github.andylke.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class DemoSpringCloudVaultTransitEncryptedKv {

  public static void main(String[] args) {
    SpringApplication.run(DemoSpringCloudVaultTransitEncryptedKv.class, args);
  }

  @Autowired private Environment environment;

  @EventListener({ApplicationReadyEvent.class})
  void onReady() {
    System.out.println(environment.getProperty("db.username"));
    System.out.println(environment.getProperty("db.password"));
    System.out.println(environment.getProperty("redis.password"));
  }
}
