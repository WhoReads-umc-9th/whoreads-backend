package whoreads.backend.domain.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.book.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}
