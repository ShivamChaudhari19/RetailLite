# Design Decisions

Lightweight ADRs (Architecture Decision Records) for the choices that shape RetailLite. Each entry
states the context, the decision actually implemented in code, and its trade-offs — not aspirational
design, only what's shipped.

## 1. Layered modular monolith, sliced by feature

**Context:** A small-team retail backend needs to move fast without turning into an unmaintainable
ball of mud, but doesn't yet need the operational overhead of microservices.

**Decision:** One deployable Spring Boot application, internally split into feature packages
(`auth`, `user`, `category`, `product`, `inventory`, `invoice`, `payment`, `storage`), each with its
own `controller / service / repository / entity / dto / exception` slice, plus a `common` package
for cross-cutting concerns (enums, `GlobalExceptionHandler`, shared validation).

**Trade-off:** Faster local development and simpler deployment/transactions than a distributed
system; the cost is that all modules share one JVM, one database, and one deploy cycle. The
package-per-feature boundary keeps a future extraction to services realistic if the product grows.

## 2. Stateless JWT authentication, method-level authorization

**Context:** The API is consumed by a decoupled frontend and needs to scale horizontally without
sticky sessions.

**Decision:** `JwtRequestFilter` validates a Bearer token on every request and populates the
`SecurityContext`; no server-side session state is kept. `@EnableMethodSecurity` + `@PreAuthorize`
enforce `ROLE_ADMIN` / `ROLE_USER` per endpoint (or per controller, e.g. `InventoryController`).
Passwords are hashed with `BCryptPasswordEncoder`; tokens are signed HMAC-SHA via `jjwt` 0.12.7.

**Trade-off:** No built-in revocation list — a compromised token is valid until it expires
(`token-expiration-in-minute`). Acceptable for the current scope; a refresh-token/blacklist scheme
is the natural next step if this goes to production traffic.

## 3. Strategy pattern for payment methods

**Context:** The business needs both an instant offline method (cash) and a gateway-backed online
method (Razorpay) today, with more gateways likely later (UPI, cards).

**Decision:** `PaymentService` is an interface (`pay`, `refund`, `verifyPayment`,
`getPaymentMethod`). `PaymentServiceFactory` collects every Spring bean implementing it into a
`Map<String, PaymentService>` keyed by each bean's own `getPaymentMethod()`. `PaymentOrchestrator`
never branches on payment type directly — it asks the factory for the right strategy.

**Trade-off:** Slightly more indirection than an `if/else` for two methods, but adding a third
payment method is a new `@Service` bean with zero changes to the orchestrator or factory — the
trade was made deliberately for extensibility over the shortest path today.

## 4. A single orchestrator owns the payment transaction

**Context:** Paying an invoice touches three concerns at once — invoice state, inventory stock, and
an external payment gateway — and all three need to succeed or fail together.

**Decision:** `PaymentOrchestrator` is the one transactional use-case coordinator: it validates the
invoice is payable, checks/reserves inventory, invokes the resolved `PaymentService`, and updates
invoice/inventory state. If a post-hoc stock shortfall is detected after a payment succeeds, it
automatically triggers a refund and cancels the invoice rather than leaving inconsistent state.

**Trade-off:** Centralizes complexity in one class, but that's intentional — it's easier to reason
about (and test) one orchestrator than the same coordination logic duplicated per payment method.

## 5. Composable renderer pipeline for invoice PDFs

**Context:** Invoice PDFs have five visually distinct regions (header, parties, items table,
totals, payment) that change independently as the business evolves (e.g. adding a GST breakdown).

**Decision:** `PdfGenerator` is the single seam between the invoice module and the underlying PDF
engine. `OpenPdfInvoiceGenerator` implements it by delegating to five independent, stateless
`InvoiceSectionRenderer` implementations, one per region, with a fresh `PageFooterEventHandler` per
document.

**Trade-off:** More classes than a single "build the PDF" method, but each renderer is independently
testable and the PDF engine itself (OpenPDF today) is swappable behind `PdfGenerator` without
touching invoice business logic.

## 6. Interface-segregated, pluggable file storage

