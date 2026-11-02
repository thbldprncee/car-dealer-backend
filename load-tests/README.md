# Load tests: monolith vs microservices

The same two k6 scenarios against both versions of the system:

- **monolith**: commit `5baba7b`, the last commit before the split (one Spring Boot app, one database);
- **microservices**: the current code (order-service, storage-service, two databases, Kafka saga).

The goal is to measure what the split cost and what it bought.

## How to run

```bash
load-tests/run.sh                    # both architectures, about 30 minutes
load-tests/run.sh microservices      # one of them
READ_RATES="5000 8000" PURCHASE_RATES="" load-tests/run.sh   # custom steps; empty skips a test
python3 load-tests/summarize.py      # tables from load-tests/results/
```

Requires only Docker. `run.sh` builds the monolith from `git archive 5baba7b` with the same Dockerfile layout as the services. It then starts a fresh stack, seeds test cars, warms the JVMs up, runs every step and stops the stack.

## Method

| | |
|---|---|
| Machine | Apple M4 Pro, 12 cores; Docker Desktop 29.9 with 12 CPUs and 12 GB |
| Limits | every application container and every PostgreSQL: **2 CPUs, 1 GB** (Kafka and Keycloak are not limited) |
| Load generator | k6 0.54 in a container on the stack's network, so requests skip Docker's port forwarding |
| Load model | `constant-arrival-rate`: requests arrive at a fixed rate whether or not the system keeps up, so saturation shows as latency and *dropped* iterations, not as a quietly lower rate |
| Warm-up | 30 s of each scenario before measuring, not recorded |
| Steps | read: 40 s per rate; purchase: 60 s per rate |
| Resources | `docker stats` sampled every 2 s during each step |
| Auth | tokens are fetched once per run; Keycloak is not part of the measured path |

**Scenarios**

1. **Read**: `GET` a car by id (1,000 different cars, random). For the microservices it runs twice: directly against storage-service, and through order-service's `/api/v1/cars/{id}`, which calls storage-service over gRPC.
2. **Purchase** of an in-stock car, every iteration on its own car:
   - monolith: create → manager approves → waiting for payment → pay → manager marks it ready. When the last call returns, the car is reserved and the order is done.
   - microservices: create → approve → waiting for payment → pay. Paying starts the saga (outbox → Kafka → storage-service reserves the car → outbox → Kafka → order-service). The saga is timed from `order_db`: from the outbox event written at payment to the order's last update. Polling the API instead would add load of its own.

**Not equal by design:** each microservice gets as much as the whole monolith, so the microservices have twice the application CPU in total. They also run Kafka and a second database.

## Results (2026-11-02)

### Read: GET a car by id

| Architecture | Path | Target, req/s | Done, req/s | p50, ms | p95, ms | p99, ms | Errors | Dropped | App CPU avg |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| monolith | app | 1000 | 999 | 0.4 | 0.7 | 1.1 | 0% | 0 | 29% |
| monolith | app | 3000 | 2998 | 0.4 | 0.5 | 1.3 | 0% | 0 | 77% |
| monolith | app | 5000 | 4940 | 0.4 | 108 | 207 | 0% | 2,243 | 134% |
| monolith | app | 8000 | **6533** | 186 | 284 | 351 | 0% | 57,254 | 200% |
| monolith | app | 12000 | **6605** | 196 | 294 | 368 | 0% | 213,897 | 201% |
| microservices | storage-service | 1000 | 999 | 0.5 | 1.0 | 2.0 | 0% | 0 | 41% |
| microservices | storage-service | 3000 | 2995 | 0.5 | 1.1 | 18 | 0% | 70 | 108% |
| microservices | storage-service | 5000 | 3771 | 355 | 501 | 596 | 0% | 47,650 | 199% |
| microservices | storage-service | 12000 | **4169** | 319 | 490 | 583 | 0% | 311,530 | 198% |
| microservices | order → gRPC → storage | 1000 | 999 | 0.6 | 1.1 | 2.1 | 0% | 0 | 58% |
| microservices | order → gRPC → storage | 3000 | 2997 | 0.6 | 1.3 | 2.6 | 0% | 0 | 139% |
| microservices | order → gRPC → storage | 5000 | 4815 | 0.7 | 304 | 421 | 0% | 7,258 | 264% |
| microservices | order → gRPC → storage | 12000 | **6064** | 223 | 300 | 425 | 0% | 235,931 | 301% |

CPU is in Docker's units: 100% is one core, and each container is capped at 200%. Every step is in [`results/`](results); `summarize.py` prints the full tables.

### Purchase of an in-stock car

| Architecture | Purchases/s | Completed | Synchronous calls p50 / p95, ms | Order ready (saga) p50 / p95 | Saga max | App CPU avg |
|---|---:|---:|---:|---:|---:|---:|
| monolith | 5 | 300/300 | 17 / 20 | immediately | — | 7% |
| monolith | 10 | 601/601 | 15 / 18 | immediately | — | 11% |
| monolith | 20 | 1201/1201 | 9 / 12 | immediately | — | 12% |
| monolith | 40 | 2401/2401 | 6 / 9 | immediately | — | 13% |
| microservices | 5 | 301/301 | 20 / 28 | 4.3 s / 5.7 s | 5.8 s | 18% |
| microservices | 10 | 600/600 | 12 / 21 | 4.4 s / 5.7 s | 5.9 s | 20% |
| microservices | 20 | 1200/1200 | 9 / 12 | 10.4 s / 16.2 s | 18.1 s | 20% |
| microservices | 40 | 2401/2401 | 6 / 9 | **46.7 s / 85.5 s** | 89.7 s | 23% |

