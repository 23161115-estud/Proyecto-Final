# Plan de commits

Reparto de la subida del proyecto para que los dos integrantes tengan la misma cantidad de commits y casi las mismas líneas. Los pasos se hacen **en orden y alternando**: Emiliano hace los impares y Francisco los pares.

## Resumen

| Integrante | Commits | Archivos | Líneas | Porcentaje |
|---|---|---|---|---|
| Emiliano | 15 | 92 | 3640 | 48.5 % |
| Francisco | 15 | 67 | 3861 | 51.5 % |

## Pasos

| Paso | Quién | Mensaje del commit | Archivos | Líneas |
|---|---|---|---|---|
| 1 | Emiliano | Agrega estructura base del repositorio y configuración de Maven | 6 | 147 |
| 2 | Francisco | Agrega base del frontend con Vite, React y estilos del diseño | 10 | 274 |
| 3 | Emiliano | Agrega esquema MySQL con migración Flyway y script de creación de la base | 2 | 133 |
| 4 | Francisco | Agrega catálogos iniciales de colonias, amenidades y dataset de IA | 7 | 197 |
| 5 | Emiliano | Agrega entidades y repositorios de usuarios, roles y tokens | 9 | 222 |
| 6 | Francisco | Agrega entidades de simulaciones y reportes PDF | 5 | 163 |
| 7 | Emiliano | Agrega manejo centralizado de errores y patrones de validación | 7 | 158 |
| 8 | Francisco | Agrega DTOs de simulaciones, catálogos, administración y paginación | 5 | 202 |
| 9 | Emiliano | Agrega seguridad con JWT, BCrypt, filtro de autenticación y limpieza de tokens | 10 | 369 |
| 10 | Francisco | Agrega cálculo de ROI, recuperación y semáforo, y análisis de IA con respaldo local | 2 | 137 |
| 11 | Emiliano | Agrega registro, login, logout y recuperación de contraseña | 4 | 303 |
| 12 | Francisco | Agrega API de simulaciones con paginación y filtros del lado del servidor | 2 | 291 |
| 13 | Emiliano | Agrega perfil de usuario con foto y cambio de contraseña | 2 | 139 |
| 14 | Francisco | Agrega generación del PDF de la simulación | 1 | 116 |
| 15 | Emiliano | Agrega envío de correo con JavaMailSender y bitácora de correos | 5 | 173 |
| 16 | Francisco | Agrega API de catálogos de colonias, amenidades y dataset de IA | 4 | 315 |
| 17 | Emiliano | Agrega envío de SMS y WhatsApp con Twilio | 4 | 146 |
| 18 | Francisco | Agrega API del panel de administración: usuarios, dashboard y logs | 4 | 287 |
| 19 | Emiliano | Agrega datos de prueba con DataSeeder por rol | 1 | 158 |
| 20 | Francisco | Agrega cliente HTTP, servicios de API, validadores y hooks del frontend | 9 | 388 |
| 21 | Emiliano | Agrega inicio de sesión con Google y Facebook en el backend | 1 | 156 |
| 22 | Francisco | Agrega navegación y rutas de la aplicación | 2 | 133 |
| 23 | Emiliano | Agrega colección de Bruno con pruebas de éxito y de error | 26 | 553 |
| 24 | Francisco | Agrega pantallas de simulación: nueva, resultado e historial | 3 | 389 |
| 25 | Emiliano | Agrega sesión, rutas protegidas y pantallas de login, registro y recuperación | 8 | 346 |
| 26 | Francisco | Agrega componente CrudPage y pantallas de catálogos del analista | 4 | 264 |
| 27 | Emiliano | Agrega botones de Google y Facebook y pantalla de perfil | 3 | 315 |
| 28 | Francisco | Agrega pantallas del panel de administración | 3 | 215 |
| 29 | Emiliano | Agrega guía de despliegue en el VPS y README del backend | 4 | 322 |
| 30 | Francisco | Agrega README del proyecto, README del frontend, división del trabajo y tablero | 6 | 490 |

## Preparación
1. Cada uno descomprime `oaxinvest.zip` en una carpeta de su computadora, por ejemplo `proyecto-completo`. Esa carpeta solo se usa como origen de los archivos; no es el repositorio.
2. Cada uno clona el repositorio en otra carpeta y configura su identidad de Git:
   ```powershell
   git clone https://github.com/23161115-estud/Proyecto-Final.git
   cd Proyecto-Final
   git config user.name "Su nombre completo"
   git config user.email "el correo de su cuenta de GitHub"
   ```
3. Los dos deben tener permiso de escritura en el repositorio (el script `tablero/crear-tablero.ps1` los agrega como colaboradores).

## Cómo hacer cada paso
Desde la carpeta del repositorio clonado:

```powershell
..\proyecto-completo\commits\aplicar-paso.ps1 -Paso 1 -Yo Emiliano -Origen ..\proyecto-completo
```

El script copia al repositorio solo los archivos de ese paso, hace el commit con el mensaje de la tabla y lo sube. Antes de empezar descarga lo que el otro ya subió. Si el paso le toca al otro integrante, se detiene.

Orden de trabajo: Emiliano hace el paso 1 y avisa; Francisco hace el paso 2 y avisa; y así hasta el 30. No hace falta hacerlos todos el mismo día.

## Reglas
- Cada quien usa su propia cuenta de GitHub y su propia computadora.
- No cambien las fechas de los commits (`--date`) ni reescriban el historial.
- El historial mostrará la fecha en que realmente se subió cada paso.
- Para cumplir con "historial a lo largo de todo el proyecto", sigan haciendo commits reales después del paso 30: correcciones de pruebas, configuración de Twilio, enlace de Figma, dominio, despliegue en el VPS y ajustes del README.
- Los pasos son el orden recomendado de subida; si hay que corregir algo, se corrige en un commit nuevo.
