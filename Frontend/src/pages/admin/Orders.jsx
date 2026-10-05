import { useState } from 'react';
import { Search, Eye, Filter, X } from 'lucide-react';

const MOCK_ORDERS = [
  { id: 'ORD-1001', customer: 'Rushi Sharma', vendor: 'ABC Electronics', amount: 50000, payment: 'PAID',    paymentMethod: 'UPI',       status: 'SHIPPED',    date: '2024-01-22', items: 2, shipment: 'TRK-001' },
  { id: 'ORD-1002', customer: 'Priya Patel',  vendor: 'XYZ Fashion',     amount: 12500, payment: 'PAID',    paymentMethod: 'Card',      status: 'DELIVERED',  date: '2024-01-21', items: 1, shipment: 'TRK-002' },
  { id: 'ORD-1003', customer: 'Arjun Nair',   vendor: 'Tech Hub',        amount: 189900,payment: 'PAID',    paymentMethod: 'Net Banking',status: 'CONFIRMED', date: '2024-01-21', items: 1, shipment: null },
  { id: 'ORD-1004', customer: 'Sneha K.',      vendor: 'Fashion Plus',    amount: 4999,  payment: 'PENDING', paymentMethod: 'COD',       status: 'PENDING',    date: '2024-01-20', items: 1, shipment: null },
  { id: 'ORD-1005', customer: 'Rahul Gupta',  vendor: 'ABC Electronics', amount: 29990, payment: 'PAID',    paymentMethod: 'UPI',       status: 'PROCESSING', date: '2024-01-20', items: 1, shipment: null },
  { id: 'ORD-1006', customer: 'Meera Iyer',   vendor: 'Sports World',    amount: 14999, payment: 'PAID',    paymentMethod: 'Card',      status: 'CANCELLED',  date: '2024-01-19', items: 2, shipment: null },
];

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const STATUS_META = {
  DELIVERED:  { label: 'Delivered',  cls: 'tag-green'  },
  SHIPPED:    { label: 'Shipped',    cls: 'tag-blue'   },
  CONFIRMED:  { label: 'Confirmed',  cls: 'tag-purple' },
  PROCESSING: { label: 'Processing', cls: 'tag-yellow' },
  PENDING:    { label: 'Pending',    cls: 'tag-orange' },
  CANCELLED:  { label: 'Cancelled',  cls: 'tag-red'    },
};

export default function AdminOrders() {
  const [search, setSearch]   = useState('');
  const [status, setStatus]   = useState('ALL');
  const [selected, setSelected] = useState(null);

  const filtered = MOCK_ORDERS.filter(o => {
    const q = search.toLowerCase();
    if (q && !o.id.toLowerCase().includes(q) && !o.customer.toLowerCase().includes(q)) return false;
    if (status !== 'ALL' && o.status !== status) return false;
    return true;
  });

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Order Management</h1>
          <p className="page-subtitle">{filtered.length} orders</p>
        </div>
      </div>

      {/* Summary */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)', marginBottom: 'var(--space-6)' }}>
        {Object.entries(STATUS_META).slice(0, 4).map(([k, v]) => (
          <div key={k} className="stat-card" style={{ cursor: 'pointer' }} onClick={() => setStatus(k)}>
            <div className="stat-info">
              <div className="stat-label">{v.label}</div>
              <div className="stat-value">{MOCK_ORDERS.filter(o => o.status === k).length}</div>
            </div>
          </div>
        ))}
      </div>

      {/* Filters */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)', display: 'flex', gap: 'var(--space-3)', flexWrap: 'wrap', alignItems: 'center' }}>
        <div className="input-wrapper" style={{ flex: 1, minWidth: 220 }}>
          <Search size={16} className="input-icon-left" />
          <input id="admin-order-search" type="search" className="form-input" placeholder="Search by order ID or customer…"
            value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <select id="admin-order-status" className="form-select" style={{ minWidth: 160 }} value={status} onChange={e => setStatus(e.target.value)}>
          <option value="ALL">All Status</option>
          {Object.entries(STATUS_META).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}
        </select>
        {status !== 'ALL' && (
          <button className="btn btn-secondary btn-sm" onClick={() => setStatus('ALL')}><X size={13} /> Clear</button>
        )}
      </div>

      {/* Table */}
      <div className="card">
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
                <th>Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(o => {
                const sm = STATUS_META[o.status] || { label: o.status, cls: 'tag-gray' };
                return (
                  <tr key={o.id}>
                    <td className="td-primary">{o.id}</td>
                    <td>{o.customer}</td>
                    <td className="text-gray text-sm">{o.vendor}</td>
                    <td className="font-bold">{fmt(o.amount)}</td>
                    <td>
                      <div>
                        <span className={`tag ${o.payment === 'PAID' ? 'tag-green' : 'tag-orange'}`}>{o.payment}</span>
                        <div style={{ fontSize: 10, color: 'var(--gray-400)', marginTop: 2 }}>{o.paymentMethod}</div>
                      </div>
                    </td>
                    <td><span className={`tag ${sm.cls}`}>{sm.label}</span></td>
                    <td className="text-gray text-sm">{new Date(o.date).toLocaleDateString('en-IN')}</td>
                    <td>
                      <button className="btn btn-secondary btn-sm" onClick={() => setSelected(o)} id={`admin-view-order-${o.id}`}>
                        <Eye size={13} /> View
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Order Detail Modal */}
      {selected && (
        <div className="modal-overlay" onClick={() => setSelected(null)}>
          <div className="modal" style={{ maxWidth: 520 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">Order: {selected.id}</span>
              <button className="modal-close" onClick={() => setSelected(null)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)' }}>
                {[
                  ['Customer', selected.customer],
                  ['Vendor', selected.vendor],
                  ['Amount', fmt(selected.amount)],
                  ['Items', selected.items],
                  ['Payment', selected.payment],
                  ['Method', selected.paymentMethod],
                  ['Status', selected.status],
                  ['Shipment', selected.shipment || 'Not yet'],
                  ['Date', new Date(selected.date).toLocaleDateString('en-IN')],
                ].map(([k, v]) => (
                  <div key={k} style={{ background: 'var(--gray-50)', padding: 'var(--space-3)', borderRadius: 'var(--radius-md)' }}>
                    <div style={{ fontSize: 11, color: 'var(--gray-400)' }}>{k}</div>
                    <div style={{ fontWeight: 600, color: 'var(--gray-800)', fontSize: 'var(--font-size-sm)' }}>{v}</div>
                  </div>
                ))}
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setSelected(null)}>Close</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
