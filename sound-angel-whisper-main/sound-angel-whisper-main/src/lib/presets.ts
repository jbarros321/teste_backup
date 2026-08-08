export type Preset = {
  id: string;
  label: string;
  hint: string;
  tips: string[];
};

export const PRESETS: Preset[] = [
  {
    id: "vocal",
    label: "Vocal",
    hint: "Voz principal / coral",
    tips: [
      "High-pass em 80–100 Hz",
      "Corte leve em 250–400 Hz (tira o abafado)",
      "Realce suave em 3–5 kHz (presença)",
      "De-esser entre 6–8 kHz se necessário",
      "Compressão 3:1, ataque médio",
    ],
  },
  {
    id: "guitarra",
    label: "Guitarra",
    hint: "Elétrica ou acústica",
    tips: [
      "High-pass em 100 Hz",
      "Cortar 300–500 Hz para limpar a mistura",
      "Realçar 2.5–4 kHz para definição",
      "Cuidado com 8 kHz se microfonando amp",
    ],
  },
  {
    id: "baixo",
    label: "Baixo",
    hint: "Contrabaixo elétrico",
    tips: [
      "Realçar 60–100 Hz (corpo)",
      "Cortar 200–300 Hz se ficar 'enlameado'",
      "Realçar 700 Hz–1 kHz (ataque)",
      "Compressão 4:1 para uniformizar",
    ],
  },
  {
    id: "teclado",
    label: "Teclado",
    hint: "Piano, pads, synth",
    tips: [
      "High-pass em 60 Hz",
      "Cortar 250–400 Hz se conflitar com guitarra",
      "Realçar 8–10 kHz (brilho do piano)",
      "Em estéreo, panorama largo",
    ],
  },
  {
    id: "bateria",
    label: "Bateria",
    hint: "Mix geral",
    tips: [
      "Kick: realçar 60 Hz e 3–5 kHz, cortar 300 Hz",
      "Caixa: realçar 200 Hz (corpo) e 5 kHz (snap)",
      "Pratos: high-pass em 300 Hz",
      "Compressão paralela na sala",
    ],
  },
  {
    id: "retorno",
    label: "Retorno (Monitor)",
    hint: "Caixa do palco / in-ear",
    tips: [
      "Cortar frequências de feedback (200 Hz, 1 kHz, 4 kHz)",
      "Manter mix simples: só o que o músico precisa",
      "Volume só o suficiente — protege o ouvido",
      "Para vocalista: realce sua própria voz +3 dB",
    ],
  },
  {
    id: "geral",
    label: "PA Geral",
    hint: "Mix da casa",
    tips: [
      "Verifique resposta da sala com música conhecida",
      "Corte 250 Hz se a sala 'troar'",
      "Cuidado com 2 kHz (cansaço auditivo)",
      "RMS alvo: -18 a -14 dB FS",
    ],
  },
];
