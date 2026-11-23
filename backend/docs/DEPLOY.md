# Despliegue en Vultr (Ubuntu 22.04/24.04)

> Repositorio con dos carpetas: los comandos de Maven y `application.properties` se ejecutan dentro de `backend/`, y los de npm dentro de `frontend/`.

Reemplaza `TU-DOMINIO.com` y `IP_DEL_VPS`. En el DNS crea un registro **A** `@` → `IP_DEL_VPS`.

## 1. Paquetes base
```bash
sudo apt update && sudo apt -y upgrade
sudo apt -y install openjdk-17-jre-headless nginx certbot python3-certbot-nginx mysql-server maven git ufw
sudo ufw allow OpenSSH && sudo ufw allow 'Nginx Full' && sudo ufw allow 25/tcp && sudo ufw enable
```
> En Ubuntu `mysql-server` instala **MySQL** (en Debian sería MariaDB, que no se acepta). Verifica con `mysql --version`.
> Vultr bloquea el puerto 25 saliente en algunas cuentas nuevas: abre un ticket para desbloquearlo o los correos no saldrán.

## 2. Base de datos
```bash
sudo mysql < database/00_create_database.sql
sudo mysql -e "CREATE USER 'oaxinvest'@'localhost' IDENTIFIED BY 'CAMBIAME'; GRANT ALL ON simulador_oaxaca.* TO 'oaxinvest'@'localhost';"
```

## 3. Backend como JAR con systemd
```bash
sudo useradd -r -s /usr/sbin/nologin oaxinvest
sudo mkdir -p /opt/oaxinvest /var/oaxinvest/{uploads,pdfs} && sudo chown -R oaxinvest: /opt/oaxinvest /var/oaxinvest
git clone https://github.com/TU_USUARIO/TU_REPO.git && cd TU_REPO
cp src/main/resources/application.properties.example /opt/oaxinvest/application.properties   # edítalo con valores reales
mvn -DskipTests package && sudo cp target/oaxinvest.jar /opt/oaxinvest/
sudo chmod 600 /opt/oaxinvest/application.properties
```
`/etc/systemd/system/oaxinvest.service`:
```ini
[Unit]
Description=OaxInvest API
After=network.target mysql.service

[Service]
User=oaxinvest
WorkingDirectory=/opt/oaxinvest
ExecStart=/usr/bin/java -jar /opt/oaxinvest/oaxinvest.jar --spring.config.location=file:/opt/oaxinvest/application.properties
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```
```bash
sudo systemctl daemon-reload && sudo systemctl enable --now oaxinvest && journalctl -u oaxinvest -f
```
Valores clave del `application.properties` del VPS: `app.frontend-url=https://TU-DOMINIO.com`, `app.cors.allowed-origins=https://TU-DOMINIO.com`, `app.mail.from=no-reply@TU-DOMINIO.com`.

## 4. Frontend (build estático) + Nginx
```bash
# en el proyecto React:  npm ci && npm run build     (VITE_API_URL=/api)
sudo mkdir -p /var/www/oaxinvest && sudo cp -r dist/* /var/www/oaxinvest/
```
`/etc/nginx/sites-available/oaxinvest`:
```nginx
server {
    server_name TU-DOMINIO.com;
    root /var/www/oaxinvest;
    index index.html;
    client_max_body_size 3m;

    location /api/     { proxy_pass http://127.0.0.1:8080; proxy_set_header Host $host;
                         proxy_set_header X-Real-IP $remote_addr; proxy_set_header X-Forwarded-Proto $scheme; }
    location /uploads/ { proxy_pass http://127.0.0.1:8080; }
    location /         { try_files $uri /index.html; }
}
```
```bash
sudo ln -s /etc/nginx/sites-available/oaxinvest /etc/nginx/sites-enabled/ && sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d TU-DOMINIO.com          # HTTPS gratis con Let's Encrypt, renovación automática
```

## 5. Postfix + SPF + DKIM
```bash
sudo apt -y install postfix opendkim opendkim-tools     # tipo: "Internet Site", nombre de correo: TU-DOMINIO.com
sudo postconf -e "inet_interfaces = loopback-only" "myhostname = TU-DOMINIO.com" "myorigin = TU-DOMINIO.com"
sudo postconf -e "milter_default_action = accept" "milter_protocol = 6" \
  "smtpd_milters = local:opendkim/opendkim.sock" "non_smtpd_milters = local:opendkim/opendkim.sock"
sudo mkdir -p /etc/opendkim/keys/TU-DOMINIO.com && cd /etc/opendkim/keys/TU-DOMINIO.com
sudo opendkim-genkey -s mail -d TU-DOMINIO.com && sudo chown -R opendkim: /etc/opendkim
```
`/etc/opendkim.conf` (agrega/ajusta):
```
Syslog yes
Socket local:/var/spool/postfix/opendkim/opendkim.sock
Domain TU-DOMINIO.com
Selector mail
KeyFile /etc/opendkim/keys/TU-DOMINIO.com/mail.private
```
```bash
sudo mkdir -p /var/spool/postfix/opendkim && sudo chown opendkim:postfix /var/spool/postfix/opendkim
sudo usermod -aG opendkim postfix && sudo systemctl restart opendkim postfix
sudo cat /etc/opendkim/keys/TU-DOMINIO.com/mail.txt     # contenido del registro DKIM
```
Registros DNS (TXT):
| Nombre | Valor |
|---|---|
| `@` | `v=spf1 ip4:IP_DEL_VPS -all` |
| `mail._domainkey` | contenido de `mail.txt` (`v=DKIM1; k=rsa; p=...`) |
| `_dmarc` | `v=DMARC1; p=none; rua=mailto:admin@TU-DOMINIO.com` |

