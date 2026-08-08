import { useEffect, useRef } from "react";
import type { BandReading } from "@/hooks/use-audio-analyzer";

type Props = {
  bands: BandReading[];
  /** Suggested gain in dB per band (-6..+6). Same length as bands. */
  suggested: number[];
};

// Smooth Catmull-Rom-ish path through points
function smoothPath(points: { x: number; y: number }[]) {
  if (points.length < 2) return "";
  let d = `M ${points[0].x} ${points[0].y}`;
  for (let i = 0; i < points.length - 1; i++) {
    const p0 = points[i - 1] ?? points[i];
    const p1 = points[i];
    const p2 = points[i + 1];
    const p3 = points[i + 2] ?? p2;
    const cp1x = p1.x + (p2.x - p0.x) / 6;
    const cp1y = p1.y + (p2.y - p0.y) / 6;
    const cp2x = p2.x - (p3.x - p1.x) / 6;
    const cp2y = p2.y - (p3.y - p1.y) / 6;
    d += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${p2.x} ${p2.y}`;
  }
  return d;
}

export function EqCurve({ bands, suggested }: Props) {
  const W = 800;
  const H = 220;
  const PAD_X = 40;
  const PAD_Y = 20;
  const innerW = W - PAD_X * 2;
  const innerH = H - PAD_Y * 2;
  const midY = PAD_Y + innerH / 2;

  // gain range -8..+8 dB
  const gainToY = (g: number) => midY - (g / 8) * (innerH / 2);

  const points = bands.map((b, i) => ({
    x: PAD_X + (i / Math.max(1, bands.length - 1)) * innerW,
    y: gainToY(suggested[i] ?? 0),
    band: b,
    gain: suggested[i] ?? 0,
  }));

  const linePath = smoothPath(points);
  const areaPath = linePath
    ? `${linePath} L ${points[points.length - 1].x} ${midY} L ${points[0].x} ${midY} Z`
    : "";

  // Animate path morphing via SVG attribute updates is already smooth because we re-render each RAF.
  const pathRef = useRef<SVGPathElement>(null);

  useEffect(() => {
    if (pathRef.current) {
      pathRef.current.style.transition = "d 80ms linear";
    }
  }, []);

  return (
    <div className="rounded-lg border bg-background/40 p-3">
      <div className="mb-2 flex items-center justify-between">
        <span className="text-xs uppercase tracking-wider text-muted-foreground">
          EQ Sugerido (tempo real)
        </span>
        <span className="font-mono text-[10px] text-muted-foreground">
          −8 dB &nbsp;|&nbsp; 0 &nbsp;|&nbsp; +8 dB
        </span>
      </div>
      <svg
        viewBox={`0 0 ${W} ${H}`}
        className="h-56 w-full"
        preserveAspectRatio="none"
      >
        {/* grid */}
        {[-6, -3, 0, 3, 6].map((g) => (
          <g key={g}>
            <line
              x1={PAD_X}
              x2={W - PAD_X}
              y1={gainToY(g)}
              y2={gainToY(g)}
              stroke="var(--color-border)"
              strokeDasharray={g === 0 ? "0" : "3 4"}
              strokeWidth={g === 0 ? 1 : 0.5}
              opacity={g === 0 ? 0.9 : 0.5}
            />
            <text
              x={PAD_X - 6}
              y={gainToY(g) + 3}
              textAnchor="end"
              className="fill-muted-foreground"
              fontSize="9"
              fontFamily="JetBrains Mono, monospace"
            >
              {g > 0 ? `+${g}` : g}
            </text>
          </g>
        ))}

        {/* area fill */}
        <defs>
          <linearGradient id="eqFill" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="var(--color-primary)" stopOpacity="0.45" />
            <stop offset="100%" stopColor="var(--color-primary)" stopOpacity="0.05" />
          </linearGradient>
        </defs>
        <path d={areaPath} fill="url(#eqFill)" />

        {/* curve */}
        <path
          ref={pathRef}
          d={linePath}
          fill="none"
          stroke="var(--color-primary)"
          strokeWidth="2.5"
          strokeLinecap="round"
          strokeLinejoin="round"
          style={{ filter: "drop-shadow(0 0 6px var(--color-primary))" }}
        />

        {/* points + labels */}
        {points.map((p) => {
          const hot = Math.abs(p.gain) > 3;
          return (
            <g key={p.band.name}>
              <circle
                cx={p.x}
                cy={p.y}
                r={hot ? 5 : 3.5}
                fill={hot ? "var(--color-meter-high)" : "var(--color-accent)"}
                stroke="var(--color-background)"
                strokeWidth="1.5"
              />
              <text
                x={p.x}
                y={H - 6}
                textAnchor="middle"
                className="fill-foreground/70"
                fontSize="9"
                fontFamily="JetBrains Mono, monospace"
              >
                {p.band.from < 1000 ? `${p.band.from}` : `${(p.band.from / 1000).toFixed(1)}k`}
              </text>
              <text
                x={p.x}
                y={p.y - 8}
                textAnchor="middle"
                fill={hot ? "var(--color-meter-high)" : "var(--color-primary)"}
                fontSize="9"
                fontFamily="JetBrains Mono, monospace"
              >
                {p.gain > 0 ? `+${p.gain.toFixed(1)}` : p.gain.toFixed(1)}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
}
