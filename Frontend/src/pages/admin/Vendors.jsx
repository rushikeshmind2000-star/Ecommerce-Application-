import { useState } from 'react';
import { Search, Eye, CheckCircle, XCircle, PauseCircle, Store, Package, ShoppingBag, TrendingUp, X } from 'lucide-react';

const MOCK_VENDORS = [
  { id: 'v1', name: 'ABC Electronics', company: 'ABC Electronics Pvt Ltd', email: 'contact@abcelectronics.com', phone: '9800000001', status: 'ACTIVE',   products: 45, orders: 230, revenue: 1250000, joinedAt: '2023-06-01', gst: '27ABCDE1234F1Z5' },
  { id: 'v2', name: 'XYZ Fashion',     company: 'XYZ Fashion House',       email: 'info@xyzfashion.com',      phone: '9800000002', status: 'ACTIVE',   products: 120, orders: 890, revenue: 3400000, joinedAt: '2023-04-15', gst: '29XYZFG5678H2Y6' },
  { id: 'v3', name: 'Tech Hub',        company: 'Tech Hub Solutions',       email: 'hello@techhub.com',        phone: '9800000003', status: 'PENDING',  products: 0, orders: 0, revenue: 0, joinedAt: '2024-01-20', gst: '24TECTH9012I3X7' },
  { id: 'v4', name: 'Sports World',    company: 'Sports World India',       email: 'sales@sportsworld.in',     phone: '9800000004', status: 'PENDING',  products: 0, orders: 0, revenue: 0, joinedAt: '2024-01-22', gst: '07SPORT4567J4W8' },
  { id: 'v5', name: 'Fashion Plus',    company: 'Fashion Plus LLP',         email: 'hi@fashionplus.com',       phone: '9800000005', status: 'SUSPENDED',products: 12, orders: 45, revenue: 120000, joinedAt: '2023-09-10', gst: '19FASPL8901K5V9' },
  { id: 'v6', name: 'HomeDecor Co',   company: 'HomeDecor Co Pvt Ltd',     email: 'info@homedecor.com',       phone: '9800000006', status: 'ACTIVE',   products: 78, orders: 340, revenue: 890000, joinedAt: '2023-07-22', gst: '06HOMDC2345L6U0' },
];

const STATUS_META = {
  ACTIVE:    { label: 'Active',    cls: 'tag-green',  icon: CheckCircle },
  PENDING:   { label: 'Pending',   cls: 'tag-orange', icon: PauseCircle },
  SUSPENDED: { label: 'Suspended', cls: 'tag-red',    icon: XCircle },
  REJECTED:  { label: 'Rejected',  cls: 'tag-gray',   icon: XCircle },
};

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

