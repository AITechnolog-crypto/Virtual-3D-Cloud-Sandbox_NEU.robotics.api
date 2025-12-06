// sw-mock-api.js — Lightweight Service Worker to mock backend APIs for local previews
// NOTE: This is a best‑effort mock to keep the dashboard interactive without a backend.
// It handles a subset of endpoints used by linelux-style.html. SSE endpoints are not emulated;
// the UI will automatically fall back to polling where possible.

const STATE = {
  mlPolicy: {
    trainingEnabled: true,
    ingestEnabled: true,
    mirrorTelemetryToDataset: false,
    retentionDays: 30,
    maxTrainPerHour: 6,
    allowExternalModels: false
  },
  mlDataset: [],
  mlModel: { trained: false, n: 0, slope: 0, intercept: 0, updated: 0 },
  federationAgg: { slope: 0, intercept: 0, avg: 0 },
  chat: { admin: [], customer: [] },
  lastEnergy: 2800,
  // Framio mock state: token and simple cache
  framio: {
    token: null,
    frames: {}
  },
  // DeepSeek key (mock)
  deepseekKey: null,
  // RunwayML mock state
  runway: {
    tasks: {}
  }
};

self.addEventListener('install', (evt) => {
  self.skipWaiting();
});

self.addEventListener('activate', (evt) => {
  evt.waitUntil(clients.claim());
});

function json(data, init = {}) {
  return new Response(JSON.stringify(data), {
    status: 200,
    headers: { 'Content-Type': 'application/json', ...(init.headers || {}) },
    ...init
  });
}

function notFound(msg){ return new Response(JSON.stringify({ error: msg||'Not found' }), { status: 404, headers:{'Content-Type':'application/json'} }); }
function badRequest(msg){ return new Response(JSON.stringify({ error: msg||'Bad request' }), { status: 400, headers:{'Content-Type':'application/json'} }); }

