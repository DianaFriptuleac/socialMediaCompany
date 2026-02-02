package dianafriptuleac.socialMediaCompany.payloads.Post;

import jakarta.validation.constraints.Size;

public record PostCreateDTO(
        @Size(max = 150) String title,
        @Size(max = 5000) String description,
        @Size(max = 2048) String externalUrl
) {
    public PostCreateDTO {
        title = normalize(title);
        description = normalize(description);
        externalUrl = normalize(externalUrl);
    }

    private static String normalize(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}
