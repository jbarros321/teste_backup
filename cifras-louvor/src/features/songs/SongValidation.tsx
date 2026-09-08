import { Alert } from '@/components/ui/alert'
import { collectChords, type SongDocument } from '@/lib/chordpro/types'
import { resolveSongKey } from '@/lib/chordpro/transposeSong'
import { isChord } from '@/lib/music/chord'

export interface SongWarning {
  message: string
}

/**
 * Validação musical (§51 do escopo).
 *
 * São AVISOS, nunca bloqueios: uma cifra que a gente não entendeu direito
 * continua sendo uma cifra que o músico quer salvar.
 */
export function validateSong(document: SongDocument): SongWarning[] {
  const warnings: SongWarning[] = []

  const chords = collectChords(document)
  const invalid = [...new Set(chords.filter((chord) => !isChord(chord)))]
  if (invalid.length > 0) {
    warnings.push({
      message: `Não reconhecemos ${invalid.length === 1 ? 'o acorde' : 'os acordes'} ${invalid
        .map((chord) => `"${chord}"`)
        .join(', ')}. Eles serão salvos como estão, mas não serão transpostos.`,
    })
  }

  if (chords.length === 0) {
    warnings.push({ message: 'A música não tem nenhum acorde — só a letra será exibida.' })
  }

  const { source } = resolveSongKey(document)
  if (source === 'detected') {
    warnings.push({
      message: 'O tom não foi declarado. Deduzimos pelos acordes — confira antes de transpor.',
    })
  } else if (source === 'unknown') {
    warnings.push({ message: 'Não foi possível determinar o tom. A transposição ficará indisponível.' })
  }

  if (document.bpm !== undefined && (document.bpm < 20 || document.bpm > 400)) {
    warnings.push({ message: `BPM fora do razoável (${document.bpm}).` })
  }

  if (document.timeSignature && !/^\d{1,2}\/\d{1,2}$/.test(document.timeSignature)) {
    warnings.push({ message: `Compasso "${document.timeSignature}" não parece válido.` })
  }

  const emptySections = document.sections.filter((section) =>
    section.lines.every((line) => line.kind === 'empty'),
  )
  if (emptySections.length > 0) {
    warnings.push({ message: `${emptySections.length} seção(ões) sem conteúdo.` })
  }

  return warnings
}

export function SongWarnings({ warnings }: { warnings: SongWarning[] }) {
  if (warnings.length === 0) return null

  return (
    <Alert>
      <ul className="space-y-1">
        {warnings.map((warning, index) => (
          <li key={index}>{warning.message}</li>
        ))}
      </ul>
    </Alert>
  )
}
