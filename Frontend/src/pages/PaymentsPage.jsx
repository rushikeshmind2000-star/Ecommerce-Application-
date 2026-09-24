import { useState } from 'react';
import { CreditCard, CheckCircle, XCircle, RefreshCw, Search, Eye, Plus, X } from 'lucide-react';
import { useApp } from '../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const PAYMENT_STATUS_META = {
  INITIATED:  { label: 'Initiated',  class: 'tag-yellow' },
  PROCESSING: { label: 'Processing', class: 'tag-blue'   },
  SUCCESS:    { label: 'Success',    class: 'tag-green'  },
  FAILED:     { label: 'Failed',     class: 'tag-red'    },
  REFUNDED:   { label: 'Refunded',   class: 'tag-purple' },
};

const MOCK_PAYMENTS = [
  { id:'pay1', orderId:'o1', userId:'u1', paymentReference:'PAY-REF-001', amount:134900, currency:'INR', paymentMethod:'UPI', status:'SUCCESS',    createdAt:'2024-01-15T10:31:00', updatedAt:'2024-01-15T10:31:30' },
  { id:'pay2', orderId:'o2', userId:'u1', paymentReference:'PAY-REF-002', amount:42989,  currency:'INR', paymentMethod:'Credit Card', status:'SUCCESS',    createdAt:'2024-01-18T15:05:00', updatedAt:'2024-01-18T15:05:45' },
  { id:'pay3', orderId:'o3', userId:'u1', paymentReference:'PAY-REF-003', amount:189900, currency:'INR', paymentMethod:'Net Banking', status:'INITIATED',  createdAt:'2024-01-22T08:01:00', updatedAt:'2024-01-22T08:01:00' },
  { id:'pay4', orderId:'o4', userId:'u1', paymentReference:'PAY-REF-004', amount:5999,   currency:'INR', paymentMethod:'UPI', status:'PROCESSING', createdAt:'2024-01-23T12:01:00', updatedAt:'2024-01-23T12:01:00' },
];

