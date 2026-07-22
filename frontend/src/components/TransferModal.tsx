import React, { useState } from 'react';
import { X, ArrowRightLeft } from 'lucide-react';
import { Product, TransferForm, UserRole } from '../types';

interface TransferModalProps {
  product: Product | null;
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (productId: string, form: TransferForm) => Promise<void>;
}

export const TransferModal: React.FC<TransferModalProps> = ({ product, isOpen, onClose, onSubmit }) => {
  const [newOwnerId, setNewOwnerId] = useState('');
  const [newOwnerRole, setNewOwnerRole] = useState<UserRole>('WHOLESALER');
  const [newStatus, setNewStatus] = useState<'IN_TRANSIT' | 'DELIVERED' | 'SOLD'>('IN_TRANSIT');
  const [loading, setLoading] = useState(false);

  if (!isOpen || !product) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newOwnerId.trim()) return;

    setLoading(true);
    try {
      await onSubmit(product.productId, {
        newOwnerId,
        newOwnerRole,
        newStatus
      });
      onClose();
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="glass-card modal-content" onClick={(e) => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <ArrowRightLeft size={20} color="#06b6d4" />
            Transfer Ownership ({product.productId})
          </h3>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer' }}>
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Current Owner</label>
            <input className="form-input" value={`${product.currentOwnerId} (${product.currentOwnerRole})`} disabled readOnly />
          </div>

          <div className="form-group">
            <label className="form-label">New Owner ID / Organization</label>
            <input
              className="form-input"
              placeholder="e.g. WHOLESALER-GLOBAL-LOGISTICS"
              value={newOwnerId}
              onChange={(e) => setNewOwnerId(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">New Owner Role</label>
            <select
              className="form-select"
              value={newOwnerRole}
              onChange={(e) => setNewOwnerRole(e.target.value as UserRole)}
            >
              <option value="WHOLESALER">Wholesaler</option>
              <option value="RETAILER">Retailer</option>
              <option value="CONSUMER">Consumer</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Updated Status</label>
            <select
              className="form-select"
              value={newStatus}
              onChange={(e) => setNewStatus(e.target.value as any)}
            >
              <option value="IN_TRANSIT">In Transit</option>
              <option value="DELIVERED">Delivered</option>
              <option value="SOLD">Sold</option>
            </select>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '2rem' }}>
            <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Recording Transfer...' : 'Endorse & Record Transfer'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
