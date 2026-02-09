package whoreads.backend.domain.library.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.library.enums.ReadingStatus;
import whoreads.backend.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_book")
public class UserBook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingStatus readingStatus;

    @Column(name = "reading_page")
    private Integer readingPage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Builder
    public UserBook(ReadingStatus readingStatus, Integer readingPage, Member member, Book book) {
        this.readingStatus = readingStatus;
        this.readingPage = readingPage;
        this.member = member;
        this.book = book;
    }

    public void updateReadingStatus(ReadingStatus readingStatus) {
        this.readingStatus = readingStatus;
    }

    public void updateReadingPage(Integer readingPage) {
        this.readingPage = readingPage;
    }
}
