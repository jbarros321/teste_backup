import { profile, content, LANGS, DEFAULT_LANG } from "./data.js";

/* ══════════════ utilidades ══════════════ */

const $ = (s) => document.querySelector(s);
const el = (tag, cls, html) => {
  const n = document.createElement(tag);
  if (cls) n.className = cls;
  if (html != null) n.innerHTML = html;
  return n;
};
const esc = (s) => String(s).replace(/[&<>"]/g, (c) =>
  ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));

const calm = matchMedia("(prefers-reduced-motion: reduce)").matches;

/* IntersectionObserver recriado a cada render — o anterior é descartado
   junto com os nós que ele observava. */
let io = null;
const wireReveals = () => {
  io?.disconnect();
  io = new IntersectionObserver((entries) => {
    entries.forEach((e) => {
      if (!e.isIntersecting) return;
      e.target.classList.add("in");
      io.unobserve(e.target);
    });
  }, { rootMargin: "0px 0px -12% 0px", threshold: 0.08 });
  document.querySelectorAll("[data-reveal]").forEach((n) => io.observe(n));
};

let counterIO = null;
const wireCounters = () => {
  counterIO?.disconnect();
  counterIO = new IntersectionObserver((entries) => {
    entries.forEach((e) => {
      if (!e.isIntersecting) return;
      counterIO.unobserve(e.target);
      const target = +e.target.dataset.count;
      if (calm) { e.target.textContent = target; return; }
      const t0 = performance.now(), dur = 1400;
      const step = (t) => {
        const p = Math.min(1, (t - t0) / dur);
        e.target.textContent = Math.round(target * (1 - Math.pow(1 - p, 3)));
        if (p < 1) requestAnimationFrame(step);
      };
      requestAnimationFrame(step);
    });
  }, { threshold: 1 });
  document.querySelectorAll("[data-count]").forEach((n) => counterIO.observe(n));
};

/* Spotlight que segue o ponteiro dentro de cada card. */
const finePointer = matchMedia("(hover: hover) and (pointer: fine)").matches && !calm;
const wireSpotlights = () => {
  if (!finePointer) return;
  document.querySelectorAll(".role, .card").forEach((card) => {
    card.addEventListener("pointermove", (e) => {
      const r = card.getBoundingClientRect();
      card.style.setProperty("--mx", `${((e.clientX - r.left) / r.width) * 100}%`);
      card.style.setProperty("--my", `${((e.clientY - r.top) / r.height) * 100}%`);
    }, { passive: true });
  });
};

/* Divide um texto em caracteres animados. */
const splitInto = (node, text, lineIndex) => {
  node.textContent = "";
  [...text].forEach((c, i) => {
    const span = el("span", "ch", c === " " ? "&nbsp;" : esc(c));
    span.style.animationDelay = calm ? "0s" : `${0.2 + lineIndex * 0.16 + i * 0.03}s`;
    node.append(span);
  });
};

/* ══════════════ render ══════════════ */

