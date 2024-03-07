import * as React from 'react';
import { useEffect, useState } from "react";
import {
    Container,
    Typography,
    Box,
    Paper,
    Grid,
    AppBar,
    Toolbar,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    TextField,
    DialogTitle,
    InputLabel,
    Select,
    MenuItem, FormControl
} from '@mui/material';
import { ThemeProvider } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";
import { useNavigate } from "react-router-dom";
// @ts-ignore
import theme from "../../components/theme.tsx";
import config from '../../config.js';
import User from "../../interfaces/User";
import { ALLOWED_EMAIL_DOMAINS, hasAllowedEmailDomain, isValidEmailFormat } from '../../utils/EmailUtils.js';
import { isValidPassword } from '../../utils/PasswordUtils.js';

const { API_URL } = config;

const UserPage = () => {

    const navigate = useNavigate();

    const [password] = useState('');
    const [passwordConfirmation] = useState('');
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

    const [user, setUser] = useState<User>({
        userId: 0,
        email: '',
        firstName: '',
        lastName: '',
        role: '',
        hasEditPermission: false,
        isAdmin: false
    });

    const [editedUser, setEditedUser] = useState<User>({
        userId: 0,
        email: '',
        firstName: '',
        lastName: '',
        role: '',
        hasEditPermission: false,
        isAdmin: false
    });


    const [openEditDialog, setOpenEditDialog] = useState(false);
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
    const isValidUserData = (data: User, passwordIsRequired: boolean) => {
        const email = data.email;
        const emptyFieldErrorMessage = "Cannot be empty";
        const userIsChangingPassword = (password !== "") && (password !== null);
        let dataIsValid = true;

        // check firstname, lastname and role
        if (data.firstName === "") {
            setFirstNameValid(false);
            setFirstNameErrorMessage(emptyFieldErrorMessage);
            dataIsValid = false;
        } else setFirstNameValid(true);
        if (data.lastName === "") {
            setLastNameValid(false);
            setLastNameErrorMessage(emptyFieldErrorMessage);
            dataIsValid = false;
        } else setLastNameValid(true);
        if (data.role === "") {
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
        if (userIsChangingPassword || passwordIsRequired) {
            if (!isValidPassword(password, setPasswordErrorMessage)) {
                setPasswordValid(false);
                dataIsValid = false;
            } else setPasswordValid(true);
            if (password !== passwordConfirmation) {
                setPasswordsMatch(false);
                setPasswordsMatchErrorMessage("Passwords do not match");
                dataIsValid = false;
            } else setPasswordsMatch(true);
        } else {
            setPasswordValid(true);
            setPasswordsMatch(true);
        }

        return dataIsValid;
    }

    useEffect(() => {
        fetch(`${API_URL}/api/users/current`, {
            method: 'GET',
            credentials: 'include',
        })
            .then(response => response.json())
            .then(data => {
                setUser(data);
                setEditedUser(data); // Set editedUser state for editing
            })
            .catch(error => console.error('Error fetching user data:', error));
    }, []);

    // Handlers for edit dialog
    const handleEditOpen = () => {
        setOpenEditDialog(true);
    };

    const handleEditClose = () => {
        setOpenEditDialog(false);
    };


    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        resetTextFieldError(name);
        setEditedUser(prevState => ({
            ...prevState,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleEditSave = () => {
        if (isValidUserData(editedUser, password !== '')) {

            // if admin, make sure edit permission is set to true as well
            const updatedUser = {
                ...editedUser,
                hasEditPermission: null,
                isAdmin: null,
                password: password === '' ? null : password // Set password to null if not changing
            };

            // Send PUT request to backend
            fetch(`${API_URL}/api/users/current`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(updatedUser),
            })
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    } else if (response.status === 409) {
                        // email already exists so display email error message
                        setEmailValid(false);
                        setEmailErrorMessage("An account with this email address already exists");
                    } else if (response.status === 400) {
                        // details are invalid so throw error (this is if user gets past frontend checks)
                        throw new Error("Invalid user details.");
                    } else {
                        throw new Error('Network response was not ok.');
                    }
                })
                .then(updatedUser => {
                    // update the user details on the page
                    setUser(updatedUser);
                })
                .catch(error => {
                    console.error('Failed to update user:', error);
                });
        }
        handleEditClose();
    };


    // When the component mounts, fetch the user data
    useEffect(() => {
        fetch(`${API_URL}/api/users/current`, {
            method: 'GET',
            credentials: 'include',
        })
            .then(response => response.json())
            .then(data => {
                setUser(data);
                setEditedUser(data); // Set the user data for editing
            })
            .catch(error => console.error('Error fetching user data:', error));
    }, []);

    if (!user) {
        return <Typography>Loading user information...</Typography>;
    }

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline/>
            <Box sx={{display: 'flex', flexDirection: 'column', height: '100vh'}}>
                <AppBar position="static">
                    <Toolbar>
                        <Typography variant="h6" noWrap component="div" sx={{flexGrow: 1}}>
                            User Profile
                        </Typography>
                        <Button variant="contained"
                                onClick={handleEditOpen}
                                sx={{
                                    marginLeft: 2,
                                    backgroundColor: theme.palette.secondary.main,
                                    '&:hover': {
                                        backgroundColor: theme.palette.secondary.dark,
                                    }
                                }}>
                            Edit
                        </Button>
                        <Button variant="contained"
                                onClick={() => navigate('/home')}
                                sx={{
                                    marginLeft: 2,
                                    backgroundColor: theme.palette.secondary.main,
                                    '&:hover': {
                                        backgroundColor: theme.palette.secondary.dark,
                                    }
                                }}>
                            Home
                        </Button>
                    </Toolbar>
                </AppBar>
                <Container component="main" maxWidth="md">
                    <Box sx={{ marginTop: 8, display: 'flex', flexDirection: 'column', alignItems: 'center', }}>
                        <Paper variant="outlined" sx={{ mt: 3, p: 2, width: '100%' }}>
                            <Grid container spacing={2}>

                                <Grid item xs={12}>
                                    <Typography><b>First Name: </b> {user.firstName}</Typography>
                                </Grid>
                                <Grid item xs={12}>
                                    <Typography><b>Last Name: </b> {user.lastName}</Typography>
                                </Grid>
                                <Grid item xs={12}>
                                    <Typography><b>Email: </b> {user.email}</Typography>
                                </Grid>
                                <Grid item xs={12}>
                                    <Typography><b>Role: </b> {user.role}</Typography>
                                </Grid>

                            </Grid>
                        </Paper>
                    </Box>
                </Container>

                {/* Edit Dialog */}
                <Dialog open={openEditDialog} onClose={() => setOpenEditDialog(false)}>
                    <DialogTitle>Edit User</DialogTitle>
                    <DialogContent>
                        <TextField
                            autoFocus
                            margin="dense"
                            name="email"
                            label="Email Address"
                            type="text"
                            fullWidth
                            value={editedUser.email}
                            onChange={handleInputChange}
                            required
                            error={!emailValid}
                            helperText={emailValid ? "" : emailErrorMessage}
                        />

                        <TextField
                            margin="dense"
                            name="firstName"
                            label="First Name"
                            type="text"
                            fullWidth
                            value={editedUser.firstName}
                            onChange={handleInputChange}
                            error={!firstNameValid}
                            helperText={firstNameValid ? "" : firstNameErrorMessage}
                        />

                        <TextField
                            margin="dense"
                            name="lastName"
                            label="Last Name"
                            type="text"
                            fullWidth
                            required
                            value={editedUser.lastName}
                            onChange={handleInputChange}
                            error={!lastNameValid}
                            helperText={lastNameValid ? "" : lastNameErrorMessage}
                        />
                        <FormControl fullWidth margin="dense" required error={!roleValid}>
                            <InputLabel id="role-label">Role</InputLabel>
                            <Select
                                labelId="role-label"
                                id="role"
                                name="role"
                                value={editedUser.role}
                                onChange={handleInputChange}
                                label="Role"
                            >
                                <MenuItem value={'MANAGEMENT'}>Management</MenuItem>
                                <MenuItem value={'DESIGNER'}>Designer</MenuItem>
                                <MenuItem value={'DEVELOPER'}>Developer</MenuItem>
                                <MenuItem value={'SALES'}>Sales</MenuItem>
                                <MenuItem value={'MARKETING'}>Marketing</MenuItem>
                                <MenuItem value={'DELIVERY_LEAD'}>Delivery Lead</MenuItem>
                            </Select>
                        </FormControl>
                        <TextField
                        margin="dense"
                        name="password"
                        label="Password"
                        type="password"
                        fullWidth
                        onChange={handleInputChange}
                        required
                        error={!passwordValid}
                        helperText={passwordValid ? "" : passwordErrorMessage}
                        />

                        <TextField
                            margin="dense"
                            name="passwordConfirmation"
                            label="Confirm Password"
                            type="password"
                            fullWidth
                            onChange={handleInputChange}
                            required
                            error={!passwordsMatch}
                            helperText={passwordsMatch ? "" : passwordsMatchErrorMessage}
                        />


                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleEditClose}>Cancel</Button>
                        <Button onClick={handleEditSave}>Save</Button>
                    </DialogActions>
                </Dialog>
            </Box>
        </ThemeProvider>
    );
};

export default UserPage;
