import { useState } from 'react';
import { Tag, Plus, Edit2, Trash2, Save, X, Search } from 'lucide-react';
import { useApp } from '../context/AppContext';

// Mirrors CategoryDTO, CreateCategoryRequest, UpdateCategoryRequest
const INITIAL_CATEGORIES = [
  { id: 'c1', name: 'Electronics',  description: 'Phones, tablets, gadgets and more',       status: 'ACTIVE'   },
  { id: 'c2', name: 'Footwear',     description: 'Shoes, sneakers, boots and sandals',       status: 'ACTIVE'   },
  { id: 'c3', name: 'Audio',        description: 'Headphones, earbuds and speakers',         status: 'ACTIVE'   },
  { id: 'c4', name: 'Laptops',      description: 'Laptops and workstations',                 status: 'ACTIVE'   },
  { id: 'c5', name: 'Apparel',      description: 'Clothing, fashion and accessories',        status: 'ACTIVE'   },
  { id: 'c6', name: 'Home & Kitchen',description:'Appliances and home essentials',           status: 'INACTIVE' },
];

export default function CategoriesPage() {
  const { products, addToast } = useApp();
  const [categories, setCategories] = useState(INITIAL_CATEGORIES);
  const [search, setSearch]   = useState('');
  const [modal,  setModal]    = useState(null); // 'create' | { type: 'edit', cat }
  const [form, setForm]       = useState({ name: '', description: '', status: 'ACTIVE' });
  const [errors, setErrors]   = useState({});

  const filtered = categories.filter(c => !search || c.name.toLowerCase().includes(search.toLowerCase()));

  const getProductCount = (catId) => products.filter(p => p.categoryId === catId).length;

  const openCreate = () => { setForm({ name: '', description: '', status: 'ACTIVE' }); setErrors({}); setModal('create'); };
  const openEdit   = (cat) => { setForm({ name: cat.name, description: cat.description, status: cat.status }); setErrors({}); setModal({ type: 'edit', cat }); };

  const validate = () => {
    const e = {};
    if (!form.name.trim()) e.name = 'Category name is mandatory';
    return e;
  };

  const handleSave = () => {
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    if (modal === 'create') {
      // CreateCategoryRequest
      const newCat = { id: 'c' + Date.now(), name: form.name.trim(), description: form.description, status: form.status };
      setCategories(prev => [...prev, newCat]);
      addToast(`Category "${newCat.name}" created`, 'success');
    } else {
      // UpdateCategoryRequest
      setCategories(prev => prev.map(c => c.id === modal.cat.id ? { ...c, name: form.name.trim(), description: form.description, status: form.status } : c));
      addToast(`Category "${form.name}" updated`, 'success');
    }
    setModal(null);
  };

  const handleDelete = (cat) => {
    const count = getProductCount(cat.id);
    if (count > 0) { addToast(`Cannot delete: ${count} products use this category`, 'error'); return; }
    setCategories(prev => prev.filter(c => c.id !== cat.id));
    addToast(`Category "${cat.name}" deleted`, 'info');
  };

  const toggleStatus = (cat) => {
    setCategories(prev => prev.map(c => c.id === cat.id ? { ...c, status: c.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' } : c));
    addToast(`Category "${cat.name}" status toggled`, 'info');
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Categories</h1>
          <p className="page-subtitle">Manage product categories</p>
        </div>
        <div className="page-actions">
          <button id="create-category-btn" className="btn btn-primary" onClick={openCreate}>
            <Plus size={16} /> New Category
          </button>
        </div>
      </div>

      {/* Search */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)' }}>
        <div className="input-wrapper">
          <Search size={16} className="input-icon-left" />
          <input id="category-search" type="search" className="form-input" placeholder="Search categories…" value={search} onChange={e => setSearch(e.target.value)} />
        </div>
      </div>

      {/* Table — CategoryDTO fields */}
      <div className="card">
        <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
          <table>
            <thead>
              <tr>
                <th>Category Name</th>
                <th>Description</th>
                <th>Status</th>
                <th>Products</th>
                <th>Category ID</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(cat => (
                <tr key={cat.id}>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                      <div style={{ width: 36, height: 36, background: 'var(--primary-light)', borderRadius: 'var(--radius-md)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <Tag size={16} color="var(--primary)" />
                      </div>
                      <span className="td-primary">{cat.name}</span>
                    </div>
                  </td>
                  <td className="text-gray text-sm" style={{ maxWidth: 220 }}>{cat.description}</td>
                  <td>
                    <button className={`tag ${cat.status === 'ACTIVE' ? 'tag-green' : 'tag-gray'}`}
                      onClick={() => toggleStatus(cat)} style={{ cursor: 'pointer', border: 'none' }}>
                      {cat.status}
                    </button>
                  </td>
                  <td>
                    <span className="tag tag-blue">{getProductCount(cat.id)} products</span>
                  </td>
                  <td className="text-gray" style={{ fontSize: 'var(--font-size-xs)' }}><code>{cat.id}</code></td>
                  <td>
                    <div style={{ display: 'flex', gap: 4 }}>
                      <button id={`edit-cat-${cat.id}`} className="btn btn-secondary btn-sm" onClick={() => openEdit(cat)}><Edit2 size={12} /> Edit</button>
                      <button id={`del-cat-${cat.id}`} className="btn btn-danger btn-sm" onClick={() => handleDelete(cat)}><Trash2 size={12} /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create/Edit Modal */}
      {modal && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">{modal === 'create' ? 'Create Category' : 'Edit Category'}</span>
              <button className="modal-close" onClick={() => setModal(null)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              {/* CreateCategoryRequest / UpdateCategoryRequest fields */}
              <div className="form-group">
                <label className="form-label" htmlFor="cat-name">Category Name <span className="required">*</span></label>
                <input id="cat-name" type="text" className={`form-input ${errors.name ? 'error' : ''}`}
                  placeholder="e.g. Electronics" value={form.name} onChange={e => { setForm(f => ({ ...f, name: e.target.value })); setErrors(er => ({ ...er, name: '' })); }} />
                {errors.name && <span className="form-error">{errors.name}</span>}
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="cat-desc">Description</label>
                <textarea id="cat-desc" className="form-textarea" placeholder="Brief description of this category…"
                  value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} rows={3} />
              </div>
              {modal !== 'create' && (
                <div className="form-group">
                  <label className="form-label">Status</label>
                  <div style={{ display: 'flex', gap: 'var(--space-3)' }}>
                    {['ACTIVE', 'INACTIVE'].map(s => (
                      <label key={s} style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', cursor: 'pointer', fontSize: 'var(--font-size-sm)', fontWeight: 500, color: 'var(--gray-700)' }}>
                        <input type="radio" name="cat-status" value={s} checked={form.status === s} onChange={() => setForm(f => ({ ...f, status: s }))} style={{ accentColor: 'var(--primary)' }} />
                        {s}
                      </label>
                    ))}
                  </div>
                </div>
              )}
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
              <button id="save-category-btn" className="btn btn-primary" onClick={handleSave}>
                <Save size={15} /> {modal === 'create' ? 'Create Category' : 'Save Changes'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
