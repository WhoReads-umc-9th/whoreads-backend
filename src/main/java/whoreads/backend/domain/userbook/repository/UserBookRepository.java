package whoreads.backend.domain.userbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.userbook.entity.UserBook;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {
}
