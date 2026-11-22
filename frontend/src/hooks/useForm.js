import { useState } from 'react';

export default function useForm(initial, validate) {
  const [values, setValues] = useState(initial);
  const [touched, setTouched] = useState({});
  const [serverErrors, setServerErrors] = useState({});

  const errors = Object.fromEntries(Object.entries(validate(values)).filter(([, m]) => m));

  const setValue = (name, value) => {
    setValues((v) => ({ ...v, [name]: value }));
    setTouched((t) => ({ ...t, [name]: true }));
    setServerErrors((s) => {
      if (!s[name]) return s;
      const rest = { ...s };
      delete rest[name];
      return rest;
    });
  };
  const onChange = (e) => {
    const { name, type, value, checked } = e.target;
    setValue(name, type === 'checkbox' ? checked : value);
  };
  const onBlur = (e) => setTouched((t) => ({ ...t, [e.target.name]: true }));
  const fieldError = (name) => (touched[name] && errors[name]) || serverErrors[name] || '';
  const bind = (name) => ({ name, value: values[name] ?? '', onChange, onBlur, error: fieldError(name) });

  const submit = (onValid) => async (e) => {
    if (e) e.preventDefault();
    setTouched(Object.fromEntries(Object.keys(values).map((k) => [k, true])));
    if (Object.keys(errors).length > 0) return;
    await onValid(values);
  };

  return { values, setValue, bind, submit, errors, setServerErrors };
}
