package project.study.jgm.customvocabulary.members;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//@Profile(value = "test")
@ActiveProfiles("test")
class MemberTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void createTest() throws Exception {
        //given
        String name = "name";
        Member member = Member.builder()
                .name(name)
                .build();
        //when
        Member save = memberRepository.save(member);

        //then
        Assertions.assertEquals(save.getName(), name);

    }
}