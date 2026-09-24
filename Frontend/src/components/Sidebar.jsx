import { useNavigate, useLocation } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import {
  LayoutDashboard, Package, ShoppingCart, ClipboardList,
  CreditCard, Warehouse, Users, LogOut, Store, Tag, ChevronRight
} from 'lucide-react';

const navItems = [
  { label: 'Main', type: 'section' },
  { path: '/dashboard',  label: 'Dashboard',  icon: LayoutDashboard },
  { path: '/products',   label: 'Products',   icon: Package },
  { path: '/cart',       label: 'Cart',       icon: ShoppingCart, badge: true },
  { path: '/orders',     label: 'My Orders',  icon: ClipboardList },

  { label: 'Management', type: 'section' },
  { path: '/inventory',  label: 'Inventory',  icon: Warehouse },
  { path: '/payments',   label: 'Payments',   icon: CreditCard },
  { path: '/categories', label: 'Categories', icon: Tag },
];

export default function Sidebar({ open, onClose }) {
  const { user, logout, cartCount } = useApp();
  const navigate  = useNavigate();
  const location  = useLocation();

  const go = (path) => { navigate(path); onClose?.(); };

  const initials = user ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase() : 'U';

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
