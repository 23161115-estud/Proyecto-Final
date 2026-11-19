const money = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN', maximumFractionDigits: 0 });
export const formatMoney = (n) => (n === null || n === undefined ? '—' : money.format(Number(n)));
export const formatPct = (n) => (n === null || n === undefined ? '—' : `${Number(n).toFixed(2)}%`);
export const formatMonths = (n) => (Number(n) >= 9999 ? 'No se recupera' : `${Number(n).toFixed(1)} meses`);
export const formatDate = (iso) => (iso ? new Date(iso).toLocaleDateString('es-MX', { day: '2-digit', month: 'short', year: 'numeric' }) : '—');
export const formatDateTime = (iso) =>
  iso ? new Date(iso).toLocaleString('es-MX', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : '—';
export const initials = (name = '') => name.split(' ').filter(Boolean).slice(0, 2).map((p) => p[0].toUpperCase()).join('') || '?';
export const avatarSrc = (url) => (url ? `${import.meta.env.VITE_API_URL || ''}${url}` : null);
export const MONTHS = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
