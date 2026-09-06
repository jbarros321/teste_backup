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

/** Uma requisição HTTPS que segue redirect e devolve o corpo como texto. */
function pegar(url, tentativasDeRedirect = 3) {
  return new Promise((ok, falha) => {
    const req = https.get(url, {
      headers: {
        // A API recusa alguns User-Agents "de robô"; este passa.
        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 ' +
                      '(KHTML, like Gecko) Chrome/124.0 Safari/537.36',
        'Accept': 'application/json,text/plain,*/*',
        'Accept-Language': 'pt-BR,pt;q=0.9,en;q=0.8',
      },
    }, r => {
      const status = r.statusCode;

      if (status >= 300 && status < 400 && r.headers.location && tentativasDeRedirect > 0) {
        r.resume();
        const destino = new URL(r.headers.location, url).toString();
        pegar(destino, tentativasDeRedirect - 1).then(ok, falha);
        return;
      }

      let corpo = '';
      r.setEncoding('utf8');
      r.on('data', p => { corpo += p; });
      r.on('end', () => ok({ status, corpo }));
    });

    req.setTimeout(15000, () => {
      req.destroy(new Error('o Songsterr demorou demais para responder'));
    });
    req.on('error', falha);
  });
}

/** Busca no catálogo público do Songsterr e devolve o JSON cru.
    Tenta duas vezes: falha de rede momentânea é comum. */
async function buscarPartituras(termo, res) {
  const url = 'https://www.songsterr.com/api/songs?pattern=' +
              encodeURIComponent(termo) + '&size=30';

  const responder = (codigo, objeto) => {
    res.writeHead(codigo, {
      'Content-Type': 'application/json; charset=utf-8',
      'Cache-Control': 'no-store',
    });
    res.end(typeof objeto === 'string' ? objeto : JSON.stringify(objeto));
  };

  let ultimoErro = null;

  for (let tentativa = 1; tentativa <= 2; tentativa++) {
    try {
      const { status, corpo } = await pegar(url);

      if (status !== 200) {
        ultimoErro = 'o Songsterr respondeu ' + status;
        continue;
      }

      // Só repassa se realmente for a lista JSON esperada — assim o navegador
      // nunca recebe uma página de erro em HTML fingindo ser resultado.
      try {
        const dados = JSON.parse(corpo);
        if (!Array.isArray(dados)) throw new Error('formato inesperado');
        responder(200, corpo);
        return;
      } catch (e) {
        ultimoErro = 'o Songsterr devolveu algo que não é a lista de músicas';
        continue;
      }
    } catch (e) {
      ultimoErro = e.message;
    }
  }

  console.error('  [busca] falhou: ' + ultimoErro);
  responder(502, { erro: 'Não consegui falar com o Songsterr (' + ultimoErro + ').' });
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
