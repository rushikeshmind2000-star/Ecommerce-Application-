import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Mail, Lock, Eye, EyeOff, User, Phone, Store, CheckCircle } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { registerUser } from '../api/userApi';

export default function RegisterPage() {
  const { login, addToast } = useApp();
  const navigate = useNavigate();

  // Fields mirror UserRequest DTO
  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '',
    password: '', confirmPassword: '', mobile: '', role: 'CUSTOMER',
  });
  const [errors, setErrors]   = useState({});
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [agreed, setAgreed]   = useState(false);

  const set = (k, v) => { setForm(f => ({ ...f, [k]: v })); setErrors(e => ({ ...e, [k]: '' })); };

  const validate = () => {
    const e = {};
    if (!form.firstName.trim())                    e.firstName = 'First name is required';
    if (!form.lastName.trim())                     e.lastName  = 'Last name is required';
    if (!form.email)                               e.email     = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email))    e.email     = 'Invalid email format';
    if (!form.password)                            e.password  = 'Password is required';
    else if (form.password.length < 8)             e.password  = 'Minimum 8 characters';
    if (form.password !== form.confirmPassword)    e.confirmPassword = 'Passwords do not match';
    if (!form.mobile)                              e.mobile    = 'Mobile number is required';
    else if (!/^[6-9]\d{9}$/.test(form.mobile))   e.mobile    = 'Invalid Indian mobile number (10 digits, starts 6-9)';
    if (!agreed)                                   e.terms     = 'You must accept the terms';
    return e;
  };

  const handleSubmit = async (ev) => {
    ev.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setLoading(true);
    try {
      // Real API call → POST /api/users/register
      const user = await registerUser({
        firstName: form.firstName,
        lastName:  form.lastName,
        email:     form.email,
        password:  form.password,
        mobile:    form.mobile,
        role:      form.role,
      });
      login(user);
      addToast('Account created successfully! Welcome to ShopNest.', 'success');
      navigate('/dashboard');
    } catch (err) {
      addToast(err.message || 'Registration failed. Please try again.', 'error');
    } finally {
      setLoading(false);
    }
  };


  return (
    <div className="auth-layout" style={{ padding: 'var(--space-6) 0' }}>
      <div className="auth-container" style={{ maxWidth: 520 }}>
        <div className="auth-card">
          {/* Logo */}
          <div className="auth-logo">
            <div className="auth-logo-icon"><Store size={24} /></div>
            <span className="auth-logo-text">Shop<span>Nest</span></span>
          </div>

          <h1 className="auth-title">Create your account</h1>
          <p className="auth-subtitle">Join millions of shoppers on ShopNest</p>

          <form onSubmit={handleSubmit} noValidate>
            {/* Name Row */}
            <div className="form-row">
              <div className="form-group">
                <label className="form-label" htmlFor="reg-firstName">First Name <span className="required">*</span></label>
                <div className="input-wrapper">
                  <User size={16} className="input-icon-left" />
                  <input id="reg-firstName" type="text" className={`form-input ${errors.firstName ? 'error' : ''}`}
                    placeholder="John" value={form.firstName} onChange={e => set('firstName', e.target.value)} />
                </div>
                {errors.firstName && <span className="form-error">{errors.firstName}</span>}
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="reg-lastName">Last Name <span className="required">*</span></label>
                <div className="input-wrapper">
                  <User size={16} className="input-icon-left" />
                  <input id="reg-lastName" type="text" className={`form-input ${errors.lastName ? 'error' : ''}`}
                    placeholder="Doe" value={form.lastName} onChange={e => set('lastName', e.target.value)} />
                </div>
                {errors.lastName && <span className="form-error">{errors.lastName}</span>}
              </div>
            </div>

            {/* Email */}
            <div className="form-group">
              <label className="form-label" htmlFor="reg-email">Email Address <span className="required">*</span></label>
              <div className="input-wrapper">
                <Mail size={16} className="input-icon-left" />
                <input id="reg-email" type="email" className={`form-input ${errors.email ? 'error' : ''}`}
                  placeholder="john.doe@example.com" value={form.email} onChange={e => set('email', e.target.value)} autoComplete="email" />
              </div>
              {errors.email && <span className="form-error">{errors.email}</span>}
            </div>

            {/* Mobile */}
            <div className="form-group">
              <label className="form-label" htmlFor="reg-mobile">Mobile Number <span className="required">*</span></label>
              <div className="input-wrapper">
                <Phone size={16} className="input-icon-left" />
                <input id="reg-mobile" type="tel" className={`form-input ${errors.mobile ? 'error' : ''}`}
                  placeholder="9876543210" value={form.mobile} onChange={e => set('mobile', e.target.value)} maxLength={10} />
              </div>
              {errors.mobile && <span className="form-error">{errors.mobile}</span>}
              <span className="form-hint">10-digit Indian mobile number</span>
            </div>

            {/* Password */}
            <div className="form-group">
              <label className="form-label" htmlFor="reg-password">Password <span className="required">*</span></label>
              <div className="input-wrapper has-right">
                <Lock size={16} className="input-icon-left" />
                <input id="reg-password" type={showPwd ? 'text' : 'password'}
                  className={`form-input ${errors.password ? 'error' : ''}`}
                  placeholder="Min. 8 characters" value={form.password} onChange={e => set('password', e.target.value)} autoComplete="new-password" />
                <button type="button" className="input-icon-right" onClick={() => setShowPwd(p => !p)}>
                  {showPwd ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              {errors.password && <span className="form-error">{errors.password}</span>}
            </div>

            {/* Confirm Password */}
            <div className="form-group">
              <label className="form-label" htmlFor="reg-confirm">Confirm Password <span className="required">*</span></label>
              <div className="input-wrapper">
                <Lock size={16} className="input-icon-left" />
                <input id="reg-confirm" type="password"
                  className={`form-input ${errors.confirmPassword ? 'error' : ''}`}
                  placeholder="Re-enter password" value={form.confirmPassword} onChange={e => set('confirmPassword', e.target.value)} />
              </div>
              {errors.confirmPassword && <span className="form-error">{errors.confirmPassword}</span>}
            </div>

            {/* Role Selection */}
            <div className="form-group" style={{ marginBottom: '1.5rem' }}>
              <label className="form-label">I want to register as a: <span className="required">*</span></label>
              <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                  <input type="radio" name="role" value="CUSTOMER" checked={form.role === 'CUSTOMER'} onChange={e => set('role', e.target.value)} />
                  Customer
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                  <input type="radio" name="role" value="VENDOR" checked={form.role === 'VENDOR'} onChange={e => set('role', e.target.value)} />
                  Vendor
                </label>
              </div>
            </div>

            {/* Terms */}
            <div className="form-group" style={{ flexDirection: 'row', alignItems: 'flex-start', gap: 'var(--space-3)' }}>
              <input id="reg-terms" type="checkbox" checked={agreed} onChange={e => { setAgreed(e.target.checked); setErrors(er => ({ ...er, terms: '' })); }}
                style={{ marginTop: 2, width: 16, height: 16, accentColor: 'var(--primary)', cursor: 'pointer', flexShrink: 0 }} />
              <label htmlFor="reg-terms" style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-600)', cursor: 'pointer' }}>
                I agree to the <button type="button" className="auth-link">Terms of Service</button> and{' '}
                <button type="button" className="auth-link">Privacy Policy</button>
              </label>
            </div>
            {errors.terms && <span className="form-error" style={{ marginTop: '-var(--space-4)' }}>{errors.terms}</span>}

            <button id="register-submit" type="submit" className="btn btn-primary btn-lg btn-full" disabled={loading} style={{ marginTop: 'var(--space-2)' }}>
              {loading ? <span className="spinner" /> : <CheckCircle size={18} />}
              {loading ? 'Creating Account…' : 'Create Account'}
            </button>
          </form>

          <div className="auth-footer">
            Already have an account?{' '}
            <button className="auth-link" onClick={() => navigate('/login')}>Sign in</button>
          </div>
        </div>
      </div>
    </div>
  );
}
