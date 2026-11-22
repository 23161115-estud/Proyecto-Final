import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { amenityApi, neighborhoodApi, simApi } from '../../api/services';
import { Alert, Button, ErrorState, Field, PageHead, Skeleton } from '../../components/ui';
import useForm from '../../hooks/useForm';
import { formatMoney } from '../../utils/format';
import { parseError } from '../../utils/errors';
import { first, range, required } from '../../utils/validators';

export default function NewSimulation() {
  const navigate = useNavigate();
  const [catalog, setCatalog] = useState(null);
  const [loadError, setLoadError] = useState(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const load = () => {
    setLoadError(null);
    Promise.all([neighborhoodApi.options(), amenityApi.options()])
      .then(([neighborhoods, amenities]) => setCatalog({ neighborhoods, amenities }))
      .catch((e) => setLoadError(parseError(e)));
  };
  useEffect(load, []);

  const form = useForm(
    { neighborhoodId: '', landWidth: '', landLength: '', numApartments: '', constructionCostPerM2: '', monthlyRentPerApartment: '', fixedMonthlyExpenses: '', amenityIds: [] },
    (v) => ({
      neighborhoodId: required(v.neighborhoodId, 'Elige una colonia'),
      landWidth: first(required(v.landWidth, 'El ancho es obligatorio'), range(v.landWidth, 1, 500, 'El ancho')),
      landLength: first(required(v.landLength, 'El largo es obligatorio'), range(v.landLength, 1, 500, 'El largo')),
      numApartments: first(required(v.numApartments, 'El número de departamentos es obligatorio'), range(v.numApartments, 1, 200, 'El número de departamentos', { integer: true })),
      constructionCostPerM2: first(required(v.constructionCostPerM2, 'El costo de construcción es obligatorio'), range(v.constructionCostPerM2, 100, 100000, 'El costo')),
      monthlyRentPerApartment: first(required(v.monthlyRentPerApartment, 'La renta es obligatoria'), range(v.monthlyRentPerApartment, 1, 1000000, 'La renta')),
      fixedMonthlyExpenses: first(required(v.fixedMonthlyExpenses, 'Los gastos fijos son obligatorios'), range(v.fixedMonthlyExpenses, 0, 10000000, 'Los gastos')),
    })
  );
  const { values } = form;

  const onNeighborhood = (e) => {
    form.bind('neighborhoodId').onChange(e);
    const n = catalog.neighborhoods.find((x) => String(x.id) === e.target.value);
    if (n && !values.constructionCostPerM2) form.setValue('constructionCostPerM2', String(n.constructionCostM2));
  };
  const toggleAmenity = (id) => {
    const set = new Set(values.amenityIds);
    if (set.has(id)) set.delete(id); else set.add(id);
    form.setValue('amenityIds', [...set]);
  };

  const area = Math.max(0, Number(values.landWidth) * Number(values.landLength)) || 0;
  const base = area * (Number(values.constructionCostPerM2) || 0);
  const pct = catalog ? catalog.amenities.filter((a) => values.amenityIds.includes(a.id)).reduce((s, a) => s + Number(a.extraCostPercentage), 0) : 0;
  const extra = base * (pct / 100);

  const onSubmit = form.submit(async (v) => {
    setBusy(true);
    setError('');
    try {
      const created = await simApi.create({
        neighborhoodId: Number(v.neighborhoodId),
        landWidth: Number(v.landWidth),
        landLength: Number(v.landLength),
        numApartments: Number(v.numApartments),
        constructionCostPerM2: Number(v.constructionCostPerM2),
        monthlyRentPerApartment: Number(v.monthlyRentPerApartment),
        fixedMonthlyExpenses: Number(v.fixedMonthlyExpenses),
        amenityIds: v.amenityIds,
      });
      navigate(`/simulations/${created.id}`);
    } catch (e) {
      const p = parseError(e);
      setError(p.message);
      form.setServerErrors(p.fields);
      setBusy(false);
    }
  });

  if (loadError) return <ErrorState error={loadError} onRetry={load} />;
  if (!catalog) return <Skeleton rows={8} />;

  return (
    <>
      <PageHead title="Nueva simulación" subtitle="Captura los datos del terreno y el proyecto." />
      <form className="split-400" onSubmit={onSubmit} noValidate>
        <div className="card stack">
          <Alert>{error}</Alert>
          <h2 style={{ margin: 0 }}>Terreno</h2>
          <div className="grid cols-3" style={{ gridTemplateColumns: '2fr 1fr 1fr' }}>
            <Field as="select" label="Colonia" {...form.bind('neighborhoodId')} onChange={onNeighborhood}>
              <option value="">Selecciona una colonia</option>
              {catalog.neighborhoods.map((n) => <option key={n.id} value={n.id}>{n.name}</option>)}
            </Field>
            <Field label="Ancho" type="number" step="0.01" min="1" suffix="m" {...form.bind('landWidth')} />
            <Field label="Largo" type="number" step="0.01" min="1" suffix="m" {...form.bind('landLength')} />
          </div>
          <h2 style={{ margin: '8px 0 0' }}>Proyecto</h2>
          <div className="grid cols-2">
            <Field label="Número de departamentos" type="number" min="1" step="1" {...form.bind('numApartments')} />
            <Field label="Costo de construcción" type="number" step="0.01" min="100" suffix="$/m²" {...form.bind('constructionCostPerM2')} />
            <Field label="Renta mensual por departamento" type="number" step="0.01" min="1" suffix="$" {...form.bind('monthlyRentPerApartment')} />
            <Field label="Gastos fijos mensuales" type="number" step="0.01" min="0" suffix="$/mes" {...form.bind('fixedMonthlyExpenses')} />
          </div>
          <h2 style={{ margin: '8px 0 0' }}>Amenidades</h2>
          <div className="chips-select" role="group" aria-label="Amenidades">
            {catalog.amenities.map((a) => {
              const on = values.amenityIds.includes(a.id);
              return (
                <button type="button" key={a.id} className="chip-toggle" aria-pressed={on} onClick={() => toggleAmenity(a.id)} title={`+${a.extraCostPercentage}% al costo`}>
                  {on && '✓'} {a.name}
                </button>
              );
            })}
          </div>
          <div className="row" style={{ marginTop: 8 }}>
            <Button type="submit" variant="accent" loading={busy}>{busy ? 'Calculando…' : 'Calcular rentabilidad'}</Button>
            <Link className="btn btn-ghost" to="/simulations">Cancelar</Link>
          </div>
        </div>

        <aside className="stack">
          <div className="card">
            <h2>Resumen en vivo</h2>
            <div className="summary-row"><span>Área total</span><b>{area ? `${area.toFixed(2)} m²` : '—'}</b></div>
            <div className="summary-row"><span>Construcción</span><b>{formatMoney(base)}</b></div>
            <div className="summary-row"><span>Amenidades (+{pct.toFixed(2)}%)</span><b>{formatMoney(extra)}</b></div>
            <div className="summary-row total"><span>Costo estimado</span><b>{formatMoney(base + extra)}</b></div>
          </div>
          <div className="alert alert-info">El cálculo final y el análisis con IA se generan al guardar. El resultado queda en tu historial.</div>
        </aside>
      </form>
    </>
  );
}
