import type { BandReading } from "@/hooks/use-audio-analyzer";

export type LiveAdvice = {
  /** Suggested gain in dB per band (-8..+8) */
  suggested: number[];
  /** Short text bullets describing what to adjust right now */
  messages: { level: "info" | "warn" | "danger"; text: string }[];
};

const SOURCE_TARGETS: Record<string, Partial<Record<string, number>>> = {
  // relative target shape (dB FS) per band name — used as reference curve
  vocal: { Sub: -70, Bass: -55, "Low Mid": -45, Mid: -35, "Upper Mid": -30, Presence: -28, Brilliance: -34, Air: -42 },
  guitarra: { Sub: -75, Bass: -50, "Low Mid": -42, Mid: -34, "Upper Mid": -30, Presence: -32, Brilliance: -38, Air: -48 },
  baixo: { Sub: -45, Bass: -32, "Low Mid": -38, Mid: -42, "Upper Mid": -45, Presence: -55, Brilliance: -65, Air: -75 },
  teclado: { Sub: -65, Bass: -45, "Low Mid": -40, Mid: -36, "Upper Mid": -34, Presence: -34, Brilliance: -36, Air: -42 },
  bateria: { Sub: -40, Bass: -35, "Low Mid": -42, Mid: -38, "Upper Mid": -34, Presence: -30, Brilliance: -32, Air: -38 },
  retorno: { Sub: -70, Bass: -50, "Low Mid": -42, Mid: -36, "Upper Mid": -32, Presence: -30, Brilliance: -36, Air: -45 },
  geral: { Sub: -55, Bass: -40, "Low Mid": -38, Mid: -34, "Upper Mid": -32, Presence: -32, Brilliance: -36, Air: -42 },
};

export function computeLiveAdvice(
  source: string,
  bands: BandReading[],
  rms: number,
  peak: number,
): LiveAdvice {
  const target = SOURCE_TARGETS[source] ?? SOURCE_TARGETS.geral;

  const suggested = bands.map((b) => {
    const t = target[b.name];
    if (t === undefined) return 0;
    // diff = how far measured is above target → negative gain (cut)
    const diff = t - b.db; // positive = needs boost
    return Math.max(-8, Math.min(8, diff * 0.4));
  });

  const messages: LiveAdvice["messages"] = [];
  const rmsDb = 20 * Math.log10(Math.max(rms, 1e-6));
  const peakDb = 20 * Math.log10(Math.max(peak, 1e-6));

  if (peakDb > -1) {
    messages.push({ level: "danger", text: "CLIP — reduza o ganho do canal imediatamente" });
  } else if (peakDb > -3) {
    messages.push({ level: "warn", text: `Pico em ${peakDb.toFixed(1)} dB — perto do clip, baixe ~3 dB` });
  }

  if (rmsDb < -50 && peakDb < -20) {
    messages.push({ level: "warn", text: "Sinal muito fraco — suba o ganho ou aproxime o microfone" });
  } else if (rmsDb > -10) {
    messages.push({ level: "warn", text: "Volume médio alto — risco de compressão excessiva" });
  }

  // Hot bands — biggest cut recommendations
  const ranked = suggested
    .map((g, i) => ({ g, b: bands[i] }))
    .filter((x) => x.b.db > -85);

  const cuts = ranked.filter((x) => x.g < -2).sort((a, b) => a.g - b.g).slice(0, 2);
  const boosts = ranked.filter((x) => x.g > 2).sort((a, b) => b.g - a.g).slice(0, 2);

  for (const c of cuts) {
    messages.push({
      level: "info",
      text: `Cortar ~${Math.abs(c.g).toFixed(1)} dB em ${c.b.from}-${c.b.to} Hz (${c.b.name})`,
    });
  }
  for (const b of boosts) {
    messages.push({
      level: "info",
      text: `Realçar +${b.g.toFixed(1)} dB em ${b.b.from}-${b.b.to} Hz (${b.b.name})`,
    });
  }

  // Feedback risk — single band well above neighbors
  for (let i = 1; i < bands.length - 1; i++) {
    const local = bands[i].db;
    const neighbors = (bands[i - 1].db + bands[i + 1].db) / 2;
    if (local - neighbors > 12 && local > -45) {
      messages.push({
        level: "danger",
        text: `Risco de microfonia em ${bands[i].from} Hz — corte estreito de -6 dB`,
      });
      break;
    }
  }

  if (messages.length === 0) {
    messages.push({ level: "info", text: "Mix equilibrado — siga ouvindo" });
  }

  return { suggested, messages };
}
