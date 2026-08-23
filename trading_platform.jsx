import { useState, useEffect, useRef, useCallback } from "react";

const IBOV_ASSETS = [
  { ticker: "PETR4", name: "Petrobras", base: 38.50, lot: 100 },
  { ticker: "VALE3", name: "Vale", base: 62.10, lot: 100 },
  { ticker: "ITUB4", name: "Itaú Unibanco", base: 33.80, lot: 100 },
  { ticker: "BBDC4", name: "Bradesco", base: 14.20, lot: 100 },
  { ticker: "BBAS3", name: "Banco do Brasil", base: 27.40, lot: 100 },
  { ticker: "WEGE3", name: "WEG", base: 51.30, lot: 100 },
  { ticker: "ABEV3", name: "Ambev", base: 12.80, lot: 100 },
  { ticker: "RENT3", name: "Localiza", base: 44.60, lot: 100 },
  { ticker: "MGLU3", name: "Magazine Luiza", base: 7.90, lot: 100 },
  { ticker: "LREN3", name: "Lojas Renner", base: 16.50, lot: 100 },
  { ticker: "SUZB3", name: "Suzano", base: 58.70, lot: 100 },
  { ticker: "HAPV3", name: "Hapvida", base: 4.20, lot: 100 },
];

const DI_FUTURES = [
  { venc: "Jan/26", code: "DI1F26", base: 13.42 },
  { venc: "Jul/26", code: "DI1N26", base: 13.18 },
  { venc: "Jan/27", code: "DI1F27", base: 12.95 },
  { venc: "Jan/28", code: "DI1F28", base: 12.61 },
  { venc: "Jan/29", code: "DI1F29", base: 12.40 },
  { venc: "Jan/30", code: "DI1F30", base: 12.28 },
  { venc: "Jan/31", code: "DI1F31", base: 12.19 },
];

const WIN_BASE = 132450;
const WDO_BASE = 5.847;

function rnd(min, max) { return Math.random() * (max - min) + min; }
function rndInt(min, max) { return Math.floor(rnd(min, max)); }
function fmt(n, d = 2) { return n.toLocaleString("pt-BR", { minimumFractionDigits: d, maximumFractionDigits: d }); }

