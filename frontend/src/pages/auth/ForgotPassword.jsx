import { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthShell from '../../components/AuthShell';
import { Alert, Button, Field } from '../../components/ui';
import useForm from '../../hooks/useForm';
import { authApi } from '../../api/services';
import { parseError } from '../../utils/errors';
import { email, required } from '../../utils/validators';

export default function ForgotPassword() {
  const [error, setError] = useState('');
  const [done, setDone] = useState('');
  const [busy, setBusy] = useState(false);
  const form = useForm({ email: '' }, (v) => ({ email: required(v.email, 'El correo es obligatorio') || email(v.email) }));

  const onSubmit = form.submit(async (v) => {
    setBusy(true);
    setError('');
    try {
      const res = await authApi.forgot({ email: v.email.trim() });
      setDone(res.message);
    } catch (e) {
      setError(parseError(e).message);
    } finally {
      setBusy(false);
    }
  });

  return (
    <AuthShell title="Recupera tu contraseña" subtitle="Te enviaremos un enlace a tu correo." footer={<Link to="/login">‹ Volver a iniciar sesión</Link>}>
      <form className="stack" onSubmit={onSubmit} noValidate>
        <Alert>{error}</Alert>
        {done && <Alert kind="info">{done} Revisa también tu carpeta de spam.</Alert>}
        <Field label="Correo electrónico" type="email" autoComplete="email" {...form.bind('email')} />
        <Button type="submit" block loading={busy}>Enviar enlace</Button>
      </form>
    </AuthShell>
  );
}
