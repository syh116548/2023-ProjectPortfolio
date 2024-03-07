package spe.projectportfolio.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import spe.projectportfolio.backend.BackendApplication;
import spe.projectportfolio.backend.config.H2TestProfileJPAConfig;
import spe.projectportfolio.backend.mapper.*;
import spe.projectportfolio.backend.pojo.CaseStudy;
import spe.projectportfolio.backend.pojo.CaseStudyUpload;
import spe.projectportfolio.backend.pojo.Image;
import spe.projectportfolio.backend.pojo.enums.EditStatus;
import spe.projectportfolio.backend.pojo.enums.ImageType;
import spe.projectportfolio.backend.pojo.enums.ProjectStatus;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static spe.projectportfolio.backend.PojoAssertions.assertCaseStudyEquals;
import static spe.projectportfolio.backend.PojoAssertions.assertImageEquals;

@SpringBootTest(classes = {BackendApplication.class, H2TestProfileJPAConfig.class})
@AutoConfigureMockMvc
@AutoConfigureMybatis
@ActiveProfiles("test")
@Transactional
class CaseStudyControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private CaseStudyMapper caseStudyMapper;

    @Value("${app.api-url}")
    private String url;

    @WithMockUser(roles = "ADMIN")
    @Test
    void testGetAllCaseStudiesByCondition() throws Exception {
        // create and insert all case studies
        List<CaseStudy> caseStudies1 = createAndInsertThreeCaseStudies(caseStudyMapper, imageMapper);

        // test get all case studies
        mockMvc.perform(get("/api/case-studies").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(caseStudies1))));

        List<CaseStudy> caseStudies2 = caseStudyMapper.findByCondition("title", null, null);
        List<CaseStudy> caseStudies3 = caseStudyMapper.findByCondition("title", null, "software2");
        List<CaseStudy> caseStudies4 = caseStudyMapper.findByCondition(null, "client2", null);

        // test get with condition
        mockMvc.perform(get("/api/case-studies?title=title").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(caseStudies2))));
        mockMvc.perform(get("/api/case-studies?title=title&industry=software2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(caseStudies3))));
        mockMvc.perform(get("/api/case-studies?client-name=client2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(getJson(caseStudies4))));

        // test with random title
        mockMvc.perform(get("/api/case-studies?title=abc").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("[]")));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testGetCaseStudyById() throws Exception {
        // create and insert case studies, and get expected JSON
        List<CaseStudy> caseStudies = createAndInsertThreeCaseStudies(caseStudyMapper, imageMapper);

        // perform tests for each case study
        for (CaseStudy caseStudy : caseStudies) {
            mockMvc.perform(get("/api/case-studies/" + caseStudy.getCaseStudyId()).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(equalTo(getJson(caseStudy))));
        }

        // test id not found
        Long id = caseStudies.get(2).getCaseStudyId() + 1;
        mockMvc.perform(get("/api/case-studies/" + id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // test invalid id
        mockMvc.perform(get("/api/case-studies/abc").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testCreateCaseStudy() throws Exception {
        String base64Image1 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(new byte[]{1,2,3});
        String base64Image2 = "data:image/png;base64,"  + Base64.getEncoder().encodeToString(new byte[]{4,5,6});

        // create three case studies using API
        CaseStudyUpload caseStudyUpload1 = new CaseStudyUpload(null, "title1", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client1", "client1 link", base64Image1, "software", "design",  new Date(), new Date(), "summary1", "team members", "links1", "problem1", "solution1", "outcomes1", "tools used", "project learnings");
        CaseStudyUpload caseStudyUpload2 = new CaseStudyUpload(null, "title2", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client2", "client2 link", base64Image2, "software2", "design", new Date(), new Date(), "summary2", "team members", "links2", "problem2", "solution2", "outcomes2", "tools used", "project learnings");
        CaseStudyUpload caseStudyUpload3 = new CaseStudyUpload(null, "title3", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client3", "client3 link", null,         "software3", null,     new Date(), new Date(), "summary3", "team members", "links3", "problem3", "solution3", "outcomes3", "tools used", "project learnings");
        MvcResult result1 = mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudyUpload1))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult result2 = mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudyUpload2))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult result3 = mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudyUpload3))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        // make sure only two images are inserted into database, and make sure they are correct
        List<Image> foundImages = imageMapper.findAll();
        assertEquals(2, foundImages.size());
        Image foundImage1 = foundImages.get(0);
        Image foundImage2 = foundImages.get(1);
        Image expectedImage1 = new Image(foundImage1.getImageId(), new byte[]{1,2,3}, ImageType.JPEG);
        Image expectedImage2 = new Image(foundImage2.getImageId(), new byte[]{4,5,6}, ImageType.PNG);
        assertImageEquals(foundImage1, expectedImage1);
        assertImageEquals(foundImage2, expectedImage2);

        // make sure only three case studies are inserted into database, and make sure they are correct
        List<CaseStudy> foundCaseStudies = caseStudyMapper.findAll();
        assertEquals(3, foundCaseStudies.size());
        CaseStudy foundCaseStudy1 = foundCaseStudies.get(0);
        CaseStudy foundCaseStudy2 = foundCaseStudies.get(1);
        CaseStudy foundCaseStudy3 = foundCaseStudies.get(2);
        caseStudyUpload1.setCaseStudyId(foundCaseStudy1.getCaseStudyId());
        caseStudyUpload2.setCaseStudyId(foundCaseStudy2.getCaseStudyId());
        caseStudyUpload3.setCaseStudyId(foundCaseStudy3.getCaseStudyId());
        CaseStudy expectedCaseStudy1 = getCaseStudyFromUploadData(caseStudyUpload1, foundImage1.getImageId());
        CaseStudy expectedCaseStudy2 = getCaseStudyFromUploadData(caseStudyUpload2, foundImage2.getImageId());
        CaseStudy expectedCaseStudy3 = getCaseStudyFromUploadData(caseStudyUpload3, null);
        assertCaseStudyEquals(foundCaseStudy1, expectedCaseStudy1);
        assertCaseStudyEquals(foundCaseStudy2, expectedCaseStudy2);
        assertCaseStudyEquals(foundCaseStudy3, expectedCaseStudy3);

        // get case study objects from JSON responses, and check they are correct
        String responseJson1 = result1.getResponse().getContentAsString();
        String responseJson2 = result2.getResponse().getContentAsString();
        String responseJson3 = result3.getResponse().getContentAsString();
        assertEquals(responseJson1, getJson(expectedCaseStudy1));
        assertEquals(responseJson2, getJson(expectedCaseStudy2));
        assertEquals(responseJson3, getJson(expectedCaseStudy3));

        // test invalid request bodies
        CaseStudyUpload nullCaseStudyUpload = null;
        mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(nullCaseStudyUpload))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("asjdslk")
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testCreateCaseStudyWithRichText() throws Exception {
        // create rich text with some text, a couple of images and a link
        String base64Image1 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(new byte[]{1,2,3});
        String base64Image2 = "data:image/png;base64,"  + Base64.getEncoder().encodeToString(new byte[]{4,5,6});
        String richText = "<p>Test 1</p><p><img src=\"" + base64Image1 + "\"></p><p>Test 2</p><p><img src=\"" + base64Image2 + "\"></p>";

        // create a case study using API
        Date date = new Date();
        CaseStudyUpload caseStudyUpload = new CaseStudyUpload(null, "title", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, null, null, null, null, null, date, date, null, null, null, richText, null, null, null, null);
        mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudyUpload))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        // make sure only two images are inserted into database, and make sure they are correct
        List<Image> foundImages = imageMapper.findAll();
        assertEquals(2, foundImages.size());
        Image foundImage1 = foundImages.get(0);
        Image foundImage2 = foundImages.get(1);
        Image expectedImage1 = new Image(foundImage1.getImageId(), new byte[]{1,2,3}, ImageType.JPEG);
        Image expectedImage2 = new Image(foundImage2.getImageId(), new byte[]{4,5,6}, ImageType.PNG);
        assertImageEquals(foundImage1, expectedImage1);
        assertImageEquals(foundImage2, expectedImage2);

        // make sure only one case study is inserted into database, and make sure it is correct
        String expectedRichText = "<p>Test 1</p>\n<p><img src=\"" + foundImage1.getImageId() + "\"></p>\n<p>Test 2</p>\n<p><img src=\"" + foundImage2.getImageId() + "\"></p>";
        List<CaseStudy> foundCaseStudies = caseStudyMapper.findAll();
        assertEquals(1, foundCaseStudies.size());
        CaseStudy foundCaseStudy = foundCaseStudies.get(0);
        CaseStudy expectedCaseStudy = new CaseStudy(foundCaseStudy.getCaseStudyId(), "title", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, null, null, null, null, null, date, date, null, null, null, expectedRichText, null, null, null, null);
        assertCaseStudyEquals(foundCaseStudy, expectedCaseStudy);
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testUpdateCaseStudy() throws Exception {
        // create and insert image
        Image image1 = new Image(null, new byte[]{1,2,3}, ImageType.JPEG);
        imageMapper.insert(image1);

        // insert case study into database
        Date startDate = new Date();
        Date endDate = new Date();
        CaseStudy caseStudy1 = new CaseStudy(null, "title1", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client1", "client1 link", image1.getImageId(), "software", "design", startDate, endDate, "summary1", "team members", "links1", "problem1", "solution1", "outcomes1", "tools used", "project learnings");
        CaseStudy caseStudy2 = new CaseStudy(null, "title3", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client3", "client3 link", null,                "software", "design", startDate, endDate, "summary3", "team members", "links3", "problem1", "solution3", "outcomes3", "tools used", "project learnings");
        caseStudyMapper.insert(caseStudy1);
        caseStudyMapper.insert(caseStudy2);

        String base64Image2 = "data:image/png;base64,"  + Base64.getEncoder().encodeToString(new byte[]{4,5,6});
        String base64Image3 = "data:image/png;base64,"  + Base64.getEncoder().encodeToString(new byte[]{7,8,9});

        // update case study using API (with updated image)
        CaseStudyUpload caseStudyUpload1 = new CaseStudyUpload(caseStudy1.getCaseStudyId(), "title2", ProjectStatus.COMPLETED, null, "client2", "client2 link", base64Image2, null, "design2", null, null, "summary2", null, "links2", "problem2", "solution2", "outcomes2", null, null);
        CaseStudyUpload caseStudyUpload2 = new CaseStudyUpload(caseStudy2.getCaseStudyId(), "title2", ProjectStatus.COMPLETED, null, "client2", "client2 link", base64Image3, null, "design2", null, null, "summary2", null, "links2", "problem2", "solution2", "outcomes2", null, null);
        MvcResult result1 = mockMvc.perform(put("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudyUpload1))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult result2 = mockMvc.perform(put("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudyUpload2))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        // check image in database is updated, and check new image is added
        List<Image> foundImages = imageMapper.findAll();
        assertEquals(2, foundImages.size());
        Image foundImage1 = foundImages.get(0);
        Image foundImage2 = foundImages.get(1);
        Image expectedImage1 = new Image(foundImage1.getImageId(), new byte[]{4,5,6}, ImageType.PNG); // updated image
        Image expectedImage2 = new Image(foundImage2.getImageId(), new byte[]{7,8,9}, ImageType.PNG); // new image
        assertImageEquals(foundImage1, expectedImage1);
        assertImageEquals(foundImage2, expectedImage2);

        // check case study in database is updated
        List<CaseStudy> foundCaseStudies = caseStudyMapper.findAll();
        assertEquals(2, foundCaseStudies.size());
        CaseStudy foundCaseStudy1 = foundCaseStudies.get(0);
        CaseStudy foundCaseStudy2 = foundCaseStudies.get(1);
        CaseStudy expectedCaseStudy1 = new CaseStudy(caseStudy1.getCaseStudyId(), "title2", ProjectStatus.COMPLETED, EditStatus.PUBLISHED, "client2", "client2 link", foundImage1.getImageId(), "software", "design2", startDate, endDate, "summary2", "team members", "links2", "problem2", "solution2", "outcomes2", "tools used", "project learnings");
        CaseStudy expectedCaseStudy2 = new CaseStudy(caseStudy2.getCaseStudyId(), "title2", ProjectStatus.COMPLETED, EditStatus.PUBLISHED, "client2", "client2 link", foundImage2.getImageId(), "software", "design2", startDate, endDate, "summary2", "team members", "links2", "problem2", "solution2", "outcomes2", "tools used", "project learnings");
        assertCaseStudyEquals(foundCaseStudy1, expectedCaseStudy1);
        assertCaseStudyEquals(foundCaseStudy2, expectedCaseStudy2);

        // get case study object from JSON response, and check it is correct
        String responseJson1 = result1.getResponse().getContentAsString();
        String responseJson2 = result2.getResponse().getContentAsString();
        assertEquals(responseJson1, getJson(foundCaseStudy1));
        assertEquals(responseJson2, getJson(foundCaseStudy2));

        // update case study using API (without updated image)
        CaseStudyUpload caseStudyUpload3 = new CaseStudyUpload(caseStudy1.getCaseStudyId(), "title3", null, EditStatus.DRAFT, "client3", "client3 link", null, null, "design3", null, null, null, null, "links3", null, null, "outcomes3", null, "project learnings 2");
        MvcResult result3 = mockMvc.perform(put("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudyUpload3))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        // check image in database still there and not updated
        foundImages = imageMapper.findAll();
        assertEquals(2, foundImages.size());
        Image foundImage3 = foundImages.get(0);
        Image expectedImage3 = new Image(foundImage3.getImageId(), new byte[]{4,5,6}, ImageType.PNG);
        assertImageEquals(foundImage3, expectedImage3);

        // check case study in database is updated
        foundCaseStudies = caseStudyMapper.findAll();
        assertEquals(2, foundCaseStudies.size());
        CaseStudy foundCaseStudy3 = foundCaseStudies.get(0);
        CaseStudy expectedCaseStudy3 = new CaseStudy(caseStudy1.getCaseStudyId(), "title3", ProjectStatus.COMPLETED, EditStatus.DRAFT, "client3", "client3 link", foundImage1.getImageId(), "software", "design3", startDate, endDate, "summary2", "team members", "links3", "problem2", "solution2", "outcomes3", "tools used", "project learnings 2");
        assertCaseStudyEquals(foundCaseStudy3, expectedCaseStudy3);

        // get case study object from JSON response, and check it is correct
        String responseJson3 = result3.getResponse().getContentAsString();
        assertEquals(responseJson3, getJson(foundCaseStudy3));

        // test invalid request bodies
        CaseStudy nullCaseStudy = null;
        mockMvc.perform(put("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(nullCaseStudy))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("asjdslk")
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }
    @WithMockUser(roles = "ADMIN")
    @Test
    void testUpdateCaseStudyWithRichText() throws Exception {
        // create rich text with some text and a couple of images
        String base64Image1 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(new byte[]{1,2,3});
        String base64Image2 = "data:image/png;base64,"  + Base64.getEncoder().encodeToString(new byte[]{4,5,6});
        String richText = "<p>Test 1</p><p><img src=\"" + base64Image1 + "\"></p><p>Test 2</p><p><img src=\"" + base64Image2 + "\"></p>";

        // create a case study using API
        Date date = new Date();
        CaseStudyUpload caseStudyUpload = new CaseStudyUpload(null, "title", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, null, null, null, null, null, date, date, null, null, null, richText, null, null, null, null);
        mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudyUpload))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        // make sure only two images are inserted into database, and make sure they are correct
        List<Image> foundImages = imageMapper.findAll();
        assertEquals(2, foundImages.size());
        Image foundImage1 = foundImages.get(0);
        Image foundImage2 = foundImages.get(1);
        Image expectedImage1 = new Image(foundImage1.getImageId(), new byte[]{1,2,3}, ImageType.JPEG);
        Image expectedImage2 = new Image(foundImage2.getImageId(), new byte[]{4,5,6}, ImageType.PNG);
        assertImageEquals(foundImage1, expectedImage1);
        assertImageEquals(foundImage2, expectedImage2);

        // create new rich text with new text, with one image removed, one from before remaining, and two new images added
        Long imageId = foundImages.get(1).getImageId();
        String imageLink = url + "/api/images/" + imageId;
        String base64Image3 = "data:image/jpeg;base64,"  + Base64.getEncoder().encodeToString(new byte[]{7,8,9});
        String base64Image4 = "data:image/png;base64,"  + Base64.getEncoder().encodeToString(new byte[]{10,11,12});
        String newRichText = "<p><img src=\"" + base64Image3 + "\"></p><p>New Test 1</p><p>New Test 2</p><p><img src=\"" + imageLink + "\"></p><p><img src=\"" + base64Image4 + "\"></p>";

        // update the case study using API
        Long caseStudyId = caseStudyMapper.findAll().get(0).getCaseStudyId();
        CaseStudyUpload caseStudyUpdate = new CaseStudyUpload(caseStudyId, null, null, null, null, null, null, null, null, null, null, null, null, null, newRichText, null, null, null, null);
        MvcResult result = mockMvc.perform(put("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudyUpdate))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        // make sure there are now three images in the database (since previously it was two, then we removed one and
        // added two more), and make sure they are correct
        foundImages = imageMapper.findAll();
        assertEquals(3, foundImages.size());
        Image foundImage3 = foundImages.get(0);
        Image foundImage4 = foundImages.get(1);
        Image foundImage5 = foundImages.get(2);
        Image expectedImage3 = new Image(foundImage3.getImageId(), new byte[]{4,5,6}, ImageType.PNG);
        Image expectedImage4 = new Image(foundImage4.getImageId(), new byte[]{7,8,9}, ImageType.JPEG);
        Image expectedImage5 = new Image(foundImage5.getImageId(), new byte[]{10,11,12}, ImageType.PNG);
        assertImageEquals(foundImage3, expectedImage3);
        assertImageEquals(foundImage4, expectedImage4);
        assertImageEquals(foundImage5, expectedImage5);

        // make sure only one case study is in the database, and make sure it is correct
        String expectedRichText = "<p><img src=\"" + foundImage4.getImageId() + "\"></p>\n<p>New Test 1</p>\n<p>New Test 2</p>\n<p><img src=\"" + foundImage3.getImageId() + "\"></p>\n<p><img src=\"" + foundImage5.getImageId() + "\"></p>";
        List<CaseStudy> foundCaseStudies = caseStudyMapper.findAll();
        assertEquals(1, foundCaseStudies.size());
        CaseStudy foundCaseStudy = foundCaseStudies.get(0);
        CaseStudy expectedCaseStudy = new CaseStudy(foundCaseStudy.getCaseStudyId(), "title", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, null, null, null, null, null, date, date, null, null, null, expectedRichText, null, null, null, null);
        assertCaseStudyEquals(foundCaseStudy, expectedCaseStudy);
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testDeleteCaseStudy() throws Exception {
        // create and insert images
        Image image1 = new Image(null, new byte[]{1,2,3}, ImageType.JPEG);
        Image image2 = new Image(null, new byte[]{4,5,6}, ImageType.PNG);
        imageMapper.insert(image1);
        imageMapper.insert(image2);

        // create and insert two case studies
        CaseStudy caseStudy1 = new CaseStudy(null, "title1", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client1", "client1 link", image1.getImageId(), "software", "design", new Date(), new Date(), "summary1", "team members", "links1", "problem1", "solution1", "outcomes1", "tools used", "project learnings");
        CaseStudy caseStudy2 = new CaseStudy(null, "title2", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client2", "client2 link", image2.getImageId(), "software2", "design", new Date(), new Date(), "summary2", "team members", "links2", "problem2", "solution2", "outcomes2", "tools used", "project learnings");
        caseStudyMapper.insert(caseStudy1);
        caseStudyMapper.insert(caseStudy2);

        // delete caseStudy2 using API
        mockMvc.perform(delete("/api/case-studies/" + caseStudy2.getCaseStudyId()))
                .andExpect(status().isOk());

        // make sure only image1 is in database, and make sure it is correct
        List<Image> foundImages = imageMapper.findAll();
        assertEquals(1, foundImages.size());
        assertImageEquals(foundImages.get(0), image1);

        // make sure only caseStudy1 is in database, and make sure it is correct
        List<CaseStudy> foundCaseStudies = caseStudyMapper.findAll();
        assertEquals(1, foundCaseStudies.size());
        assertCaseStudyEquals(foundCaseStudies.get(0), caseStudy1);

        // test id not found
        mockMvc.perform(delete("/api/case-studies/" + caseStudy2.getCaseStudyId() + 1))
                .andExpect(status().isNotFound());

        // test invalid id
        mockMvc.perform(delete("/api/case-studies/abc"))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testDeleteCaseStudyWithRichText() throws Exception {
        // create rich text with some text and a couple of images
        String base64Image1 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(new byte[]{1,2,3});
        String base64Image2 = "data:image/png;base64,"  + Base64.getEncoder().encodeToString(new byte[]{4,5,6});
        String richText = "<p>Test 1</p><p><img src=\"" + base64Image1 + "\"></p><p>Test 2</p><p><img src=\"" + base64Image2 + "\"></p>";

        // create a case study using API
        Date date = new Date();
        CaseStudyUpload caseStudyUpload = new CaseStudyUpload(null, "title", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, null, null, null, null, null, date, date, null, null, null, richText, null, null, null, null);
        mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudyUpload))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        // make sure only two images are inserted into database, and make sure they are correct
        List<Image> foundImages = imageMapper.findAll();
        assertEquals(2, foundImages.size());
        Image foundImage1 = foundImages.get(0);
        Image foundImage2 = foundImages.get(1);
        Image expectedImage1 = new Image(foundImage1.getImageId(), new byte[]{1,2,3}, ImageType.JPEG);
        Image expectedImage2 = new Image(foundImage2.getImageId(), new byte[]{4,5,6}, ImageType.PNG);
        assertImageEquals(foundImage1, expectedImage1);
        assertImageEquals(foundImage2, expectedImage2);

        // delete the case study using API
        Long caseStudyId = caseStudyMapper.findAll().get(0).getCaseStudyId();
        mockMvc.perform(delete("/api/case-studies/" + caseStudyId))
                .andExpect(status().isOk());

        // make sure there are now no images in the database
        foundImages = imageMapper.findAll();
        assertEquals(0, foundImages.size());

        // make sure no case studies are in the database
        List<CaseStudy> foundCaseStudies = caseStudyMapper.findAll();
        assertEquals(0, foundCaseStudies.size());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testRichTextHTMLSanitisation() throws Exception {
        // create rich text with some text, a link and some javascript
        String unsanitisedRichText = "<p><a href=\"https://www.example.com/\">Link</a></p><script>alert('Test');</script><p>Test 1</p><p onclick=\"alert('Test')\">Test 2</p><script src=\"script.js\"></script>";

        // create a case study using API
        Date date = new Date();
        CaseStudyUpload caseStudyUpload = new CaseStudyUpload(null, "title", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, null, null, null, null, null, date, date, null, null, null, unsanitisedRichText, null, null, null, null);
        mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudyUpload))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        // make sure only one case study is in the database, and make sure it is correct (make sure that the rich text
        // is sanitised, so the expected rich text should be just the text and link with all script tags and any other
        // javascript removed, with rel="nofollow" added to the link)
        String expectedRichText = "<p><a href=\"https://www.example.com/\" rel=\"nofollow\">Link</a></p>\n<p>Test 1</p>\n<p>Test 2</p>";
        List<CaseStudy> foundCaseStudies = caseStudyMapper.findAll();
        assertEquals(1, foundCaseStudies.size());
        CaseStudy foundCaseStudy = foundCaseStudies.get(0);
        CaseStudy expectedCaseStudy = new CaseStudy(foundCaseStudy.getCaseStudyId(), "title", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, null, null, null, null, null, date, date, null, null, null, expectedRichText, null, null, null, null);
        assertCaseStudyEquals(foundCaseStudy, expectedCaseStudy);
    }

    @Test
    void testNotLoggedInAccess() throws Exception {
        // check all API endpoints return HTTP code 401 ("unauthorized")
        mockMvc.perform(get("/api/case-studies").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/case-studies/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/case-studies").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/case-studies").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/case-studies/1"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(roles = "USER")
    @Test
    void testUserAccess() throws Exception {
        // user without edit permission should only be allowed to view case studies (not add, modify or delete them)
        CaseStudy caseStudy = createAndInsertThreeCaseStudies(caseStudyMapper, imageMapper).get(0);
        mockMvc.perform(get("/api/case-studies").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/case-studies/" + caseStudy.getCaseStudyId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudy))
                        .characterEncoding("utf-8"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudy))
                        .characterEncoding("utf-8"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/case-studies/" + caseStudy.getCaseStudyId()))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "EDITOR")
    @Test
    void testEditorAccess() throws Exception {
        // user with edit permission should be allowed to access all API endpoints for case studies
        CaseStudy caseStudy = createAndInsertThreeCaseStudies(caseStudyMapper, imageMapper).get(0);
        mockMvc.perform(get("/api/case-studies").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/case-studies/" + caseStudy.getCaseStudyId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudy))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudy))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/case-studies/" + caseStudy.getCaseStudyId()))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testAdminAccess() throws Exception {
        // admin should be allowed to access all API endpoints for case studies
        CaseStudy caseStudy = createAndInsertThreeCaseStudies(caseStudyMapper, imageMapper).get(0);
        mockMvc.perform(get("/api/case-studies").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/case-studies/" + caseStudy.getCaseStudyId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudy))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(caseStudy))
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/case-studies/" + caseStudy.getCaseStudyId()))
                .andExpect(status().isOk());
    }

    static String getJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    public static CaseStudy getCaseStudyFromUploadData(CaseStudyUpload data, Long imageId) {
        return new CaseStudy(
                data.getCaseStudyId(),
                data.getTitle(),
                data.getProjectStatus(),
                data.getEditStatus(),
                data.getClientName(),
                data.getClientLink(),
                imageId,
                data.getIndustry(),
                data.getProjectType(),
                data.getStartDate(),
                data.getEndDate(),
                data.getSummary(),
                data.getTeamMembers(),
                data.getAdvanceLink(),
                data.getProblemDescription(),
                data.getSolutionDescription(),
                data.getOutcomes(),
                data.getToolsUsed(),
                data.getProjectLearnings()
        );
    }

    static List<CaseStudy> createAndInsertThreeCaseStudies(CaseStudyMapper caseStudyMapper, ImageMapper imageMapper) {
        // create and insert images
        Image image1 = new Image(null, new byte[]{1,2,3}, ImageType.JPEG);
        Image image2 = new Image(null, new byte[]{4,5,6}, ImageType.PNG);
        Image image3 = new Image(null, new byte[]{7,8,9}, ImageType.JPEG);
        imageMapper.insert(image1);
        imageMapper.insert(image2);
        imageMapper.insert(image3);

        // create and insert case studies
        CaseStudy caseStudy1 = new CaseStudy(null, "title1", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client1", "client1 link", image1.getImageId(), "software", "design", new Date(), new Date(), "summary1", "team members", "links1", "problem1", "solution1", "outcomes1", "tools used", "project learnings");
        CaseStudy caseStudy2 = new CaseStudy(null, "title2", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client2", "client2 link", image2.getImageId(), "software2", "design", new Date(), new Date(), "summary2", "team members", "links2", "problem2", "solution2", "outcomes2", "tools used", "project learnings");
        CaseStudy caseStudy3 = new CaseStudy(null, "title3", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, "client3", "client3 link", image3.getImageId(), "software2", "design", new Date(), new Date(), "summary3", "team members", "links3", "problem3", "solution3", "outcomes3", "tools used", "project learnings");
        caseStudyMapper.insert(caseStudy1);
        caseStudyMapper.insert(caseStudy2);
        caseStudyMapper.insert(caseStudy3);

        // return list of case studies
        return List.of(caseStudy1, caseStudy2, caseStudy3);
    }
}
