import { BANDS, type BandReading } from "@/hooks/use-audio-analyzer";

type Props = { bands: BandReading[] };

// Map dB FS (-90..0) -> 0..1 height
function dbToHeight(db: number) {
  const min = -80;
  const max = -10;
  const clamped = Math.max(min, Math.min(max, db));
  return (clamped - min) / (max - min);
}

export function SpectrumBars({ bands }: Props) {
  const data = bands.length ? bands : BANDS.map((b) => ({ ...b, db: -90 }));
  return (
    <div className="flex h-64 items-end gap-2 rounded-lg bg-background/40 p-4 border">
      {data.map((b) => {
        const h = dbToHeight(b.db);
        const color =
          h > 0.85
            ? "var(--color-meter-high)"
            : h > 0.55
              ? "var(--color-meter-mid)"
              : "var(--color-meter-low)";
        return (
          <div key={b.name} className="flex flex-1 flex-col items-center gap-2">
            <div className="flex w-full flex-1 items-end">
              <div
                className="w-full rounded-sm transition-[height] duration-75 ease-out"
                style={{
                  height: `${Math.max(2, h * 100)}%`,
                  background: `linear-gradient(180deg, ${color}, oklch(0.4 0.05 60))`,
                  boxShadow: `0 0 12px ${color}`,
                }}
              />
            </div>
            <div className="text-center">
              <div className="font-mono text-[10px] text-muted-foreground">
                {b.from < 1000 ? `${b.from}` : `${(b.from / 1000).toFixed(1)}k`}
              </div>
              <div className="text-[10px] uppercase tracking-wider text-foreground/70">
                {b.name}
              </div>
              <div className="font-mono text-[10px] text-primary">
                {b.db.toFixed(0)}dB
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}

export function LevelMeter({ label, value }: { label: string; value: number }) {
  // value in 0..1
  const db = 20 * Math.log10(Math.max(value, 1e-6));
  const pct = Math.max(0, Math.min(100, ((db + 60) / 60) * 100));
  return (
    <div className="space-y-1">
      <div className="flex items-center justify-between text-xs">
        <span className="uppercase tracking-wider text-muted-foreground">{label}</span>
        <span className="font-mono text-primary">{db.toFixed(1)} dB</span>
      </div>
      <div className="relative h-3 overflow-hidden rounded-full bg-background/60 border">
        <div
          className="h-full transition-[width] duration-75"
          style={{
            width: `${pct}%`,
            background:
              "linear-gradient(90deg, var(--color-meter-low) 0%, var(--color-meter-mid) 70%, var(--color-meter-high) 95%)",
          }}
        />
      </div>
    </div>
  );
}
