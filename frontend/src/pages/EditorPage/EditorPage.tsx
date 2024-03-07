// @ts-ignore
import React, {useState} from 'react';
import {
    TextField,
    Button,
    Box,
    Typography,
    Chip,
    Switch,
    Snackbar,
    AppBar,
    Toolbar,
    ThemeProvider, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, SnackbarCloseReason
} from '@mui/material';
import {LocalizationProvider} from '@mui/x-date-pickers/LocalizationProvider';
import {AdapterDayjs} from '@mui/x-date-pickers/AdapterDayjs';
import {DatePicker} from '@mui/x-date-pickers/DatePicker';
import dayjs from 'dayjs';
import 'dayjs/locale/en-gb';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import {styled} from '@mui/material/styles';
import {useNavigate} from 'react-router-dom';
// @ts-ignore
import theme from '../../components/theme.tsx';

// @ts-ignore
import config from '../../config.js';

// @ts-ignore
import RichTextField from '../../components/RichTextField/RichTextField.tsx';
// @ts-ignore
import CaseStudyPreview from './CaseStudyPreview.tsx';


function EditorPage() {
    const {API_URL} = config;

    const [title, setTitle] = useState('');
    const [industry, setIndustry] = useState('');
    const [projectType, setProjectType] = useState('');
    const [summary, setSummary] = useState('');
    const [teamMembers, setTeamMembers] = useState('');
    const [advanceLink, setAdvanceLink] = useState('');
    const [problemDescription, setProblemDescription] = useState('');
    const [solutionDescription, setSolutionDescription] = useState('');
    const [outcomes, setOutcomes] = useState('');
    const [toolsUsed, setToolsUsed] = useState('');
    const [projectLearnings, setProjectLearnings] = useState('');
    const [projectStatus, setProjectStatus] = useState('ACTIVE');
    const [editStatus, setEditStatus] = useState('DRAFT');
    const [startDate, setStartDate] = useState<Date | null>(null);
    const [endDate, setEndDate] = useState<Date | null>(null);
    const [clientName, setClientName] = useState('');
    const [clientLink, setClientLink] = useState('');

    const [clientLogoBase64, setClientLogoBase64] = useState('');
    const [clientLogoPreview, setClientLogoPreview] = useState('');

    const [openPublishSnackbar, setOpenPublishSnackbar] = useState(false);

    const navigate = useNavigate();

    // Handlers for state updates
    const handleTitleChange = (event) => setTitle(event.target.value);
    const handleIndustryChange = (event) => setIndustry(event.target.value);
    const handleProjectTypeChange = (event) => setProjectType(event.target.value);
    const handleSummaryChange = (event) => setSummary(event.target.value);
    const handleTeamMembersChange = (event) => setTeamMembers(event.target.value);
    const handleAdvanceLinksChange = (event) => setAdvanceLink(event.target.value);
    const handleProblemDescriptionChange = (content: string) => setProblemDescription(handleEmptyContent(content));
    const handleSolutionDescriptionChange = (content: string) => setSolutionDescription(handleEmptyContent(content));
    const handleOutcomesChange = (content: string) => setOutcomes(handleEmptyContent(content));
    const handleToolsUsedChange = (content: string) => setToolsUsed(handleEmptyContent(content));
    const handleProjectLearningsChange = (content: string) => setProjectLearnings(handleEmptyContent(content));
    const handleClientNameChange = (event) => setClientName(event.target.value);
    const handleClientLinkChange = (event) => setClientLink(event.target.value);

    // Return empty string if content is just p tag with br tag inside
    const handleEmptyContent = (content: string) => content === "<p><br></p>" ? "" : content;

    // Separate handlers for closing the Snackbars
    const handleClosePublishSnackbar = (event?: Event | React.SyntheticEvent<any, Event>, reason?: SnackbarCloseReason) => {
        if (reason === 'clickaway') return;
        setOpenPublishSnackbar(false);
    };

    const handleStartDateChange = (newValue: dayjs.Dayjs | null) => {
        setStartDate(newValue ? newValue.toDate() : null);
    };

    const handleEndDateChange = (newValue: dayjs.Dayjs | null) => {
        setEndDate(newValue ? newValue.toDate() : null);
    };

    const handleProjectStatusChange = () => {
        setProjectStatus(projectStatus === 'ACTIVE' ? 'COMPLETED' : 'ACTIVE'); // Toggle between 'COMPLETED' and 'ACTIVE'
    };

    const handleClientLogoChange = async (event) => {
        if (event.target.files.length > 0) {
            const file = event.target.files[0];
            const reader = new FileReader();

            reader.onload = async () => {
                if (typeof reader.result === 'string') {
                    const base64String = reader.result;
                    setClientLogoBase64(base64String);
                    setClientLogoPreview(URL.createObjectURL(file));
                }
            };

            reader.readAsDataURL(file);
        }
    };


    // Submit form data to the backend
    const handleSubmit = async (event, isDraft) => {
        event.preventDefault();
        setEditStatus(isDraft ? 'PUBLISHED' : 'DRAFT');
        const caseStudyId = 0;

        const formData = {
            caseStudyId,
            title,
            projectStatus,
            editStatus,
            clientName,
            clientLink,
            clientLogoBase64,
            industry,
            projectType,
            startDate,
            endDate,
            summary,
            teamMembers,
            advanceLink,
            problemDescription,
            solutionDescription,
            outcomes,
            toolsUsed,
            projectLearnings,
        };

        const apiEndpoint = `${API_URL}/api/case-studies`;


        try {
            const response = await fetch(apiEndpoint, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData),
            });

            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            } else {
                navigate('/home');
            }

            setOpenPublishSnackbar(true);
        } catch (error) {
            console.error('Submission failed', error);
            // Handle submission error here, e.g., show error message
        }
    };

    const VisuallyHiddenInput = styled('input')({
        clip: 'rect(0 0 0 0)',
        clipPath: 'inset(50%)',
        height: 1,
        overflow: 'hidden',
        position: 'absolute',
        bottom: 0,
        left: 0,
        whiteSpace: 'nowrap',
        width: 1,
    });

    const [openDiscard, setOpenDiscard] = useState(false)

    const handleDiscardOpen = () => {
        setOpenDiscard(true);
    };

    const handleDiscardClose = () => {
        setOpenDiscard(false);
    };

    return (
        <ThemeProvider theme={theme}>
            <AppBar position="static">
                <Toolbar>
                    <Typography variant="h6" component="div" sx={{flexGrow: 1}}>
                        Case Study Form
                    </Typography>
                    <Button variant="contained" onClick={handleDiscardOpen}
                            sx={{
                                marginLeft: 'auto',
                                backgroundColor: theme.palette.secondary.main,
                                '&:hover': {
                                    backgroundColor: theme.palette.secondary.dark,
                                }
                            }}>
                        Cancel
                    </Button>
                </Toolbar>
            </AppBar><Box display="flex" justifyContent="center" alignItems="start" p={2}>


            {/*Snackbar for Publish Success*/}
            <Snackbar
                open={openPublishSnackbar}
                autoHideDuration={6000}
                onClose={handleClosePublishSnackbar}
                message="Publish successful"
                anchorOrigin={{vertical: 'top', horizontal: 'center'}}
                action={<Button color="secondary" size="small" onClick={handleClosePublishSnackbar}>
                    CLOSE
                </Button>}
            />

            <Box component="form" maxWidth={400} mr={2}>
                <TextField
                    fullWidth
                    label="Title"
                    value={title}
                    onChange={handleTitleChange}
                    margin="normal"
                />

                <TextField
                    fullWidth
                    label="Client Name"
                    value={clientName}
                    onChange={handleClientNameChange}
                    margin="normal"
                />

                <TextField
                    fullWidth
                    label="Client Link"
                    value={clientLink}
                    onChange={handleClientLinkChange}
                    margin="normal"
                    sx={{mb: '24px'}}
                />

                <Button component="label" variant="contained" startIcon={<CloudUploadIcon/>}>
                    Upload Logo
                    <VisuallyHiddenInput type="file" onChange={handleClientLogoChange}/>
                </Button>

                <TextField
                    fullWidth
                    label="Industry"
                    value={industry}
                    onChange={handleIndustryChange}
                    margin="normal"
                    sx={{mt: '24px'}}
                />

                <TextField
                    fullWidth
                    label="Project Type"
                    value={projectType}
                    onChange={handleProjectTypeChange}
                    margin="normal"
                />

                <LocalizationProvider adapterLocale='en-gb' dateAdapter={AdapterDayjs}>
                    <Box sx={{mt: '16px'}}>
                        <DatePicker
                            label="Start Date"
                            value={startDate ? dayjs(startDate) : null}
                            onChange={(handleStartDateChange)}
                        />
                    </Box>
                </LocalizationProvider>

                <LocalizationProvider adapterLocale='en-gb' dateAdapter={AdapterDayjs}>
                    <Box sx={{mt: '24px', mb: '8px'}}>
                        <DatePicker
                            label="End Date"
                            value={endDate ? dayjs(endDate) : null}
                            onChange={(handleEndDateChange)}
                        />
                    </Box>
                </LocalizationProvider>

                <TextField
                    fullWidth
                    label="Summary"
                    value={summary}
                    onChange={handleSummaryChange}
                    margin="normal"
                    multiline
                    minRows={4}
                />

                <TextField
                    fullWidth
                    label="Team Members"
                    value={teamMembers}
                    onChange={handleTeamMembersChange}
                    margin="normal"
                />

                <TextField
                    fullWidth
                    label="Advance Link"
                    value={advanceLink}
                    onChange={handleAdvanceLinksChange}
                    margin="normal"
                />
                
                <RichTextField
                    value={problemDescription}
                    onChange={handleProblemDescriptionChange}
                    placeholder="Problem Description"
                    style={{marginTop: '16px', marginBottom: '8px'}}
                />

                <RichTextField
                    value={solutionDescription}
                    onChange={handleSolutionDescriptionChange}
                    placeholder="Solution Description"
                    style={{marginTop: '16px', marginBottom: '8px'}}
                />

                <RichTextField
                    value={outcomes}
                    onChange={handleOutcomesChange}
                    placeholder="Outcomes"
                    style={{marginTop: '16px', marginBottom: '8px'}}
                />

                <RichTextField
                    value={toolsUsed}
                    onChange={handleToolsUsedChange}
                    placeholder="Tools Used"
                    style={{marginTop: '16px', marginBottom: '8px'}}
                />

                <RichTextField
                    value={projectLearnings}
                    onChange={handleProjectLearningsChange}
                    placeholder="Project Learnings"
                    style={{marginTop: '16px', marginBottom: '8px'}}
                />

                <Box sx={{mt: '16px', mb: '16px'}}>
                    <Chip
                        label={editStatus ? 'COMPLETED' : 'ACTIVE'}
                        color={editStatus ? 'primary' : 'default'}
                        size="small"
                        onChange={handleProjectStatusChange}/>

                    <Switch
                        checked={projectStatus === 'COMPLETED'}
                        onChange={handleProjectStatusChange}/>
                </Box>

                <Button variant="contained" color="primary" onClick={(e) => handleSubmit(e, false)}
                        style={{marginRight: 16}}>
                    Publish
                </Button>

                <Button variant="contained" color="primary" onClick={(e) => handleSubmit(e, true)}>
                    Save as Draft
                </Button>


            </Box>

            <CaseStudyPreview caseStudyDetails={{title, clientName, clientLink, clientLogoPreview, industry, projectType, summary,
                teamMembers, advanceLink, problemDescription, solutionDescription, outcomes, toolsUsed, projectLearnings}} />

            {/* discard dialog */}
            <Dialog
                open={openDiscard}
                onClose={handleDiscardClose}
                aria-labelledby="alert-dialog-title"
                aria-describedby="alert-dialog-description"
            >
                <DialogTitle id="delete">
                    {"Discard Changes?"}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description" style={{color: 'darkgrey'}}>
                        This operation cannot be recoverd or restored.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleDiscardClose}>Cancel</Button>
                    <Button onClick={() => navigate('/home')} autoFocus>
                        Confirm
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
        </ThemeProvider>
    );
}

export default EditorPage;
