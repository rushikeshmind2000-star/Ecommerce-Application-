import { useNavigate } from 'react-router-dom';
import {
  Users, Store, Package, ShoppingBag, TrendingUp, DollarSign,
  Clock, AlertTriangle, CheckCircle, XCircle, ArrowRight,
  BarChart2, RefreshCw
} from 'lucide-react';
import { useApp } from '../../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

function KpiCard({ icon: Icon, label, value, sub, color, onClick }) {
  return (
    <div className="stat-card" style={{ cursor: onClick ? 'pointer' : 'default' }} onClick={onClick}>
      <div className={`stat-icon ${color}`}><Icon size={22} /></div>
      <div className="stat-info">
        <div className="stat-label">{label}</div>
        <div className="stat-value">{value}</div>
        {sub && <div className="stat-change">{sub}</div>}
      </div>
    </div>
  );
}

const MOCK_RECENT_ORDERS = [
  { id: 'ORD-1001', customer: 'Rushi Sharma', vendor: 'ABC Electronics', amount: 50000, payment: 'PAID', status: 'SHIPPED', date: '2024-01-22' },
  { id: 'ORD-1002', customer: 'Priya Patel', vendor: 'XYZ Fashion', amount: 12500, payment: 'PAID', status: 'DELIVERED', date: '2024-01-21' },
  { id: 'ORD-1003', customer: 'Arjun Nair', vendor: 'Tech Hub', amount: 189900, payment: 'PAID', status: 'CONFIRMED', date: '2024-01-21' },
  { id: 'ORD-1004', customer: 'Sneha Kulkarni', vendor: 'Fashion Plus', amount: 4999, payment: 'PENDING', status: 'PENDING', date: '2024-01-20' },
  { id: 'ORD-1005', customer: 'Rahul Gupta', vendor: 'ABC Electronics', amount: 29990, payment: 'PAID', status: 'PROCESSING', date: '2024-01-20' },
];

const LOW_STOCK = [
  { name: 'Adidas Ultra Boost 24', vendor: 'Sports World', stock: 5, threshold: 10 },
  { name: 'Sony WH-1000XM5', vendor: 'Tech Hub', stock: 3, threshold: 10 },
  { name: 'MacBook Pro 14"', vendor: 'ABC Electronics', stock: 2, threshold: 5 },
];

const STATUS_META = {
  DELIVERED:   { label: 'Delivered',   cls: 'tag-green'  },
  SHIPPED:     { label: 'Shipped',     cls: 'tag-blue'   },
  CONFIRMED:   { label: 'Confirmed',   cls: 'tag-purple' },
  PROCESSING:  { label: 'Processing',  cls: 'tag-yellow' },
  PENDING:     { label: 'Pending',     cls: 'tag-orange' },
  CANCELLED:   { label: 'Cancelled',   cls: 'tag-red'    },
};

