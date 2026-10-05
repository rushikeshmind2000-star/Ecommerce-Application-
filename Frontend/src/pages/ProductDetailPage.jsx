import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Star, Heart, ShoppingCart, Truck, Shield, Package,
  ArrowLeft, ChevronRight, Minus, Plus, Share2, Tag
} from 'lucide-react';
import { useApp } from '../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

export default function ProductDetailPage() {
  const { id }     = useParams();
  const navigate   = useNavigate();
  const { products, addToCart, wishlist, toggleWishlist } = useApp();

  const product = products.find(p => p.id === id);
  const [qty, setQty]             = useState(1);
  const [activeImg, setActiveImg] = useState(0);

  if (!product) return (
    <div className="page-container">
      <div className="empty-state">
        <div className="empty-state-icon"><Package size={32} /></div>
        <div className="empty-state-title">Product not found</div>
        <button className="btn btn-primary" onClick={() => navigate('/products')}>Back to Products</button>
      </div>
    </div>
  );

  const isWishlisted = wishlist.includes(product.id);
  const inStock = product.status === 'ACTIVE' && product.stock > 0;

  return (
    <div className="page-container">
      {/* Breadcrumb */}
      <div className="breadcrumb">
        <span className="breadcrumb-item" onClick={() => navigate('/dashboard')}>Home</span>
        <ChevronRight size={14} className="breadcrumb-sep" />
        <span className="breadcrumb-item" onClick={() => navigate('/products')}>Products</span>
        <ChevronRight size={14} className="breadcrumb-sep" />
        <span className="breadcrumb-item">{product.category}</span>
        <ChevronRight size={14} className="breadcrumb-sep" />
        <span className="breadcrumb-item current">{product.name}</span>
      </div>

      <div className="product-detail-layout">
        {/* Gallery */}
        <div className="product-gallery">
          <div className="product-main-image">
            {product.images?.[activeImg]?.imageUrl
              ? <img src={product.images[activeImg].imageUrl} alt={product.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              : <Package size={80} color="var(--gray-300)" />
            }
          </div>
          <div className="product-thumbnails">
            {product.images?.map((img, i) => (
              <div key={img.id} className={`product-thumb ${activeImg === i ? 'active' : ''}`} onClick={() => setActiveImg(i)}>
                <img src={img.imageUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              </div>
            ))}
          </div>
        </div>

        {/* Info */}
        <div>
          {/* Badges */}
          <div style={{ display: 'flex', gap: 'var(--space-2)', marginBottom: 'var(--space-4)' }}>
            <span className={`tag ${inStock ? 'tag-green' : 'tag-red'}`}>
              {inStock ? `In Stock (${product.stock})` : 'Out of Stock'}
            </span>
            <span className="tag tag-blue"><Tag size={10} /> {product.category}</span>
          </div>

          <h1 style={{ fontSize: 'var(--font-size-3xl)', fontWeight: 800, color: 'var(--gray-900)', lineHeight: 1.2, marginBottom: 'var(--space-2)' }}>
            {product.name}
          </h1>

          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', marginBottom: 'var(--space-4)' }}>
            <span style={{ color: 'var(--gray-500)', fontSize: 'var(--font-size-sm)' }}>by <strong style={{ color: 'var(--gray-700)' }}>{product.brand}</strong></span>
            <span style={{ color: 'var(--gray-300)' }}>·</span>
            <span style={{ color: 'var(--gray-500)', fontSize: 'var(--font-size-sm)' }}>SKU: <code>{product.sku}</code></span>
          </div>

          {/* Rating */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', marginBottom: 'var(--space-5)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
              {[1,2,3,4,5].map(s => (
                <Star key={s} size={16} fill={s <= Math.round(product.rating) ? 'var(--warning)' : 'none'} color="var(--warning)" />
              ))}
            </div>
            <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--gray-700)' }}>{product.rating}</span>
            <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-400)' }}>({product.reviews?.toLocaleString()} reviews)</span>
          </div>

          {/* Price */}
          <div style={{ marginBottom: 'var(--space-6)', padding: 'var(--space-5)', background: 'var(--gray-50)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--gray-200)' }}>
            <div style={{ fontSize: 'var(--font-size-4xl)', fontWeight: 900, color: 'var(--gray-900)' }}>
              {fmt(product.price)}
            </div>
            <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-400)', marginTop: 2 }}>
              Inclusive of all taxes · {product.currency}
            </div>
          </div>

          {/* Description */}
          <div style={{ marginBottom: 'var(--space-6)' }}>
            <h3 style={{ fontSize: 'var(--font-size-md)', fontWeight: 700, color: 'var(--gray-900)', marginBottom: 'var(--space-2)' }}>Description</h3>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-600)', lineHeight: 1.7 }}>{product.description}</p>
          </div>

          {/* Qty + Cart */}
          <div style={{ display: 'flex', gap: 'var(--space-4)', alignItems: 'center', marginBottom: 'var(--space-5)' }}>
            <div className="qty-control">
              <button className="qty-btn" onClick={() => setQty(q => Math.max(1, q - 1))}><Minus size={14} /></button>
              <span className="qty-val">{qty}</span>
              <button className="qty-btn" onClick={() => setQty(q => Math.min(product.stock, q + 1))}><Plus size={14} /></button>
            </div>
            <button id="add-to-cart-btn" className="btn btn-primary btn-lg" style={{ flex: 1 }}
              disabled={!inStock} onClick={() => addToCart(product, qty)}>
              <ShoppingCart size={18} /> {inStock ? `Add ${qty > 1 ? qty + ' ×' : ''} to Cart` : 'Out of Stock'}
            </button>
            <button id="wishlist-btn" className={`btn btn-secondary`} style={{ padding: 'var(--space-3)' }}
              onClick={() => toggleWishlist(product.id)}>
              <Heart size={18} fill={isWishlisted ? 'var(--danger)' : 'none'} color={isWishlisted ? 'var(--danger)' : 'currentColor'} />
            </button>
          </div>

          {/* Trust Badges */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 'var(--space-3)' }}>
            {[
              { icon: Truck, title: 'Free Delivery', sub: 'On orders over ₹499' },
              { icon: Shield, title: 'Secure Payment', sub: 'SSL encrypted' },
              { icon: Package, title: 'Easy Returns', sub: '30-day return policy' },
            ].map(t => (
              <div key={t.title} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', padding: 'var(--space-4)', background: 'var(--gray-50)', borderRadius: 'var(--radius-lg)', border: '1px solid var(--gray-200)' }}>
                <t.icon size={20} color="var(--primary)" style={{ marginBottom: 6 }} />
                <div style={{ fontSize: 'var(--font-size-xs)', fontWeight: 700, color: 'var(--gray-800)' }}>{t.title}</div>
                <div style={{ fontSize: '10px', color: 'var(--gray-500)' }}>{t.sub}</div>
              </div>
            ))}
          </div>

          {/* Product IDs for developers */}
          <div style={{ marginTop: 'var(--space-6)', padding: 'var(--space-4)', background: 'var(--gray-50)', borderRadius: 'var(--radius-lg)', border: '1px dashed var(--gray-300)' }}>
            <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--gray-400)', marginBottom: 4, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '.05em' }}>Product Details</div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 4, fontSize: 'var(--font-size-xs)', color: 'var(--gray-600)' }}>
              <span>ID: <code>{product.id}</code></span>
              <span>Category ID: <code>{product.categoryId}</code></span>
              <span>Stock: <code>{product.stock}</code></span>
              <span>Reserved: <code>{product.reserved}</code></span>
              <span>Sold: <code>{product.sold}</code></span>
              <span>Status: <code>{product.status}</code></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
