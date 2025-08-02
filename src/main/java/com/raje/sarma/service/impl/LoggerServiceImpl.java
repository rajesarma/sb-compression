package com.raje.sarma.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raje.sarma.constants.Constants;
import com.raje.sarma.model.Logger;
import com.raje.sarma.repository.LoggerRepository;
import com.raje.sarma.service.LoggerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoggerServiceImpl implements LoggerService {

  private final LoggerRepository loggerRepository;
  private final ObjectMapper objectMapper;

  @Override
  public void saveRequest(HttpServletRequest request, Object body) {
    try {

      LocalDateTime createdDate = getCurrentDateTime();
      String requestUri = getRequestUri(request);
      log.info("Request URL: {}", requestUri);
      log.info("API start date time: {} ", createdDate);
//      Map<String, String> headers = getHeaders(request);
      Logger logger = Logger.builder()
          .requestUri(requestUri)
          .createdBy(InetAddress.getLocalHost().getHostName()).httpMethod(request.getMethod())
//          .request(objectMapper.writeValueAsString(body)).headers(headers)
          .lastUpdatedBy(InetAddress.getLocalHost().getHostName())
          .build();
      logger = loggerRepository.save(logger);
      request.setAttribute("ID", logger.getId());
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  private LocalDateTime getCurrentDateTime() {
    return LocalDateTime.now();
  }

  private String getRequestUri(HttpServletRequest request) {
    StringBuilder requestURI = new StringBuilder(request.getRequestURI());
    String queryString = request.getQueryString();
    if (Objects.nonNull(queryString)) {
      return requestURI.append("?").append(queryString).toString();
    }
    return request.getRequestURI();
  }

  @Override
  public void saveResponse(HttpServletRequest request, HttpServletResponse response,
      Object body) {
    try {
      LocalDateTime lastUpdateDateTime = getCurrentDateTime();
      log.info("API end date time: {} ", lastUpdateDateTime);
      Optional<Logger> loggerOptional = findLoggerById(request);
      if (loggerOptional.isPresent()) {
        Logger logger = loggerOptional.get();
        logger.setLastUpdatedDate(lastUpdateDateTime);
        Long duration = Duration.between(logger.getCreatedDate(),
            logger.getLastUpdatedDate()).toSeconds();
        logger.setApiDuration(duration);
        logger.setLastUpdatedBy(InetAddress.getLocalHost().getHostName());
        String responseBody = objectMapper.writeValueAsString(body);

        if (StringUtils.hasText(responseBody)
            && responseBody.getBytes(StandardCharsets.UTF_8).length
            < Constants.MAX_RESPONSE_BYTES_SIZE) {
          logger.setResponse(responseBody);
        }
        loggerRepository.save(logger);
        log.info("Total duration of API: {} sec.", duration);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  @Override
  public void saveError(HttpServletRequest request, HttpServletResponse response,
      Exception exception) {
    try {
      Optional<Logger> loggerOptional = findLoggerById(request);
      if (loggerOptional.isPresent()) {
        Logger logger = loggerOptional.get();
        logger.setErrorMessage(exception.getMessage());
        logger.setStackTrace(Arrays.toString(exception.getStackTrace()));
        logger.setLastUpdatedDate(LocalDateTime.now());
        loggerRepository.save(logger);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  private Map<String, String> getHeaders(HttpServletRequest request) {
    Map<String, String> headers = new HashMap<>();
    request.getHeaderNames().asIterator()
        .forEachRemaining(header -> headers.put(header, request.getHeader(header)));
    return headers;
  }

  private Optional<Logger> findLoggerById(HttpServletRequest request) {
    Object loggerId = request.getAttribute(Constants.ID);
    if (Objects.nonNull(loggerId)) {
      return loggerRepository.findById((Long) loggerId);
    } else {
      return Optional.empty();
    }
  }
}
