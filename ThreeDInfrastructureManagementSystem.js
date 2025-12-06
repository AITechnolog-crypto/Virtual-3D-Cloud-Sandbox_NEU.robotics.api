// ============================================
// 3D Infrastructure Management System
// Mit Live-Daten und VR-Support
// ============================================

const API_BASE = 'http://localhost:8080/api';

let scene, camera, renderer, controls;
let spaceObjects = [];
let selectedObject = null;
let autoRotate = false;
let liveDataActive = false;
let liveDataInterval = null;

// Initialize
init();
animate();
checkBackendStatus();

function init() {
    console.log('🚀 Initializing 3D System...');
    
    // Scene
    scene = new THREE.Scene();
    scene.background = new THREE.Color(0x0a0a14);
    scene.fog = new THREE.Fog(0x0a0a14, 500, 2000);
    
    // Camera
    camera = new THREE.PerspectiveCamera(
        75,
        window.innerWidth / window.innerHeight,
        0.1,
        3000
    );
    camera.position.set(0, 200, 600);
    
    // Renderer
    renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    document.getElementById('canvas-container').appendChild(renderer.domElement);
    
    // Controls
    controls = new THREE.OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;
    controls.dampingFactor = 0.05;
    controls.minDistance = 100;
    controls.maxDistance = 1500;
    
    // Lights
    const ambientLight = new THREE.AmbientLight(0x404040, 0.5);
    scene.add(ambientLight);
    
    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
    directionalLight.position.set(500, 500, 500);
    directionalLight.castShadow = true;
    scene.add(directionalLight);
    
    const pointLight1 = new THREE.PointLight(0x64b5f6, 1, 1000);
    pointLight1.position.set(0, 300, 0);
    scene.add(pointLight1);
    
    // Stars
    createStarField();
    
    // Grid
    const gridHelper = new THREE.GridHelper(2000, 50, 0x444466, 0x222233);
    scene.add(gridHelper);
    
    // Objects
    createSpaceObjects();
    createConnections();
    
    // Events
    setupMouseInteraction();
    window.addEventListener('resize', onWindowResize, false);
    
    // Hide loading
    setTimeout(() => {
        document.getElementById('loading').style.display = 'none';
    }, 1500);
}

function createStarField() {
    const starGeometry = new THREE.BufferGeometry();
    const starCount = 5000;
    const positions = new Float32Array(starCount * 3);
    
    for (let i = 0; i < starCount * 3; i++) {
        positions[i] = (Math.random() - 0.5) * 4000;
    }
    
    starGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    const starMaterial = new THREE.PointsMaterial({
        color: 0xffffff,
        size: 2,
        transparent: true,
        opacity: 0.8
    });
    
    const stars = new THREE.Points(starGeometry, starMaterial);
    scene.add(stars);
}

function createSpaceObjects() {
    const objectsData = [
        {
            id: 'central-hub',
            name: 'Central Hub',
            position: [0, 0, 0],
            color: 0x6495ed,
            size: 50,
            type: 'SERVER',
            endpoint: '/dashboard/overview',
            metadata: {
                status: 'Online',
                cpu: '45%',
                memory: '60%',
                connections: '1,234'
            }
        },
        {
            id: 'robot-fleet',
            name: 'Robot Fleet',
            position: [-180, -100, 80],
            color: 0xff8c00,
            size: 35,
            type: 'ROBOTICS',
            endpoint: '/robotics/overview',
            metadata: {
                status: 'Active',
                robots: '0',
                missions: '0',
                battery: '0%'
            }
        },
        {
            id: 'drone-swarm',
            name: 'Drone Swarm',
            position: [150, -120, -100],
            color: 0x9370db,
            size: 30,
            type: 'DRONES',
            endpoint: '/drones/overview',
            metadata: {
                status: 'Ready',
                drones: '0',
                inFlight: '0',
                coverage: '0 km²'
            }
        },
        {
            id: 'finance',
            name: 'Finance Hub',
            position: [250, -60, 90],
            color: 0x32cd32,
            size: 35,
            type: 'FINANCE',
            endpoint: '/financial/overview',
            metadata: {
                status: 'Active',
                balance: '€0',
                wallets: '0',
                transactions: '0'
            }
        },
        {
            id: 'infrastructure',
            name: 'Infrastructure',
            position: [-200, 130, -50],
            color: 0xff69b4,
            size: 45,
            type: 'INFRASTRUCTURE',
            endpoint: '/infrastructure/overview',
            metadata: {
                status: 'Online',
                instances: '0',
                cost: '€0',
                uptime: '100%'
            }
        },
        {
            id: 'blueprints',
            name: 'Blueprints',
            position: [100, 150, 120],
            color: 0xffd700,
            size: 38,
            type: 'BLUEPRINTS',
            endpoint: '/blueprints/overview',
            metadata: {
                status: 'Ready',
                total: '0',
                production: '0',
                draft: '0'
            }
        }
    ];
    
    objectsData.forEach(data => {
        const obj = createSpaceObject(data);
        spaceObjects.push(obj);
        scene.add(obj.group);
    });
    
    document.getElementById('object-count').textContent = spaceObjects.length;
}

