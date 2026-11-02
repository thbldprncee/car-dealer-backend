// Read path: a client opens a car card. A fixed number of requests per second (RATE)
// arrives regardless of how fast the system answers, so saturation shows up as latency
// and dropped iterations instead of a quietly lower request rate.
//
// READ_PATH (microservices only):
//   direct - GET storage-service /api/cars/{id}
//   grpc   - GET order-service /api/v1/cars/{id}, which calls storage-service over gRPC
import http from 'k6/http';
import { check } from 'k6';
import { ARCH, BASE, auth, carId, login, TREND_STATS } from './lib.js';

const RATE = Number(__ENV.RATE || 200);
const READ_PATH = __ENV.READ_PATH || 'direct';

// Cars 900001..901000 are never ordered by purchase.js, so they always stay available.
const FIRST_CAR = 900001;
const CARS = 1000;

export const options = {
    discardResponseBodies: true,
    summaryTrendStats: TREND_STATS,
    // Reports the measured request on its own, without the login in setup().
    thresholds: { 'http_req_duration{name:GET car}': [] },
    scenarios: {
        read: {
            executor: 'constant-arrival-rate',
            rate: RATE,
            timeUnit: '1s',
            duration: __ENV.DURATION || '40s',
            preAllocatedVUs: Math.min(RATE, 400),
            maxVUs: 1500,
        },
    },
};

function url(id) {
    if (ARCH === 'microservices' && READ_PATH === 'grpc') {
        return `${BASE.orders}/api/v1/cars/${id}`;
    }
    return `${BASE.cars}/api/cars/${id}`;
}

export function setup() {
    return { token: login('user') };
}

export default function (data) {
    const id = carId(FIRST_CAR + Math.floor(Math.random() * CARS));
    const res = http.get(url(id), auth(data.token, { tags: { name: 'GET car' }, timeout: '10s' }));
    check(res, { 'status 200': (r) => r.status === 200 });
}
