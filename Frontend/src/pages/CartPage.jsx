import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShoppingCart, Trash2, ArrowLeft, ArrowRight, Package, Tag, MapPin, Plus, Minus, Heart, Zap } from 'lucide-react';
import { useApp } from '../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const COUPONS = { SAVE10: 0.10, FLAT500: 500, WELCOME20: 0.20 };

export default function CartPage() {
  const { cart, cartSubtotal, removeFromCart, updateCartQty, placeOrder, user, addToast } = useApp();
  const navigate = useNavigate();
  const [coupon, setCoupon]       = useState('');
  const [couponMsg, setCouponMsg] = useState('');
  const [discountVal, setDiscountVal] = useState(0);
  const [step, setStep]           = useState('cart');
  const [removingId, setRemovingId] = useState(null);

  const [shipping, setShipping] = useState({
    shippingAddress: '',
    billingAddress: '',
    sameAsBilling: true,
  });
  const [errors, setErrors] = useState({});
  const [placing, setPlacing] = useState(false);
  const [placed, setPlaced]   = useState(false);

  const SHIPPING_FEE  = cartSubtotal >= 999 ? 0 : 99;
  const total = cartSubtotal - discountVal + SHIPPING_FEE;

  const applyCoupon = () => {
    const code = coupon.trim().toUpperCase();
    if (COUPONS[code] !== undefined) {
      const disc = COUPONS[code] < 1 ? Math.round(cartSubtotal * COUPONS[code]) : COUPONS[code];
      setDiscountVal(disc);
      setCouponMsg(`✓ Coupon "${code}" applied — you save ${fmt(disc)}!`);
    } else {
      setDiscountVal(0);
      setCouponMsg('✗ Invalid coupon code. Try: SAVE10, FLAT500 or WELCOME20');
    }
  };

  const handleRemove = (id) => {
    setRemovingId(id);
    setTimeout(() => { removeFromCart(id); setRemovingId(null); }, 350);
  };

  const validateCheckout = () => {
    const e = {};
    if (!shipping.shippingAddress.trim()) e.shippingAddress = 'Shipping address is required';
    if (!shipping.sameAsBilling && !shipping.billingAddress.trim()) e.billingAddress = 'Billing address is required';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handlePlaceOrder = () => {
    if (!validateCheckout()) return;
    setPlacing(true);
    setTimeout(() => {
      placeOrder({
        items: cart.map(i => ({ productId: i.productId, productName: i.productName, sku: i.sku, quantity: i.quantity, unitPrice: i.unitPrice })),
        shippingAddress: shipping.shippingAddress,
        billingAddress: shipping.sameAsBilling ? shipping.shippingAddress : shipping.billingAddress,
      });
      setPlacing(false);
      setPlaced(true);
    }, 1200);
  };

  // ── Order Placed Screen ──────────────────────────
  if (placed) {
    return (
      <div className="page-container" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh', textAlign: 'center' }}>
        <div style={{ fontSize: 80, marginBottom: 'var(--space-4)', animation: 'heartBeat 0.6s ease' }}>🎉</div>
        <h1 style={{ fontSize: 'var(--font-size-3xl)', fontWeight: 800, color: 'var(--gray-900)', marginBottom: 'var(--space-3)' }}>Order Placed!</h1>
        <p style={{ fontSize: 'var(--font-size-base)', color: 'var(--gray-500)', marginBottom: 'var(--space-8)', maxWidth: 360 }}>
          Your order has been placed successfully. You'll get an email confirmation shortly.
        </p>
        <div style={{ display: 'flex', gap: 'var(--space-4)' }}>
          <button className="btn btn-secondary" onClick={() => navigate('/customer/products')}>Continue Shopping</button>
          <button className="btn btn-primary" onClick={() => navigate('/customer/orders')}>Track My Order <ArrowRight size={15} /></button>
        </div>
      </div>
    );
  }

  // ── Empty Cart ───────────────────────────────────
  if (cart.length === 0) {
    return (
      <div className="page-container">
        <div className="empty-state" style={{ minHeight: '50vh', justifyContent: 'center' }}>
          <div className="empty-state-icon" style={{ fontSize: 64 }}>🛒</div>
          <div className="empty-state-title">Your cart is empty</div>
          <div className="empty-state-text">Discover amazing products and add them to your cart</div>
          <button className="btn btn-primary" onClick={() => navigate('/customer/products')}>
            <Package size={16} /> Browse Products
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      {/* Steps */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', marginBottom: 'var(--space-6)' }}>
        {['cart', 'checkout'].map((s, i) => (
          <div key={s} style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', cursor: step === 'checkout' && s === 'cart' ? 'pointer' : 'default' }}
              onClick={() => step === 'checkout' && s === 'cart' && setStep('cart')}>
              <div style={{ width: 28, height: 28, borderRadius: '50%', background: step === s || (s === 'cart' && step === 'checkout') ? 'var(--primary)' : 'var(--gray-200)', color: step === s || (s === 'cart') ? 'white' : 'var(--gray-500)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12, fontWeight: 700, transition: 'all 0.3s' }}>
                {i + 1}
              </div>
              <span style={{ fontWeight: step === s ? 700 : 400, color: step === s ? 'var(--gray-900)' : 'var(--gray-400)', fontSize: 'var(--font-size-sm)', textTransform: 'capitalize', transition: 'all 0.3s' }}>
                {s === 'cart' ? 'Cart' : 'Checkout'}
              </span>
            </div>
            {i < 1 && <div style={{ flex: 1, height: 2, background: step === 'checkout' ? 'var(--primary)' : 'var(--gray-200)', transition: 'background 0.5s', width: 40 }} />}
          </div>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: 'var(--space-6)', alignItems: 'start' }}>
        {/* LEFT — Cart Items / Checkout Form */}
        <div>
          {step === 'cart' ? (
            <div className="card">
              <div className="card-header">
                <span className="card-title">
                  <ShoppingCart size={16} style={{ display: 'inline', marginRight: 6 }} />
                  Cart ({cart.length} item{cart.length !== 1 ? 's' : ''})
                </span>
                <button className="btn btn-secondary btn-sm" onClick={() => navigate('/customer/products')}>
                  <ArrowLeft size={13} /> Continue Shopping
                </button>
              </div>

              <div style={{ padding: 'var(--space-2)' }}>
                {cart.map((item, idx) => (
                  <div key={item.productId} style={{
                    display: 'flex', alignItems: 'center', gap: 'var(--space-4)',
                    padding: 'var(--space-4) var(--space-3)',
                    borderBottom: idx < cart.length - 1 ? '1px solid var(--gray-100)' : 'none',
                    transition: 'all 0.35s ease',
                    opacity: removingId === item.productId ? 0 : 1,
                    transform: removingId === item.productId ? 'translateX(30px)' : 'none',
                    animation: 'fadeSlideIn 0.3s ease both',
                    animationDelay: `${idx * 0.06}s`,
                  }}>
                    {/* Image */}
                    <div style={{ width: 80, height: 80, borderRadius: 'var(--radius-lg)', overflow: 'hidden', flexShrink: 0, background: 'var(--gray-100)' }}>
                      <img src={item.image} alt={item.productName} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    </div>

                    {/* Info */}
                    <div style={{ flex: 1 }}>
                      <div style={{ fontWeight: 700, fontSize: 'var(--font-size-sm)', color: 'var(--gray-900)', marginBottom: 3 }}>{item.productName}</div>
                      <div style={{ fontSize: 11, color: 'var(--gray-400)', marginBottom: 8 }}>SKU: {item.sku}</div>
                      {/* Qty Stepper */}
                      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                        <button onClick={() => updateCartQty(item.productId, item.quantity - 1)}
                          style={{ width: 28, height: 28, borderRadius: '50%', border: '1.5px solid var(--gray-200)', background: 'var(--white)', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', transition: 'all 0.2s', color: 'var(--gray-600)' }}
                          onMouseEnter={e => { e.currentTarget.style.background = 'var(--primary)'; e.currentTarget.style.color = 'white'; e.currentTarget.style.borderColor = 'var(--primary)'; }}
                          onMouseLeave={e => { e.currentTarget.style.background = 'var(--white)'; e.currentTarget.style.color = 'var(--gray-600)'; e.currentTarget.style.borderColor = 'var(--gray-200)'; }}>
                          <Minus size={11} />
                        </button>
                        <span style={{ minWidth: 28, textAlign: 'center', fontWeight: 700, fontSize: 'var(--font-size-sm)', animation: 'countUp 0.2s ease' }}>{item.quantity}</span>
                        <button onClick={() => updateCartQty(item.productId, item.quantity + 1)}
                          style={{ width: 28, height: 28, borderRadius: '50%', border: '1.5px solid var(--gray-200)', background: 'var(--white)', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', transition: 'all 0.2s', color: 'var(--gray-600)' }}
                          onMouseEnter={e => { e.currentTarget.style.background = 'var(--primary)'; e.currentTarget.style.color = 'white'; e.currentTarget.style.borderColor = 'var(--primary)'; }}
                          onMouseLeave={e => { e.currentTarget.style.background = 'var(--white)'; e.currentTarget.style.color = 'var(--gray-600)'; e.currentTarget.style.borderColor = 'var(--gray-200)'; }}>
                          <Plus size={11} />
                        </button>
                      </div>
                    </div>

                    {/* Price + Remove */}
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontWeight: 800, color: 'var(--primary)', fontSize: 'var(--font-size-base)', marginBottom: 6 }}>
                        {fmt(item.unitPrice * item.quantity)}
                      </div>
                      <div style={{ fontSize: 11, color: 'var(--gray-400)', marginBottom: 8 }}>
                        {fmt(item.unitPrice)} × {item.quantity}
                      </div>
                      <button onClick={() => handleRemove(item.productId)}
                        style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 11, color: 'var(--danger)', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 600, transition: 'opacity 0.2s' }}
                        onMouseEnter={e => e.currentTarget.style.opacity = '0.7'}
                        onMouseLeave={e => e.currentTarget.style.opacity = '1'}>
                        <Trash2 size={12} /> Remove
                      </button>
                    </div>
                  </div>
                ))}
              </div>

              {/* Coupon */}
              <div style={{ padding: 'var(--space-4) var(--space-4)', borderTop: '1px solid var(--gray-100)', display: 'flex', gap: 'var(--space-3)', alignItems: 'flex-start', flexWrap: 'wrap' }}>
                <div className="input-wrapper" style={{ flex: 1, minWidth: 200 }}>
                  <Tag size={14} className="input-icon-left" />
                  <input id="coupon-input" type="text" className="form-input" placeholder="Coupon code (SAVE10, FLAT500…)"
                    value={coupon} onChange={e => setCoupon(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && applyCoupon()} />
                </div>
                <button className="btn btn-secondary" onClick={applyCoupon} id="apply-coupon-btn">Apply</button>
                {couponMsg && (
                  <div style={{ width: '100%', fontSize: 12, color: couponMsg.startsWith('✓') ? 'var(--success)' : 'var(--danger)', fontWeight: 500, animation: 'fadeSlideIn 0.3s ease' }}>
                    {couponMsg}
                  </div>
                )}
              </div>
            </div>
          ) : (
            /* Checkout form */
            <div className="card" style={{ animation: 'fadeSlideIn 0.4s ease' }}>
              <div className="card-header">
                <span className="card-title"><MapPin size={16} style={{ display: 'inline', marginRight: 6 }} />Shipping Details</span>
                <button className="btn btn-secondary btn-sm" onClick={() => setStep('cart')}>
                  <ArrowLeft size={13} /> Back to Cart
                </button>
              </div>
              <div className="card-body" style={{ display: 'grid', gap: 'var(--space-4)' }}>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label className="form-label">Full Delivery Address <span className="required">*</span></label>
                  <textarea id="shipping-address" className={`form-input ${errors.shippingAddress ? 'error' : ''}`}
                    style={{ minHeight: 80, resize: 'vertical' }}
                    placeholder="House no., Street, Area, City, State, PIN code"
                    value={shipping.shippingAddress}
                    onChange={e => setShipping(s => ({ ...s, shippingAddress: e.target.value }))} />
                  {errors.shippingAddress && <span className="form-error">{errors.shippingAddress}</span>}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                  <input type="checkbox" id="same-billing" checked={shipping.sameAsBilling}
                    onChange={e => setShipping(s => ({ ...s, sameAsBilling: e.target.checked }))} />
                  <label htmlFor="same-billing" style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-600)', cursor: 'pointer' }}>
                    Billing address same as shipping
                  </label>
                </div>
                {!shipping.sameAsBilling && (
                  <div className="form-group" style={{ marginBottom: 0, animation: 'slideDown 0.3s ease' }}>
                    <label className="form-label">Billing Address <span className="required">*</span></label>
                    <textarea id="billing-address" className={`form-input ${errors.billingAddress ? 'error' : ''}`}
                      style={{ minHeight: 80 }}
                      value={shipping.billingAddress}
                      onChange={e => setShipping(s => ({ ...s, billingAddress: e.target.value }))} />
                    {errors.billingAddress && <span className="form-error">{errors.billingAddress}</span>}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* RIGHT — Order Summary */}
        <div style={{ position: 'sticky', top: 80 }}>
          <div className="card">
            <div className="card-header"><span className="card-title">Order Summary</span></div>
            <div className="card-body" style={{ display: 'grid', gap: 'var(--space-3)' }}>
              {/* Line items */}
              {cart.map(item => (
                <div key={item.productId} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 'var(--font-size-sm)', color: 'var(--gray-600)' }}>
                  <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '65%' }}>{item.productName} × {item.quantity}</span>
                  <span style={{ fontWeight: 600 }}>{fmt(item.unitPrice * item.quantity)}</span>
                </div>
              ))}
              <div style={{ borderTop: '1px solid var(--gray-100)', paddingTop: 'var(--space-3)', display: 'grid', gap: 'var(--space-2)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 'var(--font-size-sm)', color: 'var(--gray-600)' }}>
                  <span>Subtotal</span><span>{fmt(cartSubtotal)}</span>
                </div>
                {discountVal > 0 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 'var(--font-size-sm)', color: 'var(--success)', animation: 'fadeSlideIn 0.3s ease' }}>
                    <span>Coupon Discount</span><span>-{fmt(discountVal)}</span>
                  </div>
                )}
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 'var(--font-size-sm)', color: 'var(--gray-600)' }}>
                  <span>Shipping</span>
                  <span style={{ color: SHIPPING_FEE === 0 ? 'var(--success)' : 'var(--gray-700)' }}>
                    {SHIPPING_FEE === 0 ? '✓ FREE' : fmt(SHIPPING_FEE)}
                  </span>
                </div>
              </div>
              <div style={{ borderTop: '2px solid var(--gray-200)', paddingTop: 'var(--space-3)', display: 'flex', justifyContent: 'space-between', fontWeight: 800, fontSize: 'var(--font-size-lg)', color: 'var(--gray-900)' }}>
                <span>Total</span>
                <span style={{ color: 'var(--primary)', animation: 'countUp 0.3s ease' }}>{fmt(total)}</span>
              </div>
              {SHIPPING_FEE > 0 && (
                <div style={{ fontSize: 11, color: 'var(--success)', textAlign: 'center', background: 'var(--success-light)', padding: '6px 12px', borderRadius: 'var(--radius-md)' }}>
                  Add {fmt(999 - cartSubtotal)} more for FREE delivery! 🚚
                </div>
              )}
            </div>
            <div style={{ padding: 'var(--space-4)' }}>
              {step === 'cart' ? (
                <button className="btn btn-primary btn-full btn-lg" onClick={() => setStep('checkout')} id="proceed-checkout-btn">
                  Proceed to Checkout <ArrowRight size={16} />
                </button>
              ) : (
                <button className="btn btn-primary btn-full btn-lg" onClick={handlePlaceOrder} disabled={placing} id="place-order-btn">
                  {placing ? <><span className="spinner" />Placing Order…</> : <><Zap size={16} /> Place Order — {fmt(total)}</>}
                </button>
              )}
            </div>
            <div style={{ textAlign: 'center', padding: '0 var(--space-4) var(--space-4)', fontSize: 11, color: 'var(--gray-400)', display: 'flex', alignItems: 'center', gap: 4, justifyContent: 'center' }}>
              🔒 Secure checkout · 100% safe payment
            </div>
          </div>

          {/* Trust badges */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 'var(--space-2)', marginTop: 'var(--space-4)' }}>
            {[['🔄', 'Easy Returns'], ['🚚', 'Fast Delivery'], ['🛡️', 'Buyer Protection']].map(([icon, label]) => (
              <div key={label} style={{ textAlign: 'center', padding: 'var(--space-3)', background: 'var(--white)', borderRadius: 'var(--radius-lg)', border: '1px solid var(--gray-100)' }}>
                <div style={{ fontSize: 20, marginBottom: 3 }}>{icon}</div>
                <div style={{ fontSize: 9, fontWeight: 600, color: 'var(--gray-500)' }}>{label}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
