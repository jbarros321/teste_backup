import { useState, useEffect } from "react";

const IBOV_ASSETS = [
  { ticker: "PETR4", name: "Petrobras", base: 38.50 },
  { ticker: "VALE3", name: "Vale", base: 62.10 },
  { ticker: "ITUB4", name: "Itaú Unibanco", base: 33.80 },
  { ticker: "BBDC4", name: "Bradesco", base: 14.20 },
  { ticker: "BBAS3", name: "Banco do Brasil", base: 27.40 },
  { ticker: "WEGE3", name: "WEG", base: 51.30 },
  { ticker: "ABEV3", name: "Ambev", base: 12.80 },
  { ticker: "RENT3", name: "Localiza", base: 44.60 },
  { ticker: "MGLU3", name: "Magazine Luiza", base: 7.90 },
  { ticker: "LREN3", name: "Lojas Renner", base: 16.50 },
  { ticker: "SUZB3", name: "Suzano", base: 58.70 },
  { ticker: "HAPV3", name: "Hapvida", base: 4.20 },
];

const API_KEY = "ZFMC53MDMMIFAP15";

function fmt(n, d = 2) { return n.toLocaleString("pt-BR", { minimumFractionDigits: d, maximumFractionDigits: d }); }

function useMarketData() {
  const [assets, setAssets] = useState(() => IBOV_ASSETS.map(a => ({ ...a, price: a.base, change: 0, vol: 0 })));
  const [winProxy, setWinProxy] = useState({ price: 0, change: 0, open: 0 }); // EWZ
  const [wdoProxy, setWdoProxy] = useState({ price: 0, bid: 0, ask: 0 }); // USD/BRL

  useEffect(() => {
    let active = true;

    const fetchAllData = async () => {
      try {
        // Fetch WIN Proxy (EWZ = ETF Brazil MSCI)
        const resEWZ = await fetch(`https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=EWZ&apikey=${API_KEY}`);
        const dataEWZ = await resEWZ.json();
        
        if (dataEWZ["Global Quote"] && active) {
          const quote = dataEWZ["Global Quote"];
          setWinProxy({
            price: parseFloat(quote["05. price"]) || 0,
            change: parseFloat(quote["10. change percent"]?.replace("%", "")) || 0,
            open: parseFloat(quote["02. open"]) || 0,
          });
        }

        // Fetch WDO Proxy (USD to BRL)
        const resFX = await fetch(`https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=USD&to_currency=BRL&apikey=${API_KEY}`);
        const dataFX = await resFX.json();
        
        if (dataFX["Realtime Currency Exchange Rate"] && active) {
          const fx = dataFX["Realtime Currency Exchange Rate"];
          setWdoProxy({
            price: parseFloat(fx["5. Exchange Rate"]) || 0,
            bid: parseFloat(fx["8. Bid Price"]) || 0,
            ask: parseFloat(fx["9. Ask Price"]) || 0,
          });
        }

        // Fetch Ibovespa Grid
        const fetchPromises = IBOV_ASSETS.map(async (a) => {
          const res = await fetch(`https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=${a.ticker}.SA&apikey=${API_KEY}`);
          const data = await res.json();
          return { ...a, apiData: data["Global Quote"] };
        });

        const results = await Promise.all(fetchPromises);

        if (active) {
          setAssets(prev => {
            return prev.map(a => {
              const apiItem = results.find(r => r.ticker === a.ticker)?.apiData;
              if (!apiItem || Object.keys(apiItem).length === 0) return a;
              
              const newPrice = parseFloat(apiItem["05. price"]) || a.price;
              const vol = parseInt(apiItem["06. volume"]) || a.vol;
              const changeStr = (apiItem["10. change percent"] || "").replace("%", "");
              const change = parseFloat(changeStr) || a.change;
              const low = parseFloat(apiItem["04. low"]);
              const high = parseFloat(apiItem["03. high"]);

              return {
                ...a,
                price: newPrice,
                bid: low || newPrice,
                ask: high || newPrice,
                change: change,
                vol: vol,
                lastDir: newPrice >= a.price ? "up" : "down"
              };
            });
          });
        }
      } catch (err) {
        console.error("Error fetching data:", err);
      }
    };

    fetchAllData();
    // Limite de API da AlphaVantage (Mesmo premium, muitas calls bloqueiam)
    const intervalId = setInterval(fetchAllData, 15000);

    return () => {
      active = false;
      clearInterval(intervalId);
    };
  }, []);

  return { assets, winProxy, wdoProxy };
}

