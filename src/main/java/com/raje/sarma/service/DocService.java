package com.raje.sarma.service;

import com.raje.sarma.dto.DocRequest;
import com.raje.sarma.dto.DocResponse;
import java.io.IOException;

public interface DocService {

  byte[] retrieveDocument(Long documentId);

  DocResponse saveDocument(DocRequest docRequest) throws IOException;
}
