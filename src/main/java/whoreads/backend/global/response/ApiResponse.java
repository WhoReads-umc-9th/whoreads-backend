package whoreads.backend.global.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.ZoneId;
import java.time.ZonedDateTime;


@Getter
@Builder
@JsonPropertyOrder({"is_success", "code", "message", "server_time", "result"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @JsonProperty("is_success")
    private final Boolean isSuccess;

    @JsonProperty("code")
    private final int code;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("server_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final ZonedDateTime serverTime;

    @JsonProperty("result")
    private final T result;

    public ApiResponse<T> withServerTime() {
        return ApiResponse.<T>builder()
                .isSuccess(this.isSuccess)
                .code(this.code)
                .message(this.message)
                .serverTime(ZonedDateTime.now(ZoneId.of("Asia/Seoul")))
                .result(this.result)
                .build();
    }

    public static <T> ApiResponse<T> success(T result) {
        return ApiResponse.<T>builder()
                .isSuccess(true)
                .code(200)
                .message("요청이 성공했습니다.")
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T result) {
        return ApiResponse.<T>builder()
                .isSuccess(true)
                .code(200)
                .message(message)
                .result(result)
                .build();
    }

    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .isSuccess(true)
                .code(200)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> created(T result) {
        return ApiResponse.<T>builder()
                .isSuccess(true)
                .code(201)
                .message("리소스가 생성되었습니다.")
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> created(String message, T result) {
        return ApiResponse.<T>builder()
                .isSuccess(true)
                .code(201)
                .message(message)
                .result(result)
                .build();
    }

    public static ApiResponse<Void> error(int code, String message) {
        return ApiResponse.<Void>builder()
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }
}
