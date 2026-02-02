package dianafriptuleac.socialMediaCompany.entities;


import dianafriptuleac.socialMediaCompany.enums.MediaType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "post_media")
@Getter
@Setter
@NoArgsConstructor
public class PostMedia {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type;

    @Column(nullable = false)
    private String url;

    private String name;

    public PostMedia(Post post, MediaType type, String url, String name) {
        this.post = post;
        this.type = type;
        this.url = url;
        this.name = name;
    }


    public PostMedia(Post post, MediaType type, String url) {
    }
}