async function handleApi(req) {
  const url = new URL(req.url);
  const path = url.pathname;

  // DeepSeek/OpenAI chat mocks
  if (path === '/api/deepseek/chat' || path === '/api/openai/chat') {
    // note: accept optional header x-deepseek-key
    const keyHdr = req.headers.get('x-deepseek-key');
    if (keyHdr) STATE.deepseekKey = keyHdr;
    let note = STATE.deepseekKey ? ' (mit Key\u2714\ufe0f)' : ' (ohne Key)';
    return json({
      response: {
        id: 'mock-chat-1',
        created: Date.now(),
        choices: [{ index: 0, message: { role: 'assistant', content: 'Mock-Antwort (SW)'+note+': Ihre Anfrage wurde empfangen und positiv bewertet.' } }]
      }
    });
  }
  // DeepSeek key store (mock)
  if (path === '/api/deepseek/key' && req.method === 'POST'){
    try{
      const body = await req.clone().json().catch(()=>({}));
      const k = String(body.key||'').trim();
      if (!k) return badRequest('key fehlt');
      STATE.deepseekKey = k;
      const masked = k.slice(0,6)+'…'+k.slice(-4);
      return json({ ok:true, masked });
    }catch(e){ return badRequest('bad json'); }
  }

  // SERP
  if (path === '/api/serp/ping') {
    return json({ ok: true, hasKey: false });
  }
  if (path === '/api/serp/search') {
    const q = url.searchParams.get('q') || '';
    return json({ results: [
      { title: `Ergebnis zu "${q}"`, link: 'https://example.com', snippet: 'Dies ist ein Mock-Suchergebnis.' },
      { title: 'Zweites Ergebnis', link: 'https://example.org', snippet: 'Weitere Vorschau (Mock).' }
    ]});
  }
  if (path === '/api/serp/lexicon') {
    const block = Number(url.searchParams.get('block')||0) || 0;
    const blockSize = Number(url.searchParams.get('block_size')||10) || 10;
    return json({ block, blockSize, estimatedTotal: 1234, groups: {
      A: [{ title: 'Alpha', url: 'https://alpha.example', snippet: 'A-Eintrag', domain: 'alpha.example' }],
      B: [{ title: 'Beta',  url: 'https://beta.example',  snippet: 'B-Eintrag', domain: 'beta.example' }]
    }});
  }
  if (path === '/api/serp/fetch') {
    const target = url.searchParams.get('url') || 'about:blank';
    const html = `<!doctype html><html><head><meta charset="utf-8"><title>Vorschau</title></head><body><h1>Mock Vorschau</h1><p>Quelle: ${target}</p></body></html>`;
    return json({ html });
  }

  // Telemetry
  if (path === '/api/telemetry/status') {
    // simulate slight random walk
    STATE.lastEnergy = Math.max(0, STATE.lastEnergy + Math.round((Math.random()-0.5)*120));
    return json({ ts: Date.now(), metrics: { energy: STATE.lastEnergy, threats: 0, temperature: 22 } });
  }
  if (path === '/api/telemetry/push' && req.method === 'POST') {
    // accept any payload
    return json({ ok: true });
  }

  // ML policy and training
  if (path === '/api/ml/status') {
    return json({ policy: STATE.mlPolicy, model: STATE.mlModel });
  }
  if (path === '/api/ml/policy') {
    if (req.method === 'GET') return json(STATE.mlPolicy);
    if (req.method === 'POST') {
      const body = await req.clone().json().catch(()=>({}));
      STATE.mlPolicy = { ...STATE.mlPolicy, ...body };
      return json({ ok: true });
    }
  }
  if (path === '/api/ml/ingest' && req.method === 'POST') {
    const body = await req.clone().json().catch(()=>({}));
    STATE.mlDataset.push({ ...body, ts: Date.now() });
    return json({ ok: true });
  }
  if (path === '/api/ml/train' && req.method === 'POST') {
    const n = STATE.mlDataset.length;
    const slope = n > 1 ? 0.5 + Math.random() : 0; // dummy
    STATE.mlModel = { trained: true, n, slope, intercept: 0, updated: Date.now() };
    return json({ n, slope });
  }
  if (path === '/api/ml/predict') {
    const steps = Number(new URL(req.url).searchParams.get('steps')||'10') || 10;
    const base = STATE.lastEnergy || 2500;
    const arr = Array.from({ length: steps }, (_, i) => base + i * (STATE.mlModel.slope || 1));
    return json({ predictions: arr });
  }

  // Federation
  if (path === '/api/federation/submitModel' && req.method === 'POST') {
    const body = await req.clone().json().catch(()=>({}));
    // naïve aggregate: overwrite
    STATE.federationAgg = { slope: Number(body.slope)||0, intercept: Number(body.intercept)||0, avg: Number(body.avg)||0 };
    return json({ ok: true });
  }
  if (path === '/api/federation/aggregate') {
    return json(STATE.federationAgg);
  }
  if (path === '/api/federation/adoptAggregate' && req.method === 'POST') {
    // pretend adoption
    return json({ ok: true });
  }

  // Azure Prediction
  if (path === '/api/azure/predict' && req.method === 'POST') {
    const body = await req.clone().text().catch(()=> '{}');
    return json({ response: { echo: body, note: 'Mock Azure Vorhersage' } });
  }

  // Consent & Warmup (mock)
  if (path === '/api/consent' && req.method === 'POST') {
    return json({ status: 'ok' });
  }
  if (path === '/api/warmup' && req.method === 'POST') {
    return json({ status: 'warmup requested' });
  }

  // Chat (very light mock)
  if (path === '/api/chat' && req.method === 'POST') {
    try{
      const body = await req.clone().json().catch(()=>({}));
      const msg = String(body.message||'').trim();
      const low = msg.toLowerCase();
      if (low.includes('plan')){
        return json({ reply: "🌸 Aiki flüstert: 'Ein Plan ist wie Tau am Morgen – handle sanft, und er bleibt bestehen.'" });
      }
      return json({ reply: msg ? ("🌙 Aiki: 'Deine Eingabe wurde in die Wolken geschrieben. Alles ist bereit.'") : 'Mock: Hallo! Wie kann ich helfen?' });
    }catch(e){ return json({ reply: 'Mock: Hallo!' }); }
  }
  if (path === '/api/chat/history') {
    const ch = url.searchParams.get('channel') || 'customer';
    return json(STATE.chat[ch] || []);
  }
  if (path === '/api/chat/send' && req.method === 'POST') {
    const body = await req.clone().json().catch(()=>({}));
    const ch = body.channel || 'customer';
    const m = { ts: Date.now(), role: body.role || 'user', sender: body.sender || 'user', text: String(body.text||'') };
    STATE.chat[ch] = (STATE.chat[ch]||[]).concat(m).slice(-200);
    return json(m);
  }
  if (path === '/api/chat/assist' && req.method === 'POST') {
    const body = await req.clone().json().catch(()=>({}));
    const ch = body.channel || 'customer';
    const m = { ts: Date.now(), role: 'assistant', sender: 'assistant', text: 'Mock‑Assist: '+(body.message||'') };
    STATE.chat[ch] = (STATE.chat[ch]||[]).concat(m).slice(-200);
    return json(m);
  }
  if (path === '/api/chat/verify') {
    return json({ valid: true, headHash: 'deadbeefcafebabe' });
  }
  if (path === '/api/chat/stream') {
    // SSE not supported in SW mock; return 404 so client won’t connect
    return notFound('SSE not supported in mock');
  }

  // Framio API mock
  // POST /api/framio/token { token: 'fio-u-...' } -> store token (masked in response)
  if (path === '/api/framio/token' && req.method === 'POST') {
    const body = await req.clone().json().catch(()=>({}));
    const token = String(body.token||'').trim();
    if (!token) return badRequest('Token fehlt');
    if (!/^fio-u-/.test(token)) return badRequest('Ungültiger Token (muss mit fio-u- beginnen)');
    STATE.framio.token = token;
    const masked = token.slice(0, 7) + '…' + token.slice(-6);
    return json({ ok: true, masked });
  }
  // GET /api/framio/ping -> ok + hasToken
  if (path === '/api/framio/ping') {
    return json({ ok: true, time: new Date().toISOString(), hasToken: !!STATE.framio.token });
  }
  // Auth helper
  function requireFramioToken(){
    if (!STATE.framio.token) return notFound('Framio Token nicht gesetzt');
    return null;
  }
  // GET /api/framio/profile -> returns demo profile; requires token
  if (path === '/api/framio/profile') {
    const err = requireFramioToken(); if (err) return new Response(JSON.stringify({ error: 'Unauthorized' }), { status: 401, headers:{'Content-Type':'application/json'} });
    const masked = STATE.framio.token ? STATE.framio.token.slice(0,7)+'…'+STATE.framio.token.slice(-6) : null;
    return json({ id: 'framio-user-1', name: 'Framio Nutzer', plan: 'sandbox', token: masked });
  }
  // POST /api/framio/echo -> echoes payload; requires token
  if (path === '/api/framio/echo' && req.method === 'POST') {
    const err = requireFramioToken(); if (err) return new Response(JSON.stringify({ error: 'Unauthorized' }), { status: 401, headers:{'Content-Type':'application/json'} });
    const body = await req.clone().json().catch(()=>({}));
    return json({ ok: true, echo: body, ts: Date.now() });
  }
  // GET /api/framio/frames?id=123 -> returns mock frame by id; requires token
  if (path === '/api/framio/frames') {
    const err = requireFramioToken(); if (err) return new Response(JSON.stringify({ error: 'Unauthorized' }), { status: 401, headers:{'Content-Type':'application/json'} });
    const id = new URL(req.url).searchParams.get('id') || 'demo';
    if (!STATE.framio.frames[id]) {
      STATE.framio.frames[id] = { id, title: 'Frame '+id, vectors: Array.from({length:8},()=> +(Math.random()*1).toFixed(3)) };
    }
    return json(STATE.framio.frames[id]);
  }

  // Meta: Java-Zähler (Mock) und Robotics-Fallback
  if (path === '/api/meta/java-counts') {
    // Leichte, statische Mock-Werte für sichtbare Zähler im Tradebot-UI
    return json({ controllers: 12, services: 24, repositories: 18, models: 30 });
  }
  if (path === '/api/robots/count') {
    // Fallback-Zahl, falls kein echter Robots-Endpoint existiert
    return json(2);
  }
  if (path === '/api/drones/count') {
    // Fallback-Zahl für Drohnen, falls kein echter Endpoint existiert
    return json(3);
  }

  // Finance overview/flow (Mock for Einnahmen/Rückfluss)
  if (path === '/api/finance/flow') {
    // simple random walk per hour figures
    const inc = 1200 + Math.round((Math.random()-0.5)*80);
    const exp = 850 + Math.round((Math.random()-0.5)*60);
    return json({ incomePerH: inc, expensePerH: exp, netPerH: inc - exp });
  }
  if (path === '/api/finance/overview') {
    const wallets = [
      { id:1, name:'Main Wallet', balance:15420.50, currency:'EUR' },
      { id:2, name:'Ops Wallet',  balance:2210.00,  currency:'EUR' },
      { id:3, name:'Green Fund',  balance:3420.10,  currency:'EUR' }
    ];
    const incomePerH = 1250, expensePerH = 860;
    const total = wallets.reduce((s,w)=> s + Number(w.balance||0), 0);
    return json({ wallets, total, flow: { incomePerH, expensePerH, netPerH: incomePerH - expensePerH } });
  }
  if (path === '/api/transactions/count') {
    return json(3);
  }

  // Store external ant API key (masked) — optional for future backend usage
  if (path === '/api/keys/ant' && req.method === 'POST') {
    const body = await req.clone().json().catch(()=>({}));
    const token = String(body.token||'').trim();
    if (!token || !/^sk-ant-/.test(token)) return badRequest('Ungültiger Token (erwartet sk-ant-...)');
    STATE.antKey = token;
    const masked = token.slice(0, 7) + '…' + token.slice(-6);
    return json({ ok:true, masked });
  }

  // Aerial View lookup (mock/proxy)
  // GET /api/aerial/lookup?address=...&videoId=...&format=mp4_medium&orientation=landscape
  if (path === '/api/aerial/lookup') {
    const q = url.searchParams;
    const format = (q.get('format') || 'mp4_medium').toUpperCase();
    const orientation = (q.get('orientation') || 'landscape').toLowerCase();
    const dummyImg = 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/50/Googleplex_HQ_%28cropped%29.jpg/640px-Googleplex_HQ_%28cropped%29.jpg';
    const dummyMp4 = 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4';
    const uris = {
      IMAGE: { landscapeUri: dummyImg, portraitUri: dummyImg },
      MP4_LOW: { landscapeUri: dummyMp4, portraitUri: dummyMp4 },
      MP4_MEDIUM: { landscapeUri: dummyMp4, portraitUri: dummyMp4 },
      MP4_HIGH: { landscapeUri: dummyMp4, portraitUri: dummyMp4 }
    };
    return json({ state: 'READY', uris });
  }

  // Vision ping
  if (path === '/api/vision/ping') {
    return json({ ok: true, time: new Date().toISOString() });
  }

  // RunwayML Image-to-Video (Mock)
  // POST /api/runway/image-to-video  { promptImage, model, ratio, seed }
  if (path === '/api/runway/image-to-video' && req.method === 'POST') {
    try{
      const body = await req.clone().json().catch(()=>({}));
      let urlStr = String(body.promptImage||'').trim();
      if (urlStr.startsWith('htttp:')) urlStr = urlStr.replace(/^htttp:/,'http:');
      if (urlStr.startsWith('htttps:')) urlStr = urlStr.replace(/^htttps:/,'https:');
      const id = 'runway-' + Date.now() + '-' + Math.random().toString(36).slice(2,8);
      STATE.runway.tasks[id] = {
        id,
        kind: 'image_to_video',
        createdAt: Date.now(),
        promptImage: urlStr || 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/50/Googleplex_HQ_%28cropped%29.jpg/640px-Googleplex_HQ_%28cropped%29.jpg',
        model: String(body.model||'gen4_turbo'),
        ratio: String(body.ratio||'1280:768'),
        seed: body.seed==null? undefined : body.seed,
        status: 'PENDING',
        step: 0,
        result: null
      };
      return json({ id, status: 'PENDING' });
    }catch(e){ return badRequest('bad json'); }
  }
  // GET /api/runway/tasks?id=...
  if (path === '/api/runway/tasks') {
    const id = new URL(req.url).searchParams.get('id')||'';
    const t = STATE.runway.tasks[id];
    if (!t) return notFound('task not found');
    // advance status deterministically by calls
    t.step = (t.step||0) + 1;
    if (t.step >= 3) {
      t.status = 'SUCCEEDED';
      t.result = {
        videoUrl: 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
        coverImage: t.promptImage
      };
    } else if (t.step >= 2) {
      t.status = 'PROCESSING';
    } else {
      t.status = 'PENDING';
    }
    return json({ id: t.id, status: t.status, result: t.result });
  }

  // Galaxy generate (prompt → params)
  if (path === '/api/galaxy/generate' && (req.method === 'POST' || req.method === 'GET')) {
    try{
      let body = {};
      if (req.method === 'POST') {
        body = await req.clone().json().catch(()=>({}));
      } else {
        const u = new URL(req.url);
        body = { prompt: u.searchParams.get('prompt')||'', mirror: u.searchParams.get('mirror')==='1', seed: u.searchParams.get('seed') };
      }
      const prompt = String(body.prompt||'').toLowerCase();
      const mirror = !!body.mirror;
      const seed = (body.seed!=null? String(body.seed) : prompt) || 'seed';
      function hash(s){ let h=2166136261>>>0; for(let i=0;i<s.length;i++){ h^=s.charCodeAt(i); h = Math.imul(h, 16777619); } return h>>>0; }
      let rnd = (function(){ let x = (hash(seed) % 2147483647) + 1; return ()=> (x = x * 48271 % 2147483647) / 2147483647; })();
      function pick(arr){ return arr[Math.floor(rnd()*arr.length)]; }
      // Palette aus Prompt ableiten
      const palettes = {
        blau: ['#66e0ff','#2288ff','#a8ffdf'],
        rot: ['#ff6688','#ff2255','#ffd1d8'],
        gruen: ['#a8ffdf','#00ff88','#228866'],
        violett: ['#aa88ff','#8844ff','#ffccff'],
        gold: ['#ffd18a','#ffbb33','#ffeecc']
      };
      let palette = palettes.blau;
      if (/rot|red/.test(prompt)) palette = palettes.rot;
      else if (/(gr[üu]n|green)/.test(prompt)) palette = palettes.gruen;
      else if (/violett|lila|purple/.test(prompt)) palette = palettes.violett;
      else if (/gold|gelb|sun/.test(prompt)) palette = palettes.gold;
      // Arme & Sterne aus Prompt ableiten
      let arms = /arm[e]?[ :]?([2-8])/.test(prompt) ? Number(RegExp.$1) : (2 + Math.floor(rnd()*4))*1; // 2..5
      arms = Math.max(2, Math.min(8, arms));
      let stars = /([0-9]{2,6})\s*(sterne|stars)/.test(prompt) ? Math.min(120000, Math.max(3000, Number(RegExp.$1))) : 36000 + Math.floor(rnd()*24000);
      let radius = 280 + Math.floor(rnd()*220);
      let twist = 0.6 + rnd()*1.2; // Spiralverdrehung
      const params = { arms, stars, radius, twist, palette, mirror, seed };
      return json({ ok:true, params });
    }catch(e){ return badRequest('Galaxy parse error'); }
  }

  // Default: let it pass through
  return fetch(req);
}

