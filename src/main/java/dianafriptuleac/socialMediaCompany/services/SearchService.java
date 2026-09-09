package dianafriptuleac.socialMediaCompany.services;

import dianafriptuleac.socialMediaCompany.exceptions.BadRequestException;
import dianafriptuleac.socialMediaCompany.payloads.search.SearchResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentMembershipService departmentMembershipService;

    @Autowired
    private EventService eventService;

    @Autowired
    private JobOpeningService jobOpeningService;

    public SearchResponseDTO search(String query, Pageable pageable) {

        if (query == null || query.isBlank()) {
            throw new BadRequestException("Search query is required");
        }
        String normalizedQuery = query.trim();

        if (normalizedQuery.length() < 2) {
            throw new BadRequestException("Search query must contain at least 2 characters");
        }
        return new SearchResponseDTO(
                userService.searchUsers(normalizedQuery, pageable),
                departmentMembershipService.serachDepartments(normalizedQuery, pageable),
                eventService.searchEvents(normalizedQuery, pageable),
                jobOpeningService.searchJobs(normalizedQuery, pageable)
        );
    }
}
