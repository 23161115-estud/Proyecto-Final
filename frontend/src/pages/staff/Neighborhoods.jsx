import CrudPage from '../../components/CrudPage';
import { neighborhoodApi } from '../../api/services';
import { formatMoney } from '../../utils/format';
import { first, maxLen, range, required } from '../../utils/validators';

const config = {
  title: 'Colonias',
  subtitle: 'Costos y factor de zona usados en el cálculo.',
  entity: 'Colonia',
  newLabel: '+ Nueva colonia',
  api: neighborhoodApi,
  removable: true,
  describe: (r) => r.name,
  initialFilters: { q: '', sort: 'name,asc' },
  filters: [
    { name: 'q', label: 'Buscar colonia', placeholder: 'Ej. Jalatlaco' },
    { name: 'sort', label: 'Ordenar por', options: [['name,asc', 'Nombre (A-Z)'], ['name,desc', 'Nombre (Z-A)'], ['landCost,desc', 'Mayor costo de terreno'], ['landCost,asc', 'Menor costo de terreno']] },
  ],
  columns: [
    { label: 'Colonia', render: (r) => <b>{r.name}</b> },
    { label: 'Ciudad', render: (r) => r.city },
    { label: 'Terreno $/m²', render: (r) => formatMoney(r.averageLandCostM2) },
    { label: 'Construcción $/m²', render: (r) => formatMoney(r.constructionCostM2) },
    { label: 'Factor de zona', render: (r) => Number(r.zoneFactor).toFixed(2) },
  ],
  fields: [
    { name: 'name', label: 'Nombre', full: true, validate: (v) => first(required(v, 'El nombre es obligatorio'), maxLen(v, 100, 'El nombre')) },
    { name: 'city', label: 'Ciudad', full: true, validate: (v) => maxLen(v, 100, 'La ciudad') },
    { name: 'averageLandCostM2', label: 'Costo de terreno', type: 'number', step: '0.01', suffix: '$/m²', validate: (v) => first(required(v, 'El costo del terreno es obligatorio'), range(v, 1, 99999999, 'El costo')) },
    { name: 'constructionCostM2', label: 'Costo de construcción', type: 'number', step: '0.01', suffix: '$/m²', validate: (v) => first(required(v, 'El costo de construcción es obligatorio'), range(v, 1, 99999999, 'El costo')) },
    { name: 'zoneFactor', label: 'Factor de zona', type: 'number', step: '0.01', validate: (v) => first(required(v, 'El factor de zona es obligatorio'), range(v, 0.1, 9.99, 'El factor')) },
  ],
  emptyValues: { name: '', city: 'Oaxaca de Juárez', averageLandCostM2: '', constructionCostM2: '', zoneFactor: '1.00' },
  toValues: (r) => ({ name: r.name, city: r.city, averageLandCostM2: String(r.averageLandCostM2), constructionCostM2: String(r.constructionCostM2), zoneFactor: String(r.zoneFactor) }),
  toPayload: (v) => ({ name: v.name.trim(), city: v.city.trim() || null, averageLandCostM2: Number(v.averageLandCostM2), constructionCostM2: Number(v.constructionCostM2), zoneFactor: Number(v.zoneFactor) }),
};

export default function Neighborhoods() {
  return <CrudPage config={config} />;
}
