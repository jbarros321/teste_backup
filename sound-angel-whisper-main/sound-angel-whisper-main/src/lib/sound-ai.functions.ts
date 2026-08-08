import { createServerFn } from "@tanstack/react-start";
import { z } from "zod";

const BandSchema = z.object({
  name: z.string(),
  from: z.number(),
  to: z.number(),
  db: z.number(),
});

const InputSchema = z.object({
  source: z.string().min(1).max(40),
  bands: z.array(BandSchema).min(1).max(16),
  rms: z.number(),
  peak: z.number(),
});

export const analyzeSound = createServerFn({ method: "POST" })
  .inputValidator((data: unknown) => InputSchema.parse(data))
  .handler(async ({ data }) => {
    const geminiApiKey = process.env.GEMINI_API_KEY;
    const lovableApiKey = process.env.LOVABLE_API_KEY;

    if (!geminiApiKey && !lovableApiKey) {
      return { 
        recommendations: "Chave de IA não configurada. Por favor, adicione GEMINI_API_KEY no seu arquivo .env na raiz do projeto.", 
        error: true as const 
      };
    }

    const bandsText = data.bands
      .map((b) => `${b.name} (${b.from}-${b.to}Hz): ${b.db.toFixed(1)} dB`)
      .join("\n");

    const rmsDb = 20 * Math.log10(Math.max(data.rms, 1e-6));
    const peakDb = 20 * Math.log10(Math.max(data.peak, 1e-6));

    const systemPrompt = `Você é um engenheiro de som experiente, especialista em mixagem ao vivo para igrejas (PA, monitores/retornos, vocais e instrumentos). Responda SEMPRE em português brasileiro, de forma direta e prática. Dê recomendações concretas de EQ (Hz e dB), ganho, compressão e retorno. Use no máximo 8 bullets curtos. Sem introduções longas.`;

    const userPrompt = `Fonte sendo analisada: ${data.source}

Níveis medidos:
- RMS: ${rmsDb.toFixed(1)} dB FS
- Pico: ${peakDb.toFixed(1)} dB FS

Energia por banda de frequência (relativa, em dB FS):
${bandsText}

Diga:
1. Diagnóstico em 1 frase (ex: "vocal abafado", "guitarra com excesso de médio-baixo").
2. Ajustes específicos de EQ (corte/realce em Hz e dB).
3. Ajuste de ganho se necessário.
4. Dica para o retorno/monitor desta fonte.`;

    // 1. Usar o Gemini diretamente se GEMINI_API_KEY estiver configurado
    if (geminiApiKey) {
      const modelsToTry = ["gemini-2.5-flash", "gemini-1.5-flash", "gemini-2.0-flash"];
      let lastErrorMsg = "";

      for (const model of modelsToTry) {
        try {
          const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${geminiApiKey}`;
          const res = await fetch(url, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              contents: [
                {
                  role: "user",
                  parts: [
                    { text: userPrompt }
                  ]
                }
              ],
              systemInstruction: {
                parts: [
                  { text: systemPrompt }
                ]
              }
            }),
          });

          if (!res.ok) {
            const t = await res.text();
            console.warn(`Model ${model} failed with status ${res.status}:`, t);
            lastErrorMsg = `Model ${model} returned status ${res.status}`;
            continue;
          }

          const json = await res.json() as {
            candidates?: Array<{
              content?: {
                parts?: Array<{ text?: string }>;
              };
            }>;
          };

          const text = json.candidates?.[0]?.content?.parts?.[0]?.text ?? "Sem resposta do Gemini.";
          return { recommendations: text, error: false as const };
        } catch (e) {
          console.error(`Error attempting model ${model}:`, e);
          lastErrorMsg = e instanceof Error ? e.message : String(e);
        }
      }

      return { 
        recommendations: `Não foi possível acessar nenhum modelo do Gemini (tentados: ${modelsToTry.join(", ")}). Último erro obtido: ${lastErrorMsg}. Verifique se a sua chave do Gemini está ativa e com acesso a esses modelos no Google AI Studio.`, 
        error: true as const 
      };
    }

    // 2. Usar o Lovable AI Gateway como fallback
    try {
      const res = await fetch("https://ai.gateway.lovable.dev/v1/chat/completions", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${lovableApiKey}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          model: "google/gemini-3-flash-preview",
          messages: [
            { role: "system", content: systemPrompt },
            { role: "user", content: userPrompt },
          ],
        }),
      });

      if (res.status === 429) {
        return { recommendations: "Muitas requisições — aguarde alguns segundos.", error: true as const };
      }
      if (res.status === 402) {
        return { recommendations: "Créditos de IA esgotados. Adicione créditos no workspace.", error: true as const };
      }
      if (!res.ok) {
        const t = await res.text();
        console.error("AI gateway error:", res.status, t);
        return { recommendations: "Falha ao consultar a IA.", error: true as const };
      }
      const json = await res.json();
      const text: string = json.choices?.[0]?.message?.content ?? "Sem resposta.";
      return { recommendations: text, error: false as const };
    } catch (e) {
      console.error("analyzeSound error", e);
      return { recommendations: "Erro de rede ao consultar a IA.", error: true as const };
    }
  });
