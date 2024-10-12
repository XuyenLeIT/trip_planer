import React from 'react';

function AuthLayout(props) {
    return (
        <div className='container'>
            <div className='row bg-success'>Header Layout Auth</div>

            <div className='row'>Content</div>

            <div className='row bg-info'>Footer Layout Auth</div>
        </div>
    );
}

export default AuthLayout;