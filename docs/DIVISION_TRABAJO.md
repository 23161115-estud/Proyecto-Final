# División del trabajo y tablero

## Integrantes
| Integrante | Nombre corto en el tablero |
|---|---|
| Ruiz Durán Emiliano | Emiliano |
| Bautista Centeno Francisco Elías | Francisco |

## Criterio de la división
Cada quien es dueño de **cortes completos** del sistema (base de datos, backend y pantalla del mismo tema), no de "todo el backend" o "todo el frontend". Así los dos entienden y pueden defender todo el flujo, y los dos aparecen con commits en `backend/` y en `frontend/`.

## Emiliano: acceso, comunicación y servidor
| Área | Qué incluye |
|---|---|
| Base de datos | Esquema MySQL, Flyway, datos de prueba, diagrama ER y respaldo `.sql` |
| Seguridad | Registro, login y logout con JWT, BCrypt, roles, validaciones, DTOs y `@RestControllerAdvice` |
| Acceso con terceros | Login con Google y Facebook (backend y botones) |
| Cuenta de usuario | Pantallas de login, registro, recuperación, verificación y perfil |
| Comunicación | JavaMailSender con Postfix, SPF, DKIM, SMS y WhatsApp con Twilio |
| Pruebas de API | Colección de Bruno con casos de éxito y de error |
| Servidor | MySQL en el VPS, backend con systemd y configuración externa |

## Francisco: simulador, administración y entrega
| Área | Qué incluye |
|---|---|
| Simulador | Cálculo de ROI y semáforo, CRUD con paginación y filtros del servidor, PDF y análisis de IA |
| Administración | Catálogos, dataset de IA, panel de administración y logs de correo (backend y pantallas) |
| Diseño | Figma (pantallas, prototipo navegable, paleta justificada y logo original) |
| Calidad | Pruebas de punta a punta con los tres roles y registro de errores como issues |
| Publicación | Dominio, Nginx y HTTPS con Certbot |
| Documentación | README, repositorio público, tablero y guion de la demostración |

## Trabajo compartido
- Revisión cruzada: cada pull request lo revisa el otro integrante.
- Despliegue final y ensayo de la demostración.
- Los dos deben poder explicar cualquier parte del sistema en la defensa.

## Cómo trabajar en Git
1. Cada integrante configura su propia identidad en su computadora:
   ```bash
   git config user.name "Su nombre completo"
   git config user.email "el correo de su cuenta de GitHub"
   ```
2. Una rama por tarea (`feature/login-social`, `fix/paginacion-usuarios`) y un pull request hacia `main`.
3. Al abrir el pull request, la tarjeta pasa a **In Review**. Cuando el otro integrante lo aprueba y se une, pasa a **Done**.
4. Commits pequeños y con mensaje descriptivo, por ejemplo: `Agrega verificación del token de Google en el backend`.
5. Cada quien hace commit con su propia cuenta y de su propio trabajo. No se hacen commits a nombre del otro ni se cambian fechas del historial.

## Tablero de GitHub Projects
Columnas, en este orden: **Backlog, To Do, In Progress, In Review, Done**.

| Columna | Significa |
|---|---|
| Backlog | Ideas o mejoras que aún no se van a hacer |
| To Do | Listo para empezar |
| In Progress | Alguien lo está haciendo ahora |
| In Review | Hay pull request o espera pruebas del otro integrante |
| Done | Terminado y revisado |

El estado inicial de las 42 tarjetas está en `tablero/issues.csv` y refleja lo que ya existe en el repositorio hoy. **Mueve cada tarjeta solo cuando el avance sea real**; la rúbrica pide un tablero activo que muestre el avance durante el desarrollo.

### Crearlo con un script
Requiere la [CLI de GitHub](https://cli.github.com/) (`gh`) instalada.

```powershell
gh auth login
cd tablero
.\crear-tablero.ps1 -UsuarioEmiliano "su-usuario-de-github" -UsuarioFrancisco "usuario-de-github-de-francisco"
```

El script hace lo siguiente:
1. Pone el repositorio en **público** y da acceso de escritura a los dos usuarios.
2. Crea las etiquetas.
3. Crea el proyecto, lo hace **público** y lo vincula al repositorio.
4. Deja las columnas Backlog, To Do, In Progress, In Review y Done.
5. Crea las 42 issues, las asigna y las coloca en su columna.

Imprime la dirección del tablero al terminar: pégala en el README, en la fila "Tablero de GitHub Projects".

Si el script falla en algún paso, se puede hacer a mano:
1. En el repositorio: pestaña **Projects → New project → Board**.
2. En el campo **Status**, edita las opciones para que queden las cinco columnas.
3. **Settings del Project → Visibility → Public**.
4. Crea las issues a partir de `tablero/issues.csv` y arrástralas a su columna.
