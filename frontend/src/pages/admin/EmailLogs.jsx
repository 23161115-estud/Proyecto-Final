import { adminApi } from '../../api/services';
import FilterBar from '../../components/FilterBar';
import { Chip, EmptyState, ErrorState, PageHead, Pagination, Skeleton } from '../../components/ui';
import usePaged from '../../hooks/usePaged';
import { formatDateTime } from '../../utils/format';

const TYPES = { VERIFICATION: 'Verificación', PDF_REPORT: 'Reporte PDF', WELCOME: 'Bienvenida', PASSWORD_RESET: 'Recuperar contraseña' };
const STATUS = { SENT: 'Enviado', FAILED: 'Fallido', PENDING: 'Pendiente' };
const FILTERS = [
  { name: 'q', label: 'Buscar destinatario', placeholder: 'Ej. ana@test.com' },
  { name: 'type', label: 'Tipo', options: [['', 'Todos'], ...Object.entries(TYPES)] },
  { name: 'status', label: 'Estado', options: [['', 'Todos'], ...Object.entries(STATUS)] },
];
const INITIAL = { q: '', type: '', status: '' };

export default function EmailLogs() {
  const list = usePaged(adminApi.emailLogs, INITIAL);
  return (
    <>
      <PageHead title="Logs de correo" subtitle="Auditoría de envíos del sistema." />
      <FilterBar fields={FILTERS} initial={INITIAL} onApply={list.apply} />
      {list.loading && !list.data ? <Skeleton /> : list.error ? <ErrorState error={list.error} onRetry={list.reload} /> : list.data.content.length === 0 ? (
        <EmptyState title="Sin registros" text="No hay correos que coincidan con los filtros." />
      ) : (
        <>
          <div className="table-wrap" style={{ opacity: list.loading ? 0.6 : 1 }}>
            <table>
              <thead><tr><th>Destinatario</th><th>Tipo</th><th>Estado</th><th>Fecha</th><th>Error</th></tr></thead>
              <tbody>
                {list.data.content.map((l) => (
                  <tr key={l.id}>
                    <td><b>{l.recipientEmail}</b></td>
                    <td><Chip kind="info">{TYPES[l.emailType] || l.emailType}</Chip></td>
                    <td><Chip kind={l.status}>{STATUS[l.status] || l.status}</Chip></td>
                    <td>{formatDateTime(l.sentAt)}</td>
                    <td>{l.errorMessage || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination data={list.data} onPage={list.setPage} />
        </>
      )}
    </>
  );
}
