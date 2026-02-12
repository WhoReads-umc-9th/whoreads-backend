package whoreads.backend.domain.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.repository.BookRepository;
import whoreads.backend.domain.celebrity.entity.CelebrityBook;
import whoreads.backend.domain.celebrity.repository.CelebrityBookRepository;
import whoreads.backend.domain.library.dto.UserBookRequest;
import whoreads.backend.domain.library.dto.UserBookResponse;
import whoreads.backend.domain.library.entity.UserBook;
import whoreads.backend.domain.library.enums.ReadingStatus;
import whoreads.backend.domain.library.repository.UserBookRepository;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.readingsession.enums.SessionStatus;
import whoreads.backend.domain.readingsession.repository.ReadingSessionRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBookServiceImpl implements UserBookService {

    private final UserBookRepository userBookRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final CelebrityBookRepository celebrityBookRepository;
    private final ReadingSessionRepository readingSessionRepository;

    @Override
    public UserBookResponse.Summary getLibrarySummary(Long memberId) {
        int completedCount = userBookRepository.countByMemberIdAndReadingStatus(memberId, ReadingStatus.COMPLETE);
        int readingCount = userBookRepository.countByMemberIdAndReadingStatus(memberId, ReadingStatus.READING);
        Long totalReadMinutes = readingSessionRepository.sumTotalMinutesByMemberIdAndStatus(memberId, SessionStatus.COMPLETED);

        return UserBookResponse.Summary.builder()
                .completedCount(completedCount)
                .readingCount(readingCount)
                .totalReadMinutes(totalReadMinutes)
                .build();
    }

    @Override
    public UserBookResponse.BookList getBookList(Long memberId, ReadingStatus status, Long cursor, Integer size) {
        // size 파라미터 방어 검증
        if (size == null || size < 1) {
            size = 10;
        } else if (size > 100) {
            size = 100;
        }

        // size + 1개를 조회하여 다음 페이지 존재 여부 확인
        List<UserBook> userBooks = userBookRepository.findByMemberIdAndStatusWithCursor(
                memberId, status, cursor, PageRequest.of(0, size + 1)
        );

        boolean hasNext = userBooks.size() > size;
        List<UserBook> pageContent = hasNext ? userBooks.subList(0, size) : userBooks;

        // 책 ID 목록으로 셀럽 정보 일괄 조회 (N+1 방지)
        List<Long> bookIds = pageContent.stream()
                .map(ub -> ub.getBook().getId())
                .toList();

        Map<Long, List<UserBookResponse.CelebritySummary>> celebrityMap = bookIds.isEmpty()
                ? Collections.emptyMap()
                : celebrityBookRepository.findByBookIdInWithCelebrity(bookIds).stream()
                .collect(Collectors.groupingBy(
                        cb -> cb.getBook().getId(),
                        Collectors.mapping(
                                cb -> UserBookResponse.CelebritySummary.builder()
                                        .id(cb.getCelebrity().getId())
                                        .profileUrl(cb.getCelebrity().getImageUrl())
                                        .build(),
                                Collectors.toList()
                        )
                ));

        List<UserBookResponse.SimpleBook> books = pageContent.stream()
                .map(ub -> {
                    List<UserBookResponse.CelebritySummary> celebrities =
                            celebrityMap.getOrDefault(ub.getBook().getId(), Collections.emptyList());
                    return UserBookResponse.SimpleBook.from(ub, celebrities);
                })
                .toList();

        Long nextCursor = hasNext ? pageContent.get(pageContent.size() - 1).getId() : null;

        return UserBookResponse.BookList.builder()
                .books(books)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Override
    @Transactional
    public UserBookResponse.AddResult addBookToLibrary(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        // 중복 체크
        userBookRepository.findByMemberIdAndBookId(memberId, bookId)
                .ifPresent(ub -> {
                    throw new CustomException(ErrorCode.DUPLICATE_USER_BOOK);
                });

        UserBook userBook = UserBook.builder()
                .readingStatus(ReadingStatus.WISH)
                .member(member)
                .book(book)
                .build();

        userBookRepository.save(userBook);

        return UserBookResponse.AddResult.builder()
                .userBookId(userBook.getId())
                .build();
    }

    @Override
    @Transactional
    public void updateUserBook(Long memberId, Long userBookId, UserBookRequest.UpdateStatus request) {
        UserBook userBook = findByIdAndValidateOwnership(userBookId, memberId);

        // READING이 아닌데 readingPage를 보내면 에러
        if (request.getReadingStatus() != ReadingStatus.READING && request.getReadingPage() != null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "READING 상태인 책만 reading_page를 변경할 수 있습니다.");
        }

        // readingPage 유효성 검증
        if (request.getReadingPage() != null) {
            if (request.getReadingPage() <= 0) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "읽은 페이지는 1 이상이어야 합니다.");
            }
            if (userBook.getBook().getTotalPage() != null
                    && request.getReadingPage() > userBook.getBook().getTotalPage()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "읽은 페이지가 전체 페이지 수를 초과할 수 없습니다.");
            }
        }

        ReadingStatus oldStatus = userBook.getReadingStatus();
        ReadingStatus newStatus = request.getReadingStatus();

        // 상태 변경
        userBook.updateReadingStatus(newStatus);

        // READING으로 전환 시 startedAt 설정 (기존 값이 없을 때만, 재시작 시 보존)
        if (newStatus == ReadingStatus.READING && userBook.getStartedAt() == null) {
            userBook.updateStartedAt(LocalDate.now());
        }

        // COMPLETE로 전환 시
        if (newStatus == ReadingStatus.COMPLETE && oldStatus != ReadingStatus.COMPLETE) {
            // WISH → COMPLETE: startedAt이 없으면 설정
            if (userBook.getStartedAt() == null) {
                userBook.updateStartedAt(LocalDate.now());
            }
            userBook.updateCompletedAt(LocalDate.now());
        }

        // readingPage 업데이트 (READING 상태이고 값이 있을 때만)
        if (request.getReadingPage() != null) {
            userBook.updateReadingPage(request.getReadingPage());
        }
    }

    @Override
    @Transactional
    public void deleteBookFromLibrary(Long memberId, Long userBookId) {
        UserBook userBook = findByIdAndValidateOwnership(userBookId, memberId);
        userBookRepository.delete(userBook);
    }

    private UserBook findByIdAndValidateOwnership(Long userBookId, Long memberId) {
        UserBook userBook = userBookRepository.findById(userBookId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_BOOK_NOT_FOUND));

        if (!userBook.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return userBook;
    }
}
