import * as React from 'react';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import {ThemeProvider } from '@mui/material/styles';
// @ts-ignore
import theme from '../../components/theme.tsx';
// @ts-ignore
import Copyright from '../../components/Copyright.tsx';
import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import config from '../../config.js';
import { Alert } from '@mui/material';



// TODO remove, this demo shouldn't need to reset the theme.
const defaultTheme = theme;


export default function PasswordResetEmailVerification() {
    const {API_URL} = config;

    const [email, setEmail] = useState('');
    const [verificationCode, setVerificationCode] = useState('');

    const [isSending, setIsSending] = useState(false);
    const [countdown, setCountdown] = useState(0);

    const [emailExists, setEmailExists] = useState(true);

    const navigate = useNavigate();
    
    const handleEmailInputChange = (event) => {
        setEmail(event.target.value);
    };

    const handleVerificationCodeInputChange = (event) => {
        setVerificationCode(event.target.value);
    };


    const checkEmailExists = async () => {
        const queryString = new URLSearchParams({
            email: email,
        }).toString();

        const url = `${API_URL}/api/users/exists?${queryString}`;

        try {
            const response = await fetch(url, {
                method: 'GET'
            });

            const data = await response.json();
            
            if (!data.exists) {
                setEmailExists(false);
                setIsSending(false);
                setCountdown(60);
            }

            return data.exists;

        } catch (error) {
            // Handle any errors
            console.error('Error checking if email already exists:', error);
        }
    }

    const handleSendVerificationCode = async () => {
        setEmailExists(true);

        const queryString = new URLSearchParams({
            email: email
        }).toString();

        const url = `${API_URL}/verify/send-email?${queryString}`;

        setIsSending(true);
        setCountdown(60);

        try {
            let emailDoesExist = await checkEmailExists();
            if (emailDoesExist) {
                await fetch(url, {
                    method: 'POST'
                });
            }
        } catch (error) {
            // Handle any errors
            console.error('Error sending verification code:', error);
        }
    }

    useEffect(() => {
        let intervalId;
        if (isSending && countdown > 0) {
            intervalId = setInterval(() => {
                setCountdown((countdown) => countdown - 1);
            }, 1000);
        } else if (countdown === 0) {
            setIsSending(false);
        }
        // Cleanup interval on unmount
        return () => clearInterval(intervalId);
    }, [isSending, countdown]);


    const handleVerifyCode = async () => {
        const queryString = new URLSearchParams({
            email: email,
            code: verificationCode
        }).toString();

        const url = `${API_URL}/verify/verify-code?${queryString}`;

        try {
            const response = await fetch(url, {
                method: 'POST'
            });

            const data = await response.json();
            // Handle the response data
            if (data.status === 200) {
                // Verification successful, redirect to password reset page
                navigate(`/reset-password?email=${encodeURIComponent(email)}&verCode=${verificationCode}`);
            } else {
                console.error('Verification failed:', data.message);
            }
        } catch (error) {
            // Handle any errors
            console.error('Error Verify verification code:', error);
        }
    }




    return (
        <ThemeProvider theme={defaultTheme}>
            <Grid container component="main" sx={{ height: '100vh' }}>
                <CssBaseline />
                <Grid
                    item
                    xs={false}
                    sm={4}
                    md={8}
                    sx={{
                        backgroundImage: 'url(https://source.unsplash.com/random?wallpapers)',
                        backgroundRepeat: 'no-repeat',
                        backgroundColor: (t) =>
                            t.palette.mode === 'light' ? t.palette.grey[50] : t.palette.grey[900],
                        backgroundSize: 'cover',
                        backgroundPosition: 'center',
                    }}
                />
                <Grid item xs={12} sm={8} md={4} component={Paper} elevation={6} square>
                    <Box
                        sx={{
                            pt: 8,
                            mx: 4,
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            minHeight: '100dvh'
                        }}
                    >

                        <Typography component="h1" variant="h5">
                           Email Verification
                        </Typography>

                        {!emailExists &&
                        <Alert severity="error" sx={{width: 1, mt: 2}}>
                            An account with that email address does not exist
                        </Alert>}

                        <Box sx={{ mt: 1, flex: 1 }}>

                            <TextField
                                autoFocus
                                margin="normal"
                                required
                                fullWidth
                                label="Email Address"
                                name="email"
                                autoComplete="email"
                                onChange={handleEmailInputChange}
                            />

                            {isSending &&
                            <TextField
                                margin="normal"
                                required
                                fullWidth
                                label="Verification Code"
                                onChange={handleVerificationCodeInputChange}
                            />}

                            <Button
                                type="button"
                                fullWidth
                                variant="contained"
                                sx={{ mt: 3, mb: 2 }}
                                onClick={handleSendVerificationCode}
                                disabled={isSending}
                            >
                                {isSending ? `Resend in ${countdown}s` : 'Send Verification Code'}
                            </Button>

                            <Button
                                type="submit"
                                fullWidth
                                variant="contained"
                                sx={{ mt: 3, mb: 2 }}
                                onClick={handleVerifyCode}
                            >
                                Confirm
                            </Button>

                        </Box>

                        <Copyright sx={{ mb: 4 }} />
                    </Box>
                </Grid>
            </Grid>
        </ThemeProvider>
    );
}