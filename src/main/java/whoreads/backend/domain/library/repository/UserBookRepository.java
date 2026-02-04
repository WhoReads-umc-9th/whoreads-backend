package whoreads.backend.domain.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.library.entity.UserBook;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {
}
