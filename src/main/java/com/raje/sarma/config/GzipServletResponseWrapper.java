package com.raje.sarma.config;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

public class GzipServletResponseWrapper extends HttpServletResponseWrapper {

  private GzipServletOutputStream gzipOutputStream;
  private PrintWriter printWriter;

  public GzipServletResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (this.printWriter != null) {
      throw new IllegalStateException("getWriter() has already been called!");
    }
    if (this.gzipOutputStream == null) {
      this.gzipOutputStream = new GzipServletOutputStream(getResponse().getOutputStream());
    }
    return this.gzipOutputStream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (this.gzipOutputStream != null) {
      throw new IllegalStateException("getOutputStream() has already been called!");
    }
    if (this.printWriter == null) {
      String characterEncoding = getCharacterEncoding();
      if (characterEncoding == null) {
        characterEncoding = "UTF-8";
        getResponse().setCharacterEncoding(characterEncoding);
      }
      this.gzipOutputStream = new GzipServletOutputStream(getResponse().getOutputStream());
      this.printWriter = new PrintWriter(
          new OutputStreamWriter(gzipOutputStream, characterEncoding), true);
    }
    return this.printWriter;
  }

  @Override
  public void flushBuffer() throws IOException {
    if (this.printWriter != null) {
      this.printWriter.flush();
    } else if (this.gzipOutputStream != null) {
      this.gzipOutputStream.flush();
    }
    // Important: Also flush the underlying response buffer
    super.flushBuffer();
  }

  /**
   * Finishes the compression by closing the GZIPOutputStream. This MUST be called at the end of the
   * filter chain or when the response is complete.
   */
  public void finish() throws IOException {
    if (this.printWriter != null) {
      this.printWriter.close(); // This will also close the underlying gzipOutputStream
    } else if (this.gzipOutputStream != null) {
      this.gzipOutputStream.close();
    }
  }

  // Crucially, do NOT set content length when using GZIP
  @Override
  public void setContentLength(int len) {
    // Do nothing. Content length will be set by the GZIPOutputStream or handled via chunked encoding.
  }

  @Override
  public void setContentLengthLong(long len) {
    // Do nothing.
  }

  // Overriding this method to ensure the Content-Encoding header is set.
  // However, the filter is a more appropriate place for this header.
  // We'll primarily rely on the filter to set it.
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

  class GzipServletOutputStream extends ServletOutputStream {

    private final GZIPOutputStream gzipOutputStream;

    public GzipServletOutputStream(OutputStream outputStream) throws IOException {
      // Ensure that the GZIPOutputStream wraps the original outputStream
      this.gzipOutputStream = new GZIPOutputStream(outputStream);
    }

    @Override
    public void write(int b) throws IOException {
      gzipOutputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      gzipOutputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      gzipOutputStream.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
      // It's crucial to call finish() on GZIPOutputStream to write the trailing footer.
      // Then close the stream.
      gzipOutputStream.finish();
      gzipOutputStream.close();
    }

    @Override
    public void flush() throws IOException {
      gzipOutputStream.flush();
    }

    @Override
    public boolean isReady() {
      // For synchronous processing, this should generally return true.
      // For asynchronous, you'd integrate with WriteListener.
      return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
      // Not implemented for synchronous filters. For async servlet, this would be important.
      throw new UnsupportedOperationException("Async operations are not supported by this stream.");
    }
  }
}