export default function AdminVendors() {
  const [search, setSearch]       = useState('');
  const [statusFilter, setStatus] = useState('ALL');
  const [vendors, setVendors]     = useState(MOCK_VENDORS);
  const [selected, setSelected]   = useState(null);

  const filtered = vendors.filter(v => {
    const q = search.toLowerCase();
    if (q && !v.name.toLowerCase().includes(q) && !v.email.toLowerCase().includes(q) && !v.company.toLowerCase().includes(q)) return false;
    if (statusFilter !== 'ALL' && v.status !== statusFilter) return false;
    return true;
  });

  const updateStatus = (id, newStatus) => {
    setVendors(prev => prev.map(v => v.id === id ? { ...v, status: newStatus } : v));
    setSelected(null);
  };

  const pending = vendors.filter(v => v.status === 'PENDING').length;
  const active  = vendors.filter(v => v.status === 'ACTIVE').length;

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Vendor Management</h1>
          <p className="page-subtitle">{filtered.length} vendors · {pending} pending approval</p>
        </div>
      </div>

      {/* Summary KPIs */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)', marginBottom: 'var(--space-6)' }}>
        <div className="stat-card">
          <div className="stat-icon green"><Store size={20} /></div>
          <div className="stat-info"><div className="stat-label">Active Vendors</div><div className="stat-value">{active}</div></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon orange"><PauseCircle size={20} /></div>
          <div className="stat-info"><div className="stat-label">Pending Approval</div><div className="stat-value">{pending}</div></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon blue"><TrendingUp size={20} /></div>
          <div className="stat-info"><div className="stat-label">Total Revenue</div><div className="stat-value">{fmt(vendors.reduce((s, v) => s + v.revenue, 0))}</div></div>
        </div>
      </div>

      {/* Filters */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)', display: 'flex', gap: 'var(--space-3)', flexWrap: 'wrap' }}>
        <div className="input-wrapper" style={{ flex: 1, minWidth: 220 }}>
          <Search size={16} className="input-icon-left" />
          <input id="vendor-search" type="search" className="form-input" placeholder="Search vendors…" value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <select id="vendor-status-filter" className="form-select" style={{ minWidth: 160 }} value={statusFilter} onChange={e => setStatus(e.target.value)}>
          <option value="ALL">All Status</option>
          <option value="ACTIVE">Active</option>
          <option value="PENDING">Pending</option>
          <option value="SUSPENDED">Suspended</option>
        </select>
      </div>

      {/* Table */}
      <div className="card">
        <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table>
            <thead>
              <tr>
                <th>Vendor</th>
                <th>Email</th>
                <th>Status</th>
                <th>Products</th>
                <th>Orders</th>
                <th>Revenue</th>
                <th>Joined</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(v => {
                const sm = STATUS_META[v.status] || { label: v.status, cls: 'tag-gray' };
                return (
                  <tr key={v.id}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                        <div style={{ width: 36, height: 36, borderRadius: 'var(--radius-md)', background: 'var(--primary-light)', color: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 13, flexShrink: 0 }}>
                          {v.name[0]}
                        </div>
                        <div>
                          <div className="td-primary">{v.name}</div>
                          <div style={{ fontSize: 11, color: 'var(--gray-400)' }}>{v.company}</div>
                        </div>
                      </div>
                    </td>
                    <td className="text-gray text-sm">{v.email}</td>
                    <td><span className={`tag ${sm.cls}`}>{sm.label}</span></td>
                    <td>{v.products}</td>
                    <td>{v.orders}</td>
                    <td className="font-bold">{fmt(v.revenue)}</td>
                    <td className="text-gray text-sm">{new Date(v.joinedAt).toLocaleDateString('en-IN')}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => setSelected(v)} id={`view-vendor-${v.id}`}><Eye size={13} /> View</button>
                        {v.status === 'PENDING' && (
                          <>
                            <button className="btn btn-success btn-sm" onClick={() => updateStatus(v.id, 'ACTIVE')} id={`approve-vendor-${v.id}`}><CheckCircle size={13} /></button>
                            <button className="btn btn-danger btn-sm" onClick={() => updateStatus(v.id, 'REJECTED')} id={`reject-vendor-${v.id}`}><XCircle size={13} /></button>
                          </>
                        )}
                        {v.status === 'ACTIVE' && (
                          <button className="btn btn-danger btn-sm" onClick={() => updateStatus(v.id, 'SUSPENDED')} id={`suspend-vendor-${v.id}`}><PauseCircle size={13} /></button>
                        )}
                        {v.status === 'SUSPENDED' && (
                          <button className="btn btn-success btn-sm" onClick={() => updateStatus(v.id, 'ACTIVE')} id={`activate-vendor-${v.id}`}><CheckCircle size={13} /></button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Vendor Detail Modal */}
      {selected && (
        <div className="modal-overlay" onClick={() => setSelected(null)}>
          <div className="modal" style={{ maxWidth: 520 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">Vendor Profile</span>
              <button className="modal-close" onClick={() => setSelected(null)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)', marginBottom: 'var(--space-5)' }}>
                <div style={{ width: 56, height: 56, borderRadius: 'var(--radius-xl)', background: 'var(--primary-light)', color: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 20 }}>
                  {selected.name[0]}
                </div>
                <div>
                  <div style={{ fontWeight: 700, fontSize: 'var(--font-size-lg)' }}>{selected.name}</div>
                  <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-500)' }}>{selected.company}</div>
                  <span className={`tag ${STATUS_META[selected.status]?.cls || 'tag-gray'}`}>{selected.status}</span>
                </div>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)', marginBottom: 'var(--space-4)' }}>
                {[
                  { label: 'Products', value: selected.products, icon: Package },
                  { label: 'Orders', value: selected.orders, icon: ShoppingBag },
                  { label: 'Revenue', value: fmt(selected.revenue), icon: TrendingUp },
                  { label: 'GST', value: selected.gst, icon: Store },
                ].map(row => (
                  <div key={row.label} style={{ background: 'var(--gray-50)', padding: 'var(--space-3)', borderRadius: 'var(--radius-lg)' }}>
                    <div style={{ fontSize: 11, color: 'var(--gray-400)', marginBottom: 2 }}>{row.label}</div>
                    <div style={{ fontWeight: 700, color: 'var(--gray-800)', fontSize: 'var(--font-size-sm)' }}>{row.value}</div>
                  </div>
                ))}
              </div>
              <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-600)' }}>
                <div>📧 {selected.email}</div>
                <div>📞 {selected.phone}</div>
                <div>📅 Joined: {new Date(selected.joinedAt).toLocaleDateString('en-IN')}</div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setSelected(null)}>Close</button>
              {selected.status === 'PENDING' && (
                <>
                  <button className="btn btn-danger" onClick={() => updateStatus(selected.id, 'REJECTED')}>Reject</button>
                  <button className="btn btn-success" onClick={() => updateStatus(selected.id, 'ACTIVE')}>Approve</button>
                </>
              )}
              {selected.status === 'ACTIVE' && (
                <button className="btn btn-danger" onClick={() => updateStatus(selected.id, 'SUSPENDED')}>Suspend</button>
              )}
              {selected.status === 'SUSPENDED' && (
                <button className="btn btn-success" onClick={() => updateStatus(selected.id, 'ACTIVE')}>Re-activate</button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
