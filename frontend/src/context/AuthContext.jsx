import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { authApi, profileApi } from '../api/services';

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(Boolean(localStorage.getItem('token')));

  useEffect(() => {
    if (!localStorage.getItem('token')) return;
    profileApi
      .get()
      .then(setUser)
      .catch(() => localStorage.removeItem('token'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    const onExpired = () => setUser(null);
    window.addEventListener('auth:expired', onExpired);
    return () => window.removeEventListener('auth:expired', onExpired);
  }, []);

  const login = useCallback(async (credentials) => {
    const res = await authApi.login(credentials);
    localStorage.setItem('token', res.token);
    setUser(res.user);
    return res.user;
  }, []);

  const loginSocial = useCallback(async (provider, token) => {
    const res = await (provider === 'google' ? authApi.google(token) : authApi.facebook(token));
    localStorage.setItem('token', res.token);
    setUser(res.user);
    return res.user;
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
    }
    localStorage.removeItem('token');
    setUser(null);
  }, []);

  const value = useMemo(() => ({ user, setUser, loading, login, loginSocial, logout }), [user, loading, login, loginSocial, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
