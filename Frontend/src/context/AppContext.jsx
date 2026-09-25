import { createContext, useContext, useState, useCallback } from 'react';

const AppContext = createContext(null);

export const useApp = () => useContext(AppContext);

// ── Mock Data ─────────────────────────────────────────────────
const MOCK_PRODUCTS = [
  { id:'p1', categoryId:'c1', name:'Apple iPhone 15 Pro', description:'A16 Bionic chip, 48MP camera', sku:'IPH-15-PRO-128', price:134900, currency:'INR', status:'ACTIVE', brand:'Apple', category:'Electronics', rating:4.8, reviews:2341, stock:45, reserved:5, sold:200, images:[{id:'i1',imageUrl:'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=400',isPrimary:true}] },
  { id:'p2', categoryId:'c1', name:'Samsung Galaxy S24 Ultra', description:'200MP camera, AI-powered features', sku:'SAM-S24-ULT-256', price:129999, currency:'INR', status:'ACTIVE', brand:'Samsung', category:'Electronics', rating:4.7, reviews:1876, stock:30, reserved:3, sold:150, images:[{id:'i2',imageUrl:'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=400',isPrimary:true}] },
  { id:'p3', categoryId:'c2', name:'Nike Air Max 2024', description:'Premium cushioning, breathable mesh', sku:'NIKE-AM-2024-BLK', price:12999, currency:'INR', status:'ACTIVE', brand:'Nike', category:'Footwear', rating:4.5, reviews:893, stock:120, reserved:8, sold:430, images:[{id:'i3',imageUrl:'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400',isPrimary:true}] },
  { id:'p4', categoryId:'c3', name:'Sony WH-1000XM5', description:'Industry-leading noise cancellation', sku:'SNY-WH1000XM5', price:29990, currency:'INR', status:'ACTIVE', brand:'Sony', category:'Audio', rating:4.9, reviews:3210, stock:60, reserved:2, sold:380, images:[{id:'i4',imageUrl:'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400',isPrimary:true}] },
  { id:'p5', categoryId:'c4', name:'MacBook Pro 14"', description:'M3 Pro chip, 18GB RAM, 512GB SSD', sku:'MBP-14-M3-512', price:189900, currency:'INR', status:'ACTIVE', brand:'Apple', category:'Laptops', rating:4.9, reviews:1245, stock:20, reserved:4, sold:95, images:[{id:'i5',imageUrl:'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400',isPrimary:true}] },
  { id:'p6', categoryId:'c2', name:'Adidas Ultra Boost 24', description:'Responsive Boost midsole, Primeknit upper', sku:'ADI-UB24-WHT', price:14999, currency:'INR', status:'ACTIVE', brand:'Adidas', category:'Footwear', rating:4.6, reviews:654, stock:5, reserved:1, sold:220, images:[{id:'i6',imageUrl:'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=400',isPrimary:true}] },
  { id:'p7', categoryId:'c5', name:'Levi\'s 511 Slim Fit Jeans', description:'Classic slim fit, premium denim', sku:'LEV-511-32-32', price:4999, currency:'INR', status:'ACTIVE', brand:"Levi's", category:'Apparel', rating:4.4, reviews:2100, stock:200, reserved:10, sold:1200, images:[{id:'i7',imageUrl:'https://images.unsplash.com/photo-1542272604-787c3835535d?w=400',isPrimary:true}] },
  { id:'p8', categoryId:'c1', name:'OnePlus 12', description:'Snapdragon 8 Gen 3, 50W wireless charging', sku:'OP-12-256', price:64999, currency:'INR', status:'OUT_OF_STOCK', brand:'OnePlus', category:'Electronics', rating:4.6, reviews:987, stock:0, reserved:0, sold:320, images:[{id:'i8',imageUrl:'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=400',isPrimary:true}] },
];

