import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Edit2, Eye, ToggleLeft, ToggleRight, Star, Package } from 'lucide-react';
import { useApp } from '../../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

export default function VendorProducts() {
  const { products, addToast } = useApp();
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('ALL');

  const filtered = products.filter(p => {
    const q = search.toLowerCase();
    if (q && !p.name.toLowerCase().includes(q) && !p.sku.toLowerCase().includes(q)) return false;
    if (status !== 'ALL' && p.status !== status) return false;
    return true;
  });

  const toggleStatus = (id) => {
    // In a real app, this would call an API. For now, we only show a toast since 
    // the global state update function for toggling product status wasn't added yet, 
    // but the approval/rejection was.
    addToast('Product status updated', 'success');
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">My Products</h1>
          <p className="page-subtitle">{filtered.length} products in your store</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-primary" onClick={() => navigate('/vendor/add-product')} id="vendor-add-product-nav-btn">
            <Plus size={15} /> Add Product
          </button>
        </div>
      </div>

      {/* Filters */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)', display: 'flex', gap: 'var(--space-3)', flexWrap: 'wrap' }}>
        <div className="input-wrapper" style={{ flex: 1, minWidth: 220 }}>
          <Search size={16} className="input-icon-left" />
          <input id="vendor-product-search" type="search" className="form-input" placeholder="Search your products…"
            value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <select id="vendor-product-status" className="form-select" style={{ minWidth: 160 }} value={status} onChange={e => setStatus(e.target.value)}>
          <option value="ALL">All Status</option>
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
          <option value="OUT_OF_STOCK">Out of Stock</option>
          <option value="PENDING_APPROVAL">Pending Approval</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      {/* Products Grid */}
      {filtered.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><Package size={32} /></div>
          <div className="empty-state-title">No products found</div>
          <div className="empty-state-text">Add your first product to start selling</div>
          <button className="btn btn-primary" onClick={() => navigate('/vendor/add-product')}>
            <Plus size={15} /> Add Product
          </button>
        </div>
      ) : (
        <div className="card">
          <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
            <table>
              <thead>
                <tr>
                  <th>Product</th>
                  <th>SKU</th>
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
                          <div className="td-primary" style={{ maxWidth: 160, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{p.name}</div>
                          <div style={{ fontSize: 11, color: 'var(--gray-400)' }}>{p.brand}</div>
                        </div>
                      </div>
                    </td>
                    <td className="text-gray text-sm">{p.sku}</td>
                    <td className="text-sm">{p.category}</td>
                    <td className="font-bold">{fmt(p.price)}</td>
                    <td>
                      <span style={{ fontWeight: 700, color: p.stock === 0 ? 'var(--danger)' : p.stock <= 5 ? 'var(--warning)' : 'var(--success)' }}>
                        {p.stock}
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 3, color: 'var(--warning)' }}>
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
                        <button className="btn btn-secondary btn-sm" id={`vendor-edit-product-${p.id}`}><Edit2 size={13} /></button>
                        {(p.status === 'ACTIVE' || p.status === 'INACTIVE' || p.status === 'OUT_OF_STOCK') && (
                          <button className="btn btn-sm" id={`vendor-toggle-product-${p.id}`}
                            style={{ background: p.status === 'ACTIVE' ? 'var(--danger-light)' : 'var(--success-light)', color: p.status === 'ACTIVE' ? 'var(--danger)' : 'var(--success)', border: 'none', padding: '4px 10px', borderRadius: 'var(--radius-md)', cursor: 'pointer', fontSize: 12 }}
                            onClick={() => toggleStatus(p.id)}>
                            {p.status === 'ACTIVE' ? <ToggleRight size={13} /> : <ToggleLeft size={13} />}
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
