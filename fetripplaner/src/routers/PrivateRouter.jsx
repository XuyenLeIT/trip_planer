import React from 'react';
import { Navigate } from 'react-router-dom';

function PrivateRouter({ element: Element, allowedRoles, userRole }) {
    if (!allowedRoles.includes(userRole)) {
        return <Navigate to="/auth/login" replace />
    }
    return <Element userRole={userRole} />
}

export default PrivateRouter;