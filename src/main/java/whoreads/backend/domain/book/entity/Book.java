package whoreads.backend.domain.book.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "book")
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

    // 책을 조회하면, 이 책에 달린 인용들도 같이 가져올 수 있도록 연결
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookQuote> quotes = new ArrayList<>();

    @Builder
    public Book(String title, String authorName, String link, String genre, String coverUrl) {
        this.title = title;
        this.authorName = authorName;
        this.link = link;
        this.genre = genre;
        this.coverUrl = coverUrl;
    }
}