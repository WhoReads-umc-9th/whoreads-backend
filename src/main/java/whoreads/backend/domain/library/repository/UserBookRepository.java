package whoreads.backend.domain.library.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import whoreads.backend.domain.library.entity.UserBook;
import whoreads.backend.domain.library.enums.ReadingStatus;

import java.util.List;
import java.util.Optional;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {

    Optional<UserBook> findByMemberIdAndBookId(Long memberId, Long bookId);

    int countByMemberIdAndReadingStatus(Long memberId, ReadingStatus readingStatus);

    @Query("SELECT ub FROM UserBook ub JOIN FETCH ub.book " +
            "WHERE ub.member.id = :memberId AND ub.readingStatus = :status " +
            "AND (:cursor IS NULL OR ub.id < :cursor) " +
            "ORDER BY ub.id DESC")
    List<UserBook> findByMemberIdAndStatusWithCursor(
            @Param("memberId") Long memberId,
            @Param("status") ReadingStatus status,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
