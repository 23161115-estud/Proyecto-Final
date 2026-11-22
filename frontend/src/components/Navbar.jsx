import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { avatarSrc, initials } from '../utils/format';
import { HOME_BY_ROLE } from '../utils/errors';
import { LogoIcon } from './Logo';
import { Button, Chip } from './ui';

const LINKS = {
  CLIENTE: [['/simulations', 'Mis simulaciones'], ['/simulations/new', 'Nueva simulación'], ['/profile', 'Perfil']],
  ANALISTA: [['/neighborhoods', 'Colonias'], ['/amenities', 'Amenidades'], ['/ai-data', 'Dataset IA'], ['/profile', 'Perfil']],
  ADMIN: [['/admin', 'Dashboard'], ['/admin/users', 'Usuarios'], ['/neighborhoods', 'Colonias'], ['/amenities', 'Amenidades'], ['/ai-data', 'Dataset IA'], ['/admin/email-logs', 'Logs de correo'], ['/profile', 'Perfil']],
};

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const src = avatarSrc(user.avatarUrl);

  const exit = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <header className={`navbar ${open ? 'open' : ''}`}>
      <div className="navbar-in">
        <Link to={HOME_BY_ROLE[user.role]} className="brand" aria-label="OaxInvest, inicio">
          <LogoIcon size={40} /> OaxInvest
        </Link>
        <button className="burger" aria-label="Abrir menú" aria-expanded={open} onClick={() => setOpen((o) => !o)}>
          <span /><span /><span />
        </button>
        <nav className="nav-links" aria-label="Principal" onClick={() => setOpen(false)}>
          {LINKS[user.role].map(([to, label]) => (
            <NavLink key={to} to={to} end className={({ isActive }) => (isActive ? 'active' : '')}>{label}</NavLink>
          ))}
        </nav>
        <div className="nav-user">
          <Chip kind={user.role} />
          <div className="nav-who">
            <b>{user.name}</b>
            <span>{user.email}</span>
          </div>
          {src ? <img className="avatar" src={src} alt="" /> : <div className="avatar" aria-hidden="true">{initials(user.name)}</div>}
          <Button variant="ghost" small onClick={exit}>Salir</Button>
        </div>
      </div>
    </header>
  );
}
