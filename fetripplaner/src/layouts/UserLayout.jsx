import React from 'react';

function UserLayout(props) {
    return (
        <div className='container'>
            <div className='row bg-success'>Header Layout User</div>

            <div className='row'>Content</div>

            <div className='row bg-info'>Footer Layout User</div>
        </div>
    );
}

export default UserLayout;