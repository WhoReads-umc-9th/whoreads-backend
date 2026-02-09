package whoreads.backend.domain.readingsession.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockedAppItem {
    @NotBlank(message = "bundle_id는 필수입니다.")
    @Size(max = 255, message = "bundle_id는 255자를 초과할 수 없습니다.")
    private String bundleId;

    @NotBlank(message = "name은 필수입니다.")
    @Size(max = 100, message = "name은 100자를 초과할 수 없습니다.")
    private String name;
}
