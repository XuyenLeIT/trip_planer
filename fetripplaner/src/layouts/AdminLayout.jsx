import React from 'react';

function AdminLayout(props) {
    return (
        <div className='container'>
            <div className='row bg-success'>Header Layout Admin</div>

            <div className='row'>Content</div>

            <div className='row bg-info'>Footer Layout Admin</div>
        </div>
    );
}

export default AdminLayout;