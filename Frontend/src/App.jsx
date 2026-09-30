import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';

import { AppProvider, useApp } from './context/AppContext';

import Sidebar          from './components/Sidebar';
import Navbar           from './components/Navbar';
import ToastContainer   from './components/ToastContainer';

// Auth Pages
import LoginPage        from './pages/LoginPage';
import RegisterPage     from './pages/RegisterPage';

// Admin Pages
import AdminDashboard   from './pages/admin/Dashboard';
import AdminUsers       from './pages/admin/Users';
import AdminVendors     from './pages/admin/Vendors';
import AdminProducts    from './pages/admin/Products';
import AdminCategories  from './pages/admin/Categories';
import AdminOrders      from './pages/admin/Orders';
import AdminPayments    from './pages/admin/Payments';
import AdminReturns     from './pages/admin/Returns';
import AdminReports     from './pages/admin/Reports';

// Vendor Pages
import VendorDashboard  from './pages/vendor/Dashboard';
import VendorProducts   from './pages/vendor/Products';
import VendorAddProduct from './pages/vendor/AddProduct';
import VendorInventory  from './pages/vendor/Inventory';
import VendorOrders     from './pages/vendor/Orders';
import VendorPayments   from './pages/vendor/Payments';

// Customer Pages
import CustomerHome     from './pages/customer/Home';
import CustomerProducts from './pages/customer/Products';
import ProductDetailPage from './pages/ProductDetailPage';
import CartPage         from './pages/CartPage';
import CustomerOrders   from './pages/OrdersPage';
import OrderDetailPage  from './pages/OrderDetailPage';
import CustomerWishlist from './pages/customer/Wishlist';
import CustomerProfile  from './pages/customer/Profile';

// Delivery Pages
import DeliveryDashboard from './pages/delivery/Dashboard';
import DeliveryAssigned  from './pages/delivery/AssignedOrders';

/* ── Auth guard ─────────────────────────────────── */
function RequireAuth({ children, allowedRoles }) {
  const { user } = useApp();
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    // Redirect to their respective dashboard if they try to access unauthorized route
    if (user.role === 'ADMIN') return <Navigate to="/admin/dashboard" replace />;
    if (user.role === 'VENDOR') return <Navigate to="/vendor/dashboard" replace />;
    if (user.role === 'DELIVERY') return <Navigate to="/delivery/dashboard" replace />;
    return <Navigate to="/customer/home" replace />;
  }

  return children;
}

