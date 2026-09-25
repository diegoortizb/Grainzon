import { NavLink, Outlet } from 'react-router'

// Shared page frame: title, tab bar, and the current page (rendered by <Outlet />).
function Layout() {
  return (
    <main>
      <h1>Products</h1>

      <nav className="tabs">
        {/* `end` keeps "All products" from also being active on /products/new */}
        <NavLink to="/products" end>
          All products
        </NavLink>
        <NavLink to="/products/new">Add product</NavLink>
      </nav>

      <Outlet />
    </main>
  )
}

export default Layout
