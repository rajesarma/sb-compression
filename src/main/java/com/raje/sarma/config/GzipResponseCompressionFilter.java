package com.raje.sarma.config;

import static com.raje.sarma.constants.Constants.GZIP;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class GzipResponseCompressionFilter extends OncePerRequestFilter {

  @Value("${app.compression.min-response-size:2048}")
  private int minResponseSize;

  @Value("${app.compression.enabled:true}")
  private boolean compressionEnabled;

  @Value("${app.compression.mime-types:}")
  private String compressibleMimeTypes;

  private static final List<String> EXCLUDE_GZIP_URLS =
      Arrays.asList("/api/no-compression", "/swagger-ui.html"
//          ,"/sb-compression/doc"
      );

//  private static final List<String> INCLUDE_GZIP_URLS = List.of("/doc");

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    boolean acceptEncoding =
        StringUtils.hasText(request.getHeader(HttpHeaders.ACCEPT_ENCODING))
            && request.getHeader(HttpHeaders.ACCEPT_ENCODING).contains("gzip");

    /*request.getHeaderNames().asIterator().forEachRemaining(header -> {
      System.out.println(header + " : " + request.getHeader(header));
    });*/

    if (!compressionEnabled || response.isCommitted() || shouldNotFilter(request)
        || !acceptEncoding || response.containsHeader(HttpHeaders.CONTENT_ENCODING)) {
      filterChain.doFilter(request, response);
      return;
    }
//    handleResponseWithSizeCheck(request, response, filterChain); // implementation without size check
    handleResponseWithoutSizeCheck(request, response, filterChain); // implementation without size check
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

      String contentType = response.getContentType();
      boolean isCompressableMimeType = false;
      if (contentType != null) {
        // Extract just the MIME type part, ignoring charset (e.g., "application/json;charset=UTF-8")
        String baseContentType = contentType.split(";")[0].trim();
        Set<String> mimeTypes = Arrays.stream(compressibleMimeTypes.trim().split(","))
            .collect(Collectors.toSet());
        for (String mimeType : mimeTypes) {
          if (baseContentType.equalsIgnoreCase(mimeType.trim())) {
            isCompressableMimeType = true;
            break;
          }
        }
      }

      boolean shouldCompress = compressionEnabled
          && originalBytes.length >= minResponseSize
          && isCompressableMimeType;

      if (response.getStatus() != HttpServletResponse.SC_OK) {
        // Non-OK status: write uncompressed
        response.getOutputStream().write(originalBytes);
        return;
      }

      if (shouldCompress) {
        response.setHeader(HttpHeaders.CONTENT_ENCODING, GZIP);
        response.setHeader("Vary", "Accept-Encoding");
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(-1); // Required when using GZIP

        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(response.getOutputStream())) {
          gzipOutputStream.write(originalBytes);
        } catch (IOException e) {
          System.err.println(
              "Error during GZIP compression, falling back to uncompressed: " + e.getMessage());
          // Clear GZIP headers if fallback
          response.setHeader(HttpHeaders.CONTENT_ENCODING, null);
          response.setHeader("Vary", null); // Remove Vary if not Gzipped
          response.setContentLength(originalBytes.length); // Set original length
          response.getOutputStream().write(originalBytes); // Write uncompressed
          response.getOutputStream().flush(); // Explicit flush for fallback
        }
      } else {
        response.setContentLength(originalBytes.length); // Set original length
        response.getOutputStream().write(originalBytes); // Write uncompressed
        response.getOutputStream().flush(); // Explicit flush for non-compressed path
      }

      response.setHeader(HttpHeaders.CONTENT_ENCODING, GZIP);
      response.setHeader("Vary", "Accept-Encoding");
      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");
      response.setContentLength(-1); // Required when using GZIP

      System.out.println("GZIP filter invoked");
      System.out.println("Original content size: " + originalBytes.length);
      System.out.println("Content type: " + contentType);
      System.out.println("Content encoding: " + response.getHeader(HttpHeaders.CONTENT_ENCODING));
    } catch (Exception e) {
      System.err.println("Unexpected error in GzipCompressionFilter: " + e.getMessage());
      throw e; // Re-throw to propagate to other error handlers
    }
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

