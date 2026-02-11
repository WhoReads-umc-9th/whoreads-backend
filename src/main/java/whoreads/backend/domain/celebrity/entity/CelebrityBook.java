package whoreads.backend.domain.celebrity.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CelebrityBook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "celebrity_id")
    private Celebrity celebrity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;
}
