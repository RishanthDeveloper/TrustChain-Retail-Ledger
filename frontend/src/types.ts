export type UserRole = 'MANUFACTURER' | 'WHOLESALER' | 'RETAILER' | 'CONSUMER';

export interface Product {
  productId: string;
  name: string;
  batchNumber: string;
  originManufacturerId: string;
  currentOwnerId: string;
  currentOwnerRole: UserRole;
  status: 'CREATED' | 'IN_TRANSIT' | 'DELIVERED' | 'SOLD';
  createdAt: number;
  updatedAt: number;
  certificateHash: string;
}

export interface TransferRecord {
  txId: string;
  fromOwnerId: string | null;
  toOwnerId: string;
  toOwnerRole: UserRole;
  status: string;
  timestamp: number;
}

export interface CreateProductForm {
  productId: string;
  name: string;
  batchNumber: string;
  certificateHash: string;
}

export interface TransferForm {
  newOwnerId: string;
  newOwnerRole: UserRole;
  newStatus: 'IN_TRANSIT' | 'DELIVERED' | 'SOLD';
}
