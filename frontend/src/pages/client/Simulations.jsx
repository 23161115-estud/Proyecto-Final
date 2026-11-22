import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { simApi } from '../../api/services';
import FilterBar from '../../components/FilterBar';
import { Button, Chip, ConfirmDialog, EmptyState, ErrorState, PageHead, Pagination, Skeleton } from '../../components/ui';
import { useToast } from '../../context/ToastContext';
import usePaged from '../../hooks/usePaged';
import { formatDate, formatMonths, formatPct } from '../../utils/format';
import { parseError } from '../../utils/errors';

const FILTERS = [
  { name: 'q', label: 'Buscar colonia', placeholder: 'Ej. Reforma' },
  { name: 'semaphore', label: 'Semáforo', options: [['', 'Todos'], ['VERDE', 'Verde'], ['AMARILLO', 'Amarillo'], ['ROJO', 'Rojo']] },
  { name: 'sort', label: 'Ordenar por', options: [['createdAt,desc', 'Más recientes'], ['createdAt,asc', 'Más antiguas'], ['roi,desc', 'Mayor ROI'], ['roi,asc', 'Menor ROI']] },
];
const INITIAL = { q: '', semaphore: '', sort: 'createdAt,desc' };

export default function Simulations() {
  const navigate = useNavigate();
  const toast = useToast();
  const list = usePaged(simApi.list, INITIAL);
  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const remove = async () => {
    setDeleting(true);
    try {
      await simApi.remove(toDelete.id);
      toast.success('Simulación eliminada');
      setToDelete(null);
      if (list.data.content.length === 1 && list.page > 0) list.setPage(list.page - 1);
      else list.reload();
    } catch (e) {
      toast.error(parseError(e).message);
    } finally {
      setDeleting(false);
    }
  };

  const filtered = Object.entries(list.filters).some(([k, v]) => v && k !== 'sort');

  return (
    <>
      <PageHead title="Mis simulaciones" subtitle={list.data ? `${list.data.totalElements} análisis de inversión guardados.` : 'Historial de tus análisis de inversión.'}>
        <Link className="btn btn-accent" to="/simulations/new">+ Nueva simulación</Link>
      </PageHead>
      <FilterBar fields={FILTERS} initial={INITIAL} onApply={list.apply} />

      {list.loading && !list.data ? <Skeleton /> : list.error ? <ErrorState error={list.error} onRetry={list.reload} /> : list.data.content.length === 0 ? (
        <EmptyState
          title={filtered ? 'Sin resultados' : 'Aún no tienes simulaciones'}
          text={filtered ? 'Prueba con otros filtros.' : 'Calcula la rentabilidad de tu primer proyecto.'}
          action={!filtered && <Link className="btn btn-accent" to="/simulations/new">Crear simulación</Link>}
        />
      ) : (
        <>
          <div className="table-wrap" style={{ opacity: list.loading ? 0.6 : 1 }}>
            <table>
              <thead>
                <tr><th>Colonia</th><th>Terreno</th><th>Deptos</th><th>ROI</th><th>Recuperación</th><th>Semáforo</th><th>Fecha</th><th>Acciones</th></tr>
              </thead>
              <tbody>
                {list.data.content.map((s) => (
                  <tr key={s.id}>
                    <td><b>{s.neighborhoodName}</b></td>
                    <td>{Number(s.totalArea)} m²</td>
                    <td>{s.numApartments}</td>
                    <td>{formatPct(s.roi)}</td>
                    <td>{formatMonths(s.paybackMonths)}</td>
                    <td><Chip kind={s.semaphore} /></td>
                    <td>{formatDate(s.createdAt)}</td>
                    <td>
                      <div className="actions">
                        <button className="link-btn" onClick={() => navigate(`/simulations/${s.id}`)}>Ver</button>
                        <button className="link-btn danger" onClick={() => setToDelete(s)}>Eliminar</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination data={list.data} onPage={list.setPage} />
        </>
      )}

      {toDelete && (
        <ConfirmDialog
          title="¿Eliminar esta simulación?"
          message={`Se eliminará la simulación de ${toDelete.neighborhoodName}.`}
          detail="Esta acción no se puede deshacer."
          confirmLabel="Eliminar"
          loading={deleting}
          onConfirm={remove}
          onCancel={() => setToDelete(null)}
        />
      )}
    </>
  );
}
