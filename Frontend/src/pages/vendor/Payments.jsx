import { DollarSign, TrendingUp, CreditCard, BarChart2 } from 'lucide-react';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const SALES = [
  { orderId: 'ORD-1001', product: 'Apple iPhone 15 Pro', date: '2024-01-22', amount: 269800, commission: 10792, net: 259008, payout: 'PAID' },
  { orderId: 'ORD-1002', product: 'Nike Air Max 2024',   date: '2024-01-21', amount: 12999,  commission: 520,   net: 12479,  payout: 'PAID' },
  { orderId: 'ORD-1004', product: 'Sony WH-1000XM5',     date: '2024-01-20', amount: 89970,  commission: 3599,  net: 86371,  payout: 'PROCESSING' },
  { orderId: 'ORD-1005', product: 'MacBook Pro 14"',     date: '2024-01-18', amount: 189900, commission: 7596,  net: 182304, payout: 'PENDING' },
];

export default function VendorPayments() {
  const totalRevenue    = SALES.reduce((s, r) => s + r.amount, 0);
  const totalCommission = SALES.reduce((s, r) => s + r.commission, 0);
  const totalNet        = SALES.reduce((s, r) => s + r.net, 0);
  const commissionRate  = ((totalCommission / totalRevenue) * 100).toFixed(1);

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Sales & Payments</h1>
          <p className="page-subtitle">Your earnings and payout history</p>
        </div>
      </div>

      {/* KPIs */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)', marginBottom: 'var(--space-6)' }}>
        <div className="stat-card"><div className="stat-icon blue"><DollarSign size={20} /></div><div className="stat-info"><div className="stat-label">Gross Sales</div><div className="stat-value">{fmt(totalRevenue)}</div></div></div>
        <div className="stat-card"><div className="stat-icon red"><BarChart2 size={20} /></div><div className="stat-info"><div className="stat-label">Commission ({commissionRate}%)</div><div className="stat-value">{fmt(totalCommission)}</div></div></div>
        <div className="stat-card"><div className="stat-icon green"><TrendingUp size={20} /></div><div className="stat-info"><div className="stat-label">Net Earnings</div><div className="stat-value">{fmt(totalNet)}</div></div></div>
        <div className="stat-card"><div className="stat-icon purple"><CreditCard size={20} /></div><div className="stat-info"><div className="stat-label">Pending Payout</div><div className="stat-value">{fmt(SALES.filter(s => s.payout !== 'PAID').reduce((x, s) => x + s.net, 0))}</div></div></div>
      </div>

      {/* Sales Table */}
      <div className="card">
        <div className="card-header"><span className="card-title">Transaction History</span></div>
        <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table>
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Product</th>
                <th>Date</th>
                <th>Gross Amount</th>
                <th>Commission</th>
                <th>Net Amount</th>
                <th>Payout</th>
              </tr>
            </thead>
            <tbody>
              {SALES.map(s => (
                <tr key={s.orderId}>
                  <td className="td-primary">{s.orderId}</td>
                  <td style={{ maxWidth: 180, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{s.product}</td>
                  <td className="text-gray text-sm">{new Date(s.date).toLocaleDateString('en-IN')}</td>
                  <td>{fmt(s.amount)}</td>
                  <td style={{ color: 'var(--danger)' }}>-{fmt(s.commission)}</td>
                  <td className="font-bold">{fmt(s.net)}</td>
                  <td>
                    <span className={`tag ${s.payout === 'PAID' ? 'tag-green' : s.payout === 'PROCESSING' ? 'tag-blue' : 'tag-orange'}`}>
                      {s.payout}
                    </span>
                  </td>
                </tr>
              ))}
              {/* Totals Row */}
              <tr style={{ background: 'var(--gray-50)', fontWeight: 700 }}>
                <td colSpan={3} style={{ fontWeight: 700, color: 'var(--gray-700)' }}>Total</td>
                <td>{fmt(totalRevenue)}</td>
                <td style={{ color: 'var(--danger)' }}>-{fmt(totalCommission)}</td>
                <td style={{ color: 'var(--success)' }}>{fmt(totalNet)}</td>
                <td></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
