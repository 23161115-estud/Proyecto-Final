import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { profileApi } from '../../api/services';
import { Alert, Button, Chip, ConfirmDialog, Field, PageHead, PasswordRules } from '../../components/ui';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import useForm from '../../hooks/useForm';
import { avatarSrc, initials } from '../../utils/format';
import { parseError } from '../../utils/errors';
import { first, matches, maxLen, minLen, password, phone, required } from '../../utils/validators';

function ContactForm() {
  const { user, setUser } = useAuth();
  const toast = useToast();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const form = useForm({ name: user.name, phone: user.phone || '' }, (v) => ({
    name: first(required(v.name, 'El nombre es obligatorio'), minLen(v.name, 2, 'El nombre'), maxLen(v.name, 100, 'El nombre')),
    phone: phone(v.phone),
  }));
  const submit = form.submit(async (v) => {
    setBusy(true);
    setError('');
    try {
      setUser(await profileApi.update({ name: v.name.trim(), phone: v.phone.trim() || null }));
      toast.success('Perfil actualizado');
    } catch (e) {
      const p = parseError(e);
      setError(p.message);
      form.setServerErrors(p.fields);
    } finally {
      setBusy(false);
    }
  });
  return (
    <form className="card stack" onSubmit={submit} noValidate>
      <h2 style={{ margin: 0 }}>Datos de contacto</h2>
      <Alert>{error}</Alert>
      <div className="grid cols-2">
        <Field label="Nombre" {...form.bind('name')} />
        <Field label="Teléfono (SMS y WhatsApp)" type="tel" placeholder="+529511234567" {...form.bind('phone')} />
        <Field label="Correo" value={user.email} disabled readOnly hint="El correo no se puede cambiar" />
      </div>
      <div><Button type="submit" loading={busy}>Guardar cambios</Button></div>
    </form>
  );
}

function PasswordForm() {
  const { user } = useAuth();
  const toast = useToast();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const needsCurrent = user.provider === 'LOCAL';
  const form = useForm({ currentPassword: '', newPassword: '', confirm: '' }, (v) => ({
    currentPassword: needsCurrent ? required(v.currentPassword, 'Escribe tu contraseña actual') : '',
    newPassword: first(required(v.newPassword, 'La nueva contraseña es obligatoria'), password(v.newPassword)),
    confirm: first(required(v.confirm, 'Confirma la nueva contraseña'), matches(v.confirm, v.newPassword)),
  }));
  const submit = form.submit(async (v) => {
    setBusy(true);
    setError('');
    try {
      const r = await profileApi.password({ currentPassword: v.currentPassword || null, newPassword: v.newPassword });
      toast.success(r.message);
      form.setValue('currentPassword', '');
      form.setValue('newPassword', '');
      form.setValue('confirm', '');
    } catch (e) {
      const p = parseError(e);
      setError(p.message);
      form.setServerErrors(p.fields);
    } finally {
      setBusy(false);
    }
  });
  return (
    <form className="card stack" onSubmit={submit} noValidate>
      <h2 style={{ margin: 0 }}>Cambiar contraseña</h2>
      <Alert>{error}</Alert>
      <div className="grid cols-3">
        {needsCurrent && <Field label="Contraseña actual" type="password" autoComplete="current-password" {...form.bind('currentPassword')} />}
        <Field label="Nueva contraseña" type="password" autoComplete="new-password" {...form.bind('newPassword')} />
        <Field label="Confirmar contraseña" type="password" autoComplete="new-password" {...form.bind('confirm')} />
      </div>
      <PasswordRules value={form.values.newPassword} />
      <div><Button type="submit" loading={busy}>Actualizar contraseña</Button></div>
    </form>
  );
}

export default function Profile() {
  const { user, setUser, logout } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const fileRef = useRef(null);
  const [uploading, setUploading] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const src = avatarSrc(user.avatarUrl);

  const onFile = async (e) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) return toast.error('Sube una imagen JPG, PNG o WEBP');
    if (file.size > 2 * 1024 * 1024) return toast.error('La imagen no puede pesar más de 2 MB');
    setUploading(true);
    try {
      setUser(await profileApi.avatar(file));
      toast.success('Foto actualizada');
    } catch (err) {
      toast.error(parseError(err).message);
    } finally {
      setUploading(false);
    }
  };

  const remove = async () => {
    setDeleting(true);
    try {
      await profileApi.remove();
      await logout();
      navigate('/login', { replace: true });
    } catch (e) {
      toast.error(parseError(e).message);
      setDeleting(false);
      setConfirmDelete(false);
    }
  };

  return (
    <>
      <PageHead title="Mi perfil" subtitle="Tus datos de cuenta y de contacto." />
      <div className="split-320">
        <div className="card stack" style={{ alignItems: 'center', textAlign: 'center' }}>
          {src ? <img className="avatar xl" src={src} alt="Tu foto de perfil" /> : <div className="avatar xl" aria-hidden="true">{initials(user.name)}</div>}
          <div><b style={{ fontSize: 18 }}>{user.name}</b><p className="muted">{user.email}</p></div>
          <Chip kind={user.emailVerified ? 'ok' : 'warn'}>{user.emailVerified ? 'Correo verificado' : 'Correo sin verificar'}</Chip>
          <input ref={fileRef} type="file" accept="image/jpeg,image/png,image/webp" hidden onChange={onFile} />
          <Button variant="ghost" loading={uploading} onClick={() => fileRef.current.click()}>Cambiar foto</Button>
        </div>
        <div className="stack">
          <ContactForm />
          <PasswordForm />
          {user.role !== 'ADMIN' && (
            <div className="card row" style={{ justifyContent: 'space-between' }}>
              <div><b>Eliminar mi cuenta</b><p className="muted">Se borran tu cuenta y todas tus simulaciones.</p></div>
              <Button variant="danger" onClick={() => setConfirmDelete(true)}>Eliminar mi cuenta</Button>
            </div>
          )}
        </div>
      </div>
      {confirmDelete && (
        <ConfirmDialog
          title="¿Eliminar tu cuenta?"
          message="Se eliminarán tu cuenta y todas tus simulaciones."
          detail="Esta acción no se puede deshacer."
          confirmLabel="Eliminar cuenta"
          loading={deleting}
          onConfirm={remove}
          onCancel={() => setConfirmDelete(false)}
        />
      )}
    </>
  );
}