**Context:** Product/category images need to work in local development (no cloud account required)
and in a cloud deployment (S3) without forking the upload logic.

**Decision:** `CategoryService` and `ProductService` depend only on the `StorageService` interface
(`upload`, `delete`, `getKey`, `getFileUrl`). `LocalStorageService` and `S3StorageService` are
interchangeable implementations selected at configuration time.

**Trade-off:** `S3StorageService` is currently incomplete (marked `//todo` in source) — local disk,
served via the public `/uploads/**` route, is the default and fully working path today.

## 7. Optimistic locking on every mutable financial/stock entity

**Context:** Stock deduction and payment state transitions can race under concurrent requests
(two checkouts for the last unit of stock, a webhook and a client-verify call landing together).

**Decision:** `Inventory`, `Invoice`, and `Payment` each carry a `@Version` column. Hibernate raises
`ObjectOptimisticLockingFailureException` on a lost update, which `GlobalExceptionHandler` maps to a
client-facing error rather than allowing silent overselling or double-charging.

**Trade-off:** Pessimistic locking would avoid client-visible conflicts but reduces throughput under
load; optimistic locking was chosen because conflicts are expected to be rare (single-store
checkout volume) and the client can safely retry.

## 8. Idempotent payment verification

**Context:** Razorpay's webhook can be retried/replayed by the gateway, and a client may also call
the verify endpoint after redirect — both paths can reach the same payment.

**Decision:** Both `PaymentController.verify` and `RazorpayWebhookController` route through
`PaymentQueryService.isAlreadySuccessful` (a read-only check in its own transaction) before
`PaymentOrchestrator` does any state-changing work. An already-`SUCCESS` payment short-circuits
instead of being re-processed.

**Trade-off:** One extra read per verification call, in exchange for guaranteed safety against
double-processing a payment — a correctness requirement that isn't optional for money movement.

## 9. Restrict-on-delete for referential integrity

**Context:** Deleting a `Category` that still has `Product`s attached (or a `Product` still
referenced by historical `Invoice`s) must never silently orphan or cascade-delete sales history.

**Decision:** `Product.category` uses `@OnDelete(action = OnDeleteAction.RESTRICT)`; deleting a
category with existing products throws `CategoryDeletionException` instead of cascading.

**Trade-off:** Slightly less convenient for admins (they must reassign/delete products first), but
protects data integrity and historical invoice accuracy — an explicit choice over cascading deletes.

## 10. Schema owned entirely by Flyway, Hibernate set to `validate`

**Context:** Early on, `ddl-auto: update` was used for fast local iteration with a single
developer. As the project matured, that trade stopped making sense — no rollback story, no schema
audit trail, and it can't express destructive changes safely.

**Decision:** Flyway (`flyway-core` + `flyway-mysql`) now owns the entire schema —
`V1__init.sql` is the single entry-point migration: it creates every table (`users`, `category`,
`product`, `inventory`, `invoice`, `invoice_item`, `payment`) with explicit column types,
constraints, and foreign keys matched column-for-column against the JPA entity mappings, then seeds
the initial `ROLE_ADMIN` account. `spring.jpa.hibernate.ddl-auto` is set to `validate` — Hibernate
checks its entity mappings against the schema Flyway created at startup and fails immediately and
loudly on any mismatch, but never creates, alters, or drops anything itself.

**Trade-off:** Every schema change now requires writing a new migration by hand instead of just
changing an `@Column` annotation and restarting — slower, but it's the only way to get a real audit
trail, safe rollouts across environments, and a startup-time guarantee (via `validate`) that the
code and the database schema actually agree, instead of finding out at query time. Two entity-level
quirks surfaced doing this migration and are called out directly in `V1__init.sql`
rather than silently "fixed": `Payment.invoice`'s join column is literally `invoiceId` (no
underscore, bypassing the naming strategy via an explicit `@JoinColumn(name=...)`), and
`Product.productId` has no uniqueness/not-null constraint at the entity level unlike its sibling ID
fields. Both are mirrored faithfully since code is the source of truth here, not "cleaned up" as
a side effect of writing SQL.
