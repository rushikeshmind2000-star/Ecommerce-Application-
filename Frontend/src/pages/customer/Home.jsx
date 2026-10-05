import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShoppingBag, Package, Heart, Clock, CheckCircle, Truck, Star, ArrowRight, ShoppingCart } from 'lucide-react';
import { useApp } from '../../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const STATUS_META = {
  DELIVERED:  { label: 'Delivered',  cls: 'tag-green',  icon: CheckCircle },
  SHIPPED:    { label: 'Shipped',    cls: 'tag-blue',   icon: Truck },
  CONFIRMED:  { label: 'Confirmed',  cls: 'tag-purple', icon: CheckCircle },
  PROCESSING: { label: 'Processing', cls: 'tag-yellow', icon: Clock },
  PENDING:    { label: 'Pending',    cls: 'tag-orange', icon: Clock },
  CANCELLED:  { label: 'Cancelled',  cls: 'tag-red',    icon: Clock },
};

export default function CustomerHome() {
  const navigate = useNavigate();
  const { products, orders, cartCount, wishlist, user, addToCart } = useApp();
  const [currentSlide, setCurrentSlide] = useState(0);

  const activeProducts = products.filter(p => p.status === 'ACTIVE');
  const featured = activeProducts.slice(0, 4);
  const trending = [...activeProducts].sort((a, b) => b.sold - a.sold).slice(0, 4);
  
  const recentOrders = orders.slice(0, 3);
  const totalSpent = orders.filter(o => o.status === 'DELIVERED').reduce((s, o) => s + o.totalAmount, 0);

  const CATEGORIES = [
    { name: 'Electronics', icon: '📱', color: 'var(--primary-light)', text: 'var(--primary)' },
    { name: 'Footwear', icon: '👟', color: 'var(--success-light)', text: 'var(--success)' },
    { name: 'Audio', icon: '🎧', color: 'var(--warning-light)', text: 'var(--warning)' },
    { name: 'Laptops', icon: '💻', color: 'var(--accent-light)', text: 'var(--accent)' },
    { name: 'Apparel', icon: '👕', color: 'var(--danger-light)', text: 'var(--danger)' },
  ];

  const BANNERS = [
    {
      tag: `Welcome back, ${user?.firstName || 'Guest'}! 👋`,
      title: 'Latest iPhones',
      subtitle: 'iPhone 15 Pro — Now from ₹1,34,900',
      cta: 'Shop Phones',
      badge: '🔥 BESTSELLER',
      bg: 'linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%)',
      img: 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600&q=80',
    },
    {
      tag: '⚡ LIMITED TIME DEAL',
      title: 'Big Tech Sale',
      subtitle: 'Up to 40% off on Laptops & Accessories',
      cta: 'Grab Deal',
      badge: '⏰ Ends Soon',
      bg: 'linear-gradient(135deg, #064e3b 0%, #10b981 100%)',
      img: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&q=80',
    },
    {
      tag: '🎧 AUDIO COLLECTION',
      title: 'Immersive Sound',
      subtitle: 'Sony WH-1000XM5 — Noise Cancellation',
      cta: 'Shop Audio',
      badge: '⭐ Top Rated',
      bg: 'linear-gradient(135deg, #4c1d95 0%, #7c3aed 100%)',
      img: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&q=80',
    },
    {
      tag: '👟 NEW ARRIVALS',
      title: 'Run Faster',
      subtitle: 'Nike Air Max 2024 — Comfort Redefined',
      cta: 'Shop Footwear',
      badge: '🆕 Just Landed',
      bg: 'linear-gradient(135deg, #7f1d1d 0%, #ef4444 100%)',
      img: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&q=80',
    },
    {
      tag: '🌟 FASHION WEEK',
      title: 'New Season Style',
      subtitle: 'Fresh drops from top brands — Up to 30% off',
      cta: 'Explore Fashion',
      badge: '✨ Trending',
      bg: 'linear-gradient(135deg, #713f12 0%, #f59e0b 100%)',
      img: 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=600&q=80',
    },
  ];

  const prevSlide = () => setCurrentSlide(s => (s - 1 + BANNERS.length) % BANNERS.length);
  const nextSlide = () => setCurrentSlide(s => (s + 1) % BANNERS.length);

  useEffect(() => {
    const timer = setInterval(() => setCurrentSlide(s => (s + 1) % BANNERS.length), 3000);
    return () => clearInterval(timer);
  }, [BANNERS.length]);

  return (
    <div className="page-container">
      {/* Advertisement Image Carousel */}
      <div style={{ position: 'relative', borderRadius: 'var(--radius-2xl)', overflow: 'hidden', marginBottom: 'var(--space-6)', height: 280, boxShadow: '0 20px 60px rgba(0,0,0,0.18)' }}>
        {BANNERS.map((b, i) => (
          <div key={i} style=
{{
            position: 'absolute', inset: 0,
            background: b.bg,
            opacity: currentSlide === i ? 1 : 0,
            transform: currentSlide === i ? 'translateX(0)' : i < currentSlide ? 'translateX(-40px)' : 'translateX(40px)',
            transition: 'all 0.55s cubic-bezier(0.4,0,0.2,1)',
            zIndex: currentSlide === i ? 1 : 0,
            display: 'flex', alignItems: 'stretch',
          }}>
            {/* Left — Text Content */}
            <div style={{ flex: 1, padding: 'var(--space-8) var(--space-10)', display: 'flex', flexDirection: 'column', justifyContent: 'center', zIndex: 1, position: 'relative' }}>
              {/* Decorative blobs */}
              <div style={{ position: 'absolute', top: -50, left: -50, width: 180, height: 180, borderRadius: '50%', background: 'rgba(255,255,255,0.06)', pointerEvents: 'none' }} />
              <div style={{ fontSize: 10, fontWeight: 700, letterSpacing: 1.5, textTransform: 'uppercase', color: 'rgba(255,255,255,0.7)', marginBottom: 'var(--space-2)' }}>{b.tag}</div>
              <h2 style={{ fontSize: 'var(--font-size-3xl)', fontWeight: 900, color: 'white', lineHeight: 1.1, margin: '0 0 var(--space-2) 0' }}>{b.title}</h2>
              <p style={{ fontSize: 'var(--font-size-sm)', color: 'rgba(255,255,255,0.85)', marginBottom: 'var(--space-5)', fontWeight: 400 }}>{b.subtitle}</p>
              <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                <button className="btn" style={{ background: 'white', color: '#111', fontWeight: 700, fontSize: 13, padding: '10px 22px', boxShadow: '0 4px 14px rgba(0,0,0,0.2)' }} onClick={() => navigate('/customer/products')}>
                  <ShoppingBag size={14} /> {b.cta} <ArrowRight size={14} />
                </button>
                <span style={{ fontSize: 11, color: 'rgba(255,255,255,0.9)', background: 'rgba(255,255,255,0.18)', padding: '4px 10px', borderRadius: 'var(--radius-full)', fontWeight: 600, backdropFilter: 'blur(6px)' }}>{b.badge}</span>
              </div>
            </div>
            {/* Right — Product Image */}
            <div style={{ width: 300, position: 'relative', overflow: 'hidden', flexShrink: 0 }}>
              <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.1)' }} />
              <img src={b.img} alt={b.title}
                style={{ width: '100%', height: '100%', objectFit: 'cover', objectPosition: 'center', transform: currentSlide === i ? 'scale(1)' : 'scale(1.06)', transition: 'transform 0.6s ease' }} />
              {/* Gradient blend from left */}
              <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(to right, rgba(0,0,0,0.3) 0%, transparent 40%)' }} />
            </div>
          </div>
        ))}

        {/* Prev / Next arrows */}
        <button onClick={prevSlide} style={{ position: 'absolute', left: 14, top: '50%', transform: 'translateY(-50%)', zIndex: 10, width: 36, height: 36, borderRadius: '50%', border: 'none', background: 'rgba(255,255,255,0.25)', backdropFilter: 'blur(6px)', color: 'white', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', transition: 'background 0.2s', fontSize: 18 }}
          onMouseEnter={e => e.currentTarget.style.background = 'rgba(255,255,255,0.45)'}
          onMouseLeave={e => e.currentTarget.style.background = 'rgba(255,255,255,0.25)'}>
          ‹
        </button>
        <button onClick={nextSlide} style={{ position: 'absolute', right: 14, top: '50%', transform: 'translateY(-50%)', zIndex: 10, width: 36, height: 36, borderRadius: '50%', border: 'none', background: 'rgba(255,255,255,0.25)', backdropFilter: 'blur(6px)', color: 'white', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', transition: 'background 0.2s', fontSize: 18 }}
          onMouseEnter={e => e.currentTarget.style.background = 'rgba(255,255,255,0.45)'}
          onMouseLeave={e => e.currentTarget.style.background = 'rgba(255,255,255,0.25)'}>
          ›
        </button>

        {/* Slide indicators */}
        <div style={{ position: 'absolute', bottom: 16, left: '50%', transform: 'translateX(-50%)', display: 'flex', gap: 7, zIndex: 10 }}>
          {BANNERS.map((_, i) => (
            <button key={i} onClick={() => setCurrentSlide(i)}
              style={{ width: currentSlide === i ? 28 : 8, height: 8, borderRadius: 4, background: 'white', opacity: currentSlide === i ? 1 : 0.45, border: 'none', cursor: 'pointer', transition: 'all 0.35s ease', padding: 0 }} />
          ))}
        </div>

        {/* Progress bar */}
        <div style={{ position: 'absolute', bottom: 0, left: 0, height: 3, background: 'rgba(255,255,255,0.3)', width: '100%', zIndex: 10 }}>
          <div key={currentSlide} style={{ height: '100%', background: 'white', animation: 'progressBar 3s linear', borderRadius: '0 2px 2px 0' }} />
        </div>
      </div>

      {/* Categories Quick Links */}
      <div style={{ display: 'flex', gap: 'var(--space-4)', marginBottom: 'var(--space-8)', overflowX: 'auto', paddingBottom: 'var(--space-2)', scrollbarWidth: 'none' }}>
        {CATEGORIES.map(c => (
          <div key={c.name} onClick={() => navigate('/customer/products')} style={{ flex: '1 0 auto', minWidth: 100, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-2)', cursor: 'pointer', transition: 'transform 0.2s' }} onMouseEnter={e => e.currentTarget.style.transform = 'translateY(-5px)'} onMouseLeave={e => e.currentTarget.style.transform = ''}>
            <div style={{ width: 64, height: 64, borderRadius: '50%', background: c.color, color: c.text, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 28, boxShadow: 'var(--shadow-sm)' }}>
              {c.icon}
            </div>
            <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--gray-700)' }}>{c.name}</span>
          </div>
        ))}
      </div>

      {/* Quick Stats */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)', marginBottom: 'var(--space-6)' }}>
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => navigate('/customer/orders')}>
          <div className="stat-icon blue"><ShoppingBag size={20} /></div>
          <div className="stat-info"><div className="stat-label">My Orders</div><div className="stat-value">{orders.length}</div></div>
        </div>
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => navigate('/customer/cart')}>
          <div className="stat-icon orange"><ShoppingCart size={20} /></div>
          <div className="stat-info"><div className="stat-label">Cart Items</div><div className="stat-value">{cartCount}</div></div>
        </div>
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => navigate('/customer/wishlist')}>
          <div className="stat-icon red"><Heart size={20} /></div>
          <div className="stat-info"><div className="stat-label">Wishlist</div><div className="stat-value">{wishlist.length}</div></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon green"><Package size={20} /></div>
          <div className="stat-info"><div className="stat-label">Total Spent</div><div className="stat-value">{fmt(totalSpent)}</div></div>
        </div>
      </div>

      {/* Featured Products */}
      <ProductSection title="⭐ Featured For You" products={featured} navigate={navigate} addToCart={addToCart} />

      {/* Trending Products */}
      <ProductSection title="🔥 Trending Now" products={trending} navigate={navigate} addToCart={addToCart} />

      {/* Recent Orders */}
      {recentOrders.length > 0 && (
        <div className="card">
          <div className="card-header">
            <span className="card-title">Recent Orders</span>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate('/customer/orders')}>
              View All <ArrowRight size={13} />
            </button>
          </div>
          <div className="table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
            <table>
              <thead><tr><th>Order #</th><th>Items</th><th>Amount</th><th>Status</th><th>Date</th></tr></thead>
              <tbody>
                {recentOrders.map(o => {
                  const sm = STATUS_META[o.status] || { label: o.status, cls: 'tag-gray' };
                  return (
                    <tr key={o.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/customer/orders/${o.id}`)}>
                      <td className="td-primary">{o.orderNumber}</td>
                      <td>{o.items.length} item{o.items.length > 1 ? 's' : ''}</td>
                      <td className="font-bold">{fmt(o.totalAmount)}</td>
                      <td><span className={`tag ${sm.cls}`}>{sm.label}</span></td>
                      <td className="text-gray text-sm">{new Date(o.createdAt).toLocaleDateString('en-IN')}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

// ── Reusable Product Section Component ──────────────────────────
function ProductSection({ title, products, navigate, addToCart }) {
  return (
    <div className="card" style={{ marginBottom: 'var(--space-6)' }}>
      <div className="card-header">
        <span className="card-title">{title}</span>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/customer/products')}>
          View All <ArrowRight size={13} />
        </button>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 'var(--space-4)', padding: 'var(--space-4)' }}>
        {products.map(p => (
          <MiniProductCard key={p.id} p={p} navigate={navigate} addToCart={addToCart} />
        ))}
      </div>
    </div>
  );
}

function MiniProductCard({ p, navigate, addToCart }) {
  const [hovered, setHovered] = useState(false);
  const [adding, setAdding]   = useState(false);

  const handleAdd = (e) => {
    e.stopPropagation();
    setAdding(true);
    addToCart(p);
    setTimeout(() => setAdding(false), 1000);
  };

  return (
    <div
      onClick={() => navigate(`/customer/products/${p.id}`)}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      style={{
        border: '1px solid var(--gray-100)',
        borderRadius: 'var(--radius-xl)',
        overflow: 'hidden',
        cursor: 'pointer',
        transition: 'all 0.25s ease',
        transform: hovered ? 'translateY(-6px)' : 'none',
        boxShadow: hovered ? 'var(--shadow-lg)' : 'none',
        background: 'var(--white)',
      }}
    >
      <div style={{ height: 140, background: 'var(--gray-100)', overflow: 'hidden', position: 'relative' }}>
        <img
          src={p.images?.[0]?.imageUrl}
          alt={p.name}
          style={{ width: '100%', height: '100%', objectFit: 'cover', transition: 'transform 0.4s ease', transform: hovered ? 'scale(1.08)' : 'scale(1)' }}
        />
        {p.discount > 0 && (
          <div style={{ position: 'absolute', top: 8, left: 8, background: 'var(--danger)', color: 'white', fontSize: 10, fontWeight: 700, padding: '2px 8px', borderRadius: 'var(--radius-full)' }}>
            -{p.discount}%
          </div>
        )}
      </div>
      <div style={{ padding: 'var(--space-3)' }}>
        <div style={{ fontSize: 10, color: 'var(--gray-400)', marginBottom: 2, textTransform: 'uppercase', letterSpacing: 0.5 }}>{p.category}</div>
        <div style={{ fontWeight: 600, fontSize: 'var(--font-size-sm)', color: 'var(--gray-900)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', marginBottom: 4 }}>{p.name}</div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-2)' }}>
          <div style={{ fontWeight: 800, color: 'var(--primary)', fontSize: 'var(--font-size-sm)' }}>
            ₹{p.price.toLocaleString('en-IN')}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 3, color: 'var(--warning)', fontSize: 11 }}>
            <Star size={10} fill="currentColor" /> {p.rating}
          </div>
        </div>
        <button
          className="btn btn-primary btn-sm btn-full"
          style={{ fontSize: 11, transition: 'all 0.2s', background: adding ? 'var(--success)' : '' }}
          onClick={handleAdd}
          disabled={p.status === 'OUT_OF_STOCK'}
        >
          <ShoppingCart size={11} />
          {p.status === 'OUT_OF_STOCK' ? 'Sold Out' : adding ? '✓ Added!' : 'Add to Cart'}
        </button>
      </div>
    </div>
  );
}
