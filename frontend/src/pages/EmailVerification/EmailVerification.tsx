import * as React from 'react';
import SignUpEmailVerification from './SignUpEmailVerification.tsx';
import PasswordResetEmailVerification from './PasswordResetEmailVerification.tsx';
import {useLocation} from 'react-router-dom';


export default function EmailVerification() {

    const location = useLocation();
    const newUser = location.state?.newUser;

    if (newUser === null || newUser === undefined) {
        return (
            <PasswordResetEmailVerification />
        );
    } else {
        return (
            <SignUpEmailVerification newUser={newUser} />
        );
    }
}