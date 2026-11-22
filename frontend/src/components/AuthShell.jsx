import { Link } from 'react-router-dom';
import { LogoIcon } from './Logo';

export default function AuthShell({ title, subtitle, children, footer }) {
  return (
    <div className="auth">
      <aside className="auth-side">
        <Link to="/login" className="brand"><LogoIcon size={56} bg="#fff" fg="#1D5E4B" arrow="#F2A900" /> OaxInvest</Link>
        <div>
          <h2>¿Conviene construir departamentos aquí?</h2>
          <p>Calcula costo, renta y retorno por colonia de Oaxaca de Juárez en menos de un minuto.</p>
        </div>
        <svg className="auth-arches" viewBox="0 0 720 340" preserveAspectRatio="none" height="340" aria-hidden="true">
          {[0, 1, 2, 3, 4].map((i) => (
            <path key={i} d={`M${60 + i * 120} 340 V${200 - i * 30} a60 60 0 0 1 120 0 V340 Z`} fill="#1D5E4B" opacity={0.35 + i * 0.1} />
          ))}
        </svg>
      </aside>
      <section className="auth-main">
        <div className="auth-form">
          <h1>{title}</h1>
          {subtitle && <p className="sub">{subtitle}</p>}
          {children}
          {footer && <p className="auth-foot">{footer}</p>}
        </div>
      </section>
    </div>
  );
}