const MOCK_ORDERS = [
  { id:'o1', userId:'u1', orderNumber:'SN-2024-001', status:'DELIVERED', totalAmount:134900, currency:'INR', shippingAddress:'123, MG Road, Bangalore, Karnataka 560001', billingAddress:'123, MG Road, Bangalore, Karnataka 560001', createdAt:'2024-01-15T10:30:00', updatedAt:'2024-01-20T14:00:00', items:[{id:'oi1',orderId:'o1',productId:'p1',productName:'Apple iPhone 15 Pro',sku:'IPH-15-PRO-128',quantity:1,unitPrice:134900,totalPrice:134900,createdAt:'2024-01-15T10:30:00'}] },
  { id:'o2', userId:'u1', orderNumber:'SN-2024-002', status:'SHIPPED', totalAmount:42989, currency:'INR', shippingAddress:'123, MG Road, Bangalore, Karnataka 560001', billingAddress:'123, MG Road, Bangalore, Karnataka 560001', createdAt:'2024-01-18T15:00:00', updatedAt:'2024-01-19T09:00:00', items:[{id:'oi2',orderId:'o2',productId:'p3',productName:'Nike Air Max 2024',sku:'NIKE-AM-2024-BLK',quantity:2,unitPrice:12999,totalPrice:25998,createdAt:'2024-01-18T15:00:00'},{id:'oi3',orderId:'o2',productId:'p4',productName:'Sony WH-1000XM5',sku:'SNY-WH1000XM5',quantity:1,unitPrice:16990,totalPrice:16990,createdAt:'2024-01-18T15:00:00'}] },
  { id:'o3', userId:'u1', orderNumber:'SN-2024-003', status:'PENDING', totalAmount:189900, currency:'INR', shippingAddress:'456, Park Street, Mumbai, Maharashtra 400001', billingAddress:'456, Park Street, Mumbai, Maharashtra 400001', createdAt:'2024-01-22T08:00:00', updatedAt:'2024-01-22T08:00:00', items:[{id:'oi4',orderId:'o3',productId:'p5',productName:'MacBook Pro 14"',sku:'MBP-14-M3-512',quantity:1,unitPrice:189900,totalPrice:189900,createdAt:'2024-01-22T08:00:00'}] },
  { id:'o4', userId:'u1', orderNumber:'SN-2024-004', status:'CONFIRMED', totalAmount:5999, currency:'INR', shippingAddress:'789, Anna Salai, Chennai, Tamil Nadu 600002', billingAddress:'789, Anna Salai, Chennai, Tamil Nadu 600002', createdAt:'2024-01-23T12:00:00', updatedAt:'2024-01-23T12:30:00', items:[{id:'oi5',orderId:'o4',productId:'p7',productName:"Levi's 511 Slim Fit Jeans",sku:'LEV-511-32-32',quantity:1,unitPrice:4999,totalPrice:4999,createdAt:'2024-01-23T12:00:00'}] },
];

export function AppProvider({ children }) {
  const [user, setUser]       = useState(null);
  const [cart, setCart]       = useState([]);
  const [products]            = useState(MOCK_PRODUCTS);
  const [orders, setOrders]   = useState(MOCK_ORDERS);
  const [toasts, setToasts]   = useState([]);
  const [wishlist, setWishlist] = useState([]);

  const addToast = useCallback((msg, type = 'success') => {
    const id = Date.now();
    setToasts(t => [...t, { id, msg, type }]);
    setTimeout(() => setToasts(t => t.filter(x => x.id !== id)), 3500);
  }, []);

  const addToCart = useCallback((product, qty = 1) => {
    setCart(prev => {
      const existing = prev.find(i => i.productId === product.id);
      if (existing) {
        addToast(`Updated "${product.name}" quantity`, 'info');
        return prev.map(i => i.productId === product.id ? { ...i, quantity: i.quantity + qty } : i);
      }
      addToast(`"${product.name}" added to cart!`, 'success');
      return [...prev, { productId: product.id, productName: product.name, sku: product.sku, quantity: qty, unitPrice: product.price, image: product.images?.[0]?.imageUrl }];
    });
  }, [addToast]);

  const removeFromCart = useCallback((productId) => {
    setCart(prev => prev.filter(i => i.productId !== productId));
    addToast('Item removed from cart', 'info');
  }, [addToast]);

  const updateCartQty = useCallback((productId, qty) => {
    if (qty <= 0) { removeFromCart(productId); return; }
    setCart(prev => prev.map(i => i.productId === productId ? { ...i, quantity: qty } : i));
  }, [removeFromCart]);

  const toggleWishlist = useCallback((productId) => {
    setWishlist(prev => prev.includes(productId)
      ? prev.filter(id => id !== productId)
      : [...prev, productId]
    );
  }, []);

  const placeOrder = useCallback((orderData) => {
    const newOrder = {
      id: 'o' + Date.now(),
      userId: user?.id,
      orderNumber: 'SN-' + new Date().getFullYear() + '-' + String(orders.length + 5).padStart(3, '0'),
      status: 'PENDING',
      totalAmount: orderData.items.reduce((s, i) => s + i.unitPrice * i.quantity, 0),
      currency: 'INR',
      shippingAddress: orderData.shippingAddress,
      billingAddress: orderData.billingAddress,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      items: orderData.items.map((item, idx) => ({ id: 'oi' + Date.now() + idx, orderId: 'o' + Date.now(), ...item, totalPrice: item.unitPrice * item.quantity, createdAt: new Date().toISOString() })),
    };
    setOrders(prev => [newOrder, ...prev]);
    setCart([]);
    addToast(`Order ${newOrder.orderNumber} placed successfully!`, 'success');
    return newOrder;
  }, [user, orders, addToast]);

  const login = useCallback((userData) => {
    setUser({
      id:        userData.id        || 'u1',
      firstName: userData.firstName || 'User',
      lastName:  userData.lastName  || '',
      email:     userData.email,
      mobile:    userData.mobile    || '',
      role:      userData.role      || 'CUSTOMER',
      status:    userData.status    || 'ACTIVE',
    });
  }, []);

  const logout = useCallback(() => { setUser(null); setCart([]); }, []);

  const cartCount    = cart.reduce((s, i) => s + i.quantity, 0);
  const cartSubtotal = cart.reduce((s, i) => s + i.unitPrice * i.quantity, 0);

  return (
    <AppContext.Provider value={{ user, login, logout, cart, cartCount, cartSubtotal, addToCart, removeFromCart, updateCartQty, products, orders, placeOrder, toasts, addToast, wishlist, toggleWishlist }}>
      {children}
    </AppContext.Provider>
  );
}
