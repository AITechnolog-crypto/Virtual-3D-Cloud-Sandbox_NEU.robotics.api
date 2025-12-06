# Framio API (Mock via Service Worker)

This project provides a lightweight, client‑side mock of a "Framio" API implemented inside `src/main/resources/static/sw-mock-api.js`. It enables UI development without a backend.

The mock requires a token with prefix `fio-u-` and supports a few simple endpoints.

## Endpoints

Base: /api/framio

- POST /token
  - Body: `{ "token": "fio-u-..." }`
  - Stores the token in the SW mock. Returns `{ ok: true, masked: "fio-u-…abc123" }`.
  - Token must start with `fio-u-`.

- GET /ping
  - Returns `{ ok: true, time: ISO, hasToken: boolean }`.

- GET /profile
  - Requires token set. Returns a demo profile: `{ id, name, plan, token: masked }`.

- POST /echo
  - Requires token set. Returns: `{ ok: true, echo: <your body>, ts }`.

- GET /frames?id=<id>
  - Requires token set. Returns a mock frame object `{ id, title, vectors:[...] }` (cached by id during session).

If a token is required but not present, the mock responds with HTTP 401 and `{ error: "Unauthorized" }`.

## Quick test (in browser console)

```js
const API = location.origin + '/api/framio';

// 1) Set token (replace with your token)
await fetch(API + '/token', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ token: 'fio-u-7lHHhkbfOKsgodfRRril3aDfgT6F3noZ9KeC5ve8rNamlDVMyNYLrspKqp9YnwF5' }) }).then(r=>r.json());

// 2) Ping
await fetch(API + '/ping').then(r=>r.json());

// 3) Profile
await fetch(API + '/profile').then(r=>r.json());

// 4) Echo
await fetch(API + '/echo', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ hello:'framio' }) }).then(r=>r.json());

// 5) Frames
await fetch(API + '/frames?id=42').then(r=>r.json());
```

## Notes
- These endpoints live entirely in the Service Worker and are meant for local preview (e.g., JetBrains 63342 or a simple static server). They are not persisted across full reloads or browser restarts.
- If you want to add a UI card later, call these endpoints from your page and show the results.
