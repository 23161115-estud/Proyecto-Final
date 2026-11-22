import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { HOME_BY_ROLE } from '../utils/errors';

const Loading = () => <div className="loading-screen"><span className="spinner" style={{ width: 32, height: 32 }} /></div>;

export function RequireAuth({ roles }) {
  const { user, loading } = useAuth();
  const location = useLocation();
  if (loading) return <Loading />;
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/forbidden" replace />;
  return <Outlet />;
}

export function PublicOnly() {
  const { user, loading } = useAuth();
  if (loading) return <Loading />;
  if (user) return <Navigate to={HOME_BY_ROLE[user.role]} replace />;
  return <Outlet />;
}
