import * as React from 'react';
import { useEffect, useState } from 'react';
import { Navigate, Outlet } from 'react-router-dom';

async function fetchCurrentUser() {
    const response = await fetch("http://localhost:8080/api/users/current", {
        method: "GET",
        credentials: "include"
    });
    const text = await response.text();
    if (text === "") return null;
    const user = JSON.parse(text);
    return user;
}

function useCurrentUser() {
    const [currentUser, setCurrentUser] = useState();

    useEffect(() => {
        async function getCurrentUser() {
            const user = await fetchCurrentUser();
            setCurrentUser(user);
        }
        getCurrentUser();
    }, []);

    return currentUser;
}

function checkUserHasPermission(role, user) {
    if (role === "user") return true;
    if (role === "editor") return user.hasEditPermission || user.isAdmin;
    if (role === "admin") return user.isAdmin;
}

export function AnonymousRoute() {
    const currentUser = useCurrentUser();

    // if undefined then we are still waiting for current user to be fetched so display nothing
    if (currentUser === undefined) return <></>;

    // if not null then we are logged in so redirect to home page
    if (currentUser !== null) return <Navigate to="/home" />;

    // else return the page the user is trying to access
    return <Outlet />;
}

export function ProtectedRoute(props) {
    const currentUser = useCurrentUser();

    // if undefined then we are still waiting for current user to be fetched so display nothing
    if (currentUser === undefined) return <></>;

    // if null then we are not logged in so redirect to login page
    if (currentUser === null) return <Navigate to="/" />;

    const userHasPermission = checkUserHasPermission(props.role, currentUser);

    // if user has permission, return the page they are trying to access and give them their user details
    if (userHasPermission) return <Outlet context={currentUser} />

    // else redirect them to the home page (we already know at this point that they are logged in, so home page is a suitable location)
    else return <Navigate to="/home" />
}