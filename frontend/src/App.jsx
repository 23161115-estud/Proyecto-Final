import { Link, Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { HOME_BY_ROLE } from './utils/errors';
import Layout from './components/Layout';
import { PublicOnly, RequireAuth } from './components/Guards';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import ForgotPassword from './pages/auth/ForgotPassword';
import ResetPassword from './pages/auth/ResetPassword';
import VerifyEmail from './pages/auth/VerifyEmail';
import Simulations from './pages/client/Simulations';
import NewSimulation from './pages/client/NewSimulation';
import SimulationResult from './pages/client/SimulationResult';
import Profile from './pages/client/Profile';
import Neighborhoods from './pages/staff/Neighborhoods';
import Amenities from './pages/staff/Amenities';
import AiData from './pages/staff/AiData';
import Dashboard from './pages/admin/Dashboard';
import Users from './pages/admin/Users';
import EmailLogs from './pages/admin/EmailLogs';

function ErrorPage({ code, title, text }) {
  const { user } = useAuth();
  return (
    <div className="centered">
      <div>
        <h1>{code}</h1>
        <h2>{title}</h2>
        <p className="muted">{text}</p>
        <div style={{ marginTop: 20 }}>
          <Link className="btn btn-primary" to={user ? HOME_BY_ROLE[user.role] : '/login'}>Volver al inicio</Link>
        </div>
      </div>
    </div>
  );
}

function Home() {
  const { user, loading } = useAuth();
  if (loading) return null;
  return <Navigate to={user ? HOME_BY_ROLE[user.role] : '/login'} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route element={<PublicOnly />}>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
      </Route>
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/verify-email" element={<VerifyEmail />} />

      <Route element={<RequireAuth />}>
        <Route element={<Layout />}>
          <Route path="/profile" element={<Profile />} />
          <Route element={<RequireAuth roles={['CLIENTE']} />}>
            <Route path="/simulations" element={<Simulations />} />
            <Route path="/simulations/new" element={<NewSimulation />} />
            <Route path="/simulations/:id" element={<SimulationResult />} />
          </Route>
          <Route element={<RequireAuth roles={['ADMIN', 'ANALISTA']} />}>
            <Route path="/neighborhoods" element={<Neighborhoods />} />
            <Route path="/amenities" element={<Amenities />} />
            <Route path="/ai-data" element={<AiData />} />
          </Route>
          <Route element={<RequireAuth roles={['ADMIN']} />}>
            <Route path="/admin" element={<Dashboard />} />
            <Route path="/admin/users" element={<Users />} />
            <Route path="/admin/email-logs" element={<EmailLogs />} />
          </Route>
        </Route>
      </Route>

      <Route path="/forbidden" element={<ErrorPage code="403" title="No tienes acceso a esta página" text="Tu rol no permite ver este contenido." />} />
      <Route path="*" element={<ErrorPage code="404" title="No encontramos esta página" text="Revisa la dirección o vuelve al inicio." />} />
    </Routes>
  );
}
