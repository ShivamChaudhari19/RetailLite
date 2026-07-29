# API Reference

Base path: **`/api/v1.0`** (`server.servlet.context-path`). All endpoints require `Authorization: Bearer <jwt>`
unless marked **Public**. Interactive docs: `GET /swagger-ui/index.html` (springdoc-openapi)
. Error shapes and status codes: **[Error Reference](errors.md)**.

## Auth — `/auth`
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/auth/login` | **Public** | Authenticate with username/password, returns JWT + roles |

## User — `/user` *(admin only)*
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/user/register` | `ADMIN` | Create a new user account |
| DELETE | `/user/{userId}` | `ADMIN` | Delete a user |
| GET | `/user/users` | `ADMIN` | Paginated list (`page`, `size`, `sortBy`, `orderedBy`) |

## Category — `/category`
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/category` | `ADMIN` | Create category — multipart: `category` (JSON) + `categoryImg` (file) |
| GET | `/category/categories` | `USER`, `ADMIN` | Paginated list (`page`, `size` max 50) |
| DELETE | `/category/{categoryId}` | `ADMIN` | Delete (rejected if products reference it) |

## Product — `/product`
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/product` | `ADMIN` | Create product — multipart: `product` (JSON) + `productImg` (file); auto-provisions `Inventory` |
| DELETE | `/product/{productId}` | `ADMIN` | Delete product |
| GET | `/product/products` | `USER`, `ADMIN` | Paginated list (`page`, `size` max 50) |

## Inventory — `/inventory` *(admin only, class-level `@PreAuthorize`)*
| Method | Path | Description |
|---|---|---|
| POST | `/inventory/{productId}/stock/add` | Add stock quantity |
| POST | `/inventory/{productId}/stock/remove` | Remove/deduct stock quantity |
| GET | `/inventory/{productId}/stock` | Get stock for one product |
| GET | `/inventory/stock` | Paginated stock list |
| PATCH | `/inventory/{productId}/stock/low/threshold` | Update low-stock threshold |
| GET | `/inventory/stock/low` | List products at/below their low-stock threshold |

## Invoice — `/invoices` *(`USER`/`ADMIN` at class level)*
| Method | Path | Description |
|---|---|---|
| POST | `/invoices/invoice` | Create an invoice (items, customer info) |
| GET | `/invoices` | Paginated list (`page`, `size` max 50, `sortBy`, `orderedBy`) |
| GET | `/invoices/{invoiceId}` | Fetch a single invoice |
| GET | `/invoices/status/{status}` | Filter by status: `pending`, `paid`, `canceled` |
| GET | `/invoices/{invoiceId}/pdf` | Download the invoice as a generated PDF |

## Payment — `/payment`
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/payment/pay` | `USER`, `ADMIN` | Pay an invoice (`CASH` completes instantly, `ONLINE` returns a Razorpay order) |
| POST | `/payment/verify` | `USER`, `ADMIN` | Client-side verification of a Razorpay payment |
| POST | `/payment/refund` | `ADMIN` | Refund a successfully paid invoice |

## Webhook — `/webhooks`
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/webhooks/razorpay` | **Public** | Razorpay server-to-server callback; verified via `X-Razorpay-Signature` header |

---
Full request/response schemas are best explored live via Swagger UI, since every module uses
Bean Validation (`@Valid`) on its DTOs and pagination defaults differ slightly per endpoint (see table above).

Non-2xx responses all share one shape — see **[Error Reference](errors.md)** for the full
exception → HTTP status catalog and retry guidance.
