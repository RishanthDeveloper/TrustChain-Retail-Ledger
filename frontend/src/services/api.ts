import { Product, TransferRecord, CreateProductForm, TransferForm, UserRole } from '../types';

const API_BASE = '/api';

// Fallback in-memory state for client standalone dev mode
const mockProducts: Map<string, Product> = new Map([
  [
    'PROD-1001',
    {
      productId: 'PROD-1001',
      name: 'Organic Extra Virgin Olive Oil 500ml',
      batchNumber: 'BATCH-2026-089',
      originManufacturerId: 'MANUFACTURER-APEX',
      currentOwnerId: 'RETAILER-HEALTHYMARKET',
      currentOwnerRole: 'RETAILER',
      status: 'DELIVERED',
      createdAt: Math.floor(Date.now() / 1000) - 86400 * 3,
      updatedAt: Math.floor(Date.now() / 1000) - 14400,
      certificateHash: '0xa4f8b92c1e7d3489fe00112233445566778899aabbccddeeff00112233445566'
    }
  ],
  [
    'PROD-1002',
    {
      productId: 'PROD-1002',
      name: 'Artisan Cold Brew Coffee Beans 1kg',
      batchNumber: 'BATCH-2026-092',
      originManufacturerId: 'MANUFACTURER-ROASTERS',
      currentOwnerId: 'MANUFACTURER-ROASTERS',
      currentOwnerRole: 'MANUFACTURER',
      status: 'CREATED',
      createdAt: Math.floor(Date.now() / 1000) - 3600 * 5,
      updatedAt: Math.floor(Date.now() / 1000) - 3600 * 5,
      certificateHash: '0x99887766554433221100ffee00112233445566778899aabbccddeeff00112233'
    }
  ]
]);

const mockHistories: Map<string, TransferRecord[]> = new Map([
  [
    'PROD-1001',
    [
      {
        txId: '0x8f31a20b7c9e4d5f1a3b5c7d9e0f2a4b6c8d0e1f2a3b4c5d6e7f8a9b0c1d2e3f',
        fromOwnerId: 'MANUFACTURER-APEX',
        toOwnerId: 'MANUFACTURER-APEX',
        toOwnerRole: 'MANUFACTURER',
        status: 'CREATED',
        timestamp: Math.floor(Date.now() / 1000) - 86400 * 3
      },
      {
        txId: '0x1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b',
        fromOwnerId: 'MANUFACTURER-APEX',
        toOwnerId: 'WHOLESALER-GLOBAL',
        toOwnerRole: 'WHOLESALER',
        status: 'IN_TRANSIT',
        timestamp: Math.floor(Date.now() / 1000) - 86400 * 2
      },
      {
        txId: '0x9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f',
        fromOwnerId: 'WHOLESALER-GLOBAL',
        toOwnerId: 'RETAILER-HEALTHYMARKET',
        toOwnerRole: 'RETAILER',
        status: 'DELIVERED',
        timestamp: Math.floor(Date.now() / 1000) - 14400
      }
    ]
  ],
  [
    'PROD-1002',
    [
      {
        txId: '0x3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d',
        fromOwnerId: 'MANUFACTURER-ROASTERS',
        toOwnerId: 'MANUFACTURER-ROASTERS',
        toOwnerRole: 'MANUFACTURER',
        status: 'CREATED',
        timestamp: Math.floor(Date.now() / 1000) - 3600 * 5
      }
    ]
  ]
]);

export async function fetchProducts(): Promise<Product[]> {
  try {
    const res = await fetch(`${API_BASE}/products`);
    if (!res.ok) throw new Error('API request failed');
    return await res.json();
  } catch (err) {
    console.warn('Falling back to local state:', err);
    return Array.from(mockProducts.values());
  }
}

export async function fetchProduct(id: string): Promise<Product> {
  try {
    const res = await fetch(`${API_BASE}/products/${id}`);
    if (!res.ok) throw new Error('API request failed');
    return await res.json();
  } catch (err) {
    const p = mockProducts.get(id);
    if (!p) throw new Error(`Product ${id} not found`);
    return p;
  }
}

export async function fetchProductHistory(id: string): Promise<TransferRecord[]> {
  try {
    const res = await fetch(`${API_BASE}/products/${id}/history`);
    if (!res.ok) throw new Error('API request failed');
    return await res.json();
  } catch (err) {
    return mockHistories.get(id) || [];
  }
}

export async function createProduct(form: CreateProductForm, currentRole: UserRole): Promise<Product> {
  const payload = {
    productId: form.productId,
    name: form.name,
    batchNumber: form.batchNumber,
    manufacturerId: `${currentRole}-APEX`,
    certificateHash: form.certificateHash
  };

  try {
    const res = await fetch(`${API_BASE}/products`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) throw new Error('Failed to create product');
    return await res.json();
  } catch (err) {
    const now = Math.floor(Date.now() / 1000);
    const newProd: Product = {
      productId: form.productId,
      name: form.name,
      batchNumber: form.batchNumber,
      originManufacturerId: `${currentRole}-APEX`,
      currentOwnerId: `${currentRole}-APEX`,
      currentOwnerRole: currentRole,
      status: 'CREATED',
      createdAt: now,
      updatedAt: now,
      certificateHash: form.certificateHash
    };
    mockProducts.set(form.productId, newProd);

    mockHistories.set(form.productId, [
      {
        txId: '0x' + Array.from({ length: 64 }, () => Math.floor(Math.random() * 16).toString(16)).join(''),
        fromOwnerId: `${currentRole}-APEX`,
        toOwnerId: `${currentRole}-APEX`,
        toOwnerRole: currentRole,
        status: 'CREATED',
        timestamp: now
      }
    ]);

    return newProd;
  }
}

export async function transferProduct(
  productId: string,
  form: TransferForm,
  callerId: string
): Promise<Product> {
  const payload = {
    callerId,
    newOwnerId: form.newOwnerId,
    newOwnerRole: form.newOwnerRole,
    newStatus: form.newStatus
  };

  try {
    const res = await fetch(`${API_BASE}/products/${productId}/transfer`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) throw new Error('Transfer failed');
    return await res.json();
  } catch (err) {
    const prod = mockProducts.get(productId);
    if (!prod) throw new Error(`Product ${productId} not found`);

    const now = Math.floor(Date.now() / 1000);
    const prevOwner = prod.currentOwnerId;

    prod.currentOwnerId = form.newOwnerId;
    prod.currentOwnerRole = form.newOwnerRole;
    prod.status = form.newStatus;
    prod.updatedAt = now;

    const history = mockHistories.get(productId) || [];
    history.push({
      txId: '0x' + Array.from({ length: 64 }, () => Math.floor(Math.random() * 16).toString(16)).join(''),
      fromOwnerId: prevOwner,
      toOwnerId: form.newOwnerId,
      toOwnerRole: form.newOwnerRole,
      status: form.newStatus,
      timestamp: now
    });
    mockHistories.set(productId, history);

    return prod;
  }
}
