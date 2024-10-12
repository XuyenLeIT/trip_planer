import AdminLayout from "../layouts/AdminLayout"
import AuthLayout from "../layouts/AuthLayout"
import ProfileLayout from "../layouts/ProfileLayout"
import UserLayout from "../layouts/UserLayout"
import AboutPage from "../pages/AboutPage"

import Dashboard from "../pages/Dashboard"
import Home from "../pages/Home"
import Login from "../pages/Login"
import Profile from "../pages/Profile"
import Register from "../pages/Register"
import UserManagent from "../pages/UserManagent"

export const pulicRouters = [
    {
        path: '/',
        element: UserLayout,
        children: [
            { path: '', element: Home },
            { path: 'about', element: AboutPage }
        ]
    },
    {
        path: '/auth',
        element: AuthLayout,
        children: [
            { path: 'login', element: Login },
            { path: 'register', element: Register }
        ]
    }
]

export const privateRouters = [
    {
        path: '/dashboard',
        allowedRoles: ["ADMIN", "MANAGER"],
        element: AdminLayout,
        children: [
            { path: '', element: Dashboard },
            { path: 'user', element: UserManagent },
        ]
    },
    {
        path: '/user',
        allowedRoles: ["USER", "ADMIN", "MANAGER"],
        element: ProfileLayout,
        children: [
            { path: 'profile', element: Profile },
        ]
    },
]

