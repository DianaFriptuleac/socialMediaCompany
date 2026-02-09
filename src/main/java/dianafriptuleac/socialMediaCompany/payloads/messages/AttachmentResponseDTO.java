package dianafriptuleac.socialMediaCompany.payloads.messages;

import java.time.Instant;
import java.util.UUID;

public record AttachmentResponseDTO(
        UUID id,
        String fileName,
        String contentType,
        long size,
        String downloadUrl,
        String publicId, Instant createdAt
) {
}
