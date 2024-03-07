# 2023-ProjectPortfolio



This readme provides an overview of the project, scope, and key information for all stakeholders and team members involved.

* [Project Overview](#project-overview)
* [Technology Stack](#technology-stack)
* [Stakeholders](#stakeholders)
* [User Stories](#user-stories)
* [Product Requirements](#product-requirements)
* [Team Members](#team-members)
* [Kanban Board](#kanban-board)
* [Getting Started](#getting-started)

------

## Project Overview

This project is a collaboration with [Amdaris](https://amdaris.com/) to develop a comprehensive internal portfolio system that will showcase the wide array of innovative software solutions they have provided to their clients. Utilizing a modern technology stack comprising React.js for the frontend, Spring Boot for the backend, and MySQL for the database, our focus is to deliver a system that is as intuitive and user-friendly for non-technical staff as it is detailed and insightful for the technical teams.

The platform will feature secure login capabilities, versatile viewing options for case studies, and an administrative interface for managing content and user permissions. Emphasizing efficient and precise search functionality, we are implementing advanced search algorithms and indexing techniques to ensure stakeholders can swiftly locate and engage with relevant project data with unparalleled accuracy.

In essence, the system will serve as a dynamic repository, enabling stakeholders from the Management, Design, Development, Sales, and Marketing teams to interact with project data in a way that enhances understanding, promotes efficiency, and streamlines project management processes. This tool is not merely an archive but a reflection of Amdaris's commitment to innovation and excellence in their field. It is designed to adapt to the ever-evolving landscape of their business needs, ensuring that the impact of workforce changes is mitigated and that the company's legacy of projects is preserved and presented in a manner befitting their market-leading status.

------

## Technology Stack

**Frontend**: 
- [React.js](https://react.dev/)

**Backend**:
- [Spring Boot](https://spring.io/projects/spring-boot)

**Database**:
- [MySQL](https://www.mysql.com/)

------

## Stakeholders

----- **Consumers** -----

**Management Team**: These are primarily non-technical members who require an overview of all projects. This enables them to focus on a high-level perspective, become familiar with all projects, and improve overall management.

**Designers and Developers**: These are the technical team members who can utilize the system to gain insight into the internal workings of existing and future projects. This knowledge helps them communicate more effectively with one another, and it facilitates the onboarding process for new team members. Additionally, they may draw inspiration from colleagues' projects in terms of design and technology stack choices.

**Sales Team and Marketing Team**: These are outcome-focused non-technical members who rely on the system to promote products to potential clients, either in person or online. Therefore, they are particularly concerned with the search features and the accuracy of information related to completed projects.

----- **Authors** -----

**Designers and Developers**: These individuals are the creators of projects and are responsible for updating project information.

**Delivery Leads**: These are technical leaders who oversee project delivery. They require swift updates to the system to ensure efficient project management.

------

## User Stories

As a software solutions provider that delivers hundreds of projects to diverse clients with customized technological innovations, our company needs an internal portfolio system to effeciently showcase the our considerable projects.

The system should have an appealing and user-friendly interface, catering to both technical and non-technical staff. It should also facilitate easy and accurate searches, delivering relevant results and grouping combinations swiftly. Project information should be concise yet informative. Given its internal nature, data security is paramount, ensuring that only our company has access and can make modifications to keep records current. There should also be a way for certain users to manage other users, setting up the correct permissions for each user as required for their specific use of the platform.

By possessing this system, we can improve the recording and organizing of our projects, mitigate the negative impact of workforce changes, and enhance overall work efficiency.

**Management Team**: As part of the management team, I require that the system will allow me to easily see a high-level overview of each project the company has worked on and is currently working on.

**Designers and Developers**: As a designer or developer, I require that the system will allow me to quickly and easily create and edit case studies to include technical information about the project being undertaken at the company. I also require that I can easily search through and find this technical information in past projects, to draw inspiration and see how previous projects were accomplished.

**Sales Team and Marketing Team**: As part of the sales team or marketing team, I require that the system will allow me to quickly search through case studies to find the relevant information needed, and to be able to easily see the outcomes of the previous projects.

**Delivery Leads**: As a delivery lead, I require that the system will allow me to swiftly make updates to the case studies / projects.

------

## Product Requirements

The full set of product requirements can be found in the documents branch.

Summary of requirements:
- Login
  - Allow users to log in using either custom login system or Microsoft 365 accounts
  - Provide option to create account, login and reset password
  - Accounts created need to be verified as being an employee of the company
- Viewing all case studies
  - Dashboard to view all projects / case studies
  - Summary of each project with key information
  - Search through and filter case studies
- Viewing individual case study
  - Show information about case study / project
  - Button to edit if have permission
- Creating case study
  - Blank template to fill out
  - Upload images
  - Basic formatting options
  - Buttons to publish and save as draft
- Editing case studies:
  - Same options as creating case studies
  - Options to unpublish and save as new case study
- Admin system
  - Manage users (like setting user permissions)
  - Manage default case study template

------

## Team Members

| Members       | Email                                                 |
| ------------- | ----------------------------------------------------- |
| Daniel Lovell | [bp22800@bristol.ac.uk](mailto:bp22800@bristol.ac.uk) |
| Kaijian Liu   | [tx22513@bristol.ac.uk](mailto:tx22513@bristol.ac.uk) |
| Ningna Hu     | [ux21079@bristol.ac.uk](mailto:ux21079@bristol.ac.uk) |

Note: sorted alphabetically by UoB username

------

## Kanban Board
**[Kanban Board](https://github.com/orgs/spe-uob/projects/85/views/1)**

**[Gantt Chart](https://github.com/orgs/spe-uob/projects/85/views/3)**

------


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system. 

## **Frontend**

### Prerequisites 

Before you begin, ensure you have met the following requirements: 

- You have installed the latest version of [Node.js and npm](https://nodejs.org/en/).
- You have a Windows/Linux/Mac machine. 
- You have read [the project documentation](https://github.com/spe-uob/2023-ProjectPortfolio/tree/main). 

### Installing 

To install the project, follow these steps: 

**1. Clone the repository**: 

```
git clone https://github.com/spe-uob/2023-ProjectPortfolio.git
```

**2. Navigate to the frontend directory**:

```
cd ./frontend
```

**3. Install the required npm packages**:

```
npm install
```

**4. Start the development server**:

```
npm start
```

This will run the app in the development mode. Open [http://localhost:3000](http://localhost:3000/) to view it in your browser. The page will reload if you make edits. You will also see any lint errors in the console.

### Running the tests

Explain how to run the automated tests for this system:

```
npm test
```

### Linting

To ensure code quality and consistency, run the linter with the following command:

```
npm run lint
```

### Building for production

To build the app for production, use the following command:

```
npm run build
```

This builds the app for production to the `build` folder. It correctly bundles React in production mode and optimizes the build for the best performance.

Copy the contents of the `build/` folder to the `src/main/resources/static/` folder inside `backend` in the root of the repo.

### Further help

If you need further assistance, you can refer to the [React documentation](https://reactjs.org/), or file an issue in this repository.

## **Backend**

### Prerequisites

Before you begin, ensure you have met the following requirements: 

- You have installed Java JDK 21.
- You have installed the latest version of [MySQL](https://www.mysql.com/downloads/)
- You have a Windows/Linux/Mac machine. 
- You have read [the project documentation](https://github.com/spe-uob/2023-ProjectPortfolio/tree/main). 

### Installing

To install the project, follow these steps: 

**1. Clone the repository**: 

```
git clone https://github.com/spe-uob/2023-ProjectPortfolio.git
```

**2. Navigate to the backend directory**:

```
cd ./backend
```

**3. Start the database server**:

Start the MySQL server if it is not already running. Then connect to MySQL with root / administrator privileges. E.g. for MacOS and Linux:

```
sudo mysql
```

**4. Create the database**:

```
CREATE DATABASE ProjectPortfolio
```

**5. Create the database user**:

Create the user (changing *password* to your desired password):

```
GRANT ALL PRIVILEGES ON ProjectPortfolio.* TO 'ProjectPortfolioUser'@'localhost' IDENTIFIED BY 'password';
```

**6. Select database**:

```
use ProjectPortfolio
```

**7. Create the database tables**:

Source the SQL script to create all of the tables:

```
source ./src/main/resources/schema.sql
```

**8. Create admin user for website**:

Add a user record to the `User` table, with admin permissions (so you can log into the website and have full access) (replace *email* with your desired admin email address):

```
INSERT INTO User(Email, FirstName, LastName, Role, HasEditPermission, IsAdmin, Password) VALUES ('email', 'Admin', 'Admin', 'MANAGEMENT', 1, 1, '$2a$10$BlHKJin5z4F38bXeXNo21.seXK0T9vi1sxqRScOb6EP2pu.c7/f22');
```

Now exit the database prompt:

```
exit
```

**9. Start the server**:

```
./mvnw spring-boot:run
```

This will download the dependencies when ran for the first time, then it will run the server on [http://localhost:8080](http://localhost:8080/).

Now you can log in as the previously created admin user, using the admin email address you previously specified, and the password `password`.

### Running the tests

```
./mvnw test
```

### Building for production

First, make sure that the frontend code is built, and that the contents of the frontend `build/` folder is copied to the `src/main/resources/static/` folder.

To build the server for production, use the following command:

```
./mvnw clean package
```

This compiles and packages the server as a .jar in the `target` folder.

### Further help

If you need further assistance, you can refer to the [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/), or file an issue in this repository.


## **Docker**

This section provides guidance on how to set up and run the project using Docker, simplifying the installation and configuration process. Using Docker is not mandatory but highly recommended.

### Prerequisites

Before you begin, make sure you have Docker installed on your machine. If you haven't installed Docker yet, please follow the instructions here: [Get Docker](https://docs.docker.com/get-docker/).

### Running the Project with Docker

**1. Clone the Repository**

```
git clone https://github.com/spe-uob/2023-ProjectPortfolio.git
```

**2. Build the Docker Images**

Build the Docker images for the frontend and backend. This process can take a few minutes.

```
docker build -t frontend ./frontend
docker build -t backend ./backend
```

**3. Run the Containers**

Once the images are built, run the containers.

```
docker run -d -p 3000:3000 frontend
docker run -d -p 8080:8080 backend
```

Here, `-p` flags map the container ports to your local machine ports.

**4. Access the Application**

Once the containers are running, you can access the frontend application at `http://localhost:3000` and the backend at `http://localhost:8080`.

**5. Using Docker Compose (Optional)**

If you prefer using Docker Compose, you can run the entire application stack with a single command. Make sure the `docker-compose.yml` file is at the root of the project.

```
docker-compose up
```
