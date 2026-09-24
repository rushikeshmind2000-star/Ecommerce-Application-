import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Package, CheckCircle, Truck, Clock, XCircle, MapPin, CreditCard, Download } from 'lucide-react';
import { useApp } from '../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const ORDER_STEPS = [
  { key: 'PENDING',    label: 'Order Placed',  icon: CheckCircle },
  { key: 'CONFIRMED',  label: 'Confirmed',     icon: CheckCircle },
  { key: 'PROCESSING', label: 'Processing',    icon: Clock       },
  { key: 'SHIPPED',    label: 'Shipped',       icon: Truck       },
  { key: 'DELIVERED',  label: 'Delivered',     icon: CheckCircle },
];

const STATUS_META = {
  PENDING:       { label: 'Pending',    class: 'tag-orange' },
  PROCESSING_SAGA:{ label: 'Processing',class: 'tag-yellow' },
  CONFIRMED:     { label: 'Confirmed',  class: 'tag-purple' },
  PROCESSING:    { label: 'Processing', class: 'tag-yellow' },
  SHIPPED:       { label: 'Shipped',    class: 'tag-blue'   },
  DELIVERED:     { label: 'Delivered',  class: 'tag-green'  },
  CANCELLED:     { label: 'Cancelled',  class: 'tag-red'    },
};

function getStepIndex(status) {
  const map = { PENDING: 0, CONFIRMED: 1, PROCESSING: 2, PROCESSING_SAGA: 2, SHIPPED: 3, DELIVERED: 4, CANCELLED: -1 };
  return map[status] ?? 0;
}

