package whoreads.backend.domain.celebrity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import whoreads.backend.domain.celebrity.entity.CelebrityBook;

import java.util.List;

public interface CelebrityBookRepository extends JpaRepository<CelebrityBook, Long> {

    @Query("SELECT cb FROM CelebrityBook cb JOIN FETCH cb.celebrity " +
            "WHERE cb.book.id IN :bookIds")
    List<CelebrityBook> findByBookIdInWithCelebrity(@Param("bookIds") List<Long> bookIds);
}
