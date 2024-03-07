import * as React from 'react';
import {
    Box,
    TextField,
    Select,
    MenuItem,
    InputLabel,
    FormControl,
    Button,
    InputAdornment,
    Typography
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';

// @ts-ignore
import config from '../../config.js';
import User from '../../interfaces/User.js';

interface FilterSidebarProps {
    currentUser: { userId: any; };
    setRows: (arg0: any) => void;
    searchText: string;
    role: string;
    editPermission: string;
    admin: string;
    allFiltersEmpty: boolean;
    filterIsActive: boolean;
    numActiveFilters: number;
    setSearchText: (arg0: any) => void;
    setRole: (arg0: any) => void;
    setEditPermission: (arg0: any) => void;
    setAdmin: (arg0: any) => void;
    setAllFiltersEmpty: (arg0: any) => void;
    setFilterIsActive: (arg0: any) => void;
    setNumActiveFilters: (arg0: any) => void;
}

const FilterSidebar = (props: FilterSidebarProps) => {
    const { API_URL } = config;

    const handleKeyPress = (event) => {
        if (event.key === 'Enter') {
            search();
        }
    };

    const checkAllFiltersEmpty = (searchText: string, role: string, editPermission: string, admin: string) => {
        return searchText     === '' &&
               role           === '' &&
               editPermission === '' &&
               admin          === '';
    }

    const handleSearchTextChange = (event) => {
        let newSearchText = event.target.value;
        props.setSearchText(newSearchText);
        props.setAllFiltersEmpty(checkAllFiltersEmpty(newSearchText, props.role, props.editPermission, props.admin));
    };

    const handleRoleChange = (event) => {
        let newRole = event.target.value === 'clear' ? '' : event.target.value;
        props.setRole(newRole);
        props.setAllFiltersEmpty(checkAllFiltersEmpty(props.searchText, newRole, props.editPermission, props.admin));
    };

    const handleEditPermissionChange = (event) => {
        let newEditPermission = event.target.value === 'clear' ? '' : event.target.value;
        props.setEditPermission(newEditPermission);
        props.setAllFiltersEmpty(checkAllFiltersEmpty(props.searchText, props.role, newEditPermission, props.admin));
    };

    const handleAdminChange = (event) => {
        let newAdmin = event.target.value === 'clear' ? '' : event.target.value;
        props.setAdmin(newAdmin);
        props.setAllFiltersEmpty(checkAllFiltersEmpty(props.searchText, props.role, props.editPermission, newAdmin));
    };

    const getNumActiveFilters = () => {
        let numActive = 0;
        let filters = [props.searchText, props.role, props.editPermission, props.admin];

        for (let filter of filters) {
            if (filter !== '') numActive++;
        }

        return numActive;
    }

    const search = () => {
        // build query parameters
        const queryParams = new URLSearchParams();
        if (props.searchText) queryParams.append("search", props.searchText);
        if (props.role) queryParams.append("role", props.role);
        if (props.editPermission) queryParams.append("edit-permission", props.editPermission);
        if (props.admin) queryParams.append("admin", props.admin);

        // fetch using query parameters
        fetch(`${API_URL}/api/users?${queryParams.toString()}`, {
            method: 'GET',
            credentials: 'include'
        })
        .then(response => response.json())
        .then((data: User[]) => {
            // remove current user from list of users
            data = data.filter((user: User) => user.userId !== props.currentUser.userId);
            // set remaining list of users as the rows
            props.setRows(data);

            props.setFilterIsActive(true);
            props.setNumActiveFilters(getNumActiveFilters());
        })
        .catch(error => console.error('Error fetching data:', error));
    };

    const reset = () => {
        // clear search and filters
        props.setSearchText('');
        props.setRole('');
        props.setEditPermission('');
        props.setAdmin('');

        props.setAllFiltersEmpty(true);
        props.setFilterIsActive(false);
        props.setNumActiveFilters(0);

        // fetch all users
        fetch(`${API_URL}/api/users`, {
            method: 'GET',
            credentials: 'include'
        })
        .then(response => response.json())
        .then((data: User[]) => {
            // remove current user from list of users
            data = data.filter((user: User) => user.userId !== props.currentUser.userId);
            // set remaining list of users as the rows
            props.setRows(data);
        })
        .catch(error => console.error('Error fetching data:', error));
    }

    return (
        <Box padding={2} boxShadow={2} marginY={3}>
            <Typography variant="h6" component="div" sx={{ textAlign: 'center', fontWeight: 'bold', marginTop:2 }}>
                { "Filters" + (props.numActiveFilters === 0 ? "" : ` (${props.numActiveFilters} active)`) }
            </Typography>

            <FormControl fullWidth margin="normal">
                <TextField
                    label="Search"
                    variant="outlined"
                    placeholder="Email, First Name, Last Name, etc."
                    value={props.searchText}
                    onChange={handleSearchTextChange}
                    onKeyDown={handleKeyPress}
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                <SearchIcon />
                            </InputAdornment>
                        ),
                    }}
                />
            </FormControl>

            <FormControl fullWidth margin="normal">
                <InputLabel id="role-select-label">Role</InputLabel>
                <Select
                    labelId="role-select-label"
                    id="role-select"
                    value={props.role}
                    label="Role"
                    onChange={handleRoleChange}
                >
                    <MenuItem value="clear">-- Clear Selection --</MenuItem>
                    <MenuItem value="MANAGEMENT">Management</MenuItem>
                    <MenuItem value="DESIGNER">Designer</MenuItem>
                    <MenuItem value="DEVELOPER">Developer</MenuItem>
                    <MenuItem value="SALES">Sales</MenuItem>
                    <MenuItem value="MARKETING">Marketing</MenuItem>
                    <MenuItem value="DELIVERY_LEAD">Delivery Lead</MenuItem>
                </Select>
            </FormControl>

            <FormControl fullWidth margin="normal">
                <InputLabel id="editPermission-select-label">Has Edit Permission</InputLabel>
                <Select
                    labelId="editPermission-select-label"
                    id="editPermission-select"
                    value={props.editPermission}
                    label="Has Edit Permission"
                    onChange={handleEditPermissionChange}
                >
                    <MenuItem value="clear">-- Clear Selection --</MenuItem>
                    <MenuItem value="true">Yes</MenuItem>
                    <MenuItem value="false">No</MenuItem>
                </Select>
            </FormControl>

            <FormControl fullWidth margin="normal">
                <InputLabel id="admin-select-label">Is Admin</InputLabel>
                <Select
                    labelId="admin-select-label"
                    id="admin-select"
                    value={props.admin}
                    label="Is Admin"
                    onChange={handleAdminChange}
                >
                    <MenuItem value="clear">-- Clear Selection --</MenuItem>
                    <MenuItem value="true">Yes</MenuItem>
                    <MenuItem value="false">No</MenuItem>
                </Select>
            </FormControl>

            <Button
                variant="contained"
                color="primary"
                fullWidth
                onClick={search}
                disabled={props.allFiltersEmpty}
                sx={{ marginTop: '16px' }}
            >
                Search
            </Button>
            
            <Button
                variant="contained"
                color="primary"
                fullWidth
                onClick={reset}
                disabled={!props.filterIsActive}
                sx={{ marginTop: '16px' }}
            >
                Remove Filters
            </Button>
        </Box>
    );
};

export default FilterSidebar;
