package whoreads.backend.domain.dna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.dna.entity.DnaTrack;
import whoreads.backend.domain.dna.enums.TrackCode;

import java.util.Optional;

public interface DnaTrackRepository extends JpaRepository<DnaTrack, Long> {

    // 트랙 코드로 트랙 정보 조회
    Optional<DnaTrack> findByTrackCode(TrackCode trackCode);
}
