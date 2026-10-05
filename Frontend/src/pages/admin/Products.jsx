import { useState } from 'react';
import { Search, Eye, CheckCircle, XCircle, Package, Star, ToggleLeft, ToggleRight, X } from 'lucide-react';
import { useApp } from '../../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);
const CATEGORIES = ['All', 'Electronics', 'Footwear', 'Audio', 'Laptops', 'Apparel'];
const VENDORS = ['All', 'ABC Electronics', 'Sports World', 'Tech Hub', 'Fashion Plus'];
const STATUS_OPTIONS = ['ALL', 'ACTIVE', 'INACTIVE', 'PENDING_APPROVAL', 'REJECTED', 'OUT_OF_STOCK'];

export default function AdminProducts() {
  const { products, approveProduct, rejectProduct, addToast } = useApp();
  const [search, setSearch]     = useState('');
  const [category, setCategory] = useState('All');
  const [vendor, setVendor]     = useState('All');
  const [status, setStatus]     = useState('ALL');
  const [selected, setSelected] = useState(null);

  const filtered = products.filter(p => {
    const q = search.toLowerCase();
    if (q && !p.name.toLowerCase().includes(q) && !p.brand.toLowerCase().includes(q)) return false;
    if (category !== 'All' && p.category !== category) return false;
    if (status !== 'ALL' && p.status !== status) return false;
    return true;
  });

  const toggleActive = (id) => {
    // Need a global toggle function in a real app.
    addToast('Product status updated', 'success');
  };

  const [rejectModal, setRejectModal] = useState(null); // { id: string }
  const [rejectReason, setRejectReason] = useState('');

  const handleReject = () => {
    if (!rejectReason.trim()) { addToast('Reason is required', 'error'); return; }
    rejectProduct(rejectModal.id, rejectReason);
    setRejectModal(null);
    setRejectReason('');
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Product Management</h1>
          <p className="page-subtitle">{filtered.length} products found</p>
        </div>
      </div>

      {/* Filters */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)', display: 'flex', gap: 'var(--space-3)', flexWrap: 'wrap', alignItems: 'center' }}>
        <div className="input-wrapper" style={{ flex: 1, minWidth: 220 }}>
          <Search size={16} className="input-icon-left" />
          <input id="admin-product-search" type="search" className="form-input" placeholder="Search products…"
            value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <select id="admin-product-category" className="form-select" style={{ minWidth: 140 }} value={category} onChange={e => setCategory(e.target.value)}>
          {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
        <select id="admin-product-vendor" className="form-select" style={{ minWidth: 150 }} value={vendor} onChange={e => setVendor(e.target.value)}>
          {VENDORS.map(v => <option key={v} value={v}>{v}</option>)}
        </select>
        <select id="admin-product-status" className="form-select" style={{ minWidth: 140 }} value={status} onChange={e => setStatus(e.target.value)}>
          {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s === 'ALL' ? 'All Status' : s.replace('_', ' ')}</option>)}
        </select>
      </div>

      {/* Table */}
      <div className="card">
        <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table>
            <thead>
              <tr>
                <th>Product</th>
                <th>Vendor</th>
                <th>Category</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Rating</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(p => (
                <tr key={p.id}>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                      <div style={{ width: 38, height: 38, borderRadius: 'var(--radius-md)', overflow: 'hidden', flexShrink: 0, background: 'var(--gray-100)' }}>
                        <img src={p.images?.[0]?.imageUrl} alt={p.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      </div>
                      <div>
                        <div className="td-primary" style={{ maxWidth: 180, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{p.name}</div>
                        <div style={{ fontSize: 11, color: 'var(--gray-400)' }}>{p.sku}</div>
                      </div>
                    </div>
                  </td>
                  <td className="text-sm">{p.vendorName || p.brand}</td>
                  <td className="text-sm">{p.category}</td>
                  <td className="font-bold">{fmt(p.price)}</td>
                  <td>
                    <span style={{ fontWeight: 700, color: p.stock === 0 ? 'var(--danger)' : p.stock <= 5 ? 'var(--warning)' : 'var(--success)' }}>
                      {p.stock}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: 'var(--warning)' }}>
                      <Star size={12} fill="currentColor" /> {p.rating}
                    </div>
                  </td>
                  <td>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                      <span className={`tag ${p.status === 'ACTIVE' ? 'tag-green' : p.status === 'OUT_OF_STOCK' || p.status === 'REJECTED' ? 'tag-red' : p.status === 'PENDING_APPROVAL' ? 'tag-orange' : 'tag-gray'}`}>
                        {p.status.replace('_', ' ')}
                      </span>
                      {p.status === 'REJECTED' && p.rejectionReason && (
                        <span style={{ fontSize: 10, color: 'var(--danger)' }} title={p.rejectionReason}>
                          Reason: {p.rejectionReason.length > 20 ? p.rejectionReason.substring(0,20)+'...' : p.rejectionReason}
                        </span>
                      )}
                    </div>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
                      <button className="btn btn-secondary btn-sm" onClick={() => setSelected(p)} id={`admin-view-product-${p.id}`}><Eye size={13} /></button>
                      {p.status === 'PENDING_APPROVAL' ? (
                        <>
                          <button className="btn btn-success btn-sm" onClick={() => approveProduct(p.id)}><CheckCircle size={13} /> Approve</button>
                          <button className="btn btn-danger btn-sm" onClick={() => setRejectModal({ id: p.id })}><XCircle size={13} /> Reject</button>
                        </>
                      ) : (p.status === 'ACTIVE' || p.status === 'INACTIVE' || p.status === 'OUT_OF_STOCK') ? (
                        <button className="btn btn-sm" id={`admin-toggle-product-${p.id}`}
                          style={{ background: p.status === 'ACTIVE' ? 'var(--danger-light)' : 'var(--success-light)', color: p.status === 'ACTIVE' ? 'var(--danger)' : 'var(--success)', border: 'none', padding: '4px 10px', borderRadius: 'var(--radius-md)', cursor: 'pointer', fontSize: 12 }}
                          onClick={() => toggleActive(p.id)}>
                          {p.status === 'ACTIVE' ? <><ToggleRight size={13} /> Active</> : <><ToggleLeft size={13} /> Inactive</>}
                        </button>
                      ) : null}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Product Detail Modal */}
      {selected && (
        <div className="modal-overlay" onClick={() => setSelected(null)}>
          <div className="modal" style={{ maxWidth: 580 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">Product Details</span>
              <button className="modal-close" onClick={() => setSelected(null)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              <div style={{ display: 'flex', gap: 'var(--space-5)', marginBottom: 'var(--space-5)' }}>
                <div style={{ width: 100, height: 100, borderRadius: 'var(--radius-xl)', overflow: 'hidden', flexShrink: 0, background: 'var(--gray-100)' }}>
                  <img src={selected.images?.[0]?.imageUrl} alt={selected.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                </div>
                <div>
                  <div style={{ fontWeight: 700, fontSize: 'var(--font-size-lg)', color: 'var(--gray-900)', marginBottom: 4 }}>{selected.name}</div>
                  <div style={{ color: 'var(--gray-500)', fontSize: 'var(--font-size-sm)', marginBottom: 8 }}>{selected.description}</div>
                  <div style={{ fontSize: 'var(--font-size-xl)', fontWeight: 800, color: 'var(--primary)' }}>{fmt(selected.price)}</div>
                </div>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)' }}>
                {[
                  ['Brand', selected.brand],
                  ['Category', selected.category],
                  ['SKU', selected.sku],
                  ['Stock', selected.stock],
                  ['Rating', `⭐ ${selected.rating} (${selected.reviews} reviews)`],
                  ['Status', selected.status],
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
              <button className="btn btn-danger" onClick={() => { setSelected(null); addToast('Product removed from marketplace', 'success'); }}>Remove</button>
              <button className={`btn ${selected.status === 'ACTIVE' ? 'btn-primary' : 'btn-success'}`} onClick={() => { toggleActive(selected.id); setSelected(null); }}>
                {selected.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
              </button>
            </div>
          </div>
        </div>
      )}
      {/* Rejection Modal */}
      {rejectModal && (
        <div className="modal-overlay" onClick={() => setRejectModal(null)}>
          <div className="modal" style={{ maxWidth: 400 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">Reject Product</span>
              <button className="modal-close" onClick={() => setRejectModal(null)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Reason for Rejection <span className="required">*</span></label>
                <textarea className="form-input" style={{ minHeight: 80, resize: 'vertical' }}
                  placeholder="Enter reason for rejection..." value={rejectReason} onChange={e => setRejectReason(e.target.value)} />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setRejectModal(null)}>Cancel</button>
              <button className="btn btn-danger" onClick={handleReject}>Confirm Rejection</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