function createSpaceObject(data) {
    const group = new THREE.Group();
    group.position.set(...data.position);
    
    // Sphere
    const geometry = new THREE.SphereGeometry(data.size, 32, 32);
    const material = new THREE.MeshPhongMaterial({
        color: data.color,
        emissive: data.color,
        emissiveIntensity: 0.2,
        shininess: 100
    });
    const sphere = new THREE.Mesh(geometry, material);
    sphere.castShadow = true;
    group.add(sphere);
    
    // Glow
    const glowGeometry = new THREE.SphereGeometry(data.size * 1.2, 32, 32);
    const glowMaterial = new THREE.MeshBasicMaterial({
        color: data.color,
        transparent: true,
        opacity: 0.2
    });
    const glow = new THREE.Mesh(glowGeometry, glowMaterial);
    group.add(glow);
    
    // Ring
    const ringGeometry = new THREE.TorusGeometry(data.size * 1.5, 2, 16, 100);
    const ringMaterial = new THREE.MeshBasicMaterial({
        color: data.color,
        transparent: true,
        opacity: 0.4
    });
    const ring = new THREE.Mesh(ringGeometry, ringMaterial);
    ring.rotation.x = Math.PI / 2;
    group.add(ring);
    
    // Label
    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');
    canvas.width = 256;
    canvas.height = 64;
    context.fillStyle = 'white';
    context.font = 'Bold 24px Arial';
    context.textAlign = 'center';
    context.fillText(data.name, 128, 40);
    
    const texture = new THREE.CanvasTexture(canvas);
    const spriteMaterial = new THREE.SpriteMaterial({ map: texture });
    const sprite = new THREE.Sprite(spriteMaterial);
    sprite.position.y = data.size + 30;
    sprite.scale.set(100, 25, 1);
    group.add(sprite);
    
    return {
        group: group,
        sphere: sphere,
        glow: glow,
        ring: ring,
        data: data,
        rotation: Math.random() * Math.PI * 2
    };
}

function createConnections() {
    const lineMaterial = new THREE.LineBasicMaterial({
        color: 0x4facfe,
        transparent: true,
        opacity: 0.3
    });
    
    for (let i = 0; i < spaceObjects.length; i++) {
        for (let j = i + 1; j < spaceObjects.length; j++) {
            const obj1 = spaceObjects[i];
            const obj2 = spaceObjects[j];
            
            const distance = obj1.group.position.distanceTo(obj2.group.position);
            
            if (distance < 400) {
                const points = [obj1.group.position, obj2.group.position];
                const geometry = new THREE.BufferGeometry().setFromPoints(points);
                const line = new THREE.Line(geometry, lineMaterial);
                scene.add(line);
            }
        }
    }
}

function setupMouseInteraction() {
    const raycaster = new THREE.Raycaster();
    const mouse = new THREE.Vector2();
    
    renderer.domElement.addEventListener('click', (event) => {
        mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
        mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;
        
        raycaster.setFromCamera(mouse, camera);
        
        const spheres = spaceObjects.map(obj => obj.sphere);
        const intersects = raycaster.intersectObjects(spheres);
        
        if (intersects.length > 0) {
            const intersectedObject = spaceObjects.find(
                obj => obj.sphere === intersects[0].object
            );
            selectObject(intersectedObject);
        }
    });
}

function selectObject(obj) {
    if (selectedObject) {
        selectedObject.sphere.material.emissiveIntensity = 0.2;
        selectedObject.glow.material.opacity = 0.2;
    }
    
    selectedObject = obj;
    
    if (obj) {
        obj.sphere.material.emissiveIntensity = 0.6;
        obj.glow.material.opacity = 0.5;
        
        updateInfoPanel(obj);
        animateCameraToObject(obj);
    }
}

function updateInfoPanel(obj) {
    document.getElementById('selected-name').textContent = obj.data.name;
    document.getElementById('selected-type').textContent = obj.data.type;
    document.getElementById('selected-status').textContent = obj.data.metadata.status;
    
    const extraMetadata = document.getElementById('extra-metadata');
    extraMetadata.innerHTML = '';
    
    Object.entries(obj.data.metadata).forEach(([key, value]) => {
        if (key !== 'status') {
            const stat = document.createElement('div');
            stat.className = 'stat';
            stat.innerHTML = `
                <span class="stat-label">${key}:</span>
                <span class="stat-value">${value}</span>
            `;
            extraMetadata.appendChild(stat);
        }
    });
}

