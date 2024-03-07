package spe.projectportfolio.backend.mapper;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import spe.projectportfolio.backend.mapper.ImageMapper;
import spe.projectportfolio.backend.pojo.Image;
import spe.projectportfolio.backend.pojo.enums.ImageType;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static spe.projectportfolio.backend.PojoAssertions.assertImageEquals;

@MybatisTest
class ImageMapperTests {
    @Autowired
    private ImageMapper imageMapper;

    @Test
    void testInsertFindDelete() {
        // create image and insert into database
        Image image1 = new Image(1L, new byte[]{1,2,3}, ImageType.PNG);
        Image image2 = new Image(2L, new byte[]{4,5,6}, ImageType.JPEG);
        Image image3 = new Image(3L, new byte[]{7,8,9}, ImageType.PNG);
        imageMapper.insert(image1);
        imageMapper.insert(image2);
        imageMapper.insert(image3);

        // test findAll
        List<Image> foundImages = imageMapper.findAll();
        assertEquals(foundImages.size(), 3);
        assertImageEquals(foundImages.get(0), image1);
        assertImageEquals(foundImages.get(1), image2);
        assertImageEquals(foundImages.get(2), image3);

        // test findById
        Image foundImage = imageMapper.findById(image3.getImageId());
        assertNotNull(foundImage);
        assertImageEquals(foundImage, image3);

        // test delete
        imageMapper.delete(image2.getImageId());
        foundImages = imageMapper.findAll();
        assertEquals(foundImages.size(), 2);
        assertImageEquals(foundImages.get(0), image1);
        assertImageEquals(foundImages.get(1), image3);
    }

    @Test
    void testUpdate() {
        // create image and insert it into database
        Image image = new Image(1L, new byte[]{1,2,3}, ImageType.JPEG);
        imageMapper.insert(image);

        // update image fields
        image.setData(new byte[]{4,5,6});
        image.setType(ImageType.PNG);

        // update image in database
        imageMapper.update(image);

        // fetch image from database and check if it was updated successfully
        Image foundImage = imageMapper.findById(image.getImageId());
        assertImageEquals(foundImage, new Image(image.getImageId(), new byte[]{4,5,6}, ImageType.PNG));
    }

    @Test
    void testDeleteByIds() {
        // create image and insert into database
        Image image1 = new Image(1L, new byte[]{1,2,3}, ImageType.PNG);
        Image image2 = new Image(2L, new byte[]{4,5,6}, ImageType.JPEG);
        Image image3 = new Image(3L, new byte[]{7,8,9}, ImageType.PNG);
        imageMapper.insert(image1);
        imageMapper.insert(image2);
        imageMapper.insert(image3);

        // delete two images
        List<Long> ids = Arrays.asList(image1.getImageId(), image3.getImageId());
        imageMapper.deleteByIds(ids);

        // check there is only image2 in database
        List<Image> foundImages = imageMapper.findAll();
        assertEquals(foundImages.size(), 1);
        assertImageEquals(foundImages.get(0), image2);
    }
}
