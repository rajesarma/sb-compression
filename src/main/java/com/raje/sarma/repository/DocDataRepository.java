package com.raje.sarma.repository;

import com.raje.sarma.model.DocData;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocDataRepository extends CrudRepository<DocData, Long> {

  DocData findByDocumentNameAndActive(String documentName, Boolean active);
  Optional<DocData> findByIdAndActive(Long documentId, Boolean active);
}
