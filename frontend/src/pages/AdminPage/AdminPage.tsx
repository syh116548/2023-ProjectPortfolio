import * as React from 'react';
import {useEffect, useState} from "react";
import {
    AppBar,
    Toolbar,
    Typography,
    Box,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow,
    Grid,
    TextField,
    Dialog,
    DialogActions,
    DialogContentText,
    DialogContent,
    DialogTitle,
    FormControl,
    MenuItem,
    Select,
    InputLabel,
    IconButton,
} from '@mui/material';
import CssBaseline from "@mui/material/CssBaseline";
import {ThemeProvider} from "@mui/material/styles";
import Checkbox from "@mui/material/Checkbox";
import Button from "@mui/material/Button";
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
// @ts-ignore
import theme from "../../components/theme.tsx";
// @ts-ignore
import FilterSidebar from "./FilterSidebar.tsx";
import FormControlLabel from "@mui/material/FormControlLabel";
import { useNavigate, useOutletContext } from 'react-router-dom';

import { ALLOWED_EMAIL_DOMAINS, hasAllowedEmailDomain, isValidEmailFormat } from '../../utils/EmailUtils.js';
import { isValidPassword } from '../../utils/PasswordUtils.js';
import config from '../../config.js';
import User from "../../interfaces/User.js";


export default function AdminPage() {
    const { API_URL } = config;
    const currentUser: User = useOutletContext<User>();
    const navigate = useNavigate();
    
    // Define the column structure
    const columns = [
        {id: 'email', label: 'Email'},
        {id: 'firstName', label: 'First Name'},
        {id: 'lastName', label: 'Last Name'},
        {id: 'role', label: 'Role'},
        {id: 'hasEditPermission', label: 'Has Edit Permission'},
        {id: 'isAdmin', label: 'Is Admin'},
    ];

    const [rows, setRows] = useState<User[]>([]);

    useEffect(() => {
        fetch(`${API_URL}/api/users`, {
            method: 'GET',
            credentials: 'include'
        })
        .then(response => response.json())
        .then((data: User[]) => {
            // remove current user from list of users
            data = data.filter((user: User) => user.userId !== currentUser.userId);
            // set remaining list of users as the rows
            setRows(data);
        })
        .catch(error => console.error('Error fetching data:', error));
    }, [API_URL, currentUser]);

    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [selectedRowId, setSelectedRowId] = useState(0);
    const [openDelete, setOpenDelete] = useState(false);
    const [openEditDialog, setOpenEditDialog] = useState(false);
    const [rowDataBeingEdited, setRowDataBeingEdited] = useState<User>({
        userId: 0,
        email: '',
        firstName: '',
        lastName: '',
        role: '',
        hasEditPermission: false,
        isAdmin: false
    });
    const [password, setPassword] = useState('');
    const [passwordConfirmation, setPasswordConfirmation] = useState('');

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

    // filter sidebar state
    const [searchText, setSearchText] = useState('');
    const [role, setRole] = useState('');
    const [editPermission, setEditPermission] = useState('');
    const [admin, setAdmin] = useState('');
    const [allFiltersEmpty, setAllFiltersEmpty] = useState(true);
    const [filterIsActive, setFilterIsActive] = useState(false);
    const [numActiveFilters, setNumActiveFilters] = useState(0);

    const fillTableWithAllUsers = () => {
        fetch(`${API_URL}/api/users`, {
            method: 'GET',
            credentials: 'include'
        })
        .then(response => response.json())
        .then((data: User[]) => {
            // remove current user from list of users
            data = data.filter((user: User) => user.userId !== currentUser.userId);
            // set remaining list of users as the rows
            setRows(data);
        })
        .catch(error => console.error('Error fetching data:', error));
    }

    const resetFilters = () => {
        setSearchText('');
        setRole('');
        setEditPermission('');
        setAdmin('');
        setAllFiltersEmpty(true);
        setFilterIsActive(false);
        setNumActiveFilters(0);
    }

    const resetTextFieldErrors = () => {
        setFirstNameValid(true);
        setLastNameValid(true);
        setRoleValid(true);
        setEmailValid(true);
        setPasswordValid(true);
        setPasswordsMatch(true);
    }

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

    const handleChangePage = (event: unknown, newPage: number) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
        setRowsPerPage(+event.target.value);
        setPage(0);
    };