export default function OrderDetailPage() {
  const { id }  = useParams();
  const navigate = useNavigate();
  const { orders } = useApp();

  const order = orders.find(o => o.id === id);

  if (!order) return (
    <div className="page-container">
      <div className="empty-state">
        <div className="empty-state-icon"><Package size={32} /></div>
        <div className="empty-state-title">Order not found</div>
        <button className="btn btn-primary" onClick={() => navigate('/orders')}>Back to Orders</button>
      </div>
    </div>
  );

  const stepIdx  = getStepIndex(order.status);
  const isCancelled = order.status === 'CANCELLED';
  const m = STATUS_META[order.status] || { label: order.status, class: 'tag-gray' };

  const tax       = Math.round(order.totalAmount * 0.18 / 1.18);
  const subtotal  = order.totalAmount - tax;

  return (
    <div className="page-container">
      {/* Breadcrumb */}
      <div className="breadcrumb">
        <span className="breadcrumb-item" onClick={() => navigate('/dashboard')}>Home</span>
        <span className="breadcrumb-sep">›</span>
        <span className="breadcrumb-item" onClick={() => navigate('/orders')}>Orders</span>
        <span className="breadcrumb-sep">›</span>
        <span className="breadcrumb-item current">{order.orderNumber}</span>
      </div>

      {/* Header */}
      <div className="page-header">
        <div className="page-header-left">
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)', flexWrap: 'wrap' }}>
            <h1 className="page-title">{order.orderNumber}</h1>
            <span className={`tag ${m.class}`} style={{ fontSize: 'var(--font-size-sm)', padding: '4px 14px' }}>{m.label}</span>
          </div>
          <p className="page-subtitle">Placed on {new Date(order.createdAt).toLocaleString('en-IN', { dateStyle: 'long', timeStyle: 'short' })}</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-secondary" onClick={() => navigate('/orders')}><ArrowLeft size={16} /> Back</button>
          <button className="btn btn-secondary" id="download-invoice"><Download size={16} /> Invoice</button>
        </div>
      </div>

      {/* Timeline */}
      {!isCancelled ? (
        <div className="card" style={{ marginBottom: 'var(--space-6)' }}>
          <div className="card-header"><span className="card-title">Order Progress</span></div>
          <div className="card-body">
            <div className="order-timeline">
              {ORDER_STEPS.map((step, i) => {
                const done    = i < stepIdx;
                const current = i === stepIdx;
                const Icon    = step.icon;
                return (
                  <div key={step.key} className={`timeline-step ${done ? 'done' : ''} ${current ? 'current' : ''}`}>
                    <div className="timeline-dot"><Icon size={16} /></div>
                    <div className="timeline-label">{step.label}</div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      ) : (
        <div style={{ background: 'var(--danger-light)', border: '1px solid #fecaca', borderRadius: 'var(--radius-xl)', padding: 'var(--space-5)', marginBottom: 'var(--space-6)', display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
          <XCircle size={24} color="var(--danger)" />
          <div>
            <div style={{ fontWeight: 700, color: 'var(--danger)' }}>Order Cancelled</div>
            <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-600)' }}>This order has been cancelled. Refund will be processed within 5-7 business days.</div>
          </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: 'var(--space-6)', alignItems: 'flex-start' }}>
        {/* Order Items — from OrderItemDTO */}
        <div>
          <div className="card" style={{ marginBottom: 'var(--space-6)' }}>
            <div className="card-header"><span className="card-title">Order Items ({order.items.length})</span></div>
            <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
              <table>
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>SKU</th>
                    <th>Qty</th>
                    <th>Unit Price</th>
                    <th>Total</th>
                  </tr>
                </thead>
                <tbody>
                  {order.items.map(item => (
                    <tr key={item.id}>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                          <div style={{ width: 44, height: 44, background: 'var(--gray-100)', borderRadius: 'var(--radius-md)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                            <Package size={18} color="var(--gray-400)" />
                          </div>
                          <span className="td-primary">{item.productName}</span>
                        </div>
                      </td>
                      <td className="text-gray text-sm">{item.sku}</td>
                      <td><span className="tag tag-gray">{item.quantity}</span></td>
                      <td>{fmt(item.unitPrice)}</td>
                      <td className="font-bold">{fmt(item.totalPrice)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Addresses */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-5)' }}>
            {[
              { title: 'Shipping Address', icon: MapPin, addr: order.shippingAddress },
              { title: 'Billing Address',  icon: CreditCard, addr: order.billingAddress },
            ].map(a => (
              <div key={a.title} className="card">
                <div className="card-header" style={{ padding: 'var(--space-4) var(--space-5)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                    <a.icon size={16} color="var(--primary)" />
                    <span style={{ fontWeight: 700, fontSize: 'var(--font-size-sm)', color: 'var(--gray-800)' }}>{a.title}</span>
                  </div>
                </div>
                <div style={{ padding: 'var(--space-4) var(--space-5)', fontSize: 'var(--font-size-sm)', color: 'var(--gray-600)', lineHeight: 1.6 }}>
                  {a.addr}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Summary */}
        <div>
          <div className="card">
            <div className="card-header"><span className="card-title">Order Summary</span></div>
            <div className="card-body">
              <div className="summary-row"><span className="text-gray">Order ID</span><span style={{ fontSize: 'var(--font-size-xs)' }}><code>{order.id}</code></span></div>
              <div className="summary-row"><span className="text-gray">Order Number</span><span className="font-bold">{order.orderNumber}</span></div>
              <div className="summary-row"><span className="text-gray">Currency</span><span>{order.currency}</span></div>
              <div className="divider" />
              <div className="summary-row"><span className="text-gray">Subtotal</span><span>{fmt(subtotal)}</span></div>
              <div className="summary-row"><span className="text-gray">GST (18%)</span><span>{fmt(tax)}</span></div>
              <div className="summary-row"><span className="text-gray">Shipping</span><span style={{ color: 'var(--success)' }}>FREE</span></div>
              <div className="divider" />
              <div className="summary-row total"><span>Total</span><span style={{ color: 'var(--primary)' }}>{fmt(order.totalAmount)}</span></div>
            </div>
          </div>

          {/* Order Metadata */}
          <div className="card" style={{ marginTop: 'var(--space-4)' }}>
            <div className="card-header"><span className="card-title">Order Info</span></div>
            <div className="card-body">
              {[
                ['Status',     m.label],
                ['User ID',    order.userId],
                ['Placed At',  new Date(order.createdAt).toLocaleString('en-IN')],
                ['Updated At', new Date(order.updatedAt).toLocaleString('en-IN')],
              ].map(([k, v]) => (
                <div key={k} className="summary-row">
                  <span className="text-gray text-sm">{k}</span>
                  <span style={{ fontSize: 'var(--font-size-xs)', fontWeight: 600, color: 'var(--gray-700)', textAlign: 'right', maxWidth: 180 }}>{v}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
