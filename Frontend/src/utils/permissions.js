// ── Permission Matrix ─────────────────────────────────────────
// Frontend controls VISIBILITY. Backend controls AUTHORIZATION.
// ─────────────────────────────────────────────────────────────

export const ROLES = {
  ADMIN: 'ADMIN',
  VENDOR: 'VENDOR',
  CUSTOMER: 'CUSTOMER',
  DELIVERY: 'DELIVERY',
};

export const PERMISSIONS = {
  // Dashboard
  VIEW_DASHBOARD: [ROLES.ADMIN, ROLES.VENDOR, ROLES.CUSTOMER, ROLES.DELIVERY],

  // User Management
  VIEW_USERS: [ROLES.ADMIN],
  MANAGE_USERS: [ROLES.ADMIN],

  // Vendor Management
  VIEW_ALL_VENDORS: [ROLES.ADMIN],
  MANAGE_VENDORS: [ROLES.ADMIN],

  // Product Management
  VIEW_ALL_PRODUCTS: [ROLES.ADMIN, ROLES.VENDOR, ROLES.CUSTOMER, ROLES.DELIVERY],
  ADD_PRODUCT: [ROLES.ADMIN, ROLES.VENDOR],
  EDIT_ANY_PRODUCT: [ROLES.ADMIN],
  EDIT_OWN_PRODUCT: [ROLES.VENDOR],
  APPROVE_PRODUCT: [ROLES.ADMIN],
  DELETE_PRODUCT: [ROLES.ADMIN],

  // Category Management
  MANAGE_CATEGORIES: [ROLES.ADMIN],
  VIEW_CATEGORIES: [ROLES.ADMIN, ROLES.VENDOR, ROLES.CUSTOMER],

  // Order Management
  VIEW_ALL_ORDERS: [ROLES.ADMIN],
  VIEW_OWN_ORDERS: [ROLES.CUSTOMER],
  VIEW_VENDOR_ORDERS: [ROLES.VENDOR],
  VIEW_ASSIGNED_ORDERS: [ROLES.DELIVERY],

  // Payment Management
  VIEW_ALL_PAYMENTS: [ROLES.ADMIN],
  VIEW_OWN_PAYMENTS: [ROLES.CUSTOMER],
  VIEW_VENDOR_PAYMENTS: [ROLES.VENDOR],

  // Inventory Management
  VIEW_ALL_INVENTORY: [ROLES.ADMIN],
  VIEW_OWN_INVENTORY: [ROLES.VENDOR],
  UPDATE_INVENTORY: [ROLES.ADMIN, ROLES.VENDOR],

  // Returns & Refunds
  MANAGE_ALL_RETURNS: [ROLES.ADMIN],
  VIEW_OWN_RETURNS: [ROLES.CUSTOMER],
  VIEW_VENDOR_RETURNS: [ROLES.VENDOR],

  // Reports
  VIEW_ALL_REPORTS: [ROLES.ADMIN],
  VIEW_OWN_REPORTS: [ROLES.VENDOR, ROLES.CUSTOMER, ROLES.DELIVERY],

  // Profile
  VIEW_PROFILE: [ROLES.ADMIN, ROLES.VENDOR, ROLES.CUSTOMER, ROLES.DELIVERY],

  // System Settings
  MANAGE_SETTINGS: [ROLES.ADMIN],

  // Delivery
  VIEW_DELIVERY: [ROLES.DELIVERY],
};

export function hasPermission(userRole, permission) {
  const allowed = PERMISSIONS[permission];
  if (!allowed) return false;
  return allowed.includes(userRole);
}

export function can(userRole, permission) {
  return hasPermission(userRole, permission);
}
