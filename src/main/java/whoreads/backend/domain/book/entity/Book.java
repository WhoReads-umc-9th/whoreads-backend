package whoreads.backend.domain.book.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.domain.celebrity.entity.CelebrityBook;
import whoreads.backend.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

// @Builder 삭제 (생성자에 있으므로 중복 제거)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "book", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"title", "author_name"})
})
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    @Column(columnDefinition = "TEXT")
    private String link;

    @Column(name = "genre")
    private String genre;

    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    @Column(name = "total_page")
    private Integer totalPage;

    // 책을 조회하면, 이 책에 달린 인용들도 같이 가져올 수 있도록 연결
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookQuote> quotes = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CelebrityBook> celebrityBookList = new ArrayList<>();

    @Builder // 생성자 레벨 빌더만 유지
    public Book(String title, String authorName, String link, String genre, String coverUrl, Integer totalPage) {
        this.title = title;
        this.authorName = authorName;
        this.link = link;
        this.genre = genre;
        this.coverUrl = coverUrl;
        this.totalPage = totalPage;
    }
}