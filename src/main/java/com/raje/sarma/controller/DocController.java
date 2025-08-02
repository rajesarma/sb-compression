package com.raje.sarma.controller;

import static com.raje.sarma.constants.Constants.INTERNAL_API_ERROR;

import com.raje.sarma.constants.Constants;
import com.raje.sarma.dto.DocRequest;
import com.raje.sarma.dto.DocResponse;
import com.raje.sarma.service.DocService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/doc")

public class DocController {

  private final DocService docService;

  @Operation(summary = "saves the uploaded document",
      description = "This api will save the uploaded document")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Document Saved Successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid input parameter"),
          @ApiResponse(responseCode = "500", description = INTERNAL_API_ERROR)})
  @PostMapping
  public ResponseEntity<DocResponse> saveDocument(@ModelAttribute @Valid DocRequest docRequest)
      throws Exception {
    try {
      return new ResponseEntity<>(docService.saveDocument(docRequest), HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage(), e.getMessage());
      return new ResponseEntity<>(docService.saveDocument(docRequest), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "fetch document.",
      description = "This api will fetch the document based on document name.")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = Constants.DOCUMENT_SUCCESS),
          @ApiResponse(responseCode = "204", description = Constants.DOC_NOT_FOUND),
          @ApiResponse(responseCode = "500", description = INTERNAL_API_ERROR)})
  @GetMapping
  public ResponseEntity<?> retrieveDoc(
          @RequestParam(value = "documentId") final Long documentId) {
    byte[] document = docService.retrieveDocument(documentId);
    if (Objects.isNull(document) || document.length == 0) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(new String(document, StandardCharsets.UTF_8));
//    return ResponseEntity
//        .ok()
//        .header("Content-Type", "text/plain; charset=UTF-8")
//        .body(document);
  }

}

