import * as React from 'react';
import {
    Container,
    Paper,
    Typography,
    Box,
    AppBar,
    Toolbar,
    ThemeProvider,
    Dialog, DialogActions, DialogContentText, DialogTitle, DialogContent,
} from '@mui/material';
// @ts-ignore
import theme from '../../components/theme.tsx';
import Button from "@mui/material/Button";
// @ts-ignore
import CaseStudyDetails from "./CaseStudyDetails.tsx";
import CssBaseline from "@mui/material/CssBaseline";
// @ts-ignore
import Footer from "../../components/Footer.tsx";
import {useLocation, useNavigate, useOutletContext} from "react-router-dom";
import {useState} from "react";

// @ts-ignore
import config from '../../config.js';
import User from "../../interfaces/User.js";
import CaseStudy from '../../interfaces/CaseStudy.js';

const ProjectPage: React.FC = () => {
    const { API_URL } = config;
    const navigate = useNavigate();
    const currentUser: User = useOutletContext<User>();
    const location = useLocation();
    const caseStudy: CaseStudy = location.state?.caseStudy;

    const [openDelete, setOpenDelete] = useState(false);
    const handleDeleteOpen = () => {
        setOpenDelete(true);
    };
    const handleDeleteClose = () => {
        setOpenDelete(false);
    };
    const handleDelete = async () => {
        setOpenDelete(false);
        try {
            const response = await fetch(`${API_URL}/api/case-studies/${caseStudy.caseStudyId}`, {
                method: 'DELETE',
                credentials: 'include',
            });
            if (response.ok) {
                navigate('/home');
            } else {
                alert("Failed to delete the case study!")
                console.error('Failed to delete the case study:', response.status);
            }
        } catch (error) {
            alert("Error")
            console.error('Error:', error);
        }
    };

    if (!caseStudy) {
        alert("No Case Study Data Available.")
    }

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline/>
            <Box sx={{
                display: 'flex',
                flexDirection: 'column',
                minHeight: '100vh',
            }}>
                <AppBar position="relative" sx={{backgroundColor: theme.palette.primary.main}}>
                    <Toolbar>
                        <Typography variant="h6" color="inherit" noWrap sx={{flexGrow: 1}}>
                            Project Details
                        </Typography>
                        {currentUser.hasEditPermission &&
                            <Button variant="contained" onClick={handleDeleteOpen} sx={{
                                marginLeft: 'auto',
                                ml: 2,
                                backgroundColor: theme.palette.error.main,
                                '&:hover': {
                                    backgroundColor: theme.palette.error.dark,
                                }
                            }}>
                                Delete
                            </Button>}

                        {currentUser.hasEditPermission &&
                            <Button variant="contained"
                                    onClick={() => navigate('/project-editor', {state: {caseStudy: caseStudy}})} sx={{
                                marginLeft: 'auto',
                                ml: 2,
                                backgroundColor: theme.palette.secondary.main,
                                '&:hover': {
                                    backgroundColor: theme.palette.secondary.dark,
                                }
                            }}>
                                Edit
                            </Button>}

                        <Button variant="contained"
                                onClick={() => navigate('/home')} sx={{
                            marginLeft: 'auto',
                            ml: 2,
                            backgroundColor: theme.palette.secondary.main,
                            '&:hover': {
                                backgroundColor: theme.palette.secondary.dark,
                            }
                        }}>
                            Home
                        </Button>

                    </Toolbar>
                </AppBar>

                <Container maxWidth="xl" sx={{
                    marginBottom: 6
                }}>
                    <Box sx={{display: 'flex', justifyContent: 'flex-start', padding: 2}}>
                    </Box>

                    <main>
                        {/* Main content */}
                        <Container maxWidth="lg">
                            <Box sx={{marginTop: 6, display: 'flex', flexDirection: 'row'}}>
                                {/* Main content */}
                                <Box sx={{flex: 1, backgroundColor: theme.palette.background.default, marginRight: 2}}>
                                    <CaseStudyDetails caseStudy={caseStudy}/>
                                </Box>

                                {/* Sidebar */}
                                <Box sx={{flex: 0.3, marginLeft: 4, maxWidth: 280}}>
                                    {caseStudy.summary &&
                                        <Paper elevation={0} sx={{padding: 2}}>
                                            <Typography variant="h2" gutterBottom>
                                                Summary
                                            </Typography>

                                            <Typography variant="body1">
                                                {caseStudy.summary}
                                            </Typography>
                                        </Paper>}
                                    {caseStudy.projectStatus &&
                                        <Paper elevation={0} sx={{padding: 2, marginTop: 3}}>
                                            <Typography variant="h2" gutterBottom>
                                                Project Status
                                            </Typography>
                                            <Typography variant="body1">
                                                {caseStudy.projectStatus === "ACTIVE" ? "Active" : "Completed"}
                                            </Typography>
                                        </Paper>}
                                    {(caseStudy.startDate || caseStudy.endDate) &&
                                        <Paper elevation={0} sx={{padding: 2, marginTop: 3}}>
                                            {caseStudy.startDate && <>
                                                <Typography variant="h2" gutterBottom>
                                                    Start Date
                                                </Typography>
                                                <Typography variant="body1">
                                                    {new Date(caseStudy.startDate).toLocaleDateString()}
                                                </Typography>
                                            </>}
                                            {caseStudy.endDate && <>
                                                <Typography variant="h2" gutterBottom sx={{mt: '20px'}}>
                                                    End Date
                                                </Typography>
                                                <Typography variant="body1">
                                                    {new Date(caseStudy.endDate).toLocaleDateString()}
                                                </Typography>
                                            </>}
                                        </Paper>}
                                </Box>
                            </Box>
                        </Container>
                    </main>
                </Container>
                <Footer/>

                {/*delete dialog*/}
                <Dialog
                    open={openDelete}
                    onClose={handleDeleteClose}
                    aria-labelledby="alert-dialog-title"
                    aria-describedby="alert-dialog-description"
                >
                    <DialogTitle id="delete">
                        {"Delete This Project?"}
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText id="alert-dialog-description" style={{color: 'darkgrey'}}>
                            This operation cannot be recoverd or restored.
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleDeleteClose}>Cancel</Button>
                        <Button onClick={handleDelete} autoFocus>
                            Confirm
                        </Button>
                    </DialogActions>
                </Dialog>
            </Box>
        </ThemeProvider>
    );
};

export default ProjectPage;
