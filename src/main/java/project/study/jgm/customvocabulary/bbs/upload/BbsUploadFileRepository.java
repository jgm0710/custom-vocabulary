package project.study.jgm.customvocabulary.bbs.upload;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BbsUploadFileRepository extends JpaRepository<BbsUploadFile, Long> {

    Optional<BbsUploadFile> findByFileName(String fileName);
    List<BbsUploadFile> findAllByBbsId(Long bbsId);
}
