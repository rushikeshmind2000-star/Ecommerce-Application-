import { useNavigate } from 'react-router-dom';
import { Heart, ShoppingCart, Package, Trash2 } from 'lucide-react';
import { useApp } from '../../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

export default function CustomerWishlist() {
  const { products, wishlist, toggleWishlist, addToCart } = useApp();
  const navigate = useNavigate();

  const wishlisted = products.filter(p => wishlist.includes(p.id));

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">My Wishlist</h1>
          <p className="page-subtitle">{wishlisted.length} saved items</p>
        </div>
      </div>

      {wishlisted.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><Heart size={32} /></div>
          <div className="empty-state-title">Your wishlist is empty</div>
          <div className="empty-state-text">Save products you love and shop later</div>
          <button className="btn btn-primary" onClick={() => navigate('/customer/products')}>
            <Package size={15} /> Browse Products
          </button>
        </div>
      ) : (
        <div className="products-grid">
          {wishlisted.map(p => (
            <div key={p.id} className="product-card">
              <div className="product-image" onClick={() => navigate(`/customer/products/${p.id}`)}>
                {p.images?.[0]?.imageUrl
                  ? <img src={p.images[0].imageUrl} alt={p.name} loading="lazy" />
                  : <Package size={48} className="product-image-placeholder" />
                }
                <button className="product-wishlist active" onClick={e => { e.stopPropagation(); toggleWishlist(p.id); }}>
                  <Heart size={14} fill="currentColor" />
                </button>
              </div>
              <div className="product-info">
                <div className="product-category">{p.category}</div>
                <div className="product-name">{p.name}</div>
                <div className="product-brand">{p.brand}</div>
                <div className="product-price">₹{p.price.toLocaleString('en-IN')}</div>
              </div>
              <div className="product-actions">
                <button className="btn btn-danger btn-sm" onClick={() => toggleWishlist(p.id)} id={`remove-wishlist-${p.id}`}>
                  <Trash2 size={13} /> Remove
                </button>
                <button className="btn btn-primary btn-sm" style={{ flex: 1 }} onClick={() => addToCart(p)} disabled={p.status === 'OUT_OF_STOCK'} id={`wishlist-add-cart-${p.id}`}>
                  <ShoppingCart size={13} /> Add to Cart
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
