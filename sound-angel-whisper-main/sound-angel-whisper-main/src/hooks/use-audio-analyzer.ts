import { useCallback, useEffect, useRef, useState } from "react";

// 8 frequency bands roughly mapping to mixer EQ ranges
export const BANDS = [
  { name: "Sub", from: 20, to: 60 },
  { name: "Bass", from: 60, to: 150 },
  { name: "Low Mid", from: 150, to: 400 },
  { name: "Mid", from: 400, to: 1000 },
  { name: "Upper Mid", from: 1000, to: 2500 },
  { name: "Presence", from: 2500, to: 5000 },
  { name: "Brilliance", from: 5000, to: 10000 },
  { name: "Air", from: 10000, to: 20000 },
] as const;

export type BandReading = { name: string; from: number; to: number; db: number };

export type AudioAnalysis = {
  bands: BandReading[];
  rms: number; // 0..1
  peak: number; // 0..1
};

export function useAudioAnalyzer() {
  const [isRunning, setIsRunning] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [analysis, setAnalysis] = useState<AudioAnalysis>({
    bands: BANDS.map((b) => ({ ...b, db: -90 })),
    rms: 0,
    peak: 0,
  });

  const ctxRef = useRef<AudioContext | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const analyserRef = useRef<AnalyserNode | null>(null);
  const rafRef = useRef<number | null>(null);

  const stop = useCallback(() => {
    if (rafRef.current) cancelAnimationFrame(rafRef.current);
    rafRef.current = null;
    streamRef.current?.getTracks().forEach((t) => t.stop());
    streamRef.current = null;
    ctxRef.current?.close().catch(() => {});
    ctxRef.current = null;
    analyserRef.current = null;
    setIsRunning(false);
  }, []);

  const start = useCallback(async () => {
    setError(null);
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: false,
          noiseSuppression: false,
          autoGainControl: false,
        },
      });
      streamRef.current = stream;
      const Ctx = window.AudioContext || (window as any).webkitAudioContext;
      const ctx: AudioContext = new Ctx();
      ctxRef.current = ctx;
      const source = ctx.createMediaStreamSource(stream);
      const analyser = ctx.createAnalyser();
      analyser.fftSize = 4096;
      analyser.smoothingTimeConstant = 0.7;
      source.connect(analyser);
      analyserRef.current = analyser;

      const freqData = new Float32Array(analyser.frequencyBinCount);
      const timeData = new Float32Array(analyser.fftSize);
      const sampleRate = ctx.sampleRate;
      const binHz = sampleRate / analyser.fftSize;

      let runningPeak = 0;

      const tick = () => {
        analyser.getFloatFrequencyData(freqData);
        analyser.getFloatTimeDomainData(timeData);

        // RMS
        let sumSq = 0;
        let peak = 0;
        for (let i = 0; i < timeData.length; i++) {
          const v = timeData[i];
          sumSq += v * v;
          const a = Math.abs(v);
          if (a > peak) peak = a;
        }
        const rms = Math.sqrt(sumSq / timeData.length);
        runningPeak = Math.max(runningPeak * 0.95, peak);

        const bands: BandReading[] = BANDS.map((b) => {
          const startBin = Math.max(1, Math.floor(b.from / binHz));
          const endBin = Math.min(freqData.length - 1, Math.ceil(b.to / binHz));
          let sum = 0;
          let count = 0;
          for (let i = startBin; i <= endBin; i++) {
            sum += freqData[i];
            count++;
          }
          const db = count > 0 ? sum / count : -90;
          return { name: b.name, from: b.from, to: b.to, db };
        });

        setAnalysis({ bands, rms, peak: runningPeak });
        rafRef.current = requestAnimationFrame(tick);
      };
      tick();
      setIsRunning(true);
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Erro ao acessar o microfone";
      setError(msg);
      stop();
    }
  }, [stop]);

  useEffect(() => () => stop(), [stop]);

  return { start, stop, isRunning, error, analysis };
}
