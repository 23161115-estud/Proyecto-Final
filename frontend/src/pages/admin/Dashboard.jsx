import { useEffect, useState } from 'react';
import { adminApi } from '../../api/services';
import { Chip, ErrorState, PageHead, Skeleton } from '../../components/ui';
import { formatDateTime, MONTHS } from '../../utils/format';
import { parseError } from '../../utils/errors';

const SEM = { VERDE: 'var(--green)', AMARILLO: 'var(--yellow)', ROJO: 'var(--red)' };

export default function Dashboard() {
  const [d, setD] = useState(null);
  const [error, setError] = useState(null);
  const load = () => {
    setError(null);
    adminApi.dashboard().then(setD).catch((e) => setError(parseError(e)));
  };
  useEffect(load, []);

  if (error) return <ErrorState error={error} onRetry={load} />;
  if (!d) return <Skeleton rows={8} />;

  const maxMonth = Math.max(1, ...d.simulationsByMonth.map((m) => m.total));
  const totalSem = Math.max(1, Object.values(d.bySemaphore).reduce((a, b) => a + b, 0));

  return (
    <>
      <PageHead title="Panel de administración" subtitle="Resumen general del sistema." />
      <div className="grid cols-4">
        <div className="card kpi"><span>Usuarios</span><b>{d.users}</b></div>
        <div className="card kpi" style={{ '--c': 'var(--gold)' }}><span>Simulaciones</span><b>{d.simulations}</b></div>
        <div className="card kpi" style={{ '--c': 'var(--green)' }}><span>Correos enviados</span><b>{d.emailsSent}</b><small>{d.emailsPending} pendientes</small></div>
        <div className="card kpi" style={{ '--c': 'var(--red)' }}><span>Correos fallidos</span><b>{d.emailsFailed}</b><small>Revisar logs</small></div>
      </div>

      <div className="split-32">
        <div className="card">
          <h2>Simulaciones por mes</h2>
          {d.simulationsByMonth.length === 0 ? <p className="muted">Aún no hay simulaciones en los últimos 6 meses.</p> : (
            <>
              <div className="bars">
                {d.simulationsByMonth.map((m, i) => (
                  <div className="bar-col" key={m.month}>
                    {m.total}
                    <div className={`bar ${i === d.simulationsByMonth.length - 1 ? 'last' : ''}`} style={{ height: `${(m.total / maxMonth) * 200}px` }} />
                  </div>
                ))}
              </div>
              <div className="bar-labels">{d.simulationsByMonth.map((m) => <span key={m.month}>{MONTHS[Number(m.month.slice(5)) - 1]}</span>)}</div>
            </>
          )}
        </div>
        <div className="stack">
          <div className="card">
            <h2>Distribución del semáforo</h2>
            {Object.entries(d.bySemaphore).map(([k, v]) => (
              <div className="meter" key={k}>
                <span>{k}</span>
                <div className="meter-track"><div className="meter-fill" style={{ width: `${(v / totalSem) * 100}%`, background: SEM[k] }} /></div>
                <span>{Math.round((v / totalSem) * 100)}%</span>
              </div>
            ))}
          </div>
          <div className="card">
            <h2>Últimos correos</h2>
            {d.recentEmails.length === 0 ? <p className="muted">Sin envíos todavía.</p> : d.recentEmails.map((e) => (
              <div key={e.id} className="row" style={{ justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid var(--border)' }}>
                <span style={{ minWidth: 0 }}>{e.recipientEmail}<br /><span className="muted" style={{ fontSize: 12 }}>{formatDateTime(e.sentAt)}</span></span>
                <Chip kind={e.status}>{{ SENT: 'Enviado', FAILED: 'Fallido', PENDING: 'Pendiente' }[e.status]}</Chip>
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  );
}
