import { useState } from 'react';
import { Plus, Edit2, Trash2, ChevronRight, ChevronDown, Tag, X, Save } from 'lucide-react';

const INITIAL_CATEGORIES = [
  {
    id: 'c1', name: 'Electronics', icon: '📱', description: 'Phones, tablets, and gadgets', products: 1240, active: true,
    subcategories: [
      { id: 'c1a', name: 'Mobile Phones', products: 450, active: true },
      { id: 'c1b', name: 'Laptops', products: 230, active: true },
      { id: 'c1c', name: 'Tablets', products: 120, active: true },
      { id: 'c1d', name: 'Accessories', products: 440, active: true },
    ]
  },
  {
    id: 'c2', name: 'Clothing', icon: '👕', description: 'Fashion for men, women and kids', products: 3200, active: true,
    subcategories: [
      { id: 'c2a', name: "Men's Wear", products: 1200, active: true },
      { id: 'c2b', name: "Women's Wear", products: 1500, active: true },
      { id: 'c2c', name: "Kids' Wear", products: 500, active: true },
    ]
  },
  {
    id: 'c3', name: 'Footwear', icon: '👟', description: 'Shoes, sandals and boots', products: 780, active: true,
    subcategories: [
      { id: 'c3a', name: 'Sports Shoes', products: 340, active: true },
      { id: 'c3b', name: 'Casual Shoes', products: 280, active: true },
      { id: 'c3c', name: 'Formal Shoes', products: 160, active: false },
    ]
  },
  {
    id: 'c4', name: 'Furniture', icon: '🪑', description: 'Home and office furniture', products: 456, active: true,
    subcategories: [
      { id: 'c4a', name: 'Living Room', products: 200, active: true },
      { id: 'c4b', name: 'Bedroom', products: 150, active: true },
      { id: 'c4c', name: 'Office', products: 106, active: true },
    ]
  },
  {
    id: 'c5', name: 'Books', icon: '📚', description: 'Books, e-books and stationery', products: 2100, active: false,
    subcategories: [
      { id: 'c5a', name: 'Fiction', products: 800, active: false },
      { id: 'c5b', name: 'Non-Fiction', products: 600, active: false },
    ]
  },
];