Every saga finished as `READY_FOR_DELIVERY`: nothing was lost, cancelled or stuck. At 40 purchases/s, the backlog took 90 s to drain after the load stopped.

### After the outbox fix

The microservice rows above were measured with the original outbox: 50 events every 3 s, each waiting for its own acknowledgement. Those runs are kept in [`results/microservices-before-outbox-fix`](results/microservices-before-outbox-fix).

The fix changes three things:

- The whole batch is handed to the producer and acknowledged together.
- Batches follow each other until the outbox is empty.
- The scheduler polls every 200 ms, with a partial index on unsent events.

| Purchases/s | Saga p50 / p95 before | Saga p50 / p95 after | Saga max after | Completed after | App CPU avg after |
|---:|---:|---:|---:|---:|---:|
| 5 | 4.3 s / 5.7 s | **240 ms / 401 ms** | 412 ms | 300/300 | 20% |
| 10 | 4.4 s / 5.7 s | **314 ms / 403 ms** | 414 ms | 600/600 | 26% |
| 20 | 10.4 s / 16.2 s | **308 ms / 400 ms** | 413 ms | 1200/1200 | 25% |
| 40 | 46.7 s / 85.5 s | **310 ms / 398 ms** | 417 ms | 2401/2401 | 31% |
| 80 | — | **313 ms / 397 ms** | 412 ms | 4801/4801 | 46% |

The saga now takes the same time at 80 purchases/s as at 5, so its ceiling is somewhere above 80/s and no backlog builds up. The remaining ~300 ms is two outbox hops, each waiting on average half of the 200 ms poll interval, plus Kafka. The synchronous calls did not change: p95 is 9–26 ms.

### Memory (peak)

| Architecture | Application containers | Databases |
|---|---:|---:|
| monolith | 409 MiB | 64 MiB |
| microservices | 1,023 MiB | 120 MiB |

## Findings

1. **Reads saturate on CPU, and the split made a single read more expensive.**
   - With the same 2 cores, the monolith serves about **6,600 req/s** and storage-service only about **4,100 req/s** for the same endpoint. Why storage-service's REST path costs more per request was not profiled. Candidates are the `@PreAuthorize` checks the monolith's `CarController` doesn't have, and the trace-id filter.
   - Going through order-service and gRPC reaches about **6,100 req/s**, but only because the work is spread over two containers: about 3 cores in total against the monolith's 2.
   - Below saturation, latency is the same for both (p95 ≈ 1 ms). The extra network hop costs CPU, not visible latency.
2. **The saga's throughput is capped by the outbox, not by CPU.** `OutboxPublisher` takes at most 50 events every 3 s (`BATCH_SIZE = 50`, `fixed-delay: 3000`) and waits for Kafka's acknowledgement one event at a time. That is at most about **16 sagas/s**. The measurement agrees: at 40 purchases/s, 2,401 sagas took about 150 s, which is 16/s. Saga time grew from 4 s to 47 s at the median. Application CPU stayed at about 23% of the 400% available. Even with no load, an order waits **4–6 s** for the warehouse: two outbox hops, each waiting up to 3 s for the scheduler. The monolith finishes the same work in one transaction in under 20 ms. **Fixed:** after the change described above, the saga takes about 0.3 s up to at least 80 purchases/s.
3. **The split costs about 2.5× the memory** (two JVMs) before Kafka is counted.
4. **Bug found while preparing the test.** `order_db` still has a foreign key from `in_stock_car_orders` to its own copy of `cars` (`fk_in_stock_order_car`, from `001-create-tables.yaml`, never dropped in `003`). An order for any car that exists only in `storage_db` fails with HTTP 500, which means any car added through the warehouse API after the seed. The test works around it by seeding the cars into both databases.

## What the numbers say about the split

At this system's real scale, a dealership with a few orders per minute, the monolith is cheaper and faster on every measured axis. The split did not improve throughput or latency here. Its argument is organisational: separate ownership of sales and warehouse data, independent deployment, and scaling the catalog separately from orders. The test also shows what that costs: eventual consistency, a saga with a hard throughput ceiling set by configuration, and more CPU and memory per request.

Obvious next steps, each to be measured again:

- Drop the leftover foreign key in `order_db`.
- Find the saga's new ceiling (above 80 purchases/s) and test isolation: heavy catalog reads running alongside purchases, and storage-service stopped under load.
- Profile storage-service's REST path against the monolith's.

## Limits of this test

- Everything ran on one laptop. k6, Kafka, Keycloak and both stacks share the host, so absolute numbers do not transfer to a server; the comparison between architectures is the point.
- Containers from another project (finguard) were running but idle during the tests, under 2% CPU.
- Each step ran once for 40–60 s. A small spike, such as storage-service's p99 of 18 ms at 3,000 req/s, may be noise (GC, scheduling).
- Only two scenarios were tested. Custom orders, test drives and list endpoints were not.
