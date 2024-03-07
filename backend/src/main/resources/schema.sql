CREATE TABLE `User` (
    UserID INT NOT NULL AUTO_INCREMENT,
    Email VARCHAR(255) NOT NULL UNIQUE,
    FirstName VARCHAR(255) NOT NULL,
    LastName VARCHAR(255) NOT NULL,
    Role ENUM('MANAGEMENT', 'DESIGNER', 'DEVELOPER', 'SALES', 'MARKETING', 'DELIVERY_LEAD') NOT NULL,
    HasEditPermission BOOL NOT NULL,
    IsAdmin BOOL NOT NULL,
    Password VARCHAR(255) NOT NULL,
    PRIMARY KEY (UserID)
);

CREATE TABLE Image (
    ImageID INT NOT NULL AUTO_INCREMENT,
    Data LONGBLOB NOT NULL,
    Type ENUM('JPEG', 'PNG') NOT NULL,
    PRIMARY KEY (ImageID)
);

CREATE TABLE CaseStudy (
    CaseStudyID INT NOT NULL AUTO_INCREMENT,
    Title VARCHAR(255),
    ProjectStatus ENUM('ACTIVE', 'COMPLETED'),
    EditStatus ENUM('DRAFT', 'PUBLISHED'),
    ClientName VARCHAR(255),
    ClientLink VARCHAR(1024),
    ClientLogoID INT,
    Industry VARCHAR(255),
    ProjectType VARCHAR(255),
    StartDate DATE,
    EndDate DATE,
    Summary TEXT,
    TeamMembers TEXT,
    AdvanceLink VARCHAR(1024),
    ProblemDescription TEXT,
    SolutionDescription TEXT,
    Outcomes TEXT,
    ToolsUsed TEXT,
    ProjectLearnings TEXT,
    PRIMARY KEY (CaseStudyID),
    FOREIGN KEY (ClientLogoID) REFERENCES Image(ImageID)
);
