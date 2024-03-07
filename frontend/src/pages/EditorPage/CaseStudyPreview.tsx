import { Box, Card, CardContent, Divider, Typography } from '@mui/material';
import * as React from 'react';
// @ts-ignore
import RichTextDisplay from '../../components/RichTextDisplay/RichTextDisplay.tsx';

interface CaseStudyPreviewProps {
    caseStudyDetails: {
        title: string;
        clientName: string;
        clientLink: string;
        clientLogoPreview: string;
        industry: string;
        projectType: string;
        summary: string;
        teamMembers: string;
        advanceLink: string;
        problemDescription: string;
        solutionDescription: string;
        outcomes: string;
        toolsUsed: string;
        projectLearnings: string;
    }
}

function CaseStudyPreview(props: CaseStudyPreviewProps) {
    return (
        <Card style={{width: 700, minHeight: 1500, whiteSpace: 'pre-wrap'}}>
            <CardContent>
                <Typography gutterBottom variant="h5" color={props.caseStudyDetails.title ? "textPrimary" : "textSecondary"} component="h2">
                    {props.caseStudyDetails.title || 'Title Here'}
                </Typography>
                <Divider sx={{my: 2}}/>
                <Typography variant="body2" color={props.caseStudyDetails.clientName ? "textPrimary" : "textSecondary"} component="div">
                    {props.caseStudyDetails.clientName ?
                    <div>
                        <Typography variant="h6">Client Name</Typography>
                        {props.caseStudyDetails.clientName}
                    </div>
                    : 'Client name here'}
                </Typography>
                <Divider sx={{my: 2}}/>
                <Typography variant="body2" color={props.caseStudyDetails.clientLink ? "textPrimary" : "textSecondary"} component="div">
                    {props.caseStudyDetails.clientLink ?
                    <div>
                        <Typography variant="h6">Client Link</Typography>
                        {props.caseStudyDetails.clientLink}
                    </div>
                    : 'Client link here'}
                </Typography>
                <Divider sx={{my: 2}}/>
                {props.caseStudyDetails.clientLogoPreview &&
                    <Box mt={2} mb={2}>
                        <Typography variant="h6">Client Logo</Typography>
                        <img src={props.caseStudyDetails.clientLogoPreview} alt="Client Logo" style={{maxWidth: '100%', height: 'auto'}}/>
                    </Box>}
                {!props.caseStudyDetails.clientLogoPreview &&
                    <Typography variant="body2" color="textSecondary" component="p">
                        Client logo here
                    </Typography>}
                <Divider sx={{my: 2}}/>
                <Typography variant="body2" color={props.caseStudyDetails.industry ? "textPrimary" : "textSecondary"} component="div">
                {props.caseStudyDetails.industry ?
                <div>
                    <Typography variant="h6">Industry</Typography>
                    {props.caseStudyDetails.industry}
                </div>
                : 'Industry here'}
                </Typography>
                <Divider sx={{my: 2}}/>
                <Typography variant="body2" color={props.caseStudyDetails.projectType ? "textPrimary" : "textSecondary"} component="div">
                    {props.caseStudyDetails.projectType ?
                    <div>
                        <Typography variant="h6">Project Type</Typography>
                        {props.caseStudyDetails.projectType}
                    </div>
                    : 'Project type here'}
                </Typography>
                <Divider sx={{my: 2}}/>
                <Typography variant="body2" color={props.caseStudyDetails.summary ? "textPrimary" : "textSecondary"} component="div">
                    {props.caseStudyDetails.summary ?
                    <div>
                        <Typography variant="h6">Summary</Typography>
                        {props.caseStudyDetails.summary}
                    </div>
                    : 'Summary here'}
                </Typography>
                <Divider sx={{my: 2}}/>
                <Typography variant="body2" color={props.caseStudyDetails.teamMembers ? "textPrimary" : "textSecondary"} component="div">
                    {props.caseStudyDetails.teamMembers ?
                    <div>
                        <Typography variant="h6">Team Members</Typography>
                        {props.caseStudyDetails.teamMembers}
                    </div>
                    : 'Team members here'}
                </Typography>
                <Divider sx={{my: 2}}/>
                <Typography variant="body2" color={props.caseStudyDetails.advanceLink ? "textPrimary" : "textSecondary"} component="div">
                    {props.caseStudyDetails.advanceLink ?
                    <div>
                        <Typography variant="h6">Advance Link</Typography>
                        {props.caseStudyDetails.advanceLink}
                    </div>
                    : 'Advance link here'}
                </Typography>
                <Divider sx={{my: 2}}/>
                <Typography variant="body2" color={props.caseStudyDetails.problemDescription ? "textPrimary" : "textSecondary"} component="div">
                    {props.caseStudyDetails.problemDescription ?
                    <div>
                        <Typography variant="h6">Problem Description</Typography>
                        <RichTextDisplay content={props.caseStudyDetails.problemDescription} />
                    </div>
                    : 'Problem description here'}
                </Typography>
                <Divider sx={{my: 2}}/>
                <Typography variant="body2" color={props.caseStudyDetails.solutionDescription ? "textPrimary" : "textSecondary"} component="div">
                    {props.caseStudyDetails.solutionDescription ?
                    <div>
                        <Typography variant="h6">Solution Description</Typography>
                        <RichTextDisplay content={props.caseStudyDetails.solutionDescription} />
                    </div>
                    : 'Solution description here'}
                </Typography>
                <Divider sx={{my: 2}}/>
                <Typography variant="body2" color={props.caseStudyDetails.outcomes ? "textPrimary" : "textSecondary"} component="div">
                    {props.caseStudyDetails.outcomes ?
                    <div>
                        <Typography variant="h6">Outcomes</Typography>
                        <RichTextDisplay content={props.caseStudyDetails.outcomes} />
                    </div>
                    : 'Outcomes here'}
                </Typography>
                <Divider sx={{my: 2}}/>
                <Typography variant="body2" color={props.caseStudyDetails.toolsUsed ? "textPrimary" : "textSecondary"} component="div">
                    {props.caseStudyDetails.toolsUsed ?
                    <div>
                        <Typography variant="h6">Tools Used</Typography>
                        <RichTextDisplay content={props.caseStudyDetails.toolsUsed} />
                    </div>
                    : 'Tools used here'}
                </Typography>
                <Divider sx={{my: 2}}/>
                <Typography variant="body2" color={props.caseStudyDetails.projectLearnings ? "textPrimary" : "textSecondary"} component="div">
                    {props.caseStudyDetails.projectLearnings ?
                    <div>
                        <Typography variant="h6">Project Learnings</Typography>
                        <RichTextDisplay content={props.caseStudyDetails.projectLearnings} />
                    </div>
                    : 'Project Learnings here'}
                </Typography>

            </CardContent>
        </Card>
    );
}

export default CaseStudyPreview;
