import React from 'react';

function ProfileLayout(props) {
    return (
        <div className='container'>
            <div className='row bg-success'>Header Layout Profile</div>

            <div className='row'>Content</div>

            <div className='row bg-info'>Footer Layout Profile</div>
        </div>
    );
}

export default ProfileLayout;