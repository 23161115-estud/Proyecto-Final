import CrudPage from '../../components/CrudPage';
import { Chip } from '../../components/ui';
import { aiDataApi } from '../../api/services';
import { formatMoney } from '../../utils/format';
import { first, maxLen, range, required } from '../../utils/validators';

const config = {
  title: 'Dataset de IA',
  subtitle: 'Rentas reales que alimentan el análisis de cada simulación.',
  entity: 'Registro',
  newLabel: '+ Agregar registro',
  api: aiDataApi,
  removable: true,
  describe: (r) => `${r.neighborhoodName} · ${formatMoney(r.realMonthlyRent)}`,
  initialFilters: { q: '', transport: '', sort: 'recordedAt,desc' },
  filters: [
    { name: 'q', label: 'Colonia', placeholder: 'Ej. Reforma' },
    { name: 'transport', label: 'Transporte cercano', options: [['', 'Todos'], ['true', 'Con transporte'], ['false', 'Sin transporte']] },
    { name: 'sort', label: 'Ordenar por', options: [['recordedAt,desc', 'Más recientes'], ['rent,desc', 'Mayor renta'], ['rent,asc', 'Menor renta'], ['distance,asc', 'Más cerca de la universidad']] },
  ],
  columns: [
    { label: 'Colonia', render: (r) => <b>{r.neighborhoodName}</b> },
    { label: 'Departamento', render: (r) => `${Number(r.apartmentM2)} m²` },
    { label: 'Distancia a universidad', render: (r) => `${r.distanceToUniversityM.toLocaleString('es-MX')} m` },
    { label: 'Transporte cercano', render: (r) => <Chip kind={r.hasNearbyTransport ? 'ok' : 'gray'}>{r.hasNearbyTransport ? 'Sí' : 'No'}</Chip> },
    { label: 'Renta real', render: (r) => formatMoney(r.realMonthlyRent) },
  ],
  fields: [
    { name: 'neighborhoodName', label: 'Colonia', full: true, validate: (v) => first(required(v, 'La colonia es obligatoria'), maxLen(v, 100, 'La colonia')) },
    { name: 'apartmentM2', label: 'Tamaño del departamento', type: 'number', step: '0.01', suffix: 'm²', validate: (v) => first(required(v, 'Los m² son obligatorios'), range(v, 5, 999.99, 'El tamaño')) },
    { name: 'distanceToUniversityM', label: 'Distancia a la universidad', type: 'number', step: '1', suffix: 'm', validate: (v) => first(required(v, 'La distancia es obligatoria'), range(v, 0, 100000, 'La distancia', { integer: true })) },
    { name: 'hasNearbyTransport', label: 'Transporte cercano', type: 'select', options: [['true', 'Sí'], ['false', 'No']], validate: (v) => required(v, 'Indica si hay transporte cercano') },
    { name: 'realMonthlyRent', label: 'Renta mensual real', type: 'number', step: '0.01', suffix: '$', validate: (v) => first(required(v, 'La renta es obligatoria'), range(v, 1, 99999999, 'La renta')) },
  ],
  emptyValues: { neighborhoodName: '', apartmentM2: '', distanceToUniversityM: '', hasNearbyTransport: '', realMonthlyRent: '' },
  toValues: (r) => ({ neighborhoodName: r.neighborhoodName, apartmentM2: String(r.apartmentM2), distanceToUniversityM: String(r.distanceToUniversityM), hasNearbyTransport: String(r.hasNearbyTransport), realMonthlyRent: String(r.realMonthlyRent) }),
  toPayload: (v) => ({ neighborhoodName: v.neighborhoodName.trim(), apartmentM2: Number(v.apartmentM2), distanceToUniversityM: Number(v.distanceToUniversityM), hasNearbyTransport: v.hasNearbyTransport === 'true', realMonthlyRent: Number(v.realMonthlyRent) }),
};

export default function AiData() {
  return <CrudPage config={config} />;
}
