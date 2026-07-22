import React from 'react';
import { X, ShieldCheck, Clock, Hash, CheckCircle2 } from 'lucide-react';
import { Product, TransferRecord } from '../types';

interface ProvenanceTimelineProps {
  product: Product | null;
  history: TransferRecord[];
  isOpen: boolean;
  onClose: () => void;
}

export const ProvenanceTimeline: React.FC<ProvenanceTimelineProps> = ({ product, history, isOpen, onClose }) => {
  if (!isOpen || !product) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="glass-card modal-content" style={{ maxWidth: '640px' }} onClick={(e) => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <div>
            <h3 style={{ fontSize: '1.25rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <ShieldCheck size={22} color="#10b981" />
              Immutable Provenance Audit Trail
            </h3>
            <div style={{ fontSize: '0.85rem', color: '#94a3b8', marginTop: '0.2rem' }}>
              {product.name} ({product.productId})
            </div>
          </div>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer' }}>
            <X size={20} />
          </button>
        </div>

        <div style={{ background: 'rgba(16, 185, 129, 0.08)', padding: '1rem', borderRadius: '12px', border: '1px solid rgba(16, 185, 129, 0.2)', marginBottom: '1.5rem' }}>
          <div style={{ fontSize: '0.8rem', color: '#10b981', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            <CheckCircle2 size={14} style={{ display: 'inline', marginRight: '4px' }} />
            Cryptographically Verified Ledger State
          </div>
          <div style={{ fontSize: '0.75rem', fontFamily: 'JetBrains Mono', color: '#64748b', wordBreak: 'break-all', marginTop: '0.3rem' }}>
            Cert Hash: {product.certificateHash}
          </div>
        </div>

        <div className="timeline">
          {history.map((record, index) => (
            <div key={record.txId || index} className="timeline-item">
              <div className="timeline-dot" />
              <div className="timeline-card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
                  <span style={{ fontWeight: 700, fontSize: '0.95rem', color: '#fff' }}>
                    {record.status} ({record.toOwnerRole})
                  </span>
                  <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                    <Clock size={12} />
                    {new Date(record.timestamp * 1000).toLocaleString()}
                  </span>
                </div>
                <div style={{ fontSize: '0.85rem', color: '#94a3b8', marginBottom: '0.4rem' }}>
                  Owner: <strong style={{ color: '#f8fafc' }}>{record.toOwnerId}</strong>
                </div>
                <div className="tx-id">
                  <Hash size={12} style={{ display: 'inline', marginRight: '2px' }} />
                  TxId: {record.txId}
                </div>
              </div>
            </div>
          ))}
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '2rem' }}>
          <button className="btn-secondary" onClick={onClose}>Close Visualizer</button>
        </div>
      </div>
    </div>
  );
};
