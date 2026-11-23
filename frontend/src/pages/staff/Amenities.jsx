import CrudPage from '../../components/CrudPage';
import { amenityApi } from '../../api/services';
import { first, maxLen, range, required } from '../../utils/validators';

const config = {
  title: 'Amenidades',
  subtitle: 'Cada amenidad suma un porcentaje al costo de construcción.',
  entity: 'Amenidad',
  newLabel: '+ Nueva amenidad',
  api: amenityApi,
  removable: true,
  describe: (r) => r.name,
  initialFilters: { q: '', sort: 'name,asc' },
  filters: [
    { name: 'q', label: 'Buscar amenidad', placeholder: 'Ej. Alberca' },
    { name: 'sort', label: 'Ordenar por', options: [['name,asc', 'Nombre (A-Z)'], ['extraCost,desc', 'Mayor costo extra'], ['extraCost,asc', 'Menor costo extra']] },
  ],
  columns: [
    { label: 'Nombre', render: (r) => <b>{r.name}</b> },
    { label: 'Descripción', render: (r) => r.description || '—' },
    { label: 'Costo extra', render: (r) => `${Number(r.extraCostPercentage).toFixed(2)}%` },
  ],
  fields: [
    { name: 'name', label: 'Nombre', full: true, validate: (v) => first(required(v, 'El nombre es obligatorio'), maxLen(v, 80, 'El nombre')) },
    { name: 'description', label: 'Descripción', type: 'textarea', validate: (v) => maxLen(v, 255, 'La descripción') },
    { name: 'extraCostPercentage', label: 'Costo extra', type: 'number', step: '0.01', suffix: '%', validate: (v) => first(required(v, 'El porcentaje es obligatorio'), range(v, 0, 99.99, 'El porcentaje')) },
  ],
  emptyValues: { name: '', description: '', extraCostPercentage: '' },
  toValues: (r) => ({ name: r.name, description: r.description || '', extraCostPercentage: String(r.extraCostPercentage) }),
  toPayload: (v) => ({ name: v.name.trim(), description: v.description.trim() || null, extraCostPercentage: Number(v.extraCostPercentage) }),
};

export default function Amenities() {
  return <CrudPage config={config} />;
}
