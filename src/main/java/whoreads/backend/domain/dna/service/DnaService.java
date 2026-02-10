package whoreads.backend.domain.dna.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.celebrity.repository.CelebrityRepository;
import whoreads.backend.domain.dna.converter.DnaConverter;
import whoreads.backend.domain.dna.dto.DnaReqDto;
import whoreads.backend.domain.dna.dto.DnaResDto;
import whoreads.backend.domain.dna.entity.DnaOption;
import whoreads.backend.domain.dna.entity.DnaQuestion;
import whoreads.backend.domain.dna.enums.TrackCode;
import whoreads.backend.domain.dna.repository.DnaOptionRepository;
import whoreads.backend.domain.dna.repository.DnaQuestionRepository;
import whoreads.backend.domain.dna.repository.DnaResultRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DnaService {

    private final DnaQuestionRepository questionRepository;
    private final DnaOptionRepository optionRepository;
//    private final CelebrityRepository celebrityRepository;
//    private final DnaResultRepository resultRepository;

    public DnaResDto.Question getRootQuestion() {
        // Q1 질문
        DnaQuestion rootQuestion = questionRepository.findByStep(1)
                .orElseThrow(() -> new EntityNotFoundException("Q1 질문을 찾을 수 없습니다."));

        // Q1 보기
        List<DnaOption> options =  optionRepository.findByQuestion(rootQuestion);

        // Q1 질문 및 보기 반환
        return DnaConverter.toQuestionDto(rootQuestion, options);
    }

    public DnaResDto.TrackQuestion getTrackQuestions(TrackCode trackCode) {
        List<DnaResDto.Question> questionsDtos = new ArrayList<>();

        // Q2~Q5 질문 가져오기
        List<DnaQuestion> questions = questionRepository.findByTrackTrackCodeAndStepBetweenOrderByStepAsc(trackCode, 2, 5);

        for (DnaQuestion question: questions) {
            List<DnaOption> options = optionRepository.findByQuestion(question);

            DnaResDto.Question dto = DnaConverter.toQuestionDto(question, options);

            questionsDtos.add(dto);
        }

        return new DnaResDto.TrackQuestion(trackCode, questionsDtos);
    }

    public DnaResDto.Result calculateResult(DnaReqDto.Submit request) {
        return null;
    }
}
