package spe.projectportfolio.backend.controller;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
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
import spe.projectportfolio.backend.pojo.Image;
import spe.projectportfolio.backend.pojo.enums.ImageType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static spe.projectportfolio.backend.controller.ImageController.getMediaTypeFromImageType;

@SpringBootTest(classes = {BackendApplication.class, H2TestProfileJPAConfig.class})
@AutoConfigureMockMvc
@AutoConfigureMybatis
@ActiveProfiles("test")
@Transactional
class ImageControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImageMapper imageMapper;

    @WithMockUser(roles = "ADMIN")
    @Test
    void testGetImageById() throws Exception {
        // create and insert images, and get expected JSON
        List<Image> images = createAndInsertThreeImages(imageMapper);

        // perform tests for each case study
        for (Image image : images) {
            mockMvc.perform(get("/api/images/" + image.getImageId()).accept(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(getMediaTypeFromImageType(image.getType())))
                    .andExpect(content().bytes(image.getData()));
        }

        // test id not found
        Long id = images.get(2).getImageId() + 1;
        mockMvc.perform(get("/api/images/" + id).accept(MediaType.IMAGE_JPEG))
                .andExpect(status().isNotFound());

        // test invalid id
        mockMvc.perform(get("/api/images/abc").accept(MediaType.IMAGE_JPEG))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testCreateImage() throws Exception {
        // create images using API
        byte[] data1 = new byte[]{1,2,3};
        byte[] data2 = new byte[]{4,5,6};
        MvcResult result1 = mockMvc.perform(post("/api/images")
                        .contentType(MediaType.IMAGE_JPEG)
                        .content(data1))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult result2 = mockMvc.perform(post("/api/images")
                        .contentType(MediaType.IMAGE_PNG)
                        .content(data2))
                .andExpect(status().isOk())
                .andReturn();

        // make sure only two images are in database, with expected data and types
        List<Image> foundImages = imageMapper.findAll();
        assertEquals(foundImages.size(), 2);
        Image foundImage1 = foundImages.get(0);
        Image foundImage2 = foundImages.get(1);
        assertArrayEquals(foundImage1.getData(), data1);
        assertArrayEquals(foundImage2.getData(), data2);
        assertEquals(foundImage1.getType(), ImageType.JPEG);
        assertEquals(foundImage2.getType(), ImageType.PNG);

        // check returned IDs are correct
        String id1 = result1.getResponse().getContentAsString();
        String id2 = result2.getResponse().getContentAsString();
        assertEquals(id1, foundImage1.getImageId().toString());
        assertEquals(id2, foundImage2.getImageId().toString());

        // test sending no data will fail
        mockMvc.perform(post("/api/images")
                        .contentType(MediaType.IMAGE_PNG)
                        .content(new byte[]{}))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testUpdateImage() throws Exception {
        // insert image into database
        Image image = new Image(null, new byte[]{1,2,3}, ImageType.JPEG);
        imageMapper.insert(image);

        // update image using API
        byte[] newData = new byte[]{4,5,6};
        mockMvc.perform(put("/api/images/" + image.getImageId())
                        .contentType(MediaType.IMAGE_PNG)
                        .content(newData))
                .andExpect(status().isOk());

        // check image in database is updated
        List<Image> foundImages = imageMapper.findAll();
        assertEquals(foundImages.size(), 1);
        Image foundImage = foundImages.get(0);
        assertArrayEquals(foundImage.getData(), newData);
        assertEquals(foundImage.getType(), ImageType.PNG);

        // test invalid requests
        Long invalidId = image.getImageId() + 1;
        mockMvc.perform(put("/api/images/" + invalidId)
                        .contentType(MediaType.IMAGE_JPEG)
                        .content(new byte[]{1,2,3}))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/images/abc")
                        .contentType(MediaType.IMAGE_PNG)
                        .content(new byte[]{1,2,3}))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/images/" + image.getImageId())
                        .contentType(MediaType.IMAGE_JPEG)
                        .content(new byte[]{}))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testDeleteImage() throws Exception {
        // create and insert two images
        Image image1 = new Image(null, new byte[]{1,2,3}, ImageType.JPEG);
        Image image2 = new Image(null, new byte[]{4,5,6}, ImageType.PNG);
        imageMapper.insert(image1);
        imageMapper.insert(image2);

        // delete image2 using API
        mockMvc.perform(delete("/api/images/" + image2.getImageId()))
                .andExpect(status().isOk());

        // make sure only image1 is in database, and make sure it is correct
        List<Image> foundImages = imageMapper.findAll();
        assertEquals(foundImages.size(), 1);
        assertImageEquals(foundImages.get(0), image1);

        // test id not found
        mockMvc.perform(delete("/api/images/" + image2.getImageId() + 1))
                .andExpect(status().isNotFound());

        // test invalid id
        mockMvc.perform(delete("/api/images/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNotLoggedInAccess() throws Exception {
        // check all API endpoints return HTTP code 401 ("unauthorized")
        mockMvc.perform(get("/api/images/1").accept(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/images").contentType(MediaType.IMAGE_JPEG))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/images/1").contentType(MediaType.IMAGE_JPEG))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/images/1"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(roles = "USER")
    @Test
    void testUserAccess() throws Exception {
        // user without edit permission should only be allowed to get images (not add, modify or delete them)
        Image image = new Image(null, new byte[]{1,2,3}, ImageType.JPEG);
        imageMapper.insert(image);
        mockMvc.perform(get("/api/images/" + image.getImageId()).accept(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/images")
                        .contentType(MediaType.IMAGE_JPEG)
                        .content(new byte[]{4,5,6}))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/images/" + image.getImageId())
                        .contentType(MediaType.IMAGE_JPEG)
                        .content(new byte[]{4,5,6}))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/images/" + image.getImageId()))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "EDITOR")
    @Test
    void testEditorAccess() throws Exception {
        // user with edit permission should be allowed to access all API endpoints for images
        Image image = new Image(null, new byte[]{1,2,3}, ImageType.JPEG);
        imageMapper.insert(image);
        mockMvc.perform(get("/api/images/" + image.getImageId()).accept(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/images")
                        .contentType(MediaType.IMAGE_JPEG)
                        .content(new byte[]{4,5,6}))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/images/" + image.getImageId())
                        .contentType(MediaType.IMAGE_JPEG)
                        .content(new byte[]{4,5,6}))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/images/" + image.getImageId()))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testAdminAccess() throws Exception {
        // admin should be allowed to access all API endpoints for images
        Image image = new Image(null, new byte[]{1,2,3}, ImageType.JPEG);
        imageMapper.insert(image);
        mockMvc.perform(get("/api/images/" + image.getImageId()).accept(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/images")
                        .contentType(MediaType.IMAGE_JPEG)
                        .content(new byte[]{4,5,6}))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/images/" + image.getImageId())
                        .contentType(MediaType.IMAGE_JPEG)
                        .content(new byte[]{4,5,6}))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/images/" + image.getImageId()))
                .andExpect(status().isOk());
    }

    // create three test images and insert them into the database
    static List<Image> createAndInsertThreeImages(ImageMapper imageMapper) {
        Image image1 = new Image(1L, new byte[]{1,2,3}, ImageType.PNG);
        Image image2 = new Image(2L, new byte[]{4,5,6}, ImageType.JPEG);
        Image image3 = new Image(3L, new byte[]{7,8,9}, ImageType.PNG);
        imageMapper.insert(image1);
        imageMapper.insert(image2);
        imageMapper.insert(image3);
        return List.of(image1, image2, image3);
    }

    static void assertImageEquals(Image image1, Image image2) {
        assertEquals(image1.getImageId(),   image2.getImageId());
        assertArrayEquals(image1.getData(), image2.getData());
        assertEquals(image1.getType(),      image2.getType());
    }
}
