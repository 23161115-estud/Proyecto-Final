import { useCallback, useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { authApi } from '../api/services';
import { useAuth } from '../context/AuthContext';
import { HOME_BY_ROLE, parseError } from '../utils/errors';
import loadScript from '../utils/loadScript';
import { Alert } from './ui';

let configPromise = null;
const getConfig = () => {
  if (!configPromise) {
    configPromise = authApi.socialConfig().catch((e) => {
      configPromise = null;
      throw e;
    });
  }
  return configPromise;
};

let fbPromise = null;
function loadFacebook(appId) {
  if (!fbPromise) {
    fbPromise = new Promise((resolve, reject) => {
      window.fbAsyncInit = () => {
        window.FB.init({ appId, cookie: false, xfbml: false, version: 'v21.0' });
        resolve(window.FB);
      };
      loadScript('https://connect.facebook.net/es_LA/sdk.js').catch((e) => {
        fbPromise = null;
        reject(e);
      });
    });
  }
  return fbPromise;
}

const FbIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">
    <path fill="#fff" d="M24 12a12 12 0 1 0-13.9 11.9v-8.4H7.1V12h3V9.4c0-3 1.8-4.7 4.5-4.7 1.3 0 2.7.2 2.7.2v3h-1.5c-1.5 0-2 .9-2 1.9V12h3.4l-.5 3.5h-2.9v8.4A12 12 0 0 0 24 12z" />
  </svg>
);

export default function SocialButtons() {
  const { loginSocial } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [config, setConfig] = useState(null);
  const [fbReady, setFbReady] = useState(false);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const googleRef = useRef(null);

  const finish = useCallback(async (provider, token) => {
    setBusy(true);
    setError('');
    try {
      const user = await loginSocial(provider, token);
      navigate(location.state?.from || HOME_BY_ROLE[user.role], { replace: true });
    } catch (e) {
      const p = parseError(e);
      setError(p.status === 401 ? 'No pudimos verificar tu cuenta. Intenta de nuevo.' : p.message);
      setBusy(false);
    }
  }, [loginSocial, navigate, location.state]);

  const finishRef = useRef(finish);
  useEffect(() => { finishRef.current = finish; }, [finish]);

  useEffect(() => {
    let alive = true;
    getConfig().then((c) => alive && setConfig(c)).catch(() => {});
    return () => { alive = false; };
  }, []);

  useEffect(() => {
    if (!config?.googleClientId || !googleRef.current) return undefined;
    let alive = true;
    loadScript('https://accounts.google.com/gsi/client')
      .then(() => {
        if (!alive || !googleRef.current) return;
        window.google.accounts.id.initialize({
          client_id: config.googleClientId,
          callback: (resp) => resp.credential && finishRef.current('google', resp.credential),
        });
        window.google.accounts.id.renderButton(googleRef.current, {
          theme: 'outline', size: 'large', text: 'continue_with', shape: 'rectangular', locale: 'es',
          width: Math.min(googleRef.current.clientWidth || 400, 400),
        });
      })
      .catch(() => alive && setError('No se pudo cargar el inicio de sesión con Google. Revisa tu conexión.'));
    return () => { alive = false; };
  }, [config]);

  useEffect(() => {
    if (!config?.facebookAppId) return undefined;
    let alive = true;
    loadFacebook(config.facebookAppId)
      .then(() => alive && setFbReady(true))
      .catch(() => alive && setError('No se pudo cargar el inicio de sesión con Facebook. Revisa tu conexión.'));
    return () => { alive = false; };
  }, [config]);

  const onFacebook = () => {
    setError('');
    window.FB.login(
      (resp) => {
        if (resp.authResponse?.accessToken) finishRef.current('facebook', resp.authResponse.accessToken);
        else setError('Se canceló el inicio de sesión con Facebook.');
      },
      { scope: 'public_profile,email' }
    );
  };

  if (!config || (!config.googleClientId && !config.facebookAppId)) return null;

  return (
    <div className="social">
      <div className="divider"><span>o continúa con</span></div>
      <Alert>{error}</Alert>
      {config.googleClientId && <div ref={googleRef} className="social-google" aria-busy={busy} />}
      {config.facebookAppId && (
        <button type="button" className="btn btn-block btn-facebook" onClick={onFacebook} disabled={!fbReady || busy}>
          <FbIcon /> Continuar con Facebook
        </button>
      )}
    </div>
  );
}
