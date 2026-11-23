# OaxInvest · Frontend (React + Vite)

Interfaz del simulador de rentabilidad de departamentos en Oaxaca. Consume la API Spring Boot (`/api/...`) y sigue el diseño de Figma (paleta verde cantera / grana cochinilla / cempasúchil, tipografía Plus Jakarta Sans).

> Completa antes de entregar: URL del proyecto `https://TU-DOMINIO.com` · Figma `PEGA_AQUI` · GitHub Projects `PEGA_AQUI`

## Requisitos y arranque
Node 18+ y el backend corriendo en `http://localhost:8080`.
```bash
npm install
npm run dev          # http://localhost:5173  (Vite reenvía /api y /uploads al backend, sin CORS)
npm run build        # genera dist/ para producción
```
`VITE_API_URL` (opcional, ver `.env.example`): déjalo **vacío** si Nginx sirve el frontend y la API bajo el mismo dominio (es el caso del despliegue en Vultr de `docs/DEPLOY.md` del backend).

## Estructura
```
src/
  api/          axios.js (JWT en cada petición, 401 -> sesión expirada) y services.js (un objeto por módulo)
  context/      AuthContext (sesión, login/logout), ToastContext (avisos)
  hooks/        useForm (validación en vivo), usePaged (paginación y filtros en el servidor)
  components/   Navbar, Layout, Guards (rutas por rol), CrudPage (CRUD genérico), FilterBar, ui.jsx (Field, Modal, Pagination...)
  pages/        auth/ · client/ · staff/ · admin/
  utils/        validators (mismas reglas que Bean Validation), format, errors
```

## Pantallas por rol
| Rol | Rutas |
|---|---|
| Público | `/login`, `/register`, `/forgot-password`, `/reset-password?token=`, `/verify-email?token=` |
| CLIENTE | `/simulations` (historial), `/simulations/new`, `/simulations/:id` (resultado, PDF, correo, SMS, WhatsApp), `/profile` |
| ANALISTA | `/neighborhoods`, `/amenities`, `/ai-data`, `/profile` |
| ADMIN | `/admin` (dashboard), `/admin/users`, `/admin/email-logs`, más colonias, amenidades y dataset IA |
| Errores | `/forbidden` (403) y cualquier ruta desconocida (404) |

Cada rol ve su propio navbar; entrar a una ruta de otro rol redirige a 403, y sin sesión a `/login`.

## Cómo cumple la rúbrica
- **Navbar** con nombre, correo, avatar, rol y "Salir" (que invalida el JWT en el servidor).
- **Validación en tiempo real** en todos los formularios, con el mensaje de error en rojo bajo el input; los errores 400 del backend por campo también se muestran bajo su input.
- **Tablas** con filtros, paginación y orden **en el servidor** (`?page=&size=&sort=&q=...`): nunca se descarga todo.
- **Estados de carga, vacío y error** (incluido "sin conexión con el servidor" con botón Reintentar), y confirmación antes de eliminar o desactivar.
- Responsivo (menú hamburguesa en móvil, tablas con scroll horizontal), foco visible y `aria-*` en campos y modales.

## Despliegue (Vultr)
```bash
npm ci && npm run build
sudo cp -r dist/* /var/www/oaxinvest/
```
La configuración de Nginx (SPA con `try_files`, proxy de `/api/` y `/uploads/`, HTTPS con Certbot) está en `docs/DEPLOY.md` del backend, sección 4.

## Diferencias con el Figma
- **Login con Google/Facebook:** se agregó en Login y Registro (`components/SocialButtons.jsx`). Los botones aparecen solo si el servidor tiene configuradas las claves (`GET /api/auth/social-config`); no hace falta ninguna variable `VITE_`. Google usa su botón oficial (Google Identity Services) y Facebook el SDK de JavaScript.
- **KPIs del historial del cliente** (ROI promedio, mejor colonia): se omitieron; la API no entrega agregados y calcularlos con una sola página sería engañoso. Se muestra el total de simulaciones.
- **Navbar de ADMIN:** se agregó "Dataset IA" y "Perfil" a los enlaces del diseño.
