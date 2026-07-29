# System Architecture — RetailLite

> Referenced from [`docs/README.md`](../../docs/README.md) and [`docs/architecture.md`](../../docs/architecture.md)

Layered Spring Boot monolith (context path `/api/v1.0`). Every request passes through the JWT
security filter before reaching a module's Controller → Service → Repository stack, backed by
MySQL and two external providers (Razorpay, AWS S3 / local disk).

<img src="architecture.svg">

**Key architectural decisions grounded in code:**
- `server.servlet.context-path=/api/v1.0` — all endpoints below are relative to this prefix (`application.yaml`).
- `JwtRequestFilter` is added before `UsernamePasswordAuthenticationFilter` in `SecurityConfig`, stateless JWT-based auth.
- `PaymentServiceFactory` builds a `Map<String, PaymentService>` at startup keyed by `getPaymentMethod()` (`CASH`, `ONLINE`) — a Strategy pattern for pluggable payment methods.
- `StorageService` has two implementations (`LocalStorageService`, `S3StorageService`), selected via configuration (`storage/config`).
- Razorpay webhook (`/webhooks/razorpay`) is the **only** payment-related endpoint that is publicly accessible (`AuthConstants.PUBLIC_URL`).

See also: [Database ERD](../erd/database-erd.md) · [Payment Sequence Flow](../sequence/payment-flow.md) · [Auth Sequence Flow](../sequence/auth-flow.md)
