import { useState } from 'react';
import { Package, MapPin, Phone, CheckCircle, Truck, Clock, X } from 'lucide-react';

const MOCK_ORDERS = [
  { id: 'ORD-1001', customer: 'Rushi Sharma', phone: '9876543210', address: '123 MG Road, Bengaluru 560001', product: 'Apple iPhone 15 Pro', weight: '0.5 kg', status: 'PICKUP_READY', vendor: 'ABC Electronics', vendorAddress: 'Koramangala, Bengaluru' },
  { id: 'ORD-1002', customer: 'Priya Patel',  phone: '9876543211', address: '456 Park St, Mumbai 400001',   product: 'Nike Air Max 2024',   weight: '0.8 kg', status: 'ASSIGNED',     vendor: 'Sports World',    vendorAddress: 'Bandra, Mumbai' },
  { id: 'ORD-1003', customer: 'Arjun Nair',   phone: '9876543212', address: '789 Anna Salai, Chennai 600002', product: 'MacBook Pro 14"',  weight: '2.1 kg', status: 'ASSIGNED',     vendor: 'Tech Hub',        vendorAddress: 'T Nagar, Chennai' },
];

const STATUS_META = {
  ASSIGNED:         { label: 'Assigned',         cls: 'tag-orange' },
  PICKUP_READY:     { label: 'Ready for Pickup', cls: 'tag-blue'   },
  OUT_FOR_DELIVERY: { label: 'Out for Delivery', cls: 'tag-purple' },
  DELIVERED:        { label: 'Delivered',         cls: 'tag-green'  },
};

export default function DeliveryAssignedOrders() {
  const [orders, setOrders]   = useState(MOCK_ORDERS);
  const [selected, setSelected] = useState(null);

  const pickupOrder = (id) => {
    setOrders(prev => prev.map(o => o.id === id ? { ...o, status: 'OUT_FOR_DELIVERY' } : o));
    setSelected(null);
  };

  const deliverOrder = (id) => {
    setOrders(prev => prev.map(o => o.id === id ? { ...o, status: 'DELIVERED' } : o));
    setSelected(null);
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Assigned Orders</h1>
          <p className="page-subtitle">{orders.filter(o => o.status !== 'DELIVERED').length} active assignments</p>
        </div>
      </div>

      <div style={{ display: 'grid', gap: 'var(--space-4)' }}>
        {orders.map(o => {
          const sm = STATUS_META[o.status] || { label: o.status, cls: 'tag-gray' };
          return (
            <div key={o.id} className="card">
              <div className="card-body">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 'var(--space-4)' }}>
                  <div>
                    <div style={{ fontWeight: 700, fontSize: 'var(--font-size-base)', color: 'var(--gray-900)' }}>{o.id}</div>
                    <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-500)' }}>{o.product} · {o.weight}</div>
                  </div>
                  <span className={`tag ${sm.cls}`}>{sm.label}</span>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-4)', marginBottom: 'var(--space-4)' }}>
                  <div style={{ background: 'var(--gray-50)', padding: 'var(--space-3)', borderRadius: 'var(--radius-lg)' }}>
                    <div style={{ fontSize: 10, color: 'var(--gray-400)', marginBottom: 4, fontWeight: 600 }}>PICKUP FROM</div>
                    <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--gray-800)' }}>{o.vendor}</div>
                    <div style={{ fontSize: 11, color: 'var(--gray-500)' }}><MapPin size={10} style={{ display: 'inline' }} /> {o.vendorAddress}</div>
                  </div>
                  <div style={{ background: 'var(--gray-50)', padding: 'var(--space-3)', borderRadius: 'var(--radius-lg)' }}>
                    <div style={{ fontSize: 10, color: 'var(--gray-400)', marginBottom: 4, fontWeight: 600 }}>DELIVER TO</div>
                    <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--gray-800)' }}>{o.customer}</div>
                    <div style={{ fontSize: 11, color: 'var(--gray-500)' }}><MapPin size={10} style={{ display: 'inline' }} /> {o.address}</div>
                  </div>
                </div>

                <div style={{ display: 'flex', gap: 'var(--space-3)', alignItems: 'center' }}>
                  <a href={`tel:${o.phone}`} className="btn btn-secondary btn-sm">
                    <Phone size={13} /> {o.phone}
                  </a>
                  {o.status === 'PICKUP_READY' && (
                    <button className="btn btn-primary btn-sm" id={`pickup-${o.id}`} onClick={() => pickupOrder(o.id)}>
                      <Package size={13} /> Confirm Pickup
                    </button>
                  )}
                  {o.status === 'OUT_FOR_DELIVERY' && (
                    <button className="btn btn-success btn-sm" id={`deliver-${o.id}`} onClick={() => deliverOrder(o.id)}>
                      <CheckCircle size={13} /> Mark Delivered
                    </button>
                  )}
                  {o.status === 'ASSIGNED' && (
                    <button className="btn btn-primary btn-sm" id={`start-pickup-${o.id}`} onClick={() => setOrders(prev => prev.map(x => x.id === o.id ? { ...x, status: 'PICKUP_READY' } : x))}>
                      <Truck size={13} /> Start Pickup
                    </button>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
