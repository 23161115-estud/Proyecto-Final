export const PASSWORD_RULES = [
  { id: 'len', label: '8 caracteres o más', test: (v) => v.length >= 8 },
  { id: 'upper', label: 'Una mayúscula', test: (v) => /[A-Z]/.test(v) },
  { id: 'digit', label: 'Un número', test: (v) => /\d/.test(v) },
  { id: 'special', label: 'Un carácter especial', test: (v) => /[^A-Za-z0-9]/.test(v) },
];
export const PASSWORD_MSG = 'La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial';

const blank = (v) => v === undefined || v === null || String(v).trim() === '';

export const required = (v, msg = 'Este campo es obligatorio') => (blank(v) ? msg : '');
export const email = (v) => (blank(v) || /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(String(v).trim()) ? '' : 'El correo no tiene un formato válido');
export const password = (v) => (blank(v) || PASSWORD_RULES.every((r) => r.test(v)) ? '' : PASSWORD_MSG);
export const phone = (v) => (blank(v) || /^\+[1-9]\d{7,14}$/.test(String(v).trim()) ? '' : 'Usa formato internacional, por ejemplo +529511234567');
export const maxLen = (v, n, label = 'El campo') => (String(v ?? '').length > n ? `${label} no puede exceder ${n} caracteres` : '');
export const minLen = (v, n, label = 'El campo') => (!blank(v) && String(v).trim().length < n ? `${label} debe tener al menos ${n} caracteres` : '');
export const range = (v, min, max, label = 'El valor', { integer = false } = {}) => {
  if (blank(v)) return '';
  const n = Number(v);
  if (Number.isNaN(n)) return `${label} debe ser un número`;
  if (integer && !Number.isInteger(n)) return `${label} debe ser un número entero`;
  if (n < min) return `${label} debe ser al menos ${min}`;
  if (n > max) return `${label} no puede ser mayor a ${max}`;
  return '';
};
export const matches = (v, other, msg = 'Las contraseñas no coinciden') => (v === other ? '' : msg);
export const first = (...msgs) => msgs.find((m) => m) || '';
