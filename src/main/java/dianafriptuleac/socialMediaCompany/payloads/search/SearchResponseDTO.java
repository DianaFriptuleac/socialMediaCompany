package dianafriptuleac.socialMediaCompany.payloads.search;

import org.springframework.data.domain.Page;


public record SearchResponseDTO(
        Page<UserSearchDTO> users,
        Page<DepartmentSearchDTO> departments,
        Page<EventSearchDTO> events,
        Page<JobSearchDTO> jobs
) {
}