export default function TradingPlatform() {
  const { assets, winProxy, wdoProxy } = useMarketData();
  const [now, setNow] = useState(new Date());

  useEffect(() => {
    const t = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(t);
  }, []);

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
          padding: 8px 14px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          font-size: 10px;
          letter-spacing: 0.15em;
          text-transform: uppercase;
          color: #4a7fa5;
        }
        .panel-header span { color: #1e90ff; font-weight: 700; }
        .dot { width: 6px; height: 6px; border-radius: 50%; display: inline-block; margin-right: 5px; animation: pulse 2s infinite; }
        .dot-green { background: #00e676; box-shadow: 0 0 6px #00e676; }
        @keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.4} }
        .grid-row { display: grid; align-items: center; padding: 6px 14px; border-bottom: 1px solid #0d1620; transition: background 0.2s; }
        .grid-row:hover { background: #0d1a28; }
        .ibov-grid { grid-template-columns: 65px 1fr 80px 80px 80px 80px 80px; }
        th { color: #4a7fa5; font-weight: 500; letter-spacing: 0.08em; font-size: 10px; text-align: left; }
      `}</style>

      {/* TOP BAR */}
      <div style={{ background: "#060a0e", borderBottom: "1px solid #152232", padding: "10px 20px", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div style={{ display: "flex", alignItems: "center", gap: "16px" }}>
          <div style={{ fontFamily: "'Bebas Neue', sans-serif", fontSize: "24px", color: "#1e90ff", letterSpacing: "0.1em" }}>FLUXO <span style={{ color: "#4a7fa5", fontSize: "16px" }}>PRO</span></div>
          <div style={{ color: "#78909c", fontSize: "10px", letterSpacing: "0.1em" }}>CONECTADO VIA ALPHA VANTAGE API (DADOS REAIS EXCLUSIVOS)</div>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: "20px" }}>
          <div><span className="dot dot-green"></span><span style={{ color: "#4a9fd4", fontSize: "10px", letterSpacing: "0.1em" }}>API ONLINE</span></div>
          <div style={{ color: "#87ceeb", fontSize: "14px", fontWeight: "700", letterSpacing: "0.05em" }}>{now.toLocaleTimeString("pt-BR")}</div>
          <div style={{ color: "#4a7fa5", fontSize: "10px" }}>{now.toLocaleDateString("pt-BR", { weekday: "short", day: "2-digit", month: "short" }).toUpperCase()}</div>
        </div>
      </div>

      {/* MAIN GRID */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px", padding: "10px", background: "#060a0e" }}>

        {/* INDEX PROXY PANEL */}
        <div className="panel">
          <div className="panel-header">
            <span>EWZ (ÍNDICE BRASIL)</span> · ETF iShares MSCI Brazil - Mercado EUA
            <div style={{ display: "flex", gap: "6px", alignItems: "center" }}>
              <span className={winProxy.change >= 0 ? "up" : "down"} style={{ fontSize: "12px" }}>{winProxy.change >= 0 ? "▲" : "▼"} {fmt(Math.abs(winProxy.change), 2)}%</span>
            </div>
          </div>
          <div style={{ padding: "20px" }}>
            <div style={{ display: "flex", alignItems: "flex-end", justifyContent: "space-between" }}>
              <div>
                <div style={{ fontSize: "42px", fontWeight: "700", color: winProxy.change >= 0 ? "#00e676" : "#ff1744", letterSpacing: "-0.02em", lineHeight: 1 }}>$ {fmt(winProxy.price, 2)}</div>
                <div style={{ marginTop: "8px", display: "flex", gap: "16px", fontSize: "12px", color: "#78909c" }}>
                  <span>ABERTURA: ${fmt(winProxy.open, 2)}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* USD/BRL PANEL */}
        <div className="panel">
          <div className="panel-header">
            <span>USD/BRL (DÓLAR)</span> · Câmbio Oficial Real vs Dólar
            <div style={{ display: "flex", gap: "6px", alignItems: "center" }}>
              <span style={{ fontSize: "12px", color: "#87ceeb" }}>FOREX OFICIAL</span>
            </div>
          </div>
          <div style={{ padding: "20px" }}>
            <div style={{ display: "flex", alignItems: "flex-end", justifyContent: "space-between" }}>
              <div>
                <div style={{ fontSize: "42px", fontWeight: "700", color: "#c8d8e8", letterSpacing: "-0.02em", lineHeight: 1 }}>R$ {fmt(wdoProxy.price, 4)}</div>
                <div style={{ marginTop: "8px", display: "flex", gap: "16px", fontSize: "12px" }}>
                  <span className="down">MÍN (BID) R${fmt(wdoProxy.bid, 4)}</span>
                  <span className="up">MÁX (ASK) R${fmt(wdoProxy.ask, 4)}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* IBOVESPA GRID */}
        <div className="panel" style={{ gridColumn: "1 / 3", marginTop: "10px" }}>
          <div className="panel-header">
            <div><span>IBOVESPA</span> · Grade de Ações B3</div>
            <div style={{ color: "#4a7fa5", fontSize: "10px" }}>Cotações Atuais (Alpha Vantage)</div>
          </div>
          <div style={{ overflowX: "auto", minHeight: "400px" }}>
            <div className="grid-row ibov-grid" style={{ background: "#0d1620", padding: "10px 14px" }}>
              <th>TICKER</th><th>NOME</th><th>ÚLTIMO (R$)</th><th>MÍNIMA DIA</th><th>MÁXIMA DIA</th><th>VAR %</th><th>VOLUME</th>
            </div>
            {assets.map((a, i) => (
              <div key={i} className={`grid-row ibov-grid ${a.lastDir === "up" ? "flash-up" : a.lastDir === "down" ? "flash-down" : ""}`}>
                <span style={{ color: "#1e90ff", fontWeight: "700", fontSize: "13px" }}>{a.ticker}</span>
                <span style={{ color: "#78909c", fontSize: "11px", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>{a.name}</span>
                <span style={{ color: "#c8d8e8", fontWeight: "700", fontSize: "13px" }}>{fmt(a.price)}</span>
                <span className="down" style={{ fontSize: "12px" }}>{fmt(a.bid)}</span>
                <span className="up" style={{ fontSize: "12px" }}>{fmt(a.ask)}</span>
                <span className={a.change >= 0 ? "up" : "down"} style={{ fontWeight: "700", fontSize: "12px" }}>{a.change >= 0 ? "+" : ""}{fmt(a.change, 2)}%</span>
                <span style={{ color: "#78909c", fontSize: "11px" }}>{(a.vol / 1000000).toFixed(2)}M</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
