    package whoreads.backend.domain.dna.service;

    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import whoreads.backend.domain.celebrity.entity.Celebrity;
    import whoreads.backend.domain.celebrity.entity.CelebrityBook;
    import whoreads.backend.domain.celebrity.repository.CelebrityBookRepository;
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
    import whoreads.backend.domain.dna.repository.DnaResultRepository;
    import whoreads.backend.domain.member.entity.Member;
    import whoreads.backend.domain.member.repository.MemberRepository;
    import whoreads.backend.global.exception.CustomException;
    import whoreads.backend.global.exception.ErrorCode;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    @Transactional(readOnly = true)
    public class DnaService {

        private final CelebrityBookRepository celebrityBookRepository;

        private final DnaQuestionRepository dnaQuestionRepository;
        private final DnaOptionRepository dnaOptionRepository;
        private final DnaResultRepository dnaResultRepository;
        private final CelebrityRepository celebrityRepository;
        private final MemberRepository memberRepository;

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

        public DnaResDto.Result getTestResult(Long memberId) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            if (member.getDnaType() == null)
                throw new CustomException(ErrorCode.DNA_TEST_NOT_COMPLETED);

            TrackCode trackCode = TrackCode.valueOf(member.getDnaType());
            String celebrityName = member.getDnaTypeName();

            Celebrity celebrity = celebrityRepository.findByName(celebrityName)
                    .orElseThrow(() -> new CustomException(ErrorCode.DNA_TEST_NOT_FOUND_RESULT_CELEBRITY));

            String finalCommentary = getCommentary(celebrity.getId(), trackCode);

            return DnaConverter.toResultDto(celebrity, trackCode, finalCommentary);
        }

        /**
         * [최종 목적지] 독서 DNA 테스트 제출 및 결과 반환
         */
        @Transactional
        public DnaResDto.Result submitTest(DnaReqDto.Submit request, Long memberId) {
            // 1. 실시간으로 상위 5명 Pool 추출 (아래 메서드 참고)
            List<Long> poolIds = getTargetGenresForTrack(request.trackCode());

            // DB에서 해당 5명의 인물 정보 조회
            List<Celebrity> pool = celebrityRepository.findAllById(poolIds);

            // 2. [핵심] 인물들의 책 데이터를 리포지토리에서 직접 긁어오기
            List<CelebrityBook> allBooks = celebrityBookRepository.findAllByCelebrityIdsWithBook(poolIds);

            // 3. 긁어온 책들을 인물 ID별로 그룹화 (Map으로 정리)
            Map<Long, List<CelebrityBook>> booksByCelebrity = allBooks.stream()
                    .collect(Collectors.groupingBy(cb -> cb.getCelebrity().getId()));

            // 2. 유저 답변(Q2~Q5) 장르 점수 합산
            Map<GenreCode, Integer> userScores = calculateMemberGenreScores(request.selectedOptionIds());

            // [중요 로그] 사용자의 점수가 어떻게 나왔는지 확인
            userScores.forEach((genre, score) ->
                    System.out.println("사용자 점수 -> 장르: " + genre + ", 점수: " + score));

            // 3. Pool 5명 중 최종 승자(1명) 계산
            Celebrity winner = null;
            double maxScore = -1.0;

            for (Celebrity celebrity : pool) {
                // 인물 객체의 리스트가 아닌, 우리가 직접 조회한 Map에서 리스트를 꺼냅니다.
                List<CelebrityBook> books = booksByCelebrity.getOrDefault(celebrity.getId(), new ArrayList<>());

                // 로그로 확인 (이제 절대 0이 나올 수 없습니다)
                System.out.println("ID: " + celebrity.getId() + " | 이름: " + celebrity.getName() + " | 실제 책 권수: " + books.size());

                double currentScore = calculateFitScore(books, userScores);

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

            // 결과값을 DB의 Member 엔티티에 저장
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            member.setDnaType(request.trackCode().name()); // 트랙 코드 저장 (COMFORT)
            member.setDnaTypeName(winner.getName());       // 인물 이름 저장

            // 하드코딩된 RESULT_COMMENTS 맵에서 이 인물+트랙에 맞는 문구 추출
            String finalCommentary = getCommentary(winner.getId(), request.trackCode());

            return DnaConverter.toResultDto(winner, request.trackCode(), finalCommentary);
        }

        /**
         * 트랙별 중점 장르 매핑 (DB의 String 장르값과 정확히 일치해야 함)
         */
        private List<Long> getTargetGenresForTrack(TrackCode trackCode) {
            return switch (trackCode) {
                case COMFORT -> List.of(37L, 27L, 33L, 70L, 35L);
                case HABIT -> List.of(73L, 104L, 102L, 7L, 95L);
                case CAREER -> List.of(59L, 97L, 102L, 26L, 6L);
                case INSIGHT -> List.of(59L, 17L, 97L, 26L, 4L);
                case FOCUS -> List.of(33L, 13L, 2L, 25L, 8L);
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
                    if (cb.getBook().getGenre().trim().equals(code.getDescription().trim())) {
                        count++;
                    }
                }
                // 여기에 로그 삽입!
                System.out.println(code.getDescription() + " | 일치 개수: " + count + " (전체: " + totalCount + ")");

                ratios.put(code, totalCount > 0 ? (double) count / totalCount : 0.0);
            }
            return ratios;
        }

        /**
         * 최종 적합도 점수 합산 (Σ 유저점수 * 인물비율)
         */
        private double calculateFitScore(List<CelebrityBook> books, Map<GenreCode, Integer> userScores) {
            double totalFitScore = 0.0;
//            Map<GenreCode, Double> celebRatios = calculateCelebrityGenreRatio(celebrity.getCelebrityBookList());
            Map<GenreCode, Double> celebRatios = calculateCelebrityGenreRatio(books);

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

        private static final Map<String, String> RESULT_COMMENTS = new HashMap<>() {{
            // === 1. COMFORT (마음 정리 / 위로) ===
            put("37_COMFORT", """
            바쁜 일상 속에서도 혼자만의 독서 시간을 통해 마음과 생각을 정리해 온 인물이에요.
            지금의 당신처럼, 감정을 서두르지 않고 나 자신에게 집중하는 독서가 필요할 때 이 인물의 추천 책을 읽어보세요.""");
            put("27_COMFORT", """
            서사와 인물의 밀도를 높이기 위해 문학에서 끊임없이 영감을 받아온 영화 감독이에요.
            지금의 당신처럼, 이야기 속 인물과 마음을 깊이 들여다보며 나 자신의 마음을 알아가고자 하는 독서가 필요할 때 이 인물의 추천 책을 읽어보세요.""");
            put("33_COMFORT", """
            이야기 구조와 언어 감각을 다져온 소설가로, 다양한 문학과 논픽션을 통해 사유와 상상의 폭을 넓혀온 인물이에요.
            지금의 당신처럼, 감정을 설명하려 하기보다 이야기 속에 잠시 머물며 마음을 정리하고 싶을 때 이 인물의 추천 책을 읽어보세요.""");
            put("70_COMFORT", """
            음악과 감정을 더 섬세하게 표현하기 위해 책과 영화, 문학을 통해 자신의 감각을 꾸준히 다듬어 온 인물이에요.
            지금의 당신처럼, 감정을 바로 말로 정리하기보다 천천히 느끼고 표현을 가다듬는 시간이 필요할 때 이 인물의 추천 책을 읽어보세요.""");
            put("35_COMFORT", """
            문학을 통해 일상의 감정과 생각을 차분히 정리해 온 창작자예요. 책방을 운영하며, 읽는 시간 자체를 사유의 과정으로 만들어 온 인물이기도 해요.
            지금의 당신처럼, 마음을 서둘러 결론 내리기보다 일상의 언어로 천천히 정리하고 싶을 때 이 인물의 추천 책을 읽어 보세요.""");

            // === 2. HABIT (실행력 / 습관) ===
            put("73_HABIT", """
            잠재력을 실현하는 행동과 사고의 원리를 탐구해 온 동기부여 연설가이자 자기계발 저자예요. 심리·성공·자기 혁신에 관한 독서를 통해 변화를 실행으로 옮기는 방법을 꾸준히 다뤄왔어요.
            지금의 당신처럼, 결심을 행동으로 바꾸고 지속 가능한 습관을 만들고 싶을 때 이 인물의 추천 책을 읽어보세요.""");
            put("104_HABIT", """
            명상과 자기 규율을 위해 철학적 독서를 일상의 루틴처럼 실천해 온 인물이에요. 생각을 다듬는 시간을 통해 삶의 리듬과 행동의 기준을 만들어 온 방식이 특징이에요.
            지금의 당신처럼, 의욕보다 먼저 지속할 수 있는 습관과 구조가 필요할 때 이 인물의 추천 책을 읽어보세요.""");
            put("102_HABIT", """
            기술과 사회 변화의 흐름을 이해하기 위해 비즈니스와 산업 관련 독서를 이어온 배우이자 투자자예요. 새로운 흐름을 빠르게 읽고 판단을 행동으로 옮기는 기준을 책에서 다져온 인물이에요.
            지금의 당신처럼, 막연한 동기보다 현실적인 판단과 실행의 근거가 필요할 때 이 인물의 추천 책을 읽어보세요.""");
            put("7_HABIT", """
            성과 중심의 성공관을 다시 정의하며, 일과 삶의 균형을 탐구해 온 미디어 기업가이자 작가예요. 번아웃을 계기로 지속 가능한 실행과 회복의 리듬을 독서를 통해 정리해 온 인물이에요.
            지금의 당신처럼, 더 열심히 하기보다 오래 지속할 수 있는 방식의 실행이 필요할 때 이 인물의 추천 책을 읽어보세요.""");
            put("95_HABIT", """
            사회 문제를 구조적으로 이해하고 실질적인 변화를 만들기 위해 폭넓은 비즈니스·사회 분야 독서를 이어온 인물이에요. 책을 통해 가치와 실행을 연결하는 기준을 다져왔어요.
            지금의 당신처럼, 의미 있는 목표를 꾸준한 행동으로 옮기고 싶을 때 이 인물의 추천 책을 읽어보세요.""");

            // === 3. CAREER (커리어 / 시야 넓히기) ===
            put("59_CAREER", """
            유머와 풍자를 통해 사회와 개인의 경계를 끊임없이 질문해 온 코미디언이자 작가예요. 자신의 경험을 출발점으로 권력, 차별, 책임 같은 주제를 정면으로 다뤄온 독서와 사유가 특징이에요.
            지금의 당신처럼, 하나의 정답보다 세상과 커리어를 여러 각도에서 바라보고 싶을 때 이 인물의 추천 책을 읽어보세요.""");
            put("97_CAREER", """
            사회 구조와 권력, 개인의 선택을 끊임없이 질문해 온 코미디언이자 작가예요. 정치·철학·자기 성찰을 넘나드는 독서를 통해 기존의 성공과 커리어 관념을 다시 생각하게 만드는 시선을 보여줘요.
            지금의 당신처럼, 주어진 틀 안에서 답을 찾기보다 다른 질문을 통해 나만의 방향을 고민하고 싶을 때 이 인물의 추천 책을 읽어보세요.""");
            put("102_CAREER", """
            기술과 산업, 사회 변화의 흐름을 이해하기 위해 비즈니스와 혁신 관련 독서를 이어온 배우이자 투자자예요. 단기 성과보다 세상이 어디로 향하는지 읽는 시선을 통해 커리어의 선택지를 넓혀온 인물이에요.
            지금의 당신처럼, 하나의 직무보다 더 큰 맥락 속에서 커리어를 바라보고 싶을 때 이 인물의 추천 책을 읽어보세요.""");
            put("26_CAREER", """
            학습과 사고의 구조를 정리하며, 개인의 선택을 사회와 시대의 맥락 속에서 바라보는 대한민국의 스타 강사예요. 지식을 쌓는 데서 그치지 않고 어떤 기준으로 생각하고 판단할지를 책을 통해 다뤄온 인물이에요.
            지금의 당신처럼, 눈앞의 목표를 넘어서 스스로의 방향과 기준을 다시 세우고 싶을 때 이 인물의 추천 책을 읽어보세요.""");
            put("6_CAREER", """
            다양한 분야의 사람들과 대화를 이어오며, 과학·사회·문화 전반을 폭넓게 탐구해 온 인물이에요. 정답을 제시하기보다 서로 다른 관점을 연결하며 생각의 범위를 넓혀온 독서가 특징이에요.
            지금의 당신처럼, 하나의 길에 갇히기보다 여러 시선을 통해 커리어의 가능성을 탐색하고 싶을 때 이 인물의 추천 책을 읽어보세요.""");

            // === 4. INSIGHT (사고 확장 / 관점) ===
            put("59_INSIGHT", """
            유머와 풍자를 통해 당연하게 여겨온 전제와 권력을 끊임없이 의심해 온 작가이자 코미디언이에요. 개인의 경험을 출발점으로 사회적 이슈를 다른 각도에서 해석하는 독서와 사유를 이어온 인물이에요.
            지금의 당신처럼, 정답을 찾기보다 생각의 프레임을 바꾸는 질문이 필요할 때 이 인물의 추천 책을 읽어보세요.""");
            put("17_INSIGHT", """
            진화생물학자의 시선으로 인간 사회와 자연의 관계를 넓게 바라봐 온 인물이에요. 과학을 출발점으로 당연하게 여겨온 생각과 제도를 다시 보게 만드는 독서를 이어왔어요.
            지금의 당신처럼, 익숙한 판단을 잠시 내려놓고 다른 기준으로 세상을 해석해 보고 싶을 때 이 인물의 추천 책을 읽어보세요.""");
            put("97_INSIGHT", """
            개인의 경험과 사회 구조를 연결하며 권력, 성공, 자유에 대한 전제를 끊임없이 질문해 온 인물이에요. 철학과 정치, 자기 성찰을 넘나드는 독서를 통해 익숙한 담론을 다른 각도에서 바라보는 시선을 키워왔어요.
            지금의 당신처럼, 정답에 기대기보다 의심하고 다시 질문하는 관점이 필요할 때 이 인물의 추천 책을 읽어보세요.""");
            put("26_INSIGHT", """
            지식과 사고의 구조를 정리하며, 개인의 선택을 사회와 시대의 맥락 속에서 바라보게 하는 대한민국의 스타 강사예요. 단편적인 정보보다 생각하는 기준을 세우는 독서를 통해 관점을 확장해 온 인물이에요.
            지금의 당신처럼, 익숙한 판단을 넘어서 스스로 사고의 틀을 점검하고 싶을 때 이 인물의 추천 책을 읽어보세요.""");
            put("4_INSIGHT", """
            우주와 과학을 출발점으로 인간의 인식과 사고를 넓혀 온 천체물리학자이자 과학 커뮤니케이터예요. 복잡한 개념을 일상의 언어로 풀어내며 세상을 바라보는 기준 자체를 다시 생각하게 만드는 독서를 이어온 인물이에요.
            지금의 당신처럼, 익숙한 세계를 넘어 더 넓은 관점에서 질문하고 싶을 때 이 인물의 추천 책을 읽어보세요.""");

            // === 5. FOCUS (재미 / 몰입) ===
            put("33_FOCUS", """
            이야기의 힘으로 독자를 단숨에 끌어들이는 소설가예요. 장르를 넘나드는 서사와 생생한 인물 묘사를 통해 읽는 순간에 깊이 몰입하게 만드는 독서 경험을 꾸준히 만들어 온 인물이에요.
            지금의 당신처럼, 의미를 따지기보다 이야기에 빠져드는 즐거움을 온전히 느끼고 싶을 때 이 인물의 추천 책을 읽어보세요.""");
            put("13_FOCUS", """
            강한 서사와 매력적인 인물을 중심으로 이야기의 몰입도를 중요하게 여겨 온 배우이자 프로듀서예요. 읽는 순간 바로 빠져들 수 있는 작품들을 꾸준히 선택하며 재미와 완성도를 모두 갖춘 독서 경험을 만들어 온 인물이에요.
            지금의 당신처럼, 부담 없이 책을 펼치고 끝까지 읽고 싶어지는 재미를 느끼고 싶을 때 이 인물의 추천 책을 읽어보세요.""");
            put("2_FOCUS", """
            현실과 환상을 자연스럽게 넘나들며 이야기 그 자체의 힘을 믿어온 작가예요. 신화와 판타지를 바탕으로 한 서사를 통해 상상 속 세계에 깊이 빠져들게 만드는 독서 경험을 만들어 온 인물이에요.
            지금의 당신처럼, 생각을 잠시 내려두고 이야기의 흐름에 몸을 맡기고 싶을 때 이 인물의 추천 책을 읽어보세요.""");
            put("25_FOCUS", """
            이야기의 힘으로 사람들의 마음을 사로잡아 온 미디어 아이콘이에요. 강한 서사와 공감 가는 인물을 중심으로 한 번 펼치면 끝까지 읽게 만드는 책들을 꾸준히 선택해 온 인물이에요.
            지금의 당신처럼, 의미를 해석하기보다 읽는 즐거움에 온전히 빠지고 싶을 때 이 인물의 추천 책을 읽어보세요.""");
            put("8_FOCUS", """
            이야기의 전개와 캐릭터에 몰입하는 즐거움을 중요하게 여기는 대한민국의 유튜버예요. 가볍게 시작해도 끝까지 읽게 만드는 서사를 중심으로 재미에 충실한 독서 경험을 꾸준히 즐겨온 인물이에요.
            지금의 당신처럼, 복잡한 해석 없이 순수하게 읽는 재미에 빠지고 싶을 때 이 인물의 추천 책을 읽어보세요.""");
        }};

        // 2. 문구 가져오는 헬퍼 메서드
        private String getCommentary(Long celebrityId, TrackCode trackCode) {
            String key = celebrityId + "_" + trackCode.name();
            // 만약 지도에 없으면 "기본 문구"를 반환하도록 설정
            return RESULT_COMMENTS.getOrDefault(key, "WhoReads가 추천하는 인물입니다. 인물의 추천 도서를 통해 새로운 영감을 얻어보세요.");
        }
    }
