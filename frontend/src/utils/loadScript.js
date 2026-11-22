const cache = new Map();

export default function loadScript(src) {
  if (cache.has(src)) return cache.get(src);
  const p = new Promise((resolve, reject) => {
    const el = document.createElement('script');
    el.src = src;
    el.async = true;
    el.defer = true;
    el.onload = () => resolve();
    el.onerror = () => {
      cache.delete(src);
      el.remove();
      reject(new Error(`No se pudo cargar ${src}`));
    };
    document.head.appendChild(el);
  });
  cache.set(src, p);
  return p;
}
