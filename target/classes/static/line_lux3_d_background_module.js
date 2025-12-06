
// LineLux 3D Background Module
// Usage: import './linelux-3d.js' or <script src="linelux-3d.js"></script>

(function() {
    'use strict';

    function init3DBackground() {
        const canvas = document.getElementById('bg-canvas');
        if (!canvas || !window.THREE) {
            console.warn('⚠️ Canvas or THREE.js not found');
            return;
        }

        const scene = new THREE.Scene();
        scene.fog = new THREE.Fog(0x000000, 1, 1000);

        const camera = new THREE.PerspectiveCamera(
            75,
            window.innerWidth / window.innerHeight,
            0.1,
            1000
        );
        camera.position.z = 400;

        const renderer = new THREE.WebGLRenderer({
            canvas: canvas,
            antialias: true,
            alpha: true
        });
        renderer.setSize(window.innerWidth, window.innerHeight);
        renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

        // Particles
        const particleCount = 1000;
        const geometry = new THREE.BufferGeometry();
        const vertices = [];
        const particles = [];

        for (let i = 0; i < particleCount; i++) {
            const x = (Math.random() - 0.5) * 1000;
            const y = (Math.random() - 0.5) * 1000;
            const z = (Math.random() - 0.5) * 1000;

            vertices.push(x, y, z);
            particles.push({
                x, y, z,
                vx: (Math.random() - 0.5) * 0.5,
                vy: (Math.random() - 0.5) * 0.5,
                vz: (Math.random() - 0.5) * 0.5
            });
        }

        geometry.setAttribute('position', new THREE.Float32BufferAttribute(vertices, 3));

        const material = new THREE.PointsMaterial({
            color: 0x00ff88,
            size: 2,
            transparent: true,
            opacity: 0.8
        });

        const points = new THREE.Points(geometry, material);
        scene.add(points);

        // Animation
        function animate() {
            requestAnimationFrame(animate);
            scene.rotation.y += 0.001;

            const positions = points.geometry.attributes.position.array;
            for (let i = 0; i < particles.length; i++) {
                particles[i].x += particles[i].vx;
                particles[i].y += particles[i].vy;
                particles[i].z += particles[i].vz;

                if (Math.abs(particles[i].x) > 500) particles[i].vx *= -1;
                if (Math.abs(particles[i].y) > 500) particles[i].vy *= -1;
                if (Math.abs(particles[i].z) > 500) particles[i].vz *= -1;

                positions[i * 3] = particles[i].x;
                positions[i * 3 + 1] = particles[i].y;
                positions[i * 3 + 2] = particles[i].z;
            }

            points.geometry.attributes.position.needsUpdate = true;
            renderer.render(scene, camera);
        }

        // Resize handler
        window.addEventListener('resize', () => {
            camera.aspect = window.innerWidth / window.innerHeight;
            camera.updateProjectionMatrix();
            renderer.setSize(window.innerWidth, window.innerHeight);
        });

        animate();
    }

    // Auto-initialize when DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init3DBackground);
    } else {
        init3DBackground();
    }

    // Export for manual initialization
    window.LineLux = { init3D: init3DBackground };
})();
