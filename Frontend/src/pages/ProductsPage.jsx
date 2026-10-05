import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Search, Filter, Grid, List, Star, Heart, ShoppingCart,
  Package, ChevronDown, SlidersHorizontal, X
} from 'lucide-react';
import { useApp } from '../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const STATUS_COLORS = {
  ACTIVE:      'tag-green',
  INACTIVE:    'tag-gray',
  OUT_OF_STOCK:'tag-red',
};

const CATEGORIES = ['All', 'Electronics', 'Footwear', 'Audio', 'Laptops', 'Apparel'];
const SORT_OPTIONS = [
  { label: 'Newest First',   value: 'newest'    },
  { label: 'Price: Low–High',value: 'price-asc' },
  { label: 'Price: High–Low',value: 'price-desc'},
  { label: 'Top Rated',      value: 'rating'    },
];

export default function ProductsPage() {
  const { products, addToCart, wishlist, toggleWishlist } = useApp();
  const navigate = useNavigate();

  const [search,   setSearch]   = useState('');
  const [category, setCategory] = useState('All');
  const [sort,     setSort]     = useState('newest');
  const [view,     setView]     = useState('grid');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [showFilter, setShowFilter] = useState(false);

  const filtered = products
    .filter(p => {
      const q = search.toLowerCase();
      if (q && !p.name.toLowerCase().includes(q) && !p.brand.toLowerCase().includes(q) && !p.sku.toLowerCase().includes(q)) return false;
      if (category !== 'All' && p.category !== category) return false;
      if (minPrice && p.price < Number(minPrice)) return false;
      if (maxPrice && p.price > Number(maxPrice)) return false;
      return true;
    })
    .sort((a, b) => {
      if (sort === 'price-asc')  return a.price - b.price;
      if (sort === 'price-desc') return b.price - a.price;
      if (sort === 'rating')     return b.rating - a.rating;
      return 0;
    });

  return (
    <div className="page-container">
      {/* Header */}
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Products</h1>
          <p className="page-subtitle">{filtered.length} products available</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-secondary" onClick={() => setView(v => v === 'grid' ? 'list' : 'grid')} id="toggle-view">
            {view === 'grid' ? <List size={16} /> : <Grid size={16} />}
            {view === 'grid' ? 'List View' : 'Grid View'}
          </button>
        </div>
      </div>

      {/* Filters Bar */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-6)', display: 'flex', gap: 'var(--space-4)', flexWrap: 'wrap', alignItems: 'center' }}>
        {/* Search */}
        <div className="input-wrapper" style={{ flex: 1, minWidth: 220 }}>
          <Search size={16} className="input-icon-left" />
          <input id="product-search" type="search" className="form-input" placeholder="Search by name, brand, SKU…"
            value={search} onChange={e => setSearch(e.target.value)} />
        </div>

        {/* Category pills */}
        <div style={{ display: 'flex', gap: 'var(--space-2)', flexWrap: 'wrap' }}>
          {CATEGORIES.map(c => (
            <button key={c}
              style={{ padding: '6px 14px', borderRadius: 'var(--radius-full)', border: '1.5px solid', borderColor: category === c ? 'var(--primary)' : 'var(--gray-200)', background: category === c ? 'var(--primary)' : 'var(--white)', color: category === c ? 'var(--white)' : 'var(--gray-600)', fontSize: 'var(--font-size-xs)', fontWeight: 600, cursor: 'pointer', transition: 'all var(--transition)' }}
              onClick={() => setCategory(c)}>
              {c}
            </button>
          ))}
        </div>

        {/* Sort */}
        <div style={{ position: 'relative' }}>
          <select id="product-sort" className="form-select" style={{ paddingRight: 32, appearance: 'none', minWidth: 160 }}
            value={sort} onChange={e => setSort(e.target.value)}>
            {SORT_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
          <ChevronDown size={14} style={{ position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none', color: 'var(--gray-400)' }} />
        </div>

        {/* Price Filter Toggle */}
        <button className={`btn ${showFilter ? 'btn-primary' : 'btn-secondary'} btn-sm`} onClick={() => setShowFilter(s => !s)} id="filter-toggle">
          <SlidersHorizontal size={14} /> Filter
        </button>
      </div>

      {/* Price Range Filter */}
      {showFilter && (
        <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-5)', marginBottom: 'var(--space-6)', display: 'flex', gap: 'var(--space-5)', alignItems: 'flex-end', flexWrap: 'wrap' }}>
          <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 140 }}>
            <label className="form-label">Min Price (₹)</label>
            <input id="min-price" type="number" className="form-input" placeholder="0" value={minPrice} onChange={e => setMinPrice(e.target.value)} />
          </div>
          <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 140 }}>
            <label className="form-label">Max Price (₹)</label>
            <input id="max-price" type="number" className="form-input" placeholder="Any" value={maxPrice} onChange={e => setMaxPrice(e.target.value)} />
          </div>
          <button className="btn btn-secondary btn-sm" onClick={() => { setMinPrice(''); setMaxPrice(''); }}>
            <X size={14} /> Clear
          </button>
        </div>
      )}

      {/* Product Grid */}
      {filtered.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><Package size={32} /></div>
          <div className="empty-state-title">No products found</div>
          <div className="empty-state-text">Try adjusting your filters or search term</div>
          <button className="btn btn-primary" onClick={() => { setSearch(''); setCategory('All'); setMinPrice(''); setMaxPrice(''); }}>Clear Filters</button>
        </div>
      ) : view === 'grid' ? (
        <div className="products-grid">
          {filtered.map(p => (
            <ProductCard key={p.id} product={p} onAddToCart={addToCart} onView={() => navigate(`/products/${p.id}`)}
              isWishlisted={wishlist.includes(p.id)} onWishlist={() => toggleWishlist(p.id)} />
          ))}
        </div>
      ) : (
        <div className="card">
          <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
            <table>
              <thead>
                <tr>
                  <th>Product</th>
                  <th>SKU</th>
                  <th>Category</th>
                  <th>Brand</th>
                  <th>Price</th>
                  <th>Status</th>
                  <th>Rating</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map(p => (
                  <tr key={p.id}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                        <div style={{ width: 40, height: 40, background: 'var(--gray-100)', borderRadius: 'var(--radius-md)', overflow: 'hidden', flexShrink: 0 }}>
                          <img src={p.images?.[0]?.imageUrl} alt={p.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                        </div>
                        <span className="td-primary" style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{p.name}</span>
                      </div>
                    </td>
                    <td className="text-gray text-sm">{p.sku}</td>
                    <td>{p.category}</td>
                    <td>{p.brand}</td>
                    <td className="font-bold">{fmt(p.price)}</td>
                    <td><span className={`tag ${STATUS_COLORS[p.status] || 'tag-gray'}`}>{p.status.replace('_', ' ')}</span></td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: 'var(--warning)' }}>
                        <Star size={12} fill="currentColor" /> {p.rating}
                      </div>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => navigate(`/products/${p.id}`)}>View</button>
                        <button className="btn btn-primary btn-sm" onClick={() => addToCart(p)} disabled={p.status === 'OUT_OF_STOCK'}>
                          <ShoppingCart size={12} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

function ProductCard({ product: p, onAddToCart, onView, isWishlisted, onWishlist }) {
  const STATUS_COLORS = { ACTIVE: 'tag-green', INACTIVE: 'tag-gray', OUT_OF_STOCK: 'tag-red' };
  return (
    <div className="product-card">
      <div className="product-image" onClick={onView}>
        {p.images?.[0]?.imageUrl
          ? <img src={p.images[0].imageUrl} alt={p.name} loading="lazy" />
          : <Package size={48} className="product-image-placeholder" />
        }
        <div className="product-badge">
          <span className={`tag ${STATUS_COLORS[p.status] || 'tag-gray'}`} style={{ fontSize: 10 }}>
            {p.status === 'OUT_OF_STOCK' ? 'Out of Stock' : p.status}
          </span>
        </div>
        <button className={`product-wishlist ${isWishlisted ? 'active' : ''}`} onClick={e => { e.stopPropagation(); onWishlist(); }}>
          <Heart size={14} fill={isWishlisted ? 'currentColor' : 'none'} />
        </button>
      </div>

      <div className="product-info" onClick={onView}>
        <div className="product-category">{p.category}</div>
        <div className="product-name">{p.name}</div>
        <div className="product-brand">{p.brand} · {p.sku}</div>
        <div className="product-footer">
          <div>
            <div className="product-price">
              <span className="currency">₹</span>
              {(p.price / 100).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </div>
          </div>
          <div className="product-rating">
            <Star size={12} fill="currentColor" /> {p.rating} ({p.reviews >= 1000 ? (p.reviews / 1000).toFixed(1) + 'k' : p.reviews})
          </div>
        </div>
      </div>

      <div className="product-actions">
        <button className="btn btn-secondary btn-sm" style={{ flex: 1 }} onClick={onView}>View Details</button>
        <button className="btn btn-primary btn-sm" style={{ flex: 1 }} onClick={() => onAddToCart(p)} disabled={p.status === 'OUT_OF_STOCK'}>
          <ShoppingCart size={13} />
          {p.status === 'OUT_OF_STOCK' ? 'Sold Out' : 'Add to Cart'}
        </button>
      </div>
    </div>
  );
}
