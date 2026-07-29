# Database Schema

RetailLite persists to **MySQL 8** via Spring Data JPA/Hibernate. Schema is owned entirely by
**Flyway** (`V1__init.sql`) — `ddl-auto: validate` means Hibernate checks its mappings against that
schema at startup but never creates or alters it.

### Database ER Diagram — RetailLite

<img src="../diagrams/erd/ERD_RetailLite.svg">

## Tables

| Table (entity) | Key columns | Notes |
|---|---|---|
| `users` (`User`) | `userId`, `username` (unique), `password` (BCrypt), `role` | `Role`: `ROLE_USER`, `ROLE_ADMIN` |
| `category` (`Category`) | `categoryId` (unique), `name` (unique), `imageKey` | Delete restricted while linked products exist |
| `product` (`Product`) | `productId`, `price`, `taxRate`, `category_id` (FK) | `ON DELETE RESTRICT` toward `Category` |
| `inventory` (`Inventory`) | `product_id` (FK, unique/1:1), `availableQuantity`, `lowStockThreshold`, `version` | Optimistic locking (`@Version`) |
| `invoice` (`Invoice`) | `invoiceId` (unique), `user_id` (FK), `subTotal`/`tax`/`grandTotal`, `invoiceStatus`, `version` | `InvoiceStatus`: `PENDING`, `PAID`, `CANCELED` |
| `invoice_item` (`InvoiceItem`) | `invoice_id` (FK), `product_id` (FK), `quantity`, `unitPrice`, `lineTotal` | Cascade `ALL` + `orphanRemoval` from `Invoice` |
| `payment` (`Payment`) | `paymentId` (unique), `invoiceId` (FK), `paymentMethod`, `gatewayOrderId` (unique), `paymentStatus`, `version` | `PaymentMethod`: `CASH`, `ONLINE`; `PaymentStatus`: `PENDING`, `SUCCESS`, `FAILED`, `EXPIRED`, `REFUNDED` |

## Relationships

- `Category (1) → Product (N)` — restrict-on-delete.
- `Product (1) ↔ Inventory (1)` — one-to-one, cascade `ALL`, orphan removal.
- `User (1) → Invoice (N)` — invoice raised by a staff user.
- `Invoice (1) → InvoiceItem (N)` — cascade `ALL`, orphan removal.
- `Invoice (1) → Payment (N)` — cascade `PERSIST`/`MERGE` only (payments outlive certain invoice operations by design).

## Concurrency control

`Inventory`, `Invoice` and `Payment` each declare a `@Version` field, so simultaneous stock
deductions, invoice status changes, and payment state transitions are protected by Hibernate's
optimistic locking (`ObjectOptimisticLockingFailureException` is explicitly handled in
`GlobalExceptionHandler`).
