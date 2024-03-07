import * as React from 'react';
import {Typography, Paper, Grid, Box, Link, Divider} from '@mui/material';
// @ts-ignore
import theme from '../../components/theme.tsx';
import CaseStudy from "../../interfaces/CaseStudy.js";
import config from '../../config.js';
// @ts-ignore
import RichTextDisplay from '../../components/RichTextDisplay/RichTextDisplay.tsx';


interface CaseStudyDetailsProps {
    caseStudy: CaseStudy;
}

const { API_URL } = config;

const CaseStudyDetails: React.FC<CaseStudyDetailsProps> = ({caseStudy}) => {
    return (
        <Paper elevation={0} sx={{
            backgroundColor: theme.palette.background.default,
            '& h4': {
                color: theme.palette.primary.main,
            },
            '& .subHeader': {
                color: theme.palette.secondary.main,
            },
            '& a': {
                color: theme.palette.info.main,
            },
        }}>
            <Typography variant="h1">
                {caseStudy.title}
            </Typography>
            <Divider style={{marginTop: '16px', marginBottom: '16px'}}/>
            {caseStudy.industry && <Typography gutterBottom>
                Industry: {caseStudy.industry}
            </Typography>}
            {caseStudy.projectType && <Typography gutterBottom>
                Project Type: {caseStudy.projectType}
            </Typography>}

            {caseStudy.clientName && <Grid item xs={12} sx={{my: 2}}>
                <Typography variant="h2">
                    Client
                </Typography>
                <Typography variant="body1" gutterBottom>
                    Name: {caseStudy.clientName}
                </Typography>
                {"Link: "}
                <Link href={caseStudy.clientLink}>
                    {caseStudy.clientLink}
                </Link>
            </Grid>}
                {caseStudy.clientLogoId && <Box my={2}>
                    <Box my={2} sx={{maxHeight: '100px', overflow: 'hidden'}}>
                        <img
                            src={`${API_URL}/api/images/` + caseStudy.clientLogoId}
                            alt="Client Logo"
                            style={{maxHeight: '100px', minHeight: '100px', width: 'auto'}}/>
                    </Box>
                </Box>}

                <Grid container sx={{whiteSpace: 'pre-wrap'}}>
                    {caseStudy.teamMembers && <Grid item xs={12} sx={{my: 2}}>
                        <Typography variant="h2">
                            Team Members
                        </Typography>
                        <Typography variant="body1" gutterBottom>
                            {caseStudy.teamMembers}
                        </Typography>
                    </Grid>}
                    {caseStudy.advanceLink && <Grid item xs={12} sx={{my: 2}}>
                        <Typography variant="h2">
                            Advance Link
                        </Typography>
                        <Link href={caseStudy.advanceLink}>
                            {caseStudy.advanceLink}
                        </Link>
                    </Grid>}
                    {caseStudy.problemDescription && <Grid item xs={12} sx={{my: 2}}>
                        <Typography variant="h2">
                            The Problem
                        </Typography>
                        <RichTextDisplay content={caseStudy.problemDescription} />
                    </Grid>}
                    {caseStudy.solutionDescription && <Grid item xs={12} sx={{my: 2}}>
                        <Typography variant="h2">
                            What We Did
                        </Typography>
                        <RichTextDisplay content={caseStudy.solutionDescription} />
                    </Grid>}
                    {caseStudy.outcomes && <Grid item xs={12} sx={{my: 2}}>
                        <Typography variant="h2">
                            The Outcomes
                        </Typography>
                        <RichTextDisplay content={caseStudy.outcomes} />
                    </Grid>}
                    {caseStudy.toolsUsed && <Grid item xs={12} sx={{my: 2}}>
                        <Typography variant="h2">
                            Tools Used
                        </Typography>
                        <RichTextDisplay content={caseStudy.toolsUsed} />
                    </Grid>}
                    {caseStudy.projectLearnings && <Grid item xs={12} sx={{my: 2}}>
                        <Typography variant="h2">
                            Project Learnings
                        </Typography>
                        <RichTextDisplay content={caseStudy.projectLearnings} />
                    </Grid>}
                </Grid>
            </Paper>
                );
            };

export default CaseStudyDetails;
