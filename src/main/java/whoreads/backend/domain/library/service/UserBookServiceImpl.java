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
import whoreads.backend.infra.s3.S3Service;

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
    private final S3Service s3Service;

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
                                        .profileUrl(s3Service.generateUrl(cb.getCelebrity().getImageUrl()))
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
    public void updateUserBook(Long memberId, Long userBookId, UserBookRequest.UpdateReadingInfo request) {
        UserBook userBook = findByIdAndValidateOwnership(userBookId, memberId);

        // reading_status를 보내지 않으면 현재 상태 유지 (날짜/페이지만 수정하는 경우)
        ReadingStatus newStatus = request.getReadingStatus() != null
                ? request.getReadingStatus()
                : userBook.getReadingStatus();

        validateReadingInfoRequest(request, newStatus, userBook.getBook().getTotalPage());

        // 날짜/페이지는 서버에서 자동으로 채우지 않고, 요청에 온 값만 반영한다 (프론트 재량)
        LocalDate startedAt = request.getStartedAt() != null ? request.getStartedAt() : userBook.getStartedAt();
        LocalDate completedAt = request.getCompletedAt() != null ? request.getCompletedAt() : userBook.getCompletedAt();
        boolean clearCompletedAt = false;
        if (startedAt != null && completedAt != null && startedAt.isAfter(completedAt)) {
            // 다시 읽기 시작한 경우: 시작일만 보냈고 COMPLETE가 아니면, 이전 완료일을 비우고 새 시작일을 반영한다
            if (request.getCompletedAt() == null && newStatus != ReadingStatus.COMPLETE) {
                clearCompletedAt = true;
            } else {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "시작일은 완료일보다 늦을 수 없습니다.");
            }
        }

        userBook.updateReadingStatus(newStatus);
        if (request.getStartedAt() != null) {
            userBook.updateStartedAt(request.getStartedAt());
        }
        if (request.getCompletedAt() != null) {
            userBook.updateCompletedAt(request.getCompletedAt());
        } else if (clearCompletedAt) {
            userBook.updateCompletedAt(null);
        }
        if (request.getReadingPage() != null) {
            userBook.updateReadingPage(request.getReadingPage());
        }
    }

    private void validateReadingInfoRequest(UserBookRequest.UpdateReadingInfo request, ReadingStatus newStatus, Integer totalPage) {
        // 시작일/완료일: READING, COMPLETE에서만 입력 가능
        if (newStatus == ReadingStatus.WISH && (request.getStartedAt() != null || request.getCompletedAt() != null)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "읽는 중이거나 다 읽은 책만 시작일과 완료일을 입력할 수 있습니다.");
        }
        // 읽은 페이지: READING에서만 입력 가능
        if (newStatus != ReadingStatus.READING && request.getReadingPage() != null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "읽는 중인 책만 읽은 페이지를 입력할 수 있습니다.");
        }

        LocalDate today = LocalDate.now();
        if ((request.getStartedAt() != null && request.getStartedAt().isAfter(today))
                || (request.getCompletedAt() != null && request.getCompletedAt().isAfter(today))) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "미래 날짜는 입력할 수 없습니다.");
        }

        Integer readingPage = request.getReadingPage();
        if (readingPage != null) {
            if (readingPage <= 0) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "읽은 페이지는 1 이상이어야 합니다.");
            }
            if (totalPage != null && readingPage > totalPage) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "읽은 페이지가 전체 페이지 수를 초과할 수 없습니다.");
            }
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

    // 사용자와 유명인 간의 친밀도 점수 계산
    public int calculateIntimacyScore(Long memberId, Long celebrityId) {

        List<CelebrityBook> celebBooks = celebrityBookRepository.findAllByCelebrityIdsWithBook(List.of(celebrityId));

        int totalRecommendedCount = celebBooks.size();

        List<Long> recommendedBookIds = celebBooks.stream()
                .map(celebBook -> celebBook.getBook().getId())
                .toList();

        int userReadCount = userBookRepository.countByMemberIdAndReadingStatusAndBookIdIn(
                memberId,
                ReadingStatus.COMPLETE,
                recommendedBookIds
        );

        int score = (int) (((double) userReadCount / totalRecommendedCount) * 100);

        return score;
    }
}
