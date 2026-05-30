package in.shivam.retaillite.auth.security;

public final class AuthConstants {
    public static  String[] PUBLIC_URL = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/auth/login",
            "/auth/encode",
            "/uploads"
    };
    public static String[] ADMIN_URL={
            "/user/**",
            "/category/delete",
            "/product/delete"
    };
    public static String[] USER_URL={
            "/product/**",
            "/category/**"
    };
}
