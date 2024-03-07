
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';

import HomePage from './pages/HomePage/HomePage.tsx';
import ProjectPage from './pages/ProjectPage/ProjectPage.tsx';
import EditorPage from "./pages/EditorPage/EditorPage.tsx";
import LoginPage from "./pages/LoginPage/LoginPage.tsx";
import SignUpPage from "./pages/SignUpPage/SignUpPage.tsx"
import AdminPage from "./pages/AdminPage/AdminPage.tsx";
import ProjectEditorPage from "./pages/EditorPage/ProjectEditorPage.tsx";
import PasswordResetPage from "./pages/PasswordResetPage/PasswordResetPage.tsx";
import UserPage from "./pages/UserPage/UserPage.tsx";
import {AnonymousRoute, ProtectedRoute} from "./components/Route.tsx";
import EmailVerification from './pages/EmailVerification/EmailVerification.tsx';

function App() {
    return (
        <Router>
            <Routes>

                {/* pages restricted to non-logged-in users */}
                <Route element={<AnonymousRoute />}>
                    <Route path="/" element={<LoginPage />} />
                    <Route path="/register" element={<SignUpPage />} />
                    <Route path="/email-verification" element={<EmailVerification />} />
                    <Route path="/reset-password" element={<PasswordResetPage/>} />
                </Route>

                {/* pages restricted to any logged-in user */}
                <Route element={<ProtectedRoute role="user" />}>
                    <Route path="/home" element={<HomePage />} />
                    <Route path="/project" element={<ProjectPage />} />
                    <Route path="/user" element={<UserPage />} />
                </Route>

                {/* pages restricted to users with edit permissions */}
                <Route element={<ProtectedRoute role="editor" />}>
                    <Route path="/editor" element={<EditorPage />} />
                    <Route path="/project-editor" element={<ProjectEditorPage />} />
                </Route>

                {/* pages restricted to admins */}
                <Route element={<ProtectedRoute role="admin" />}>
                    <Route path="/admin" element={<AdminPage />} />
                </Route>

            </Routes>
        </Router>
    );
}

export default App;
