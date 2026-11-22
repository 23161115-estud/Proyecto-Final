import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import AuthShell from '../../components/AuthShell';
import SocialButtons from '../../components/SocialButtons';
import { Alert, Button, Field } from '../../components/ui';
import { useAuth } from '../../context/AuthContext';
import useForm from '../../hooks/useForm';
import { HOME_BY_ROLE, parseError } from '../../utils/errors';
import { email, required } from '../../utils/validators';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const form = useForm({ email: '', password: '' }, (v) => ({
    email: required(v.email, 'El correo es obligatorio') || email(v.email),
    password: required(v.password, 'La contraseña es obligatoria'),
  }));

  const onSubmit = form.submit(async (v) => {
    setBusy(true);
    setError('');
    try {
      const user = await login({ email: v.email.trim(), password: v.password });
      navigate(location.state?.from || HOME_BY_ROLE[user.role], { replace: true });
    } catch (e) {
      const p = parseError(e);
      setError(p.status === 401 ? 'Correo o contraseña incorrectos' : p.message);
      form.setServerErrors(p.fields);
    } finally {
      setBusy(false);
    }
  });

  return (
    <AuthShell title="Inicia sesión" subtitle="Entra para ver tus simulaciones." footer={<>¿Aún no tienes cuenta? <Link to="/register">Crear cuenta</Link></>}>
      <form className="stack" onSubmit={onSubmit} noValidate>
        <Alert>{error}</Alert>
        <Field label="Correo electrónico" type="email" autoComplete="email" {...form.bind('email')} />
        <Field label="Contraseña" type="password" autoComplete="current-password" {...form.bind('password')} />
        <div><Link to="/forgot-password">¿Olvidaste tu contraseña?</Link></div>
        <Button type="submit" block loading={busy}>Iniciar sesión</Button>
      </form>
      <SocialButtons />
    </AuthShell>
  );
}
