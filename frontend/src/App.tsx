import { Navigate, Route, Routes } from 'react-router'
import Layout from './components/Layout'
import AddProductPage from './pages/AddProductPage'
import ProductListPage from './pages/ProductListPage'

// URL → page mapping. Layout renders the header and tabs around every page.
function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/products" element={<ProductListPage />} />
        <Route path="/products/new" element={<AddProductPage />} />
        <Route path="*" element={<Navigate to="/products" replace />} />
      </Route>
    </Routes>
  )
}

export default App
