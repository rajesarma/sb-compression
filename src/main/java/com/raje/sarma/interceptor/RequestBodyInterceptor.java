package com.raje.sarma.interceptor;

import com.raje.sarma.constants.Constants;
import com.raje.sarma.service.LoggerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

@ControllerAdvice
@RequiredArgsConstructor
public class RequestBodyInterceptor extends RequestBodyAdviceAdapter implements HandlerInterceptor {
  private final LoggerService loggerService;

  @Override
  public boolean supports(@NonNull MethodParameter methodParameter, @NonNull Type targetType,
      @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public @NonNull Object afterBodyRead(@NonNull Object body, @NonNull HttpInputMessage inputMessage,
      @NonNull MethodParameter parameter,
      @NonNull Type targetType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
    HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
        RequestContextHolder.getRequestAttributes())).getRequest();
    try {
      loggerService.saveRequest(request, body);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull Object handler) {
    try {
      if (HttpMethod.GET.toString().equalsIgnoreCase(request.getMethod())
          || HttpMethod.DELETE.toString().equalsIgnoreCase(request.getMethod())) {
        loggerService.saveRequest(request, null);
      } else if (
          HttpMethod.POST.toString().equalsIgnoreCase(request.getMethod())
              && request.getContentType().contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, Object> parameterMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(multipartRequest.getFileMap())) {
          multipartRequest.getFileMap().forEach(
              (key, value) ->
                  parameterMap.put(key, Map.of(Constants.FILENAME,
                      Objects.requireNonNull(value.getOriginalFilename()),
                      Constants.CONTENT_TYPE,
                      Objects.requireNonNull(value.getContentType()))));
          parameterMap.putAll(multipartRequest.getParameterMap());
        } else {
          parameterMap.putAll(request.getParameterMap());
        }
        loggerService.saveRequest(request, parameterMap);
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return true;
  }

}
