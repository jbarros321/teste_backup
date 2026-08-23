// Integração com a API do Google Gemini (Generative Language API)

export const GEMINI_KEY_STORAGE = 'uiflux_gemini_key';
export const GEMINI_MODEL_STORAGE = 'uiflux_gemini_model';

// Modelos disponíveis (família 2.5 pra cima)
export const GEMINI_MODELS: { id: string; label: string }[] = [
  { id: 'gemini-2.5-pro', label: 'Gemini 2.5 Pro — mais inteligente' },
  { id: 'gemini-2.5-flash', label: 'Gemini 2.5 Flash — equilibrado (recomendado)' },
  { id: 'gemini-2.5-flash-lite', label: 'Gemini 2.5 Flash-Lite — mais rápido e barato' },
];

export const DEFAULT_GEMINI_MODEL = 'gemini-2.5-flash';

export const getGeminiKey = (): string => (localStorage.getItem(GEMINI_KEY_STORAGE) || '').trim();
export const getGeminiModel = (): string => localStorage.getItem(GEMINI_MODEL_STORAGE) || DEFAULT_GEMINI_MODEL;

/**
 * Chama o Gemini e devolve o texto da resposta.
 * @param jsonMode quando true, pede à API para responder em JSON válido.
 */
export async function callGemini(
  apiKey: string,
  model: string,
  systemText: string,
  userText: string,
  jsonMode = false
): Promise<string> {
  const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${encodeURIComponent(apiKey)}`;

  const body: any = {
    contents: [{ role: 'user', parts: [{ text: userText }] }],
    generationConfig: { temperature: 0.3 },
  };
  if (systemText) {
    body.system_instruction = { parts: [{ text: systemText }] };
  }
  if (jsonMode) {
    body.generationConfig.responseMimeType = 'application/json';
  }

  const resp = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });

  const data = await resp.json();
  if (data.error) {
    throw new Error(data.error.message || 'Erro na API do Gemini');
  }

  const parts = data.candidates?.[0]?.content?.parts;
  const text = Array.isArray(parts) ? parts.map((p: any) => p.text || '').join('') : '';
  return text;
}
