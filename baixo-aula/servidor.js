/* Servidor local mínimo (sem dependências).
   O navegador só libera o microfone em http://localhost — por isso não abra
   o index.html direto com dois cliques. Rode:  node servidor.js            */

const http = require('http');
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

http.createServer((req, res) => {
  let rel = decodeURIComponent(req.url.split('?')[0]);
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
