package spe.projectportfolio.backend;

import spe.projectportfolio.backend.pojo.*;

import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PojoAssertions {
    public static void assertCaseStudyEquals(CaseStudy caseStudy1, CaseStudy caseStudy2) {
        assertEquals(caseStudy1.getCaseStudyId(),         caseStudy2.getCaseStudyId());
        assertEquals(caseStudy1.getTitle(),               caseStudy2.getTitle());
        assertEquals(caseStudy1.getProjectStatus(),       caseStudy2.getProjectStatus());
        assertEquals(caseStudy1.getEditStatus(),          caseStudy2.getEditStatus());
        assertEquals(caseStudy1.getClientName(),          caseStudy2.getClientName());
        assertEquals(caseStudy1.getClientLink(),          caseStudy2.getClientLink());
        assertEquals(caseStudy1.getClientLogoId(),        caseStudy2.getClientLogoId());
        assertEquals(caseStudy1.getIndustry(),            caseStudy2.getIndustry());
        assertEquals(caseStudy1.getProjectType(),         caseStudy2.getProjectType());
        assertEquals(caseStudy1.getSummary(),             caseStudy2.getSummary());
        assertEquals(caseStudy1.getTeamMembers(),         caseStudy2.getTeamMembers());
        assertEquals(caseStudy1.getAdvanceLink(),         caseStudy2.getAdvanceLink());
        assertEquals(caseStudy1.getProblemDescription(),  caseStudy2.getProblemDescription());
        assertEquals(caseStudy1.getSolutionDescription(), caseStudy2.getSolutionDescription());
        assertEquals(caseStudy1.getOutcomes(),            caseStudy2.getOutcomes());
        assertEquals(caseStudy1.getToolsUsed(),           caseStudy2.getToolsUsed());
        assertEquals(caseStudy1.getProjectLearnings(),    caseStudy2.getProjectLearnings());

        // convert the Date objects to strings in the format "yyyy-MM-dd", and then compare the strings
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String caseStudy1StartDate = dateFormat.format(caseStudy1.getStartDate());
        String caseStudy1EndDate   = dateFormat.format(caseStudy1.getEndDate());
        String caseStudy2StartDate = dateFormat.format(caseStudy2.getStartDate());
        String caseStudy2EndDate   = dateFormat.format(caseStudy2.getEndDate());
        assertEquals(caseStudy1StartDate, caseStudy2StartDate);
        assertEquals(caseStudy1EndDate,   caseStudy2EndDate);
    }

    public static void assertImageEquals(Image image1, Image image2) {
        assertEquals(image1.getImageId(),   image2.getImageId());
        assertArrayEquals(image1.getData(), image2.getData());
        assertEquals(image1.getType(),      image2.getType());
    }

    public static void assertUserEquals(User user1, User user2) {
        assertEquals(user1.getUserId(),         user2.getUserId());
        assertEquals(user1.getEmail(),          user2.getEmail());
        assertEquals(user1.getFirstName(),      user2.getFirstName());
        assertEquals(user1.getLastName(),       user2.getLastName());
        assertEquals(user1.getRole(),           user2.getRole());
        assertEquals(user1.hasEditPermission(), user2.hasEditPermission());
        assertEquals(user1.isAdmin(),           user2.isAdmin());
        assertEquals(user1.getPassword(),       user2.getPassword());
    }
}
