package spe.projectportfolio.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import spe.projectportfolio.backend.BackendApplication;
import spe.projectportfolio.backend.config.H2TestProfileJPAConfig;
import spe.projectportfolio.backend.mapper.CaseStudyMapper;
import spe.projectportfolio.backend.mapper.ImageMapper;
import spe.projectportfolio.backend.pojo.CaseStudy;
import spe.projectportfolio.backend.pojo.CaseStudyUpload;
import spe.projectportfolio.backend.pojo.Image;
import spe.projectportfolio.backend.pojo.enums.EditStatus;
import spe.projectportfolio.backend.pojo.enums.ImageType;
import spe.projectportfolio.backend.pojo.enums.ProjectStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {BackendApplication.class, H2TestProfileJPAConfig.class})
@AutoConfigureMockMvc
@AutoConfigureMybatis
@ActiveProfiles("test")
public class CaseStudyControllerRollbackTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private CaseStudyMapper caseStudyMapper;

    @WithMockUser(roles = "ADMIN")
    @Test
    void testCreateCaseStudyRollback() throws Exception {
        // test invalid case study rich text HTML to check that any inserted images are rolled back
        // (the following is bad rich text because when inserting case studies, only images with base64 are accepted,
        //  so it should crash after inserting the first image when trying to insert the second, so we are testing to
        //  see if the insertion of the first image is rolled back)
        String badRichText = "<img src=\"data:image/png;base64,iVBORw0KGgoAAAAN\"><img src=\"http://localhost/api/images/100\">";
        CaseStudyUpload badCaseStudy = new CaseStudyUpload(null, "title", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, null, null, null, null, null, null, null, null, null, null, badRichText, null, null, null, null);
        int numImagesInDatabase = imageMapper.findAll().size();
        mockMvc.perform(post("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(badCaseStudy))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        int newNumImagesInDatabase = imageMapper.findAll().size();
        assertEquals(numImagesInDatabase, newNumImagesInDatabase);
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testUpdateCaseStudyRollback() throws Exception {
        Image image = new Image(null, new byte[]{0,1,2}, ImageType.JPEG);
        imageMapper.insert(image);
        String richText = "<img src=\"" + image.getImageId() + "\">";
        CaseStudy caseStudy = new CaseStudy(null, "title", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, null, null, null, null, null, null, null, null, null, null, richText, null, null, null, null);
        caseStudyMapper.insert(caseStudy);

        // test invalid case study rich text HTML to check that any inserted and/or deleted images are rolled back
        // (the following is bad rich text because when updating case studies, if an image src doesn't contain a link,
        //  it is interpreted as base64, and clearly "abc" is not valid base64 (it doesn't contain "data:image/..." at the start)
        //  so it should crash after inserting the first image when trying to insert the second, so we are testing to
        //  see if the insertion of the first image is rolled back)
        String badRichText = "<img src=\"data:image/png;base64,iVBORw0KGgoAAAAN\"><img src=\"abc\">";
        CaseStudyUpload badCaseStudyUpdate = new CaseStudyUpload(caseStudy.getCaseStudyId(), "title", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, null, null, null, null, null, null, null, null, null, null, badRichText, null, null, null, null);

        int numImagesInDatabase = imageMapper.findAll().size();
        mockMvc.perform(put("/api/case-studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(badCaseStudyUpdate))
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        int newNumImagesInDatabase = imageMapper.findAll().size();
        assertEquals(numImagesInDatabase, newNumImagesInDatabase);

        // delete image and case study from database to not interfere with other tests
        imageMapper.delete(image.getImageId());
        caseStudyMapper.delete(caseStudy.getCaseStudyId());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testDeleteCaseStudyRollback() throws Exception {
        // test invalid case study rich text HTML to check that any deleted images are rolled back
        // (the following is bad rich text because when deleting case studies, only image tags with src containing just
        //  an image ID are accepted, so it should crash after deleting the first image when trying to delete the
        //  second, so we are testing to see if the deletion of the first image is rolled back)
        Image image = new Image(null, new byte[]{0,1,2}, ImageType.PNG);
        imageMapper.insert(image);
        String badRichText = "<img src=\"" + image.getImageId() + "\"><img src=\"http://localhost/api/images/100\">";
        CaseStudy badCaseStudy = new CaseStudy(null, "title", ProjectStatus.ACTIVE, EditStatus.PUBLISHED, null, null, null, null, null, null, null, null, null, null, badRichText, null, null, null, null);
        caseStudyMapper.insert(badCaseStudy);

        int numImagesInDatabase = imageMapper.findAll().size();
        mockMvc.perform(delete("/api/case-studies/" + badCaseStudy.getCaseStudyId()))
                .andExpect(status().isBadRequest());
        int newNumImagesInDatabase = imageMapper.findAll().size();
        assertEquals(numImagesInDatabase, newNumImagesInDatabase);

        // delete image and case study from database to not interfere with other tests
        imageMapper.delete(image.getImageId());
        caseStudyMapper.delete(badCaseStudy.getCaseStudyId());
    }

    static String getJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }
}
