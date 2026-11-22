import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { simApi } from '../../api/services';
import { Alert, Button, Chip, ErrorState, Field, Modal, PageHead, Skeleton } from '../../components/ui';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import useForm from '../../hooks/useForm';
import { formatDate, formatMoney, formatMonths, formatPct } from '../../utils/format';
import { parseError } from '../../utils/errors';
import { phone, required } from '../../utils/validators';

const COPY = {
  VERDE: ['VERDE', 'Inversión rentable', 'ROI de 15% o más al año'],
  AMARILLO: ['AMARILLO', 'Rentabilidad media', 'ROI entre 8% y 15% al año'],
  ROJO: ['ROJO', 'No recomendada', 'ROI menor a 8% al año'],
};

function PhoneDialog({ channel, onSend, onClose }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const form = useForm({ phone: '' }, (v) => ({ phone: required(v.phone, 'El teléfono es obligatorio') || phone(v.phone) }));
  const submit = form.submit(async (v) => {
    setBusy(true);
    setError('');
    try {
      await onSend(v.phone.trim());
      onClose();
    } catch (e) {
      setError(parseError(e).message);
      setBusy(false);
    }
  });
  return (
    <Modal title={`Enviar por ${channel}`} subtitle="Aún no tienes un teléfono en tu perfil. Escríbelo para enviar el resultado." onClose={onClose}>
      <form className="stack" onSubmit={submit} noValidate>
        <Alert>{error}</Alert>
        <Field label="Teléfono" type="tel" placeholder="+529511234567" {...form.bind('phone')} />
        <div className="modal-actions">
          <Button type="button" variant="ghost" onClick={onClose}>Cancelar</Button>
          <Button type="submit" loading={busy}>Enviar</Button>
        </div>
      </form>
    </Modal>
  );
}

export default function SimulationResult() {
  const { id } = useParams();
  const { user } = useAuth();
  const toast = useToast();
  const [sim, setSim] = useState(null);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState('');
  const [phoneFor, setPhoneFor] = useState(null);

  const load = () => {
    setError(null);
    simApi.get(id).then(setSim).catch((e) => setError(parseError(e)));
  };
  useEffect(load, [id]);

  const run = async (key, fn, okMsg) => {
    setBusy(key);
    try {
      const res = await fn();
      toast.success(okMsg || res?.message || 'Listo');
    } catch (e) {
      toast.error(parseError(e).message);
    } finally {
      setBusy('');
    }
  };

  const downloadPdf = () =>
    run('pdf', async () => {
      const blob = await simApi.pdf(id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `oaxinvest-simulacion-${id}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    }, 'PDF descargado');

  const sendPhone = (channel) => {
    const fn = (phoneNumber) => (channel === 'sms' ? simApi.sendSms(id, phoneNumber) : simApi.sendWhatsapp(id, phoneNumber));
    if (user.phone) return run(channel, () => fn(undefined));
    setPhoneFor({ channel, fn });
  };

  if (error) return <ErrorState error={error} onRetry={error.status === 404 ? undefined : load} />;
  if (!sim) return <Skeleton rows={8} />;
  const [label, title, rule] = COPY[sim.semaphore];

  return (
    <>
      <Link to="/simulations" className="back">‹ Mis simulaciones</Link>
      <PageHead title={`Resultado · ${sim.neighborhoodName}`} subtitle={`Simulación #${sim.id} · ${formatDate(sim.createdAt)}`} />

      <div className="result-grid">
        <div className="semaphore-card">
          <span style={{ color: '#cfe3da', fontWeight: 600 }}>Semáforo de inversión</span>
          <div className="semaphore-main">
            <div className={`light ${sim.semaphore}`} role="img" aria-label={`Semáforo ${label}`} />
            <div><h2>{label}</h2><p>{title}</p></div>
          </div>
          <span style={{ color: '#cfe3da', fontSize: 13 }}>{rule}</span>
        </div>
        <div className="grid cols-2">
          <div className="card kpi" style={{ '--c': 'var(--green)' }}><span>ROI anual</span><b>{formatPct(sim.roi)}</b></div>
          <div className="card kpi"><span>Recuperación</span><b>{formatMonths(sim.paybackMonths)}</b></div>
          <div className="card kpi"><span>Costo total</span><b>{formatMoney(sim.totalCost)}</b></div>
          <div className="card kpi" style={{ '--c': 'var(--gold)' }}><span>Ingreso neto anual</span><b>{formatMoney(sim.annualIncome)}</b></div>
        </div>
      </div>

      <div className="grid" style={{ marginTop: 24 }}>
        <div className="card">
          <h2>Análisis</h2>
          <p style={{ maxWidth: 900 }}>{sim.aiAnalysis || 'Sin análisis disponible.'}</p>
        </div>
        <div className="card">
          <h2>Datos de la simulación</h2>
          <div className="grid cols-3">
            <div className="summary-row"><span>Terreno</span><b>{Number(sim.landWidth)} × {Number(sim.landLength)} m ({Number(sim.totalArea)} m²)</b></div>
            <div className="summary-row"><span>Departamentos</span><b>{sim.numApartments}</b></div>
            <div className="summary-row"><span>Construcción por m²</span><b>{formatMoney(sim.constructionCostPerM2)}</b></div>
            <div className="summary-row"><span>Renta por departamento</span><b>{formatMoney(sim.monthlyRentPerApartment)}</b></div>
            <div className="summary-row"><span>Gastos fijos mensuales</span><b>{formatMoney(sim.fixedMonthlyExpenses)}</b></div>
          </div>
          <div className="row" style={{ marginTop: 14 }}>
            <span className="muted">Amenidades:</span>
            {sim.amenities.length ? sim.amenities.map((a) => <Chip key={a.id} kind="info">{a.name}</Chip>) : <span>Ninguna</span>}
          </div>
        </div>
        <div className="card">
          <h2>Compartir resultado</h2>
          <div className="row">
            <Button onClick={downloadPdf} loading={busy === 'pdf'}>Descargar PDF</Button>
            <Button variant="soft" onClick={() => run('email', () => simApi.sendEmail(id))} loading={busy === 'email'}>Enviar por correo</Button>
            <Button variant="soft" onClick={() => sendPhone('sms')} loading={busy === 'sms'}>Enviar por SMS</Button>
            <Button variant="accent" onClick={() => sendPhone('whatsapp')} loading={busy === 'whatsapp'}>Enviar por WhatsApp</Button>
          </div>
        </div>
      </div>

      {phoneFor && (
        <PhoneDialog
          channel={phoneFor.channel === 'sms' ? 'SMS' : 'WhatsApp'}
          onSend={async (p) => { const r = await phoneFor.fn(p); toast.success(r.message); }}
          onClose={() => setPhoneFor(null)}
        />
      )}
    </>
  );
}
