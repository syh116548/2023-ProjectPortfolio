import * as React from 'react';
import AppBar from '@mui/material/AppBar';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import CssBaseline from '@mui/material/CssBaseline';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import {ThemeProvider} from '@mui/material/styles';
// @ts-ignore
import theme from '../../components/theme.tsx';
import {Avatar, IconButton, InputAdornment, MenuItem, Select, TextField} from "@mui/material";
import {useState, useEffect} from "react";
import SearchIcon from "@mui/icons-material/Search";
import {useNavigate, useOutletContext} from "react-router-dom";
// @ts-ignore
import Footer from "../../components/Footer.tsx";

import config from '../../config.js';
import User from '../../interfaces/User.js';
import CaseStudy from '../../interfaces/CaseStudy.js';


export default function HomePage() {
    const { API_URL } = config;
    const navigate = useNavigate();
    const currentUser: User = useOutletContext<User>();

    const [caseStudies, setCaseStudies] = useState<CaseStudy[]>([]);

    const handleProfileClick = () => {
        navigate('/user');
    };

    const [searchTerm, setSearchTerm] = useState('');
    const [filter, setFilter] = useState('general');

    const handleKeyPress = (event) => {
        if (event.key === 'Enter') {
            handleSearch();
        }
    };

    const handleSearch = () => {
        setCaseStudies([]);
        const queryParams = new URLSearchParams();


        if (filter === 'general' && searchTerm) {
            queryParams.append('search', searchTerm);
        } else if (searchTerm) {

            queryParams.append(filter, searchTerm);
        }

        fetch(`${API_URL}/api/case-studies?${queryParams.toString()}`, {
            method: 'GET',
            credentials: 'include'
        })
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error('Network response was not ok.');
            })
            .then(data => {
                setCaseStudies(data);
            })
            .catch(error => {
                console.error('Error fetching case studies:', error);
            });
    };

    useEffect(() => {
        fetch(`${API_URL}/api/case-studies`, {
            method: 'GET',
            credentials: 'include'
        })
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error('Network response was not ok.');
            })
            .then(data => setCaseStudies(data))
            .catch(error => console.error('Error fetching case studies:', error));
    }, [API_URL]);

    const handleAddNewProject = () => {
        navigate('/editor'); // navigate to editor
    };

    const handleLogout = () => {
        fetch(`${API_URL}/auth/logout`, {
            method: 'POST',
            credentials: 'include'
        })
            .then(async (response: Response) => {
                if (response.status === 200) {
                    navigate('/') // navigate to login page
                }
            });
    }

    // limit text to a certain length, and add an elipsis to the end if the text was shortened
    const limitText = (text: string, limit: number) => {
        if (text.length <= limit) {
            return text;
        } else {
            return text.substring(0, limit) + '...';
        }
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
                            Home Page
                        </Typography>
                        {currentUser.hasEditPermission &&
                            <Button variant="contained"
                                    onClick={handleAddNewProject}
                                    sx={{
                                        marginLeft: 'auto',
                                        backgroundColor: theme.palette.secondary.main,
                                        '&:hover': {
                                            backgroundColor: theme.palette.secondary.dark,
                                        }
                                    }}>
                                Add New Project
                            </Button>}
                        {currentUser.isAdmin &&
                            <Button variant="contained"
                                    onClick={() => navigate('/admin')}
                                    sx={{
                                        marginLeft: 2,
                                        backgroundColor: theme.palette.secondary.main,
                                        '&:hover': {
                                            backgroundColor: theme.palette.secondary.dark,
                                        }
                                    }}>
                                Manage Users
                            </Button>}
                        <Button variant="contained" onClick={handleLogout} sx={{
                            marginLeft: 2,
                            backgroundColor: theme.palette.secondary.main,
                            '&:hover': {
                                backgroundColor: theme.palette.secondary.dark,
                            }
                        }}>
                            Sign Out
                        </Button>
                        <IconButton onClick={handleProfileClick} sx={{ml: 2}}>
                            <Avatar>
                                {currentUser.firstName.charAt(0) + currentUser.lastName.charAt(0)}
                            </Avatar>
                        </IconButton>
                    </Toolbar>
                </AppBar>
                <main>
                    {/* Hero unit */}
                    <Box
                        sx={{
                            bgcolor: 'background.paper',
                            pt: 8,
                            pb: 6,
                        }}
                    >
                        <Container maxWidth="sm">
                            <Box sx={{display: 'flex', justifyContent: 'center'}}>
                                {/* Filter Selector */}
                                <Select
                                    value={filter}
                                    onChange={(e) => setFilter(e.target.value)}
                                    displayEmpty
                                    inputProps={{'aria-label': 'Without label'}}
                                    sx={{mr: 1}}
                                >
                                    <MenuItem value="general">General</MenuItem>
                                    <MenuItem value="title">Title</MenuItem>
                                    <MenuItem value="industry">Industry</MenuItem>
                                    <MenuItem value="client-name">Client Name</MenuItem>
                                    {/* other filters */}
                                </Select>

                                <TextField
                                    fullWidth
                                    variant="outlined"
                                    placeholder="Search for projects..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                    onKeyDown={handleKeyPress}

                                    size="medium"
                                    InputProps={{
                                        endAdornment: (
                                            <InputAdornment position="end">
                                                <IconButton onClick={handleSearch}>
                                                    <SearchIcon/>
                                                </IconButton>
                                            </InputAdornment>
                                        ),
                                        style: {
                                            height: '56px',
                                            fontSize: '1.5rem',
                                        },
                                    }}
                                    sx={{
                                        maxWidth: 'md',
                                        mx: 'auto',
                                        flexGrow: 1,
                                    }}
                                />
                            </Box>

                        </Container>
                    </Box>
                    <Container sx={{py: 8}} maxWidth="lg">

                        <Grid container spacing={4}>
                            {caseStudies.map((caseStudy) => (
                                <Grid item key={caseStudy.caseStudyId} xs={12} sm={6} md={4}>
                                    <Card
                                        sx={{height: '100%', display: 'flex', flexDirection: 'column', backgroundColor: '#f5f5f5'}}
                                    >
                                        <CardContent sx={{flexGrow: 1}}>
                                            <Box sx={{display: 'flex', flexDirection: 'row'}}>
                                                <Typography gutterBottom variant="h5" component="h2" sx={{flexGrow: 1}}>
                                                    {caseStudy.title}
                                                </Typography>
                                                {caseStudy.clientLogoId &&
                                                <Box sx={{ml: '10px'}}>
                                                    <img
                                                        src={`${API_URL}/api/images/` + caseStudy.clientLogoId}
                                                        alt="Client Logo"
                                                        style={{maxWidth: '80px'}}/>
                                                </Box>}
                                            </Box>
                                            <Typography variant="body2" sx={{mb: '8px'}}>
                                                <b>Client:</b> {caseStudy.clientName}
                                            </Typography>
                                            <Typography variant="body2" sx={{mb: '8px'}}>
                                                <b>Status:</b> {caseStudy.projectStatus === "ACTIVE" ? "Active" : "Completed"}
                                            </Typography>
                                            <Typography variant="body2" sx={{mb: '8px'}}>
                                                <b>Dates:</b> {caseStudy.startDate ? new Date(caseStudy.startDate).toLocaleDateString() : ''} - {caseStudy.endDate ? new Date(caseStudy.endDate).toLocaleDateString() : ''}
                                            </Typography>
                                            <Typography variant="body2">
                                                <b>Summary:</b> {caseStudy.summary ? limitText(caseStudy.summary, 200) : ''}
                                            </Typography>
                                        </CardContent>
                                        <CardActions>
                                            <Button
                                                size="small"
                                                onClick={() => navigate('/project', {state: {caseStudy: caseStudy}})}
                                            >
                                                View
                                            </Button>
                                            {currentUser.hasEditPermission &&
                                                <Button
                                                    size="small"
                                                    onClick={() => navigate('/project-editor', {state: {caseStudy: caseStudy}})}
                                                >
                                                    Edit
                                                </Button>}
                                        </CardActions>
                                    </Card>
                                </Grid>
                            ))}
                        </Grid>
                    </Container>
                </main>
                <Footer/>
            </Box>
        </ThemeProvider>
    );
};
