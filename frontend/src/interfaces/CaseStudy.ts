interface CaseStudy {
    caseStudyId: number;
    title?: string;
    projectStatus?: string;
    editStatus?: string;
    clientName?: string;
    clientLink?: string;
    clientLogoId?: number;
    industry?: string;
    projectType?: string;
    startDate?: Date;
    endDate?: Date;
    summary?: string;
    teamMembers?: string;
    advanceLink?: string;
    problemDescription?: string;
    solutionDescription?: string;
    outcomes?: string;
    toolsUsed?: string;
    projectLearnings?: string;
}

export default CaseStudy;