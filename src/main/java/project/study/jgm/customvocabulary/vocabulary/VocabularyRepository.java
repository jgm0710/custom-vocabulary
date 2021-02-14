package project.study.jgm.customvocabulary.vocabulary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    List<Vocabulary> findByCategoryId(Long categoryId);
}
