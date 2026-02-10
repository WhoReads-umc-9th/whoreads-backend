package whoreads.backend.domain.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.library.entity.UserBook;

import java.util.Optional;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {

    Optional<UserBook> findByMemberIdAndBookId(Long memberId, Long bookId);
}
