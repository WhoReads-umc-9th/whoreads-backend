package whoreads.backend.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "jwtAuth";

        // API 요청 시 헤더에 Authorization: Bearer {token}이 자동으로 붙도록 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // 자물쇠 버튼 클릭 시 나타날 입력창 설정
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")            // 'bearer'를 앞에 붙여줌
                        .bearerFormat("JWT"));         // 포맷은 JWT

        return new OpenAPI()
                .info(new Info()
                        .title("WhoReads API Document")
                        .description("WhoReads 서비스의 API 명세서입니다.")
                        .version("1.0.0"))
                .addServersItem(new Server().url("/").description("Staging(개발)"))
                .addServersItem(new Server().url("https://api.whoreads.kro.kr").description("Production"))
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
    }

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        return new ModelResolver(objectMapper);
    }

    @Bean
    public OpenApiCustomizer globalUnauthorizedResponse() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    if (operation.getResponses().get("401") == null) {
                        operation.getResponses().addApiResponse("401",
                                new ApiResponse()
                                        .description("인증 실패")
                                        .content(new Content().addMediaType(
                                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                                new MediaType().schema(new Schema<>()
                                                        .example("""
                                                                {
                                                                  "is_success": false,
                                                                  "code": 401,
                                                                  "message": "인증이 필요합니다."
                                                                }
                                                                """))
                                        ))
                        );
                    }
                })
        );
    }
}
