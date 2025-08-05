package com.raje.sarma.config;

import static com.raje.sarma.constants.Constants.GZIP;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GzipResponseCompressionFilter extends OncePerRequestFilter {

  @Value("${app.compression.min-response-size:2048}")
  private int minResponseSize;

  @Value("${app.compression.enabled:true}")
  private boolean compressionEnabled;

  @Value("${app.compression.mime-types:}")
  private String compressibleMimeTypes;

  private static final List<String> EXCLUDE_GZIP_URLS =
      Arrays.asList("/api/no-compression", "/swagger-ui.html","/sb-compression/doc/fetch");

//  private static final List<String> INCLUDE_GZIP_URLS = List.of("/doc");

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    boolean acceptEncoding =
        StringUtils.hasText(request.getHeader(HttpHeaders.ACCEPT_ENCODING))
            && request.getHeader(HttpHeaders.ACCEPT_ENCODING).contains(GZIP);

    /*request.getHeaderNames().asIterator().forEachRemaining(header -> {
      log.info(header + " : {} " , request.getHeader(header));
    });*/

    if (!compressionEnabled || response.isCommitted() || shouldNotFilter(request)
        || !acceptEncoding || response.containsHeader(HttpHeaders.CONTENT_ENCODING)) {
      filterChain.doFilter(request, response);
      return;
    }
    handleResponseWithSizeCheck(request, response, filterChain); // implementation without size check
//    handleResponseWithoutSizeCheck(request, response, filterChain); // implementation without size check
  }

  private void handleResponseWithSizeCheck(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws IOException, ServletException {

    GzipResponseWrapper wrappedResponse = new GzipResponseWrapper(response);
    try {

      filterChain.doFilter(request, wrappedResponse);
      byte[] originalBytes = wrappedResponse.getCapturedData();
      String originalContentType = wrappedResponse.getContentType();
      String originalEncoding = wrappedResponse.getCharacterEncoding();

      boolean isCompressableMimeType = false;
      if (StringUtils.hasText(originalContentType) && StringUtils.hasText(compressibleMimeTypes)) {
        // Extract just the MIME type part, ignoring charset (e.g., "application/json;charset=UTF-8")
        String baseContentType = originalContentType.split(";")[0].trim();
        isCompressableMimeType = Arrays.stream(compressibleMimeTypes.split(","))
            .map(String::trim)
            .anyMatch(baseContentType::equalsIgnoreCase);
      }

      boolean shouldCompress = compressionEnabled
          && originalBytes.length >= minResponseSize
          && isCompressableMimeType
          && response.getStatus() == HttpServletResponse.SC_OK;
//          && !response.isCommitted();

      if (shouldCompress) {
        response.setHeader(HttpHeaders.CONTENT_ENCODING, GZIP);
        response.setHeader("Vary", "Accept-Encoding");

        response.setContentType(originalContentType);
        if (StringUtils.hasText(originalEncoding)) {
          response.setCharacterEncoding(originalEncoding);
        }

        response.setContentLength(-1); // Required when using GZIP

        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(response.getOutputStream())) {
          gzipOutputStream.write(originalBytes);
//          gzipOutputStream.finish(); // Ensure full compression output
        } /*catch (IOException e) {
          System.err.println(
              "Error during GZIP compression, falling back to uncompressed: {} " , e.getMessage());
          fallbackToUncompressed(response, originalBytes, originalContentType, originalEncoding);
        }*/
      } else {
        fallbackToUncompressed(response, originalBytes, originalContentType, originalEncoding);
      }

      // Debug output
      /*log.info("GZIP filter executed");
      log.info("Original response size: {} " , originalBytes.length);
      log.info("Should compress: {} " , shouldCompress);
      log.info("Content-Encoding set: {} " , response.getHeader(HttpHeaders.CONTENT_ENCODING));

      log.info("Final response content-type: {} " , response.getContentType());*/

      log.info("Original Content-Type: {}", originalContentType);
      log.info("Compression Enabled: {}", compressionEnabled);
      log.info("Original Size: {} " , originalBytes.length);
      log.info("Min Size: {} " , minResponseSize);
      log.info("Mime Type OK: {} " , isCompressableMimeType);
      log.info("Response Committed: {} " , response.isCommitted());
      log.info("Status: {} " , response.getStatus());
//      log.info("Final headers: ");
//      response.getHeaderNames()
//          .forEach(name -> log.info(name + ": {} " , response.getHeader(name)));

    } catch (Exception e) {
      log.error("Unexpected error in GzipCompressionFilter: {} " , e.getMessage());
      throw e; // Re-throw to propagate to other error handlers
    }
  }

  private void fallbackToUncompressed(HttpServletResponse response, byte[] originalBytes,
      String contentType, String encoding) throws IOException {
    // Clean up GZIP headers if previously set
    response.setHeader(HttpHeaders.CONTENT_ENCODING, null);
    response.setHeader("Vary", null);
    if (contentType != null) response.setContentType(contentType);
    if (encoding != null) response.setCharacterEncoding(encoding);
    response.setContentLength(originalBytes.length);
    response.getOutputStream().write(originalBytes);
    response.getOutputStream().flush();
  }

  private void handleResponseWithoutSizeCheck(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws IOException, ServletException {

    /*if (response.getContentType() == null ||
        Arrays.stream(compressibleMimeTypes.trim().split(","))
            .anyMatch(response.getContentType()::contains)) {
      response.setHeader(HttpHeaders.CONTENT_ENCODING, GZIP); // Set the header early
    }*/

    /*String contentType = request.getContentType(); // Get content type after chain.doFilter
    boolean isCompressible = StringUtils.hasText(contentType)
        && Arrays.stream(compressibleMimeTypes.trim().split(","))
        .anyMatch(contentType::contains);*/

    response.setHeader(HttpHeaders.CONTENT_ENCODING, GZIP); // Set the header early
    GzipServletResponseWrapper gzipResponseWrapper = new GzipServletResponseWrapper(response);
    try {
      filterChain.doFilter(request, gzipResponseWrapper);

    } finally {
      gzipResponseWrapper.flushBuffer();
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    AntPathMatcher pathMatcher = new AntPathMatcher();
    String servletPath = request.getServletPath();
    String requestURI = request.getRequestURI();
    return EXCLUDE_GZIP_URLS.stream().anyMatch(pattern ->
        pathMatcher.match(pattern, servletPath) || pathMatcher.match(pattern, requestURI));
  }
}

