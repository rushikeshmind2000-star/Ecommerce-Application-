import { useNavigate } from 'react-router-dom';
import { Package, ShoppingBag, AlertTriangle, DollarSign, TrendingUp, Plus, ArrowRight, Star, Warehouse } from 'lucide-react';
import { useApp } from '../../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const LOW_STOCK = [
  { name: 'Adidas Ultra Boost 24', stock: 3 },
  { name: 'Samsung Galaxy S24 Ultra', stock: 5 },
  { name: 'MacBook Pro 14"', stock: 2 },
];

const RECENT_ORDERS = [
  { id: 'ORD-1001', product: 'Apple iPhone 15 Pro', qty: 2, amount: 269800, status: 'SHIPPED' },
  { id: 'ORD-1002', product: 'Nike Air Max 2024',   qty: 1, amount: 12999,  status: 'DELIVERED' },
  { id: 'ORD-1003', product: 'MacBook Pro 14"',     qty: 1, amount: 189900, status: 'CONFIRMED' },
  { id: 'ORD-1004', product: 'Sony WH-1000XM5',     qty: 3, amount: 89970,  status: 'PENDING' },
];

const STATUS_META = {
  DELIVERED:  { cls: 'tag-green' },
  SHIPPED:    { cls: 'tag-blue' },
  CONFIRMED:  { cls: 'tag-purple' },
  PROCESSING: { cls: 'tag-yellow' },
  PENDING:    { cls: 'tag-orange' },
};

