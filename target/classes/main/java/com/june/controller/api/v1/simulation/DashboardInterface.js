<script>
// ========================================
// CONFIG
// ========================================
const API_BASE = 'http://localhost:8080/api';
let statsChart;
let isBackendOnline = false;

// ========================================
// TAB SWITCHING
// ========================================
function switchTab(tabName) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    
    event.target.classList.add('active');
    document.getElementById('tab-' + tabName).classList.add('active');
    
    if (tabName === 'map' && !window.mapInitialized) {
        initMap();
    }
}

// ========================================
// MAP
// ========================================
function initMap() {
    const map = L.map('map').setView([44.97554, 16.08238], 13);
    
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap'
    }).addTo(map);
    
    L.marker([44.97554, 16.08238])
        .addTo(map)
        .bindPopup('🛡️ NurSystem HQ')
        .openPopup();
    
    // Load robot positions from backend
    fetch(`${API_BASE}/robots`)
        .then(res => res.json())
        .then(robots => {
            robots.forEach((robot, i) => {
                const lat = 44.97554 + (Math.random() - 0.5) * 0.05;
                const lng = 16.08238 + (Math.random() - 0.5) * 0.05;
                
                L.circleMarker([lat, lng], {
                    radius: 5,
                    fillColor: '#00ff88',
                    color: '#fff',
                    weight: 1,
                    opacity: 1,
                    fillOpacity: 0.8
                }).addTo(map).bindPopup(`🤖 ${robot.name || 'Robot-' + robot.id}`);
            });
        })
        .catch(() => {
            // Fallback to mock data
            for (let i = 0; i < 20; i++) {
                const lat = 44.97554 + (Math.random() - 0.5) * 0.05;
                const lng = 16.08238 + (Math.random() - 0.5) * 0.05;
                L.circleMarker([lat, lng], {
                    radius: 5,
                    fillColor: '#00ff88',
                    color: '#fff',
                    weight: 1,
                    opacity: 1,
                    fillOpacity: 0.8
                }).addTo(map).bindPopup(`🤖 Tarngrille-${i + 1}`);
            }
        });
    
    window.mapInitialized = true;
}

