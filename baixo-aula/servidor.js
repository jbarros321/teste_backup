/* Servidor local mínimo (sem dependências).
   O navegador só libera o microfone em http://localhost — por isso não abra
   o index.html direto com dois cliques. Rode:  node servidor.js

   Ele também faz uma ponte para a busca de partituras (modo 7): o navegador
   não pode chamar o Songsterr direto (política de CORS), então quem chama é
   este servidor, em /api/partituras?q=nome+da+musica                        */

const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');

const PORTA = 8765;
const RAIZ = __dirname;
const TIPOS = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.md': 'text/plain; charset=utf-8',
};

/** Busca no catálogo público do Songsterr e devolve o JSON cru. */
function buscarPartituras(termo, res) {
  const url = 'https://www.songsterr.com/api/songs?pattern=' +
              encodeURIComponent(termo) + '&size=20';

  const req = https.get(url, { headers: { 'User-Agent': 'baixo-aula/1.0', 'Accept': 'application/json' } }, r => {
    let corpo = '';
    r.setEncoding('utf8');
    r.on('data', p => { corpo += p; });
    r.on('end', () => {
      res.writeHead(r.statusCode === 200 ? 200 : 502, {
        'Content-Type': 'application/json; charset=utf-8',
        'Cache-Control': 'no-store',
      });
      res.end(r.statusCode === 200 ? corpo : JSON.stringify({ erro: 'O Songsterr respondeu ' + r.statusCode }));
    });
  });

  req.setTimeout(12000, () => {
    req.destroy();
  });
  req.on('error', e => {
    res.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify({ erro: 'Não consegui falar com a internet (' + e.message + ')' }));
  });
}

http.createServer((req, res) => {
  const [caminho, consulta] = req.url.split('?');
  let rel = decodeURIComponent(caminho);

  if (rel === '/api/partituras') {
    const params = new URLSearchParams(consulta || '');
    const termo = (params.get('q') || '').trim();
    if (!termo) {
      res.writeHead(400, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify({ erro: 'Diga o nome da música.' }));
      return;
    }
    buscarPartituras(termo, res);
    return;
  }

  if (rel === '/') rel = '/index.html';
  const arquivo = path.join(RAIZ, path.normalize(rel));

  if (!arquivo.startsWith(RAIZ)) { res.writeHead(403).end('Proibido'); return; }

  fs.readFile(arquivo, (erro, dados) => {
    if (erro) { res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' }).end('Não encontrado'); return; }
    res.writeHead(200, {
      'Content-Type': TIPOS[path.extname(arquivo).toLowerCase()] || 'application/octet-stream',
      'Cache-Control': 'no-store',
    });
    res.end(dados);
  });
}).listen(PORTA, () => {
  console.log('');
  console.log('  🎸 Aula de Contrabaixo rodando em:  http://localhost:' + PORTA);
  console.log('  (para parar, aperte Ctrl+C nesta janela)');
  console.log('');
});
