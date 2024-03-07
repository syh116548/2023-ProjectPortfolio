import * as React from 'react';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import Link from '@mui/material/Link';
import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import {ThemeProvider } from '@mui/material/styles';
// @ts-ignore
import theme from '../../components/theme.tsx';
// @ts-ignore
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { useState } from 'react';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import InputLabel from '@mui/material/InputLabel';
import FormControl from '@mui/material/FormControl';
// @ts-ignore
import Copyright from '../../components/Copyright.tsx';
import { ALLOWED_EMAIL_DOMAINS, hasAllowedEmailDomain, isValidEmailFormat } from '../../utils/EmailUtils.js';
import { isValidPassword } from '../../utils/PasswordUtils.js';
import config from '../../config.js';
import {IconButton, InputAdornment} from '@mui/material';
import {Visibility, VisibilityOff} from "@mui/icons-material";


// TODO remove, this demo shouldn't need to reset the theme.
const defaultTheme = theme;

export default function SignUpPage() {
    const {API_URL} = config;

    const navigate = useNavigate();

    const [passwordConfirmation, setPasswordConfirmation] = useState('');
    const [newUser, setNewUser] = useState({
        email: '',
        firstName: '',
        lastName: '',
        role: '',
        password: ''
    });
    const [firstNameValid, setFirstNameValid] = useState(true);
    const [firstNameErrorMessage, setFirstNameErrorMessage] = useState('');
    const [lastNameValid, setLastNameValid] = useState(true);
    const [lastNameErrorMessage, setLastNameErrorMessage] = useState('');
    const [roleValid, setRoleValid] = useState(true);
    const [emailValid, setEmailValid] = useState(true);
    const [emailErrorMessage, setEmailErrorMessage] = useState('');
    const [passwordValid, setPasswordValid] = useState(true);
    const [passwordErrorMessage, setPasswordErrorMessage] = useState('');
    const [passwordsMatch, setPasswordsMatch] = useState(true);
    const [passwordsMatchErrorMessage, setPasswordsMatchErrorMessage] = useState('');
    const [showPassword, setShowPassword] = useState(false);

    const handleClickShowPassword = () => {
        setShowPassword(!showPassword);
    };

    const handleMouseDownPassword = (event) => {
        event.preventDefault();
    };

    const resetTextFieldError = (name: string) => {
        if (name === "firstName") setFirstNameValid(true);
        if (name === "lastName") setLastNameValid(true);
        if (name === "role") setRoleValid(true);
        if (name === "email") setEmailValid(true);
        if (name === "password") setPasswordValid(true);
        if (name === "passwordConfirmation") setPasswordsMatch(true);
    }

    // checks if given user data is valid and also sets the corresponding error messages
    // for the fields that are invalid
    const isValidUserData = () => {
        const email = newUser.email;
        const password = newUser.password;
        const emptyFieldErrorMessage = "Cannot be empty";
        let dataIsValid = true;
        
        // check firstname, lastname and role
        if (newUser.firstName === "") {
            setFirstNameValid(false);
            setFirstNameErrorMessage(emptyFieldErrorMessage);
            dataIsValid = false;
        } else setFirstNameValid(true);
        if (newUser.lastName === "") {
            setLastNameValid(false);
            setLastNameErrorMessage(emptyFieldErrorMessage);
            dataIsValid = false;
        } else setLastNameValid(true);
        if (newUser.role === "") {
            setRoleValid(false);
            dataIsValid = false;
        } else setRoleValid(true);

        // check email address
        if (email === "") {
            setEmailValid(false);
            setEmailErrorMessage(emptyFieldErrorMessage);
            dataIsValid = false;
        } else if (!isValidEmailFormat(email)) {
            setEmailValid(false);
            setEmailErrorMessage("Invalid email address");
            dataIsValid = false;
        } else if (!hasAllowedEmailDomain(email)) {
            setEmailValid(false);
            setEmailErrorMessage("Email address must have one of the following domains: " + ALLOWED_EMAIL_DOMAINS.join(", "));
            dataIsValid = false;
        } else setEmailValid(true);

        // check password
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

    const emailAlreadyExists = async () => {
        const queryString = new URLSearchParams({
            email: newUser.email,
        }).toString();

        const url = `${API_URL}/api/users/exists?${queryString}`;

        try {
            const response = await fetch(url, {
                method: 'GET'
            });

            const data = await response.json();
            
            if (data.exists) {
                setEmailValid(false);
                setEmailErrorMessage("Email already exists");
            } else {
                setEmailValid(true);
            }

            return data.exists;
        } catch (error) {
            // Handle any errors
            console.error('Error checking if email already exists:', error);
        }
    }

    const handleInputChange = (e) => {
        const {name, value} = e.target;
        resetTextFieldError(name);
        if (name === "passwordConfirmation") setPasswordConfirmation(value);
        else {
            setNewUser({
                ...newUser,
                [name]: value
            });
        }
    };

    const handleSubmit = async () => {
        let emailExists = await emailAlreadyExists();
        if (!emailExists) {
            if (isValidUserData()) {
                navigate('/email-verification', {state: {newUser: newUser}});
            }
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
                            Sign Up
                        </Typography>

                        <Box sx={{ mt: 1, flex: 1 }}>
                            <TextField
                                autoFocus
                                margin="normal"
                                fullWidth
                                label="Email Address"
                                name="email"
                                autoComplete="email"
                                onChange={handleInputChange}
                                error={!emailValid}
                                helperText={emailValid ? "" : emailErrorMessage}
                            />
                            <TextField
                                margin="normal"
                                fullWidth
                                label="First Name"
                                name="firstName"
                                onChange={handleInputChange}
                                error={!firstNameValid}
                                helperText={firstNameValid ? "" : firstNameErrorMessage}
                            />
                            <TextField
                                margin="normal"
                                fullWidth
                                label="Last Name"
                                name="lastName"
                                onChange={handleInputChange}
                                error={!lastNameValid}
                                helperText={lastNameValid ? "" : lastNameErrorMessage}
                            />
                            <FormControl
                                margin="normal"
                                fullWidth
                                error={!roleValid}
                            >
                                <InputLabel id="role-label">Role</InputLabel>
                                <Select
                                    labelId="role-label"
                                    id="role"
                                    label="Role"
                                    name="role"
                                    value={newUser.role}
                                    onChange={handleInputChange}
                                >
                                    <MenuItem value={"MANAGEMENT"}>Management</MenuItem>
                                    <MenuItem value={"DESIGNER"}>Designer</MenuItem>
                                    <MenuItem value={"DEVELOPER"}>Developer</MenuItem>
                                    <MenuItem value={"SALES"}>Sales</MenuItem>
                                    <MenuItem value={"MARKETING"}>Marketing</MenuItem>
                                    <MenuItem value={"DELIVERY_LEAD"}>Delivery Lead</MenuItem>
                                </Select>
                            </FormControl>
                            <TextField
                                margin="normal"
                                fullWidth
                                name="password"
                                label="Password"
                                type={showPassword ? 'text' : 'password'}
                                onChange={handleInputChange}
                                error={!passwordValid}
                                helperText={passwordValid ? "" : passwordErrorMessage}
                                InputProps={{
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton
                                            aria-label="toggle password visibility"
                                            onClick={handleClickShowPassword}
                                            onMouseDown={handleMouseDownPassword}
                                        >
                                            {showPassword ? <VisibilityOff /> : <Visibility />}
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            }}
                                />

                            <TextField
                                margin="normal"
                                fullWidth
                                name="passwordConfirmation"
                                label="Confirm Password"
                                type={showPassword ? 'text' : 'password'}
                                onChange={handleInputChange}
                                error={!passwordsMatch}
                                helperText={passwordsMatch ? "" : passwordsMatchErrorMessage}
                                InputProps={{
                                    endAdornment: (
                                        <InputAdornment position="end">
                                            <IconButton
                                                aria-label="toggle password visibility"
                                                onClick={handleClickShowPassword}
                                                onMouseDown={handleMouseDownPassword}
                                            >
                                                {showPassword ? <VisibilityOff /> : <Visibility />}
                                            </IconButton>
                                        </InputAdornment>
                                    ),
                                }}
                            />

                            <Button
                                onClick={handleSubmit}
                                fullWidth
                                variant="contained"
                                sx={{ mt: 3, mb: 2 }}
                            >
                                Sign Up
                            </Button>
                            <Link component={RouterLink} to="/" variant="body2" sx={{textAlign: "center"}}>
                                <p>Already have an account? Log in</p>
                            </Link>
                        </Box>

                        <Copyright sx={{ mb: 4 }} />
                    </Box>
                </Grid>
            </Grid>
        </ThemeProvider>
    );
}