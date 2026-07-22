import React from 'react';
import { X, QrCode, Download } from 'lucide-react';
import { Product } from '../types';

interface QRCodeModalProps {
  product: Product | null;
  isOpen: boolean;
  onClose: () => void;
}

export const QRCodeModal: React.FC<QRCodeModalProps> = ({ product, isOpen, onClose }) => {
  if (!isOpen || !product) return null;

  const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(
    window.location.origin + '?product=' + product.productId
  )}`;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="glass-card modal-content" style={{ textAlign: 'center', maxWidth: '420px' }} onClick={(e) => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <QrCode size={20} color="#10b981" />
            Consumer Verification QR Code
          </h3>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer' }}>
            <X size={20} />
          </button>
        </div>

        <div style={{ fontSize: '0.9rem', color: '#94a3b8', marginBottom: '1rem' }}>
          Scan to verify authentic provenance & origin details for <strong>{product.name}</strong>
        </div>

        <div className="qr-preview-box">
          <img src={qrUrl} alt="Product QR Code" style={{ width: '180px', height: '180px', borderRadius: '8px' }} />
        </div>

        <div style={{ fontSize: '0.8rem', fontFamily: 'JetBrains Mono', color: '#06b6d4', marginBottom: '1.5rem' }}>
          ID: {product.productId} | Batch: {product.batchNumber}
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
          <button className="btn-secondary" onClick={onClose}>Close</button>
          <a href={qrUrl} download={`QR-${product.productId}.png`} className="btn-primary" style={{ textDecoration: 'none' }}>
            <Download size={16} />
            Download QR
          </a>
        </div>
      </div>
    </div>
  );
};
