import { useState, useEffect } from 'react';
import { Header } from './components/Header';
import { CreateProductModal } from './components/CreateProductModal';
import { TransferModal } from './components/TransferModal';
import { ProvenanceTimeline } from './components/ProvenanceTimeline';
import { QRCodeModal } from './components/QRCodeModal';
import { Product, TransferRecord, UserRole, CreateProductForm, TransferForm } from './types';
import { fetchProducts, fetchProductHistory, createProduct, transferProduct } from './services/api';
import { Plus, Search, ShieldCheck, QrCode, ArrowRightLeft, History, RefreshCw } from 'lucide-react';

export function App() {
  const [role, setRole] = useState<UserRole>('MANUFACTURER');
  const [products, setProducts] = useState<Product[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  // Modal states
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [selectedProductForTransfer, setSelectedProductForTransfer] = useState<Product | null>(null);
  const [selectedProductForHistory, setSelectedProductForHistory] = useState<Product | null>(null);
  const [history, setHistory] = useState<TransferRecord[]>([]);
  const [selectedProductForQR, setSelectedProductForQR] = useState<Product | null>(null);

  const loadData = async () => {
    setLoading(true);
    try {
      const data = await fetchProducts();
      setProducts(data);
    } catch (err) {
      console.error('Failed to load products:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();

    // Check query params for consumer QR scan lookup
    const params = new URLSearchParams(window.location.search);
    const scannedId = params.get('product');
    if (scannedId) {
      setSearch(scannedId);
      setRole('CONSUMER');
    }
  }, []);

  const handleCreateProduct = async (form: CreateProductForm) => {
    await createProduct(form, role);
    await loadData();
  };

  const handleTransfer = async (productId: string, form: TransferForm) => {
    await transferProduct(productId, form, `${role}-USER`);
    await loadData();
  };

  const handleViewHistory = async (product: Product) => {
    setSelectedProductForHistory(product);
    try {
      const h = await fetchProductHistory(product.productId);
      setHistory(h);
    } catch (err) {
      console.error(err);
    }
  };

  const filteredProducts = products.filter(
    (p) =>
      p.productId.toLowerCase().includes(search.toLowerCase()) ||
      p.name.toLowerCase().includes(search.toLowerCase()) ||
      p.batchNumber.toLowerCase().includes(search.toLowerCase()) ||
      p.currentOwnerId.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="app-container">
      <Header currentRole={role} onRoleChange={setRole} />

      {/* Top Banner info */}
      <div className="glass-card" style={{ padding: '1.25rem 1.75rem', marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ fontSize: '0.85rem', color: '#10b981', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Current Portal Context
          </div>
          <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#fff' }}>
            Logged in as: <span style={{ color: '#06b6d4' }}>{role}</span>
          </div>
        </div>
        <button className="btn-secondary" onClick={loadData}>
          <RefreshCw size={16} className={loading ? 'spin' : ''} />
          Sync Ledger State
        </button>
      </div>

      {/* Actions bar */}
      <div className="actions-bar">
        <input
          type="text"
          className="search-input"
          placeholder="Search product ID, batch number, name or owner..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />

        {role === 'MANUFACTURER' && (
          <button className="btn-primary" onClick={() => setIsCreateOpen(true)}>
            <Plus size={18} />
            Create Product (Digital Twin)
          </button>
        )}
      </div>

      {/* Product Cards */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem', color: '#94a3b8' }}>
          Syncing with Hyperledger Fabric ledger...
        </div>
      ) : filteredProducts.length === 0 ? (
        <div className="glass-card" style={{ textAlign: 'center', padding: '4rem' }}>
          <ShieldCheck size={48} color="#64748b" style={{ marginBottom: '1rem' }} />
          <h3 style={{ fontSize: '1.2rem', color: '#fff', marginBottom: '0.5rem' }}>No ledger records found</h3>
          <p style={{ color: '#94a3b8', fontSize: '0.9rem' }}>
            {role === 'MANUFACTURER'
              ? 'Click "Create Product" to issue a new digital twin on the blockchain.'
              : 'No products match your current search query.'}
          </p>
        </div>
      ) : (
        <div className="product-grid">
          {filteredProducts.map((p) => (
            <div key={p.productId} className="glass-card product-card">
              <div>
                <div className="product-header">
                  <span className="product-id">{p.productId}</span>
                  <span className={`status-badge ${p.status}`}>{p.status.replace('_', ' ')}</span>
                </div>

                <div className="product-title">{p.name}</div>

                <div className="meta-row">
                  <span>Batch Number:</span>
                  <strong style={{ color: '#f8fafc' }}>{p.batchNumber}</strong>
                </div>
                <div className="meta-row">
                  <span>Current Owner:</span>
                  <strong style={{ color: '#06b6d4' }}>{p.currentOwnerId}</strong>
                </div>
                <div className="meta-row">
                  <span>Owner Role:</span>
                  <strong style={{ color: '#10b981' }}>{p.currentOwnerRole}</strong>
                </div>

                <div className="hash-preview" title={p.certificateHash}>
                  Cert Hash: {p.certificateHash}
                </div>
              </div>

              <div className="card-actions">
                <button className="btn-secondary" style={{ flex: 1 }} onClick={() => handleViewHistory(p)}>
                  <History size={16} />
                  Trace History
                </button>

                <button className="btn-secondary" onClick={() => setSelectedProductForQR(p)}>
                  <QrCode size={16} />
                </button>

                {role !== 'CONSUMER' && (
                  <button className="btn-primary" onClick={() => setSelectedProductForTransfer(p)}>
                    <ArrowRightLeft size={16} />
                    Transfer
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modals */}
      <CreateProductModal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onSubmit={handleCreateProduct}
      />

      <TransferModal
        product={selectedProductForTransfer}
        isOpen={!!selectedProductForTransfer}
        onClose={() => setSelectedProductForTransfer(null)}
        onSubmit={handleTransfer}
      />

      <ProvenanceTimeline
        product={selectedProductForHistory}
        history={history}
        isOpen={!!selectedProductForHistory}
        onClose={() => setSelectedProductForHistory(null)}
      />

      <QRCodeModal
        product={selectedProductForQR}
        isOpen={!!selectedProductForQR}
        onClose={() => setSelectedProductForQR(null)}
      />
    </div>
  );
}
