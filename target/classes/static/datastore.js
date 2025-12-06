// datastore.js — schlanker Speicher mit drei Treibern (Local | File | Http)
// Minimal-invasive Einbindung: Polyfill für bus, Default-Store, Export/Import & Usage-Anzeige
(function(){
  'use strict';

  // ---------- Utils
  function safeJSONParse(s, fallback){ try{ return JSON.parse(s); }catch(_){ return fallback; } }
  function lsGet(k, d){ try{ const v = localStorage.getItem(k); return v==null? d : v; }catch(_){ return d; } }
  function lsSet(k, v){ try{ localStorage.setItem(k, v); }catch(_){ } }

  // ---------- Store Factory
  const Store = (function(){
    function load(key){ return safeJSONParse(lsGet(key, '[]'), []); }
    function save(key, arr){ lsSet(key, JSON.stringify(arr)); }

    class Local {
      constructor({ key='nur.events', budgetKB=256 }={}){
        this.key = key;
        this.cap = budgetKB * 1024;
        this.buf = load(this.key);
      }
      append(ev){ this.buf.push(ev); this.gc(); save(this.key, this.buf); }
      list(){ return this.buf.slice(); }
      clear(){ this.buf = []; save(this.key, this.buf); }
      usage(){ const bytes = (lsGet(this.key, '')||'').length; return { bytes, cap: this.cap }; }
      gc(){ let s = JSON.stringify(this.buf); while (s.length > this.cap){ this.buf.shift(); s = JSON.stringify(this.buf); } }
    }

    class File { // Export/Import als JSON-Datei
      constructor(opts){ this.local = new Local(opts||{}); }
      append(e){ this.local.append(e); }
      list(){ return this.local.list(); }
      clear(){ this.local.clear(); }
      usage(){ return this.local.usage(); }
      export(){ try{ const a=document.createElement('a'); a.href=URL.createObjectURL(new Blob([JSON.stringify(this.local.list())],{type:'application/json'})); a.download='events.json'; a.click(); setTimeout(()=>URL.revokeObjectURL(a.href), 0); }catch(_){}}
      import(text){ const arr = safeJSONParse(text, null); if (Array.isArray(arr)){ this.local.clear(); arr.forEach(e=> this.local.append(e)); }
      }
    }

    class Http { // versucht POST, fällt bei Fehlern auf Local zurück
      constructor({ base=location.origin+'/api', key='nur.events', budgetKB=256 }={}){ this.base = base; this.local = new Local({ key, budgetKB }); }
      async append(ev){ this.local.append(ev); try{ await fetch(this.base + '/events', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(ev) }); }catch(_){ /* fallback already appended */ } }
      async list(){ try{ const r = await fetch(this.base + '/events', { cache:'no-store' }); if (r.ok) return await r.json(); }catch(_){ } return this.local.list(); }
      clear(){ this.local.clear(); }
      async usage(){ return this.local.usage(); }
    }

    function make(opts){ const d = (opts && opts.driver) || 'local';
      if (d === 'http') return new Http(opts||{});
      if (d === 'file') return new File(opts||{});
      return new Local(opts||{});
    }

    return { make };
  })();

  // Expose
  try{ window.Store = Store; }catch(_){ }

  // ---------- Mini-Bus Polyfill
  (function(){
    if (window.bus && typeof window.bus.on === 'function') return;
    const listeners = Object.create(null);
    window.bus = {
      on(type, cb){ if(!listeners[type]) listeners[type]=[]; listeners[type].push(cb); return ()=> this.off(type, cb); },
      off(type, cb){ const arr=listeners[type]||[]; const i=arr.indexOf(cb); if(i>=0) arr.splice(i,1); },
      emit(type, payload){ const arr=(listeners[type]||[]).slice(); arr.forEach(fn=>{ try{ fn({ type, payload }); }catch(_){ } }); }
    };
  })();

  // ---------- Default-Store initialisieren (falls nicht vorhanden)
  const DEFAULT_KEY = 'nur.events';
  const DEFAULT_BUDGET_KB = Number(lsGet('nur.store.budgetKB','256')) || 256;
  if (!window.__eventStore){
    try{ window.__eventStore = Store.make({ driver: 'local', key: DEFAULT_KEY, budgetKB: DEFAULT_BUDGET_KB }); }catch(_){ }
  }
  if (!window.store){ try{ window.store = window.__eventStore; }catch(_){ } }

  // ---------- Event-Wiring: hal / learn / vision
  try{
    const store = window.__eventStore;
    const append = (type, payload)=>{ try{ store.append({ t: Date.now(), type, data: payload }); }catch(_){ } };
    window.bus.on('hal:sensors', m=> append('hal',   (m && m.payload)!=null ? m.payload : m));
    window.bus.on('learn:stats', m=> append('learn', (m && m.payload)!=null ? m.payload : m));
    window.bus.on('vision:blob', m=> append('vision',(m && m.payload)!=null ? m.payload : m));
  }catch(_){ }

  // ---------- Bridge: NurTrio Testbench postMessage -> bus.emit
  try{
    window.addEventListener('message', (ev)=>{
      const d = ev && ev.data; if (!d || d.ns !== 'NurTrioV1') return;
      if (typeof d.type === 'string'){ window.bus.emit(d.type, { source:'postMessage', payload: d }); }
    });
  }catch(_){ }

  // ---------- Export/Import Helper (Buttons oder Mini-Toolbox)
  function ensureButtons(){
    function bind(){
      try{
        const store = window.__eventStore;
        const btnEx = document.getElementById('btnExport');
        const btnIm = document.getElementById('btnImport');
        if (btnEx && !btnEx.__wired){ btnEx.__wired=true; btnEx.addEventListener('click', ()=>{ if (store.export) store.export(); else { const a=document.createElement('a'); a.href=URL.createObjectURL(new Blob([JSON.stringify(store.list())],{type:'application/json'})); a.download='events.json'; a.click(); } }); }
        if (btnIm && !btnIm.__wired){ btnIm.__wired=true; btnIm.addEventListener('click', ()=>{ const i=document.createElement('input'); i.type='file'; i.accept='application/json'; i.onchange=()=>{ const f=i.files && i.files[0]; if(!f) return; const r=new FileReader(); r.onload=()=>{ try{ if (store.import) store.import(r.result); else lsSet(DEFAULT_KEY, r.result); location.reload(); }catch(_){ } }; r.readAsText(f); }; i.click(); }); }
        return !!(btnEx||btnIm);
      }catch(_){ return false; }
    }
    if (bind()) return; // Buttons existieren
    // Falls Buttons fehlen: kleine, dezente Toolbox einblenden (unten rechts)
    const host = document.createElement('div');
    host.id = 'storeToolbox';
    host.style.cssText = 'position:fixed;right:12px;bottom:12px;z-index:99998;display:flex;gap:6px;';
    const b1 = document.createElement('button'); b1.textContent='Export'; b1.id='btnExport';
    const b2 = document.createElement('button'); b2.textContent='Import'; b2.id='btnImport';
    [b1,b2].forEach(b=>{ b.style.cssText='background:rgba(255,255,255,.06);color:#e8f2ff;border:1px solid rgba(255,255,255,.15);padding:6px 10px;border-radius:10px;cursor:pointer' });
    host.appendChild(b1); host.appendChild(b2);
    document.addEventListener('DOMContentLoaded', ()=>{ try{ document.body.appendChild(host); bind(); }catch(_){ } });
  }
  ensureButtons();

  // ---------- Usage Monitor + DevBadge-Integration
  (function(){
    let storeUsed = '';
    async function update(){
      try{ const u = await (window.__eventStore && window.__eventStore.usage ? window.__eventStore.usage() : {bytes:0,cap:1});
        storeUsed = (u && u.cap) ? ((u.bytes/1024).toFixed(1) + '/' + (u.cap/1024).toFixed(0) + ' KB') : '';
        window.__storeUsage = storeUsed;
        // Update Dev Badge if present
        const badge = document.getElementById('devBadge');
        if (badge){
          let tag = badge.querySelector('#storeUsageTag');
          if (!tag){ tag = document.createElement('div'); tag.id='storeUsageTag'; tag.className='small'; tag.style.opacity='0.85'; tag.style.marginTop='2px'; badge.appendChild(tag); }
          tag.textContent = '🗃️ store ' + storeUsed;
        }
      }catch(_){ }
    }
    setInterval(update, 1500); // alle 1.5s wie gefordert
    // erste Initialisierung nach DOM bereit
    if (document.readyState === 'loading'){ document.addEventListener('DOMContentLoaded', update); } else { update(); }
  })();

})();