function useMarketData() {
  const [assets, setAssets] = useState(() =>
    IBOV_ASSETS.map(a => ({
      ...a,
      price: a.base,
      bid: a.base - rnd(0.01, 0.05),
      ask: a.base + rnd(0.01, 0.05),
      change: rnd(-3, 3),
      vol: rndInt(1000000, 50000000),
      buyPressure: rnd(30, 70),
      lastDir: null,
    }))
  );

  const [di, setDi] = useState(() =>
    DI_FUTURES.map(d => ({
      ...d,
      rate: d.base,
      pu: 100000 / (1 + d.base / 100) ** 0.5,
      change: rnd(-0.05, 0.05),
      buyVol: rndInt(500, 5000),
      sellVol: rndInt(500, 5000),
    }))
  );

  const [win, setWin] = useState({
    price: WIN_BASE, bid: WIN_BASE - 5, ask: WIN_BASE + 5,
    change: rnd(-0.8, 0.8), buyAgg: 0, sellAgg: 0,
    buyVol: rndInt(5000, 30000), sellVol: rndInt(5000, 30000),
  });

  const [wdo, setWdo] = useState({
    price: WDO_BASE, bid: WDO_BASE - 0.001, ask: WDO_BASE + 0.001,
    change: rnd(-0.5, 0.5), buyAgg: 0, sellAgg: 0,
    buyVol: rndInt(3000, 20000), sellVol: rndInt(3000, 20000),
  });

  const [tape, setTape] = useState(() =>
    Array.from({ length: 20 }, (_, i) => ({
      id: i,
      time: new Date(Date.now() - i * 3000).toLocaleTimeString("pt-BR"),
      asset: ["WIN", "WDO"][rndInt(0, 2)],
      side: Math.random() > 0.5 ? "C" : "V",
      qty: rndInt(1, 20),
      price: Math.random() > 0.5 ? WIN_BASE + rndInt(-50, 50) : WDO_BASE + rnd(-0.01, 0.01),
    }))
  );

  // Fetch real data from brAPI
  useEffect(() => {
    let active = true;
    const fetchApiData = async () => {
      try {
        const tickers = IBOV_ASSETS.map(a => a.ticker).join("%2C");
        const res = await fetch(`https://brapi.dev/api/quote/${tickers}`);
        const data = await res.json();
        
        if (data && data.results && active) {
          setAssets(prev => {
            return prev.map(a => {
              const apiItem = data.results.find(r => r.symbol === a.ticker);
              if (!apiItem) return a;
              
              const newPrice = apiItem.regularMarketPrice || a.price;
              const delta = newPrice - a.price;
              const spread = rnd(0.01, 0.04);
              const vol = apiItem.regularMarketVolume || a.vol;
              const change = apiItem.regularMarketChangePercent || a.change;
              
              return {
                ...a,
                price: newPrice,
                bid: apiItem.regularMarketDayLow || newPrice - spread,
                ask: apiItem.regularMarketDayHigh || newPrice + spread,
                change: change,
                vol: vol,
                buyPressure: Math.min(95, Math.max(5, a.buyPressure + rnd(-10, 10))),
                lastDir: delta > 0 ? "up" : delta < 0 ? "down" : a.lastDir,
              };
            });
          });
        }
      } catch (err) {
        console.error("Error fetching brAPI data:", err);
      }
    };

    // Fetch immediately
    fetchApiData();
    // Poll every 10 seconds for real data (brAPI limits rate)
    const apiInterval = setInterval(fetchApiData, 10000);

    return () => {
      active = false;
      clearInterval(apiInterval);
    };
  }, []);

  // Fast interval for simulated data only
  useEffect(() => {
    const interval = setInterval(() => {
      setDi(prev => prev.map(d => {
        const dr = rnd(-0.01, 0.01);
        const newRate = d.rate + dr;
        return {
          ...d,
          rate: newRate,
          pu: 100000 / (1 + newRate / 100) ** 0.5,
          change: d.change + dr,
          buyVol: d.buyVol + rndInt(0, 200),
          sellVol: d.sellVol + rndInt(0, 200),
        };
      }));

      setWin(prev => {
        const delta = rndInt(-25, 25);
        const newPrice = prev.price + delta;
        const buyAgg = rndInt(10, 200);
        const sellAgg = rndInt(10, 200);
        return {
          ...prev,
          price: newPrice,
          bid: newPrice - 5,
          ask: newPrice + 5,
          change: prev.change + rnd(-0.03, 0.03),
          buyAgg, sellAgg,
          buyVol: prev.buyVol + buyAgg,
          sellVol: prev.sellVol + sellAgg,
        };
      });

      setWdo(prev => {
        const delta = rnd(-0.003, 0.003);
        const newPrice = prev.price + delta;
        const buyAgg = rndInt(5, 100);
        const sellAgg = rndInt(5, 100);
        return {
          ...prev,
          price: newPrice,
          bid: newPrice - 0.001,
          ask: newPrice + 0.001,
          change: prev.change + rnd(-0.02, 0.02),
          buyAgg, sellAgg,
          buyVol: prev.buyVol + buyAgg,
          sellVol: prev.sellVol + sellAgg,
        };
      });

      setTape(prev => {
        const isWin = Math.random() > 0.4;
        const newEntry = {
          id: Date.now(),
          time: new Date().toLocaleTimeString("pt-BR"),
          asset: isWin ? "WIN" : "WDO",
          side: Math.random() > 0.5 ? "C" : "V",
          qty: rndInt(1, 30),
          price: isWin ? WIN_BASE + rndInt(-80, 80) : WDO_BASE + rnd(-0.015, 0.015),
        };
        return [newEntry, ...prev.slice(0, 29)];
      });
    }, 800);
    return () => clearInterval(interval);
  }, []);

  return { assets, di, win, wdo, tape };
}