export default function AdminCategories() {
  const [categories, setCategories] = useState(INITIAL_CATEGORIES);
  const [expanded, setExpanded]     = useState({});
  const [modal, setModal]           = useState(null); // { type: 'add'|'edit'|'addSub', data? }
  const [form, setForm]             = useState({ name: '', description: '', icon: '📦', parentId: '' });

  const toggleExpand = (id) => setExpanded(prev => ({ ...prev, [id]: !prev[id] }));

  const totalProducts = categories.reduce((s, c) => s + c.products, 0);
  const activeCount   = categories.filter(c => c.active).length;

  const toggleCategoryActive = (id) => {
    setCategories(prev => prev.map(c => c.id === id ? { ...c, active: !c.active } : c));
  };

  const deleteCategory = (id) => {
    setCategories(prev => prev.filter(c => c.id !== id));
  };

  const handleSave = () => {
    if (!form.name.trim()) return;
    if (modal?.type === 'add') {
      const newCat = { id: 'c' + Date.now(), name: form.name, description: form.description, icon: form.icon, products: 0, active: true, subcategories: [] };
      setCategories(prev => [...prev, newCat]);
    } else if (modal?.type === 'addSub') {
      const newSub = { id: 'cs' + Date.now(), name: form.name, products: 0, active: true };
      setCategories(prev => prev.map(c => c.id === modal.data.id ? { ...c, subcategories: [...(c.subcategories || []), newSub] } : c));
    }
    setModal(null);
    setForm({ name: '', description: '', icon: '📦', parentId: '' });
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Category Management</h1>
          <p className="page-subtitle">{categories.length} categories · {totalProducts.toLocaleString()} products</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-primary" id="add-category-btn" onClick={() => setModal({ type: 'add' })}>
            <Plus size={15} /> Add Category
          </button>
        </div>
      </div>

      {/* Summary */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)', marginBottom: 'var(--space-6)' }}>
        <div className="stat-card">
          <div className="stat-icon blue"><Tag size={20} /></div>
          <div className="stat-info"><div className="stat-label">Total Categories</div><div className="stat-value">{categories.length}</div></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon green"><Tag size={20} /></div>
          <div className="stat-info"><div className="stat-label">Active Categories</div><div className="stat-value">{activeCount}</div></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon orange"><Tag size={20} /></div>
          <div className="stat-info"><div className="stat-label">Total Products</div><div className="stat-value">{totalProducts.toLocaleString()}</div></div>
        </div>
      </div>

      {/* Category Tree */}
      <div className="card">
        {categories.map(cat => (
          <div key={cat.id} style={{ borderBottom: '1px solid var(--gray-100)' }}>
            {/* Main Category Row */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', padding: 'var(--space-4) var(--space-5)', background: cat.active ? 'var(--white)' : 'var(--gray-50)' }}>
              <button style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--gray-400)', padding: 2 }} onClick={() => toggleExpand(cat.id)}>
                {expanded[cat.id] ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
              </button>
              <div style={{ fontSize: 22 }}>{cat.icon}</div>
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: 700, color: cat.active ? 'var(--gray-900)' : 'var(--gray-400)', fontSize: 'var(--font-size-sm)' }}>{cat.name}</div>
                <div style={{ fontSize: 11, color: 'var(--gray-400)' }}>{cat.description}</div>
              </div>
              <span style={{ fontSize: 12, color: 'var(--gray-400)' }}>{cat.products.toLocaleString()} products</span>
              <span className={`tag ${cat.active ? 'tag-green' : 'tag-gray'}`}>{cat.active ? 'Active' : 'Inactive'}</span>
              <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
                <button className="btn btn-secondary btn-sm" id={`add-subcategory-${cat.id}`} onClick={() => { setModal({ type: 'addSub', data: cat }); setForm({ name: '', description: '', icon: '📦', parentId: cat.id }); }}>
                  <Plus size={12} /> Sub
                </button>
                <button className="btn btn-secondary btn-sm" onClick={() => toggleCategoryActive(cat.id)}>
                  {cat.active ? 'Deactivate' : 'Activate'}
                </button>
                <button className="btn btn-danger btn-sm" id={`delete-category-${cat.id}`} onClick={() => deleteCategory(cat.id)}>
                  <Trash2 size={12} />
                </button>
              </div>
            </div>

            {/* Subcategories */}
            {expanded[cat.id] && cat.subcategories?.map(sub => (
              <div key={sub.id} style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', padding: 'var(--space-3) var(--space-5) var(--space-3) 72px', background: 'var(--gray-50)', borderTop: '1px solid var(--gray-100)' }}>
                <div style={{ width: 8, height: 8, borderRadius: '50%', background: sub.active ? 'var(--success)' : 'var(--gray-300)', flexShrink: 0 }} />
                <div style={{ flex: 1, fontSize: 'var(--font-size-sm)', fontWeight: 500, color: sub.active ? 'var(--gray-700)' : 'var(--gray-400)' }}>{sub.name}</div>
                <span style={{ fontSize: 11, color: 'var(--gray-400)' }}>{sub.products} products</span>
                <span className={`tag ${sub.active ? 'tag-green' : 'tag-gray'}`} style={{ fontSize: 10 }}>{sub.active ? 'Active' : 'Inactive'}</span>
                <button className="btn btn-secondary btn-sm"><Edit2 size={11} /></button>
              </div>
            ))}
          </div>
        ))}
      </div>

      {/* Modal */}
      {modal && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" style={{ maxWidth: 440 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">{modal.type === 'add' ? 'Add Category' : `Add Subcategory to ${modal.data?.name}`}</span>
              <button className="modal-close" onClick={() => setModal(null)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              {modal.type === 'add' && (
                <div className="form-group">
                  <label className="form-label">Icon (emoji)</label>
                  <input className="form-input" value={form.icon} onChange={e => setForm(f => ({ ...f, icon: e.target.value }))} placeholder="📦" />
                </div>
              )}
              <div className="form-group">
                <label className="form-label">Name <span className="required">*</span></label>
                <input id="category-name-input" className="form-input" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} placeholder="Category name…" />
              </div>
              {modal.type === 'add' && (
                <div className="form-group">
                  <label className="form-label">Description</label>
                  <input className="form-input" value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} placeholder="Short description…" />
                </div>
              )}
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
              <button className="btn btn-primary" id="save-category-btn" onClick={handleSave}><Save size={14} /> Save</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
