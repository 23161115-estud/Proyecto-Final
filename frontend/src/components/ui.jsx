import { useEffect, useId } from 'react';
import { PASSWORD_RULES } from '../utils/validators';

export function Button({ variant = 'primary', loading = false, block = false, small = false, children, className = '', ...rest }) {
  const cls = `btn btn-${variant} ${block ? 'btn-block' : ''} ${small ? 'btn-sm' : ''} ${className}`;
  return (
    <button className={cls} disabled={loading || rest.disabled} {...rest}>
      {loading && <span className="spinner" aria-hidden="true" />}
      {children}
    </button>
  );
}

export function Field({ label, error, as = 'input', suffix, hint, children, ...rest }) {
  const id = useId();
  const Tag = as;
  return (
    <div className={`field ${error ? 'invalid' : ''}`}>
      {label && <label htmlFor={id}>{label}</label>}
      <div className={`control ${suffix ? 'has-suffix' : ''}`}>
        <Tag id={id} aria-invalid={Boolean(error)} aria-describedby={error ? `${id}-e` : undefined} {...rest}>
          {children}
        </Tag>
        {suffix && <span className="suffix">{suffix}</span>}
      </div>
      {hint && !error && <span className="field-hint">{hint}</span>}
      {error && <span id={`${id}-e`} className="field-error">{error}</span>}
    </div>
  );
}

export const Chip = ({ kind, children }) => <span className={`chip chip-${kind}`}>{children ?? kind}</span>;
export const Alert = ({ kind = 'error', children }) => (children ? <div className={`alert alert-${kind}`} role="alert">{children}</div> : null);

export function PasswordRules({ value }) {
  return (
    <div className="req-list" aria-label="Requisitos de la contraseña">
      {PASSWORD_RULES.map((r) => {
        const ok = r.test(value || '');
        return (
          <div key={r.id} className={`req ${ok ? 'ok' : ''}`}>
            <i aria-hidden="true">{ok ? '✓' : ''}</i>
            {r.label}
          </div>
        );
      })}
    </div>
  );
}

export function PageHead({ title, subtitle, children }) {
  return (
    <div className="page-head">
      <div>
        <h1>{title}</h1>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {children}
    </div>
  );
}

export function Modal({ title, subtitle, onClose, children, wide }) {
  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && onClose();
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onClose]);
  return (
    <div className="overlay" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal" role="dialog" aria-modal="true" aria-label={title} style={wide ? { maxWidth: 720 } : undefined}>
        <h2>{title}</h2>
        {subtitle && <p className="sub">{subtitle}</p>}
        {children}
      </div>
    </div>
  );
}

export function ConfirmDialog({ title, message, detail, confirmLabel = 'Confirmar', danger = true, loading, onConfirm, onCancel }) {
  return (
    <Modal title={title} onClose={onCancel}>
      {danger && <div className="warn-icon" aria-hidden="true">!</div>}
      <p>{message}</p>
      {detail && <p className="muted" style={{ marginTop: 6 }}>{detail}</p>}
      <div className="modal-actions">
        <Button variant="ghost" onClick={onCancel} disabled={loading}>Cancelar</Button>
        <Button variant={danger ? 'danger' : 'primary'} loading={loading} onClick={onConfirm}>{confirmLabel}</Button>
      </div>
    </Modal>
  );
}

export function Skeleton({ rows = 5 }) {
  return (
    <div className="card stack" aria-busy="true" aria-label="Cargando">
      {Array.from({ length: rows }).map((_, i) => <div key={i} className="skeleton" style={{ height: 22 }} />)}
    </div>
  );
}
export function ErrorState({ error, onRetry }) {
  return (
    <div className="card state-box" role="alert">
      <h3>{error.network ? 'Sin conexión con el servidor' : 'No pudimos cargar los datos'}</h3>
      <p>{error.message}</p>
      {onRetry && <Button variant="primary" onClick={onRetry}>Reintentar</Button>}
    </div>
  );
}
export function EmptyState({ title, text, action }) {
  return (
    <div className="card state-box">
      <h3>{title}</h3>
      <p>{text}</p>
      {action}
    </div>
  );
}

export function Pagination({ data, onPage }) {
  if (!data) return null;
  const { page, totalPages, totalElements, size, content } = data;
  const from = totalElements === 0 ? 0 : page * size + 1;
  const to = page * size + content.length;
  const nums = [];
  for (let i = Math.max(0, page - 2); i <= Math.min(totalPages - 1, page + 2); i++) nums.push(i);
  return (
    <nav className="pager" aria-label="Paginación">
      <span>Mostrando {from}–{to} de {totalElements}</span>
      {totalPages > 1 && (
        <div className="pages">
          <button className="page-btn" disabled={data.first} onClick={() => onPage(page - 1)} aria-label="Página anterior">‹</button>
          {nums.map((n) => (
            <button key={n} className={`page-btn ${n === page ? 'current' : ''}`} aria-current={n === page ? 'page' : undefined} onClick={() => onPage(n)}>{n + 1}</button>
          ))}
          <button className="page-btn" disabled={data.last} onClick={() => onPage(page + 1)} aria-label="Página siguiente">›</button>
        </div>
      )}
    </nav>
  );
}
