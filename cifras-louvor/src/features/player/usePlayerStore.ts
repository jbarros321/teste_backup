import { create } from 'zustand'

/**
 * Estado de execução (§60 do escopo).
 *
 * Só o que precisa ser visto de fora da tela da música. O tom e o BPM vivem
 * junto da música que está aberta; o modo palco precisa estar aqui porque o
 * tema do documento inteiro depende dele.
 */
interface PlayerState {
  stageMode: boolean
  setStageMode: (stage: boolean) => void
  toggleStageMode: () => void
}

export const usePlayerStore = create<PlayerState>((set) => ({
  stageMode: false,
  setStageMode: (stageMode) => set({ stageMode }),
  toggleStageMode: () => set((state) => ({ stageMode: !state.stageMode })),
}))
