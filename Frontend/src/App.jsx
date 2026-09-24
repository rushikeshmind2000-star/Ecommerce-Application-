import { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import { AppProvider, useApp } from './context/AppContext';

import Sidebar          from './components/Sidebar';
import Navbar           from './components/Navbar';
import ToastContainer   from './components/ToastContainer';

import LoginPage        from './pages/LoginPage';
import RegisterPage     from './pages/RegisterPage';
import DashboardPage    from './pages/DashboardPage';
import ProductsPage     from './pages/ProductsPage';
import ProductDetailPage from './pages/ProductDetailPage';
import CartPage         from './pages/CartPage';
import OrdersPage       from './pages/OrdersPage';
import OrderDetailPage  from './pages/OrderDetailPage';
import InventoryPage    from './pages/InventoryPage';
import PaymentsPage     from './pages/PaymentsPage';
import CategoriesPage   from './pages/CategoriesPage';

/* ── Auth guard ─────────────────────────────────── */
function RequireAuth({ children }) {
  const { user } = useApp();
  if (!user) return <Navigate to="/login" replace />;
  return children;
}

/* ── App shell (sidebar + navbar + content) ─────── */
function AppShell() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  return (
    <div className="app-layout">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="main-content">
        <Navbar onToggleSidebar={() => setSidebarOpen(o => !o)} sidebarOpen={sidebarOpen} />
        <Routes>
          <Route path="/dashboard"        element={<DashboardPage    />} />
          <Route path="/products"         element={<ProductsPage     />} />
          <Route path="/products/:id"     element={<ProductDetailPage />} />
          <Route path="/cart"             element={<CartPage         />} />
          <Route path="/orders"           element={<OrdersPage       />} />
          <Route path="/orders/:id"       element={<OrderDetailPage  />} />
          <Route path="/inventory"        element={<InventoryPage    />} />
          <Route path="/payments"         element={<PaymentsPage     />} />
          <Route path="/categories"       element={<CategoriesPage   />} />
          <Route path="*"                 element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </div>
    </div>
  );
}

/* ── Root ────────────────────────────────────────── */
function AppRoutes() {
  const { user } = useApp();
  return (
    <>
      <ToastContainer />
      <Routes>
        <Route path="/login"    element={user ? <Navigate to="/dashboard" replace /> : <LoginPage    />} />
        <Route path="/register" element={user ? <Navigate to="/dashboard" replace /> : <RegisterPage />} />
        <Route path="/*"        element={<RequireAuth><AppShell /></RequireAuth>} />
      </Routes>
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppProvider>
        <AppRoutes />
      </AppProvider>
    </BrowserRouter>
  );
}