export default function TradingPlatform() {
  const { assets, di, win, wdo, tape } = useMarketData();
  const [now, setNow] = useState(new Date());

  useEffect(() => {
    const t = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(t);
  }, []);

  const winBuyPct = win.buyVol / (win.buyVol + win.sellVol) * 100;
  const wdoBuyPct = wdo.buyVol / (wdo.buyVol + wdo.sellVol) * 100;

  return (
    <div style={{
      fontFamily: "'JetBrains Mono', 'Fira Code', 'Courier New', monospace",
      background: "#080c10",
      color: "#c8d8e8",
      minHeight: "100vh",
      padding: "0",
      fontSize: "11px",
    }}>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@300;400;500;600;700&family=Bebas+Neue&display=swap');
        * { box-sizing: border-box; margin: 0; padding: 0; }
        ::-webkit-scrollbar { width: 4px; height: 4px; }
        ::-webkit-scrollbar-track { background: #0d1117; }
        ::-webkit-scrollbar-thumb { background: #1e3a5f; border-radius: 2px; }
        .up { color: #00e676; }
        .down { color: #ff1744; }
        .neutral { color: #78909c; }
        .flash-up { animation: flashUp 0.4s ease; }
        .flash-down { animation: flashDown 0.4s ease; }
        @keyframes flashUp { 0%,100%{background:transparent} 50%{background:rgba(0,230,118,0.15)} }
        @keyframes flashDown { 0%,100%{background:transparent} 50%{background:rgba(255,23,68,0.12)} }
        .panel {
          background: #0a0f14;
          border: 1px solid #152232;
          border-radius: 2px;
        }
        .panel-header {
          background: #0d1620;
          border-bottom: 1px solid #152232;
          padding: 5px 10px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          font-size: 9px;
          letter-spacing: 0.15em;
          text-transform: uppercase;
          color: #4a7fa5;
        }
        .panel-header span { color: #1e90ff; font-weight: 700; }
        .bar-container {
          height: 4px;
          background: #152232;
          border-radius: 2px;
          overflow: hidden;
          margin-top: 3px;
        }
        .bar-buy { height: 100%; background: linear-gradient(90deg, #00c853, #00e676); border-radius: 2px; transition: width 0.4s ease; }
        .buy-chip { background: rgba(0,230,118,0.1); border: 1px solid rgba(0,230,118,0.3); color: #00e676; padding: 1px 6px; border-radius: 2px; font-size: 9px; }
        .sell-chip { background: rgba(255,23,68,0.1); border: 1px solid rgba(255,23,68,0.3); color: #ff1744; padding: 1px 6px; border-radius: 2px; font-size: 9px; }
        .dot { width: 6px; height: 6px; border-radius: 50%; display: inline-block; margin-right: 5px; animation: pulse 2s infinite; }
        .dot-green { background: #00e676; box-shadow: 0 0 6px #00e676; }
        @keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.4} }
        .mini-btn { background: #0d1a28; border: 1px solid #1e3a5f; color: #4a9fd4; padding: 3px 8px; border-radius: 2px; cursor: pointer; font-family: inherit; font-size: 9px; letter-spacing: 0.1em; transition: all 0.15s; }
        .mini-btn:hover { background: #1e3a5f; color: #87ceeb; }
        .grid-row { display: grid; align-items: center; padding: 3px 10px; border-bottom: 1px solid #0d1620; transition: background 0.2s; }
        .grid-row:hover { background: #0d1a28; }
        .ibov-grid { grid-template-columns: 58px 1fr 70px 70px 70px 65px 75px 60px; }
        .di-grid { grid-template-columns: 50px 85px 70px 60px 65px 65px 1fr; }
        th { color: #4a7fa5; font-weight: 500; letter-spacing: 0.08em; font-size: 9px; }
        .agg-block {
          display: flex;
          gap: 3px;
          align-items: center;
          justify-content: flex-end;
        }
        .agg-num { font-size: 10px; font-weight: 700; min-width: 28px; text-align: right; }
      `}</style>

      {/* TOP BAR */}
      <div style={{ background: "#060a0e", borderBottom: "1px solid #152232", padding: "6px 14px", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div style={{ display: "flex", alignItems: "center", gap: "16px" }}>
          <div style={{ fontFamily: "'Bebas Neue', sans-serif", fontSize: "20px", color: "#1e90ff", letterSpacing: "0.1em" }}>FLUXO <span style={{ color: "#4a7fa5", fontSize: "14px" }}>PRO</span></div>
          <div style={{ color: "#78909c", fontSize: "9px", letterSpacing: "0.1em" }}>B3 · SIMULAÇÃO · DADOS ILUSTRATIVOS</div>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: "20px" }}>
          <div><span className="dot dot-green"></span><span style={{ color: "#4a9fd4", fontSize: "9px", letterSpacing: "0.1em" }}>MERCADO ABERTO</span></div>
          <div style={{ color: "#87ceeb", fontSize: "12px", fontWeight: "700", letterSpacing: "0.05em" }}>{now.toLocaleTimeString("pt-BR")}</div>
          <div style={{ color: "#4a7fa5", fontSize: "9px" }}>{now.toLocaleDateString("pt-BR", { weekday: "short", day: "2-digit", month: "short" }).toUpperCase()}</div>
        </div>
      </div>

      {/* MAIN GRID */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 240px", gridTemplateRows: "auto auto 1fr", gap: "1px", padding: "1px", background: "#060a0e" }}>

        {/* WIN PANEL */}
        <div className="panel">
          <div className="panel-header">
            <span>WIN</span> · Mini Índice Futuro
            <div style={{ display: "flex", gap: "6px", alignItems: "center" }}>
              <span className={win.change >= 0 ? "up" : "down"} style={{ fontSize: "10px" }}>{win.change >= 0 ? "▲" : "▼"} {fmt(Math.abs(win.change), 2)}%</span>
            </div>
          </div>
          <div style={{ padding: "10px 14px" }}>
            <div style={{ display: "flex", alignItems: "flex-end", justifyContent: "space-between" }}>
              <div>
                <div style={{ fontSize: "28px", fontWeight: "700", color: win.change >= 0 ? "#00e676" : "#ff1744", letterSpacing: "-0.02em", lineHeight: 1 }}>{win.price.toLocaleString("pt-BR")}</div>
                <div style={{ marginTop: "4px", display: "flex", gap: "12px", fontSize: "10px" }}>
                  <span className="up">C {win.bid.toLocaleString("pt-BR")}</span>
                  <span className="down">V {win.ask.toLocaleString("pt-BR")}</span>
                </div>
              </div>
              <div style={{ textAlign: "right" }}>
                <div style={{ color: "#4a7fa5", fontSize: "9px", marginBottom: "3px" }}>AGRESSÃO</div>
                <div className="agg-block">
                  <span className="buy-chip">C {win.buyAgg}</span>
                  <span className="sell-chip">V {win.sellAgg}</span>
                </div>
              </div>
            </div>

            <div style={{ marginTop: "10px" }}>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "3px", fontSize: "9px" }}>
                <span className="up">COMPRA {fmt(winBuyPct, 1)}%</span>
                <span className="down">VENDA {fmt(100 - winBuyPct, 1)}%</span>
              </div>
              <div style={{ height: "6px", background: "#152232", borderRadius: "3px", overflow: "hidden" }}>
                <div style={{ height: "100%", width: `${winBuyPct}%`, background: "linear-gradient(90deg, #00c853, #00e676)", borderRadius: "3px", transition: "width 0.5s ease" }}></div>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", marginTop: "6px", fontSize: "9px", color: "#4a7fa5" }}>
                <span>VOL C: <span className="up">{win.buyVol.toLocaleString("pt-BR")}</span></span>
                <span>VOL V: <span className="down">{win.sellVol.toLocaleString("pt-BR")}</span></span>
              </div>
            </div>
          </div>
        </div>

        {/* WDO PANEL */}
        <div className="panel">
          <div className="panel-header">
            <span>WDO</span> · Mini Dólar Futuro
            <div style={{ display: "flex", gap: "6px", alignItems: "center" }}>
              <span className={wdo.change >= 0 ? "up" : "down"} style={{ fontSize: "10px" }}>{wdo.change >= 0 ? "▲" : "▼"} {fmt(Math.abs(wdo.change), 2)}%</span>
            </div>
          </div>
          <div style={{ padding: "10px 14px" }}>
            <div style={{ display: "flex", alignItems: "flex-end", justifyContent: "space-between" }}>
              <div>
                <div style={{ fontSize: "28px", fontWeight: "700", color: wdo.change >= 0 ? "#00e676" : "#ff1744", letterSpacing: "-0.02em", lineHeight: 1 }}>R$ {fmt(wdo.price, 3)}</div>
                <div style={{ marginTop: "4px", display: "flex", gap: "12px", fontSize: "10px" }}>
                  <span className="up">C {fmt(wdo.bid, 3)}</span>
                  <span className="down">V {fmt(wdo.ask, 3)}</span>
                </div>
              </div>
              <div style={{ textAlign: "right" }}>
                <div style={{ color: "#4a7fa5", fontSize: "9px", marginBottom: "3px" }}>AGRESSÃO</div>
                <div className="agg-block">
                  <span className="buy-chip">C {wdo.buyAgg}</span>
                  <span className="sell-chip">V {wdo.sellAgg}</span>
                </div>
              </div>
            </div>

            <div style={{ marginTop: "10px" }}>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "3px", fontSize: "9px" }}>
                <span className="up">COMPRA {fmt(wdoBuyPct, 1)}%</span>
                <span className="down">VENDA {fmt(100 - wdoBuyPct, 1)}%</span>
              </div>
              <div style={{ height: "6px", background: "#152232", borderRadius: "3px", overflow: "hidden" }}>
                <div style={{ height: "100%", width: `${wdoBuyPct}%`, background: "linear-gradient(90deg, #00c853, #00e676)", borderRadius: "3px", transition: "width 0.5s ease" }}></div>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", marginTop: "6px", fontSize: "9px", color: "#4a7fa5" }}>
                <span>VOL C: <span className="up">{wdo.buyVol.toLocaleString("pt-BR")}</span></span>
                <span>VOL V: <span className="down">{wdo.sellVol.toLocaleString("pt-BR")}</span></span>
              </div>
            </div>
          </div>
        </div>

        {/* TIME & SALES */}
        <div className="panel" style={{ gridRow: "1 / 3" }}>
          <div className="panel-header"><span>TIME & SALES</span> · Tape</div>
          <div style={{ overflowY: "auto", maxHeight: "280px" }}>
            {tape.map(t => {
              const isWin = t.asset === "WIN";
              const isBuy = t.side === "C";
              const isLarge = t.qty >= 15;
              return (
                <div key={t.id} style={{
                  display: "grid", gridTemplateColumns: "46px 36px 30px 1fr 40px",
                  padding: "3px 10px", borderBottom: "1px solid #0a0f14",
                  background: isLarge ? (isBuy ? "rgba(0,230,118,0.05)" : "rgba(255,23,68,0.05)") : "transparent",
                  alignItems: "center",
                }}>
                  <span style={{ color: "#4a7fa5", fontSize: "9px" }}>{t.time}</span>
                  <span style={{ color: "#87ceeb", fontWeight: "600", fontSize: "9px" }}>{t.asset}</span>
                  <span style={{ color: isBuy ? "#00e676" : "#ff1744", fontWeight: "700", fontSize: "9px" }}>{t.side}</span>
                  <span style={{ color: "#c8d8e8", fontSize: "10px", textAlign: "right" }}>{isWin ? t.price.toLocaleString("pt-BR") : fmt(t.price, 3)}</span>
                  <span style={{
                    color: isBuy ? "#00e676" : "#ff1744",
                    fontWeight: isLarge ? "700" : "400",
                    fontSize: isLarge ? "11px" : "9px",
                    textAlign: "right"
                  }}>{t.qty}</span>
                </div>
              );
            })}
          </div>
        </div>

        {/* DI FUTURES */}
        <div className="panel" style={{ gridColumn: "1 / 3" }}>
          <div className="panel-header"><span>CURVA DE JUROS</span> · DI Futuro · B3</div>
          <div style={{ overflowX: "auto" }}>
            <div className="grid-row di-grid" style={{ background: "#0d1620", padding: "4px 10px" }}>
              <th>VENC</th><th>CONTRATO</th><th>TAXA %</th><th>VAR</th><th>VOL C</th><th>VOL V</th><th>PRESSÃO</th>
            </div>
            {di.map((d, i) => {
              const buyPct = d.buyVol / (d.buyVol + d.sellVol) * 100;
              return (
                <div key={i} className={`grid-row di-grid ${d.change > 0 ? "flash-up" : "flash-down"}`}>
                  <span style={{ color: "#87ceeb", fontWeight: "600" }}>{d.venc}</span>
                  <span style={{ color: "#4a7fa5" }}>{d.code}</span>
                  <span style={{ color: "#c8d8e8", fontWeight: "700" }}>{fmt(d.rate, 2)}</span>
                  <span className={d.change >= 0 ? "up" : "down"}>{d.change >= 0 ? "+" : ""}{fmt(d.change, 3)}</span>
                  <span className="up">{d.buyVol.toLocaleString("pt-BR")}</span>
                  <span className="down">{d.sellVol.toLocaleString("pt-BR")}</span>
                  <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                    <div style={{ flex: 1, height: "5px", background: "#152232", borderRadius: "2px", overflow: "hidden" }}>
                      <div style={{ height: "100%", width: `${buyPct}%`, background: buyPct > 55 ? "#00e676" : buyPct < 45 ? "#ff1744" : "#ffa726", transition: "width 0.5s" }}></div>
                    </div>
                    <span style={{ color: "#4a7fa5", minWidth: "30px", fontSize: "9px" }}>{fmt(buyPct, 0)}%</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* IBOVESPA GRID */}
        <div className="panel" style={{ gridColumn: "1 / 4" }}>
          <div className="panel-header">
            <div><span>IBOVESPA</span> · Grade de Ativos · Fluxo de Ordens</div>
            <div style={{ color: "#4a7fa5", fontSize: "9px" }}>↑ Dados simulados para fins ilustrativos</div>
          </div>
          <div style={{ overflowX: "auto" }}>
            <div className="grid-row ibov-grid" style={{ background: "#0d1620", padding: "4px 10px" }}>
              <th>TICKER</th><th>NOME</th><th>ÚLTIMO</th><th>COMPRA</th><th>VENDA</th><th>VAR %</th><th>VOLUME</th><th>PRESSÃO C/V</th>
            </div>
            {assets.map((a, i) => (
              <div key={i} className={`grid-row ibov-grid ${a.lastDir === "up" ? "flash-up" : a.lastDir === "down" ? "flash-down" : ""}`}>
                <span style={{ color: "#1e90ff", fontWeight: "700" }}>{a.ticker}</span>
                <span style={{ color: "#78909c", fontSize: "9px", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>{a.name}</span>
                <span style={{ color: "#c8d8e8", fontWeight: "600" }}>{fmt(a.price)}</span>
                <span className="up">{fmt(a.bid)}</span>
                <span className="down">{fmt(a.ask)}</span>
                <span className={a.change >= 0 ? "up" : "down"} style={{ fontWeight: "600" }}>{a.change >= 0 ? "+" : ""}{fmt(a.change, 2)}%</span>
                <span style={{ color: "#78909c", fontSize: "9px" }}>{(a.vol / 1000000).toFixed(1)}M</span>
                <div style={{ display: "flex", alignItems: "center", gap: "4px" }}>
                  <div style={{ flex: 1, height: "5px", background: "#152232", borderRadius: "2px", overflow: "hidden" }}>
                    <div style={{ height: "100%", width: `${a.buyPressure}%`, background: a.buyPressure > 60 ? "#00e676" : a.buyPressure < 40 ? "#ff1744" : "#ffa726", transition: "width 0.4s" }}></div>
                  </div>
                  <span style={{ color: "#4a7fa5", minWidth: "28px", fontSize: "9px" }}>{fmt(a.buyPressure, 0)}%</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* FOOTER */}
      <div style={{ background: "#060a0e", borderTop: "1px solid #152232", padding: "4px 14px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div style={{ color: "#263238", fontSize: "9px", letterSpacing: "0.1em" }}>FLUXO PRO · SIMULAÇÃO APENAS · NÃO CONSTITUI RECOMENDAÇÃO DE INVESTIMENTO</div>
        <div style={{ display: "flex", gap: "16px", fontSize: "9px", color: "#37474f" }}>
          <span>WIN: {win.price.toLocaleString("pt-BR")}</span>
          <span>WDO: {fmt(wdo.price, 3)}</span>
          <span>DI JAN/26: {fmt(di[0]?.rate, 2)}%</span>
        </div>
      </div>
    </div>
  );
}
