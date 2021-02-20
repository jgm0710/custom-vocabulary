package project.study.jgm.customvocabulary.vocabulary.word.upload;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WordImageFileRepository extends JpaRepository<WordImageFile, Long> {

    Optional<WordImageFile> findByFileName(String fileName);
}
