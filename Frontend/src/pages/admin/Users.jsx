import { useState } from 'react';
import { Search, Filter, Eye, UserCheck, UserX, ChevronDown, User, Mail, Phone, Calendar, Shield } from 'lucide-react';

const MOCK_USERS = [
  { id: 'u1', firstName: 'Rohit', lastName: 'Sharma', email: 'rohit@gmail.com', phone: '9876543210', role: 'CUSTOMER', status: 'ACTIVE', createdAt: '2024-01-10', orders: 12 },
  { id: 'u2', firstName: 'Raj', lastName: 'Patel', email: 'raj@gmail.com', phone: '9876543211', role: 'VENDOR', status: 'ACTIVE', createdAt: '2024-01-08', orders: 0 },
  { id: 'u3', firstName: 'Pranav', lastName: 'Kulkarni', email: 'pranav@gmail.com', phone: '9876543212', role: 'CUSTOMER', status: 'BLOCKED', createdAt: '2023-12-15', orders: 5 },
  { id: 'u4', firstName: 'Sneha', lastName: 'Nair', email: 'sneha@gmail.com', phone: '9876543213', role: 'CUSTOMER', status: 'ACTIVE', createdAt: '2024-01-15', orders: 8 },
  { id: 'u5', firstName: 'System', lastName: 'Admin', email: 'admin@shopnest.com', phone: '9876543214', role: 'ADMIN', status: 'ACTIVE', createdAt: '2023-01-01', orders: 0 },
  { id: 'u6', firstName: 'Meera', lastName: 'Iyer', email: 'meera@gmail.com', phone: '9876543215', role: 'CUSTOMER', status: 'ACTIVE', createdAt: '2024-01-12', orders: 3 },
  { id: 'u7', firstName: 'Kumar', lastName: 'Rajan', email: 'kumar@gmail.com', phone: '9876543216', role: 'VENDOR', status: 'ACTIVE', createdAt: '2024-01-05', orders: 0 },
  { id: 'u8', firstName: 'Delivery', lastName: 'Partner', email: 'delivery@shopnest.com', phone: '9876543217', role: 'DELIVERY', status: 'ACTIVE', createdAt: '2024-01-01', orders: 0 },
];

const ROLE_COLORS = { CUSTOMER: 'tag-blue', VENDOR: 'tag-purple', ADMIN: 'tag-red', DELIVERY: 'tag-orange' };

