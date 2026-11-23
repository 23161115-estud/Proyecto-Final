import { useState } from 'react';
import FilterBar from './FilterBar';
import { Alert, Button, ConfirmDialog, EmptyState, ErrorState, Field, Modal, PageHead, Pagination, Skeleton } from './ui';
import { useToast } from '../context/ToastContext';
import useForm from '../hooks/useForm';
import usePaged from '../hooks/usePaged';
import { parseError } from '../utils/errors';

function FormModal({ config, row, onClose, onSaved }) {
  const { fields, entity, toValues, emptyValues, toPayload, api } = config;
  const editing = Boolean(row);
  const toast = useToast();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const active = fields.filter((f) => !(editing && f.onlyCreate));

  const form = useForm(editing ? toValues(row) : emptyValues, (v) =>
    Object.fromEntries(active.map((f) => [f.name, f.validate ? f.validate(v[f.name], v) : '']))
  );

  const submit = form.submit(async (v) => {
    setBusy(true);
    setError('');
    try {
      const payload = toPayload(v, editing);
      if (editing) await api.update(row.id, payload); else await api.create(payload);
      toast.success(editing ? `${entity} actualizado` : `${entity} creado`);
      onSaved();
    } catch (e) {
      const p = parseError(e);
      setError(p.message);
      form.setServerErrors(p.fields);
      setBusy(false);
    }
  });

  return (
    <Modal title={editing ? `Editar ${entity.toLowerCase()}` : `Nuevo ${entity.toLowerCase()}`} onClose={onClose}>
      <form className="stack" onSubmit={submit} noValidate>
        <Alert>{error}</Alert>
        <div className="grid cols-2">
          {active.map((f) => {
            const common = { key: f.name, label: f.label, suffix: f.suffix, placeholder: f.placeholder, ...form.bind(f.name) };
            if (f.type === 'select') {
              return (
                <Field {...common} as="select" style={f.full ? { gridColumn: '1 / -1' } : undefined}>
                  <option value="">Selecciona…</option>
                  {f.options.map(([v, l]) => <option key={v} value={v}>{l}</option>)}
                </Field>
              );
            }
            if (f.type === 'textarea') return <div key={f.name} style={{ gridColumn: '1 / -1' }}><Field {...common} as="textarea" /></div>;
            return (
              <div key={f.name} style={f.full ? { gridColumn: '1 / -1' } : undefined}>
                <Field {...common} type={f.type || 'text'} step={f.step} min={f.min} autoComplete="off" />
              </div>
            );
          })}
        </div>
        <div className="modal-actions">
          <Button type="button" variant="ghost" onClick={onClose}>Cancelar</Button>
          <Button type="submit" loading={busy}>{editing ? 'Guardar cambios' : `Crear ${entity.toLowerCase()}`}</Button>
        </div>
      </form>
    </Modal>
  );
}

export default function CrudPage({ config }) {
  const toast = useToast();
  const list = usePaged(config.api.list, config.initialFilters);
  const [editing, setEditing] = useState(null);
  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const saved = () => {
    setEditing(null);
    list.reload();
  };
  const remove = async () => {
    setDeleting(true);
    try {
      await config.api.remove(toDelete.id);
      toast.success(`${config.entity} eliminado`);
      setToDelete(null);
      if (list.data.content.length === 1 && list.page > 0) list.setPage(list.page - 1); else list.reload();
    } catch (e) {
      toast.error(parseError(e).message);
      setToDelete(null);
    } finally {
      setDeleting(false);
    }
  };

  const filtered = Object.values(list.filters).some((v) => v);

  return (
    <>
      <PageHead title={config.title} subtitle={config.subtitle}>
        <Button variant="accent" onClick={() => setEditing({})}>{config.newLabel}</Button>
      </PageHead>
      <FilterBar fields={config.filters} initial={config.initialFilters} onApply={list.apply} />

      {list.loading && !list.data ? <Skeleton /> : list.error ? <ErrorState error={list.error} onRetry={list.reload} /> : list.data.content.length === 0 ? (
        <EmptyState title={filtered ? 'Sin resultados' : 'Aún no hay registros'} text={filtered ? 'Prueba con otros filtros.' : `Crea el primer ${config.entity.toLowerCase()}.`} />
      ) : (
        <>
          <div className="table-wrap" style={{ opacity: list.loading ? 0.6 : 1 }}>
            <table>
              <thead>
                <tr>{config.columns.map((c) => <th key={c.label}>{c.label}</th>)}<th>Acciones</th></tr>
              </thead>
              <tbody>
                {list.data.content.map((row) => (
                  <tr key={row.id}>
                    {config.columns.map((c) => <td key={c.label}>{c.render(row)}</td>)}
                    <td>
                      <div className="actions">
                        <button className="link-btn" onClick={() => setEditing(row)}>Editar</button>
                        {config.removable && <button className="link-btn danger" onClick={() => setToDelete(row)}>Eliminar</button>}
                        {config.rowActions?.(row, { reload: list.reload, toast })}
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

      {editing && <FormModal config={config} row={editing.id ? editing : null} onClose={() => setEditing(null)} onSaved={saved} />}
      {toDelete && (
        <ConfirmDialog
          title={`¿Eliminar ${config.entity.toLowerCase()}?`}
          message={`Se eliminará "${config.describe(toDelete)}".`}
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
