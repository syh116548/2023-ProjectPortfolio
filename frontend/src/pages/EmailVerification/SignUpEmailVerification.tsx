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


export default function SignUpEmailVerification(props: any) {
    const {API_URL} = config;

    const [verificationCode, setVerificationCode] = useState('');

    const [isSending, setIsSending] = useState(false);
    const [countdown, setCountdown] = useState(0);

    const [emailExists, setEmailExists] = useState(true);

    const navigate = useNavigate();

    const newUser = props.newUser;

    const email = newUser.email;


    const handleVerificationCodeInputChange = (event) => {
        setVerificationCode(event.target.value);
    };


    const handleSendVerificationCode = async () => {
        setEmailExists(true);

        const queryString = new URLSearchParams({
            email: email
        }).toString();

        const url = `${API_URL}/verify/send-email?${queryString}`;

        setIsSending(true);
        setCountdown(60);

        try {
            await fetch(url, {
                method: 'POST'
            });
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


    const sendRegistrationDetails = async () => {
        const signUpInfo = {
            ...newUser,
            verificationCode: verificationCode
        };

        fetch(`${API_URL}/auth/register`, {
            method: "POST",
            credentials: "include",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(signUpInfo)
        })
        .then((response: Response) => {
            if (response.ok) {
                // account registration was successful so navigate to the login page
                navigate("/");
            } else if (response.status === 409) {
                // email already exists so display email error message
                // setEmailValid(false);
                // setEmailErrorMessage("An account with this email address already exists");
                throw new Error("An account with this email address already exists");
            } else if (response.status === 400) {
                // details are invalid so throw error (this is if user gets past frontend checks)
                throw new Error("Invalid registration details.");
            } else {
                throw new Error("Network response was not ok.");
            }
        })
        .catch(error => {
            console.error('Failed to sign up:', error);
        });
    }


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
                // Verification successful, send registration details
                sendRegistrationDetails();
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
                                margin="normal"
                                required
                                fullWidth
                                label="Verification Code"
                                onChange={handleVerificationCodeInputChange}
                            />

                            <Button
                                type="button"
                                fullWidth
                                variant="contained"
                                sx={{ mt: 3, mb: 2 }}
                                onClick={handleSendVerificationCode}
                                disabled={isSending}
                            >
                                {isSending ? `Resend in ${countdown}s` : 'Resend Verification Code'}
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