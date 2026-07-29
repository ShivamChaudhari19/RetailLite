# RetailLite Documentation

| Doc | Description |
|---|---|
| [Architecture](architecture.md) | Layered design, request lifecycle, patterns, external integrations |
| [Design Decisions](design-decisions.md) | ADR-style write-up of *why*, with trade-offs, for every major decision |
| [Getting Started](getting-started.md) | Env vars, Docker Compose, local run, tests |
| [API Reference](api-reference.md) | Every endpoint, method, path, access rule, and example payload |
| [Error Reference](errors.md) | Error response shape, full exception → HTTP status catalog, retry guidance |
| [Database Schema](database-schema.md) | Tables, relationships, concurrency control |
| [Security](security.md) | JWT auth flow, roles, public endpoints |

## Modules

| Module | Doc |
|---|---|
| 🔐 [auth](modules/auth.md) | JWT issuance/validation, Spring Security configuration |
| 👤 [user](modules/user.md) | Admin-managed staff accounts & roles |
| 🏷️ [category](modules/category.md) | Product categories with image upload |
| 📦 [product](modules/product.md) | Product catalog: pricing, tax rate, category linkage |
| 📊 [inventory](modules/inventory.md) | Stock levels, low-stock thresholds |
| 🧾 [invoice](modules/invoice.md) | Invoice lifecycle + composable PDF generation |
| 💳 [payment](modules/payment.md) | Strategy-based payments, refunds, webhooks |
| ☁️ [storage](modules/storage.md) | Pluggable local disk / AWS S3 file storage |

## Diagrams

All diagrams referenced throughout these docs live in [`/diagrams`](../diagrams)

