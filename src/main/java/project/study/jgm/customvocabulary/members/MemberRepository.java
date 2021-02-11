package project.study.jgm.customvocabulary.members;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByJoinId(String joinId);

    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByJoinIdOrNickname(String joinId, String nickname);
}
