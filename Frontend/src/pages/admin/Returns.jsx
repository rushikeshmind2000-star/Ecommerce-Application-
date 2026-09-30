import { useState } from 'react';
import { Search, AlertTriangle, CheckCircle, X, Eye } from 'lucide-react';

const MOCK_RETURNS = [
  { id: 'RET-001', orderId: 'ORD-1001', customer: 'Rushi Sharma', product: 'Apple iPhone 15 Pro', reason: 'Defective product - screen flickering', requestedAt: '2024-01-25', status: 'PENDING', amount: 50000 },
  { id: 'RET-002', orderId: 'ORD-1002', customer: 'Priya Patel',  product: 'Nike Air Max 2024', reason: 'Wrong size delivered', requestedAt: '2024-01-24', status: 'APPROVED', amount: 12999, refundStatus: 'PROCESSING' },
  { id: 'RET-003', orderId: 'ORD-1005', customer: 'Rahul Gupta',  product: 'Sony WH-1000XM5',   reason: 'Not as described', requestedAt: '2024-01-23', status: 'REJECTED', amount: 29990 },
  { id: 'RET-004', orderId: 'ORD-1003', customer: 'Arjun Nair',   product: 'MacBook Pro 14"',   reason: 'Changed mind', requestedAt: '2024-01-22', status: 'PENDING', amount: 189900 },
];

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);
const STATUS_META = {
  PENDING:  { label: 'Pending',  cls: 'tag-orange' },
  APPROVED: { label: 'Approved', cls: 'tag-green'  },
  REJECTED: { label: 'Rejected', cls: 'tag-red'    },
};

export default function AdminReturns() {
  const [returns, setReturns] = useState(MOCK_RETURNS);
  const [selected, setSelected] = useState(null);
  const [search, setSearch] = useState('');

  const filtered = returns.filter(r => {
    const q = search.toLowerCase();
    return !q || r.id.toLowerCase().includes(q) || r.customer.toLowerCase().includes(q) || r.product.toLowerCase().includes(q);
  });

  const updateStatus = (id, newStatus) => {
    setReturns(prev => prev.map(r => r.id === id ? { ...r, status: newStatus, refundStatus: newStatus === 'APPROVED' ? 'PROCESSING' : undefined } : r));
    setSelected(null);
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Returns & Refunds</h1>
          <p className="page-subtitle">{returns.filter(r => r.status === 'PENDING').length} pending requests</p>
        </div>
      </div>

      {/* Stats */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)', marginBottom: 'var(--space-6)' }}>
        <div className="stat-card"><div className="stat-icon orange"><AlertTriangle size={20} /></div><div className="stat-info"><div className="stat-label">Pending</div><div className="stat-value">{returns.filter(r => r.status === 'PENDING').length}</div></div></div>
        <div className="stat-card"><div className="stat-icon green"><CheckCircle size={20} /></div><div className="stat-info"><div className="stat-label">Approved</div><div className="stat-value">{returns.filter(r => r.status === 'APPROVED').length}</div></div></div>
        <div className="stat-card"><div className="stat-icon red"><X size={20} /></div><div className="stat-info"><div className="stat-label">Rejected</div><div className="stat-value">{returns.filter(r => r.status === 'REJECTED').length}</div></div></div>
      </div>

      {/* Search */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)' }}>
        <div className="input-wrapper">
          <Search size={16} className="input-icon-left" />
          <input id="returns-search" type="search" className="form-input" placeholder="Search by return ID, customer, product…"
            value={search} onChange={e => setSearch(e.target.value)} />
        </div>
      </div>

      <div className="card">
        <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table>
            <thead>
              <tr>
                <th>Return ID</th>
                <th>Order</th>
                <th>Customer</th>
                <th>Product</th>
                <th>Reason</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(r => {
                const sm = STATUS_META[r.status] || { label: r.status, cls: 'tag-gray' };
                return (
                  <tr key={r.id}>
                    <td className="td-primary">{r.id}</td>
                    <td className="text-gray text-sm">{r.orderId}</td>
                    <td>{r.customer}</td>
                    <td style={{ maxWidth: 150, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{r.product}</td>
                    <td style={{ maxWidth: 180, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', color: 'var(--gray-500)', fontSize: 12 }}>{r.reason}</td>
                    <td className="font-bold">{fmt(r.amount)}</td>
                    <td><span className={`tag ${sm.cls}`}>{sm.label}</span></td>
                    <td className="text-gray text-sm">{new Date(r.requestedAt).toLocaleDateString('en-IN')}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => setSelected(r)} id={`view-return-${r.id}`}><Eye size={13} /></button>
                        {r.status === 'PENDING' && (
                          <>
                            <button className="btn btn-success btn-sm" onClick={() => updateStatus(r.id, 'APPROVED')} id={`approve-return-${r.id}`}><CheckCircle size={13} /></button>
                            <button className="btn btn-danger btn-sm" onClick={() => updateStatus(r.id, 'REJECTED')} id={`reject-return-${r.id}`}><X size={13} /></button>
                          </>
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
          <div className="modal" style={{ maxWidth: 480 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">Return: {selected.id}</span>
              <button className="modal-close" onClick={() => setSelected(null)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              <div style={{ display: 'grid', gap: 'var(--space-3)' }}>
                {[
                  ['Order ID', selected.orderId], ['Customer', selected.customer], ['Product', selected.product],
                  ['Amount', fmt(selected.amount)], ['Status', selected.status], ['Refund', selected.refundStatus || 'N/A'],
                  ['Reason', selected.reason], ['Requested', new Date(selected.requestedAt).toLocaleDateString('en-IN')],
                ].map(([k, v]) => (
                  <div key={k} style={{ display: 'flex', gap: 'var(--space-3)', padding: 'var(--space-2) 0', borderBottom: '1px solid var(--gray-100)' }}>
                    <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-400)', minWidth: 90 }}>{k}</span>
                    <span style={{ fontWeight: 600, color: 'var(--gray-800)', fontSize: 'var(--font-size-sm)' }}>{v}</span>
                  </div>
                ))}
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setSelected(null)}>Close</button>
              {selected.status === 'PENDING' && (
                <>
                  <button className="btn btn-danger" onClick={() => updateStatus(selected.id, 'REJECTED')}>Reject</button>
                  <button className="btn btn-success" onClick={() => updateStatus(selected.id, 'APPROVED')}>Approve Refund</button>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
