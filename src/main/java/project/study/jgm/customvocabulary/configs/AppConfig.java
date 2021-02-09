package project.study.jgm.customvocabulary.configs;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.study.jgm.customvocabulary.members.Gender;
import project.study.jgm.customvocabulary.members.MemberService;
import project.study.jgm.customvocabulary.members.dto.MemberCreateDto;

import javax.persistence.EntityManager;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final EntityManager entityManager;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return new ApplicationRunner() {

            @Autowired
            MemberService memberService;

            @Override
            public void run(ApplicationArguments args) throws Exception {
                MemberCreateDto user = MemberCreateDto.builder()
                        .joinId("user")
                        .email("user@email.com")
                        .password("user")
                        .name("홍길동")
                        .nickname("userHong")
                        .dateOfBirth(LocalDate.of(1997, 1, 1))
                        .gender(Gender.MALE)
                        .simpleAddress("인천 서구")
                        .build();

                memberService.userJoin(user);

                MemberCreateDto admin = MemberCreateDto.builder()
                        .joinId("admin")
                        .email("admin@email.com")
                        .password("admin")
                        .name("admin")
                        .nickname("admin")
                        .gender(Gender.MALE)
                        .build();

                memberService.adminJoin(admin);
            }
        };
    }
}
