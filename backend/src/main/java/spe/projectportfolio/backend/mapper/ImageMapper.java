package spe.projectportfolio.backend.mapper;

import org.apache.ibatis.annotations.*;
import spe.projectportfolio.backend.pojo.Image;

import java.util.List;

@Mapper
public interface ImageMapper {
    List<Image> findAll();

    Image findById(Long id);

    void insert(Image image);

    void update(Image image);

    void delete(Long id);

    void deleteByIds(List<Long> ids);
}
