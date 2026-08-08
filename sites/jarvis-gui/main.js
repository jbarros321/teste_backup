import * as THREE from 'three';

// ── Scene setup ──────────────────────────────────────────────────────────────
let scene, camera, renderer;
let particleSystem;
const particleCount = 4000;
let currentState = 'idle';

const colors = {
    idle:      new THREE.Color(0x00d4ff),
    listening: new THREE.Color(0xffd700),
    thinking:  new THREE.Color(0xffffff),
    speaking:  new THREE.Color(0x00d4ff)
};

let basePositions = new Float32Array(particleCount * 3);

// ── Soft circle texture ───────────────────────────────────────────────────────
function createCircleTexture() {
    const canvas = document.createElement('canvas');
    canvas.width = 32; canvas.height = 32;
    const ctx = canvas.getContext('2d');
    const g = ctx.createRadialGradient(16, 16, 0, 16, 16, 16);
    g.addColorStop(0,   'rgba(255,255,255,1)');
    g.addColorStop(0.2, 'rgba(255,255,255,0.8)');
    g.addColorStop(0.5, 'rgba(255,255,255,0.2)');
    g.addColorStop(1,   'rgba(0,0,0,0)');
    ctx.fillStyle = g;
    ctx.fillRect(0, 0, 32, 32);
    return new THREE.CanvasTexture(canvas);
}

// ── Init Three.js ─────────────────────────────────────────────────────────────
function init() {
    scene  = new THREE.Scene();
    camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
    camera.position.z = 5;

    renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setPixelRatio(window.devicePixelRatio);
    document.getElementById('container').appendChild(renderer.domElement);

    createParticles();
    animate();

    window.addEventListener('resize', onWindowResize, false);
}

// ── Particle sphere ───────────────────────────────────────────────────────────
function createParticles() {
    const geometry  = new THREE.BufferGeometry();
    const positions = new Float32Array(particleCount * 3);
    const colorsArr = new Float32Array(particleCount * 3);

    for (let i = 0; i < particleCount; i++) {
        const theta = Math.random() * Math.PI * 2;
        const phi   = Math.acos((Math.random() * 2) - 1);
        const r     = 2;

        const x = r * Math.sin(phi) * Math.cos(theta);
        const y = r * Math.sin(phi) * Math.sin(theta);
        const z = r * Math.cos(phi);

        positions[i*3]   = basePositions[i*3]   = x;
        positions[i*3+1] = basePositions[i*3+1] = y;
        positions[i*3+2] = basePositions[i*3+2] = z;

        colorsArr[i*3]   = colors.idle.r;
        colorsArr[i*3+1] = colors.idle.g;
        colorsArr[i*3+2] = colors.idle.b;
    }

    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    geometry.setAttribute('color',    new THREE.BufferAttribute(colorsArr, 3));

    const material = new THREE.PointsMaterial({
        size:         0.07,
        vertexColors: true,
        transparent:  true,
        opacity:      0.85,
        map:          createCircleTexture(),
        blending:     THREE.AdditiveBlending,
        depthWrite:   false
    });

    particleSystem = new THREE.Points(geometry, material);
    scene.add(particleSystem);
}

