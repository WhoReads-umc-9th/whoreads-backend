package whoreads.backend.domain.celebrity.entity;

public enum CelebrityTag {

    ENTREPRENEUR("기업가"),
    WRITER("작가"),
    ATHLETE("스포츠선수"),
    MOVIE_DIRECTOR("영화감독"),
    SINGER("가수"),
    CHEF("요리사"),
    ACTOR("배우"),
    MUSICAL_ACTOR("뮤지컬 배우"),
    INSTRUCTOR("강사"),
    SCHOLAR("학자"),
    PROFILER("프로파일러"),
    LITERARY_CRITIC("문학평론가"),
    IDOL("아이돌"),
    YOUTUBER("유튜버"),
    MEDIA_CRITIC("언론비평가"),
    ANNOUNCER("아나운서"),
    TRANSLATOR("번역가"),
    COMEDIAN("개그맨"),
    LYRICIST("작사가"),
    LAWYER("변호사"),
    PHYSICIST("물리학자"),
    NOVELIST("소설가"),
    RAPPER("래퍼"),
    SPORTS_COMMENTATOR("스포츠해설가"),
    JOURNALIST("기자"),
    COACH("코치"),
    FILM_CRITIC("영화평론가"),
    BIOLOGIST("생물학자"),
    PRESIDENT("대통령"),
    POLITICIAN("정치인"),
    PROFESSOR("교수");

    private final String description;

    CelebrityTag(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}