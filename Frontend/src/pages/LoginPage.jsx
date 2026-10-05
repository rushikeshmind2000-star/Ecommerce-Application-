import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Mail, Lock, Eye, EyeOff, Store, ArrowRight } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { fetchAllUsers } from '../api/userApi';

export default function LoginPage() {
  const { login, addToast } = useApp();
  const navigate = useNavigate();

  const [form, setForm]       = useState({ email: '', password: '' });
  const [errors, setErrors]   = useState({});
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const validate = () => {
    const e = {};
    if (!form.email)                              e.email    = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email))   e.email    = 'Invalid email format';
    if (!form.password)                           e.password = 'Password is required';
    else if (form.password.length < 6)            e.password = 'Minimum 6 characters';
    return e;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setLoading(true);
    try {
      // Fetch users from backend and find by email
      const users = await fetchAllUsers();
      const found = users.find(u => u.email === form.email);
      if (!found) {
        addToast('No account found with this email.', 'error');
        return;
      }
      // Login with real user data from backend
      login(found);
      addToast(`Welcome back, ${found.firstName}!`, 'success');
      navigate('/dashboard');
    } catch (err) {
      addToast(err.message || 'Login failed. Please try again.', 'error');
    } finally {
      setLoading(false);
    }
  };


  return (
    <div className="auth-layout">
      <div className="auth-container">
        <div className="auth-card">
          {/* Logo */}
          <div className="auth-logo">
            <div className="auth-logo-icon"><Store size={24} /></div>
            <span className="auth-logo-text">Shop<span>Nest</span></span>
          </div>

          <h1 className="auth-title">Welcome back</h1>
          <p className="auth-subtitle">Sign in to your account to continue shopping</p>

          <form onSubmit={handleSubmit} noValidate>
            {/* Email */}
            <div className="form-group">
              <label className="form-label" htmlFor="login-email">
                Email Address <span className="required">*</span>
              </label>
              <div className="input-wrapper">
                <Mail size={16} className="input-icon-left" />
                <input
                  id="login-email"
                  type="email"
                  className={`form-input ${errors.email ? 'error' : ''}`}
                  placeholder="john.doe@example.com"
                  value={form.email}
                  onChange={e => set('email', e.target.value)}
                  autoComplete="email"
                />
              </div>
              {errors.email && <span className="form-error">{errors.email}</span>}
            </div>

            {/* Password */}
            <div className="form-group">
              <label className="form-label" htmlFor="login-password">
                Password <span className="required">*</span>
              </label>
              <div className="input-wrapper has-right">
                <Lock size={16} className="input-icon-left" />
                <input
                  id="login-password"
                  type={showPwd ? 'text' : 'password'}
                  className={`form-input ${errors.password ? 'error' : ''}`}
                  placeholder="••••••••"
                  value={form.password}
                  onChange={e => set('password', e.target.value)}
                  autoComplete="current-password"
                />
                <button type="button" className="input-icon-right" onClick={() => setShowPwd(p => !p)}>
                  {showPwd ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              {errors.password && <span className="form-error">{errors.password}</span>}
            </div>

            {/* Forgot */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 'var(--space-6)', marginTop: '-var(--space-2)' }}>
              <button type="button" className="auth-link" style={{ fontSize: 'var(--font-size-sm)' }}>
                Forgot password?
              </button>
            </div>

            {/* Submit */}
            <button
              id="login-submit"
              type="submit"
              className="btn btn-primary btn-lg btn-full"
              disabled={loading}
            >
              {loading ? <span className="spinner" /> : <ArrowRight size={18} />}
              {loading ? 'Signing in…' : 'Sign In'}
            </button>
          </form>

          <div className="auth-divider">or explore demo accounts</div>

          <div style={{ display: 'flex', gap: 'var(--space-2)', marginBottom: 'var(--space-4)' }}>
            <button
              type="button"
              className="btn btn-secondary"
              style={{ flex: 1, padding: '0.5rem', fontSize: '13px' }}
              onClick={() => {
                login({ email: 'customer@shopnest.com', firstName: 'Demo', lastName: 'Customer', role: 'CUSTOMER', status: 'ACTIVE' });
                addToast('Logged in as Customer', 'success');
                navigate('/dashboard');
              }}
            >
              Demo Customer
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              style={{ flex: 1, padding: '0.5rem', fontSize: '13px' }}
              onClick={() => {
                login({ email: 'vendor@shopnest.com', firstName: 'Demo', lastName: 'Vendor', role: 'VENDOR', status: 'ACTIVE' });
                addToast('Logged in as Vendor', 'success');
                navigate('/dashboard');
              }}
            >
              Demo Vendor
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              style={{ flex: 1, padding: '0.5rem', fontSize: '13px' }}
              onClick={() => {
                login({ email: 'admin@shopnest.com', firstName: 'System', lastName: 'Admin', role: 'ADMIN', status: 'ACTIVE' });
                addToast('Logged in as Admin', 'success');
                navigate('/dashboard');
              }}
            >
              Demo Admin
            </button>
          </div>

          <div className="auth-footer">
            Don&apos;t have an account?{' '}
            <button className="auth-link" onClick={() => navigate('/register')}>Create account</button>
          </div>
        </div>
      </div>
    </div>
  );
}
