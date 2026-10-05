import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import { User, Mail, Phone, MapPin, Lock, Bell, Plus, Edit2, Trash2, Save, X } from 'lucide-react';

export default function CustomerProfile() {
  const { user, addToast } = useApp();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('info');
  const [editMode, setEditMode] = useState(false);
  const [form, setForm] = useState({
    firstName: user?.firstName || '',
    lastName:  user?.lastName  || '',
    email:     user?.email     || '',
    phone:     user?.mobile    || '',
  });
  const [addresses, setAddresses] = useState([
    { id: 'a1', type: 'Home',   line1: '123, MG Road', city: 'Bengaluru', state: 'Karnataka', pincode: '560001', default: true },
    { id: 'a2', type: 'Office', line1: '456, Park Street', city: 'Mumbai', state: 'Maharashtra', pincode: '400001', default: false },
  ]);
  const [pwdForm, setPwdForm] = useState({ current: '', newPwd: '', confirm: '' });

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const saveProfile = () => {
    addToast('Profile updated successfully!', 'success');
    setEditMode(false);
  };

  const tabs = [
    { id: 'info',      label: 'Personal Info',  icon: User },
    { id: 'addresses', label: 'Addresses',       icon: MapPin },
    { id: 'password',  label: 'Password',        icon: Lock },
    { id: 'notifications', label: 'Notifications', icon: Bell },
  ];

  return (
    <div className="page-container" style={{ maxWidth: 800 }}>
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">My Profile</h1>
          <p className="page-subtitle">Manage your account settings</p>
        </div>
      </div>

      {/* Profile Hero */}
      <div className="card" style={{ marginBottom: 'var(--space-5)' }}>
        <div className="card-body" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-5)' }}>
          <div style={{ width: 80, height: 80, borderRadius: '50%', background: 'var(--primary-light)', color: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, fontSize: 28, flexShrink: 0 }}>
            {user?.firstName?.[0]}{user?.lastName?.[0]}
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: 'var(--font-size-xl)', color: 'var(--gray-900)' }}>{user?.firstName} {user?.lastName}</div>
            <div style={{ color: 'var(--gray-500)', fontSize: 'var(--font-size-sm)', marginTop: 2 }}>{user?.email}</div>
            <span className="tag tag-blue" style={{ marginTop: 6 }}>{user?.role}</span>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 'var(--space-2)', marginBottom: 'var(--space-5)', borderBottom: '1px solid var(--gray-200)', paddingBottom: 0 }}>
        {tabs.map(t => (
          <button key={t.id}
            style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', padding: 'var(--space-3) var(--space-4)', background: 'none', border: 'none', borderBottom: `2px solid ${activeTab === t.id ? 'var(--primary)' : 'transparent'}`, color: activeTab === t.id ? 'var(--primary)' : 'var(--gray-500)', fontWeight: activeTab === t.id ? 700 : 400, cursor: 'pointer', fontSize: 'var(--font-size-sm)', transition: 'all var(--transition)', borderRadius: 'var(--radius-md) var(--radius-md) 0 0' }}
            onClick={() => setActiveTab(t.id)}>
            <t.icon size={14} /> {t.label}
          </button>
        ))}
      </div>

      {/* Tab Panels */}
      {activeTab === 'info' && (
        <div className="card">
          <div className="card-header">
            <span className="card-title">Personal Information</span>
            <button className="btn btn-secondary btn-sm" onClick={() => setEditMode(e => !e)} id="edit-profile-btn">
              {editMode ? <><X size={13} /> Cancel</> : <><Edit2 size={13} /> Edit</>}
            </button>
          </div>
          <div className="card-body" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-4)' }}>
            {[
              { label: 'First Name', key: 'firstName', icon: User },
              { label: 'Last Name',  key: 'lastName',  icon: User },
              { label: 'Email',      key: 'email',     icon: Mail, type: 'email' },
              { label: 'Phone',      key: 'phone',     icon: Phone, type: 'tel' },
            ].map(field => (
              <div key={field.key} className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">{field.label}</label>
                {editMode ? (
                  <div className="input-wrapper">
                    <field.icon size={15} className="input-icon-left" />
                    <input type={field.type || 'text'} className="form-input" value={form[field.key]} onChange={e => set(field.key, e.target.value)} id={`profile-${field.key}`} />
                  </div>
                ) : (
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', padding: 'var(--space-3) 0', color: 'var(--gray-700)', fontWeight: 500 }}>
                    <field.icon size={14} color="var(--gray-400)" />
                    {form[field.key] || <span style={{ color: 'var(--gray-300)' }}>Not set</span>}
                  </div>
                )}
              </div>
            ))}
          </div>
          {editMode && (
            <div className="card-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: 'var(--space-3)' }}>
              <button className="btn btn-secondary" onClick={() => setEditMode(false)}>Cancel</button>
              <button className="btn btn-primary" id="save-profile-btn" onClick={saveProfile}><Save size={14} /> Save Changes</button>
            </div>
          )}
        </div>
      )}

      {activeTab === 'addresses' && (
        <div style={{ display: 'grid', gap: 'var(--space-4)' }}>
          {addresses.map(addr => (
            <div key={addr.id} className="card">
              <div className="card-body">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-1)' }}>
                      <span style={{ fontWeight: 700, color: 'var(--gray-800)' }}>{addr.type}</span>
                      {addr.default && <span className="tag tag-green" style={{ fontSize: 10 }}>Default</span>}
                    </div>
                    <div style={{ color: 'var(--gray-600)', fontSize: 'var(--font-size-sm)' }}>
                      {addr.line1}, {addr.city}, {addr.state} - {addr.pincode}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
                    <button className="btn btn-secondary btn-sm" id={`edit-address-${addr.id}`}><Edit2 size={13} /></button>
                    <button className="btn btn-danger btn-sm" id={`delete-address-${addr.id}`} onClick={() => setAddresses(prev => prev.filter(a => a.id !== addr.id))}><Trash2 size={13} /></button>
                  </div>
                </div>
              </div>
            </div>
          ))}
          <button className="btn btn-primary btn-sm" id="add-address-btn" style={{ alignSelf: 'flex-start' }} onClick={() => addToast('Add address form would appear here', 'info')}>
            <Plus size={14} /> Add New Address
          </button>
        </div>
      )}

      {activeTab === 'password' && (
        <div className="card">
          <div className="card-header"><span className="card-title">Change Password</span></div>
          <div className="card-body" style={{ display: 'grid', gap: 'var(--space-4)', maxWidth: 400 }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Current Password</label>
              <input id="current-password" type="password" className="form-input" value={pwdForm.current} onChange={e => setPwdForm(f => ({ ...f, current: e.target.value }))} />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">New Password</label>
              <input id="new-password" type="password" className="form-input" value={pwdForm.newPwd} onChange={e => setPwdForm(f => ({ ...f, newPwd: e.target.value }))} />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Confirm New Password</label>
              <input id="confirm-password" type="password" className="form-input" value={pwdForm.confirm} onChange={e => setPwdForm(f => ({ ...f, confirm: e.target.value }))} />
            </div>
            <button className="btn btn-primary" style={{ alignSelf: 'flex-start' }} id="change-password-btn"
              onClick={() => { addToast('Password changed successfully!', 'success'); setPwdForm({ current: '', newPwd: '', confirm: '' }); }}>
              <Lock size={14} /> Update Password
            </button>
          </div>
        </div>
      )}

      {activeTab === 'notifications' && (
        <div className="card">
          <div className="card-header"><span className="card-title">Notification Preferences</span></div>
          <div className="card-body" style={{ display: 'grid', gap: 'var(--space-4)' }}>
            {[
              { label: 'Order Updates', desc: 'Get notified about your order status changes', checked: true },
              { label: 'Promotions & Offers', desc: 'Receive deals and discount notifications', checked: false },
              { label: 'New Products', desc: 'Know when new products are available', checked: true },
              { label: 'SMS Notifications', desc: 'Receive important updates via SMS', checked: true },
            ].map((pref, i) => (
              <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: 'var(--space-3) 0', borderBottom: '1px solid var(--gray-100)' }}>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 'var(--font-size-sm)', color: 'var(--gray-800)' }}>{pref.label}</div>
                  <div style={{ fontSize: 11, color: 'var(--gray-400)', marginTop: 2 }}>{pref.desc}</div>
                </div>
                <div style={{ position: 'relative', width: 44, height: 24 }}>
                  <input type="checkbox" defaultChecked={pref.checked} id={`notif-${i}`} style={{ display: 'none' }} />
                  <label htmlFor={`notif-${i}`} style={{ position: 'absolute', inset: 0, background: pref.checked ? 'var(--primary)' : 'var(--gray-300)', borderRadius: 12, cursor: 'pointer', transition: 'background var(--transition)' }}>
                    <div style={{ position: 'absolute', top: 3, left: pref.checked ? 23 : 3, width: 18, height: 18, background: 'white', borderRadius: '50%', transition: 'left var(--transition-md)', boxShadow: 'var(--shadow-sm)' }} />
                  </label>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
