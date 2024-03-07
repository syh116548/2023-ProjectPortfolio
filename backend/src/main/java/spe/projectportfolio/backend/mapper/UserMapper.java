package spe.projectportfolio.backend.mapper;

import org.apache.ibatis.annotations.*;

import spe.projectportfolio.backend.pojo.CaseStudy;
import spe.projectportfolio.backend.pojo.User;
import spe.projectportfolio.backend.pojo.enums.Role;

import java.util.List;

@Mapper
public interface UserMapper {
    List<User> findAll();

    User findById(Long id);

    User findByEmail(String email);

    List<User> findByCondition(String search, String email, String firstName, String lastName, Role role, Boolean editPermission, Boolean admin);

    void insert(User user);

    void update(User user);

    void delete(Long id);

    void deleteByIds(List<Long> ids);
}
