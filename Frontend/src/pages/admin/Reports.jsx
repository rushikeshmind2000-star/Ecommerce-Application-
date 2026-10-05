import { TrendingUp, ShoppingBag, Users, DollarSign, BarChart2, Package } from 'lucide-react';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const MONTHLY = [
  { month: 'Aug', revenue: 1800000, orders: 2800 },
  { month: 'Sep', revenue: 2100000, orders: 3200 },
  { month: 'Oct', revenue: 1950000, orders: 3000 },
  { month: 'Nov', revenue: 2800000, orders: 4200 },
  { month: 'Dec', revenue: 3500000, orders: 5500 },
  { month: 'Jan', revenue: 2450000, orders: 3800 },
];
const MAX_REV = Math.max(...MONTHLY.map(m => m.revenue));

const TOP_VENDORS = [
  { name: 'XYZ Fashion',     revenue: 3400000, orders: 890, products: 120 },
  { name: 'ABC Electronics', revenue: 1250000, orders: 230, products: 45 },
  { name: 'HomeDecor Co',    revenue: 890000,  orders: 340, products: 78 },
  { name: 'Fashion Plus',    revenue: 120000,  orders: 45,  products: 12 },
];

const TOP_PRODUCTS = [
  { name: "Levi's 511 Slim Fit Jeans", sold: 1200, revenue: 5998800 },
  { name: 'Nike Air Max 2024',          sold: 430,  revenue: 5589700 },
  { name: 'Sony WH-1000XM5',           sold: 380,  revenue: 11396200 },
  { name: 'Samsung Galaxy S24 Ultra',  sold: 150,  revenue: 19499850 },
];

export default function AdminReports() {
  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Platform Reports</h1>
          <p className="page-subtitle">Analytics & performance overview</p>
        </div>
      </div>

      {/* KPIs */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)', marginBottom: 'var(--space-6)' }}>
        <div className="stat-card"><div className="stat-icon green"><DollarSign size={20} /></div><div className="stat-info"><div className="stat-label">Total Revenue</div><div className="stat-value">{fmt(14695000)}</div><div className="stat-change">+18% YoY</div></div></div>
        <div className="stat-card"><div className="stat-icon blue"><ShoppingBag size={20} /></div><div className="stat-info"><div className="stat-label">Total Orders</div><div className="stat-value">35,450</div><div className="stat-change">+22% YoY</div></div></div>
        <div className="stat-card"><div className="stat-icon orange"><Users size={20} /></div><div className="stat-info"><div className="stat-label">Total Customers</div><div className="stat-value">12,540</div><div className="stat-change">+340 this week</div></div></div>
        <div className="stat-card"><div className="stat-icon purple"><Package size={20} /></div><div className="stat-info"><div className="stat-label">Avg Order Value</div><div className="stat-value">{fmt(414)}</div><div className="stat-change">+5% vs last month</div></div></div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: 'var(--space-6)' }}>

        {/* Revenue Chart */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">Monthly Revenue & Orders</span>
          </div>
          <div className="card-body">
            <div style={{ display: 'flex', gap: 'var(--space-2)', alignItems: 'flex-end', height: 200, marginBottom: 'var(--space-4)' }}>
              {MONTHLY.map(m => (
                <div key={m.month} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
                  <div style={{ width: '100%', position: 'relative', display: 'flex', flexDirection: 'column', alignItems: 'center', height: `${(m.revenue / MAX_REV) * 180}px`, justifyContent: 'flex-end' }}>
                    <div style={{ width: '70%', height: '100%', background: 'linear-gradient(180deg, var(--primary) 0%, var(--primary-light) 100%)', borderRadius: 'var(--radius-md) var(--radius-md) 0 0', transition: 'all var(--transition-md)', cursor: 'default' }}
                      title={`${m.month}: ${fmt(m.revenue)}`} />
                  </div>
                  <div style={{ fontSize: 11, color: 'var(--gray-400)', fontWeight: 500 }}>{m.month}</div>
                </div>
              ))}
            </div>
            <div style={{ display: 'flex', gap: 'var(--space-6)', justifyContent: 'center' }}>
              {MONTHLY.map(m => (
                <div key={m.month} style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: 11, fontWeight: 700, color: 'var(--gray-700)' }}>{(m.revenue / 100000).toFixed(1)}L</div>
                  <div style={{ fontSize: 10, color: 'var(--gray-400)' }}>{m.orders} orders</div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Top Vendors */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">Top Vendors</span>
          </div>
          <div className="card-body" style={{ padding: 0 }}>
            {TOP_VENDORS.map((v, i) => (
              <div key={v.name} style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', padding: 'var(--space-4) var(--space-5)', borderBottom: '1px solid var(--gray-100)' }}>
                <div style={{ width: 28, height: 28, borderRadius: '50%', background: ['#dbeafe', '#dcfce7', '#fef9c3', '#fee2e2'][i], display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 12, color: ['#1d4ed8', '#16a34a', '#ca8a04', '#dc2626'][i], flexShrink: 0 }}>
                  {i + 1}
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: 600, fontSize: 'var(--font-size-sm)', color: 'var(--gray-800)' }}>{v.name}</div>
                  <div style={{ fontSize: 11, color: 'var(--gray-400)' }}>{v.orders} orders · {v.products} products</div>
                </div>
                <div style={{ fontWeight: 700, fontSize: 'var(--font-size-sm)', color: 'var(--gray-900)' }}>{fmt(v.revenue)}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Top Products */}
      <div className="card" style={{ marginTop: 'var(--space-6)' }}>
        <div className="card-header">
          <span className="card-title">Top Products by Revenue</span>
        </div>
        <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table>
            <thead><tr><th>#</th><th>Product</th><th>Units Sold</th><th>Revenue</th><th>Share</th></tr></thead>
            <tbody>
              {TOP_PRODUCTS.map((p, i) => {
                const total = TOP_PRODUCTS.reduce((s, x) => s + p.revenue, 0);
                const pct = Math.round((p.revenue / total) * 100);
                return (
                  <tr key={p.name}>
                    <td style={{ fontWeight: 700, color: 'var(--gray-400)' }}>#{i + 1}</td>
                    <td className="td-primary">{p.name}</td>
                    <td>{p.sold.toLocaleString()}</td>
                    <td className="font-bold">{fmt(p.revenue)}</td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                        <div className="inventory-bar" style={{ width: 80 }}>
                          <div className="inventory-bar-fill high" style={{ width: `${pct}%` }} />
                        </div>
                        <span style={{ fontSize: 11, color: 'var(--gray-400)' }}>{pct}%</span>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