// ── Animation loop ────────────────────────────────────────────────────────────
function animate() {
    requestAnimationFrame(animate);

    const time = Date.now() * 0.001;
    const pos  = particleSystem.geometry.attributes.position.array;
    const col  = particleSystem.geometry.attributes.color.array;

    for (let i = 0; i < particleCount; i++) {
        const ix = i*3, iy = i*3+1, iz = i*3+2;
        const bx = basePositions[ix], by = basePositions[iy], bz = basePositions[iz];

        let noise = 0, scale = 1;

        if (currentState === 'idle') {
            noise = Math.sin(time + (bx * by * bz) * 0.1) * 0.02;
        } else if (currentState === 'listening') {
            noise = Math.sin(time * 5 + i) * 0.06;
        } else if (currentState === 'thinking') {
            noise = (Math.random() - 0.5) * 0.12;
        } else if (currentState === 'speaking') {
            scale = 1 + Math.sin(time * 15 + (bx * by)) * 0.18;
            noise = Math.sin(time * 8 + i) * 0.06;
        }

        pos[ix] = (bx * scale) + noise;
        pos[iy] = (by * scale) + noise;
        pos[iz] = (bz * scale) + noise;

        const tc = colors[currentState];
        col[ix] += (tc.r - col[ix]) * 0.05;
        col[iy] += (tc.g - col[iy]) * 0.05;
        col[iz] += (tc.b - col[iz]) * 0.05;
    }

    const rotSpeeds = { idle: 0.001, listening: 0.006, thinking: 0.022, speaking: 0.006 };
    const tiltSpeeds = { idle: 0.0005, listening: 0, thinking: 0, speaking: 0 };

    particleSystem.rotation.y += rotSpeeds[currentState]  || 0.001;
    particleSystem.rotation.x += tiltSpeeds[currentState] || 0;

    particleSystem.geometry.attributes.position.needsUpdate = true;
    particleSystem.geometry.attributes.color.needsUpdate    = true;

    renderer.render(scene, camera);
}

function onWindowResize() {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
}

// ── HUD: Uptime counter ───────────────────────────────────────────────────────
const startTime = Date.now();
setInterval(() => {
    const elapsed = Math.floor((Date.now() - startTime) / 1000);
    const h = String(Math.floor(elapsed / 3600)).padStart(2, '0');
    const m = String(Math.floor((elapsed % 3600) / 60)).padStart(2, '0');
    const s = String(elapsed % 60).padStart(2, '0');
    const el = document.getElementById('uptime-display');
    if (el) el.textContent = `${h}:${m}:${s}`;
}, 1000);

// ── HUD: Fake telemetry ───────────────────────────────────────────────────────
function randomRange(min, max) { return Math.random() * (max - min) + min; }

function updateTelemetry() {
    const latency = randomRange(80, 320);
    const conf    = randomRange(75, 98);
    const vocab   = randomRange(40, 90);
    const ctx     = randomRange(10, 60);
    const load    = randomRange(15, 85);

    const set = (id, val, unit, barId, barW, barClass) => {
        const v = document.getElementById(id);
        const b = document.getElementById(barId);
        if (v) v.textContent = val.toFixed(1) + ' ' + unit;
        if (b) { b.style.width = barW + '%'; if (barClass) b.className = `metric-bar ${barClass}`; }
    };

    const loadEl = document.getElementById('neural-load');
    if (loadEl) loadEl.textContent = load.toFixed(1) + '%';

    set('val-latency', latency, 'ms', 'bar-latency', Math.min((latency / 400) * 100, 100),
        latency > 250 ? 'bar-yellow' : '');
    set('val-conf',    conf,    '%',  'bar-conf',    conf, 'bar-green');
    set('val-vocab',   vocab,   '%',  'bar-vocab',   vocab, 'bar-yellow');
    set('val-ctx',     ctx,     '%',  'bar-ctx',     ctx, '');
}

setInterval(updateTelemetry, 1400);
updateTelemetry();

// ── HUD: Waveform ─────────────────────────────────────────────────────────────
const waveSpans = document.querySelectorAll('.waveform span');

function updateWaveform() {
    const isActive = currentState === 'listening' || currentState === 'speaking';
    waveSpans.forEach(span => {
        const h = isActive ? randomRange(4, 34) : randomRange(2, 6);
        span.style.height = h + 'px';
        span.style.opacity = isActive ? '0.9' : '0.2';
    });
}

setInterval(updateWaveform, 80);

