import { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthShell from '../../components/AuthShell';
import SocialButtons from '../../components/SocialButtons';
import { Alert, Button, Field, PasswordRules } from '../../components/ui';
import useForm from '../../hooks/useForm';
import { authApi } from '../../api/services';
import { parseError } from '../../utils/errors';
import { email, first, matches, maxLen, minLen, password, phone, required } from '../../utils/validators';

export default function Register() {
  const [error, setError] = useState('');
  const [done, setDone] = useState('');
  const [busy, setBusy] = useState(false);

  const form = useForm({ name: '', email: '', password: '', confirm: '', phone: '' }, (v) => ({
    name: first(required(v.name, 'El nombre es obligatorio'), minLen(v.name, 2, 'El nombre'), maxLen(v.name, 100, 'El nombre')),
    email: first(required(v.email, 'El correo es obligatorio'), email(v.email), maxLen(v.email, 150, 'El correo')),
    password: first(required(v.password, 'La contraseña es obligatoria'), password(v.password)),
    confirm: first(required(v.confirm, 'Confirma tu contraseña'), matches(v.confirm, v.password, 'Las contraseñas no coinciden')),
    phone: phone(v.phone),
  }));

  const onSubmit = form.submit(async (v) => {
    setBusy(true);
    setError('');
    try {
      const res = await authApi.register({ name: v.name.trim(), email: v.email.trim(), password: v.password, phone: v.phone.trim() || null });
      setDone(res.message);
    } catch (e) {
      const p = parseError(e);
      setError(p.message);
      form.setServerErrors(p.fields);
    } finally {
      setBusy(false);
    }
  });

  if (done) {
    return (
      <AuthShell title="Revisa tu correo" subtitle="Casi terminamos.">
        <Alert kind="success">{done}</Alert>
        <Link className="btn btn-primary btn-block" to="/login">Ir a iniciar sesión</Link>
      </AuthShell>
    );
  }

  return (
    <AuthShell title="Crea tu cuenta" subtitle="Guarda y compara tus simulaciones." footer={<>¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link></>}>
      <form className="stack" onSubmit={onSubmit} noValidate>
        <Alert>{error}</Alert>
        <Field label="Nombre completo" autoComplete="name" {...form.bind('name')} />
        <Field label="Correo electrónico" type="email" autoComplete="email" {...form.bind('email')} />
        <Field label="Contraseña" type="password" autoComplete="new-password" {...form.bind('password')} />
        <PasswordRules value={form.values.password} />
        <Field label="Confirmar contraseña" type="password" autoComplete="new-password" {...form.bind('confirm')} />
        <Field label="Teléfono (opcional, para SMS y WhatsApp)" type="tel" placeholder="+529511234567" {...form.bind('phone')} />
        <Button type="submit" block loading={busy}>Crear cuenta</Button>
      </form>
      <SocialButtons />
    </AuthShell>
  );
}
