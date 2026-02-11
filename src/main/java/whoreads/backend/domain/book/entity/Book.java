package whoreads.backend.domain.book.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.domain.celebrity.entity.CelebrityBook;
import whoreads.backend.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "book", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"title", "author_name"}) // 제목+저자 중복 방지 (선택 사항이나 추천)
})
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "author_name", nullable = false)
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
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookQuote> quotes = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CelebrityBook> celebrityBookList = new ArrayList<>();

    @Builder
    public Book(String title, String authorName, String link, String genre, String coverUrl, Integer totalPage) {
        this.title = title;
        this.authorName = authorName;
        this.link = link;
        this.genre = genre;
        this.coverUrl = coverUrl;
        this.totalPage = totalPage;
    }
}
