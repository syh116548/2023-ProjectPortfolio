// @ts-ignore
import * as React from 'react';
// @ts-ignore
import { Button, Container, CssBaseline, Typography, Box } from '@mui/material';
// @ts-ignore
import MicrosoftLogo from './Microsoft_logo.png';

function MicrosoftLogin() {
    const handleLogin = () => {
        console.log("Attempting to log in with Microsoft...");
    };

    return (
        <Container
            maxWidth={false}
            component="div"
            sx={{
                backgroundImage: 'url(https://source.unsplash.com/random?wallpapers)',
                backgroundRepeat: 'no-repeat',
                backgroundPosition: 'center center',
                backgroundSize:'cover',
                width: '100vw',
                height: '100vh',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',

            }}
        >
            <CssBaseline/>
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    backgroundColor: 'rgba(255,255,255,0.8)', //white background
                    padding: 4,
                    borderRadius: 2
                }}
            >
                <Typography component="h1" variant="h5">
                    Sign in
                </Typography>
                <Button
                    fullWidth
                    variant="outlined"
                    color="primary"
                    onClick={handleLogin}
                    startIcon={<img src={MicrosoftLogo} alt="Microsoft logo" style={{width: 24}}/>}
                    sx={{mt: 3}}
                >
                    Sign in with Microsoft
                </Button>
            </Box>
        </Container>
    );
}

export default MicrosoftLogin;
