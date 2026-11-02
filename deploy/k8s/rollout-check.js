// Steady traffic through both services while deploy.sh restarts them one after the other.
// A zero-downtime rollout means no failed request: new pods take traffic only once ready,
// and old pods finish what they have before they stop.
import http from 'k6/http';
import { check } from 'k6';

const NODE = 'http://car-dealer-control-plane';
const CAR = 'cccccccc-cccc-cccc-cccc-ccccccccccc2';

export const options = {
    discardResponseBodies: true,
    summaryTrendStats: ['med', 'p(95)', 'p(99)', 'max'],
    thresholds: { http_req_failed: ['rate==0'] },
    scenarios: {
        reads: {
            executor: 'constant-arrival-rate',
            rate: 50,
            timeUnit: '1s',
            duration: __ENV.DURATION || '150s',
            preAllocatedVUs: 20,
            maxVUs: 200,
        },
    },
};

export function setup() {
    const res = http.post(`${NODE}:30180/realms/car-dealer/protocol/openid-connect/token`, {
        client_id: 'car-dealer-backend', grant_type: 'password', username: 'user', password: 'user',
    }, { responseType: 'text' });
    if (res.status !== 200) {
        throw new Error(`login: ${res.status} ${res.body}`);
    }
    return { token: res.json('access_token') };
}

export default function (data) {
    const params = { headers: { Authorization: `Bearer ${data.token}` }, timeout: '10s' };
    // order-service -> gRPC -> storage-service, then storage-service directly.
    const viaOrders = http.get(`${NODE}:30080/api/v1/cars/${CAR}`, { ...params, tags: { name: 'order-service' } });
    const direct = http.get(`${NODE}:30081/api/cars/${CAR}`, { ...params, tags: { name: 'storage-service' } });
    check(viaOrders, { 'order-service 200': (r) => r.status === 200 });
    check(direct, { 'storage-service 200': (r) => r.status === 200 });
}
