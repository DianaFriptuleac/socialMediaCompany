package dianafriptuleac.socialMediaCompany.payloads.Post;

import dianafriptuleac.socialMediaCompany.enums.MediaType;

import java.util.UUID;

public record PostMediaDTO(UUID id, MediaType type, String url) {
}
