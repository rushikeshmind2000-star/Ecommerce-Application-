import { useState } from 'react';
import { Search, Eye, CheckCircle, X } from 'lucide-react';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const MOCK_ORDERS = [
  { id: 'ORD-1001', product: 'Apple iPhone 15 Pro', qty: 2, amount: 269800, status: 'PENDING',   customer: 'Rushi Sharma', address: '123 MG Road, Bengaluru', date: '2024-01-22' },
  { id: 'ORD-1002', product: 'Nike Air Max 2024',   qty: 1, amount: 12999,  status: 'SHIPPED',   customer: 'Priya Patel',  address: '456 Park St, Mumbai',   date: '2024-01-21' },
  { id: 'ORD-1003', product: 'MacBook Pro 14"',     qty: 1, amount: 189900, status: 'CONFIRMED', customer: 'Arjun Nair',   address: '789 Anna Salai, Chennai',date: '2024-01-21' },
  { id: 'ORD-1004', product: 'Sony WH-1000XM5',     qty: 3, amount: 89970,  status: 'DELIVERED', customer: 'Meera Iyer',   address: '12 DP Road, Pune',      date: '2024-01-20' },
];

const STATUS_META = {
  PENDING:   { label: 'Pending',   cls: 'tag-orange' },
  CONFIRMED: { label: 'Confirmed', cls: 'tag-purple' },
  READY:     { label: 'Ready',     cls: 'tag-blue'   },
  SHIPPED:   { label: 'Shipped',   cls: 'tag-blue'   },
  DELIVERED: { label: 'Delivered', cls: 'tag-green'  },
  CANCELLED: { label: 'Cancelled', cls: 'tag-red'    },
};

export default function VendorOrders() {
  const [orders, setOrders] = useState(MOCK_ORDERS);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('ALL');
  const [selected, setSelected] = useState(null);

  const filtered = orders.filter(o => {
    const q = search.toLowerCase();
    if (q && !o.id.toLowerCase().includes(q) && !o.product.toLowerCase().includes(q)) return false;
    if (status !== 'ALL' && o.status !== status) return false;
    return true;
  });

  const advanceStatus = (id) => {
    const flow = { PENDING: 'CONFIRMED', CONFIRMED: 'READY', READY: 'SHIPPED', SHIPPED: 'DELIVERED' };
    setOrders(prev => prev.map(o => o.id === id && flow[o.status] ? { ...o, status: flow[o.status] } : o));
    setSelected(null);
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Vendor Orders</h1>
          <p className="page-subtitle">Orders containing your products</p>
        </div>
      </div>

      {/* Stats */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)', marginBottom: 'var(--space-6)' }}>
        {['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED'].map(s => (
          <div key={s} className="stat-card" style={{ cursor: 'pointer' }} onClick={() => setStatus(s)}>
            <div className="stat-info">
              <div className="stat-label">{STATUS_META[s]?.label || s}</div>
              <div className="stat-value">{orders.filter(o => o.status === s).length}</div>
            </div>
          </div>
        ))}
      </div>

      {/* Filters */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)', display: 'flex', gap: 'var(--space-3)', flexWrap: 'wrap' }}>
        <div className="input-wrapper" style={{ flex: 1, minWidth: 220 }}>
          <Search size={16} className="input-icon-left" />
          <input id="vendor-order-search" type="search" className="form-input" placeholder="Search by order or product…"
            value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <select id="vendor-order-status" className="form-select" style={{ minWidth: 160 }} value={status} onChange={e => setStatus(e.target.value)}>
          <option value="ALL">All Status</option>
          {Object.entries(STATUS_META).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}
        </select>
        {status !== 'ALL' && <button className="btn btn-secondary btn-sm" onClick={() => setStatus('ALL')}><X size={13} /></button>}
      </div>

      <div className="card">
        <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table>
            <thead>
              <tr><th>Order ID</th><th>Product</th><th>Qty</th><th>Amount</th><th>Status</th><th>Date</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {filtered.map(o => {
                const sm = STATUS_META[o.status] || { label: o.status, cls: 'tag-gray' };
                return (
                  <tr key={o.id}>
                    <td className="td-primary">{o.id}</td>
                    <td>{o.product}</td>
                    <td>{o.qty}</td>
                    <td className="font-bold">{fmt(o.amount)}</td>
                    <td><span className={`tag ${sm.cls}`}>{sm.label}</span></td>
                    <td className="text-gray text-sm">{new Date(o.date).toLocaleDateString('en-IN')}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => setSelected(o)} id={`vendor-view-order-${o.id}`}><Eye size={13} /></button>
                        {['PENDING', 'CONFIRMED', 'READY'].includes(o.status) && (
                          <button className="btn btn-primary btn-sm" onClick={() => advanceStatus(o.id)} id={`vendor-advance-order-${o.id}`}>
                            <CheckCircle size={13} /> {o.status === 'PENDING' ? 'Confirm' : o.status === 'CONFIRMED' ? 'Mark Ready' : 'Ship'}
                          </button>
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

      {selected && (
        <div className="modal-overlay" onClick={() => setSelected(null)}>
          <div className="modal" style={{ maxWidth: 460 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">Order: {selected.id}</span>
              <button className="modal-close" onClick={() => setSelected(null)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              {[
                ['Product', selected.product], ['Customer', selected.customer],
                ['Qty', selected.qty], ['Amount', fmt(selected.amount)],
                ['Status', selected.status], ['Ship To', selected.address],
                ['Date', new Date(selected.date).toLocaleDateString('en-IN')],
              ].map(([k, v]) => (
                <div key={k} style={{ display: 'flex', gap: 'var(--space-3)', padding: 'var(--space-2) 0', borderBottom: '1px solid var(--gray-100)' }}>
                  <span style={{ color: 'var(--gray-400)', fontSize: 'var(--font-size-sm)', minWidth: 80 }}>{k}</span>
                  <span style={{ fontWeight: 600, color: 'var(--gray-800)', fontSize: 'var(--font-size-sm)' }}>{v}</span>
                </div>
              ))}
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setSelected(null)}>Close</button>
              {['PENDING', 'CONFIRMED', 'READY'].includes(selected.status) && (
                <button className="btn btn-primary" onClick={() => advanceStatus(selected.id)}>
                  {selected.status === 'PENDING' ? 'Confirm Order' : selected.status === 'CONFIRMED' ? 'Mark Ready' : 'Mark Shipped'}
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
