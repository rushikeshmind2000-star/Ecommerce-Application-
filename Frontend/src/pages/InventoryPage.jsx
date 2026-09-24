import { useState } from 'react';
import {
  Warehouse, Plus, Search, AlertTriangle, Package,
  CheckCircle, TrendingDown, Edit2, Save, X, RefreshCw
} from 'lucide-react';
import { useApp } from '../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

function getLevel(available, total) {
  if (total === 0) return 'low';
  const pct = (available / total) * 100;
  if (pct > 50) return 'high';
  if (pct > 20) return 'medium';
  return 'low';
}

export default function InventoryPage() {
  const { products, addToast } = useApp();
  const [search, setSearch]   = useState('');
  const [modal,  setModal]    = useState(null); // { type: 'add'|'reserve'|'confirm'|'release', productId }
  const [formData, setFormData] = useState({ productId: '', quantity: '', orderId: '' });
  const [editId, setEditId]   = useState(null);
  const [inventory, setInventory] = useState(() =>
    products.reduce((acc, p) => ({
      ...acc,
      [p.id]: { availableQuantity: p.stock, reservedQuantity: p.reserved, soldQuantity: p.sold }
    }), {})
  );

  const filtered = products.filter(p => {
    const q = search.toLowerCase();
    return !q || p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q);
  });

  const totalValue = products.reduce((s, p) => s + p.price * (inventory[p.id]?.availableQuantity || 0), 0);
  const outOfStock = products.filter(p => (inventory[p.id]?.availableQuantity || 0) === 0).length;
  const lowStock   = products.filter(p => { const inv = inventory[p.id]; return inv && inv.availableQuantity > 0 && inv.availableQuantity <= 5; }).length;

  // InventoryRequest: productId, quantity
  const handleAddStock = () => {
    const { productId, quantity } = formData;
    if (!productId || !quantity || Number(quantity) < 1) { addToast('Invalid input', 'error'); return; }
    setInventory(prev => ({
      ...prev,
      [productId]: { ...prev[productId], availableQuantity: (prev[productId]?.availableQuantity || 0) + Number(quantity) }
    }));
    addToast(`Stock added for ${products.find(p => p.id === productId)?.name}`, 'success');
    setModal(null);
    setFormData({ productId: '', quantity: '', orderId: '' });
  };

  // ReserveStockRequest: orderId, productId, quantity
  const handleReserve = () => {
    const { productId, quantity, orderId } = formData;
    if (!productId || !quantity || !orderId) { addToast('All fields required', 'error'); return; }
    const inv = inventory[productId];
    if (inv.availableQuantity < Number(quantity)) { addToast('Insufficient available stock', 'error'); return; }
    setInventory(prev => ({
      ...prev,
      [productId]: { ...prev[productId], availableQuantity: prev[productId].availableQuantity - Number(quantity), reservedQuantity: prev[productId].reservedQuantity + Number(quantity) }
    }));
    addToast('Stock reserved successfully', 'success');
    setModal(null);
  };

  // ConfirmStockRequest: orderId
  const handleConfirm = () => {
    const { productId } = formData;
    const inv = inventory[productId];
    setInventory(prev => ({
      ...prev,
      [productId]: { ...prev[productId], reservedQuantity: 0, soldQuantity: prev[productId].soldQuantity + prev[productId].reservedQuantity }
    }));
    addToast('Stock confirmed / moved to sold', 'success');
    setModal(null);
  };

  // ReleaseStockRequest: orderId
  const handleRelease = () => {
    const { productId } = formData;
    const inv = inventory[productId];
    setInventory(prev => ({
      ...prev,
      [productId]: { ...prev[productId], availableQuantity: prev[productId].availableQuantity + prev[productId].reservedQuantity, reservedQuantity: 0 }
    }));
    addToast('Reserved stock released back to available', 'info');
    setModal(null);
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Inventory Management</h1>
          <p className="page-subtitle">Monitor stock levels across all products</p>
        </div>
        <div className="page-actions">
          <button id="add-stock-btn" className="btn btn-primary" onClick={() => setModal('add')}>
            <Plus size={16} /> Add Stock
          </button>
          <button id="reserve-stock-btn" className="btn btn-secondary" onClick={() => setModal('reserve')}>
            Reserve Stock
          </button>
        </div>
      </div>

      {/* Stats */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon blue"><Warehouse size={22} /></div>
          <div className="stat-info"><div className="stat-label">Total Products</div><div className="stat-value">{products.length}</div></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon green"><CheckCircle size={22} /></div>
          <div className="stat-info"><div className="stat-label">Inventory Value</div><div className="stat-value">{fmt(totalValue)}</div></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon orange"><TrendingDown size={22} /></div>
          <div className="stat-info"><div className="stat-label">Low Stock</div><div className="stat-value">{lowStock}</div><div className="stat-change" style={{ color: 'var(--warning)' }}>≤ 5 units</div></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon red"><AlertTriangle size={22} /></div>
          <div className="stat-info"><div className="stat-label">Out of Stock</div><div className="stat-value">{outOfStock}</div></div>
        </div>
      </div>

      {/* Search */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)' }}>
        <div className="input-wrapper">
          <Search size={16} className="input-icon-left" />
          <input id="inventory-search" type="search" className="form-input" placeholder="Search by product name or SKU…" value={search} onChange={e => setSearch(e.target.value)} />
        </div>
      </div>

      {/* Table — shows InventoryResponse fields */}
      <div className="card">
        <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table>
            <thead>
              <tr>
                <th>Product</th>
                <th>SKU</th>
                <th>Available</th>
                <th>Reserved</th>
                <th>Sold</th>
                <th>Stock Level</th>
                <th>Value</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(p => {
                const inv   = inventory[p.id] || { availableQuantity: 0, reservedQuantity: 0, soldQuantity: 0 };
                const total = inv.availableQuantity + inv.reservedQuantity + inv.soldQuantity;
                const level = getLevel(inv.availableQuantity, total);
                const pct   = total > 0 ? Math.round((inv.availableQuantity / total) * 100) : 0;
                return (
                  <tr key={p.id}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                        <div style={{ width: 36, height: 36, background: 'var(--gray-100)', borderRadius: 'var(--radius-md)', overflow: 'hidden', flexShrink: 0 }}>
                          <img src={p.images?.[0]?.imageUrl} alt={p.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                        </div>
                        <span className="td-primary" style={{ maxWidth: 180, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{p.name}</span>
                      </div>
                    </td>
                    <td className="text-gray text-sm">{p.sku}</td>
                    <td>
                      <span style={{ fontWeight: 700, color: inv.availableQuantity === 0 ? 'var(--danger)' : inv.availableQuantity <= 5 ? 'var(--warning)' : 'var(--success)' }}>
                        {inv.availableQuantity}
                      </span>
                    </td>
                    <td><span className="tag tag-yellow">{inv.reservedQuantity}</span></td>
                    <td className="text-gray">{inv.soldQuantity}</td>
                    <td>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <div className="inventory-bar">
                          <div className={`inventory-bar-fill ${level}`} style={{ width: `${pct}%` }} />
                        </div>
                        <span style={{ fontSize: 10, color: 'var(--gray-400)' }}>{pct}% available</span>
                      </div>
                    </td>
                    <td className="font-bold">{fmt(p.price * inv.availableQuantity)}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 4 }}>
                        <button className="btn btn-success btn-sm" title="Add Stock"
                          onClick={() => { setFormData({ productId: p.id, quantity: '', orderId: '' }); setModal('add'); }}>
                          <Plus size={12} />
                        </button>
                        <button className="btn btn-secondary btn-sm" title="Reserve"
                          onClick={() => { setFormData({ productId: p.id, quantity: '', orderId: '' }); setModal('reserve'); }}>
                          <RefreshCw size={12} />
                        </button>
                        {inv.reservedQuantity > 0 && (
                          <>
                            <button className="btn btn-primary btn-sm" title="Confirm"
                              onClick={() => { setFormData({ productId: p.id }); setModal('confirm'); }}>
                              <CheckCircle size={12} />
                            </button>
                            <button className="btn btn-danger btn-sm" title="Release"
                              onClick={() => { setFormData({ productId: p.id }); setModal('release'); }}>
                              <X size={12} />
                            </button>
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

      {/* Modal */}
      {modal && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">
                {modal === 'add' && 'Add Stock'}
                {modal === 'reserve' && 'Reserve Stock'}
                {modal === 'confirm' && 'Confirm Stock'}
                {modal === 'release' && 'Release Stock'}
              </span>
              <button className="modal-close" onClick={() => setModal(null)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              {/* InventoryRequest / ReserveStockRequest fields */}
              {(modal === 'add' || modal === 'reserve') && (
                <>
                  <div className="form-group">
                    <label className="form-label">Product <span className="required">*</span></label>
                    <select id="inv-product" className="form-select" value={formData.productId}
                      onChange={e => setFormData(f => ({ ...f, productId: e.target.value }))}>
                      <option value="">Select product…</option>
                      {products.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                    </select>
                  </div>
                  {modal === 'reserve' && (
                    <div className="form-group">
                      <label className="form-label">Order ID <span className="required">*</span></label>
                      <input id="inv-order-id" type="text" className="form-input" placeholder="e.g. o1234" value={formData.orderId}
                        onChange={e => setFormData(f => ({ ...f, orderId: e.target.value }))} />
                    </div>
                  )}
                  <div className="form-group">
                    <label className="form-label">Quantity <span className="required">*</span></label>
                    <input id="inv-quantity" type="number" className="form-input" placeholder="e.g. 50" min="1" value={formData.quantity}
                      onChange={e => setFormData(f => ({ ...f, quantity: e.target.value }))} />
                    <span className="form-hint">Minimum quantity: 1</span>
                  </div>
                </>
              )}
              {/* ConfirmStockRequest / ReleaseStockRequest */}
              {(modal === 'confirm' || modal === 'release') && (
                <div>
                  <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-600)', marginBottom: 'var(--space-4)' }}>
                    {modal === 'confirm'
                      ? 'This will move all reserved stock to sold quantity. This action represents a completed order fulfillment.'
                      : 'This will release all reserved stock back to available inventory. This represents a cancelled/refunded order.'
                    }
                  </p>
                  <div style={{ padding: 'var(--space-4)', background: 'var(--gray-50)', borderRadius: 'var(--radius-lg)', border: '1px solid var(--gray-200)' }}>
                    <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--gray-700)' }}>
                      Product: {products.find(p => p.id === formData.productId)?.name}
                    </div>
                    <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-500)', marginTop: 4 }}>
                      Reserved Qty: {inventory[formData.productId]?.reservedQuantity}
                    </div>
                  </div>
                </div>
              )}
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
              <button id="inventory-action-btn" className={`btn ${modal === 'release' ? 'btn-danger' : 'btn-primary'}`}
                onClick={modal === 'add' ? handleAddStock : modal === 'reserve' ? handleReserve : modal === 'confirm' ? handleConfirm : handleRelease}>
                {modal === 'add' && 'Add Stock'}
                {modal === 'reserve' && 'Reserve Stock'}
                {modal === 'confirm' && 'Confirm Stock'}
                {modal === 'release' && 'Release Stock'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
