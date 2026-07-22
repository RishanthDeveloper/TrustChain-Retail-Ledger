import React from 'react';
import { UserRole } from '../types';

interface RoleSwitcherProps {
  currentRole: UserRole;
  onRoleChange: (role: UserRole) => void;
}

const roles: UserRole[] = ['MANUFACTURER', 'WHOLESALER', 'RETAILER', 'CONSUMER'];

export const RoleSwitcher: React.FC<RoleSwitcherProps> = ({ currentRole, onRoleChange }) => {
  return (
    <div className="role-pills">
      {roles.map((r) => (
        <button
          key={r}
          className={`role-pill ${currentRole === r ? 'active' : ''}`}
          onClick={() => onRoleChange(r)}
        >
          {r.charAt(0) + r.slice(1).toLowerCase()}
        </button>
      ))}
    </div>
  );
};
