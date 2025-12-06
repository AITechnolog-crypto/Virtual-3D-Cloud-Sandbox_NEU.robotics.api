// saas-client.js — minimal client for Azure Functions or similar SaaS endpoints
// Persisted config (localStorage): saas.base, saas.token
(function(){
  const LS_BASE = 'saas.base';
  const LS_TOKEN = 'saas.token';

  function getBase(){ return (localStorage.getItem(LS_BASE)||'').trim(); }
  function getToken(){ return (localStorage.getItem(LS_TOKEN)||'').trim(); }
  function setBase(v){ try{ localStorage.setItem(LS_BASE, String(v||'').trim()); }catch(_){} SaaS.base = getBase(); return SaaS.base; }
  function setToken(v){ try{ localStorage.setItem(LS_TOKEN, String(v||'').trim()); }catch(_){} SaaS.token = getToken(); return SaaS.token; }
  function head(){ const h = { 'Content-Type':'application/json' }; const t = getToken(); if (t) h['Authorization'] = 'Bearer ' + t; return h; }

  async function health(){ const r = await fetch(getBase() + '/health'); return r.json(); }
  async function config(){ const r = await fetch(getBase() + '/config', { headers: head() }); return r.json(); }
  async function skills(){ const r = await fetch(getBase() + '/skillpack', { headers: head() }); return r.json(); }
  async function events(ev){ try{ await fetch(getBase() + '/events', { method:'POST', headers: head(), body: JSON.stringify(ev||{}) }); }catch(_){ /* no-op */ } }
  async function infer(state){ const r = await fetch(getBase() + '/infer', { method:'POST', headers: head(), body: JSON.stringify(state||{}) }); return r.json(); }

  // Public API
  const SaaS = window.SaaS = {
    base: getBase(),
    token: getToken(),
    setBase, setToken,
    head,
    health, config, skills, events, infer,
    // aliases matching the issue description
    async inferAssist(state){ return infer(state); },
    async sendEvent(ev){ return events(ev); }
  };

  // Helper to wire Remote/Skills once base is configured
  SaaS.applyRemote = async function(){
    try{
      if (typeof window.Remote === 'object' && typeof window.Remote.setSource === 'function'){
        window.Remote.setSource(getBase() + '/config');
      }
      if (typeof window.Skills === 'object' && typeof window.Skills.sync === 'function'){
        await window.Skills.sync(getBase() + '/skillpack');
      }
    }catch(_){ }
  };

  // If base already set, try to preload Remote/Skills non-blocking
  if (getBase()) { SaaS.applyRemote().catch(()=>{}); }
})();
