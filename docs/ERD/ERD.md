```mermaid
erDiagram
    quote ||--o{ quote_source : "has"
    celebrity ||--o{ quote : "has"
    quote ||--o{ quote_context : "has"
    quote ||--o{ topic_quote : "has"
    topic ||--o{ topic_quote : "has"
    member ||--o{ member_celebrity : "has"
    celebrity ||--o{ member_celebrity : "has"
    member ||--o{ member_book : "has"
    member ||--o{ alarm : "has"
    member_book ||--o{ reading_session : "has"

    quote_source {
        bigint id PK 
        bigint quote_id  FK
        varchar source_url  
        enum source_type  
        VARCHAR(255) timestamp  
    }
    quote {
        bigint id PK 
        bigint celebrity_id  FK
        bigint bood_id  FK
        varchar original_text  
        int confidence_score  
        int context_score  
        enum language  
        boolean book_recommendation  
        boolean is_direct_quote  
    }
    focus_mode {
        bigint id PK 
        bigint user_id  FK
        boolean lock_enabled  
        int seeting_time  
        boolean is_white_noise_on  
        enum white_noise_type  
    }
    Book {
        bigint id PK 
        varchar title  
        text cover_url  
    }
    member {
        bigint id PK 
        varchar nickname  
        enum gender  
        enum years  
        varchar email  
        varchar password  
        datetime(6) created_at  
        datetime(6) updated_at  
        VARCHAR(255) dna_type  
        VARCHAR(255) dna_type_name  
    }
    quote_context {
        bigint id PK 
        bigint quote_id  FK
        varchar how  
        varchar when  
        varchar why  
        varchar help  
    }
    topic_quote {
        bigint id PK 
        bigint quote_id  FK
        bigint topic_id  FK
    }
    member_celebrity {
        bigint id PK 
        bigint member_id  FK
        bigint celebrity_id  FK
        datetime created_at  
        datetime updated_at  
    }
    member_book {
        bigint id PK 
        bigint member_id  FK
        bigint book_id  FK
        enum status  
        datetime(6) created_at  
        datetime(6) updated_at  
    }
    alarm {
        bigint id PK 
        bigint member_id  FK
        enum days  
        VARCHAR(255) time  
        boolean is_enabled  
    }
    reading_session {
        bigint id PK 
        bigint member_book_id  FK
        datetime started_at  
        datetime ended_at  
        int duration  
        enum ended_by  
    }
    celebrity {
        bigint id PK 
        varchar(20) name  
        varchar image_url  
        varchar short_bio  
        enum tags  
    }
    topic {
        bigint id PK 
        enum? name  
        varchar description  
        enum? tags  
    }
    Untitled2 {
        bigint id PK 
    }
```