// -----Add----------------------------------------------------------------------
    const [openAdd, setOpenAdd] = useState(false);
    const [newUser, setNewUser] = useState<User>({
        userId: 0,
        email: '',
        firstName: '',
        lastName: '',
        role: '',
        hasEditPermission: false,
        isAdmin: false
    });


    const handleAddClickOpen = () => {
        setNewUser({
            userId: 0,
            email: '',
            firstName: '',
            lastName: '',
            role: '',
            hasEditPermission: false,
            isAdmin: false
        });
        setPassword('');
        resetTextFieldErrors();
        setOpenAdd(true);
    };

    const handleAddClose = () => {
        setOpenAdd(false);
    };

    const handleAddInputChange = (e) => {
        const {name, value, type, checked} = e.target;
        resetTextFieldError(name);
        if (name === "password") setPassword(value);
        else if (name === "passwordConfirmation") setPasswordConfirmation(value);
        else {
            setNewUser(prevState => ({
                ...prevState,
                [name]: type === 'checkbox' ? checked : value
            }));
        }
    };


    const handleAddSave = () => {
        if (isValidUserData(newUser, true)) {
            // if admin, make sure edit permission is set to true as well
            if (newUser.isAdmin) {
                newUser.hasEditPermission = true;
            }

            const dataToSend = {...newUser, password: password};

            // post data to backend
            fetch(`${API_URL}/api/users`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(dataToSend),
            })
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    } else if (response.status === 409) {
                        // email already exists so display email error message
                        setEmailValid(false);
                        setEmailErrorMessage("An account with this email address already exists");
                        throw new Error("An account with this email address already exists");
                    } else if (response.status === 400) {
                        // details are invalid so throw error (this is if user gets past frontend checks)
                        throw new Error("Invalid user details.");
                    } else {
                        throw new Error('Network response was not ok.');
                    }
                })
                .then(data => {
                    // refresh table with all users and reset filters
                    fillTableWithAllUsers();
                    resetFilters();
                    handleAddClose();
                })
                .catch(error => {
                    console.error('Failed to create user:', error);
                });
        }
    };

//-----------------------------------------------------------------------------------

//-----Delete------------------------------------------------------------------------
    const handleDeleteClick = (rowId: number) => {
        setSelectedRowId(rowId);
        setOpenDelete(true);
    };

    const handleDeleteClose = () => {
        setOpenDelete(false);
    };

    const handleDelete = () => {
        setOpenDelete(false);

        fetch(`${API_URL}/api/users/${selectedRowId}`, {
            method: 'DELETE',
            credentials: 'include'
        }).then(responses => {
            // delete user from user list
            setRows(rows.filter(row => row.userId !== selectedRowId));
            setSelectedRowId(0);
        }).catch(error => {
            console.error('Error deleting users:', error);
        });
    };

//------------------------------------------------------------------------------------

//-----Edit---------------------------------------------------------------------------
    const handleEditClick = (rowId: number) => {
        const rowDataToEdit = rows.find(row => row.userId === rowId);
        if (rowDataToEdit) {
            setRowDataBeingEdited(rowDataToEdit);
            setPassword('');
            resetTextFieldErrors();
            setOpenEditDialog(true);
        }
    };

    const handleEditInputChange = (e) => {
        const {name, value, type, checked} = e.target;
        resetTextFieldError(name);
        if (name === "password") setPassword(value);
        else if (name === "passwordConfirmation") setPasswordConfirmation(value);
        else {
            setRowDataBeingEdited({
                ...rowDataBeingEdited,
                [name]: type === 'checkbox' ? checked : value
            });
        }
    };

    const handleSaveEdit = () => {
        if (isValidUserData(rowDataBeingEdited, false)) {
            // set password to null if password not provided
            let passwordToSend: string | null;
            if (password === '') {
                passwordToSend = null;
            } else {
                passwordToSend = password;
            }

            // if admin, make sure edit permission is set to true as well
            if (rowDataBeingEdited.isAdmin) {
                rowDataBeingEdited.hasEditPermission = true;
            }

            const dataToSend = {...rowDataBeingEdited, password: passwordToSend};

            // put data to backend
            fetch(`${API_URL}/api/users`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(dataToSend),
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
                // update user in user list
                setRows(rows.map(row => row.userId === updatedUser.userId ? updatedUser : row));
                setSelectedRowId(0);
                setOpenEditDialog(false);
            })
            .catch(error => {
                console.error('Failed to update user:', error);
            });
        }
    };


