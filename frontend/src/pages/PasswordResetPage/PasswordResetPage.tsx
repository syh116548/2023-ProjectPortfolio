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
import {useState} from "react";
import {useLocation, useNavigate} from 'react-router-dom';
import { isValidPassword } from '../../utils/PasswordUtils.js';

import config from '../../config.js';
import { IconButton, InputAdornment } from '@mui/material';
import {Visibility, VisibilityOff} from "@mui/icons-material";

// TODO remove, this demo shouldn't need to reset the theme.
const defaultTheme = theme;


export default function PasswordResetPage() {
    const {API_URL} = config;

    const location = useLocation();
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const queryParams = new URLSearchParams(location.search);
    const email = queryParams.get('email');
    const verificationCode = queryParams.get('verCode');

    const [passwordConfirmation, setPasswordConfirmation] = useState('');
    const [passwordValid, setPasswordValid] = useState(true);
    const [passwordErrorMessage, setPasswordErrorMessage] = useState('');
    const [passwordsMatch, setPasswordsMatch] = useState(true);
    const [passwordsMatchErrorMessage, setPasswordsMatchErrorMessage] = useState('');


    // Add state for toggling password visibility
    const [showPassword, setShowPassword] = useState(false);

    // Function to toggle password visibility
    const handleClickShowPassword = () => {
        setShowPassword(!showPassword);
    };

    // Function to handle mouse down event on the icon button
    const handleMouseDownPassword = (event) => {
        event.preventDefault();
    };


    const isValid = () => {
        let dataIsValid = true;
        if (!isValidPassword(password, setPasswordErrorMessage)) {
            setPasswordValid(false);
            dataIsValid = false;
        } else setPasswordValid(true);
        if (password !== passwordConfirmation) {
            setPasswordsMatch(false);
            setPasswordsMatchErrorMessage("Passwords do not match");
            dataIsValid = false;
        } else setPasswordsMatch(true);

        return dataIsValid;
    }


    const handleInputChange = (event) => {
        // Destructure the name and value from event.target
        const { name, value } = event.target;

        // Use the name to determine which state to set
        if (name === 'password') {
            setPassword(value);
        } else if (name === 'passwordConfirmation') {
            setPasswordConfirmation(value);
        }

        // Reset validation states
        setPasswordValid(true);
        setPasswordsMatch(true);
    };


    const handleResetPassword = async () => {
        // Construct the request payload
        const payload = {
            email: email,
            password: password,
            verificationCode: verificationCode
        };

        try {
            if (isValid()) {
                const response = await fetch(`${API_URL}/auth/reset-password`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(payload)
                });

                const data = await response.json();
                // Handle the response
                if (response.ok) {
                    // Navigate to login after password reset
                    navigate('/');
                } else {
                    // Handle errors, such as showing an alert or a message on the page
                    console.error('Failed to reset password:', data);
                }
            }
        } catch (error) {
            // Handle network errors
            console.error('Network error:', error);
        }
    };


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
                            Password Reset
                        </Typography>

                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            name="password"
                            label="New Password"
                            type={showPassword ? "text" : "password"}
                            onChange={handleInputChange}
                            error={!passwordValid}
                            helperText={passwordValid ? "" : passwordErrorMessage}
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton
                                            onClick={handleClickShowPassword}
                                            onMouseDown={handleMouseDownPassword}
                                            edge="end"
                                        >
                                            {showPassword ? <VisibilityOff /> : <Visibility />}
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            }}
                        />
                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            name="passwordConfirmation"
                            label="New password confirmation"
                            type={showPassword ? "text" : "password"}
                            onChange={handleInputChange}
                            error={!passwordsMatch}
                            helperText={passwordsMatch ? "" : passwordsMatchErrorMessage}
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton
                                            onClick={handleClickShowPassword}
                                            onMouseDown={handleMouseDownPassword}
                                            edge="end"
                                        >
                                            {showPassword ? <VisibilityOff /> : <Visibility />}
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            }}
                        />

                        <Button
                            onClick={handleResetPassword}
                            fullWidth
                            variant="contained"
                            sx={{ mt: 3, mb: 2 }}
                        >
                            Submit
                        </Button>

                        <Copyright sx={{ mb: 4 }} />
                    </Box>
                </Grid>
            </Grid>
        </ThemeProvider>
    );
}
