package project.study.jgm.customvocabulary.vocabulary.upload;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VocabularyThumbnailImageFileRepository extends JpaRepository<VocabularyThumbnailImageFile, Long> {
    Optional<VocabularyThumbnailImageFile> findByFileName(String fileName);
}