export default function AdminUsers() {
  const [search, setSearch]     = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [users, setUsers]       = useState(MOCK_USERS);
  const [selected, setSelected] = useState(null);

  const filtered = users.filter(u => {
    const q = search.toLowerCase();
    if (q && !u.firstName.toLowerCase().includes(q) && !u.lastName.toLowerCase().includes(q) && !u.email.toLowerCase().includes(q)) return false;
    if (roleFilter !== 'ALL' && u.role !== roleFilter) return false;
    if (statusFilter !== 'ALL' && u.status !== statusFilter) return false;
    return true;
  });

  const toggleStatus = (id) => {
    setUsers(prev => prev.map(u => u.id === id ? { ...u, status: u.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE' } : u));
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">User Management</h1>
          <p className="page-subtitle">{filtered.length} of {users.length} users</p>
        </div>
      </div>

      {/* Filters */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)', display: 'flex', gap: 'var(--space-3)', flexWrap: 'wrap', alignItems: 'center' }}>
        <div className="input-wrapper" style={{ flex: 1, minWidth: 220 }}>
          <Search size={16} className="input-icon-left" />
          <input id="user-search" type="search" className="form-input" placeholder="Search by name or email…"
            value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <select id="role-filter" className="form-select" style={{ minWidth: 140 }} value={roleFilter} onChange={e => setRoleFilter(e.target.value)}>
          <option value="ALL">All Roles</option>
          <option value="CUSTOMER">Customer</option>
          <option value="VENDOR">Vendor</option>
          <option value="ADMIN">Admin</option>
          <option value="DELIVERY">Delivery</option>
        </select>
        <select id="status-filter" className="form-select" style={{ minWidth: 140 }} value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
          <option value="ALL">All Status</option>
          <option value="ACTIVE">Active</option>
          <option value="BLOCKED">Blocked</option>
        </select>
      </div>

      {/* Table */}
      <div className="card">
        <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table>
            <thead>
              <tr>
                <th>User</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Role</th>
                <th>Status</th>
                <th>Joined</th>
                <th>Orders</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(u => (
                <tr key={u.id}>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                      <div style={{ width: 32, height: 32, borderRadius: '50%', background: 'var(--primary-light)', color: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 12, flexShrink: 0 }}>
                        {u.firstName[0]}{u.lastName[0]}
                      </div>
                      <span className="td-primary">{u.firstName} {u.lastName}</span>
                    </div>
                  </td>
                  <td className="text-gray text-sm">{u.email}</td>
                  <td className="text-gray text-sm">{u.phone}</td>
                  <td><span className={`tag ${ROLE_COLORS[u.role] || 'tag-gray'}`}>{u.role}</span></td>
                  <td>
                    <span className={`tag ${u.status === 'ACTIVE' ? 'tag-green' : 'tag-red'}`}>{u.status}</span>
                  </td>
                  <td className="text-gray text-sm">{new Date(u.createdAt).toLocaleDateString('en-IN')}</td>
                  <td>{u.orders}</td>
                  <td>
                    <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
                      <button className="btn btn-secondary btn-sm" onClick={() => setSelected(u)} id={`view-user-${u.id}`}>
                        <Eye size={13} /> View
                      </button>
                      <button
                        className={`btn btn-sm ${u.status === 'ACTIVE' ? 'btn-danger' : 'btn-success'}`}
                        onClick={() => toggleStatus(u.id)}
                        disabled={u.role === 'ADMIN'}
                        id={`toggle-user-${u.id}`}>
                        {u.status === 'ACTIVE' ? <><UserX size={13} /> Block</> : <><UserCheck size={13} /> Activate</>}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* User Detail Modal */}
      {selected && (
        <div className="modal-overlay" onClick={() => setSelected(null)}>
          <div className="modal" style={{ maxWidth: 500 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">User Details</span>
              <button className="modal-close" onClick={() => setSelected(null)}>✕</button>
            </div>
            <div className="modal-body">
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)', marginBottom: 'var(--space-6)' }}>
                <div style={{ width: 64, height: 64, borderRadius: '50%', background: 'var(--primary-light)', color: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 22 }}>
                  {selected.firstName[0]}{selected.lastName[0]}
                </div>
                <div>
                  <div style={{ fontWeight: 700, fontSize: 'var(--font-size-lg)', color: 'var(--gray-900)' }}>{selected.firstName} {selected.lastName}</div>
                  <span className={`tag ${ROLE_COLORS[selected.role] || 'tag-gray'}`}>{selected.role}</span>
                </div>
              </div>
              {[
                { icon: Mail, label: 'Email', value: selected.email },
                { icon: Phone, label: 'Phone', value: selected.phone },
                { icon: Shield, label: 'Status', value: selected.status },
                { icon: Calendar, label: 'Joined', value: new Date(selected.createdAt).toLocaleDateString('en-IN') },
                { icon: User, label: 'Orders', value: selected.orders },
              ].map(row => (
                <div key={row.label} style={{ display: 'flex', gap: 'var(--space-3)', alignItems: 'center', padding: 'var(--space-3) 0', borderBottom: '1px solid var(--gray-100)' }}>
                  <row.icon size={16} color="var(--gray-400)" />
                  <span style={{ color: 'var(--gray-500)', fontSize: 'var(--font-size-sm)', minWidth: 80 }}>{row.label}</span>
                  <span style={{ fontWeight: 600, color: 'var(--gray-800)' }}>{row.value}</span>
                </div>
              ))}
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setSelected(null)}>Close</button>
              <button className={`btn ${selected.status === 'ACTIVE' ? 'btn-danger' : 'btn-success'}`} onClick={() => { toggleStatus(selected.id); setSelected(null); }} disabled={selected.role === 'ADMIN'}>
                {selected.status === 'ACTIVE' ? 'Block User' : 'Activate User'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