// ── setState ──────────────────────────────────────────────────────────────────
const stateLabels = {
    idle:      { label: 'IDLE',      desc: 'Sistema em repouso. Aguardando ativação.' },
    listening: { label: 'LISTENING', desc: 'Capturando entrada de áudio em tempo real.' },
    thinking:  { label: 'THINKING',  desc: 'Processando e inferindo resposta neural.' },
    speaking:  { label: 'SPEAKING',  desc: 'Sintetizando e emitindo saída de voz.' }
};

const modeTags = {
    idle:      'STANDBY',
    listening: 'INPUT',
    thinking:  'PROCESSING',
    speaking:  'OUTPUT'
};

window.setState = function(state) {
    if (!colors[state]) return;

    currentState = state;

    // Body class for CSS state theming
    document.body.className = `state-${state}`;

    const statusEl   = document.getElementById('status-text');
    const feedbackEl = document.getElementById('feedback-text');
    const micEl      = document.getElementById('mic-indicator');
    const micLbl     = document.getElementById('mic-label');
    const stateDisp  = document.getElementById('state-display');
    const stateDesc  = document.getElementById('state-desc');
    const modeDisp   = document.getElementById('mode-display');
    const pillEl     = document.getElementById('status-pill');

    if (statusEl) {
        statusEl.textContent = state.toUpperCase();
        statusEl.className = state === 'listening' ? 'text-yellow' : '';
    }

    if (stateDisp) {
        stateDisp.classList.add('glitch');
        setTimeout(() => stateDisp.classList.remove('glitch'), 250);
        stateDisp.textContent = stateLabels[state]?.label || state.toUpperCase();
    }
    if (stateDesc) stateDesc.textContent = stateLabels[state]?.desc || '';
    if (modeDisp)  modeDisp.textContent  = modeTags[state] || 'STANDBY';

    // Mic
    if (state === 'listening') {
        if (feedbackEl)  feedbackEl.textContent = 'Ouvindo agora...';
        if (micEl)       micEl.className = 'mic-on';
        if (micLbl)      micLbl.textContent = 'MIC ON';
    } else if (state === 'thinking') {
        if (feedbackEl)  feedbackEl.textContent = 'Processando resposta...';
        if (micEl)       micEl.className = 'mic-off';
        if (micLbl)      micLbl.textContent = 'MIC OFF';
    } else if (state === 'speaking') {
        if (feedbackEl)  feedbackEl.textContent = 'B.I falando...';
        if (micEl)       micEl.className = 'mic-off';
        if (micLbl)      micLbl.textContent = 'MIC OFF';
    } else {
        if (feedbackEl)  feedbackEl.textContent = 'Aguardando comando...';
        if (micEl)       micEl.className = 'mic-off';
        if (micLbl)      micLbl.textContent = 'MIC OFF';
    }
};

// ── addLog ────────────────────────────────────────────────────────────────────
window.addLog = function(message, type = 'info') {
    const logContainer = document.getElementById('activity-log');
    if (!logContainer) return;

    const entry = document.createElement('div');
    entry.className = `log-entry type-${type}`;

    const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });

    entry.innerHTML = `
        <div class="log-time">${time}</div>
        <div class="log-msg">${message}</div>
    `;

    logContainer.appendChild(entry);
    logContainer.scrollTop = logContainer.scrollHeight;

    if (logContainer.children.length > 50) {
        logContainer.removeChild(logContainer.firstChild);
    }
};

// ── Eel expose ────────────────────────────────────────────────────────────────
if (window.eel) {
    window.eel.expose(window.setState, 'setState');
    window.eel.expose(window.addLog,   'addLog');
}

// ── Boot ──────────────────────────────────────────────────────────────────────
init();

// Boot log entries
setTimeout(() => window.addLog('Sistema O.L.A B.I inicializado.', 'system'), 400);
setTimeout(() => window.addLog('Motor neural ativo. 4.000 partículas carregadas.', 'result'), 900);
setTimeout(() => window.addLog('Aguardando primeiro comando de voz.', 'info'), 1400);
