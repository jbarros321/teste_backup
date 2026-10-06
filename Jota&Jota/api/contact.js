// Serverless function (Vercel). Roda no servidor: nada aqui chega ao browser.
// Variáveis de ambiente necessárias (Vercel > Settings > Environment Variables):
//   CONTACT_TO     destinatário final  (ex.: jbarros213@icloud.com)
//   CONTACT_FROM   remetente verificado no provedor (ex.: site@seudominio.com.br)
//   RESEND_API_KEY chave da API do Resend (https://resend.com)

const MAX = { nome: 120, email: 180, mensagem: 4000 };
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

// Rate limit best-effort, por instância quente da lambda.
const hits = new Map();
const WINDOW_MS = 10 * 60 * 1000;
const LIMIT = 5;

function rateLimited(ip) {
  const now = Date.now();
  const list = (hits.get(ip) || []).filter((t) => now - t < WINDOW_MS);
  list.push(now);
  hits.set(ip, list);
  if (hits.size > 5000) hits.clear(); // teto de memória
  return list.length > LIMIT;
}

const clean = (v, max) =>
  String(v == null ? '' : v)
    .replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F]/g, '')
    .trim()
    .slice(0, max);

// Evita header injection no Reply-To.
const headerSafe = (v) => v.replace(/[\r\n]/g, ' ');

const esc = (s) =>
  s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    res.setHeader('Allow', 'POST');
    return res.status(405).json({ ok: false, error: 'Método não permitido.' });
  }

  const to = process.env.CONTACT_TO;
  const from = process.env.CONTACT_FROM;
  const key = process.env.RESEND_API_KEY;
  if (!to || !from || !key) {
    console.error('contact: variáveis de ambiente ausentes');
    return res.status(500).json({ ok: false, error: 'Serviço de contato indisponível.' });
  }

  const ip =
    (req.headers['x-forwarded-for'] || '').split(',')[0].trim() ||
    req.socket?.remoteAddress ||
    'unknown';
  if (rateLimited(ip)) {
    return res.status(429).json({ ok: false, error: 'Muitas tentativas. Tente de novo em alguns minutos.' });
  }

  let body = req.body;
  if (typeof body === 'string') {
    try { body = JSON.parse(body); } catch { body = null; }
  }
  if (!body || typeof body !== 'object') {
    return res.status(400).json({ ok: false, error: 'Requisição inválida.' });
  }

  // Honeypot + tempo mínimo de preenchimento: descarta bots sem dar pista.
  if (clean(body.website, 50) !== '') return res.status(200).json({ ok: true });
  const elapsed = Number(body.t);
  if (Number.isFinite(elapsed) && elapsed < 2000) return res.status(200).json({ ok: true });

  const nome = clean(body.nome, MAX.nome);
  const email = clean(body.email, MAX.email).toLowerCase();
  const mensagem = clean(body.mensagem, MAX.mensagem);

  if (nome.length < 2 || !EMAIL_RE.test(email) || mensagem.length < 10) {
    return res.status(400).json({ ok: false, error: 'Preencha nome, e-mail válido e uma mensagem.' });
  }

  const texto = `Nome: ${nome}\nE-mail: ${email}\nIP: ${ip}\n\n${mensagem}`;

  try {
    const r = await fetch('https://api.resend.com/emails', {
      method: 'POST',
      headers: { Authorization: `Bearer ${key}`, 'Content-Type': 'application/json' },
      body: JSON.stringify({
        from,
        to: [to],
        reply_to: headerSafe(email),
        subject: `Contato pelo site — ${headerSafe(nome)}`,
        text: texto,
        html: `<p><b>Nome:</b> ${esc(nome)}<br><b>E-mail:</b> ${esc(email)}<br><b>IP:</b> ${esc(ip)}</p>
<pre style="font:14px/1.6 ui-monospace,monospace;white-space:pre-wrap">${esc(mensagem)}</pre>`,
      }),
    });

    if (!r.ok) {
      console.error('contact: provedor respondeu', r.status, await r.text().catch(() => ''));
      return res.status(502).json({ ok: false, error: 'Não foi possível enviar agora. Tente mais tarde.' });
    }
    return res.status(200).json({ ok: true });
  } catch (err) {
    console.error('contact: falha no envio', err?.message);
    return res.status(502).json({ ok: false, error: 'Não foi possível enviar agora. Tente mais tarde.' });
  }
}
