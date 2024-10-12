import { useState } from 'react'
import { Route, Routes } from 'react-router-dom'
import { privateRouters, pulicRouters } from './routers/ListRouter'
import PrivateRouter from './routers/PrivateRouter'

function App() {
  const [count, setCount] = useState(0);
  const userRole = "USER";

  return (
    <>
      <Routes>
        {pulicRouters.map((route, index) => (
          <Route key={index} path={route.path} element={<route.element />}>
            {route.children && route.children.map((chilRoute, idx) => (
              <Route key={idx} path={chilRoute.path} element={<chilRoute.element />} />
            ))}
          </Route>
        ))}

        {privateRouters.map((route, index) => (
          <Route key={index} path={route.path} element={<PrivateRouter element={route.element} allowedRoles={route.allowedRoles} userRole={userRole} />}>

            {route.children && route.children.map((chilRoute, idx) => (
              <Route key={idx} path={chilRoute.path} element={<PrivateRouter element={route.element} allowedRoles={route.allowedRoles} userRole={userRole} />} />
            ))}
          </Route>
        ))}
      </Routes>
    </>
  )
}

export default App
