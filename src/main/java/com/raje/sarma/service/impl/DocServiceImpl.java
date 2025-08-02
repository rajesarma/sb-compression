package com.raje.sarma.service.impl;

import com.raje.sarma.constants.Constants;
import com.raje.sarma.dto.DocRequest;
import com.raje.sarma.dto.DocResponse;
import com.raje.sarma.model.DocData;
import com.raje.sarma.repository.DocDataRepository;
import com.raje.sarma.service.DocService;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocServiceImpl implements DocService {
  private final DocDataRepository docDataRepository;

  @Override
  public byte[] retrieveDocument(Long documentId) {
    try {
      Optional<DocData> docData = docDataRepository.findByIdAndActive(documentId, true);
      return docData.map(DocData::getDocument).orElse(new byte[0]);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Transactional
  public DocResponse saveDocument(DocRequest docRequest)
      throws IOException {
    DocData existingDocData = docDataRepository.findByDocumentNameAndActive(
        docRequest.getDocumentName(), true);
    if (Objects.nonNull(existingDocData)) {
      return getDocResponse(existingDocData);
    }

    DocData docData = DocData.builder()
            .document(docRequest.getDocument().getBytes())
            .documentName(docRequest.getDocumentName())
            .active(true)
            .build();
      return getDocResponse(docData);
  }

  private DocResponse getDocResponse(DocData docData) {
    DocData savedDocData = docDataRepository.save(docData);
    return DocResponse.builder()
        .docId(savedDocData.getId())
        .docName(savedDocData.getDocumentName())
        .createdBy(savedDocData.getCreatedBy())
        .createdDate(savedDocData.getCreatedDate())
        .lastUpdatedBy(savedDocData.getLastUpdatedBy())
        .lastUpdatedDate(savedDocData.getLastUpdatedDate())
        .build();
  }
}
