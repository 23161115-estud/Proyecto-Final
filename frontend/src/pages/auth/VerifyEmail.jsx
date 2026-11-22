import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import AuthShell from '../../components/AuthShell';
import { Alert } from '../../components/ui';
import { authApi } from '../../api/services';
import { parseError } from '../../utils/errors';

export default function VerifyEmail() {
  const [params] = useSearchParams();
  const token = params.get('token');
  const [state, setState] = useState({ status: token ? 'loading' : 'error', message: token ? '' : 'El enlace de verificación está incompleto.' });
  const called = useRef(false);

  useEffect(() => {
    if (!token || called.current) return;
    called.current = true;
    authApi
      .verify(token)
      .then((r) => setState({ status: 'ok', message: r.message }))
      .catch((e) => setState({ status: 'error', message: parseError(e).message }));
  }, [token]);

  return (
    <AuthShell title="Verificación de correo" footer={<Link to="/login">Ir a iniciar sesión</Link>}>
      {state.status === 'loading' && <Alert kind="info">Confirmando tu correo…</Alert>}
      {state.status === 'ok' && <Alert kind="success">{state.message}</Alert>}
      {state.status === 'error' && <Alert>{state.message}</Alert>}
    </AuthShell>
  );
}
