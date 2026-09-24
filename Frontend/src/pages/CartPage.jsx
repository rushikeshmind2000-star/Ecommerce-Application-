import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShoppingCart, Trash2, ArrowLeft, ArrowRight, Package, Tag, MapPin } from 'lucide-react';
import { useApp } from '../context/AppContext';

const fmt = (n) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

export default function CartPage() {
  const { cart, cartSubtotal, removeFromCart, updateCartQty, placeOrder, user } = useApp();
  const navigate = useNavigate();
  const [coupon, setCoupon]     = useState('');
  const [couponMsg, setCouponMsg] = useState('');
  const [discount, setDiscount] = useState(0);
  const [step, setStep]         = useState('cart'); // cart | checkout

  // CreateOrderRequest fields
  const [shipping, setShipping] = useState({
    shippingAddress: user?.shippingAddress || '',
    billingAddress:  user?.billingAddress  || '',
    sameAsBilling:   true,
  });
  const [errors, setErrors] = useState({});

  const applyCoupon = () => {
    if (coupon.toUpperCase() === 'SAVE10') {
      setDiscount(Math.round(cartSubtotal * 0.1));
      setCouponMsg('Coupon SAVE10 applied! 10% off.');
    } else {
      setDiscount(0);
      setCouponMsg('Invalid coupon code.');
    }
  };

  const shipping5 = cartSubtotal > 49900 ? 0 : 4900;
  const tax       = Math.round((cartSubtotal - discount) * 0.18);
  const total     = cartSubtotal - discount + shipping5 + tax;

  const validate = () => {
    const e = {};
    if (!shipping.shippingAddress.trim()) e.shippingAddress = 'Shipping address is required';
    if (!shipping.billingAddress.trim())  e.billingAddress  = 'Billing address is required';
    return e;
  };

  const handlePlaceOrder = () => {
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    const order = placeOrder({
      shippingAddress: shipping.shippingAddress,
      billingAddress:  shipping.billingAddress,
      currency: 'INR',
      items: cart.map(i => ({ productId: i.productId, productName: i.productName, sku: i.sku, quantity: i.quantity, unitPrice: i.unitPrice })),
    });
    navigate(`/orders/${order.id}`);
  };

  if (cart.length === 0) return (
    <div className="page-container">
      <div className="empty-state" style={{ minHeight: 400 }}>
        <div className="empty-state-icon"><ShoppingCart size={32} /></div>
        <div className="empty-state-title">Your cart is empty</div>
        <div className="empty-state-text">Add some amazing products to get started!</div>
        <button className="btn btn-primary" onClick={() => navigate('/products')}><Package size={16} /> Browse Products</button>
      </div>
    </div>
  );

  return (
    <div className="page-container">
      <div className="page-header">
        <div className="page-header-left">
          <h1 className="page-title">{step === 'cart' ? 'Shopping Cart' : 'Checkout'}</h1>
          <p className="page-subtitle">{cart.length} item{cart.length > 1 ? 's' : ''} in your cart</p>
        </div>
        <button className="btn btn-secondary" onClick={() => step === 'cart' ? navigate('/products') : setStep('cart')}>
          <ArrowLeft size={16} /> {step === 'cart' ? 'Continue Shopping' : 'Back to Cart'}
        </button>
      </div>

      {/* Progress */}
      <div style={{ display: 'flex', gap: 0, marginBottom: 'var(--space-8)' }}>
        {['Cart', 'Checkout', 'Confirmation'].map((s, i) => (
          <div key={s} style={{ flex: 1, display: 'flex', alignItems: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
              <div style={{ width: 28, height: 28, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 'var(--font-size-xs)', background: (step === 'cart' ? i === 0 : i <= 1) ? 'var(--primary)' : 'var(--gray-200)', color: (step === 'cart' ? i === 0 : i <= 1) ? 'var(--white)' : 'var(--gray-400)' }}>
                {i + 1}
              </div>
              <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: (step === 'cart' ? i === 0 : i <= 1) ? 'var(--primary)' : 'var(--gray-400)' }}>{s}</span>
            </div>
            {i < 2 && <div style={{ flex: 1, height: 2, background: 'var(--gray-200)', margin: '0 var(--space-4)' }} />}
          </div>
        ))}
      </div>

      <div className="cart-layout">
        {/* Items */}
        <div>
          {step === 'cart' ? (
            <div className="card">
              <div className="card-header">
                <span className="card-title">Order Items</span>
              </div>
              {cart.map(item => (
                <div key={item.productId} className="cart-item">
                  <div className="cart-item-image">
                    {item.image ? <img src={item.image} alt={item.productName} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      : <Package size={24} color="var(--gray-400)" />}
                  </div>
                  <div className="cart-item-info">
                    <div className="cart-item-name">{item.productName}</div>
                    <div className="cart-item-sku">SKU: {item.sku}</div>
                    <div className="cart-item-actions">
                      <div className="qty-control">
                        <button className="qty-btn" onClick={() => updateCartQty(item.productId, item.quantity - 1)}>−</button>
                        <span className="qty-val">{item.quantity}</span>
                        <button className="qty-btn" onClick={() => updateCartQty(item.productId, item.quantity + 1)}>+</button>
                      </div>
                      <button className="btn btn-danger btn-sm" onClick={() => removeFromCart(item.productId)}>
                        <Trash2 size={13} /> Remove
                      </button>
                    </div>
                  </div>
                  <div className="cart-item-price">
                    <div className="cart-item-total">{fmt(item.unitPrice * item.quantity)}</div>
                    <div className="cart-item-unit">{fmt(item.unitPrice)} each</div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            /* Checkout Form — matches CreateOrderRequest DTO */
            <div className="card">
              <div className="card-header">
                <span className="card-title">Delivery Details</span>
                <MapPin size={20} color="var(--primary)" />
              </div>
              <div className="card-body">
                <div className="form-group">
                  <label className="form-label" htmlFor="shipping-address">Shipping Address <span className="required">*</span></label>
                  <textarea id="shipping-address" className={`form-textarea ${errors.shippingAddress ? 'error' : ''}`}
                    placeholder="House/Flat No., Street, City, State, PIN"
                    value={shipping.shippingAddress}
                    onChange={e => { setShipping(s => ({ ...s, shippingAddress: e.target.value, billingAddress: s.sameAsBilling ? e.target.value : s.billingAddress })); setErrors(er => ({ ...er, shippingAddress: '' })); }}
                    rows={3} />
                  {errors.shippingAddress && <span className="form-error">{errors.shippingAddress}</span>}
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', marginBottom: 'var(--space-5)' }}>
                  <input id="same-billing" type="checkbox" checked={shipping.sameAsBilling} style={{ width: 16, height: 16, accentColor: 'var(--primary)', cursor: 'pointer' }}
                    onChange={e => setShipping(s => ({ ...s, sameAsBilling: e.target.checked, billingAddress: e.target.checked ? s.shippingAddress : '' }))} />
                  <label htmlFor="same-billing" style={{ fontSize: 'var(--font-size-sm)', color: 'var(--gray-600)', cursor: 'pointer', fontWeight: 500 }}>
                    Billing address same as shipping
                  </label>
                </div>

                {!shipping.sameAsBilling && (
                  <div className="form-group">
                    <label className="form-label" htmlFor="billing-address">Billing Address <span className="required">*</span></label>
                    <textarea id="billing-address" className={`form-textarea ${errors.billingAddress ? 'error' : ''}`}
                      placeholder="House/Flat No., Street, City, State, PIN"
                      value={shipping.billingAddress}
                      onChange={e => { setShipping(s => ({ ...s, billingAddress: e.target.value })); setErrors(er => ({ ...er, billingAddress: '' })); }}
                      rows={3} />
                    {errors.billingAddress && <span className="form-error">{errors.billingAddress}</span>}
                  </div>
                )}

                {/* Payment Method */}
                <div className="form-group">
                  <label className="form-label">Payment Method</label>
                  {['UPI', 'Credit / Debit Card', 'Net Banking', 'Cash on Delivery'].map(pm => (
                    <label key={pm} style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', padding: 'var(--space-3) var(--space-4)', borderRadius: 'var(--radius-md)', border: '1.5px solid var(--gray-200)', marginBottom: 'var(--space-2)', cursor: 'pointer', transition: 'border-color var(--transition)' }}>
                      <input type="radio" name="payment" value={pm} defaultChecked={pm === 'UPI'} style={{ accentColor: 'var(--primary)' }} />
                      <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: 500, color: 'var(--gray-700)' }}>{pm}</span>
                    </label>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Order Summary */}
        <div className="order-summary">
          <div className="card">
            <div className="card-header"><span className="card-title">Order Summary</span></div>
            <div className="card-body">
              <div className="summary-row"><span className="text-gray">Subtotal ({cart.reduce((s,i)=>s+i.quantity,0)} items)</span><span className="font-bold">{fmt(cartSubtotal)}</span></div>
              {discount > 0 && <div className="summary-row"><span style={{ color: 'var(--success)' }}>Discount (SAVE10)</span><span style={{ color: 'var(--success)', fontWeight: 700 }}>−{fmt(discount)}</span></div>}
              <div className="summary-row"><span className="text-gray">Shipping</span><span className="font-bold" style={{ color: shipping5 === 0 ? 'var(--success)' : '' }}>{shipping5 === 0 ? 'FREE' : fmt(shipping5)}</span></div>
              <div className="summary-row"><span className="text-gray">GST (18%)</span><span className="font-bold">{fmt(tax)}</span></div>
              <div className="divider" />
              <div className="summary-row total"><span>Total</span><span style={{ color: 'var(--primary)' }}>{fmt(total)}</span></div>

              {/* Coupon */}
              {step === 'cart' && (
                <div style={{ marginTop: 'var(--space-5)' }}>
                  <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
                    <div className="input-wrapper" style={{ flex: 1 }}>
                      <Tag size={14} className="input-icon-left" />
                      <input id="coupon-input" type="text" className="form-input" placeholder="Coupon code" value={coupon} onChange={e => setCoupon(e.target.value)} />
                    </div>
                    <button className="btn btn-secondary btn-sm" onClick={applyCoupon} id="apply-coupon">Apply</button>
                  </div>
                  {couponMsg && <div style={{ fontSize: 'var(--font-size-xs)', marginTop: 'var(--space-2)', color: discount > 0 ? 'var(--success)' : 'var(--danger)' }}>{couponMsg}</div>}
                  <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--gray-400)', marginTop: 'var(--space-1)' }}>Try: SAVE10</div>
                </div>
              )}
            </div>
            <div className="card-footer">
              {step === 'cart' ? (
                <button id="proceed-checkout" className="btn btn-primary btn-full btn-lg" onClick={() => setStep('checkout')}>
                  Proceed to Checkout <ArrowRight size={16} />
                </button>
              ) : (
                <button id="place-order-btn" className="btn btn-accent btn-full btn-lg" onClick={handlePlaceOrder}>
                  Place Order — {fmt(total)} <ArrowRight size={16} />
                </button>
              )}
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--gray-400)', textAlign: 'center', marginTop: 'var(--space-3)' }}>
                🔒 Secured by 256-bit SSL encryption
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