self.addEventListener('fetch', (evt) => {
  const req = evt.request;
  const url = new URL(req.url);
  // API routes handled by mock
  if (url.pathname.startsWith('/api/')) {
    evt.respondWith(handleApi(req));
    return;
  }

  // Dynamic HTML variants: /variants/variant-XX.html (XX = 01..50)
  // If a specific file is missing, synthesize a minimal variant page on the fly.
  try {
    const m = url.pathname.match(/\/variants\/variant-(\d{2})\.html$/);
    const acceptHtml = (req.headers.get('accept') || '').includes('text/html');
    if (m && acceptHtml && req.method === 'GET') {
      const num = parseInt(m[1], 10);
      // First, try network (existing files should win)
      evt.respondWith((async () => {
        try {
          const netRes = await fetch(req);
          if (netRes && netRes.ok) return netRes;
        } catch (_) { /* fall through to synth */ }
        // Synthesize
        const hue = Math.round((num / 50) * 360);
        const html = `<!doctype html><html lang="de"><head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>LineLux Variante ${m[1]}</title>
<link rel="stylesheet" href="../LineLuxSharedStyles.css">
<style>:root{--h:${hue}} body{background:hsl(var(--h),35%,8%);color:#eef6ff;font-family:system-ui,Segoe UI,Roboto,Arial}.wrap{max-width:980px;margin:0 auto;padding:18px}h1{font-size:24px;margin:8px 0 12px;background:linear-gradient(90deg,hsl(var(--h),90%,60%),#00ccff);-webkit-background-clip:text;color:transparent}.card{background:rgba(255,255,255,.06);border:1px solid rgba(255,255,255,.12);border-radius:12px;padding:12px}a{color:hsl(var(--h),80%,70%)}</style>
</head><body>
<div class="wrap">
  <h1>LineLux – Variante ${m[1]}</h1>
  <div class="card">
    <p>Automatisch generierte Minimal‑Variante Nr. ${m[1]} • Farbschema Hue=${hue}°.</p>
    <ul>
      <li><a href="../linelux-style.html">Zur Haupt‑UI</a></li>
      <li><a href="variants-index.html">Varianten‑Übersicht</a></li>
    </ul>
  </div>
</div>
</body></html>`;
        return new Response(html, { status: 200, headers: { 'Content-Type': 'text/html; charset=utf-8' } });
      })());
      return;
    }
  } catch (e) { /* ignore and fall through */ }

  // Dynamic Aerial variants: /variants/av-XX.html (01..100)
  try {
    const m2 = url.pathname.match(/\/variants\/av-(\d{2,3})\.html$/);
    const acceptHtml2 = (req.headers.get('accept') || '').includes('text/html');
    if (m2 && acceptHtml2 && req.method === 'GET') {
      const num = parseInt(m2[1], 10);
      evt.respondWith((async () => {
        try {
          const netRes = await fetch(req);
          if (netRes && netRes.ok) return netRes;
        } catch (_) {}
        const nStr = String(num).padStart(2,'0');
        const hue = Math.round(((num % 100) / 100) * 360);
        const html = `<!doctype html><html lang="de"><head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Aerial Variante ${nStr}</title>
<link rel="stylesheet" href="../LineLuxSharedStyles.css">
<style>:root{--h:${hue}} body{background:hsl(var(--h),35%,8%);color:#eef6ff;font-family:system-ui,Segoe UI,Roboto,Arial}.wrap{max-width:980px;margin:0 auto;padding:18px}h1{font-size:24px;margin:8px 0 12px;background:linear-gradient(90deg,hsl(var(--h),90%,60%),#00ccff);-webkit-background-clip:text;color:transparent}.card{background:rgba(255,255,255,.06);border:1px solid rgba(255,255,255,.12);border-radius:12px;padding:12px}a{color:hsl(var(--h),80%,70%)}</style>
</head><body>
<div class="wrap">
  <h1>Aerial – Variante ${nStr}</h1>
  <div class="card">
    <p>Auto‑Variante mit Aerial‑Demo. Öffne die Aerial‑Seite oder diesen Clip:</p>
    <ul>
      <li><a href="../aerial-view.html">Aerial‑View Demo</a></li>
      <li><a href="../aerial-view.html?address=${encodeURIComponent('1600 Amphitheatre Parkway, Mountain View, CA')}&format=mp4_medium&orientation=landscape">Beispiel‑Video</a></li>
      <li><a href="aerial-variants-index.html">Aerial‑Varianten‑Übersicht</a></li>
    </ul>
  </div>
</div>
<script>if('serviceWorker'in navigator){addEventListener('load',()=>navigator.serviceWorker.register('../sw-mock-api.js').catch(()=>{}))}</script>
</body></html>`;
        return new Response(html, { status: 200, headers: { 'Content-Type': 'text/html; charset=utf-8' } });
      })());
      return;
    }
  } catch (e) {}

  // Dynamic Material variants: /variants/mat-XX.html (01..120)
  try {
    const m3 = url.pathname.match(/\/variants\/mat-(\d{2,3})\.html$/);
    const acceptHtml3 = (req.headers.get('accept') || '').includes('text/html');
    if (m3 && acceptHtml3 && req.method === 'GET') {
      const num = parseInt(m3[1], 10);
      evt.respondWith((async () => {
        try {
          const netRes = await fetch(req);
          if (netRes && netRes.ok) return netRes;
        } catch (_) {}
        const nStr = String(num).padStart(2,'0');
        const hue = Math.round(((num % 120) / 120) * 360);
        const html = `<!doctype html><html lang="de"><head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Material Seite ${nStr}</title>
<link rel="stylesheet" href="../LineLuxSharedStyles.css">
<style>:root{--h:${hue}} body{background:hsl(var(--h),35%,8%);color:#eef6ff;font-family:system-ui,Segoe UI,Roboto,Arial}.wrap{max-width:980px;margin:0 auto;padding:18px}h1{font-size:24px;margin:8px 0 12px;background:linear-gradient(90deg,hsl(var(--h),90%,60%),#00ccff);-webkit-background-clip:text;color:transparent}.card{background:rgba(255,255,255,.06);border:1px solid rgba(255,255,255,.12);border-radius:12px;padding:12px}a{color:hsl(var(--h),80%,70%)}</style>
</head><body>
<div class="wrap">
  <h1>Material – Seite ${nStr}</h1>
  <div class="card">
    <p>Automatisch generierte HTML‑Materialseite Nr. ${nStr} • Farbschema Hue=${hue}°.</p>
    <ul>
      <li><a href="material-variants-index.html">Material‑Index</a></li>
      <li><a href="variants-index.html">Varianten‑Übersicht</a></li>
      <li><a href="../linelux-style.html">Haupt‑UI</a></li>
    </ul>
  </div>
</div>
<script>if('serviceWorker'in navigator){addEventListener('load',()=>navigator.serviceWorker.register('../sw-mock-api.js').catch(()=>{}))}</script>
</body></html>`;
        return new Response(html, { status: 200, headers: { 'Content-Type': 'text/html; charset=utf-8' } });
      })());
      return;
    }
  } catch (e) {}

  // Default: pass through
  // Note: we avoid caching here to always reflect latest static files during development.
  // You may add caching if desired.
  // No respondWith => let browser fetch normally.
});
