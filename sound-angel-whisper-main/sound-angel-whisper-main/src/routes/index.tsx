import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useMemo, useRef, useState } from "react";
import { useServerFn } from "@tanstack/react-start";
import { AlertTriangle, Mic, MicOff, Sparkles, Save, Trash2, Music2, Radio, Loader2, Activity } from "lucide-react";

import { useAudioAnalyzer, BANDS } from "@/hooks/use-audio-analyzer";
import { SpectrumBars, LevelMeter } from "@/components/spectrum";
import { EqCurve } from "@/components/eq-curve";
import { computeLiveAdvice } from "@/lib/live-advice";
import { PRESETS } from "@/lib/presets";
import { analyzeSound } from "@/lib/sound-ai.functions";
import { supabase } from "@/integrations/supabase/client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
import { Toaster } from "@/components/ui/sonner";

export const Route = createFileRoute("/")({
  component: Console,
  head: () => ({
    meta: [
      { title: "Mixer IA — Painel de Som da Igreja" },
      {
        name: "description",
        content:
          "Análise de som ao vivo com IA: equalização, ganho, retorno e presets para vocais e instrumentos da igreja.",
      },
      { property: "og:title", content: "Mixer IA — Painel de Som da Igreja" },
      {
        property: "og:description",
        content: "Ouça, analise e ajuste o som da igreja com recomendações em linguagem natural.",
      },
    ],
  }),
});

type Session = {
  id: string;
  name: string;
  source: string;
  rms: number;
  peak: number;
  recommendations: string | null;
  created_at: string;
};

