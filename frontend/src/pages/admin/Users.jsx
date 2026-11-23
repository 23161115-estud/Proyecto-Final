import { useState } from 'react';
import CrudPage from '../../components/CrudPage';
import { ConfirmDialog, Chip } from '../../components/ui';
import { adminApi } from '../../api/services';
import { useAuth } from '../../context/AuthContext';
import { parseError } from '../../utils/errors';
import { email, first, maxLen, minLen, password, phone, required } from '../../utils/validators';

const ROLES = [['CLIENTE', 'Cliente'], ['ANALISTA', 'Analista'], ['ADMIN', 'Administrador']];

export default function Users() {
  const { user: me } = useAuth();
  const [pending, setPending] = useState(null);
  const [busy, setBusy] = useState(false);

  const toggle = async () => {
    const { row, ctx } = pending;
    setBusy(true);
    try {
      await (row.active ? adminApi.users.deactivate(row.id) : adminApi.users.activate(row.id));
      ctx.toast.success(row.active ? 'Usuario desactivado' : 'Usuario activado');
      ctx.reload();
      setPending(null);
    } catch (e) {
      ctx.toast.error(parseError(e).message);
      setPending(null);
    } finally {
      setBusy(false);
    }
  };

  const config = {
    title: 'Usuarios',
    subtitle: 'Gestiona cuentas, roles y estado.',
    entity: 'Usuario',
    newLabel: '+ Nuevo usuario',
    api: adminApi.users,
    removable: false,
    initialFilters: { q: '', role: '', verified: '', sort: 'createdAt,desc' },
    filters: [
      { name: 'q', label: 'Buscar por nombre o correo', placeholder: 'Ej. ana@test.com' },
      { name: 'role', label: 'Rol', options: [['', 'Todos'], ...ROLES] },
      { name: 'verified', label: 'Verificado', options: [['', 'Todos'], ['true', 'Sí'], ['false', 'No']] },
    ],
    columns: [
      { label: 'Nombre', render: (r) => <b>{r.name}</b> },
      { label: 'Correo', render: (r) => r.email },
      { label: 'Rol', render: (r) => <Chip kind={r.role} /> },
      { label: 'Acceso', render: (r) => (r.provider === 'LOCAL' ? 'Local' : r.provider === 'GOOGLE' ? 'Google' : 'Facebook') },
      { label: 'Verificado', render: (r) => <Chip kind={r.emailVerified ? 'ok' : 'warn'}>{r.emailVerified ? 'Sí' : 'No'}</Chip> },
      { label: 'Estado', render: (r) => <Chip kind={r.active ? 'ok' : 'gray'}>{r.active ? 'Activo' : 'Inactivo'}</Chip> },
    ],
    rowActions: (row, ctx) =>
      row.id === me.id ? null : (
        <button className={`link-btn ${row.active ? 'danger' : ''}`} onClick={() => setPending({ row, ctx })}>
          {row.active ? 'Desactivar' : 'Activar'}
        </button>
      ),
    fields: [
      { name: 'name', label: 'Nombre completo', full: true, validate: (v) => first(required(v, 'El nombre es obligatorio'), minLen(v, 2, 'El nombre'), maxLen(v, 100, 'El nombre')) },
      { name: 'email', label: 'Correo electrónico', type: 'email', onlyCreate: true, validate: (v) => first(required(v, 'El correo es obligatorio'), email(v), maxLen(v, 150, 'El correo')) },
      { name: 'password', label: 'Contraseña', type: 'password', onlyCreate: true, validate: (v) => first(required(v, 'La contraseña es obligatoria'), password(v)) },
      { name: 'role', label: 'Rol', type: 'select', options: ROLES, validate: (v) => required(v, 'Elige un rol para continuar') },
      { name: 'phone', label: 'Teléfono', type: 'tel', placeholder: '+529511234567', validate: (v) => phone(v) },
    ],
    emptyValues: { name: '', email: '', password: '', role: '', phone: '' },
    toValues: (r) => ({ name: r.name, role: r.role, phone: r.phone || '' }),
    toPayload: (v, editing) => ({
      name: v.name.trim(),
      role: v.role,
      phone: v.phone.trim() || null,
      ...(editing ? {} : { email: v.email.trim(), password: v.password }),
    }),
  };

  return (
    <>
      <CrudPage config={config} />
      {pending && (
        <ConfirmDialog
          title={pending.row.active ? `¿Desactivar a ${pending.row.name}?` : `¿Activar a ${pending.row.name}?`}
          message={pending.row.active ? 'Ya no podrá iniciar sesión.' : 'Podrá volver a iniciar sesión.'}
          detail={pending.row.active ? 'Sus simulaciones se conservan y puedes reactivar la cuenta cuando quieras.' : undefined}
          danger={pending.row.active}
          confirmLabel={pending.row.active ? 'Desactivar usuario' : 'Activar usuario'}
          loading={busy}
          onConfirm={toggle}
          onCancel={() => setPending(null)}
        />
      )}
    </>
  );
}
