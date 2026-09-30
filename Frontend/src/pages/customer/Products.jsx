import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Star, Heart, ShoppingCart, Package, SlidersHorizontal, X, ChevronDown, Zap, TrendingUp } from 'lucide-react';
import { useApp } from '../../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const CATEGORIES = ['All', 'Electronics', 'Footwear', 'Audio', 'Laptops', 'Apparel'];
const SORT_OPTIONS = [
  { label: 'Best Match',    value: 'default' },
  { label: 'Top Rated',    value: 'rating' },
  { label: 'Best Selling', value: 'sold' },
  { label: 'Price: Low→High', value: 'price-asc' },
  { label: 'Price: High→Low', value: 'price-desc' },
  { label: 'Newest First', value: 'newest' },
];

export default function CustomerProducts() {
  const { products, addToCart, wishlist, toggleWishlist } = useApp();
  const navigate = useNavigate();
  const [search, setSearch]     = useState('');
  const [category, setCategory] = useState('All');
  const [sort, setSort]         = useState('default');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [showFilter, setShowFilter] = useState(false);
  const [viewMode, setViewMode]     = useState('grid'); // 'grid' | 'list'
  const [animKey, setAnimKey]       = useState(0);

  // Re-trigger card entrance animation on filter change
  useEffect(() => { setAnimKey(k => k + 1); }, [search, category, sort, minPrice, maxPrice]);

  const filtered = useMemo(() => {
    return products
      .filter(p => {
        if (p.status === 'PENDING_APPROVAL' || p.status === 'REJECTED') return false;
        const q = search.toLowerCase();
        if (q && !p.name.toLowerCase().includes(q) && !p.brand?.toLowerCase().includes(q) && !p.category?.toLowerCase().includes(q)) return false;
        if (category !== 'All' && p.category !== category) return false;
        if (minPrice && p.price < Number(minPrice)) return false;
        if (maxPrice && p.price > Number(maxPrice)) return false;
        return true;
      })
      .sort((a, b) => {
        if (sort === 'price-asc')  return a.price - b.price;
        if (sort === 'price-desc') return b.price - a.price;
        if (sort === 'rating')     return b.rating - a.rating;
        if (sort === 'sold')       return (b.sold || 0) - (a.sold || 0);
        return 0;
      });
  }, [products, search, category, sort, minPrice, maxPrice]);

  const clearFilters = () => { setSearch(''); setCategory('All'); setMinPrice(''); setMaxPrice(''); };
  const activeFilters = [search && `"${search}"`, category !== 'All' && category, minPrice && `₹${minPrice}+`, maxPrice && `≤₹${maxPrice}`].filter(Boolean);

  return (
    <div className="page-container">
      {/* Header */}
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">Shop All Products</h1>
          <p className="page-subtitle">
            {filtered.length} products
            {activeFilters.length > 0 && <span style={{ color: 'var(--primary)', marginLeft: 6 }}>· Filtered by: {activeFilters.join(', ')}</span>}
          </p>
        </div>
        <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
          {/* View Mode Toggle */}
          <div style={{ display: 'flex', background: 'var(--gray-100)', borderRadius: 'var(--radius-lg)', padding: 2, gap: 2 }}>
            {['grid', 'list'].map(m => (
              <button key={m} onClick={() => setViewMode(m)}
                style={{ padding: '6px 14px', borderRadius: 'var(--radius-md)', border: 'none', background: viewMode === m ? 'var(--white)' : 'transparent', color: viewMode === m ? 'var(--primary)' : 'var(--gray-500)', fontWeight: viewMode === m ? 700 : 400, cursor: 'pointer', fontSize: 12, transition: 'all 0.2s', boxShadow: viewMode === m ? 'var(--shadow-sm)' : 'none' }}>
                {m === 'grid' ? '⊞ Grid' : '☰ List'}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Search + Filter Bar */}
      <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-4) var(--space-5)', marginBottom: 'var(--space-5)', display: 'flex', gap: 'var(--space-3)', flexWrap: 'wrap', alignItems: 'center', boxShadow: 'var(--shadow-sm)' }}>
        <div className="input-wrapper" style={{ flex: 1, minWidth: 220 }}>
          <Search size={16} className="input-icon-left" />
          <input id="customer-product-search" type="search" className="form-input" placeholder="Search products, brands, categories…"
            value={search} onChange={e => setSearch(e.target.value)} style={{ paddingRight: search ? 36 : undefined }} />
          {search && (
            <button onClick={() => setSearch('')} style={{ position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--gray-400)' }}>
              <X size={14} />
            </button>
          )}
        </div>
        <select id="customer-product-sort" className="form-select" style={{ minWidth: 170 }} value={sort} onChange={e => setSort(e.target.value)}>
          {SORT_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
        <button className={`btn ${showFilter ? 'btn-primary' : 'btn-secondary'} btn-sm`} onClick={() => setShowFilter(s => !s)} id="customer-filter-toggle">
          <SlidersHorizontal size={14} /> Filters {activeFilters.length > 0 && <span style={{ background: 'white', color: 'var(--primary)', borderRadius: 'var(--radius-full)', padding: '1px 6px', fontSize: 10, fontWeight: 800 }}>{activeFilters.length}</span>}
        </button>
        {activeFilters.length > 0 && (
          <button className="btn btn-secondary btn-sm" onClick={clearFilters}><X size={13} /> Clear All</button>
        )}
      </div>

      {/* Category Pills */}
      <div style={{ display: 'flex', gap: 'var(--space-2)', marginBottom: 'var(--space-4)', flexWrap: 'wrap' }}>
        {CATEGORIES.map(c => (
          <button key={c} onClick={() => setCategory(c)}
            style={{ padding: '6px 16px', borderRadius: 'var(--radius-full)', border: '1.5px solid', fontWeight: 600, fontSize: 'var(--font-size-xs)', cursor: 'pointer', transition: 'all 0.2s', borderColor: category === c ? 'var(--primary)' : 'var(--gray-200)', background: category === c ? 'var(--primary)' : 'var(--white)', color: category === c ? 'white' : 'var(--gray-600)', transform: category === c ? 'scale(1.05)' : 'scale(1)' }}>
            {c}
          </button>
        ))}
      </div>

      {/* Price Filter Panel */}
      {showFilter && (
        <div style={{ background: 'var(--white)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)', padding: 'var(--space-5)', marginBottom: 'var(--space-5)', display: 'flex', gap: 'var(--space-5)', alignItems: 'flex-end', flexWrap: 'wrap', boxShadow: 'var(--shadow-sm)', animation: 'slideDown 0.2s ease' }}>
          <div>
            <label className="form-label">Min Price (₹)</label>
            <input id="min-price" type="number" className="form-input" placeholder="0" value={minPrice} onChange={e => setMinPrice(e.target.value)} style={{ width: 140 }} />
          </div>
          <div>
            <label className="form-label">Max Price (₹)</label>
            <input id="max-price" type="number" className="form-input" placeholder="Any" value={maxPrice} onChange={e => setMaxPrice(e.target.value)} style={{ width: 140 }} />
          </div>
          <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
            {[5000, 15000, 50000, 100000].map(p => (
              <button key={p} onClick={() => setMaxPrice(String(p))}
                style={{ padding: '6px 12px', borderRadius: 'var(--radius-md)', border: '1.5px solid var(--gray-200)', background: maxPrice === String(p) ? 'var(--primary)' : 'var(--white)', color: maxPrice === String(p) ? 'white' : 'var(--gray-600)', fontSize: 12, cursor: 'pointer', transition: 'all 0.2s', fontWeight: 600 }}>
                ≤ ₹{(p / 1000).toFixed(0)}k
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Product Grid / List */}
      {filtered.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><Package size={32} /></div>
          <div className="empty-state-title">No products found</div>
          <div className="empty-state-text">Try adjusting your filters or search terms</div>
          <button className="btn btn-primary" onClick={clearFilters}><X size={14} /> Clear Filters</button>
        </div>
      ) : viewMode === 'grid' ? (
        <div key={animKey} style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 'var(--space-5)' }}>
          {filtered.map((p, i) => (
            <ProductCard key={p.id} product={p} index={i}
              onNavigate={() => navigate(`/customer/products/${p.id}`)}
              onAddToCart={() => addToCart(p)}
              isWishlisted={wishlist.includes(p.id)}
              onWishlist={() => toggleWishlist(p.id)} />
          ))}
        </div>
      ) : (
        <div key={animKey} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
          {filtered.map((p, i) => (
            <ProductListCard key={p.id} product={p} index={i}
              onNavigate={() => navigate(`/customer/products/${p.id}`)}
              onAddToCart={() => addToCart(p)}
              isWishlisted={wishlist.includes(p.id)}
              onWishlist={() => toggleWishlist(p.id)} />
          ))}
        </div>
      )}
    </div>
  );
}

/* ── Grid Card ─────────────────────────────────── */
function ProductCard({ product: p, index, onNavigate, onAddToCart, isWishlisted, onWishlist }) {
  const [hovered, setHovered] = useState(false);
  const [adding, setAdding]   = useState(false);

  const handleAdd = (e) => {
    e.stopPropagation();
    if (p.status === 'OUT_OF_STOCK') return;
    setAdding(true);
    onAddToCart();
    setTimeout(() => setAdding(false), 1200);
  };

  return (
    <div
      onClick={onNavigate}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      style={{
        border: `1px solid ${hovered ? 'var(--primary)' : 'var(--gray-100)'}`,
        borderRadius: 'var(--radius-xl)',
        overflow: 'hidden',
        cursor: 'pointer',
        background: 'var(--white)',
        transition: 'all 0.25s ease',
        transform: hovered ? 'translateY(-8px)' : 'none',
        boxShadow: hovered ? '0 20px 40px rgba(99,102,241,0.15)' : 'var(--shadow-sm)',
        animation: `fadeSlideIn 0.4s ease both`,
        animationDelay: `${Math.min(index * 0.05, 0.5)}s`,
      }}>
      {/* Image */}
      <div style={{ height: 180, background: 'var(--gray-100)', overflow: 'hidden', position: 'relative' }}>
        <img src={p.images?.[0]?.imageUrl} alt={p.name}
          style={{ width: '100%', height: '100%', objectFit: 'cover', transition: 'transform 0.5s ease', transform: hovered ? 'scale(1.1)' : 'scale(1)' }} />
        {/* Overlay badges */}
        <div style={{ position: 'absolute', top: 10, left: 10, display: 'flex', flexDirection: 'column', gap: 4 }}>
          {p.status === 'OUT_OF_STOCK' && <span style={{ background: 'var(--danger)', color: 'white', fontSize: 9, fontWeight: 700, padding: '2px 8px', borderRadius: 'var(--radius-full)' }}>OUT OF STOCK</span>}
          {p.sold > 300 && p.status !== 'OUT_OF_STOCK' && <span style={{ background: 'var(--warning)', color: 'white', fontSize: 9, fontWeight: 700, padding: '2px 8px', borderRadius: 'var(--radius-full)', display: 'flex', alignItems: 'center', gap: 3 }}><TrendingUp size={9} /> TRENDING</span>}
          {p.rating >= 4.8 && <span style={{ background: 'var(--primary)', color: 'white', fontSize: 9, fontWeight: 700, padding: '2px 8px', borderRadius: 'var(--radius-full)', display: 'flex', alignItems: 'center', gap: 3 }}><Zap size={9} /> TOP RATED</span>}
        </div>
        {/* Wishlist */}
        <button onClick={e => { e.stopPropagation(); onWishlist(); }}
          style={{ position: 'absolute', top: 10, right: 10, width: 32, height: 32, borderRadius: '50%', border: 'none', background: 'rgba(255,255,255,0.9)', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', transition: 'all 0.2s', color: isWishlisted ? 'var(--danger)' : 'var(--gray-400)', transform: isWishlisted ? 'scale(1.2)' : 'scale(1)', backdropFilter: 'blur(4px)' }}>
          <Heart size={14} fill={isWishlisted ? 'currentColor' : 'none'} />
        </button>
        {/* Quick view on hover */}
        {hovered && (
          <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, background: 'rgba(99,102,241,0.92)', color: 'white', fontSize: 11, fontWeight: 600, textAlign: 'center', padding: '6px', transition: 'all 0.2s' }}>
            Quick View →
          </div>
        )}
      </div>

      {/* Info */}
      <div style={{ padding: 'var(--space-4)' }}>
        <div style={{ fontSize: 10, color: 'var(--gray-400)', marginBottom: 3, textTransform: 'uppercase', letterSpacing: 0.8 }}>{p.category} · {p.brand}</div>
        <div style={{ fontWeight: 700, fontSize: 'var(--font-size-sm)', color: 'var(--gray-900)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', marginBottom: 6 }}>{p.name}</div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-3)' }}>
          <div style={{ fontWeight: 800, color: 'var(--primary)', fontSize: 'var(--font-size-base)' }}>
            ₹{p.price.toLocaleString('en-IN')}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 3, color: 'var(--warning)', fontSize: 12, fontWeight: 600 }}>
            <Star size={12} fill="currentColor" /> {p.rating}
            <span style={{ color: 'var(--gray-400)', fontWeight: 400 }}>({p.reviews >= 1000 ? (p.reviews/1000).toFixed(1)+'k' : p.reviews})</span>
          </div>
        </div>
        <button
          className="btn btn-primary btn-sm btn-full"
          style={{ transition: 'all 0.25s', background: adding ? 'var(--success)' : p.status === 'OUT_OF_STOCK' ? 'var(--gray-300)' : '', fontSize: 12 }}
          onClick={handleAdd} disabled={p.status === 'OUT_OF_STOCK'}>
          <ShoppingCart size={12} />
          {p.status === 'OUT_OF_STOCK' ? 'Sold Out' : adding ? '✓ Added to Cart!' : 'Add to Cart'}
        </button>
      </div>
    </div>
  );
}

/* ── List Card ─────────────────────────────────── */
function ProductListCard({ product: p, index, onNavigate, onAddToCart, isWishlisted, onWishlist }) {
  const [adding, setAdding] = useState(false);
  const handleAdd = (e) => {
    e.stopPropagation();
    if (p.status === 'OUT_OF_STOCK') return;
    setAdding(true);
    onAddToCart();
    setTimeout(() => setAdding(false), 1200);
  };
  return (
    <div onClick={onNavigate} style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)', background: 'var(--white)', border: '1px solid var(--gray-100)', borderRadius: 'var(--radius-xl)', padding: 'var(--space-4)', cursor: 'pointer', transition: 'all 0.2s', animation: `fadeSlideIn 0.4s ease both`, animationDelay: `${Math.min(index * 0.04, 0.4)}s` }}
      onMouseEnter={e => { e.currentTarget.style.borderColor = 'var(--primary)'; e.currentTarget.style.boxShadow = '0 4px 20px rgba(99,102,241,0.1)'; }}
      onMouseLeave={e => { e.currentTarget.style.borderColor = 'var(--gray-100)'; e.currentTarget.style.boxShadow = ''; }}>
      <div style={{ width: 90, height: 90, borderRadius: 'var(--radius-lg)', overflow: 'hidden', flexShrink: 0, background: 'var(--gray-100)' }}>
        <img src={p.images?.[0]?.imageUrl} alt={p.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
      </div>
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: 10, color: 'var(--gray-400)', textTransform: 'uppercase', letterSpacing: 0.8 }}>{p.category} · {p.brand}</div>
        <div style={{ fontWeight: 700, fontSize: 'var(--font-size-base)', color: 'var(--gray-900)', margin: '4px 0' }}>{p.name}</div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 3, color: 'var(--warning)', fontSize: 12 }}><Star size={11} fill="currentColor" /> {p.rating}</div>
          <span style={{ color: 'var(--gray-300)' }}>·</span>
          <span style={{ fontSize: 11, color: 'var(--gray-400)' }}>{p.reviews?.toLocaleString()} reviews</span>
          {p.sold > 300 && <span style={{ fontSize: 10, color: 'var(--warning)', fontWeight: 600 }}>🔥 {p.sold?.toLocaleString()} sold</span>}
        </div>
      </div>
      <div style={{ textAlign: 'right', flexShrink: 0 }}>
        <div style={{ fontWeight: 800, fontSize: 'var(--font-size-lg)', color: 'var(--primary)', marginBottom: 'var(--space-2)' }}>₹{p.price.toLocaleString('en-IN')}</div>
        <div style={{ display: 'flex', gap: 'var(--space-2)', justifyContent: 'flex-end' }}>
          <button onClick={e => { e.stopPropagation(); onWishlist(); }} style={{ width: 34, height: 34, borderRadius: '50%', border: '1px solid var(--gray-200)', background: 'var(--white)', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', color: isWishlisted ? 'var(--danger)' : 'var(--gray-400)', transition: 'all 0.2s' }}>
            <Heart size={14} fill={isWishlisted ? 'currentColor' : 'none'} />
          </button>
          <button className="btn btn-primary btn-sm" style={{ transition: 'all 0.2s', background: adding ? 'var(--success)' : '', minWidth: 130 }} onClick={handleAdd} disabled={p.status === 'OUT_OF_STOCK'}>
            <ShoppingCart size={12} /> {p.status === 'OUT_OF_STOCK' ? 'Sold Out' : adding ? '✓ Added!' : 'Add to Cart'}
          </button>
        </div>
      </div>
    </div>
  );
}
