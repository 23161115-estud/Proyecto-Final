export function parseError(err) {
  if (!err.response) {
    return { network: true, status: 0, message: 'No se pudo conectar con el servidor. Revisa tu conexión a internet e intenta de nuevo.', fields: {} };
  }
  const data = err.response.data instanceof Blob ? {} : err.response.data || {};
  const status = err.response.status;
  let message = data.message || 'Ocurrió un error inesperado. Intenta de nuevo.';
  if (status >= 500 && !data.message) message = 'El servidor tuvo un problema. Intenta de nuevo en unos minutos.';
  return { network: false, status, message, fields: data.errors || {} };
}

export const HOME_BY_ROLE = { CLIENTE: '/simulations', ANALISTA: '/neighborhoods', ADMIN: '/admin' };
