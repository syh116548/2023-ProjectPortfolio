interface User {
    userId: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
    hasEditPermission: boolean;
    isAdmin: boolean;
}

export default User;