import React, { useState } from 'react';
import { X, Sparkles } from 'lucide-react';
import { CreateProductForm } from '../types';

interface CreateProductModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (form: CreateProductForm) => Promise<void>;
}

export const CreateProductModal: React.FC<CreateProductModalProps> = ({ isOpen, onClose, onSubmit }) => {
  const [productId, setProductId] = useState(`PROD-${Math.floor(1000 + Math.random() * 9000)}`);
  const [name, setName] = useState('');
  const [batchNumber, setBatchNumber] = useState(`BATCH-2026-${Math.floor(100 + Math.random() * 900)}`);
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleGenerateHash = () => {
    return '0x' + Array.from({ length: 64 }, () => Math.floor(Math.random() * 16).toString(16)).join('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    setLoading(true);
    try {
      await onSubmit({
        productId,
        name,
        batchNumber,
        certificateHash: handleGenerateHash()
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
            <Sparkles size={20} color="#10b981" />
            Create Digital Twin on Ledger
          </h3>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer' }}>
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Product ID (Unique Ledger Key)</label>
            <input className="form-input" value={productId} onChange={(e) => setProductId(e.target.value)} required />
          </div>

          <div className="form-group">
            <label className="form-label">Product Name</label>
            <input
              className="form-input"
              placeholder="e.g. Organic Extra Virgin Olive Oil"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Batch / Lot Number</label>
            <input className="form-input" value={batchNumber} onChange={(e) => setBatchNumber(e.target.value)} required />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '2rem' }}>
            <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Submitting to Ledger...' : 'Commit to Blockchain'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
