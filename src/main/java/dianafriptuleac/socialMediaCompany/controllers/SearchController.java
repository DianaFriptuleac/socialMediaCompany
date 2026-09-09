package dianafriptuleac.socialMediaCompany.controllers;

import dianafriptuleac.socialMediaCompany.payloads.search.SearchResponseDTO;
import dianafriptuleac.socialMediaCompany.services.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public SearchResponseDTO search(
            @RequestParam("q") String query, Pageable pageable
    ) {
        return searchService.search(query, pageable);
    }
}
