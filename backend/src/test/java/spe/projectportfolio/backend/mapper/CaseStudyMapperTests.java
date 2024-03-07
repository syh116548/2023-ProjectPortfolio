package spe.projectportfolio.backend.mapper;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import spe.projectportfolio.backend.pojo.CaseStudy;
import spe.projectportfolio.backend.pojo.Image;
import spe.projectportfolio.backend.pojo.enums.EditStatus;
import spe.projectportfolio.backend.pojo.enums.ImageType;
import spe.projectportfolio.backend.pojo.enums.ProjectStatus;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static spe.projectportfolio.backend.PojoAssertions.assertCaseStudyEquals;

@MybatisTest
class CaseStudyMapperTests {
    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private CaseStudyMapper caseStudyMapper;

    @Test
    void testInsertFindDelete() {
        // create images and insert into database
        Image image1 = new Image(1L, new byte[]{1,2,3}, ImageType.PNG);
        Image image2 = new Image(2L, new byte[]{4,5,6}, ImageType.JPEG);
        Image image3 = new Image(3L, new byte[]{7,8,9}, ImageType.PNG);
        imageMapper.insert(image1);
        imageMapper.insert(image2);
        imageMapper.insert(image3);

        // create case studies and insert into database
        CaseStudy caseStudy1 = new CaseStudy(1L, "title1", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client1", "client1 link", image1.getImageId(), "software", "design", new Date(), new Date(), "summary1", "team members", "links1", "problem1", "solution1", "outcomes1", "tools used", "project learnings");
        CaseStudy caseStudy2 = new CaseStudy(2L, "title2", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client2", "client2 link", image2.getImageId(), "software", "design", new Date(), new Date(), "summary2", "team members", "links2", "problem2", "solution2", "outcomes2", "tools used", "project learnings");
        CaseStudy caseStudy3 = new CaseStudy(3L, "title3", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client3", "client3 link", image3.getImageId(), "software", "design", new Date(), new Date(), "summary3", "team members", "links3", "problem3", "solution3", "outcomes3", "tools used", "project learnings");
        caseStudyMapper.insert(caseStudy1);
        caseStudyMapper.insert(caseStudy2);
        caseStudyMapper.insert(caseStudy3);

        // test findAll
        List<CaseStudy> foundCaseStudies = caseStudyMapper.findAll();
        assertEquals(foundCaseStudies.size(), 3);
        assertCaseStudyEquals(foundCaseStudies.get(0), caseStudy1);
        assertCaseStudyEquals(foundCaseStudies.get(1), caseStudy2);
        assertCaseStudyEquals(foundCaseStudies.get(2), caseStudy3);

        // test findById
        CaseStudy foundCaseStudy = caseStudyMapper.findById(caseStudy3.getCaseStudyId());
        assertNotNull(foundCaseStudy);
        assertCaseStudyEquals(foundCaseStudy, caseStudy3);

        // test delete
        caseStudyMapper.delete(caseStudy2.getCaseStudyId());
        foundCaseStudies = caseStudyMapper.findAll();
        assertEquals(foundCaseStudies.size(), 2);
        assertCaseStudyEquals(foundCaseStudies.get(0), caseStudy1);
        assertCaseStudyEquals(foundCaseStudies.get(1), caseStudy3);
    }

    @Test
    void testFindByCondition() {
        // create images and insert into database
        Image image1 = new Image(1L, new byte[]{1,2,3}, ImageType.PNG);
        Image image2 = new Image(2L, new byte[]{4,5,6}, ImageType.JPEG);
        Image image3 = new Image(3L, new byte[]{7,8,9}, ImageType.PNG);
        imageMapper.insert(image1);
        imageMapper.insert(image2);
        imageMapper.insert(image3);

        // create case studies and insert into database
        CaseStudy caseStudy1 = new CaseStudy(1L, "title1", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client1", "client1 link", image1.getImageId(), "software", "design", new Date(), new Date(), "summary1", "team members", "links1", "problem1", "solution1", "outcomes1", "tools used", "project learnings");
        CaseStudy caseStudy2 = new CaseStudy(2L, "title2", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client2", "client2 link", image2.getImageId(), "software", "design", new Date(), new Date(), "summary2", "team members", "links2", "problem2", "solution2", "outcomes2", "tools used", "project learnings");
        CaseStudy caseStudy3 = new CaseStudy(3L, "title3", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client3", "client3 link", image3.getImageId(), "software", "design", new Date(), new Date(), "summary3", "team members", "links3", "problem3", "solution3", "outcomes3", "tools used", "project learnings");
        CaseStudy caseStudy4 = new CaseStudy(4L, "title4", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client2", "client2 link", image2.getImageId(), "other industry", "design", new Date(), new Date(), "summary3", "team members", "links3", "problem3", "solution3", "outcomes3", "tools used", "project learnings");
        CaseStudy caseStudy5 = new CaseStudy(5L, "title4", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client2", "client2 link", image2.getImageId(), "software", "design", new Date(), new Date(), "summary3", "team members", "links3", "problem3", "solution3", "outcomes3", "tools used", "project learnings");
        caseStudyMapper.insert(caseStudy1);
        caseStudyMapper.insert(caseStudy2);
        caseStudyMapper.insert(caseStudy3);
        caseStudyMapper.insert(caseStudy4);
        caseStudyMapper.insert(caseStudy5);

        // test finding by title (should return all five case studies)
        List<CaseStudy> foundCaseStudies = caseStudyMapper.findByCondition("title", null, null);
        assertEquals(foundCaseStudies.size(), 5);
        assertCaseStudyEquals(foundCaseStudies.get(0), caseStudy1);
        assertCaseStudyEquals(foundCaseStudies.get(1), caseStudy2);
        assertCaseStudyEquals(foundCaseStudies.get(2), caseStudy3);
        assertCaseStudyEquals(foundCaseStudies.get(3), caseStudy4);
        assertCaseStudyEquals(foundCaseStudies.get(4), caseStudy5);

        // test finding by client and industry
        foundCaseStudies = caseStudyMapper.findByCondition(null, "client2", "software");
        assertEquals(foundCaseStudies.size(), 2);
        assertCaseStudyEquals(foundCaseStudies.get(0), caseStudy2);
        assertCaseStudyEquals(foundCaseStudies.get(1), caseStudy5);
    }

    @Test
    void testUpdate() {
        // create images and insert into database
        Image image1 = new Image(1L, new byte[]{1,2,3}, ImageType.JPEG);
        Image image2 = new Image(1L, new byte[]{1,2,3}, ImageType.JPEG);
        imageMapper.insert(image1);
        imageMapper.insert(image2);

        // create case studies and insert into database
        Date oldDate = new Date();
        CaseStudy caseStudy1 = new CaseStudy(1L, "title1", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client1", "client1 link", image1.getImageId(), "software1", "design1", oldDate, oldDate, "summary1", "team members", "links1", "problem1", "solution1", "outcomes1", "tools used 1", "project learnings 1");
        CaseStudy caseStudy2 = new CaseStudy(2L, "title1", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client1", "client1 link", image1.getImageId(), "software1", "design1", oldDate, oldDate, "summary1", "team members", "links1", "problem1", "solution1", "outcomes1", "tools used 1", "project learnings 1");
        caseStudyMapper.insert(caseStudy1);
        caseStudyMapper.insert(caseStudy2);

        // update caseStudy1 fields
        Date newDate = new Date();
        caseStudy1.setTitle("title2");
        caseStudy1.setProjectStatus(ProjectStatus.COMPLETED);
        caseStudy1.setEditStatus(EditStatus.DRAFT);
        caseStudy1.setClientName("client2");
        caseStudy1.setClientLink("client2 link");
        caseStudy1.setClientLogoId(image2.getImageId());
        caseStudy1.setIndustry("software2");
        caseStudy1.setProjectType("design2");
        caseStudy1.setStartDate(newDate);
        caseStudy1.setEndDate(newDate);
        caseStudy1.setSummary("summary2");
        caseStudy1.setAdvanceLink("links2");
        caseStudy1.setProblemDescription("problem2");
        caseStudy1.setSolutionDescription("solution2");
        caseStudy1.setOutcomes("outcomes2");
        caseStudy1.setToolsUsed("tools used 2");
        caseStudy1.setProjectLearnings("project learnings 2");

        // set caseStudy2 fields to null except one (so they do not get updated in database except one)
        caseStudy2.setTitle("title2");
        caseStudy2.setProjectStatus(null);
        caseStudy2.setEditStatus(null);
        caseStudy2.setClientName(null);
        caseStudy2.setClientLink(null);
        caseStudy2.setClientLogoId(null);
        caseStudy2.setIndustry(null);
        caseStudy2.setProjectType(null);
        caseStudy2.setStartDate(null);
        caseStudy2.setEndDate(null);
        caseStudy2.setSummary(null);
        caseStudy2.setAdvanceLink(null);
        caseStudy2.setProblemDescription(null);
        caseStudy2.setSolutionDescription(null);
        caseStudy2.setOutcomes(null);
        caseStudy2.setToolsUsed(null);
        caseStudy2.setProjectLearnings(null);

        // update case studies in database
        caseStudyMapper.update(caseStudy1);
        caseStudyMapper.update(caseStudy2);

        // fetch case studies from database and check if they were updated successfully (caseStudy2 should be unchanged except for one field)
        CaseStudy foundCaseStudy1 = caseStudyMapper.findById(caseStudy1.getCaseStudyId());
        CaseStudy foundCaseStudy2 = caseStudyMapper.findById(caseStudy2.getCaseStudyId());
        assertCaseStudyEquals(foundCaseStudy1, new CaseStudy(caseStudy1.getCaseStudyId(), "title2", ProjectStatus.COMPLETED, EditStatus.DRAFT, "client2", "client2 link", image2.getImageId(), "software2", "design2", newDate, newDate, "summary2", "team members", "links2", "problem2", "solution2", "outcomes2", "tools used 2", "project learnings 2"));
        assertCaseStudyEquals(foundCaseStudy2, new CaseStudy(caseStudy2.getCaseStudyId(), "title2", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client1", "client1 link", image1.getImageId(), "software1", "design1", oldDate, oldDate, "summary1", "team members", "links1", "problem1", "solution1", "outcomes1", "tools used 1", "project learnings 1"));
    }

    @Test
    void testDeleteByIds() {
        // create images and insert into database
        Image image1 = new Image(1L, new byte[]{1,2,3}, ImageType.PNG);
        Image image2 = new Image(2L, new byte[]{4,5,6}, ImageType.JPEG);
        Image image3 = new Image(3L, new byte[]{7,8,9}, ImageType.PNG);
        imageMapper.insert(image1);
        imageMapper.insert(image2);
        imageMapper.insert(image3);

        // create case studies and insert into database
        CaseStudy caseStudy1 = new CaseStudy(1L, "title1", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client1", "client1 link", image1.getImageId(), "software", "design", new Date(), new Date(), "summary1", "team members", "links1", "problem1", "solution1", "outcomes1", "tools used", "project learnings");
        CaseStudy caseStudy2 = new CaseStudy(2L, "title2", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client2", "client2 link", image2.getImageId(), "software", "design", new Date(), new Date(), "summary2", "team members", "links2", "problem2", "solution2", "outcomes2", "tools used", "project learnings");
        CaseStudy caseStudy3 = new CaseStudy(3L, "title3", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client3", "client3 link", image3.getImageId(), "software", "design", new Date(), new Date(), "summary3", "team members", "links3", "problem3", "solution3", "outcomes3", "tools used", "project learnings");
        caseStudyMapper.insert(caseStudy1);
        caseStudyMapper.insert(caseStudy2);
        caseStudyMapper.insert(caseStudy3);

        // delete two case studies
        List<Long> ids = Arrays.asList(caseStudy1.getCaseStudyId(), caseStudy3.getCaseStudyId());
        caseStudyMapper.deleteByIds(ids);

        // check there is only caseStudy2 in database
        List<CaseStudy> foundCaseStudies = caseStudyMapper.findAll();
        assertEquals(foundCaseStudies.size(), 1);
        assertCaseStudyEquals(foundCaseStudies.get(0), caseStudy2);
    }
}
