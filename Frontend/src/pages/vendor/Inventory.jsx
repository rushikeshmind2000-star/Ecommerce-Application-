import { useState } from 'react';
import { Search, Plus, RefreshCw, X, CheckCircle, AlertTriangle, Warehouse } from 'lucide-react';
import { useApp } from '../../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

export default function VendorInventory() {
  const { products, addToast } = useApp();
  const [search, setSearch] = useState('');
  const [inventory, setInventory] = useState(() =>
    products.reduce((acc, p) => ({ ...acc, [p.id]: { available: p.stock, reserved: p.reserved } }), {})
  );
  const [modal, setModal] = useState(null); // { product, type: 'update' }
  const [qty, setQty]     = useState('');

  const filtered = products.filter(p => {
    const q = search.toLowerCase();
    return !q || p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q);
  });

  const lowStock  = products.filter(p => (inventory[p.id]?.available || 0) > 0 && (inventory[p.id]?.available || 0) <= 5).length;
  const outOfStock = products.filter(p => (inventory[p.id]?.available || 0) === 0).length;

  const updateStock = () => {
    if (!qty || isNaN(qty) || Number(qty) < 0) { addToast('Enter a valid quantity', 'error'); return; }
    setInventory(prev => ({ ...prev, [modal.product.id]: { ...prev[modal.product.id], available: Number(qty) } }));
    addToast(`Stock updated for ${modal.product.name}`, 'success');
    setModal(null);
    setQty('');
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">My Inventory</h1>
          <p className="page-subtitle">Manage your product stock levels</p>
        </div>
      </div>

      {/* Stats */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)', marginBottom: 'var(--space-6)' }}>
        <div className="stat-card"><div className="stat-icon blue"><Warehouse size={20} /></div><div className="stat-info"><div className="stat-label">Total Products</div><div className="stat-value">{products.length}</div></div></div>
        <div className="stat-card"><div className="stat-icon orange"><AlertTriangle size={20} /></div><div className="stat-info"><div className="stat-label">Low Stock (≤5)</div><div className="stat-value">{lowStock}</div></div></div>
        <div className="stat-card"><div className="stat-icon red"><X size={20} /></div><div className="stat-info"><div className="stat-label">Out of Stock</div><div className="stat-value">{outOfStock}</div></div></div>
      </div>

      {/* Search */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)' }}>
        <div className="input-wrapper">
          <Search size={16} className="input-icon-left" />
          <input id="vendor-inventory-search" type="search" className="form-input" placeholder="Search products…"
            value={search} onChange={e => setSearch(e.target.value)} />
        </div>
      </div>

      {/* Table */}
      <div className="card">
        <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table>
            <thead>
              <tr>
                <th>Product</th>
                <th>SKU</th>
                <th>Available</th>
                <th>Reserved</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(p => {
                const inv = inventory[p.id] || { available: 0, reserved: 0 };
                const status = inv.available === 0 ? 'Out of Stock' : inv.available <= 5 ? 'Low Stock' : 'Available';
                const statusCls = inv.available === 0 ? 'tag-red' : inv.available <= 5 ? 'tag-orange' : 'tag-green';
                return (
                  <tr key={p.id}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                        <div style={{ width: 36, height: 36, borderRadius: 'var(--radius-md)', overflow: 'hidden', flexShrink: 0, background: 'var(--gray-100)' }}>
                          <img src={p.images?.[0]?.imageUrl} alt={p.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                        </div>
                        <span className="td-primary" style={{ maxWidth: 180, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{p.name}</span>
                      </div>
                    </td>
                    <td className="text-gray text-sm">{p.sku}</td>
                    <td>
                      <span style={{ fontWeight: 700, color: inv.available === 0 ? 'var(--danger)' : inv.available <= 5 ? 'var(--warning)' : 'var(--success)' }}>
                        {inv.available}
                      </span>
                    </td>
                    <td><span className="tag tag-yellow">{inv.reserved}</span></td>
                    <td><span className={`tag ${statusCls}`}>{status}</span></td>
                    <td>
                      <button className="btn btn-primary btn-sm" id={`update-stock-${p.id}`}
                        onClick={() => { setModal({ product: p }); setQty(String(inv.available)); }}>
                        <RefreshCw size={12} /> Update Stock
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Update Modal */}
      {modal && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" style={{ maxWidth: 400 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">Update Stock</span>
              <button className="modal-close" onClick={() => setModal(null)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              <div style={{ marginBottom: 'var(--space-4)', padding: 'var(--space-4)', background: 'var(--gray-50)', borderRadius: 'var(--radius-lg)' }}>
                <div style={{ fontWeight: 600, color: 'var(--gray-800)', fontSize: 'var(--font-size-sm)' }}>{modal.product.name}</div>
                <div style={{ fontSize: 11, color: 'var(--gray-400)', marginTop: 2 }}>SKU: {modal.product.sku}</div>
              </div>
              <div className="form-group">
                <label className="form-label">New Stock Quantity <span className="required">*</span></label>
                <input id="new-stock-input" type="number" className="form-input" placeholder="Enter new quantity" min="0"
                  value={qty} onChange={e => setQty(e.target.value)} autoFocus />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
              <button className="btn btn-primary" id="confirm-stock-update" onClick={updateStock}>
                <CheckCircle size={14} /> Update
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
