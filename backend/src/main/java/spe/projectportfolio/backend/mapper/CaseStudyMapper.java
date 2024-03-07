package spe.projectportfolio.backend.mapper;

import org.apache.ibatis.annotations.*;
import spe.projectportfolio.backend.pojo.CaseStudy;

import java.util.List;

@Mapper
public interface CaseStudyMapper {
    List<CaseStudy> findAll();

    CaseStudy findById(Long id);

    List<CaseStudy> findByCondition(String title, String clientName, String industry);

    List<CaseStudy> findByGlobalSearch(String search);

    void insert(CaseStudy caseStudy);

    void update(CaseStudy caseStudy);

    void delete(Long id);

    void deleteByIds(List<Long> ids);

}
