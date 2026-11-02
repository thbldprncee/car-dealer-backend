import http from 'k6/http';

// ARCH=monolith | microservices. Both stacks name their hosts the same way inside
// their own docker network, so the scripts run unchanged against either of them.
export const ARCH = __ENV.ARCH || 'monolith';

export const BASE = ARCH === 'monolith'
    ? { orders: 'http://app:8080', cars: 'http://app:8080' }
    : { orders: 'http://order-service:8080', cars: 'http://storage-service:8081' };

export const CLIENT_ID = '11111111-1111-1111-1111-111111111111';

export function carId(n) {
    return `cccccccc-0000-4000-8000-${String(n).padStart(12, '0')}`;
}

// Tokens are requested once per run in setup(): Keycloak is not what is being measured.
// The default access token lifespan is 5 minutes, so every run stays well below it.
export function login(username) {
    const res = http.post('http://keycloak:8080/realms/car-dealer/protocol/openid-connect/token', {
        client_id: 'car-dealer-backend',
        grant_type: 'password',
        username,
        password: username,
    }, { responseType: 'text' });
    if (res.status !== 200) {
        throw new Error(`login ${username}: ${res.status} ${res.body}`);
    }
    return res.json('access_token');
}

export function auth(token, extra = {}) {
    return { headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }, ...extra };
}

export const TREND_STATS = ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'];
