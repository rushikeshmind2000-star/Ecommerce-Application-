import { useNavigate } from 'react-router-dom';
import { Package, CheckCircle, Truck, Clock, MapPin, ArrowRight } from 'lucide-react';
import { useApp } from '../../context/AppContext';

const ASSIGNED_ORDERS = [
  { id: 'ORD-1001', customer: 'Rushi Sharma', address: '123 MG Road, Bengaluru 560001', product: 'Apple iPhone 15 Pro', status: 'PICKUP_READY', distance: '3.2 km' },
  { id: 'ORD-1002', customer: 'Priya Patel', address: '456 Park St, Mumbai 400001', product: 'Nike Air Max 2024', status: 'OUT_FOR_DELIVERY', distance: '1.8 km' },
];

const STATUS_META = {
  ASSIGNED:         { label: 'Assigned',          cls: 'tag-orange', icon: Clock },
  PICKUP_READY:     { label: 'Ready for Pickup',  cls: 'tag-blue',   icon: Package },
  OUT_FOR_DELIVERY: { label: 'Out for Delivery',  cls: 'tag-purple', icon: Truck },
  DELIVERED:        { label: 'Delivered',          cls: 'tag-green',  icon: CheckCircle },
};

export default function DeliveryDashboard() {
  const navigate = useNavigate();
  const { user } = useApp();

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Delivery Dashboard 🚴</h1>
          <p className="page-subtitle">Welcome, {user?.firstName}! Your delivery overview for today.</p>
        </div>
      </div>

      {/* Stats */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)', marginBottom: 'var(--space-6)' }}>
        <div className="stat-card"><div className="stat-icon blue"><Package size={22} /></div><div className="stat-info"><div className="stat-label">Assigned Today</div><div className="stat-value">8</div></div></div>
        <div className="stat-card"><div className="stat-icon purple"><Truck size={22} /></div><div className="stat-info"><div className="stat-label">Out for Delivery</div><div className="stat-value">3</div></div></div>
        <div className="stat-card"><div className="stat-icon green"><CheckCircle size={22} /></div><div className="stat-info"><div className="stat-label">Delivered Today</div><div className="stat-value">5</div></div></div>
        <div className="stat-card"><div className="stat-icon orange"><Clock size={22} /></div><div className="stat-info"><div className="stat-label">Pending Pickup</div><div className="stat-value">2</div></div></div>
      </div>

      {/* Active Deliveries */}
      <div className="card" style={{ marginBottom: 'var(--space-6)' }}>
        <div className="card-header">
          <span className="card-title">Active Assignments</span>
          <button className="btn btn-secondary btn-sm" onClick={() => navigate('/delivery/assigned')}>
            View All <ArrowRight size={13} />
          </button>
        </div>
        <div style={{ display: 'grid', gap: 'var(--space-4)', padding: 'var(--space-4)' }}>
          {ASSIGNED_ORDERS.map(o => {
            const sm = STATUS_META[o.status] || { label: o.status, cls: 'tag-gray', icon: Clock };
            return (
              <div key={o.id} style={{ border: '1px solid var(--gray-200)', borderRadius: 'var(--radius-xl)', padding: 'var(--space-5)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 'var(--space-3)' }}>
                  <div>
                    <div style={{ fontWeight: 700, color: 'var(--gray-900)', marginBottom: 2 }}>{o.id}</div>
                    <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-600)' }}>{o.product}</div>
                  </div>
                  <span className={`tag ${sm.cls}`}>{sm.label}</span>
                </div>
                <div style={{ display: 'flex', gap: 'var(--space-2)', alignItems: 'center', marginBottom: 'var(--space-1)', fontSize: 'var(--font-size-sm)', color: 'var(--gray-600)' }}>
                  <Package size={13} /> {o.customer}
                </div>
                <div style={{ display: 'flex', gap: 'var(--space-2)', alignItems: 'center', marginBottom: 'var(--space-4)', fontSize: 'var(--font-size-sm)', color: 'var(--gray-500)' }}>
                  <MapPin size={13} /> {o.address} · {o.distance}
                </div>
                <div style={{ display: 'flex', gap: 'var(--space-3)' }}>
                  {o.status === 'PICKUP_READY' && (
                    <button className="btn btn-primary btn-sm" id={`pickup-btn-${o.id}`}>
                      <Package size={13} /> Picked Up
                    </button>
                  )}
                  {o.status === 'OUT_FOR_DELIVERY' && (
                    <button className="btn btn-success btn-sm" id={`deliver-btn-${o.id}`}>
                      <CheckCircle size={13} /> Mark Delivered
                    </button>
                  )}
                  <button className="btn btn-secondary btn-sm">
                    <MapPin size={13} /> Navigate
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="card">
        <div className="card-header"><span className="card-title">Quick Navigation</span></div>
        <div className="card-body" style={{ display: 'flex', gap: 'var(--space-4)', flexWrap: 'wrap' }}>
          {[
            { label: 'Assigned Orders', icon: Package,      path: '/delivery/assigned',  color: 'var(--primary-light)', text: 'var(--primary)' },
            { label: 'Pickup',          icon: Truck,         path: '/delivery/pickup',    color: 'var(--accent-light)',  text: 'var(--accent)' },
            { label: 'Out for Delivery',icon: MapPin,        path: '/delivery/delivery',  color: 'var(--success-light)', text: 'var(--success)' },
            { label: 'History',         icon: CheckCircle,  path: '/delivery/history',   color: 'var(--warning-light)', text: 'var(--warning)' },
          ].map(a => (
            <button key={a.path} onClick={() => navigate(a.path)}
              style={{ flex: 1, minWidth: 130, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-2)', padding: 'var(--space-5)', background: a.color, borderRadius: 'var(--radius-xl)', border: 'none', cursor: 'pointer', transition: 'transform var(--transition-md)', color: a.text }}
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
