# SaaS Integration – Oracle Events & Node DSN

This note summarizes the backend pieces referenced by the dashboard’s SaaS card.

## Azure Functions base

Use your Azure Functions URL as BASE (example):

```
https://<deine-function>.azurewebsites.net
```

### Quick tests

```js
fetch("https://<deine-function>.azurewebsites.net/health")
  .then(r=>r.json()).then(console.log);

fetch("https://<deine-function>.azurewebsites.net/infer",{
  method: "POST",
  headers: { 'Content-Type':'application/json' },
  body: JSON.stringify({ pose:{x:0,y:0,theta:0}, prox:{front:2.0} })
}).then(r=>r.json()).then(console.log);
```

If your function requires a token, add `Authorization: Bearer <API_TOKEN>`.

---

## Oracle table for events

Example DDL (Oracle):

```sql
CREATE TABLE NUR_EVENTS (
  ID        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  TS_MS     NUMBER(13),
  KIND      VARCHAR2(16),
  PAYLOAD   CLOB
);
```

Example event payloads your `/events` endpoint may receive:

```json
{ "type": "hal", "data": { "pose": {"x":0.2,"y":-0.1,"theta":0.7}, "prox": {"front":1.8} } }
```

```json
{ "pose": {"x":0.2,"y":-0.1,"theta":0.7}, "prox": {"front":1.8}, "vision": {"x":-0.15,"size":0.04} }
```

Persist as-is in PAYLOAD; store a millisecond timestamp in TS_MS.

---

## Node oracledb – classic DSN

```js
const dsn = "(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=<host>)(PORT=1521))\n"+
            "(CONNECT_DATA=(SERVICE_NAME=<service>)))";
```

Use with `oracledb.getConnection({ user, password, connectString: dsn })`.

---

## Infer response shape

Example response from `/infer` your front-end expects:

```json
{ "left": 0.46, "right": 0.72, "device": "cpu|cuda" }
```

The dashboard’s `saas-client.js` uses:

```js
const BASE = "https://<deine-function>.azurewebsites.net";
const HEAD = { "Content-Type":"application/json", "Authorization":"Bearer <API_TOKEN>" };

async function inferAssist(state){
  const r = await fetch(BASE+"/infer", { method:"POST", headers: HEAD, body: JSON.stringify(state) });
  return r.json();
}
async function sendEvent(ev){
  await fetch(BASE+"/events", { method:"POST", headers: HEAD, body: JSON.stringify(ev) });
}
```

These helpers are also exposed by the UI via the SaaS card.
