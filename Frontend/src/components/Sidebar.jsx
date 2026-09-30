import { useNavigate, useLocation } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import {
  LayoutDashboard, Package, ShoppingCart, ClipboardList,
  CreditCard, Warehouse, Users, LogOut, Store, Tag, ChevronRight, Plus
} from 'lucide-react';

export default function Sidebar({ open, onClose }) {
  const { user, logout, cartCount } = useApp();
  const navigate  = useNavigate();
  const location  = useLocation();

  const go = (path) => { navigate(path); onClose?.(); };

  const initials = user ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase() : 'U';

  const role = user?.role || 'CUSTOMER';

  let navItems = [];
  if (role === 'CUSTOMER') {
    navItems = [
      { label: 'Main', type: 'section' },
      { path: '/customer/home',      label: 'Home',       icon: LayoutDashboard },
      { path: '/customer/products',  label: 'Products',   icon: Package },
      { path: '/customer/cart',      label: 'Cart',       icon: ShoppingCart, badge: true },
      { path: '/customer/orders',    label: 'My Orders',  icon: ClipboardList },
      { path: '/customer/wishlist',  label: 'Wishlist',   icon: Tag },
      { path: '/customer/profile',   label: 'Profile',    icon: Users },
    ];
  } else if (role === 'VENDOR') {
    navItems = [
      { label: 'Vendor Portal', type: 'section' },
      { path: '/vendor/dashboard',  label: 'Dashboard',   icon: LayoutDashboard },
      { path: '/vendor/products',   label: 'My Products', icon: Package },
      { path: '/vendor/add-product',label: 'Add Product', icon: Plus },
      { path: '/vendor/inventory',  label: 'Inventory',   icon: Warehouse },
      { path: '/vendor/orders',     label: 'Orders',      icon: ClipboardList },
      { path: '/vendor/payments',   label: 'Payments',    icon: CreditCard },
      { path: '/customer/profile',  label: 'Profile',     icon: Users },
    ];
  } else if (role === 'ADMIN') {
    navItems = [
      { label: 'Admin Portal', type: 'section' },
      { path: '/admin/dashboard',  label: 'Dashboard',  icon: LayoutDashboard },
      { path: '/admin/users',      label: 'Users',      icon: Users },
      { path: '/admin/vendors',    label: 'Vendors',    icon: Store },
      { path: '/admin/products',   label: 'All Products', icon: Package },
      { path: '/admin/categories', label: 'Categories', icon: Tag },
      { path: '/admin/orders',     label: 'Orders',     icon: ClipboardList },
      { path: '/admin/payments',   label: 'Payments',   icon: CreditCard },
      { path: '/admin/inventory',  label: 'Inventory',  icon: Warehouse },
      { path: '/admin/returns',    label: 'Returns',    icon: Tag },
      { path: '/admin/reports',    label: 'Reports',    icon: Tag },
      { path: '/customer/profile', label: 'Profile',    icon: Users },
    ];
  } else if (role === 'DELIVERY') {
    navItems = [
      { label: 'Delivery Portal', type: 'section' },
      { path: '/delivery/dashboard',  label: 'Dashboard',  icon: LayoutDashboard },
      { path: '/delivery/assigned',   label: 'Assigned Orders', icon: Package },
      { path: '/customer/profile',    label: 'Profile',    icon: Users },
    ];
  }

  return (
    <aside className={`sidebar ${open ? 'open' : ''}`}>
      {/* Logo */}
      <div className="sidebar-logo">
        <div className="logo-icon">
          <Store size={20} />
        </div>
        <span className="logo-text">Shop<span>Nest</span></span>
      </div>

      {/* Nav */}
      <nav className="sidebar-nav">
        {navItems.map((item, idx) => {
          if (item.type === 'section') {
            return <div key={idx} className="sidebar-section-label">{item.label}</div>;
          }
          const Icon    = item.icon;
          const active  = location.pathname === item.path;
          return (
            <button key={item.path} className={`sidebar-link ${active ? 'active' : ''}`} onClick={() => go(item.path)}>
              <Icon size={18} className="sidebar-link-icon" />
              <span style={{ flex: 1 }}>{item.label}</span>
              {item.badge && cartCount > 0 && (
                <span style={{ background: 'var(--primary)', color: '#fff', borderRadius: '999px', fontSize: '10px', fontWeight: 700, padding: '2px 7px', minWidth: 20, textAlign: 'center' }}>
                  {cartCount}
                </span>
              )}
            </button>
          );
        })}
      </nav>

      {/* User */}
      <div className="sidebar-footer">
        <div className="sidebar-user" onClick={logout}>
          <div className="nav-avatar" style={{ width: 36, height: 36 }}>{initials}</div>
          <div className="sidebar-user-info">
            <div className="sidebar-user-name">{user?.firstName} {user?.lastName}</div>
            <div className="sidebar-user-role">{user?.email}</div>
          </div>
          <LogOut size={15} color="var(--gray-400)" />
        </div>
      </div>
    </aside>
  );
}
