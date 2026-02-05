package dianafriptuleac.socialMediaCompany.payloads.Post;

public record PostLikeStatusDTO(
        long likeCount,
        boolean likedByMe
) {
}
