import React from 'react';
import { ShieldCheck, Cpu } from 'lucide-react';
import { RoleSwitcher } from './RoleSwitcher';
import { UserRole } from '../types';

interface HeaderProps {
  currentRole: UserRole;
  onRoleChange: (role: UserRole) => void;
}

export const Header: React.FC<HeaderProps> = ({ currentRole, onRoleChange }) => {
  return (
    <header className="glass-card header">
      <div className="brand">
        <div className="brand-icon">
          <ShieldCheck size={26} />
        </div>
        <div>
          <div className="brand-title">TrustChain Ledger</div>
          <div className="brand-subtitle">Hyperledger Fabric Traceability</div>
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.8rem', color: '#10b981', background: 'rgba(16,185,129,0.1)', padding: '0.4rem 0.8rem', borderRadius: '20px', border: '1px solid rgba(16,185,129,0.2)' }}>
          <Cpu size={14} />
          <span>Ledger Status: ACTIVE</span>
        </div>
        <RoleSwitcher currentRole={currentRole} onRoleChange={onRoleChange} />
      </div>
    </header>
  );
};
