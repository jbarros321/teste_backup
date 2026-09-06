#!/usr/bin/env python3
"""Plano B do servidor local, para quem não tem Node instalado.

Faz o mesmo que o servidor.js: serve os arquivos da pasta e faz a ponte para a
busca de partituras em /api/partituras?q=nome+da+musica (a API do Songsterr não
manda cabeçalho CORS, então quem chama tem de ser o servidor, não o navegador).

Só usa a biblioteca padrão do Python 3 — nada para instalar.
"""

import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer

PORTA = 8765
RAIZ = os.path.dirname(os.path.abspath(__file__))

CABECALHOS = {
    'User-Agent': ('Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 '
                   '(KHTML, like Gecko) Chrome/124.0 Safari/537.36'),
    'Accept': 'application/json,text/plain,*/*',
    'Accept-Language': 'pt-BR,pt;q=0.9,en;q=0.8',
}


def buscar_no_songsterr(termo):
    """Devolve (codigo_http, corpo_em_bytes). Tenta duas vezes."""
    url = ('https://www.songsterr.com/api/songs?pattern='
           + urllib.parse.quote(termo) + '&size=30')
    ultimo_erro = 'desconhecido'

    for _ in range(2):
        try:
            pedido = urllib.request.Request(url, headers=CABECALHOS)
            with urllib.request.urlopen(pedido, timeout=15) as r:
                corpo = r.read()
            dados = json.loads(corpo.decode('utf-8'))
            if not isinstance(dados, list):
                ultimo_erro = 'o Songsterr devolveu algo que não é a lista de músicas'
                continue
            return 200, corpo
        except urllib.error.HTTPError as e:
            ultimo_erro = 'o Songsterr respondeu %s' % e.code
        except Exception as e:                                   # rede, timeout, JSON
            ultimo_erro = str(e) or e.__class__.__name__

    print('  [busca] falhou: ' + ultimo_erro, file=sys.stderr)
    corpo = json.dumps({'erro': 'Não consegui falar com o Songsterr (%s).' % ultimo_erro})
    return 502, corpo.encode('utf-8')


class Manipulador(SimpleHTTPRequestHandler):

    def __init__(self, *a, **kw):
        super().__init__(*a, directory=RAIZ, **kw)

    def responder_json(self, codigo, corpo):
        self.send_response(codigo)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.send_header('Cache-Control', 'no-store')
        self.send_header('Content-Length', str(len(corpo)))
        self.end_headers()
        self.wfile.write(corpo)

    def do_GET(self):
        caminho, _, consulta = self.path.partition('?')

        if caminho == '/api/partituras':
            termo = urllib.parse.parse_qs(consulta).get('q', [''])[0].strip()
            if not termo:
                self.responder_json(400, json.dumps({'erro': 'Diga o nome da música.'}).encode())
                return
            codigo, corpo = buscar_no_songsterr(termo)
            self.responder_json(codigo, corpo)
            return

        super().do_GET()

    def end_headers(self):
        # Sem cache: editou musicas.js, recarregou, já vale.
        if not self.path.startswith('/api/'):
            self.send_header('Cache-Control', 'no-store')
        super().end_headers()

    def log_message(self, *a):
        pass                                       # a janela fica limpa


if __name__ == '__main__':
    print('')
    print('  🎸 Aula de Contrabaixo rodando em:  http://localhost:%d' % PORTA)
    print('  (usando Python — para parar, aperte Ctrl+C nesta janela)')
    print('')
    try:
        ThreadingHTTPServer(('127.0.0.1', PORTA), Manipulador).serve_forever()
    except KeyboardInterrupt:
        print('\n  Até a próxima. 🎸\n')