export default function VendorDashboard() {
  const navigate = useNavigate();
  const { user, products } = useApp();

  const MONTHLY = [420, 580, 510, 720, 890, 750];
  const months = ['Aug', 'Sep', 'Oct', 'Nov', 'Dec', 'Jan'];
  const MAX = Math.max(...MONTHLY);

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Vendor Dashboard 🏪</h1>
          <p className="page-subtitle">Welcome back, {user?.firstName}! Here's your store overview.</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-primary" onClick={() => navigate('/vendor/add-product')} id="vendor-add-product-btn">
            <Plus size={15} /> Add Product
          </button>
        </div>
      </div>

      {/* KPIs */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)' }}>
        <div className="stat-card"><div className="stat-icon blue"><Package size={22} /></div><div className="stat-info"><div className="stat-label">My Products</div><div className="stat-value">120</div><div className="stat-change">105 active</div></div></div>
        <div className="stat-card"><div className="stat-icon orange"><AlertTriangle size={22} /></div><div className="stat-info"><div className="stat-label">Low Stock</div><div className="stat-value">8</div><div className="stat-change">Needs attention</div></div></div>
        <div className="stat-card"><div className="stat-icon green"><DollarSign size={22} /></div><div className="stat-info"><div className="stat-label">Today's Sales</div><div className="stat-value">₹75,000</div><div className="stat-change">+12% vs yesterday</div></div></div>
        <div className="stat-card"><div className="stat-icon purple"><ShoppingBag size={22} /></div><div className="stat-info"><div className="stat-label">Pending Orders</div><div className="stat-value">15</div><div className="stat-change">Today's orders: 20</div></div></div>
      </div>

      {/* Content Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: 'var(--space-6)', marginTop: 'var(--space-6)' }}>

        {/* Sales Chart */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">Monthly Sales (₹)</span>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate('/vendor/reports')}>View Reports <ArrowRight size={13} /></button>
          </div>
          <div className="card-body">
            <div style={{ display: 'flex', gap: 'var(--space-2)', alignItems: 'flex-end', height: 160, marginBottom: 'var(--space-3)' }}>
              {MONTHLY.map((v, i) => (
                <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
                  <div style={{ width: '70%', height: `${(v / MAX) * 140}px`, background: 'linear-gradient(180deg, var(--primary) 0%, var(--primary-light) 100%)', borderRadius: 'var(--radius-md) var(--radius-md) 0 0' }}
                    title={`${months[i]}: ₹${(v * 1000).toLocaleString('en-IN')}`} />
                  <div style={{ fontSize: 10, color: 'var(--gray-400)' }}>{months[i]}</div>
                </div>
              ))}
            </div>
            <div style={{ display: 'flex', gap: 'var(--space-4)', justifyContent: 'center', flexWrap: 'wrap' }}>
              {MONTHLY.map((v, i) => (
                <div key={i} style={{ textAlign: 'center' }}>
                  <div style={{ fontWeight: 700, fontSize: 11, color: 'var(--gray-700)' }}>₹{(v / 100).toFixed(1)}L</div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Low Stock */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">⚠️ Low Stock Alert</span>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate('/vendor/inventory')}>Manage</button>
          </div>
          <div className="card-body" style={{ padding: 0 }}>
            {LOW_STOCK.map(item => (
              <div key={item.name} style={{ padding: 'var(--space-4) var(--space-5)', borderBottom: '1px solid var(--gray-100)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                  <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--gray-800)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '70%' }}>{item.name}</div>
                  <span style={{ fontWeight: 700, color: item.stock <= 2 ? 'var(--danger)' : 'var(--warning)', fontSize: 'var(--font-size-sm)' }}>{item.stock} left</span>
                </div>
                <div className="inventory-bar"><div className="inventory-bar-fill low" style={{ width: `${(item.stock / 20) * 100}%` }} /></div>
              </div>
            ))}
            <div style={{ padding: 'var(--space-4) var(--space-5)' }}>
              <button className="btn btn-warning btn-sm btn-full" onClick={() => navigate('/vendor/inventory')}>
                <Warehouse size={13} /> Update Stock Levels
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Orders */}
      <div className="card" style={{ marginTop: 'var(--space-6)' }}>
        <div className="card-header">
          <span className="card-title">Recent Orders</span>
          <button className="btn btn-secondary btn-sm" onClick={() => navigate('/vendor/orders')}>
            View All <ArrowRight size={13} />
          </button>
        </div>
        <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table>
            <thead><tr><th>Order ID</th><th>Product</th><th>Qty</th><th>Amount</th><th>Status</th></tr></thead>
            <tbody>
              {RECENT_ORDERS.map(o => (
                <tr key={o.id} style={{ cursor: 'pointer' }} onClick={() => navigate('/vendor/orders')}>
                  <td className="td-primary">{o.id}</td>
                  <td>{o.product}</td>
                  <td>{o.qty}</td>
                  <td className="font-bold">{fmt(o.amount)}</td>
                  <td><span className={`tag ${STATUS_META[o.status]?.cls || 'tag-gray'}`}>{o.status}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="card" style={{ marginTop: 'var(--space-6)' }}>
        <div className="card-header"><span className="card-title">Quick Actions</span></div>
        <div className="card-body" style={{ display: 'flex', gap: 'var(--space-4)', flexWrap: 'wrap' }}>
          {[
            { label: 'Add Product',     icon: Plus,      path: '/vendor/add-product', color: 'var(--primary-light)', text: 'var(--primary)' },
            { label: 'Manage Inventory',icon: Warehouse,  path: '/vendor/inventory',   color: 'var(--accent-light)',  text: 'var(--accent)' },
            { label: 'View Orders',     icon: ShoppingBag,path: '/vendor/orders',      color: 'var(--success-light)', text: 'var(--success)' },
            { label: 'View Sales',      icon: TrendingUp, path: '/vendor/payments',    color: 'var(--warning-light)', text: 'var(--warning)' },
          ].map(a => (
            <button key={a.path} onClick={() => navigate(a.path)}
              style={{ flex: 1, minWidth: 140, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-2)', padding: 'var(--space-5)', background: a.color, borderRadius: 'var(--radius-xl)', border: 'none', cursor: 'pointer', transition: 'transform var(--transition-md)', color: a.text }}
              onMouseEnter={e => e.currentTarget.style.transform = 'translateY(-3px)'}
              onMouseLeave={e => e.currentTarget.style.transform = ''}>
              <a.icon size={24} />
              <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: 600 }}>{a.label}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
