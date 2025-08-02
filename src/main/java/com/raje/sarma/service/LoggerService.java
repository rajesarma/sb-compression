package com.raje.sarma.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.UnknownHostException;

public interface LoggerService {

  void saveRequest(HttpServletRequest request, Object body)
      throws JsonProcessingException, UnknownHostException;

  void saveResponse(HttpServletRequest request, HttpServletResponse response, Object body)
      throws JsonProcessingException;

  void saveError(HttpServletRequest request, HttpServletResponse response, Exception exception);
}