export default function PaymentsPage() {
  const { orders, addToast } = useApp();
  const [payments, setPayments] = useState(MOCK_PAYMENTS);
  const [search, setSearch]   = useState('');
  const [modal,  setModal]    = useState(null); // 'initiate' | 'refund' | { type: 'detail', payment }
  const [form, setForm]       = useState({ orderId:'', userId:'', amount:'', currency:'INR', paymentMethod:'UPI' });
  const [refundReason, setRefundReason] = useState('');
  const [selectedPay, setSelectedPay]  = useState(null);

  const filtered = payments.filter(p => {
    const q = search.toLowerCase();
    return !q || p.paymentReference.toLowerCase().includes(q) || p.orderId.toLowerCase().includes(q) || p.status.toLowerCase().includes(q);
  });

  // PaymentInitiateRequest fields
  const handleInitiate = () => {
    if (!form.orderId || !form.amount || Number(form.amount) < 0.01) { addToast('Please fill all required fields', 'error'); return; }
    const newPay = {
      id: 'pay' + Date.now(), orderId: form.orderId, userId: 'u1',
      paymentReference: 'PAY-REF-' + String(payments.length + 1).padStart(3, '0'),
      amount: Number(form.amount) * 100, currency: form.currency, paymentMethod: form.paymentMethod,
      status: 'INITIATED', createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(),
    };
    setPayments(prev => [newPay, ...prev]);
    addToast('Payment initiated successfully', 'success');
    setModal(null);
    setForm({ orderId:'', userId:'', amount:'', currency:'INR', paymentMethod:'UPI' });
  };

  // RefundRequest field: reason
  const handleRefund = () => {
    if (!refundReason.trim()) { addToast('Refund reason is required', 'error'); return; }
    setPayments(prev => prev.map(p => p.id === selectedPay.id ? { ...p, status: 'REFUNDED', updatedAt: new Date().toISOString() } : p));
    addToast('Refund initiated successfully', 'success');
    setModal(null);
    setRefundReason('');
  };

  const successTotal = payments.filter(p => p.status === 'SUCCESS').reduce((s, p) => s + p.amount, 0);
  const refundedTotal = payments.filter(p => p.status === 'REFUNDED').reduce((s, p) => s + p.amount, 0);

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Payments</h1>
          <p className="page-subtitle">Track all payment transactions</p>
        </div>
        <div className="page-actions">
          <button id="initiate-payment-btn" className="btn btn-primary" onClick={() => setModal('initiate')}>
            <Plus size={16} /> Initiate Payment
          </button>
        </div>
      </div>

      {/* Stats */}
      <div className="stats-grid">
        <div className="stat-card"><div className="stat-icon green"><CheckCircle size={22} /></div><div className="stat-info"><div className="stat-label">Total Collected</div><div className="stat-value">{fmt(successTotal)}</div></div></div>
        <div className="stat-card"><div className="stat-icon orange"><CreditCard size={22} /></div><div className="stat-info"><div className="stat-label">Total Payments</div><div className="stat-value">{payments.length}</div></div></div>
        <div className="stat-card"><div className="stat-icon blue"><RefreshCw size={22} /></div><div className="stat-info"><div className="stat-label">Pending</div><div className="stat-value">{payments.filter(p => ['INITIATED','PROCESSING'].includes(p.status)).length}</div></div></div>
        <div className="stat-card"><div className="stat-icon red"><XCircle size={22} /></div><div className="stat-info"><div className="stat-label">Refunded</div><div className="stat-value">{fmt(refundedTotal)}</div></div></div>
      </div>

      {/* Search */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)' }}>
        <div className="input-wrapper">
          <Search size={16} className="input-icon-left" />
          <input id="payment-search" type="search" className="form-input" placeholder="Search by reference, order ID or status…" value={search} onChange={e => setSearch(e.target.value)} />
        </div>
      </div>

      {/* Table — PaymentResponse fields */}
      <div className="card">
        <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table>
            <thead>
              <tr>
                <th>Reference</th>
                <th>Order ID</th>
                <th>Amount</th>
                <th>Currency</th>
                <th>Method</th>
                <th>Status</th>
                <th>Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(pay => {
                const m = PAYMENT_STATUS_META[pay.status] || { label: pay.status, class: 'tag-gray' };
                return (
                  <tr key={pay.id}>
                    <td className="td-primary">{pay.paymentReference}</td>
                    <td className="text-gray text-sm">{pay.orderId}</td>
                    <td className="font-bold">{fmt(pay.amount)}</td>
                    <td>{pay.currency}</td>
                    <td><span className="tag tag-gray">{pay.paymentMethod}</span></td>
                    <td><span className={`tag ${m.class}`}>{m.label}</span></td>
                    <td className="text-gray text-sm">{new Date(pay.createdAt).toLocaleDateString('en-IN')}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 4 }}>
                        <button id={`view-payment-${pay.id}`} className="btn btn-secondary btn-sm"
                          onClick={() => { setSelectedPay(pay); setModal('detail'); }}><Eye size={12} /></button>
                        {pay.status === 'SUCCESS' && (
                          <button id={`refund-payment-${pay.id}`} className="btn btn-danger btn-sm"
                            onClick={() => { setSelectedPay(pay); setModal('refund'); }}>
                            <RefreshCw size={12} /> Refund
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

      {/* Initiate Payment Modal — PaymentInitiateRequest */}
      {modal === 'initiate' && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header"><span className="modal-title">Initiate Payment</span><button className="modal-close" onClick={() => setModal(null)}><X size={16} /></button></div>
            <div className="modal-body">
              <div className="form-group">
                <label className="form-label">Order ID <span className="required">*</span></label>
                <select id="pay-order" className="form-select" value={form.orderId} onChange={e => setForm(f => ({ ...f, orderId: e.target.value }))}>
                  <option value="">Select order…</option>
                  {orders.map(o => <option key={o.id} value={o.id}>{o.orderNumber} — {fmt(o.totalAmount)}</option>)}
                </select>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Amount (₹) <span className="required">*</span></label>
                  <input id="pay-amount" type="number" className="form-input" placeholder="0.01 min" min="0.01" step="0.01"
                    value={form.amount} onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} />
                  <span className="form-hint">Must be ≥ 0.01</span>
                </div>
                <div className="form-group">
                  <label className="form-label">Currency</label>
                  <select id="pay-currency" className="form-select" value={form.currency} onChange={e => setForm(f => ({ ...f, currency: e.target.value }))}>
                    {['INR','USD','EUR','GBP'].map(c => <option key={c}>{c}</option>)}
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Payment Method <span className="required">*</span></label>
                <select id="pay-method" className="form-select" value={form.paymentMethod} onChange={e => setForm(f => ({ ...f, paymentMethod: e.target.value }))}>
                  {['UPI','Credit Card','Debit Card','Net Banking','Cash on Delivery'].map(m => <option key={m}>{m}</option>)}
                </select>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
              <button id="submit-payment-btn" className="btn btn-primary" onClick={handleInitiate}><CreditCard size={15} /> Initiate Payment</button>
            </div>
          </div>
        </div>
      )}

      {/* Refund Modal — RefundRequest */}
      {modal === 'refund' && selectedPay && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header"><span className="modal-title">Initiate Refund</span><button className="modal-close" onClick={() => setModal(null)}><X size={16} /></button></div>
            <div className="modal-body">
              <div style={{ background: 'var(--danger-light)', borderRadius: 'var(--radius-lg)', padding: 'var(--space-4)', marginBottom: 'var(--space-5)', border: '1px solid #fecaca' }}>
                <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--danger)' }}>Refund Amount: {fmt(selectedPay.amount)}</div>
                <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--gray-600)' }}>Reference: {selectedPay.paymentReference}</div>
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="refund-reason">Refund Reason <span className="required">*</span></label>
                <textarea id="refund-reason" className="form-textarea" placeholder="Please provide a reason for the refund…"
                  value={refundReason} onChange={e => setRefundReason(e.target.value)} rows={3} />
                <span className="form-hint">This field is required (RefundRequest.reason)</span>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
              <button id="submit-refund-btn" className="btn btn-danger" onClick={handleRefund}><RefreshCw size={15} /> Confirm Refund</button>
            </div>
          </div>
        </div>
      )}

      {/* Detail Modal — PaymentResponse */}
      {modal === 'detail' && selectedPay && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header"><span className="modal-title">Payment Details</span><button className="modal-close" onClick={() => setModal(null)}><X size={16} /></button></div>
            <div className="modal-body">
              {[
                ['Payment ID',   selectedPay.id],
                ['Order ID',     selectedPay.orderId],
                ['User ID',      selectedPay.userId],
                ['Reference',    selectedPay.paymentReference],
                ['Amount',       fmt(selectedPay.amount)],
                ['Currency',     selectedPay.currency],
                ['Method',       selectedPay.paymentMethod],
                ['Status',       selectedPay.status],
                ['Created At',   new Date(selectedPay.createdAt).toLocaleString('en-IN')],
                ['Updated At',   new Date(selectedPay.updatedAt).toLocaleString('en-IN')],
              ].map(([k, v]) => (
                <div key={k} className="summary-row">
                  <span className="text-gray text-sm">{k}</span>
                  <span style={{ fontWeight: 600, fontSize: 'var(--font-size-sm)', color: 'var(--gray-800)' }}>{v}</span>
                </div>
              ))}
            </div>
            <div className="modal-footer">
              <button className="btn btn-primary" onClick={() => setModal(null)}>Close</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
