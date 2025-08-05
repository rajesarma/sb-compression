package com.raje.sarma.constants;

public class Constants {

  private Constants() {
    throw new IllegalStateException("Constant class");
  }

  public static final String DOCUMENT_SUCCESS = "Document found successfully.";
  public static final String INTERNAL_API_ERROR = "Internal api error.";
  public static final String DOC_NOT_FOUND = "Document not found.";
  public static final String FILENAME = "fileName";
  public static final String CONTENT_TYPE = "contentType";
  public static final String ID = "ID";
  public static final String GZIP = "gzip";
  public static final int MAX_RESPONSE_BYTES_SIZE = 1024 * 1024 * 16;

}