function render(lang) {
  const t = content[lang];

  /* --- documento --- */
  document.documentElement.lang = t.htmlLang;
  document.title = t.docTitle;
  $("#metaDesc").setAttribute("content", t.docDesc);
  $("#skipLink").textContent = lang === "pt" ? "Ir para o conteúdo" : "Skip to content";

  /* --- navegação --- */
  const navLinks = $("#navLinks");
  navLinks.textContent = "";
  navLinks.append(...t.nav.map((n) => {
    const li = el("li");
    const a = el("a", null, esc(n.label));
    a.href = n.href;
    li.append(a);
    return li;
  }));

  /* --- hero --- */
  $("#availability").textContent = t.availability;
  splitInto($("#nameFirst"), t.firstName, 0);
  splitInto($("#nameLast"), t.lastName, 1);
  $("#scrollCue").textContent = t.scrollCue;

  const roles = $("#roles");
  roles.setAttribute("aria-label", lang === "pt" ? "Atuações" : "Roles");
  roles.textContent = "";
  roles.append(...t.roles.map((r) => el("li", null, esc(r))));

  const metrics = $("#metrics");
  metrics.textContent = "";
  metrics.append(...t.metrics.map((m) => {
    const d = el("div", "metric");
    d.append(
      el("b", null, `<span data-count="${m.value}">0</span>${esc(m.suffix)}`),
      el("span", null, esc(m.label)),
    );
    return d;
  }));

  /* --- títulos de seção --- */
  Object.entries(t.sections).forEach(([k, v]) => { $(`#h-${k}`).textContent = v; });

  /* --- resumo --- */
  const summary = $("#summary");
  summary.textContent = "";
  summary.append(...t.summary.map((p, i) => {
    const n = el("p", null, esc(p));
    n.setAttribute("data-reveal", "");
    n.style.transitionDelay = `${i * 0.1}s`;
    return n;
  }));

  /* --- marquee (duplicado para o loop de -50% fechar) --- */
  const chips = (hot) => [...t.stack, ...t.stack].map((s, i) =>
    el("span", `chip${hot && i % 3 === 0 ? " chip--hot" : ""}`, esc(s)));
  $("#marqueeA").textContent = "";
  $("#marqueeB").textContent = "";
  $("#marqueeA").append(...chips(true));
  $("#marqueeB").append(...chips(false));

  /* --- experiência --- */
  const timeline = $("#timeline");
  timeline.textContent = "";
  timeline.append(...t.companies.map((c) => {
    const org = el("div", "org");
    org.setAttribute("data-reveal", "");

    const id = el("div", "org__id");
    const name = el("h3", "org__name", esc(c.company));
    if (c.current) name.append(el("span", "badge", esc(t.nowBadge)));
    id.append(name, el("span", "org__span", esc(c.span)));

    const list = el("div", "org__roles");
    c.roles.forEach((r) => {
      const card = el("article", "role");
      const top = el("div", "role__top");
      top.append(
        el("h4", "role__title", esc(r.title)),
        el("span", "role__period", esc(r.period)),
      );
      card.append(top);
      if (r.meta) card.append(el("p", "role__meta", esc(r.meta)));

      const ul = el("ul", "role__bullets");
      ul.append(...r.bullets.map((b) => el("li", null, esc(b))));
      card.append(ul);

      if (r.tags?.length) {
        const tags = el("div", "role__tags");
        tags.append(...r.tags.map((x) => el("span", "tag", esc(x))));
        card.append(tags);
      }
      list.append(card);
    });

    org.append(id, list);
    return org;
  }));

  /* --- competências --- */
  const clusters = $("#clusters");
  clusters.textContent = "";
  clusters.append(...t.clusters.map((c, i) => {
    const card = el("article", "card");
    card.setAttribute("data-reveal", "");
    card.style.transitionDelay = `${i * 0.08}s`;
    const ul = el("ul");
    ul.append(...c.items.map((x) => el("li", null, esc(x))));
    card.append(el("h3", null, esc(c.title)), el("p", "card__line", esc(c.line)), ul);
    return card;
  }));

  /* --- formação complementar --- */
  const tl = $("#trainingList");
  tl.textContent = "";
  tl.append(...t.training.map((x) => {
    const li = el("li");
    li.setAttribute("data-reveal", "");
    li.append(el("b", null, esc(x.name)), el("span", null, esc(x.issuer)));
    return li;
  }));

  /* --- contato --- */
  $("#contactKicker").textContent = t.contact.kicker;
  $("#contactBig").innerHTML = t.contact.big;   // contém <br> e <em> de propósito

  const links = [
    { k: t.contact.keys.email,    v: profile.email,         href: `mailto:${profile.email}` },
    { k: t.contact.keys.linkedin, v: profile.linkedinLabel, href: profile.linkedin },
    { k: t.contact.keys.location, v: t.contact.location,    href: null },
  ];
  const cl = $("#contactLinks");
  cl.textContent = "";
  cl.append(...links.map((l) => {
    const node = el(l.href ? "a" : "div", "clink");
    if (l.href) {
      node.href = l.href;
      if (l.href.startsWith("http")) { node.target = "_blank"; node.rel = "noopener"; }
    }
    node.setAttribute("data-reveal", "");
    node.append(el("span", "clink__k", esc(l.k)), el("span", "clink__v", esc(l.v)));
    if (l.href) node.append(el("span", "clink__a", "↗"));
    return node;
  }));

  $("#year").textContent = new Date().getFullYear();

  /* --- estado dos botões de idioma --- */
  document.querySelectorAll(".lang__btn").forEach((b) => {
    const on = b.dataset.lang === lang;
    b.classList.toggle("is-on", on);
    b.setAttribute("aria-pressed", String(on));
  });
  $("#lang").dataset.active = lang;

  /* --- re-liga observers e interações nos nós novos --- */
  wireReveals();
  wireCounters();
  wireSpotlights();
}

/* ══════════════ idioma: estado + troca ══════════════ */

const STORE = "jb-lang";

const pickInitial = () => {
  const url = new URLSearchParams(location.search).get("lang");
  if (LANGS.includes(url)) return url;
  try {
    const saved = localStorage.getItem(STORE);
    if (LANGS.includes(saved)) return saved;
  } catch { /* localStorage bloqueado — segue no padrão */ }
  // navegador em português ganha PT, qualquer outra coisa ganha EN
  return (navigator.language || "").toLowerCase().startsWith("pt") ? "pt" : "en";
};

let current = pickInitial() || DEFAULT_LANG;

