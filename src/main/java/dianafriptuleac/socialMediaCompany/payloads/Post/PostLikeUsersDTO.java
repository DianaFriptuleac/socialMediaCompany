package dianafriptuleac.socialMediaCompany.payloads.Post;

import dianafriptuleac.socialMediaCompany.payloads.Comment.UserPublicDTO;

import java.util.List;

public record PostLikeUsersDTO(
        long likeCount,
        List<UserPublicDTO> users
) {
}
