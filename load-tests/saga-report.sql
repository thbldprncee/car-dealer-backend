-- Saga timing in order_db for purchases paid at or after :'since' (an ISO timestamp).
-- Paying writes the order.sent-for-approval outbox event in the same transaction,
-- so its created_at is the moment of payment; the order's updated_at is the moment
-- order-service applied the warehouse's answer.
WITH paid AS (
    SELECT o.status,
           1000 * extract(epoch FROM e.sent_at - e.created_at) AS outbox_wait_ms,
           CASE WHEN o.status = 'READY_FOR_DELIVERY'
                THEN 1000 * extract(epoch FROM o.updated_at - e.created_at) END AS saga_ms
    FROM in_stock_car_orders o
    JOIN outbox_events e ON e.aggregate_id = o.id AND e.topic = 'order.sent-for-approval'
    WHERE e.created_at >= :'since'
)
SELECT count(*)                                                          AS paid,
       count(*) FILTER (WHERE status = 'READY_FOR_DELIVERY')             AS ready,
       count(*) FILTER (WHERE status = 'CANCELED')                       AS canceled,
       count(*) FILTER (WHERE status = 'PAID')                           AS still_paid,
       round(percentile_cont(0.5) WITHIN GROUP (ORDER BY outbox_wait_ms)) AS outbox_wait_p50_ms,
       round(percentile_cont(0.95) WITHIN GROUP (ORDER BY outbox_wait_ms)) AS outbox_wait_p95_ms,
       round(percentile_cont(0.5) WITHIN GROUP (ORDER BY saga_ms))        AS saga_p50_ms,
       round(percentile_cont(0.95) WITHIN GROUP (ORDER BY saga_ms))       AS saga_p95_ms,
       round(max(saga_ms))                                                AS saga_max_ms
FROM paid;
