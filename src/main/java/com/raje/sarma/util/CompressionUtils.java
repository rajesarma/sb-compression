package com.raje.sarma.util;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompressionUtils {

  public static byte[] compressFile(byte[] data) throws Exception {
    log.debug("Entered compressFile");
    byte[] compressedData;
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Deflater deflater = new Deflater();
      deflater.setInput(data);
      deflater.finish();
      byte[] buffer = new byte[1024];
      while (!deflater.finished()) {
        int count = deflater.deflate(buffer);
        outputStream.write(buffer, 0, count);
      }
      compressedData = outputStream.toByteArray();
    }
    log.debug("Exiting compressFile");
    return compressedData;
  }

  public static byte[] decompressData(byte[] compressedArray)
      throws IOException, DataFormatException {
    log.debug("Entered decompressData");
    byte[] unCompressedData;
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Inflater inflater = new Inflater();
      inflater.setInput(compressedArray);
      byte[] buffer = new byte[1024];
      while (!inflater.finished()) {
        int count = inflater.inflate(buffer);
        outputStream.write(buffer, 0, count);
      }
      unCompressedData = outputStream.toByteArray();
    }
    log.debug("Exiting decompressData");
    return unCompressedData;
  }

  public static byte[] getCompressedByteArray(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return null;
    }

    ByteArrayOutputStream bout = null;
    GZIPOutputStream zout = null;
    try {
      bout = new ByteArrayOutputStream();
      zout = new GZIPOutputStream(bout);
      zout.write(bytes);
    } catch (Exception e) {
      throw new RuntimeException("zip failed : ", e);
    } finally {
      try {
        if (zout != null) {
          zout.close();
        }
        if (bout != null) {
          bout.close();
        }
      } catch (Exception ee) {
        log.error("close failed : ", ee);
      }
    }
    return bout.toByteArray();
  }

}