/* ── App shell (sidebar + navbar + content) ─────── */
function AppShell() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { user } = useApp();

  return (
    <div className="app-layout">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="main-content">
        <Navbar onToggleSidebar={() => setSidebarOpen(o => !o)} sidebarOpen={sidebarOpen} />
        <Routes>
          {/* Admin Routes */}
          <Route path="/admin/dashboard"  element={<RequireAuth allowedRoles={['ADMIN']}><AdminDashboard /></RequireAuth>} />
          <Route path="/admin/users"      element={<RequireAuth allowedRoles={['ADMIN']}><AdminUsers /></RequireAuth>} />
          <Route path="/admin/vendors"    element={<RequireAuth allowedRoles={['ADMIN']}><AdminVendors /></RequireAuth>} />
          <Route path="/admin/products"   element={<RequireAuth allowedRoles={['ADMIN']}><AdminProducts /></RequireAuth>} />
          <Route path="/admin/categories" element={<RequireAuth allowedRoles={['ADMIN']}><AdminCategories /></RequireAuth>} />
          <Route path="/admin/orders"     element={<RequireAuth allowedRoles={['ADMIN']}><AdminOrders /></RequireAuth>} />
          <Route path="/admin/payments"   element={<RequireAuth allowedRoles={['ADMIN']}><AdminPayments /></RequireAuth>} />
          <Route path="/admin/returns"    element={<RequireAuth allowedRoles={['ADMIN']}><AdminReturns /></RequireAuth>} />
          <Route path="/admin/reports"    element={<RequireAuth allowedRoles={['ADMIN']}><AdminReports /></RequireAuth>} />
          <Route path="/admin/inventory"  element={<RequireAuth allowedRoles={['ADMIN']}><VendorInventory /></RequireAuth>} /> {/* Shared for now */}

          {/* Vendor Routes */}
          <Route path="/vendor/dashboard"   element={<RequireAuth allowedRoles={['VENDOR']}><VendorDashboard /></RequireAuth>} />
          <Route path="/vendor/products"    element={<RequireAuth allowedRoles={['VENDOR']}><VendorProducts /></RequireAuth>} />
          <Route path="/vendor/add-product" element={<RequireAuth allowedRoles={['VENDOR']}><VendorAddProduct /></RequireAuth>} />
          <Route path="/vendor/inventory"   element={<RequireAuth allowedRoles={['VENDOR']}><VendorInventory /></RequireAuth>} />
          <Route path="/vendor/orders"      element={<RequireAuth allowedRoles={['VENDOR']}><VendorOrders /></RequireAuth>} />
          <Route path="/vendor/payments"    element={<RequireAuth allowedRoles={['VENDOR']}><VendorPayments /></RequireAuth>} />

          {/* Customer Routes */}
          <Route path="/customer/home"      element={<RequireAuth allowedRoles={['CUSTOMER']}><CustomerHome /></RequireAuth>} />
          <Route path="/customer/products"  element={<RequireAuth allowedRoles={['CUSTOMER']}><CustomerProducts /></RequireAuth>} />
          <Route path="/customer/products/:id" element={<RequireAuth allowedRoles={['CUSTOMER', 'VENDOR', 'ADMIN']}><ProductDetailPage /></RequireAuth>} />
          <Route path="/customer/cart"      element={<RequireAuth allowedRoles={['CUSTOMER']}><CartPage /></RequireAuth>} />
          <Route path="/customer/orders"    element={<RequireAuth allowedRoles={['CUSTOMER']}><CustomerOrders /></RequireAuth>} />
          <Route path="/customer/orders/:id" element={<RequireAuth allowedRoles={['CUSTOMER']}><OrderDetailPage /></RequireAuth>} />
          <Route path="/customer/wishlist"  element={<RequireAuth allowedRoles={['CUSTOMER']}><CustomerWishlist /></RequireAuth>} />
          <Route path="/customer/profile"   element={<RequireAuth allowedRoles={['CUSTOMER', 'VENDOR', 'ADMIN', 'DELIVERY']}><CustomerProfile /></RequireAuth>} />
          
          {/* Aliases for old paths */}
          <Route path="/dashboard" element={<RequireAuth><RouteRedirector /></RequireAuth>} />
          <Route path="/products" element={<RequireAuth><RouteRedirector path="products" /></RequireAuth>} />
          <Route path="/products/:id" element={<RequireAuth><RouteRedirector path="products/:id" /></RequireAuth>} />
          <Route path="/cart" element={<RequireAuth><RouteRedirector path="cart" /></RequireAuth>} />
          <Route path="/orders" element={<RequireAuth><RouteRedirector path="orders" /></RequireAuth>} />
          <Route path="/orders/:id" element={<RequireAuth><RouteRedirector path="orders/:id" /></RequireAuth>} />
          <Route path="/inventory" element={<RequireAuth><RouteRedirector path="inventory" /></RequireAuth>} />
          <Route path="/payments" element={<RequireAuth><RouteRedirector path="payments" /></RequireAuth>} />
          <Route path="/categories" element={<RequireAuth><RouteRedirector path="categories" /></RequireAuth>} />

          {/* Delivery Routes */}
          <Route path="/delivery/dashboard" element={<RequireAuth allowedRoles={['DELIVERY']}><DeliveryDashboard /></RequireAuth>} />
          <Route path="/delivery/assigned"  element={<RequireAuth allowedRoles={['DELIVERY']}><DeliveryAssigned /></RequireAuth>} />

          {/* Catch All */}
          <Route path="*" element={<RequireAuth><RouteRedirector /></RequireAuth>} />
        </Routes>
      </div>
    </div>
  );
}

function RouteRedirector({ path = '' }) {
  const { user } = useApp();
  const location = useLocation();
  
  if (!user) return <Navigate to="/login" replace />;

  if (user.role === 'ADMIN') {
    if (path === 'dashboard' || path === '') return <Navigate to="/admin/dashboard" replace />;
    if (path === 'products') return <Navigate to="/admin/products" replace />;
    if (path === 'orders') return <Navigate to="/admin/orders" replace />;
    if (path === 'inventory') return <Navigate to="/admin/inventory" replace />;
    if (path === 'payments') return <Navigate to="/admin/payments" replace />;
    if (path === 'categories') return <Navigate to="/admin/categories" replace />;
    return <Navigate to="/admin/dashboard" replace />;
  }
  
  if (user.role === 'VENDOR') {
    if (path === 'dashboard' || path === '') return <Navigate to="/vendor/dashboard" replace />;
    if (path === 'products') return <Navigate to="/vendor/products" replace />;
    if (path === 'orders') return <Navigate to="/vendor/orders" replace />;
    if (path === 'inventory') return <Navigate to="/vendor/inventory" replace />;
    if (path === 'payments') return <Navigate to="/vendor/payments" replace />;
    return <Navigate to="/vendor/dashboard" replace />;
  }
  
  if (user.role === 'DELIVERY') {
    if (path === 'dashboard' || path === '') return <Navigate to="/delivery/dashboard" replace />;
    return <Navigate to="/delivery/dashboard" replace />;
  }

  // Default to Customer
  if (path === 'dashboard' || path === '') return <Navigate to="/customer/home" replace />;
  if (path === 'products') return <Navigate to="/customer/products" replace />;
  if (path === 'cart') return <Navigate to="/customer/cart" replace />;
  if (path === 'orders') return <Navigate to="/customer/orders" replace />;
  if (path.startsWith('products/')) return <Navigate to={`/customer/${path}`} replace />;
  if (path.startsWith('orders/')) return <Navigate to={`/customer/${path}`} replace />;
  
  return <Navigate to="/customer/home" replace />;
}

/* ── Root ────────────────────────────────────────── */
function AppRoutes() {
  const { user } = useApp();
  return (
    <>
      <ToastContainer />
      <Routes>
        <Route path="/login"    element={user ? <RouteRedirector /> : <LoginPage    />} />
        <Route path="/register" element={user ? <RouteRedirector /> : <RegisterPage />} />
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