/* monta os botões uma única vez */
const langBox = $("#lang");
LANGS.forEach((code) => {
  const b = el("button", "lang__btn", esc(content[code].label));
  b.type = "button";
  b.dataset.lang = code;
  b.setAttribute("aria-label", content[code].switchTo);
  b.addEventListener("click", () => setLang(code));
  langBox.append(b);
});

function setLang(lang) {
  if (lang === current || !LANGS.includes(lang)) return;
  current = lang;
  try { localStorage.setItem(STORE, lang); } catch { /* ignorado */ }

  if (calm) { render(lang); return; }

  // fade curto para a troca não "pular" na tela
  document.body.classList.add("is-swapping");
  setTimeout(() => {
    render(lang);
    requestAnimationFrame(() => document.body.classList.remove("is-swapping"));
  }, 180);
}

render(current);

/* atalho de teclado: L alterna o idioma */
addEventListener("keydown", (e) => {
  if (e.key.toLowerCase() !== "l" || e.metaKey || e.ctrlKey || e.altKey) return;
  const tag = document.activeElement?.tagName;
  if (tag === "INPUT" || tag === "TEXTAREA") return;
  setLang(current === "pt" ? "en" : "pt");
});

/* ══════════════ barra de progresso + nav ══════════════ */

const bar = $("#bar"), nav = $("#nav");
let ticking = false;

const onScroll = () => {
  const max = document.documentElement.scrollHeight - innerHeight;
  bar.style.width = `${max > 0 ? (scrollY / max) * 100 : 0}%`;
  nav.classList.toggle("is-stuck", scrollY > 40);
  ticking = false;
};
addEventListener("scroll", () => {
  if (ticking) return;
  ticking = true;
  requestAnimationFrame(onScroll);
}, { passive: true });
onScroll();

/* ══════════════ glow do cursor ══════════════ */

if (finePointer) {
  document.body.classList.add("has-pointer");
  const glow = $("#glow");
  let gx = innerWidth / 2, gy = innerHeight / 2, cx = gx, cy = gy;

  addEventListener("pointermove", (e) => { gx = e.clientX; gy = e.clientY; }, { passive: true });

  (function trail() {
    cx += (gx - cx) * 0.09;
    cy += (gy - cy) * 0.09;
    glow.style.transform = `translate3d(${cx}px,${cy}px,0)`;
    requestAnimationFrame(trail);
  })();
}

/* ══════════════ campo ambiente (canvas) ══════════════ */
/* Blobs em deriva, desenhados a 45% da resolução e desfocados pelo
   compositor — barato, e lê como um gradiente vivo. */

(function field() {
  const cv = $("#field");
  if (calm) { cv.style.display = "none"; return; }

  const ctx = cv.getContext("2d", { alpha: true });
  const SCALE = 0.45;
  let w = 0, h = 0, raf = null;

  const blobs = [
    { r: .42, hue: "110,231,255", sx: .00011, sy: .00017, px: 0, py: 0 },
    { r: .38, hue: "167,139,250", sx: .00015, sy: .00009, px: 2, py: 4 },
    { r: .30, hue: "255,122,184", sx: .00008, sy: .00021, px: 5, py: 1 },
    { r: .34, hue: "80,120,255",  sx: .00019, sy: .00013, px: 3, py: 6 },
  ];

  const resize = () => {
    w = cv.width  = Math.max(1, Math.floor(innerWidth  * SCALE));
    h = cv.height = Math.max(1, Math.floor(innerHeight * SCALE));
    cv.style.filter = `blur(${Math.round(innerWidth * 0.045)}px)`;
  };

  const draw = (t) => {
    ctx.clearRect(0, 0, w, h);
    ctx.globalCompositeOperation = "lighter";

    blobs.forEach((b) => {
      // duas senoides por eixo → trajetória que nunca repete visivelmente
      const x = (0.5 + 0.34 * Math.sin(t * b.sx + b.px) * Math.cos(t * b.sy * 0.6)) * w;
      const y = (0.5 + 0.34 * Math.cos(t * b.sy + b.py) * Math.sin(t * b.sx * 0.8)) * h;
      const rad = b.r * Math.min(w, h);

      const g = ctx.createRadialGradient(x, y, 0, x, y, rad);
      g.addColorStop(0,   `rgba(${b.hue},.30)`);
      g.addColorStop(.45, `rgba(${b.hue},.10)`);
      g.addColorStop(1,   `rgba(${b.hue},0)`);
      ctx.fillStyle = g;
      ctx.beginPath();
      ctx.arc(x, y, rad, 0, Math.PI * 2);
      ctx.fill();
    });

    raf = requestAnimationFrame(draw);
  };

  resize();
  addEventListener("resize", resize);
  raf = requestAnimationFrame(draw);

  document.addEventListener("visibilitychange", () => {
    if (document.hidden) { cancelAnimationFrame(raf); raf = null; }
    else if (!raf) raf = requestAnimationFrame(draw);
  });
})();
