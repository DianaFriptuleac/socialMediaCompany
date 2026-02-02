package dianafriptuleac.socialMediaCompany.payloads.Post;

import jakarta.validation.constraints.Size;

public record PostUpdateDTO(
        @Size(max = 150) String title,
        String description,
        String externalUrl
) {
}
