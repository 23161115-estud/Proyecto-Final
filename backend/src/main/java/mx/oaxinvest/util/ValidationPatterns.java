package mx.oaxinvest.util;

public final class ValidationPatterns {
    private ValidationPatterns() {
    }

    public static final String PASSWORD = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";
    public static final String PASSWORD_MSG =
            "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial";

    public static final String PHONE = "^\\+[1-9]\\d{7,14}$";
    public static final String PHONE_MSG = "El teléfono debe estar en formato internacional, por ejemplo +529511234567";

    public static final String ROLE = "^(ADMIN|ANALISTA|CLIENTE)$";
    public static final String ROLE_MSG = "El rol debe ser ADMIN, ANALISTA o CLIENTE";
}
