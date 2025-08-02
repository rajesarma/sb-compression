package com.raje.sarma.config;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class GzipResponseWrapper extends HttpServletResponseWrapper {

  private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  private ServletOutputStream servletOutputStream;
  private PrintWriter writer;

  public GzipResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  @Override
  public ServletOutputStream getOutputStream() {
    if (writer != null) {
      throw new IllegalStateException("getWriter() has already been called");
    }

    if (servletOutputStream == null) {
      servletOutputStream = new ServletOutputStream() {
        @Override
        public void write(int b) {
          buffer.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
          buffer.write(b, off, len);
        }

        @Override
        public boolean isReady() {
          return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {
          throw new UnsupportedOperationException(
              "Async operations are not supported by this stream.");
        }
      };
    }
    return servletOutputStream;
  }

  @Override
  public PrintWriter getWriter() {
    if (servletOutputStream != null) {
      throw new IllegalStateException("getOutputStream() has already been called");
    }

    if (writer == null) {
      String characterEncoding = getCharacterEncoding();
      if (characterEncoding == null) {
        characterEncoding = "UTF-8";
        getResponse().setCharacterEncoding(characterEncoding);
      }
      writer = new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8));
    }
    return writer;
  }

  @Override
  public void setContentLength(int len) {
    // Do nothing. Content length will be set by the GZIPOutputStream or handled via chunked encoding.
  }

  @Override
  public void setContentLengthLong(long len) {
    // Do nothing.
  }

  @Override
  public void setHeader(String name, String value) {
    if (!"Content-Length".equalsIgnoreCase(name)) { // Prevent setting Content-Length
      super.setHeader(name, value);
    }
  }

  @Override
  public void addHeader(String name, String value) {
    if (!"Content-Length".equalsIgnoreCase(name)) { // Prevent adding Content-Length
      super.addHeader(name, value);
    }
  }

  public byte[] getCapturedData() {
    if (writer != null) {
      writer.flush();
    }
    return buffer.toByteArray();
  }
}
