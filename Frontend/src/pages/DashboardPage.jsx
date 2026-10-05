import { useNavigate } from 'react-router-dom';
import {
  TrendingUp, ShoppingBag, Package, DollarSign,
  Users, ArrowRight, Star, Clock, CheckCircle, Truck,
  Store, Warehouse, ClipboardList
} from 'lucide-react';
import { useApp } from '../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

function StatCard({ icon: Icon, label, value, change, color }) {
  return (
    <div className="stat-card">
      <div className={`stat-icon ${color}`}><Icon size={22} /></div>
      <div className="stat-info">
        <div className="stat-label">{label}</div>
        <div className="stat-value">{value}</div>
        {change && <div className="stat-change">↑ {change}</div>}
      </div>
    </div>
  );
}

export default function DashboardPage() {
  const { products, orders, cartCount, user } = useApp();
  const navigate = useNavigate();

  const totalRevenue = orders.filter(o => o.status === 'DELIVERED').reduce((s, o) => s + o.totalAmount, 0);
  const pendingOrders = orders.filter(o => ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED'].includes(o.status)).length;
  const activeProducts = products.filter(p => p.status === 'ACTIVE').length;

  const statusMeta = {
    DELIVERED:      { label: 'Delivered',  class: 'tag-green',  icon: CheckCircle },
    SHIPPED:        { label: 'Shipped',    class: 'tag-blue',   icon: Truck },
    CONFIRMED:      { label: 'Confirmed',  class: 'tag-purple', icon: CheckCircle },
    PROCESSING:     { label: 'Processing', class: 'tag-yellow', icon: Clock },
    PROCESSING_SAGA:{ label: 'Processing', class: 'tag-yellow', icon: Clock },
    PENDING:        { label: 'Pending',    class: 'tag-orange', icon: Clock },
    CANCELLED:      { label: 'Cancelled',  class: 'tag-red',    icon: Clock },
  };

  const recentOrders = orders.slice(0, 4);
  const featuredProducts = products.filter(p => p.status === 'ACTIVE').slice(0, 4);

  return (
    <div className="page-container">
      {/* Header */}
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Good morning, {user?.firstName}! 👋</h1>
          <p className="page-subtitle">Here's what's happening with your store today.</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-secondary" onClick={() => navigate('/products')}>
            <Package size={16} /> Browse Products
          </button>
          <button className="btn btn-primary" onClick={() => navigate('/orders')}>
            <ShoppingBag size={16} /> My Orders
          </button>
        </div>
      </div>

      {/* Stats */}
      <div className="stats-grid">
        {user?.role === 'CUSTOMER' && (
          <>
            <StatCard icon={DollarSign} label="Total Spent" value={fmt(totalRevenue)} change="vs last month" color="green" />
            <StatCard icon={ShoppingBag} label="Total Orders" value={orders.length} change={`${pendingOrders} active`} color="blue" />
            <StatCard icon={Package} label="Wishlist" value={2} change="saved items" color="orange" />
            <StatCard icon={Users} label="Cart Items" value={cartCount} change="ready to checkout" color="purple" />
          </>
        )}
        {user?.role === 'VENDOR' && (
          <>
            <StatCard icon={DollarSign} label="Sales Revenue" value={fmt(450000)} change="+12% this week" color="green" />
            <StatCard icon={ShoppingBag} label="Vendor Orders" value={45} change={`${pendingOrders} pending`} color="blue" />
            <StatCard icon={Package} label="My Products" value={12} change="active listings" color="orange" />
            <StatCard icon={Star} label="Avg Rating" value={"4.8"} change="out of 5" color="purple" />
          </>
        )}
        {user?.role === 'ADMIN' && (
          <>
            <StatCard icon={DollarSign} label="Total Platform Revenue" value={fmt(2450000)} change="+5% vs last month" color="green" />
            <StatCard icon={Store} label="Total Vendors" value={24} change="3 pending approval" color="blue" />
            <StatCard icon={Users} label="Total Customers" value={1450} change="+120 this week" color="orange" />
            <StatCard icon={Package} label="Total Products" value={850} change="15 pending approval" color="purple" />
          </>
        )}
      </div>

      {/* Content Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 380px', gap: 'var(--space-6)' }}>

        {/* Recent Orders */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">{user?.role === 'ADMIN' ? 'Recent Platform Orders' : 'Recent Orders'}</span>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate('/orders')}>
              View all <ArrowRight size={14} />
            </button>
          </div>
          <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
            <table>
              <thead>
                <tr>
                  <th>Order #</th>
                  <th>Items</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {recentOrders.map(o => {
                  const m = statusMeta[o.status] || { label: o.status, class: 'tag-gray' };
                  return (
                    <tr key={o.id} onClick={() => navigate(`/orders/${o.id}`)} style={{ cursor: 'pointer' }}>
                      <td className="td-primary">{o.orderNumber}</td>
                      <td>{o.items.length} item{o.items.length > 1 ? 's' : ''}</td>
                      <td className="font-bold">{fmt(o.totalAmount)}</td>
                      <td><span className={`tag ${m.class}`}>{m.label}</span></td>
                      <td className="text-gray text-sm">{new Date(o.createdAt).toLocaleDateString('en-IN')}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>

        {/* Featured Products */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">{user?.role === 'VENDOR' ? 'My Top Products' : 'Top Products'}</span>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate('/products')}>
              View all <ArrowRight size={14} />
            </button>
          </div>
          <div className="card-body" style={{ padding: 0 }}>
            {featuredProducts.map(p => (
              <div key={p.id} style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', padding: 'var(--space-4) var(--space-5)', borderBottom: '1px solid var(--gray-100)', cursor: 'pointer', transition: 'background var(--transition)' }}
                onClick={() => navigate(`/products/${p.id}`)}
                onMouseEnter={e => e.currentTarget.style.background = 'var(--gray-50)'}
                onMouseLeave={e => e.currentTarget.style.background = ''}>
                <div style={{ width: 48, height: 48, background: 'var(--gray-100)', borderRadius: 'var(--radius-md)', overflow: 'hidden', flexShrink: 0 }}>
                  <img src={p.images?.[0]?.imageUrl} alt={p.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                </div>
                <div style={{ flex: 1, overflow: 'hidden' }}>
                  <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--gray-900)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{p.name}</div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: 'var(--warning)', fontSize: 'var(--font-size-xs)' }}>
                    <Star size={10} fill="currentColor" /> {p.rating}
                  </div>
                </div>
                <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 700, color: 'var(--gray-900)', whiteSpace: 'nowrap' }}>
                  {fmt(p.price)}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="card" style={{ marginTop: 'var(--space-6)' }}>
        <div className="card-header">
          <span className="card-title">Quick Actions</span>
        </div>
        <div className="card-body" style={{ display: 'flex', gap: 'var(--space-4)', flexWrap: 'wrap' }}>
          {user?.role === 'CUSTOMER' && [
            { label: 'Browse Products', icon: Package, path: '/products', color: 'var(--primary-light)', text: 'var(--primary)' },
            { label: 'View Cart', icon: ShoppingBag, path: '/cart', color: 'var(--accent-light)', text: 'var(--accent)' },
            { label: 'Track Orders', icon: Truck, path: '/orders', color: 'var(--success-light)', text: 'var(--success)' },
          ].map(a => (
            <button key={a.path} onClick={() => navigate(a.path)}
              style={{ flex: 1, minWidth: 160, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-3)', padding: 'var(--space-6)', background: a.color, borderRadius: 'var(--radius-xl)', border: 'none', cursor: 'pointer', transition: 'transform var(--transition-md), box-shadow var(--transition-md)', color: a.text }}
              onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-3px)'; e.currentTarget.style.boxShadow = 'var(--shadow-lg)'; }}
              onMouseLeave={e => { e.currentTarget.style.transform = ''; e.currentTarget.style.boxShadow = ''; }}>
              <a.icon size={28} />
              <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600 }}>{a.label}</span>
            </button>
          ))}

          {user?.role === 'VENDOR' && [
            { label: 'Add Product', icon: Package, path: '/products/new', color: 'var(--primary-light)', text: 'var(--primary)' },
            { label: 'Manage Inventory', icon: Warehouse, path: '/inventory', color: 'var(--accent-light)', text: 'var(--accent)' },
            { label: 'View Orders', icon: ClipboardList, path: '/orders', color: 'var(--success-light)', text: 'var(--success)' },
          ].map(a => (
            <button key={a.path} onClick={() => navigate(a.path)}
              style={{ flex: 1, minWidth: 160, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-3)', padding: 'var(--space-6)', background: a.color, borderRadius: 'var(--radius-xl)', border: 'none', cursor: 'pointer', transition: 'transform var(--transition-md), box-shadow var(--transition-md)', color: a.text }}
              onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-3px)'; e.currentTarget.style.boxShadow = 'var(--shadow-lg)'; }}
              onMouseLeave={e => { e.currentTarget.style.transform = ''; e.currentTarget.style.boxShadow = ''; }}>
              <a.icon size={28} />
              <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600 }}>{a.label}</span>
            </button>
          ))}

          {user?.role === 'ADMIN' && [
            { label: 'Review Vendors', icon: Store, path: '/vendors', color: 'var(--primary-light)', text: 'var(--primary)' },
            { label: 'Review Products', icon: Package, path: '/products/pending', color: 'var(--accent-light)', text: 'var(--accent)' },
            { label: 'Manage Users', icon: Users, path: '/users', color: 'var(--success-light)', text: 'var(--success)' },
          ].map(a => (
            <button key={a.path} onClick={() => navigate(a.path)}
              style={{ flex: 1, minWidth: 160, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-3)', padding: 'var(--space-6)', background: a.color, borderRadius: 'var(--radius-xl)', border: 'none', cursor: 'pointer', transition: 'transform var(--transition-md), box-shadow var(--transition-md)', color: a.text }}
              onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-3px)'; e.currentTarget.style.boxShadow = 'var(--shadow-lg)'; }}
              onMouseLeave={e => { e.currentTarget.style.transform = ''; e.currentTarget.style.boxShadow = ''; }}>
              <a.icon size={28} />
              <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600 }}>{a.label}</span>
            </button>
          ))}
        </div>

      </div>
    </div>
  );
}