// ========================================
// CHART
// ========================================
function initChart() {
    statsChart = new Chart(document.getElementById('statsChart'), {
        type: 'line',
        data: {
            labels: Array(15).fill(''),
            datasets: [{
                label: 'Energie',
                data: Array(15).fill(0).map(() => Math.random() * 3000 + 2000),
                borderColor: '#00ff88',
                backgroundColor: 'rgba(0, 255, 136, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { display: false },
                y: {
                    ticks: { color: '#00ff88' },
                    grid: { color: 'rgba(0, 255, 136, 0.1)' }
                }
            }
        }
    });
    
    setInterval(() => {
        statsChart.data.datasets[0].data.shift();
        statsChart.data.datasets[0].data.push(Math.random() * 3000 + 2000);
        statsChart.update('none');
    }, 2000);
}

// ========================================
// BACKEND CONNECTION
// ========================================
async function checkBackend() {
    try {
        const response = await fetch(`${API_BASE}/dashboard/status`);
        if (response.ok) {
            isBackendOnline = true;
            document.getElementById('systemStatus').textContent = 'Online';
            document.getElementById('systemStatus').style.color = '#00ff88';
            return await response.json();
        }
    } catch (e) {
        isBackendOnline = false;
        document.getElementById('systemStatus').textContent = 'Offline (Mock-Daten)';
        document.getElementById('systemStatus').style.color = '#ffaa00';
    }
    return null;
}

// ========================================
// DATA LOADING (mit Backend)
// ========================================
async function loadRoboter() {
    document.getElementById('robotList').innerHTML = '<div class="loading"><div class="spinner"></div>Lade...</div>';
    
    try {
        const response = await fetch(`${API_BASE}/robots`);
        const data = await response.json();
        
        let html = '<table class="data-table"><tr><th>ID</th><th>Name</th><th>Status</th><th>Energie</th></tr>';
        data.forEach(r => {
            html += `<tr>
                <td>${r.id}</td>
                <td>${r.name || 'Robot-' + r.id}</td>
                <td>${r.status || 'ACTIVE'}</td>
                <td>${r.energy || Math.floor(Math.random() * 100)}%</td>
            </tr>`;
        });
        html += '</table>';
        
        document.getElementById('robotList').innerHTML = html;
        document.getElementById('robotCount').textContent = data.length;
    } catch (e) {
        // Fallback zu Mock-Daten
        console.log('Backend offline, verwende Mock-Daten');
        loadRoboterMock();
    }
}

function loadRoboterMock() {
    const data = [
        { id: 1, name: 'Tarngrille-001', status: 'ACTIVE', energy: 87 },
        { id: 2, name: 'Tarngrille-002', status: 'ACTIVE', energy: 92 },
        { id: 3, name: 'Tarngrille-003', status: 'IDLE', energy: 45 }
    ];
    
    let html = '<table class="data-table"><tr><th>ID</th><th>Name</th><th>Status</th><th>Energie</th></tr>';
    data.forEach(r => {
        html += `<tr><td>${r.id}</td><td>${r.name}</td><td>${r.status}</td><td>${r.energy}%</td></tr>`;
    });
    html += '</table>';
    
    document.getElementById('robotList').innerHTML = html;
    document.getElementById('robotCount').textContent = data.length;
}

async function loadDrohnen() {
    document.getElementById('droneList').innerHTML = '<div class="loading"><div class="spinner"></div>Lade...</div>';
    
    try {
        const response = await fetch(`${API_BASE}/drones`);
        const data = await response.json();
        
        let html = '<table class="data-table"><tr><th>Name</th><th>Mission</th><th>Akku</th></tr>';
        data.forEach(d => {
            html += `<tr>
                <td>${d.name || 'Drohne-' + d.id}</td>
                <td>${d.mission || 'PATROL'}</td>
                <td>${d.battery || Math.floor(Math.random() * 100)}%</td>
            </tr>`;
        });
        html += '</table>';
        
        document.getElementById('droneList').innerHTML = html;
        document.getElementById('droneCount').textContent = data.length;
    } catch (e) {
        loadDrohnenMock();
    }
}

function loadDrohnenMock() {
    const data = [
        { id: 1, name: 'Drohne-Alpha', mission: 'PATROL', battery: 78 },
        { id: 2, name: 'Drohne-Beta', mission: 'RECON', battery: 91 }
    ];
    
    let html = '<table class="data-table"><tr><th>Name</th><th>Mission</th><th>Akku</th></tr>';
    data.forEach(d => {
        html += `<tr><td>${d.name}</td><td>${d.mission}</td><td>${d.battery}%</td></tr>`;
    });
    html += '</table>';
    
    document.getElementById('droneList').innerHTML = html;
    document.getElementById('droneCount').textContent = data.length;
}

async function loadWallets() {
    document.getElementById('walletList').innerHTML = '<div class="loading"><div class="spinner"></div>Lade...</div>';
    
    try {
        const response = await fetch(`${API_BASE}/wallets`);
        const data = await response.json();
        
        let html = '<table class="data-table"><tr><th>Name</th><th>Balance</th></tr>';
        data.forEach(w => {
            html += `<tr>
                <td>${w.walletName}</td>
                <td>${parseFloat(w.balance).toLocaleString('de-DE')} ${w.currency}</td>
            </tr>`;
        });
        html += '</table>';
        
        document.getElementById('walletList').innerHTML = html;
        document.getElementById('walletCount').textContent = data.length;
    } catch (e) {
        loadWalletsMock();
    }
}

function loadWalletsMock() {
    const data = [
        { walletName: 'Main Wallet',    balance: 15420.50, currency: 'EUR' },
        { walletName: 'Backup Wallet',  balance: 8750.20,  currency: 'EUR' },
        { walletName: 'Ops Wallet',     balance: 2210.00,  currency: 'EUR' },
        { walletName: 'Reserve A',      balance: 504.75,   currency: 'EUR' },
        { walletName: 'Reserve B',      balance: 11990.00, currency: 'EUR' },
        { walletName: 'Green Fund',     balance: 3420.10,  currency: 'EUR' },
        { walletName: 'Research Lab',   balance: 785.33,   currency: 'EUR' }
    ];
    const total = data.reduce((s,w)=> s + Number(w.balance||0), 0);
    let html = '<table class="data-table"><tr><th>Name</th><th>Balance</th></tr>';
    data.forEach(w => {
        html += `<tr><td>${w.walletName}</td><td>${Number(w.balance).toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ${w.currency}</td></tr>`;
    });
    html += `<tr><td style="font-weight:700">Gesamtsumme</td><td style="font-weight:700">${total.toLocaleString('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} EUR</td></tr>`;
    html += '</table>';
    
    document.getElementById('walletList').innerHTML = html;
    const cnt = document.getElementById('walletCount'); if (cnt) cnt.textContent = data.length;
}

async function loadTransactions() {
    document.getElementById('transactionList').innerHTML = '<div class="loading"><div class="spinner"></div>Lade...</div>';
    
    try {
        const response = await fetch(`${API_BASE}/transactions/recent`);
        const data = await response.json();
        
        let html = '<table class="data-table"><tr><th>Typ</th><th>Betrag</th><th>Kategorie</th></tr>';
        data.forEach(t => {
            const color = t.transactionType === 'INCOME' ? '#00ff88' : '#ff0044';
            html += `<tr>
                <td>${t.transactionType}</td>
                <td style="color:${color}">${parseFloat(t.amount).toFixed(2)} EUR</td>
                <td>${t.category}</td>
            </tr>`;
        });
        html += '</table>';
        
        document.getElementById('transactionList').innerHTML = html;
        document.getElementById('txCount').textContent = data.length;
    } catch (e) {
        loadTransactionsMock();
    }
}

function loadTransactionsMock() {
    const data = [
        { transactionType: 'INCOME', amount: 250.00, category: 'Solar' },
        { transactionType: 'EXPENSE', amount: -120.00, category: 'Maintenance' }
    ];
    
    let html = '<table class="data-table"><tr><th>Typ</th><th>Betrag</th><th>Kategorie</th></tr>';
    data.forEach(t => {
        const color = t.transactionType === 'INCOME' ? '#00ff88' : '#ff0044';
        html += `<tr><td>${t.transactionType}</td><td style="color:${color}">${t.amount.toFixed(2)} EUR</td><td>${t.category}</td></tr>`;
    });
    html += '</table>';
    
    document.getElementById('transactionList').innerHTML = html;
    document.getElementById('txCount').textContent = data.length;
}

function loadBlueprints() {
    document.getElementById('blueprintList').innerHTML = '<p style="color:#888">Mock-Daten (kein Backend-Endpunkt)</p>';
}

function loadTechInstances() {
    document.getElementById('techList').innerHTML = '<p style="color:#888">Mock-Daten (kein Backend-Endpunkt)</p>';
}

// ========================================
// ACTIONS
// ========================================
function deploySwarm() {
    alert('🚀 Schwarm deployed!\n\n✅ +100 Roboter hinzugefügt');
    document.getElementById('swarmSize').textContent = '10.1B';
}

function collectEnergy() {
    alert('⚡ Energie gesammelt!\n\n✅ +150 kWh');
    const current = parseInt(document.getElementById('energy').textContent.replace(/[^0-9]/g, ''));
    document.getElementById('energy').textContent = (current + 150).toLocaleString('de-DE') + ' kWh';
}

function simulateThreat() {
    alert('⚠️ Bedrohung simuliert!\n\n🛡️ Blockiert');
    const current = parseInt(document.getElementById('threats').textContent);
    document.getElementById('threats').textContent = current + 1;
}

// ========================================
// INIT
// ========================================
document.addEventListener('DOMContentLoaded', async () => {
    console.log('🛡️ NurSystem wird geladen...');
    
    // Check backend
    await checkBackend();
    
    // Init
    initChart();
    loadRoboter();
    loadDrohnen();
    loadWallets();
    loadTransactions();
    loadBlueprints();
    loadTechInstances();
    
    // Auto-refresh every 30 seconds
    setInterval(async () => {
        await checkBackend();
        loadRoboter();
        loadDrohnen();
        loadWallets();
        loadTransactions();
    }, 30000);
});
</script>
