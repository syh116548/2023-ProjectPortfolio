package spe.projectportfolio.backend.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import spe.projectportfolio.backend.pojo.enums.EditStatus;
import spe.projectportfolio.backend.pojo.enums.ProjectStatus;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseStudy {
    private Long caseStudyId;
    private String title;
    private ProjectStatus projectStatus;
    private EditStatus editStatus;
    private String clientName;
    private String clientLink;
    private Long clientLogoId;
    private String industry;
    private String projectType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
    private String summary;
    private String teamMembers;
    private String advanceLink;
    private String problemDescription;
    private String solutionDescription;
    private String outcomes;
    private String toolsUsed;
    private String projectLearnings;
}