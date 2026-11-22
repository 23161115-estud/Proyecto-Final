import { useState } from 'react';
import { Button, Field } from './ui';

export default function FilterBar({ fields, initial, onApply }) {
  const [values, setValues] = useState(initial);
  const set = (name) => (e) => setValues((v) => ({ ...v, [name]: e.target.value }));
  const clear = () => {
    setValues(initial);
    onApply(initial);
  };
  return (
    <form className="card filters" onSubmit={(e) => { e.preventDefault(); onApply(values); }} role="search">
      {fields.map((f) =>
        f.options ? (
          <Field key={f.name} as="select" label={f.label} value={values[f.name]} onChange={set(f.name)}>
            {f.options.map(([v, l]) => <option key={v} value={v}>{l}</option>)}
          </Field>
        ) : (
          <Field key={f.name} label={f.label} placeholder={f.placeholder} value={values[f.name]} onChange={set(f.name)} maxLength={100} />
        )
      )}
      <Button type="submit">Filtrar</Button>
      <Button type="button" variant="ghost" onClick={clear}>Limpiar</Button>
    </form>
  );
}
