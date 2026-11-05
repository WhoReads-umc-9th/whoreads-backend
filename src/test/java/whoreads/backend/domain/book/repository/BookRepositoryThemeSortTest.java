package whoreads.backend.domain.book.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.config.TestFirebaseConfig;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.library.entity.UserBook;
import whoreads.backend.domain.library.enums.ReadingStatus;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.enums.AgeGroup;
import whoreads.backend.domain.member.enums.Gender;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.entity.TopicBook;
import whoreads.backend.domain.topic.entity.TopicTag;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 주제별 도서 정렬: 서재에 담긴 횟수가 많은 순, 같으면 제목순 (H2).
 */
@SpringBootTest
@Import(TestFirebaseConfig.class)
@Transactional
class BookRepositoryThemeSortTest {

    @Autowired BookRepository bookRepository;
    @Autowired EntityManager em;

    Topic topic;

    @BeforeEach
    void setUp() {
        topic = Topic.builder().name(TopicTag.MINDSET).build();
        em.persist(topic);
    }

    @Test
    @DisplayName("서재에 담긴 책이 없으면 제목순(숫자, 영문, 한글)으로 내려준다")
    void noUserBooks_sortedByTitle() {
        book("가나다");
        book("Bizarre Romance");
        book("1984");

        assertThat(titles(Pageable.unpaged())).containsExactly("1984", "Bizarre Romance", "가나다");
    }

    @Test
    @DisplayName("서재에 담긴 책이 앞으로 오고 담긴 횟수가 많은 순으로 정렬된다")
    void userBooks_comeFirst_byCount() {
        book("1984");
        Book once = book("Bizarre Romance");
        Book twice = book("가나다");
        addToLibrary(once, ReadingStatus.WISH);
        addToLibrary(twice, ReadingStatus.WISH);
        addToLibrary(twice, ReadingStatus.WISH);

        assertThat(titles(Pageable.unpaged())).containsExactly("가나다", "Bizarre Romance", "1984");
    }

    @Test
    @DisplayName("독서 상태와 관계없이 1건으로 세고, 횟수가 같으면 제목순이다")
    void allStatuses_countAsOne_tieBrokenByTitle() {
        Book wish = book("가나다");
        Book reading = book("Bizarre Romance");
        Book complete = book("1984");
        addToLibrary(wish, ReadingStatus.WISH);
        addToLibrary(reading, ReadingStatus.READING);
        addToLibrary(complete, ReadingStatus.COMPLETE);

        assertThat(titles(Pageable.unpaged())).containsExactly("1984", "Bizarre Romance", "가나다");
    }

    @Test
    @DisplayName("여러 명이 담은 책도 한 번만 나오고 페이징은 정렬 순서를 따른다")
    void paging_followsSortOrder_withoutDuplicates() {
        book("1984");
        Book popular = book("가나다");
        addToLibrary(popular, ReadingStatus.READING);
        addToLibrary(popular, ReadingStatus.COMPLETE);

        assertThat(titles(PageRequest.of(0, 1))).containsExactly("가나다");
        assertThat(titles(PageRequest.of(1, 1))).containsExactly("1984");
    }

    private List<String> titles(Pageable pageable) {
        em.flush();
        return bookRepository.findBooksByTheme(TopicTag.MINDSET, pageable).stream()
                .map(Book::getTitle)
                .toList();
    }

    private Book book(String title) {
        Book book = Book.builder().title(title).authorName("저자").build();
        em.persist(book);
        em.persist(TopicBook.builder().topic(topic).book(book).build());
        return book;
    }

    private void addToLibrary(Book book, ReadingStatus status) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        Member member = Member.builder()
                .nickname("t" + u).gender(Gender.FEMALE).ageGroup(AgeGroup.TWENTIES)
                .email(u + "@test.com").loginId("id" + u).password("pw").build();
        em.persist(member);
        em.persist(UserBook.builder().readingStatus(status).member(member).book(book).build());
    }
}
