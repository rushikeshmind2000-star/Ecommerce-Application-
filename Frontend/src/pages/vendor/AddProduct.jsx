import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Package, Save, Plus, X, Upload, ArrowLeft, Info } from 'lucide-react';
import { useApp } from '../../context/AppContext';

const CATEGORIES = ['Electronics', 'Footwear', 'Audio', 'Laptops', 'Apparel', 'Furniture', 'Books'];
const BRANDS     = ['Apple', 'Samsung', 'Nike', 'Sony', 'Dell', 'LG', 'Other'];

export default function VendorAddProduct() {
  const navigate = useNavigate();
  const { submitProduct } = useApp();

  const [form, setForm] = useState({
    name: '', description: '', category: '', brand: '', price: '', discount: '',
    sku: '', stock: '', weight: '', dimensions: '', specifications: '',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [specs, setSpecs]     = useState([{ key: '', value: '' }]);

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const validate = () => {
    const e = {};
    if (!form.name.trim())      e.name     = 'Product name is required';
    if (!form.category)         e.category = 'Category is required';
    if (!form.price || isNaN(form.price) || Number(form.price) <= 0) e.price = 'Valid price is required';
    if (!form.stock || isNaN(form.stock) || Number(form.stock) < 0)  e.stock = 'Valid stock is required';
    if (!form.sku.trim())       e.sku      = 'SKU is required';
    return e;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      submitProduct({ ...form, price: Number(form.price), stock: Number(form.stock) });
      navigate('/vendor/products');
    }, 800);
  };

  const addSpec = () => setSpecs(prev => [...prev, { key: '', value: '' }]);
  const removeSpec = (i) => setSpecs(prev => prev.filter((_, idx) => idx !== i));
  const setSpec = (i, k, v) => setSpecs(prev => prev.map((s, idx) => idx === i ? { ...s, [k]: v } : s));

  return (
    <div className="page-container" style={{ maxWidth: 900 }}>
      <div className="page-header">
        <div className="page-header-left">
          <button className="btn btn-secondary btn-sm" onClick={() => navigate('/vendor/products')} style={{ marginBottom: 'var(--space-2)' }}>
            <ArrowLeft size={14} /> Back to Products
          </button>
          <h1 className="page-title">Add New Product</h1>
          <p className="page-subtitle">Fill in the details below. Admin approval required before listing.</p>
        </div>
      </div>

      {/* Info Banner */}
      <div style={{ display: 'flex', gap: 'var(--space-3)', alignItems: 'center', background: 'var(--primary-light)', border: '1px solid var(--primary)', borderRadius: 'var(--radius-xl)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)', color: 'var(--primary)' }}>
        <Info size={16} />
        <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: 500 }}>
          Products are submitted for admin review. Once approved, they will be visible on the marketplace.
        </span>
      </div>

      <form onSubmit={handleSubmit} noValidate>
        <div style={{ display: 'grid', gap: 'var(--space-6)' }}>

          {/* Basic Info */}
          <div className="card">
            <div className="card-header"><span className="card-title"><Package size={16} /> Basic Information</span></div>
            <div className="card-body" style={{ display: 'grid', gap: 'var(--space-4)' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Product Name <span className="required">*</span></label>
                <input id="product-name" type="text" className={`form-input ${errors.name ? 'error' : ''}`}
                  placeholder="e.g. Apple iPhone 15 Pro 128GB" value={form.name} onChange={e => set('name', e.target.value)} />
                {errors.name && <span className="form-error">{errors.name}</span>}
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Description</label>
                <textarea id="product-description" className="form-input" style={{ minHeight: 100, resize: 'vertical' }}
                  placeholder="Describe your product in detail…" value={form.description} onChange={e => set('description', e.target.value)} />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-4)' }}>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label className="form-label">Category <span className="required">*</span></label>
                  <select id="product-category" className={`form-select ${errors.category ? 'error' : ''}`}
                    value={form.category} onChange={e => set('category', e.target.value)}>
                    <option value="">Select category…</option>
                    {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                  </select>
                  {errors.category && <span className="form-error">{errors.category}</span>}
                </div>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label className="form-label">Brand</label>
                  <select id="product-brand" className="form-select" value={form.brand} onChange={e => set('brand', e.target.value)}>
                    <option value="">Select brand…</option>
                    {BRANDS.map(b => <option key={b} value={b}>{b}</option>)}
                  </select>
                </div>
              </div>
            </div>
          </div>

          {/* Pricing & Inventory */}
          <div className="card">
            <div className="card-header"><span className="card-title">💰 Pricing & Inventory</span></div>
            <div className="card-body" style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 'var(--space-4)' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Price (₹) <span className="required">*</span></label>
                <input id="product-price" type="number" className={`form-input ${errors.price ? 'error' : ''}`}
                  placeholder="e.g. 49999" min="0" value={form.price} onChange={e => set('price', e.target.value)} />
                {errors.price && <span className="form-error">{errors.price}</span>}
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Discount (%)</label>
                <input id="product-discount" type="number" className="form-input"
                  placeholder="e.g. 10" min="0" max="100" value={form.discount} onChange={e => set('discount', e.target.value)} />
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Initial Stock <span className="required">*</span></label>
                <input id="product-stock" type="number" className={`form-input ${errors.stock ? 'error' : ''}`}
                  placeholder="e.g. 50" min="0" value={form.stock} onChange={e => set('stock', e.target.value)} />
                {errors.stock && <span className="form-error">{errors.stock}</span>}
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">SKU <span className="required">*</span></label>
                <input id="product-sku" type="text" className={`form-input ${errors.sku ? 'error' : ''}`}
                  placeholder="e.g. APL-IPH15P-128" value={form.sku} onChange={e => set('sku', e.target.value)} />
                {errors.sku && <span className="form-error">{errors.sku}</span>}
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Weight (kg)</label>
                <input id="product-weight" type="number" className="form-input"
                  placeholder="e.g. 0.5" step="0.01" min="0" value={form.weight} onChange={e => set('weight', e.target.value)} />
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Dimensions (L×W×H cm)</label>
                <input id="product-dimensions" type="text" className="form-input"
                  placeholder="e.g. 15×7×1" value={form.dimensions} onChange={e => set('dimensions', e.target.value)} />
              </div>
            </div>
          </div>

          {/* Specifications */}
          <div className="card">
            <div className="card-header">
              <span className="card-title">🔧 Specifications</span>
              <button type="button" className="btn btn-secondary btn-sm" onClick={addSpec} id="add-spec-btn">
                <Plus size={13} /> Add
              </button>
            </div>
            <div className="card-body" style={{ display: 'grid', gap: 'var(--space-3)' }}>
              {specs.map((s, i) => (
                <div key={i} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr auto', gap: 'var(--space-3)', alignItems: 'center' }}>
                  <input className="form-input" placeholder="e.g. Storage" value={s.key} onChange={e => setSpec(i, 'key', e.target.value)} id={`spec-key-${i}`} />
                  <input className="form-input" placeholder="e.g. 128GB" value={s.value} onChange={e => setSpec(i, 'value', e.target.value)} id={`spec-value-${i}`} />
                  {specs.length > 1 && (
                    <button type="button" className="btn btn-danger btn-sm" style={{ padding: '8px' }} onClick={() => removeSpec(i)}>
                      <X size={13} />
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Images */}
          <div className="card">
            <div className="card-header"><span className="card-title">🖼️ Product Images</span></div>
            <div className="card-body">
              <div style={{ border: '2px dashed var(--gray-300)', borderRadius: 'var(--radius-xl)', padding: 'var(--space-10)', textAlign: 'center', cursor: 'pointer', transition: 'border-color var(--transition)', background: 'var(--gray-50)' }}
                onMouseEnter={e => e.currentTarget.style.borderColor = 'var(--primary)'}
                onMouseLeave={e => e.currentTarget.style.borderColor = 'var(--gray-300)'}>
                <Upload size={32} color="var(--gray-300)" style={{ marginBottom: 'var(--space-3)' }} />
                <div style={{ fontWeight: 600, color: 'var(--gray-600)', marginBottom: 4 }}>Drop images here or click to upload</div>
                <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-400)' }}>PNG, JPG up to 5MB each · First image will be primary</div>
              </div>
            </div>
          </div>
        </div>

        {/* Submit */}
        <div style={{ display: 'flex', gap: 'var(--space-4)', justifyContent: 'flex-end', marginTop: 'var(--space-6)' }}>
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/vendor/products')}>Cancel</button>
          <button id="submit-product-btn" type="submit" className="btn btn-primary btn-lg" disabled={loading}>
            {loading ? <span className="spinner" /> : <Save size={16} />}
            {loading ? 'Submitting…' : 'Submit for Approval'}
          </button>
        </div>
      </form>
    </div>
  );
}
