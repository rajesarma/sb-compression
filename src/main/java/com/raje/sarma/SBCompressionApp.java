package com.raje.sarma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SBCompressionApp {

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(SBCompressionApp.class);
    app.run(args);
  }
}
