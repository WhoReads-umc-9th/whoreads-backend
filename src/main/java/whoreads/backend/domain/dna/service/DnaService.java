package whoreads.backend.domain.dna.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityBook;
import whoreads.backend.domain.celebrity.repository.CelebrityRepository;
import whoreads.backend.domain.dna.converter.DnaConverter;
import whoreads.backend.domain.dna.dto.DnaReqDto;
import whoreads.backend.domain.dna.dto.DnaResDto;
import whoreads.backend.domain.dna.entity.DnaOption;
import whoreads.backend.domain.dna.entity.DnaQuestion;
import whoreads.backend.domain.dna.enums.GenreCode;
import whoreads.backend.domain.dna.enums.TrackCode;
import whoreads.backend.domain.dna.repository.DnaOptionRepository;
import whoreads.backend.domain.dna.repository.DnaQuestionRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DnaService {

    private final DnaQuestionRepository dnaQuestionRepository;
    private final DnaOptionRepository dnaOptionRepository;
    private final CelebrityRepository celebrityRepository;
//    private final CelebrityRepository celebrityRepository;
//    private final DnaResultRepository resultRepository;

    public DnaResDto.Question getRootQuestion() {
        // Q1 질문
        DnaQuestion rootQuestion = dnaQuestionRepository.findByStep(1)
                .orElseThrow(() -> new CustomException(ErrorCode.DNA_TEST_NOT_FOUND));

        // Q1 보기
        List<DnaOption> options =  dnaOptionRepository.findByQuestion(rootQuestion);

        // Q1 질문 및 보기 반환
        return DnaConverter.toQuestionDto(rootQuestion, options);
    }

    public DnaResDto.TrackQuestion getTrackQuestions(TrackCode trackCode) {
        List<DnaResDto.Question> questionsDtos = new ArrayList<>();

        // Q2~Q5 질문 가져오기
        List<DnaQuestion> questions = dnaQuestionRepository.findQuestionsByTrackAndStepRange(trackCode, 2, 5);

        for (DnaQuestion question: questions) {
            List<DnaOption> options = dnaOptionRepository.findByQuestion(question);

            DnaResDto.Question dto = DnaConverter.toQuestionDto(question, options);

            questionsDtos.add(dto);
        }

        return new DnaResDto.TrackQuestion(trackCode, questionsDtos);
    }

    /**
     * [최종 목적지] 독서 DNA 테스트 제출 및 결과 반환
     */
    public DnaResDto.Result submitTest(DnaReqDto.Submit request) {
        // 1. 실시간으로 상위 5명 Pool 추출 (아래 메서드 참고)
        List<Celebrity> pool = getTop5CelebritiesOnTheFly(request.trackCode());

        // 2. 유저 답변(Q2~Q5) 장르 점수 합산
        Map<GenreCode, Integer> userScores = calculateMemberGenreScores(request.selectedOptionIds());

        // 3. Pool 5명 중 최종 승자(1명) 계산
        Celebrity winner = null;
        double maxScore = -1.0;

        for (Celebrity celebrity : pool) {
            double currentScore = calculateFitScore(celebrity, userScores);

            if (currentScore > maxScore) {
                maxScore = currentScore;
                winner = celebrity;
            } else if (currentScore == maxScore) {
                // 적합도 점수가 같으면 책을 더 많이 추천한 사람 우선
                if (winner == null || celebrity.getCelebrityBookList().size() > winner.getCelebrityBookList().size()) {
                    winner = celebrity;
                }
            }
        }

        if (winner == null)
            throw new RuntimeException("매칭된 인물이 없습니다.");

        // 4. 단 한 명의 결과를 DTO로 변환하여 반환
        return DnaConverter.toResultDto(winner, request.trackCode(), winner.getShortBio());
    }

    /**
     * [핵심 로직] DB 컬럼 없이 매핑 테이블로 상위 5명 뽑기
     */
    public List<Celebrity> getTop5CelebritiesOnTheFly(TrackCode trackCode) {
        List<Celebrity> allCelebrities = celebrityRepository.findAll();

        // 랭킹 정보를 담을 리스트 (여기에 CelebrityRankDto가 들어갑니다)
        List<CelebrityRankDto> rankingList = new ArrayList<>();

        // 트랙별 타겟 장르 이름 리스트 (예: "문학", "에세이·회고")
        List<String> targetGenreNames = getTargetGenresForTrack(trackCode);

        for (Celebrity celebrity : allCelebrities) {
            List<CelebrityBook> books = celebrity.getCelebrityBookList();
            int totalCount = books.size();

            // 최소 권수 필터 (FOCUS만 10권, 나머지 5권)
            int minCount = (trackCode == TrackCode.FOCUS) ? 10 : 5;
            if (totalCount < minCount)
                continue;

            // 해당 트랙 장르 권수 카운트
            int targetCount = 0;
            for (CelebrityBook celebrityBook : books) {
                if (targetGenreNames.contains(celebrityBook.getBook().getGenre())) {
                    targetCount++;
                }
            }

            double ratio = (double) targetCount / totalCount;

            // CelebrityRankDto를 사용해서 인물과 점수를 묶어둠
            rankingList.add(new CelebrityRankDto(celebrity, ratio));
        }

        // 랭킹 리스트를 비율(ratio) 기준으로 내림차순 정렬
        rankingList.sort((o1, o2) -> Double.compare(o2.ratio(), o1.ratio()));

        // 정렬된 결과에서 상위 5명만 Celebrity 객체로 추출
        List<Celebrity> top5 = new ArrayList<>();
        for (int i = 0; i < Math.min(5, rankingList.size()); i++) {
            top5.add(rankingList.get(i).celebrity());
        }

        return top5;
    }

    /**
     * 트랙별 중점 장르 매핑 (DB의 String 장르값과 정확히 일치해야 함)
     */
    private List<String> getTargetGenresForTrack(TrackCode trackCode) {
        return switch (trackCode) {
            case COMFORT -> List.of("문학", "에세이·회고");
            case HABIT -> List.of("자기계발·심리", "경제·경영·커리어");
            case CAREER -> List.of("경제·경영·커리어", "인문·철학", "사회·역사");
            case INSIGHT -> List.of("인문·철학", "사회·역사", "과학·기술");
            case FOCUS -> List.of("재미·몰입");
        };
    }

    // 인물별 실제 장르 추천 비율 계산
    private Map<GenreCode, Double> calculateCelebrityGenreRatio(List<CelebrityBook> books) {
        Map<GenreCode, Double> ratios = new HashMap<>();
        int totalCount = books.size();

        for (GenreCode code : GenreCode.values()) {
            int count = 0;
            for (CelebrityBook cb : books) {
                // Book의 String 장르와 GenreCode의 description(한글) 비교
                if (cb.getBook().getGenre().equals(code.getDescription())) {
                    count++;
                }
            }
            ratios.put(code, totalCount > 0 ? (double) count / totalCount : 0.0);
        }
        return ratios;
    }

    /**
     * 최종 적합도 점수 합산 (Σ 유저점수 * 인물비율)
     */
    private double calculateFitScore(Celebrity celebrity, Map<GenreCode, Integer> userScores) {
        double totalFitScore = 0.0;
        Map<GenreCode, Double> celebRatios = calculateCelebrityGenreRatio(celebrity.getCelebrityBookList());

        for (GenreCode code : GenreCode.values()) {
            totalFitScore += userScores.get(code) * celebRatios.get(code);
        }
        return totalFitScore;
    }

    // 사용자 점수 계산
    public Map<GenreCode, Integer> calculateMemberGenreScores(List<Long> id) {
        Map<GenreCode, Integer> scores = new HashMap<>();
        for (GenreCode code: GenreCode.values())
            scores.put(code, 0);

        List<DnaOption> options = dnaOptionRepository.findAllById(id);
        for (DnaOption option: options) {
            GenreCode targetGenre = option.getGenre();
            int points = option.getScore();
            scores.put(targetGenre, scores.get(targetGenre) + points);
        }
        return scores;
    }

    // 임시 랭킹 저장용 내부 클래스 (레코드는 getter 없이 .ratio()로 바로 접근 가능)
    private record CelebrityRankDto(Celebrity celebrity, double ratio) {}
}
