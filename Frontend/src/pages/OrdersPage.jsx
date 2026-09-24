import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ClipboardList, Eye, Search, ChevronDown, Package, Filter } from 'lucide-react';
import { useApp } from '../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const STATUS_META = {
  PENDING:       { label: 'Pending',    class: 'tag-orange' },
  PROCESSING_SAGA:{ label: 'Processing',class: 'tag-yellow' },
  CONFIRMED:     { label: 'Confirmed',  class: 'tag-purple' },
  PROCESSING:    { label: 'Processing', class: 'tag-yellow' },
  SHIPPED:       { label: 'Shipped',    class: 'tag-blue'   },
  DELIVERED:     { label: 'Delivered',  class: 'tag-green'  },
  CANCELLED:     { label: 'Cancelled',  class: 'tag-red'    },
};

const ALL_STATUSES = ['ALL', 'PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

export default function OrdersPage() {
  const { orders } = useApp();
  const navigate   = useNavigate();
  const [search,   setSearch]   = useState('');
  const [status,   setStatus]   = useState('ALL');
  const [sortDir,  setSortDir]  = useState('desc');

  const filtered = orders
    .filter(o => {
      if (status !== 'ALL' && o.status !== status) return false;
      if (search) {
        const q = search.toLowerCase();
        return o.orderNumber.toLowerCase().includes(q) || o.items.some(i => i.productName.toLowerCase().includes(q));
      }
      return true;
    })
    .sort((a, b) => sortDir === 'desc'
      ? new Date(b.createdAt) - new Date(a.createdAt)
      : new Date(a.createdAt) - new Date(b.createdAt)
    );

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">My Orders</h1>
          <p className="page-subtitle">{orders.length} orders total</p>
        </div>
      </div>

      {/* Filters */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)', display: 'flex', gap: 'var(--space-4)', alignItems: 'center', flexWrap: 'wrap' }}>
        <div className="input-wrapper" style={{ flex: 1, minWidth: 200 }}>
          <Search size={16} className="input-icon-left" />
          <input id="order-search" type="search" className="form-input" placeholder="Search by order number or product…" value={search} onChange={e => setSearch(e.target.value)} />
        </div>

        <div style={{ display: 'flex', gap: 'var(--space-2)', flexWrap: 'wrap' }}>
          {ALL_STATUSES.map(s => {
            const m = STATUS_META[s] || {};
            return (
              <button key={s}
                style={{ padding: '6px 14px', borderRadius: 'var(--radius-full)', border: '1.5px solid', borderColor: status === s ? 'var(--primary)' : 'var(--gray-200)', background: status === s ? 'var(--primary)' : 'var(--white)', color: status === s ? 'var(--white)' : 'var(--gray-600)', fontSize: 'var(--font-size-xs)', fontWeight: 600, cursor: 'pointer', transition: 'all var(--transition)' }}
                onClick={() => setStatus(s)}>
                {s === 'ALL' ? 'All' : m.label || s}
              </button>
            );
          })}
        </div>

        <button className="btn btn-secondary btn-sm" onClick={() => setSortDir(d => d === 'desc' ? 'asc' : 'desc')}>
          <Filter size={14} /> {sortDir === 'desc' ? 'Newest First' : 'Oldest First'}
        </button>
      </div>

      {filtered.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><ClipboardList size={32} /></div>
          <div className="empty-state-title">No orders found</div>
          <div className="empty-state-text">Try adjusting your search or status filter</div>
          <button className="btn btn-primary" onClick={() => navigate('/products')}><Package size={16} /> Start Shopping</button>
        </div>
      ) : (
        <div className="card">
          <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
            <table>
              <thead>
                <tr>
                  <th>Order #</th>
                  <th>Date</th>
                  <th>Items</th>
                  <th>Total</th>
                  <th>Status</th>
                  <th>Shipping Address</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map(order => {
                  const m = STATUS_META[order.status] || { label: order.status, class: 'tag-gray' };
                  return (
                    <tr key={order.id}>
                      <td className="td-primary">{order.orderNumber}</td>
                      <td className="text-gray text-sm">{new Date(order.createdAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}</td>
                      <td>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                          {order.items.slice(0, 2).map(i => (
                            <span key={i.id} style={{ fontSize: 'var(--font-size-xs)', color: 'var(--gray-600)' }}>
                              {i.productName} × {i.quantity}
                            </span>
                          ))}
                          {order.items.length > 2 && <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--gray-400)' }}>+{order.items.length - 2} more</span>}
                        </div>
                      </td>
                      <td className="font-bold">{fmt(order.totalAmount)}</td>
                      <td><span className={`tag ${m.class}`}>{m.label}</span></td>
                      <td className="text-gray text-sm" style={{ maxWidth: 180, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {order.shippingAddress}
                      </td>
                      <td>
                        <button id={`view-order-${order.id}`} className="btn btn-secondary btn-sm" onClick={() => navigate(`/orders/${order.id}`)}>
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
      )}
    </div>
  );
}
