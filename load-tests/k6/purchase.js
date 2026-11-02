// Write path: buying an in-stock car, RATE purchases started per second.
// Every iteration orders its own pre-seeded car (OFFSET + iteration number).
//
//   monolith:      create -> approve -> waiting-for-payment -> pay -> ready-for-delivery
//                  The last step is a manager call; the car is reserved inside one transaction.
//   microservices: create -> approve -> waiting-for-payment -> pay
//                  Paying starts the saga. The order becomes READY_FOR_DELIVERY asynchronously
//                  (outbox -> Kafka -> storage-service -> outbox -> Kafka -> order-service);
//                  that part is measured afterwards from database timestamps (saga-report.sql),
//                  because polling the API here would add load of its own.
import http from 'k6/http';
import exec from 'k6/execution';
import { check } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { ARCH, BASE, CLIENT_ID, auth, carId, login, TREND_STATS } from './lib.js';

const RATE = Number(__ENV.RATE || 5);
const OFFSET = Number(__ENV.OFFSET || 0);

const flowDuration = new Trend('purchase_sync_duration', true);
const flowOk = new Rate('purchase_ok');

const STEPS = ['create', 'approve', 'waiting-for-payment', 'pay', 'ready-for-delivery'];

export const options = {
    summaryTrendStats: TREND_STATS,
    // Empty thresholds make k6 report every step as its own sub-metric.
    thresholds: Object.fromEntries(STEPS.map((s) => [`http_req_duration{name:${s}}`, []])),
    scenarios: {
        purchase: {
            executor: 'constant-arrival-rate',
            rate: RATE,
            timeUnit: '1s',
            duration: __ENV.DURATION || '60s',
            preAllocatedVUs: Math.max(10, RATE * 2),
            maxVUs: Math.max(100, RATE * 30),
        },
    },
};

export function setup() {
    return { user: login('user'), manager: login('manager') };
}

function step(method, url, token, name, body = null) {
    const res = http.request(method, url, body, auth(token, { tags: { name }, timeout: '30s' }));
    const ok = check(res, { [`${name} 2xx`]: (r) => r.status >= 200 && r.status < 300 });
    return ok ? res : null;
}

export default function (data) {
    const orders = `${BASE.orders}/api/orders/in-stock`;
    const started = Date.now();
    const car = carId(OFFSET + exec.scenario.iterationInTest + 1);

    const created = step('POST', orders, data.user, 'create',
        JSON.stringify({ clientId: CLIENT_ID, carId: car }));
    if (!created) { flowOk.add(false); return; }
    const id = created.json('id');

    const ok = step('PATCH', `${orders}/${id}/approve-by-manager`, data.manager, 'approve')
        && step('PATCH', `${orders}/${id}/waiting-for-payment`, data.manager, 'waiting-for-payment')
        && step('PATCH', `${orders}/${id}/pay`, data.user, 'pay')
        && (ARCH !== 'monolith'
            || step('PATCH', `${orders}/${id}/ready-for-delivery`, data.manager, 'ready-for-delivery'));

    flowOk.add(Boolean(ok));
    if (ok) {
        flowDuration.add(Date.now() - started);
    }
}
