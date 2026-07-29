# Security

RetailLite uses **stateless JWT authentication** on top of Spring Security, with **method-level**
authorization (`@EnableMethodSecurity`).

📐 **[Auth sequence diagrams →]**

## a. Login Sequence Diagram
<img src="../diagrams/sequence/login-sequence.svg">

## b. Authenticated Request Sequence Diagram
<img src="../diagrams/sequence/authenticated-request.svg">

## How it works

- `SecurityConfig` disables CSRF (stateless API), enables CORS for `http://localhost:5173`, and
  registers `JwtRequestFilter` before `UsernamePasswordAuthenticationFilter`.
- `AppUserDetailsService` loads a `User` (via `UserService.findEntityByUsername`) and maps its
  single `Role` to a Spring `GrantedAuthority`.
- Passwords are hashed with `BCryptPasswordEncoder`.
- `JwtServiceImpl` signs tokens with HMAC-SHA (`jjwt` 0.12.7), keyed by `app.security.jwt-secret-key`
  and expiring after `app.security.token-expiration-in-minute` minutes.
- Unauthorized/invalid tokens are routed through `HandlerExceptionResolver`; access-denied requests
  return `403` with a plain-text body from a custom `accessDeniedHandler`.

## Public endpoints (no token required)

Defined in `AuthConstants.PUBLIC_URL`:

```
/swagger-ui/**
/v3/api-docs/**
/auth/login
/uploads/**
/webhooks/razorpay
```

Everything else requires a valid Bearer token, and most write operations additionally require
`ROLE_ADMIN` (see [API Reference](api-reference.md) for the per-endpoint breakdown).


## Roles

| Role | Typical access |
|---|---|
| `ROLE_ADMIN` | Full access: user management, catalog/category management, inventory, refunds |
| `ROLE_USER` | Read catalog/inventory, create invoices, initiate payments and verification |

## Payment-specific safeguards

- The Razorpay webhook verifies the `X-Razorpay-Signature` header before trusting any payload.
- `PaymentOrchestrator` treats payment verification as **idempotent** — a payment already `SUCCESS`
  short-circuits instead of re-processing.
- Stock and payment/invoice state changes are guarded by optimistic locking (`@Version`) to prevent
  double-spend/oversell races under concurrent requests.
