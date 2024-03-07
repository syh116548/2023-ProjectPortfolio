package spe.projectportfolio.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spe.projectportfolio.backend.mapper.CaseStudyMapper;
import spe.projectportfolio.backend.pojo.CaseStudy;
import spe.projectportfolio.backend.pojo.CaseStudyUpload;
import spe.projectportfolio.backend.service.CaseStudyService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class CaseStudyController {
    private final CaseStudyMapper caseStudyMapper;
    private final CaseStudyService caseStudyService;

    @GetMapping(path = "/case-studies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CaseStudy>> getAllCaseStudiesByCondition(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "client-name", required = false) String clientName,
            @RequestParam(name = "industry", required = false) String industry,
            @RequestParam(name = "search", required = false) String search)
    {
        List<CaseStudy> caseStudies;
        if (search != null && !search.isEmpty()) {
            caseStudies = caseStudyService.getCaseStudiesByGlobalSearch(search);
        } else {
            caseStudies = caseStudyService.getCaseStudiesByCondition(title, clientName, industry);
        }
        return ResponseEntity.ok(caseStudies);
    }

    @GetMapping(path = "/case-studies/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseStudy> getCaseStudyById(@PathVariable Long id) {
        CaseStudy foundCaseStudy = caseStudyService.getCaseStudyById(id);
        if (foundCaseStudy == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(foundCaseStudy);
    }

    @PostMapping(path = "/case-studies", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseStudy> createCaseStudy(@RequestBody CaseStudyUpload data) {
        CaseStudy caseStudy;
        try {
            caseStudy = caseStudyService.addCaseStudy(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(caseStudy);
    }

    @PutMapping(path = "/case-studies", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseStudy> updateCaseStudy(@RequestBody CaseStudyUpload data) {
        ResponseEntity<CaseStudy> badRequest = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        CaseStudy updatedCaseStudy;
        try {
            updatedCaseStudy = caseStudyService.updateCaseStudy(data);
            if (updatedCaseStudy == null) { // if null then the case study ID in the data could not be found, so it is bad request
                return badRequest;
            }
        } catch (Exception e) {
            return badRequest;
        }
        return ResponseEntity.ok(updatedCaseStudy);
    }

    @DeleteMapping(path = "/case-studies/{id}")
    public ResponseEntity<Void> deleteCaseStudy(@PathVariable Long id) {
        CaseStudy caseStudy = caseStudyMapper.findById(id);

        // check if case study exists
        if (caseStudy == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            caseStudyService.deleteCaseStudy(id);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
