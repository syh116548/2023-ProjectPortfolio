import * as React from 'react';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import Link from '@mui/material/Link';
import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Alert from '@mui/material/Alert'
import Typography from '@mui/material/Typography';
import {ThemeProvider } from '@mui/material/styles';
// @ts-ignore
import theme from '../../components/theme.tsx';
// @ts-ignore
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { useState } from 'react';
// @ts-ignore
import Copyright from '../../components/Copyright.tsx';
import { isValidEmailFormat } from '../../utils/EmailUtils.js';
// @ts-ignore
import config from '../../config.js';

const defaultTheme = theme;

export default function LoginPage() {
    const { API_URL } = config;
    const navigate = useNavigate();

    const [loginFailed, setLoginFailed] = useState(false);
    const [loginFailedMessage, setLoginFailedMessage] = useState('');
    const [emailValid, setEmailValid] = useState(true);
    const [emailErrorMessage, setEmailErrorMessage] = useState('');
    const [passwordValid, setPasswordValid] = useState(true);
    const [passwordErrorMessage, setPasswordErrorMessage] = useState('');

    const resetTextFieldError = (name: string) => {
        if (name === "username") setEmailValid(true);
        if (name === "password") setPasswordValid(true);
    }

    // checks if given login data is valid (in terms of format) and also sets the corresponding error messages
    // for the fields that are invalid
    const isValidCredentials = (data) => {
        const email = data.get("username");
        const password = data.get("password");
        const emptyFieldErrorMessage = "Cannot be empty";
        let dataIsValid = true;

        // check email address
        if (email === "") {
            setEmailValid(false);
            setEmailErrorMessage(emptyFieldErrorMessage);
            dataIsValid = false;
        } else if (!isValidEmailFormat(email)) {
            setEmailValid(false);
            setEmailErrorMessage("Invalid email address");
            dataIsValid = false;
        } else setEmailValid(true);

        // check password
        if (password === "") {
            setPasswordValid(false);
            setPasswordErrorMessage(emptyFieldErrorMessage);
            dataIsValid = false;
        } else setPasswordValid(true);

        return dataIsValid;
    }

    const handleInputChange = (e) => {
        const {name} = e.target;
        resetTextFieldError(name);
    };

    const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const data = new FormData(event.currentTarget);
        
        if (isValidCredentials(data)) {
            // send login credentials
            fetch(`${API_URL}/auth/login`, {
                method: "POST",
                credentials: "include",
                body: data
            })
            .then((response: Response) => {
                if (response.ok) {
                    // credentials are correct so navigate to home page
                    navigate("/home");
                } else if (response.status === 401) {
                    // credentials are incorrect so display login failure message
                    setLoginFailed(true);
                    setLoginFailedMessage("Incorrect email or password");
                } else {
                    throw new Error("Network response was not ok.");
                }
            })
            .catch(error => {
                console.error('Failed to log in:', error);
            });
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
                            Sign In
                        </Typography>

                        {loginFailed &&
                        <Alert severity="error" sx={{width: 1, mt: 2}}>
                            {loginFailedMessage}
                        </Alert>}

                        <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 1, flex: 1 }}>
                            <TextField
                                autoFocus
                                margin="normal"
                                fullWidth
                                id="email"
                                label="Email Address"
                                name="username"
                                autoComplete="email"
                                onChange={handleInputChange}
                                error={!emailValid}
                                helperText={emailValid ? "" : emailErrorMessage}
                            />
                            <TextField
                                margin="normal"
                                fullWidth
                                name="password"
                                label="Password"
                                type="password"
                                id="password"
                                autoComplete="current-password"
                                onChange={handleInputChange}
                                error={!passwordValid}
                                helperText={passwordValid ? "" : passwordErrorMessage}
                            />
                            <Button
                                type="submit"
                                fullWidth
                                variant="contained"
                                sx={{ mt: 3, mb: 2 }}
                            >
                                Sign In
                            </Button>
                            <Grid container>
                                <Grid item xs>
                                    <Link component={RouterLink} to="/email-verification" variant="body2">
                                        Forgot password?
                                    </Link>
                                </Grid>
                                <Grid item>
                                    <Link component={RouterLink} to="/register" variant="body2">
                                        Don't have an account? Sign up
                                    </Link>
                                </Grid>
                            </Grid>
                        </Box>

                        <Copyright sx={{ mb: 4 }} />
                    </Box>
                </Grid>
            </Grid>
        </ThemeProvider>
    );
}