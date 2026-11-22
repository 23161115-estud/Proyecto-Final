import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import AuthShell from '../../components/AuthShell';
import { Alert, Button, Field, PasswordRules } from '../../components/ui';
import useForm from '../../hooks/useForm';
import { authApi } from '../../api/services';
import { parseError } from '../../utils/errors';
import { first, matches, password, required } from '../../utils/validators';

export default function ResetPassword() {
  const [params] = useSearchParams();
  const token = params.get('token');
  const [error, setError] = useState('');
  const [done, setDone] = useState('');
  const [busy, setBusy] = useState(false);

  const form = useForm({ newPassword: '', confirm: '' }, (v) => ({
    newPassword: first(required(v.newPassword, 'La contraseña es obligatoria'), password(v.newPassword)),
    confirm: first(required(v.confirm, 'Confirma tu contraseña'), matches(v.confirm, v.newPassword)),
  }));

  const onSubmit = form.submit(async (v) => {
    setBusy(true);
    setError('');
    try {
      const res = await authApi.reset({ token, newPassword: v.newPassword });
      setDone(res.message);
    } catch (e) {
      const p = parseError(e);
      setError(p.message);
      form.setServerErrors(p.fields);
    } finally {
      setBusy(false);
    }
  });

  if (!token) {
    return (
      <AuthShell title="Enlace no válido" footer={<Link to="/forgot-password">Solicitar un enlace nuevo</Link>}>
        <Alert>El enlace de recuperación está incompleto.</Alert>
      </AuthShell>
    );
  }
  if (done) {
    return (
      <AuthShell title="Contraseña actualizada">
        <Alert kind="success">{done}</Alert>
        <Link className="btn btn-primary btn-block" to="/login">Iniciar sesión</Link>
      </AuthShell>
    );
  }
  return (
    <AuthShell title="Nueva contraseña" subtitle="Elige una contraseña segura.">
      <form className="stack" onSubmit={onSubmit} noValidate>
        <Alert>{error}</Alert>
        <Field label="Nueva contraseña" type="password" autoComplete="new-password" {...form.bind('newPassword')} />
        <PasswordRules value={form.values.newPassword} />
        <Field label="Confirmar contraseña" type="password" autoComplete="new-password" {...form.bind('confirm')} />
        <Button type="submit" block loading={busy}>Guardar contraseña</Button>
      </form>
    </AuthShell>
  );
}
