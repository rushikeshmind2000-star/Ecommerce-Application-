import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Bell, ShoppingCart, Menu, X } from 'lucide-react';
import { useApp } from '../context/AppContext';

export default function Navbar({ onToggleSidebar, sidebarOpen }) {
  const { cartCount, user } = useApp();
  const navigate = useNavigate();
  const [search, setSearch] = useState('');

  const initials = user ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase() : 'U';

  return (
    <header className="navbar">
      <div className="navbar-left">
        <button
          className="nav-icon-btn"
          onClick={onToggleSidebar}
          aria-label="Toggle menu"
          id="menu-toggle"
        >
          {sidebarOpen ? <X size={20} /> : <Menu size={20} />}
        </button>

        <div className="navbar-search">
          <Search size={16} className="search-icon" />
          <input
            id="global-search"
            type="search"
            placeholder="Search products, orders…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>
      </div>

      <div className="navbar-right">
        <button className="nav-icon-btn" id="notifications-btn" aria-label="Notifications">
          <Bell size={20} />
          <span className="badge">3</span>
        </button>

        <button className="nav-icon-btn" id="cart-nav-btn" aria-label="Cart" onClick={() => navigate('/cart')}>
          <ShoppingCart size={20} />
          {cartCount > 0 && <span className="badge">{cartCount}</span>}
        </button>

        <div className="nav-avatar" onClick={() => navigate('/profile')} title="Profile" id="profile-avatar">
          {initials}
        </div>
      </div>
    </header>
  );
}
