import { useCallback, useEffect, useRef, useState } from 'react';
import { parseError } from '../utils/errors';

export default function usePaged(fetcher, initialFilters = {}, size = 10) {
  const [state, setState] = useState({ filters: initialFilters, page: 0 });
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [tick, setTick] = useState(0);
  const req = useRef(0);

  useEffect(() => {
    const id = ++req.current;
    setLoading(true);
    setError(null);
    fetcher({ ...state.filters, page: state.page, size })
      .then((d) => {
        if (id === req.current) setData(d);
      })
      .catch((e) => {
        if (id === req.current) setError(parseError(e));
      })
      .finally(() => {
        if (id === req.current) setLoading(false);
      });
  }, [state, tick, size]);

  const apply = useCallback((filters) => setState({ filters, page: 0 }), []);
  const setPage = useCallback((page) => setState((s) => ({ ...s, page })), []);
  const reload = useCallback(() => setTick((t) => t + 1), []);
  return { data, loading, error, filters: state.filters, page: state.page, setPage, apply, reload };
}
