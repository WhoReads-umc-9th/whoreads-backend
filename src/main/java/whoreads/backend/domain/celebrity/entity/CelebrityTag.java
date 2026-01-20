package whoreads.backend.domain.celebrity.entity;

public enum CelebrityTag {
    ENTREPRENEUR("기업가"),
    WRITER("작가"),
    ATHLETE("운동선수"),
    MOVIE_DIRECTOR("영화감독");


    private final String description;

    CelebrityTag(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}