Además pide en Vultr el **registro PTR (rDNS)** de la IP = `TU-DOMINIO.com` (Settings → IPv4 → Reverse DNS). Prueba con: `echo "prueba" | mail -s "Test" tu_gmail@gmail.com` y revisa los encabezados `spf=pass` y `dkim=pass` (o usa mail-tester.com).

## 6. Verificación
`curl -s https://TU-DOMINIO.com/api/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@oaxinvest.mx","password":"Admin#2026"}'` debe regresar un token.

## 7. Si Vultr mantiene bloqueado el puerto 25 saliente (relay por 587)
Spring Boot sigue enviando a **Postfix local** (`localhost:25`, sin cambios en el código). Lo que cambia es que Postfix entrega el correo a un *smarthost* por el puerto **587** en lugar de conectarse directo a los servidores del destinatario.

1. Prueba si realmente está bloqueado: `nc -zv gmail-smtp-in.l.google.com 25` (si no conecta, sí lo está).
2. Crea una cuenta gratuita en un proveedor SMTP que permita tu dominio (ej. Brevo, SMTP2GO, Mailjet), verifica `TU-DOMINIO.com` y copia su host SMTP, usuario y clave SMTP.
3. Configura Postfix:
```bash
sudo apt -y install libsasl2-modules
sudo postconf -e "relayhost = [smtp.TU-PROVEEDOR.com]:587" \
  "smtp_sasl_auth_enable = yes" "smtp_sasl_password_maps = hash:/etc/postfix/sasl_passwd" \
  "smtp_sasl_security_options = noanonymous" "smtp_tls_security_level = encrypt" \
  "smtp_tls_CAfile = /etc/ssl/certs/ca-certificates.crt"
echo "[smtp.TU-PROVEEDOR.com]:587 USUARIO:CLAVE_SMTP" | sudo tee /etc/postfix/sasl_passwd
sudo chmod 600 /etc/postfix/sasl_passwd && sudo postmap /etc/postfix/sasl_passwd
sudo systemctl restart postfix
```
4. DNS: SPF = `v=spf1 include:<dominio-spf-del-proveedor> -all` y publica los registros DKIM que te dé el proveedor (o conserva tu opendkim si el proveedor no re-firma). Mantén `_dmarc`.
5. Prueba: `echo "prueba" | mail -s "Test" tu_gmail@gmail.com` y revisa `spf=pass` y `dkim=pass` en los encabezados. Los fallos quedan en `GET /api/admin/email-logs`.

**Avisa a tu profesora**: la rúbrica pide Postfix propio sin SendGrid/Mailgun. Con este esquema Postfix, SPF y DKIM siguen siendo tuyos, pero la última entrega sale por un relay. Pide su visto bueno por escrito y menciónalo en el README.

## 8. Inicio de sesión con Google y Facebook (opcional)
No usa el puerto 25 ni Postfix; solo necesita HTTPS (Certbot) y salida a internet por el 443.

1. Agrega al `application.properties` del servidor (el mismo archivo donde están las demás credenciales):
   ```properties
   app.google.client-id=TU_CLIENT_ID.apps.googleusercontent.com
   app.facebook.app-id=TU_APP_ID
   app.facebook.app-secret=TU_APP_SECRET
   ```
2. En Google Cloud Console, agrega `https://tu-dominio.com` a *Orígenes autorizados de JavaScript* del ID de cliente. En Facebook Developers, agrega tu dominio en *Dominios de la app* y el sitio web `https://tu-dominio.com`.
3. Reinicia el servicio: `sudo systemctl restart oaxinvest`.
4. Comprueba que el servidor alcanza a los proveedores: `curl -sI https://oauth2.googleapis.com | head -1` y `curl -sI https://graph.facebook.com | head -1`.
5. Abre `https://tu-dominio.com/api/auth/social-config`: debe mostrar tus IDs (el de Facebook aparece solo si también hay secreto). Si salen vacíos, el servidor no leyó las propiedades.
