// NurTrio Learning v1 — tabulares Q‑Learning für Differentialantrieb
(function(){
  function clamp(x,a,b){ return Math.max(a, Math.min(b, x)); }
  function bin(value, edges){ for (let i=0;i<edges.length;i++){ if (value < edges[i]) return i; } return edges.length; }

  class QLearner{
    constructor(cfg){ cfg = cfg||{};
      this.gamma  = cfg.gamma  ?? 0.92;
      this.alpha  = cfg.alpha  ?? 0.25;
      this.eps    = cfg.eps    ?? 0.10;
      this.maxMem = cfg.maxMem ?? 5000;
      this.distEdges = [0.5, 1.0, 2.0, 3.0, 5.0, 8.0];
      this.yawEdges  = [-1.2, -0.6, -0.2, 0.2, 0.6, 1.2];
      this.actions = [ [0.7,0.7],[0.9,0.4],[0.4,0.9],[-0.4,0.6],[0.6,-0.4],[0.0,0.0] ];
      this.Q = {}; this.mem = []; this.prev=null; this.stats={steps:0,episodes:0,rewardSum:0,lastReward:0};
      this.load();
    }
    keyForState(s){ const dBin = bin(s?.prox?.front??10, this.distEdges); const y = clamp((s?.imu?.yaw)||0, -Math.PI, Math.PI); const yBin = bin(y, this.yawEdges); return dBin+"|"+yBin; }
    getQ(key){ let row = this.Q[key]; if (!row){ row = new Float32Array(this.actions.length); this.Q[key]=row; } return row; }
    chooseAction(s){ const key=this.keyForState(s); const row=this.getQ(key); if (Math.random()<this.eps){ return Math.floor(Math.random()*this.actions.length); } let best=0, bestV=row[0]; for (let i=1;i<row.length;i++){ if (row[i]>bestV){ best=i; bestV=row[i]; } } return best; }
    reward(prevPose, s){ let r=0; if (prevPose){ const dx=s.pose.x-prevPose.x; const dy=s.pose.y-prevPose.y; const forward=Math.cos(prevPose.theta)*dx + Math.sin(prevPose.theta)*dy; r += forward*2.0; }
      const d=s.prox.front; if (d<1.0) r-=2.0; if (d<0.5) r-=4.0; if (Math.abs(s.imu.yaw)>1.5) r-=0.3; r-=0.02; return r; }
    step(sensors, actuateFn){ const a=this.chooseAction(sensors); const cmd=this.actions[a]; try{ actuateFn(cmd[0],cmd[1]); }catch(_){}
      const nowPose=sensors.pose; if (this.prev){ const r=this.reward(this.prev.pose, sensors); this.stats.rewardSum+=r; this.stats.lastReward=r; const s0k=this.keyForState(this.prev.s); const s1k=this.keyForState(sensors); const Q0=this.getQ(s0k); const Q1=this.getQ(s1k); let maxNext=Q1[0]; for (let i=1;i<Q1.length;i++){ if (Q1[i]>maxNext) maxNext=Q1[i]; } const td = r + this.gamma*maxNext - Q0[this.prev.a]; Q0[this.prev.a] += this.alpha * td; this.mem.push({s0:s0k,a:this.prev.a,r,s1:s1k}); if (this.mem.length>this.maxMem) this.mem.shift(); }
      this.prev = { s:sensors, a, pose: nowPose }; this.stats.steps++; }
      setParams({alpha,gamma,eps}){ if (alpha!=null) this.alpha=alpha; if (gamma!=null) this.gamma=gamma; if (eps!=null) this.eps=eps; }
    clear(){ this.Q={}; this.mem=[]; this.stats={steps:0,episodes:0,rewardSum:0,lastReward:0}; this.prev=null; this.save(); }
    save(){ try{ const blob={ Q:Object.fromEntries(Object.entries(this.Q).map(([k,v])=>[k,Array.from(v)])), cfg:{alpha:this.alpha,gamma:this.gamma,eps:this.eps}, stats:this.stats }; localStorage.setItem('nurtrio.qtable', JSON.stringify(blob)); }catch(_){} }
    load(){ try{ const s=localStorage.getItem('nurtrio.qtable'); if(!s) return; const o=JSON.parse(s); this.Q={}; for (const k in o.Q){ const arr=o.Q[k]; const row=new Float32Array(arr.length); for (let i=0;i<arr.length;i++) row[i]=arr[i]; this.Q[k]=row; } if (o.cfg) this.setParams(o.cfg); if (o.stats) this.stats=o.stats; }catch(_){} }
  }
  window.NurLearn = { QLearner };
})();