function Console() {
  const { start, stop, isRunning, error, analysis } = useAudioAnalyzer();
  const [activeTab, setActiveTab] = useState<"single" | "double">("single");

  // Single mode states
  const [source, setSource] = useState("vocal");
  const [aiText, setAiText] = useState<string>("");
  const [aiLoading, setAiLoading] = useState(false);

  // Double mode states: Left (Vocal)
  const [sourceLeft, setSourceLeft] = useState("vocal");
  const [aiTextLeft, setAiTextLeft] = useState("");
  const [aiLoadingLeft, setAiLoadingLeft] = useState(false);

  // Double mode states: Right (Instrumental)
  const [sourceRight, setSourceRight] = useState("guitarra");
  const [aiTextRight, setAiTextRight] = useState("");
  const [aiLoadingRight, setAiLoadingRight] = useState(false);

  const [sessions, setSessions] = useState<Session[]>([]);
  const [sessionName, setSessionName] = useState("");
  const analyzeFn = useServerFn(analyzeSound);

  const preset = PRESETS.find((p) => p.id === source) ?? PRESETS[0];

  // Vocal vs Instrumental presets separation
  const vocalPresets = PRESETS.filter((p) => p.id === "vocal" || p.id === "retorno" || p.id === "geral");
  const instrumentalPresets = PRESETS.filter((p) => p.id === "guitarra" || p.id === "baixo" || p.id === "teclado" || p.id === "bateria");

  // Live (rule-based) advice — recomputed every render from analysis
  const advice = useMemo(
    () => computeLiveAdvice(source, analysis.bands, analysis.rms, analysis.peak),
    [source, analysis],
  );

  const adviceLeft = useMemo(
    () => computeLiveAdvice(sourceLeft, analysis.bands, analysis.rms, analysis.peak),
    [sourceLeft, analysis],
  );

  const adviceRight = useMemo(
    () => computeLiveAdvice(sourceRight, analysis.bands, analysis.rms, analysis.peak),
    [sourceRight, analysis],
  );

  // Smooth the suggested EQ curves so they animate instead of jittering
  const smoothedRef = useRef<number[]>(advice.suggested.map(() => 0));
  const [smoothed, setSmoothed] = useState<number[]>(() => advice.suggested.map(() => 0));

  const smoothedLeftRef = useRef<number[]>([]);
  const [smoothedLeft, setSmoothedLeft] = useState<number[]>(() => BANDS.map(() => 0));

  const smoothedRightRef = useRef<number[]>([]);
  const [smoothedRight, setSmoothedRight] = useState<number[]>(() => BANDS.map(() => 0));

  useEffect(() => {
    if (!isRunning) return;
    let raf = 0;
    const tick = () => {
      // Single Mode
      const target = advice.suggested;
      const cur = smoothedRef.current;
      const next = target.map((t, i) => {
        const c = cur[i] ?? 0;
        return c + (t - c) * 0.15;
      });
      smoothedRef.current = next;
      setSmoothed(next);

      // Left Column (Vocal)
      const targetL = adviceLeft.suggested;
      const curL = smoothedLeftRef.current.length ? smoothedLeftRef.current : targetL.map(() => 0);
      const nextL = targetL.map((t, i) => {
        const c = curL[i] ?? 0;
        return c + (t - c) * 0.15;
      });
      smoothedLeftRef.current = nextL;
      setSmoothedLeft(nextL);

      // Right Column (Instrumental)
      const targetR = adviceRight.suggested;
      const curR = smoothedRightRef.current.length ? smoothedRightRef.current : targetR.map(() => 0);
      const nextR = targetR.map((t, i) => {
        const c = curR[i] ?? 0;
        return c + (t - c) * 0.15;
      });
      smoothedRightRef.current = nextR;
      setSmoothedRight(nextR);

      raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [advice.suggested, adviceLeft.suggested, adviceRight.suggested, isRunning]);


  const loadSessions = async () => {
    const { data, error: e } = await supabase
      .from("sound_sessions")
      .select("id,name,source,rms,peak,recommendations,created_at")
      .order("created_at", { ascending: false })
      .limit(20);
    if (!e && data) setSessions(data as Session[]);
  };

  useEffect(() => {
    loadSessions();
  }, []);

  useEffect(() => {
    if (error) toast.error(error);
  }, [error]);

  const handleAnalyze = async () => {
    if (!isRunning) {
      toast.error("Inicie a captura do microfone primeiro");
      return;
    }
    setAiLoading(true);
    try {
      const res = await analyzeFn({
        data: {
          source,
          bands: analysis.bands,
          rms: analysis.rms,
          peak: analysis.peak,
        },
      });
      setAiText(res.recommendations);
      if (res.error) toast.error("IA retornou erro");
      else toast.success("Análise pronta");
    } catch (e) {
      toast.error("Falha ao analisar");
    } finally {
      setAiLoading(false);
    }
  };

  const handleAnalyzeLeft = async () => {
    if (!isRunning) {
      toast.error("Inicie a captura do microfone primeiro");
      return;
    }
    setAiLoadingLeft(true);
    try {
      const res = await analyzeFn({
        data: {
          source: sourceLeft,
          bands: analysis.bands,
          rms: analysis.rms,
          peak: analysis.peak,
        },
      });
      setAiTextLeft(res.recommendations);
      if (res.error) toast.error("IA retornou erro");
      else toast.success("Análise Vocal pronta");
    } catch (e) {
      toast.error("Falha ao analisar");
    } finally {
      setAiLoadingLeft(false);
    }
  };

  const handleAnalyzeRight = async () => {
    if (!isRunning) {
      toast.error("Inicie a captura do microfone primeiro");
      return;
    }
    setAiLoadingRight(true);
    try {
      const res = await analyzeFn({
        data: {
          source: sourceRight,
          bands: analysis.bands,
          rms: analysis.rms,
          peak: analysis.peak,
        },
      });
      setAiTextRight(res.recommendations);
      if (res.error) toast.error("IA retornou erro");
      else toast.success("Análise Instrumental pronta");
    } catch (e) {
      toast.error("Falha ao analisar");
    } finally {
      setAiLoadingRight(false);
    }
  };

  const handleSave = async () => {
    const activePreset = PRESETS.find((p) => p.id === (activeTab === "single" ? source : sourceLeft)) ?? PRESETS[0];
    const name = sessionName.trim() || `${activePreset.label} — ${new Date().toLocaleTimeString()}`;
    const { error: e } = await supabase.from("sound_sessions").insert({
      name,
      source: activeTab === "single" ? source : `${sourceLeft} & ${sourceRight}`,
      bands: analysis.bands as never,
      rms: analysis.rms,
      peak: analysis.peak,
      recommendations: activeTab === "single" ? (aiText || null) : (`Vocal: ${aiTextLeft} \n\nInstrumental: ${aiTextRight}`),
    });
    if (e) toast.error("Erro ao salvar");
    else {
      toast.success("Sessão salva");
      setSessionName("");
      loadSessions();
    }
  };

  const handleDelete = async (id: string) => {
    await supabase.from("sound_sessions").delete().eq("id", id);
    loadSessions();
  };

  return (
    <div className="min-h-screen">
      <Toaster theme="dark" position="top-right" />

      {/* Top bar */}
      <header className="border-b bg-background/80 backdrop-blur sticky top-0 z-10">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <div className="flex items-center gap-3">
            <div className="grid h-10 w-10 place-items-center rounded-md bg-primary/15 border border-primary/30">
              <Radio className="h-5 w-5 text-primary" />
            </div>
            <div>
              <h1 className="text-xl font-bold tracking-tight">
                <span className="ember-text">MIXER IA</span> · Som da Igreja
              </h1>
              <p className="text-xs text-muted-foreground">
                Análise ao vivo · EQ · Retorno · Presets de instrumentos
              </p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              <span className={`led ${isRunning ? "led-on" : ""}`} />
              <span className="text-xs uppercase tracking-wider text-muted-foreground">
                {isRunning ? "Captando" : "Parado"}
              </span>
            </div>
            {isRunning ? (
              <Button variant="secondary" size="sm" onClick={stop} className="h-8">
                <MicOff className="mr-1.5 h-3.5 w-3.5" /> Desligar Captura
              </Button>
            ) : (
              <Button size="sm" onClick={start} className="bg-primary hover:bg-primary/90 h-8">
                <Mic className="mr-1.5 h-3.5 w-3.5" /> Ligar Captura
              </Button>
            )}
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl space-y-6 px-6 py-8">
        
        {/* Navigation / Mode selector tabs */}
        <div className="flex gap-1.5 p-1 bg-secondary/60 backdrop-blur rounded-lg border max-w-md mx-auto">
          <button
            onClick={() => setActiveTab("single")}
            className={`flex-1 flex items-center justify-center gap-2 py-2 text-xs font-semibold rounded-md transition-all ${
              activeTab === "single"
                ? "bg-background text-foreground shadow-sm"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            <Radio className="h-3.5 w-3.5" />
            Canal Único
          </button>
          <button
            onClick={() => setActiveTab("double")}
            className={`flex-1 flex items-center justify-center gap-2 py-2 text-xs font-semibold rounded-md transition-all ${
              activeTab === "double"
                ? "bg-background text-foreground shadow-sm"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            <Activity className="h-3.5 w-3.5" />
            Painel Duplo (Vocal & Instrumento)
          </button>
        </div>

        {activeTab === "double" ? (
          /* ======================================================================= */
          /* NEW DUAL DASHBOARD VIEW (Vocal on the left, Instrumental on the right) */
          /* ======================================================================= */
          <div className="grid gap-6 lg:grid-cols-2">
            
            {/* LADO ESQUERDO — VOCAL */}
            <div className="space-y-6">
              
              {/* Header Panel */}
              <section className="panel p-5 border-l-4 border-l-primary relative overflow-hidden">
                <div className="absolute top-0 right-0 p-4 opacity-5">
                  <Mic className="h-20 w-20 text-primary" />
                </div>
                
                <div className="flex flex-col gap-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Mic className="h-5 w-5 text-primary animate-pulse" />
                      <h2 className="text-base font-bold tracking-tight uppercase text-primary">
                        Canal Vocal (Lado Esquerdo)
                      </h2>
                    </div>
                    <span className="rounded-full bg-primary/10 px-2.5 py-0.5 text-[10px] font-semibold text-primary uppercase border border-primary/20">
                      Vocal
                    </span>
                  </div>

                  <div className="flex gap-2">
                    {vocalPresets.map((p) => (
                      <button
                        key={p.id}
                        onClick={() => setSourceLeft(p.id)}
                        className={`rounded-md border px-3 py-1 text-xs font-medium transition-all ${
                          sourceLeft === p.id
                            ? "border-primary bg-primary text-primary-foreground glow-primary"
                            : "border-border bg-secondary text-foreground hover:border-primary/50"
                        }`}
                      >
                        {p.label}
                      </button>
                    ))}
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {PRESETS.find((p) => p.id === sourceLeft)?.hint}
                  </p>
                </div>
              </section>

              {/* Real-time frequencies & EQ curve */}
              <section className="panel space-y-4 p-5">
                <div className="flex items-center justify-between border-b border-border/40 pb-2">
                  <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                    Análise Frequencial & Curva EQ (Vocal)
                  </h3>
                  {isRunning ? (
                    <span className="text-[9px] uppercase font-mono px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center gap-1">
                      <span className="h-1 w-1 rounded-full bg-emerald-500 animate-ping" />
                      Ativo
                    </span>
                  ) : (
                    <span className="text-[9px] uppercase font-mono px-2 py-0.5 rounded bg-muted text-muted-foreground border border-border">
                      Parado
                    </span>
                  )}
                </div>

                <SpectrumBars bands={analysis.bands} />
                <EqCurve bands={analysis.bands} suggested={smoothedLeft} />

                <div className="grid gap-3 sm:grid-cols-2">
                  <LevelMeter label="RMS (vocal)" value={analysis.rms} />
                  <LevelMeter label="Pico (vocal)" value={analysis.peak} />
                </div>
              </section>

              {/* Live adjustment guidelines */}
              <section className="panel p-5 space-y-3">
                <div className="flex items-center gap-2 border-b border-border/40 pb-2">
                  <Activity className="h-4 w-4 text-primary animate-pulse" />
                  <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                    Ajustes em Tempo Real
                  </h3>
                </div>
                {isRunning ? (
                  <ul className="space-y-1.5">
                    {adviceLeft.messages.map((m, i) => {
                      const color =
                        m.level === "danger"
                          ? "text-destructive border-destructive/40 bg-destructive/10"
                          : m.level === "warn"
                            ? "text-warn border-warn/40 bg-warn/10"
                            : "text-foreground/85 border-border bg-secondary/40";
                      return (
                        <li
                          key={i}
                          className={`flex items-start gap-2 rounded border px-2.5 py-1.5 text-xs ${color}`}
                        >
                          {m.level !== "info" && <AlertTriangle className="mt-0.5 h-3 w-3 shrink-0" />}
                          <span>{m.text}</span>
                        </li>
                      );
                    })}
                  </ul>
                ) : (
                  <p className="text-xs text-muted-foreground">
                    Inicie o microfone para monitorar em tempo real.
                  </p>
                )}
              </section>

              {/* AI suggestion */}
              <section className="panel p-5 space-y-4">
                <div className="flex items-center justify-between border-b border-border/40 pb-2">
                  <div className="flex items-center gap-2">
                    <Sparkles className="h-4 w-4 text-accent animate-pulse" />
                    <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                      Análise Inteligente por IA (Vocal)
                    </h3>
                  </div>
                  <Button
                    size="sm"
                    onClick={handleAnalyzeLeft}
                    disabled={aiLoadingLeft || !isRunning}
                    className="bg-gradient-to-r from-primary to-accent text-primary-foreground hover:opacity-90 h-8 px-3 text-xs"
                  >
                    {aiLoadingLeft ? (
                      <Loader2 className="mr-1 h-3.5 w-3.5 animate-spin" />
                    ) : (
                      <Sparkles className="mr-1 h-3.5 w-3.5" />
                    )}
                    Pedir Ajuste IA
                  </Button>
                </div>

                {aiTextLeft ? (
                  <div className="bg-background/40 border border-border/30 rounded-lg p-3">
                    <pre className="whitespace-pre-wrap font-sans text-xs leading-relaxed text-foreground/90">
                      {aiTextLeft}
                    </pre>
                  </div>
                ) : (
                  <p className="text-xs text-muted-foreground">
                    Clique em <em>Pedir Ajuste IA</em> para receber recomendações especializadas do Gemini sobre equalização, ganho e retorno do vocal.
                  </p>
                )}
              </section>
            </div>

            {/* LADO DIREITO — INSTRUMENTAL */}
            <div className="space-y-6">
              
              {/* Header Panel */}
              <section className="panel p-5 border-l-4 border-l-accent relative overflow-hidden">
                <div className="absolute top-0 right-0 p-4 opacity-5">
                  <Music2 className="h-20 w-20 text-accent" />
                </div>
                
                <div className="flex flex-col gap-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Music2 className="h-5 w-5 text-accent animate-pulse" />
                      <h2 className="text-base font-bold tracking-tight uppercase text-accent">
                        Canal Instrumental (Lado Direito)
                      </h2>
                    </div>
                    <span className="rounded-full bg-accent/10 px-2.5 py-0.5 text-[10px] font-semibold text-accent uppercase border border-accent/20">
                      Instrumento
                    </span>
                  </div>

                  <div className="flex flex-wrap gap-2">
                    {instrumentalPresets.map((p) => (
                      <button
                        key={p.id}
                        onClick={() => setSourceRight(p.id)}
                        className={`rounded-md border px-3 py-1 text-xs font-medium transition-all ${
                          sourceRight === p.id
                            ? "border-accent bg-accent text-accent-foreground glow-primary"
                            : "border-border bg-secondary text-foreground hover:border-primary/50"
                        }`}
                      >
                        {p.label}
                      </button>
                    ))}
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {PRESETS.find((p) => p.id === sourceRight)?.hint}
                  </p>
                </div>
              </section>

              {/* Real-time frequencies & EQ curve */}
              <section className="panel space-y-4 p-5">
                <div className="flex items-center justify-between border-b border-border/40 pb-2">
                  <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                    Análise Frequencial & Curva EQ (Instrumento)
                  </h3>
                  {isRunning ? (
                    <span className="text-[9px] uppercase font-mono px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center gap-1">
                      <span className="h-1 w-1 rounded-full bg-emerald-500 animate-ping" />
                      Ativo
                    </span>
                  ) : (
                    <span className="text-[9px] uppercase font-mono px-2 py-0.5 rounded bg-muted text-muted-foreground border border-border">
                      Parado
                    </span>
                  )}
                </div>

                <SpectrumBars bands={analysis.bands} />
                <EqCurve bands={analysis.bands} suggested={smoothedRight} />

                <div className="grid gap-3 sm:grid-cols-2">
                  <LevelMeter label="RMS (instrumento)" value={analysis.rms} />
                  <LevelMeter label="Pico (instrumento)" value={analysis.peak} />
                </div>
              </section>

              {/* Live adjustment guidelines */}
              <section className="panel p-5 space-y-3">
                <div className="flex items-center gap-2 border-b border-border/40 pb-2">
                  <Activity className="h-4 w-4 text-accent animate-pulse" />
                  <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                    Ajustes em Tempo Real
                  </h3>
                </div>
                {isRunning ? (
                  <ul className="space-y-1.5">
                    {adviceRight.messages.map((m, i) => {
                      const color =
                        m.level === "danger"
                          ? "text-destructive border-destructive/40 bg-destructive/10"
                          : m.level === "warn"
                            ? "text-warn border-warn/40 bg-warn/10"
                            : "text-foreground/85 border-border bg-secondary/40";
                      return (
                        <li
                          key={i}
                          className={`flex items-start gap-2 rounded border px-2.5 py-1.5 text-xs ${color}`}
                        >
                          {m.level !== "info" && <AlertTriangle className="mt-0.5 h-3 w-3 shrink-0" />}
                          <span>{m.text}</span>
                        </li>
                      );
                    })}
                  </ul>
                ) : (
                  <p className="text-xs text-muted-foreground">
                    Inicie o microfone para monitorar em tempo real.
                  </p>
                )}
              </section>

              {/* AI suggestion */}
              <section className="panel p-5 space-y-4">
                <div className="flex items-center justify-between border-b border-border/40 pb-2">
                  <div className="flex items-center gap-2">
                    <Sparkles className="h-4 w-4 text-accent animate-pulse" />
                    <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                      Análise Inteligente por IA (Instrumento)
                    </h3>
                  </div>
                  <Button
                    size="sm"
                    onClick={handleAnalyzeRight}
                    disabled={aiLoadingRight || !isRunning}
                    className="bg-gradient-to-r from-accent to-primary text-accent-foreground hover:opacity-90 h-8 px-3 text-xs"
                  >
                    {aiLoadingRight ? (
                      <Loader2 className="mr-1 h-3.5 w-3.5 animate-spin" />
                    ) : (
                      <Sparkles className="mr-1 h-3.5 w-3.5" />
                    )}
                    Pedir Ajuste IA
                  </Button>
                </div>

                {aiTextRight ? (
                  <div className="bg-background/40 border border-border/30 rounded-lg p-3">
                    <pre className="whitespace-pre-wrap font-sans text-xs leading-relaxed text-foreground/90">
                      {aiTextRight}
                    </pre>
                  </div>
                ) : (
                  <p className="text-xs text-muted-foreground">
                    Clique em <em>Pedir Ajuste IA</em> para receber recomendações especializadas do Gemini sobre equalização, ganho e retorno do instrumento.
                  </p>
                )}
              </section>
            </div>
          </div>
        ) : (
          /* ======================================================================= */
          /* ORIGINAL SINGLE CHANNEL DASHBOARD VIEW                                 */
          /* ======================================================================= */
          <>
            {/* Source selector */}
            <section className="panel p-5">
              <div className="mb-3 flex items-center gap-2">
                <Music2 className="h-4 w-4 text-accent" />
                <h2 className="text-sm font-semibold uppercase tracking-wider">
                  Fonte sendo medida
                </h2>
              </div>
              <div className="flex flex-wrap gap-2">
                {PRESETS.map((p) => (
                  <button
                    key={p.id}
                    onClick={() => setSource(p.id)}
                    className={`rounded-md border px-4 py-2 text-sm font-medium transition-all ${
                      source === p.id
                        ? "border-primary bg-primary text-primary-foreground glow-primary"
                        : "border-border bg-secondary text-foreground hover:border-primary/50"
                    }`}
                  >
                    {p.label}
                  </button>
                ))}
              </div>
              <p className="mt-3 text-xs text-muted-foreground">{preset.hint}</p>
            </section>

            {/* Main grid */}
            <div className="grid gap-6 lg:grid-cols-3">
              {/* Spectrum + meters */}
              <section className="panel space-y-4 p-5 lg:col-span-2">
                <div className="flex items-center justify-between">
                  <h2 className="text-sm font-semibold uppercase tracking-wider">
                    Analisador em tempo real
                  </h2>
                </div>

                <SpectrumBars bands={analysis.bands} />

                <EqCurve bands={analysis.bands} suggested={smoothed} />

                <div className="grid gap-4 sm:grid-cols-2">
                  <LevelMeter label="RMS (volume médio)" value={analysis.rms} />
                  <LevelMeter label="Pico" value={analysis.peak} />
                </div>

                <div className="flex flex-wrap gap-2 pt-2">
                  <Button
                    onClick={handleAnalyze}
                    disabled={aiLoading || !isRunning}
                    className="bg-gradient-to-r from-primary to-accent text-primary-foreground hover:opacity-90 animate-pulse"
                  >
                    {aiLoading ? (
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    ) : (
                      <Sparkles className="mr-2 h-4 w-4" />
                    )}
                    Pedir ajuste à IA
                  </Button>
                </div>
              </section>

              {/* Live advice + preset tips */}
              <aside className="panel space-y-4 p-5">
                <div>
                  <div className="mb-2 flex items-center gap-2">
                    <Activity className={`h-4 w-4 ${isRunning ? "text-primary animate-pulse" : "text-muted-foreground"}`} />
                    <h2 className="text-sm font-semibold uppercase tracking-wider">
                      Ajustes ao vivo
                    </h2>
                  </div>
                  {isRunning ? (
                    <ul className="space-y-1.5">
                      {advice.messages.map((m, i) => {
                        const color =
                          m.level === "danger"
                            ? "text-destructive border-destructive/40 bg-destructive/10"
                            : m.level === "warn"
                              ? "text-warn border-warn/40 bg-warn/10"
                              : "text-foreground/85 border-border bg-secondary/40";
                        return (
                          <li
                            key={i}
                            className={`flex items-start gap-2 rounded border px-2.5 py-1.5 text-xs ${color}`}
                          >
                            {m.level !== "info" && <AlertTriangle className="mt-0.5 h-3 w-3 shrink-0" />}
                            <span>{m.text}</span>
                          </li>
                        );
                      })}
                    </ul>
                  ) : (
                    <p className="text-xs text-muted-foreground">
                      Inicie o microfone para receber dicas em tempo real.
                    </p>
                  )}
                </div>

                <div className="border-t border-border pt-3">
                  <h3 className="mb-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                    Preset · {preset.label}
                  </h3>
                  <ul className="space-y-1.5">
                    {preset.tips.map((t) => (
                      <li key={t} className="flex gap-2 text-xs text-foreground/75">
                        <span className="mt-1.5 h-1 w-1 shrink-0 rounded-full bg-primary" />
                        <span>{t}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </aside>
            </div>

            {/* AI output */}
            <section className="panel p-5">
              <div className="mb-3 flex items-center gap-2">
                <Sparkles className="h-4 w-4 text-accent" />
                <h2 className="text-sm font-semibold uppercase tracking-wider">
                  Recomendação da IA
                </h2>
              </div>
              {aiText ? (
                <pre className="whitespace-pre-wrap font-sans text-sm leading-relaxed text-foreground/90">
                  {aiText}
                </pre>
              ) : (
                <p className="text-sm text-muted-foreground">
                  Inicie o microfone, escolha a fonte e clique em <em>Pedir ajuste à IA</em>.
                  A análise considera as 8 bandas de frequência, RMS e pico para sugerir EQ,
                  ganho e ajuste de retorno.
                </p>
              )}
            </section>
          </>
        )}

        {/* Global Save Controls for both modes */}
        <section className="panel p-5">
          <div className="flex flex-wrap items-center gap-4">
            <div className="flex-1 min-w-[200px]">
              <h3 className="text-sm font-semibold uppercase tracking-wider">Salvar Sessão de Som</h3>
              <p className="text-xs text-muted-foreground">Grave a equalização e a análise atual no histórico.</p>
            </div>
            <div className="flex flex-1 gap-2 max-w-xl">
              <Input
                placeholder="Nome do registro (ex: Culto de Domingo - Vocal e Guitarra)"
                value={sessionName}
                onChange={(e) => setSessionName(e.target.value)}
                className="bg-background/60"
              />
              <Button variant="secondary" onClick={handleSave}>
                <Save className="mr-2 h-4 w-4" /> Salvar Sessão
              </Button>
            </div>
          </div>
        </section>

        {/* History */}
        <section className="panel p-5">
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wider">
            Histórico de sessões salvas
          </h2>
          {sessions.length === 0 ? (
            <p className="text-sm text-muted-foreground">Nenhuma sessão salva ainda.</p>
          ) : (
            <ul className="divide-y divide-border">
              {sessions.map((s) => (
                <li key={s.id} className="flex items-start justify-between gap-4 py-3">
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <span className="rounded bg-primary/15 px-2 py-0.5 text-[10px] uppercase tracking-wider text-primary">
                        {s.source}
                      </span>
                      <span className="truncate font-medium">{s.name}</span>
                    </div>
                    {s.recommendations && (
                      <pre className="mt-2 p-3 whitespace-pre-wrap font-sans text-xs bg-background/30 rounded border border-border/40 text-muted-foreground leading-relaxed">
                        {s.recommendations}
                      </pre>
                    )}
                    <p className="mt-1.5 font-mono text-[10px] text-muted-foreground">
                      {new Date(s.created_at).toLocaleString("pt-BR")}
                    </p>
                  </div>
                  <button
                    onClick={() => handleDelete(s.id)}
                    className="text-muted-foreground hover:text-destructive p-1"
                    aria-label="Excluir"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>
    </div>
  );
}
