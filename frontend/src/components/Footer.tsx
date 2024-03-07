// Footer.tsx
import * as React from 'react';
import Box from '@mui/material/Box';
import { Typography } from "@mui/material";
// @ts-ignore
import Copyright from "./Copyright.tsx";

const Footer: React.FC = () => {
    return (
        <Box sx={{ mt: 'auto' }}>
            <Box sx={{ bgcolor: 'background.paper', p: 6 }} component="footer">
                <Typography variant="h6" align="center" gutterBottom>
                    Footer
                </Typography>
                <Typography
                    variant="subtitle1"
                    align="center"
                    color="text.secondary"
                    component="p"
                >
                    Something here to give the footer a purpose!
                </Typography>
                <Copyright />
            </Box>
        </Box>
    );
};

export default Footer;