function animateCameraToObject(obj) {
    const targetPosition = obj.group.position.clone();
    targetPosition.z += 200;
    targetPosition.y += 100;
    
    const startPosition = camera.position.clone();
    const duration = 1000;
    const startTime = Date.now();
    
    function animateCamera() {
        const elapsed = Date.now() - startTime;
        const progress = Math.min(elapsed / duration, 1);
        
        camera.position.lerpVectors(startPosition, targetPosition, progress);
        controls.target.lerp(obj.group.position, progress);
        
        if (progress < 1) {
            requestAnimationFrame(animateCamera);
        }
    }
    
    animateCamera();
}

function animate() {
    requestAnimationFrame(animate);
    
    spaceObjects.forEach(obj => {
        obj.rotation += 0.005;
        obj.ring.rotation.z = obj.rotation;
        obj.glow.rotation.y += 0.01;
        
        const scale = 1 + Math.sin(Date.now() * 0.002 + obj.rotation) * 0.05;
        obj.glow.scale.set(scale, scale, scale);
    });
    
    if (autoRotate) {
        camera.position.x = Math.cos(Date.now() * 0.0002) * 600;
        camera.position.z = Math.sin(Date.now() * 0.0002) * 600;
        camera.lookAt(0, 0, 0);
    }
    
    controls.update();
    updateFPS();
    renderer.render(scene, camera);
}

let lastTime = Date.now();
let frames = 0;

function updateFPS() {
    frames++;
    const currentTime = Date.now();
    
    if (currentTime > lastTime + 1000) {
        document.getElementById('fps').textContent = frames;
        frames = 0;
        lastTime = currentTime;
    }
}

function onWindowResize() {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
}

// ============================================
// Control Functions
// ============================================

function resetCamera() {
    camera.position.set(0, 200, 600);
    controls.target.set(0, 0, 0);
    autoRotate = false;
}

function toggleAutoRotate() {
    autoRotate = !autoRotate;
}

function toggleLiveData() {
    liveDataActive = !liveDataActive;
    
    if (liveDataActive) {
        document.getElementById('live-status').innerHTML = '🟢 Connected';
        startLiveData();
    } else {
        document.getElementById('live-status').innerHTML = '🔴 Disconnected';
        stopLiveData();
    }
}

function startLiveData() {
    liveDataInterval = setInterval(() => {
        fetchLiveData();
    }, 3000);
    fetchLiveData();
}

function stopLiveData() {
    if (liveDataInterval) {
        clearInterval(liveDataInterval);
        liveDataInterval = null;
    }
}

async function fetchLiveData() {
    for (const obj of spaceObjects) {
        if (obj.data.endpoint) {
            try {
                const response = await fetch(`${API_BASE}${obj.data.endpoint}`);
                if (response.ok) {
                    const data = await response.json();
                    updateObjectData(obj, data);
                }
            } catch (error) {
                console.warn(`⚠️ Could not fetch data for ${obj.data.name}`);
            }
        }
    }
}

function updateObjectData(obj, data) {
    const id = obj.data.id;
    
    if (id === 'robot-fleet' && data.totalRobots !== undefined) {
        obj.data.metadata.robots = data.totalRobots.toString();
        obj.data.metadata.missions = (data.deployedRobots || 0).toString();
        obj.data.metadata.battery = '78%';
    } else if (id === 'drone-swarm' && data.totalDrones !== undefined) {
        obj.data.metadata.drones = data.totalDrones.toString();
        obj.data.metadata.inFlight = (data.availableCount || 0).toString();
    } else if (id === 'finance' && data.totalBalance !== undefined) {
        obj.data.metadata.balance = `€${(data.totalBalance / 1000).toFixed(1)}K`;
        obj.data.metadata.wallets = (data.walletsCount || 0).toString();
    } else if (id === 'infrastructure' && data.totalInstances !== undefined) {
        obj.data.metadata.instances = data.totalInstances.toString();
        obj.data.metadata.cost = `€${(data.totalMonthlyCost || 0).toFixed(0)}`;
    } else if (id === 'blueprints' && data.totalBlueprints !== undefined) {
        obj.data.metadata.total = data.totalBlueprints.toString();
        obj.data.metadata.production = (data.productionCount || 0).toString();
    }
    
    if (selectedObject === obj) {
        updateInfoPanel(obj);
    }
}

async function checkBackendStatus() {
    try {
        const response = await fetch(`${API_BASE}/dashboard/health`);
        if (response.ok) {
            document.getElementById('backend-status').innerHTML = '🟢 Online';
        } else {
            document.getElementById('backend-status').innerHTML = '🟡 Limited';
        }
    } catch (error) {
        document.getElementById('backend-status').innerHTML = '🔴 Offline';
    }
}

function enterVR() {
    alert('🥽 VR Mode\n\nVR wird unterstützt auf:\n• Meta Quest (Browser)\n• HTC Vive\n• Valve Index\n\nBitte mit VR-Gerät verbinden!');
}

console.log('🚀 3D Infrastructure System loaded!');
console.log('💡 Tip: Click on objects to view details');
console.log('📡 Enable "Live Data" to see real-time updates from backend');
