// Servidor local de desenvolvimento. Não vai para o deploy (ver .vercelignore).
// Uso: node dev-server.mjs   ->   http://localhost:3000
import { createServer } from 'node:http';
import { readFile } from 'node:fs/promises';
import { extname, join, normalize } from 'node:path';
import handler from './api/contact.js';

// Carrega .env.local sem dependências.
try {
  const env = await readFile('.env.local', 'utf8');
  for (const line of env.split('\n')) {
    const m = line.match(/^\s*([A-Z0-9_]+)\s*=\s*(.*)\s*$/);
    if (m && !process.env[m[1]]) process.env[m[1]] = m[2].replace(/^["']|["']$/g, '');
  }
  console.log('.env.local carregado');
} catch {
  console.warn('AVISO: .env.local não encontrado');
}

const TYPES = {
  '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8', '.svg': 'image/svg+xml', '.png': 'image/png',
  '.jpg': 'image/jpeg', '.mp4': 'video/mp4', '.txt': 'text/plain; charset=utf-8',
};

createServer(async (req, res) => {
  const url = new URL(req.url, 'http://localhost');

  if (url.pathname === '/api/contact') {
    const chunks = [];
    for await (const c of req) chunks.push(c);
    const raw = Buffer.concat(chunks).toString('utf8');
    try { req.body = raw ? JSON.parse(raw) : {}; } catch { req.body = null; }
    res.status = (c) => { res.statusCode = c; return res; };
    res.json = (o) => { res.setHeader('Content-Type', 'application/json'); res.end(JSON.stringify(o)); return res; };
    try { await handler(req, res); } catch (e) { console.error(e); res.statusCode = 500; res.end('{}'); }
    return;
  }

  let p = decodeURIComponent(url.pathname);
  if (p === '/') p = '/index.html';
  const safe = normalize(join(process.cwd(), p));
  if (!safe.startsWith(process.cwd())) { res.statusCode = 403; return res.end('forbidden'); }
  try {
    const buf = await readFile(safe);
    res.setHeader('Content-Type', TYPES[extname(safe)] || 'application/octet-stream');
    res.end(buf);
  } catch { res.statusCode = 404; res.end('404'); }
}).listen(3000, () => console.log('http://localhost:3000'));
