import { useState } from 'react';
import { Search, CreditCard, X, Eye } from 'lucide-react';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const MOCK_PAYMENTS = [
  { id: 'TXN-8001', orderId: 'ORD-1001', customer: 'Rushi Sharma', amount: 50000, status: 'SUCCESS', method: 'UPI',         gateway: 'Razorpay', date: '2024-01-22T10:30:00', refundStatus: null },
  { id: 'TXN-8002', orderId: 'ORD-1002', customer: 'Priya Patel',  amount: 12500, status: 'SUCCESS', method: 'Credit Card',  gateway: 'Stripe',   date: '2024-01-21T15:00:00', refundStatus: null },
  { id: 'TXN-8003', orderId: 'ORD-1003', customer: 'Arjun Nair',   amount: 189900,status: 'SUCCESS', method: 'Net Banking',  gateway: 'Razorpay', date: '2024-01-21T09:00:00', refundStatus: null },
  { id: 'TXN-8004', orderId: 'ORD-1004', customer: 'Sneha K.',      amount: 4999,  status: 'PENDING', method: 'COD',          gateway: '-',        date: '2024-01-20T12:00:00', refundStatus: null },
  { id: 'TXN-8005', orderId: 'ORD-1005', customer: 'Rahul Gupta',  amount: 29990, status: 'SUCCESS', method: 'UPI',          gateway: 'Razorpay', date: '2024-01-20T08:00:00', refundStatus: null },
  { id: 'TXN-8006', orderId: 'ORD-1006', customer: 'Meera Iyer',   amount: 14999, status: 'REFUNDED', method: 'Credit Card', gateway: 'Stripe',   date: '2024-01-19T11:00:00', refundStatus: 'COMPLETED' },
  { id: 'TXN-8007', orderId: 'ORD-1007', customer: 'Kumar Rajan',  amount: 8500,  status: 'FAILED',  method: 'Debit Card',   gateway: 'Stripe',   date: '2024-01-18T16:00:00', refundStatus: null },
];

const STATUS_META = {
  SUCCESS:  { label: 'Success',  cls: 'tag-green'  },
  PENDING:  { label: 'Pending',  cls: 'tag-orange' },
  FAILED:   { label: 'Failed',   cls: 'tag-red'    },
  REFUNDED: { label: 'Refunded', cls: 'tag-blue'   },
};

export default function AdminPayments() {
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('ALL');
  const [selected, setSelected] = useState(null);

  const filtered = MOCK_PAYMENTS.filter(p => {
    const q = search.toLowerCase();
    if (q && !p.id.toLowerCase().includes(q) && !p.orderId.toLowerCase().includes(q) && !p.customer.toLowerCase().includes(q)) return false;
    if (status !== 'ALL' && p.status !== status) return false;
    return true;
  });

  const totalSuccess = MOCK_PAYMENTS.filter(p => p.status === 'SUCCESS').reduce((s, p) => s + p.amount, 0);
  const totalFailed  = MOCK_PAYMENTS.filter(p => p.status === 'FAILED').length;
  const totalRefunded = MOCK_PAYMENTS.filter(p => p.status === 'REFUNDED').reduce((s, p) => s + p.amount, 0);

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Payment Management</h1>
          <p className="page-subtitle">All platform transactions</p>
        </div>
      </div>

      {/* KPIs */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)', marginBottom: 'var(--space-6)' }}>
        <div className="stat-card">
          <div className="stat-icon green"><CreditCard size={20} /></div>
          <div className="stat-info"><div className="stat-label">Total Collected</div><div className="stat-value">{fmt(totalSuccess)}</div></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon red"><X size={20} /></div>
          <div className="stat-info"><div className="stat-label">Failed Transactions</div><div className="stat-value">{totalFailed}</div></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon blue"><CreditCard size={20} /></div>
          <div className="stat-info"><div className="stat-label">Total Refunded</div><div className="stat-value">{fmt(totalRefunded)}</div></div>
        </div>
      </div>

      {/* Filters */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)', display: 'flex', gap: 'var(--space-3)', flexWrap: 'wrap' }}>
        <div className="input-wrapper" style={{ flex: 1, minWidth: 220 }}>
          <Search size={16} className="input-icon-left" />
          <input id="payment-search" type="search" className="form-input" placeholder="Search by TXN ID, order, customer…"
            value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <select id="payment-status-filter" className="form-select" style={{ minWidth: 160 }} value={status} onChange={e => setStatus(e.target.value)}>
          <option value="ALL">All Status</option>
          {Object.entries(STATUS_META).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}
        </select>
      </div>

      {/* Table */}
      <div className="card">
        <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table>
            <thead>
              <tr>
                <th>Transaction ID</th>
                <th>Order ID</th>
                <th>Customer</th>
                <th>Amount</th>
                <th>Method</th>
                <th>Gateway</th>
                <th>Status</th>
                <th>Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(p => {
                const sm = STATUS_META[p.status] || { label: p.status, cls: 'tag-gray' };
                return (
                  <tr key={p.id}>
                    <td className="td-primary text-sm">{p.id}</td>
                    <td className="text-gray text-sm">{p.orderId}</td>
                    <td>{p.customer}</td>
                    <td className="font-bold">{fmt(p.amount)}</td>
                    <td className="text-sm">{p.method}</td>
                    <td className="text-gray text-sm">{p.gateway}</td>
                    <td><span className={`tag ${sm.cls}`}>{sm.label}</span></td>
                    <td className="text-gray text-sm">{new Date(p.date).toLocaleDateString('en-IN')}</td>
                    <td>
                      <button className="btn btn-secondary btn-sm" onClick={() => setSelected(p)} id={`view-payment-${p.id}`}><Eye size={13} /></button>
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
          <div className="modal" style={{ maxWidth: 440 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">Transaction: {selected.id}</span>
              <button className="modal-close" onClick={() => setSelected(null)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)' }}>
                {[
                  ['Order ID', selected.orderId],
                  ['Customer', selected.customer],
                  ['Amount', fmt(selected.amount)],
                  ['Method', selected.method],
                  ['Gateway', selected.gateway],
                  ['Status', selected.status],
                  ['Refund', selected.refundStatus || 'N/A'],
                  ['Date', new Date(selected.date).toLocaleString('en-IN')],
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