export default function AdminDashboard() {
  const navigate = useNavigate();
  const { user } = useApp();

  return (
    <div className="page-container">
      {/* Header */}
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Admin Dashboard</h1>
          <p className="page-subtitle">Platform-level overview · {new Date().toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-secondary" id="admin-refresh-btn">
            <RefreshCw size={15} /> Refresh
          </button>
          <button className="btn btn-primary" onClick={() => navigate('/admin/reports')}>
            <BarChart2 size={15} /> Reports
          </button>
        </div>
      </div>

      {/* Primary KPIs */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)' }}>
        <KpiCard icon={Users}      label="Total Users"     value="12,540" sub="+340 this week"        color="blue"   onClick={() => navigate('/admin/users')} />
        <KpiCard icon={Store}      label="Total Vendors"   value="245"    sub="12 pending approval"   color="purple" onClick={() => navigate('/admin/vendors')} />
        <KpiCard icon={Package}    label="Total Products"  value="8,920"  sub="35 pending review"     color="orange" onClick={() => navigate('/admin/products')} />
        <KpiCard icon={ShoppingBag}label="Total Orders"    value="35,450" sub="+450 today"            color="green"  onClick={() => navigate('/admin/orders')} />
      </div>

      {/* Secondary KPIs */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)', marginTop: 'var(--space-4)' }}>
        <KpiCard icon={DollarSign}    label="Today's Revenue"    value="₹8,50,000"  sub="+12% vs yesterday"  color="green"  />
        <KpiCard icon={TrendingUp}    label="Today's Orders"     value="450"        sub="vs 380 yesterday"   color="blue"   />
        <KpiCard icon={Clock}         label="Pending Vendors"    value="12"         sub="awaiting approval"  color="orange" onClick={() => navigate('/admin/vendors')} />
        <KpiCard icon={AlertTriangle} label="Pending Refunds"    value="18"         sub="₹2.1L at risk"      color="red"    onClick={() => navigate('/admin/returns')} />
      </div>

      {/* Main Content Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: 'var(--space-6)', marginTop: 'var(--space-6)' }}>

        {/* Recent Orders */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">Recent Orders</span>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate('/admin/orders')}>
              View All <ArrowRight size={13} />
            </button>
          </div>
          <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
            <table>
              <thead>
                <tr>
                  <th>Order ID</th>
                  <th>Customer</th>
                  <th>Vendor</th>
                  <th>Amount</th>
                  <th>Payment</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {MOCK_RECENT_ORDERS.map(o => {
                  const sm = STATUS_META[o.status] || { label: o.status, cls: 'tag-gray' };
                  return (
                    <tr key={o.id} style={{ cursor: 'pointer' }} onClick={() => navigate('/admin/orders')}>
                      <td className="td-primary">{o.id}</td>
                      <td>{o.customer}</td>
                      <td className="text-gray text-sm">{o.vendor}</td>
                      <td className="font-bold">{fmt(o.amount)}</td>
                      <td>
                        <span className={`tag ${o.payment === 'PAID' ? 'tag-green' : 'tag-orange'}`}>
                          {o.payment}
                        </span>
                      </td>
                      <td><span className={`tag ${sm.cls}`}>{sm.label}</span></td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>

        {/* Right Column */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>

          {/* Pending Actions */}
          <div className="card">
            <div className="card-header">
              <span className="card-title">Pending Actions</span>
            </div>
            <div className="card-body" style={{ padding: 0 }}>
              {[
                { label: 'Vendor Approvals', count: 12, color: 'var(--warning)', path: '/admin/vendors', icon: Store },
                { label: 'Product Reviews',  count: 35, color: 'var(--primary)', path: '/admin/products', icon: Package },
                { label: 'Refund Requests',  count: 18, color: 'var(--danger)', path: '/admin/returns', icon: RefreshCw },
              ].map(item => (
                <div key={item.label}
                  onClick={() => navigate(item.path)}
                  style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', padding: 'var(--space-4) var(--space-5)', borderBottom: '1px solid var(--gray-100)', cursor: 'pointer', transition: 'background var(--transition)' }}
                  onMouseEnter={e => e.currentTarget.style.background = 'var(--gray-50)'}
                  onMouseLeave={e => e.currentTarget.style.background = ''}>
                  <div style={{ width: 36, height: 36, borderRadius: 'var(--radius-md)', background: item.color + '22', display: 'flex', alignItems: 'center', justifyContent: 'center', color: item.color, flexShrink: 0 }}>
                    <item.icon size={16} />
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--gray-800)' }}>{item.label}</div>
                  </div>
                  <span style={{ background: item.color, color: '#fff', borderRadius: 'var(--radius-full)', fontSize: 11, fontWeight: 700, padding: '2px 10px' }}>{item.count}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Low Stock Alert */}
          <div className="card">
            <div className="card-header">
              <span className="card-title">Low Stock Alert</span>
              <button className="btn btn-secondary btn-sm" onClick={() => navigate('/admin/inventory')}>
                View All
              </button>
            </div>
            <div className="card-body" style={{ padding: 0 }}>
              {LOW_STOCK.map(item => (
                <div key={item.name} style={{ padding: 'var(--space-3) var(--space-5)', borderBottom: '1px solid var(--gray-100)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                    <div>
                      <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--gray-800)' }}>{item.name}</div>
                      <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--gray-400)' }}>{item.vendor}</div>
                    </div>
                    <span style={{ fontWeight: 700, fontSize: 'var(--font-size-sm)', color: item.stock <= 3 ? 'var(--danger)' : 'var(--warning)' }}>
                      {item.stock} left
                    </span>
                  </div>
                  <div className="inventory-bar">
                    <div className="inventory-bar-fill low" style={{ width: `${(item.stock / item.threshold) * 100}%` }} />
                  </div>
                </div>
              ))}
            </div>
          </div>

        </div>
      </div>

      {/* Quick Actions */}
      <div className="card" style={{ marginTop: 'var(--space-6)' }}>
        <div className="card-header">
          <span className="card-title">Quick Actions</span>
        </div>
        <div className="card-body" style={{ display: 'flex', gap: 'var(--space-4)', flexWrap: 'wrap' }}>
          {[
            { label: 'Manage Users',     icon: Users,      path: '/admin/users',      color: 'var(--primary-light)',  text: 'var(--primary)' },
            { label: 'Review Vendors',   icon: Store,      path: '/admin/vendors',    color: 'var(--accent-light)',   text: 'var(--accent)' },
            { label: 'Review Products',  icon: Package,    path: '/admin/products',   color: 'var(--success-light)',  text: 'var(--success)' },
            { label: 'View Orders',      icon: ShoppingBag,path: '/admin/orders',     color: 'var(--warning-light)',  text: 'var(--warning)' },
            { label: 'View Payments',    icon: DollarSign, path: '/admin/payments',   color: '#f0fdf4',               text: '#16a34a' },
            { label: 'System Settings',  icon: AlertTriangle,path: '/admin/settings', color: '#fdf4ff',               text: '#9333ea' },
          ].map(a => (
            <button key={a.path} onClick={() => navigate(a.path)}
              style={{ flex: 1, minWidth: 140, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-2)', padding: 'var(--space-5)', background: a.color, borderRadius: 'var(--radius-xl)', border: 'none', cursor: 'pointer', transition: 'transform var(--transition-md), box-shadow var(--transition-md)', color: a.text }}
              onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-3px)'; e.currentTarget.style.boxShadow = 'var(--shadow-lg)'; }}
              onMouseLeave={e => { e.currentTarget.style.transform = ''; e.currentTarget.style.boxShadow = ''; }}>
              <a.icon size={24} />
              <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: 600 }}>{a.label}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