//------------------------------------------------------------------------------------

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline/>
            <Box sx={{display: 'flex', flexDirection: 'column', height: '100vh'}}>
                <AppBar position="static">
                    <Toolbar>
                        <Typography variant="h6" noWrap component="div" sx={{flexGrow: 1}}>
                            Admin Panel
                        </Typography>
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
                <Box sx={{flexGrow: 1, p: 3, textAlign: 'center'}}>
                    <Grid container spacing={2}>
                        {/* FilterSidebar will take full width on extra-small and small screens, and 4/12 of the width on medium screens and up */}
                        <Grid item xs={12} md={4}>
                            <FilterSidebar
                                setRows={setRows}
                                currentUser={currentUser}
                                searchText={searchText}
                                role={role}
                                editPermission={editPermission}
                                admin={admin}
                                allFiltersEmpty={allFiltersEmpty}
                                filterIsActive={filterIsActive}
                                numActiveFilters={numActiveFilters}
                                setSearchText={setSearchText}
                                setRole={setRole}
                                setEditPermission={setEditPermission}
                                setAdmin={setAdmin}
                                setAllFiltersEmpty={setAllFiltersEmpty}
                                setFilterIsActive={setFilterIsActive}
                                setNumActiveFilters={setNumActiveFilters}
                            />
                        </Grid>

                        <Grid item xs={12} md={8}>
                            <Paper sx={{width: '100%', overflow: 'hidden'}}>
                                <TableContainer sx={{maxHeight: '100%'}}>
                                    <Table stickyHeader aria-label="sticky table">
                                        <TableHead>
                                            <TableRow>
                                                {columns.map((column) => (
                                                    <TableCell
                                                        key={column.id}
                                                        align="center"
                                                    >
                                                        {column.label}
                                                    </TableCell>
                                                ))}
                                                <TableCell></TableCell>
                                                <TableCell></TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {rows
                                                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                                                .map((row) => {
                                                    return (
                                                        <TableRow key={row.userId}>
                                                            {columns.map((column) => {
                                                                const value = row[column.id];
                                                                return (
                                                                    <TableCell key={column.id} align="center">
                                                                        {typeof value === 'boolean' ? (value ? 'Yes' : 'No') : value}
                                                                    </TableCell>
                                                                );
                                                            })}
                                                            <TableCell align="center">
                                                                <IconButton onClick={() => handleEditClick(row.userId)}>
                                                                    <EditIcon color="primary"/>
                                                                </IconButton>
                                                            </TableCell>
                                                            <TableCell align="center">
                                                                <IconButton onClick={() => handleDeleteClick(row.userId)}>
                                                                    <DeleteIcon color="error" />
                                                                </IconButton>
                                                            </TableCell>
                                                        </TableRow>
                                                    );
                                                })}
                                        </TableBody>

                                    </Table>
                                </TableContainer>
                                <TablePagination
                                    rowsPerPageOptions={[10, 25, 100]}
                                    component="div"
                                    count={rows.length}
                                    rowsPerPage={rowsPerPage}
                                    page={page}
                                    onPageChange={handleChangePage}
                                    onRowsPerPageChange={handleChangeRowsPerPage}
                                />
                            </Paper>

                            {/*// Buttons----------------------------------------------------------------------*/}
                            <Box sx={{display: 'flex', justifyContent: 'flex-end', p: 2}}>
                                <Button
                                    variant="contained"
                                    color="primary"
                                    sx={{mr: 1}}
                                    onClick={handleAddClickOpen}
                                >
                                    Add New User
                                </Button>
                            </Box>

                        </Grid>
                    </Grid>
                </Box>

                {/*// Dialogs----------------------------------------------------------------------------------*/}

                {/* Add User Dialog */}
                <Dialog open={openAdd} onClose={handleAddClose}>
                    <DialogTitle>Add New User</DialogTitle>
                    <DialogContent>
                        <TextField
                            autoFocus
                            margin="dense"
                            name="email"
                            label="Email Address"
                            type="text"
                            fullWidth
                            onChange={handleAddInputChange}
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
                            onChange={handleAddInputChange}
                            required
                            error={!firstNameValid}
                            helperText={firstNameValid ? "" : firstNameErrorMessage}
                        />
                        <TextField
                            margin="dense"
                            name="lastName"
                            label="Last Name"
                            type="text"
                            fullWidth
                            onChange={handleAddInputChange}
                            required
                            error={!lastNameValid}
                            helperText={lastNameValid ? "" : lastNameErrorMessage}
                        />
                        <FormControl fullWidth margin="dense" required error={!roleValid}>
                            <InputLabel id="role-label">Role</InputLabel>
                            <Select
                                labelId="role-label"
                                id="role"
                                name="role"
                                value={newUser.role}
                                onChange={handleAddInputChange}
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
                            onChange={handleAddInputChange}
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
                            onChange={handleAddInputChange}
                            required
                            error={!passwordsMatch}
                            helperText={passwordsMatch ? "" : passwordsMatchErrorMessage}
                        />
                        <FormControlLabel
                            control={
                                <Checkbox
                                    name="hasEditPermission"
                                    color="primary"
                                    onChange={handleAddInputChange}
                                    checked={newUser ? (newUser.hasEditPermission || newUser.isAdmin) : false}
                                    disabled={newUser ? newUser.isAdmin : false}
                                />
                            }
                            label="Has Edit Permission"
                        />
                        <FormControlLabel
                            control={
                                <Checkbox
                                    name="isAdmin"
                                    color="primary"
                                    onChange={handleAddInputChange}
                                />
                            }
                            label="Is Admin"
                        />

                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleAddClose}>Cancel</Button>
                        <Button onClick={handleAddSave}>Save</Button>
                    </DialogActions>
                </Dialog>

                {/* Delete Dialog */}
                <Dialog
                    open={openDelete}
                    onClose={handleDeleteClose}
                    aria-labelledby="alert-dialog-title"
                    aria-describedby="alert-dialog-description"
                >
                    <DialogTitle id="delete">
                        {"Delete User?"}
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText id="alert-dialog-description" style={{ color: 'darkgrey' }}>
                            This operation cannot be recoverd or restored.
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleDeleteClose}>Cancel</Button>
                        <Button onClick={handleDelete} autoFocus>
                            Confirm
                        </Button>
                    </DialogActions>
                </Dialog>

                {/* Edit Dialog */}
                <Dialog open={openEditDialog} onClose={() => setOpenEditDialog(false)}>
                    <DialogTitle>Edit User</DialogTitle>
                    <DialogContent>
                        <TextField
                            autoFocus
                            margin="dense"
                            name="email"
                            label="Email"
                            type="email"
                            fullWidth
                            required
                            value={rowDataBeingEdited.email}
                            onChange={handleEditInputChange}
                            error={!emailValid}
                            helperText={emailValid ? "" : emailErrorMessage}
                        />
                        <TextField
                            margin="dense"
                            name="firstName"
                            label="First Name"
                            type="text"
                            fullWidth
                            required
                            value={rowDataBeingEdited.firstName}
                            onChange={handleEditInputChange}
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
                            value={rowDataBeingEdited.lastName}
                            onChange={handleEditInputChange}
                            error={!lastNameValid}
                            helperText={lastNameValid ? "" : lastNameErrorMessage}
                        />
                        <FormControl fullWidth margin="dense" required>
                            <InputLabel>Role</InputLabel>
                            <Select
                                value={rowDataBeingEdited.role}
                                onChange={handleEditInputChange}
                                label="Role"
                                name="role"
                            >
                                <MenuItem value="MANAGEMENT">Management</MenuItem>
                                <MenuItem value="DESIGNER">Designer</MenuItem>
                                <MenuItem value="DEVELOPER">Developer</MenuItem>
                                <MenuItem value="SALES">Sales</MenuItem>
                                <MenuItem value="MARKETING">Marketing</MenuItem>
                                <MenuItem value="DELIVERY_LEAD">Delivery Lead</MenuItem>
                            </Select>
                        </FormControl>
                        <TextField
                            margin="dense"
                            name="password"
                            label="Change Password"
                            type="password"
                            fullWidth
                            onChange={handleEditInputChange}
                            error={!passwordValid}
                            helperText={passwordValid ? "" : passwordErrorMessage}
                        />
                        {/* only show confirm password text field if password field has text in it */}
                        {(password !== '') &&
                        <TextField
                            margin="dense"
                            name="passwordConfirmation"
                            label="Confirm Password"
                            type="password"
                            fullWidth
                            required
                            onChange={handleEditInputChange}
                            error={!passwordsMatch}
                            helperText={passwordsMatch ? "" : passwordsMatchErrorMessage}
                        />}
                        <FormControlLabel
                            control={
                                <Checkbox
                                    name="hasEditPermission"
                                    checked={(rowDataBeingEdited.hasEditPermission || rowDataBeingEdited.isAdmin)}
                                    onChange={handleEditInputChange}
                                    disabled={rowDataBeingEdited.isAdmin}
                                />
                            }
                            label="Has Edit Permission"
                        />
                        <FormControlLabel
                            control={
                                <Checkbox
                                    name="isAdmin"
                                    checked={rowDataBeingEdited.isAdmin}
                                    onChange={handleEditInputChange}
                                />
                            }
                            label="Is Admin"
                        />
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setOpenEditDialog(false)}>Cancel</Button>
                        <Button onClick={handleSaveEdit}>Save</Button>
                    </DialogActions>
                </Dialog>
            </Box>
        </ThemeProvider>
    );
}
