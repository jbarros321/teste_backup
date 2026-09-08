import { ArrowLeft, FileUp, Save } from 'lucide-react'
import { useMemo, useRef, useState, type ChangeEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'

import { Alert } from '@/components/ui/alert'
import { Button, buttonVariants } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { MetadataSearch } from '@/features/import/MetadataSearch'
import { ChordRenderer } from '@/features/songs/ChordRenderer'
import { SongWarnings, validateSong } from '@/features/songs/SongValidation'
import { useIsDemo, useSaveSong } from '@/features/songs/songQueries'
import { serializeChordPro } from '@/lib/chordpro/serializer'
import { importSongText } from '@/lib/import/parseSongText'

const FORMAT_LABELS: Record<string, string> = {
  chordpro: 'ChordPro',
  chords_above: 'Cifra tradicional (acordes acima da letra)',
  lyrics_only: 'Somente letra',
  tab: 'Tablatura',
}

/**
 * Importação por texto colado (§55 do escopo).
 *
 * O usuário cola o que ele já tem — de um arquivo, de um e-mail, do caderno
 * digitado — e revisa antes de salvar. Nada é buscado ou copiado
 * automaticamente da web.
 */
export default function ImportPage() {
  const navigate = useNavigate()
  const isDemo = useIsDemo()
  const save = useSaveSong()
  const fileInput = useRef<HTMLInputElement>(null)

  const [text, setText] = useState('')
  const [title, setTitle] = useState('')
  const [artist, setArtist] = useState('')
  const [error, setError] = useState<string | null>(null)

  const result = useMemo(() => (text.trim() ? importSongText(text) : null), [text])
  const validation = useMemo(
    () => (result ? validateSong(result.document) : []),
    [result],
  )

  // Título e artista da tela vencem o que foi deduzido do texto.
  const effectiveTitle = title.trim() || result?.document.title?.trim() || ''
  const effectiveArtist = artist.trim() || result?.document.artist?.trim() || ''

  if (isDemo) {
    return (
      <section className="space-y-4 py-8">
        <h1 className="text-2xl font-semibold tracking-tight">Importação indisponível</h1>
        <Alert>
          A importação precisa do Supabase configurado. Siga o <code>SETUP.md</code>.
        </Alert>
        <Link to="/" className={buttonVariants({ variant: 'outline' })}>
          Voltar
        </Link>
      </section>
    )
  }

  function handleFile(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (!file) return

    setError(null)
    file
      .text()
      .then(setText)
      .catch(() => setError('Não foi possível ler o arquivo.'))
  }

  async function handleSave() {
    if (!result) return
    setError(null)

    try {
      const document = { ...result.document }
      if (title.trim()) document.title = title.trim()
      if (artist.trim()) document.artist = artist.trim()

      const song = await save.mutateAsync({
        chordpro: serializeChordPro(document),
        artistName: effectiveArtist || undefined,
      })
      navigate(`/musica/${song.slug}`, { replace: true })
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Não foi possível salvar.')
    }
  }

  return (
    <div className="space-y-5 pb-16">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Link
          to="/"
          className={`${buttonVariants({ variant: 'ghost', size: 'sm' })} -ml-2 text-muted-foreground`}
        >
          <ArrowLeft aria-hidden />
          Voltar
        </Link>

        <Button
          type="button"
          onClick={() => void handleSave()}
          disabled={!result || save.isPending || effectiveTitle.length === 0}
        >
          <Save aria-hidden />
          {save.isPending ? 'Salvando…' : 'Salvar música'}
        </Button>
      </div>

      <div>
        <h1 className="text-2xl font-semibold tracking-tight">Importar música</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Cole a cifra ou envie um arquivo de texto. Identificamos o formato, os acordes, as seções
          e o tom — e mostramos tudo para você conferir antes de salvar.
        </p>
      </div>

      {error && <Alert variant="destructive">{error}</Alert>}

      <div className="grid gap-5 lg:grid-cols-2">
        <div className="space-y-4">
          <div className="space-y-1.5">
            <div className="flex items-center justify-between">
              <Label htmlFor="import-text">Cifra</Label>
              <Button type="button" variant="outline" size="sm" onClick={() => fileInput.current?.click()}>
                <FileUp aria-hidden />
                Arquivo .txt
              </Button>
              <input
                ref={fileInput}
                type="file"
                accept=".txt,.cho,.crd,.chordpro,.pro,text/plain"
                className="hidden"
                onChange={handleFile}
              />
            </div>
            <Textarea
              id="import-text"
              className="h-72 font-mono text-[13px] leading-relaxed"
              spellCheck={false}
              placeholder={'Sublime Graça\nTom: G\n\n   G        G7        C\nSublime graça do Senhor'}
              value={text}
              onChange={(event) => setText(event.target.value)}
            />
          </div>

          <MetadataSearch
            onPick={(metadata) => {
              setTitle(metadata.title)
              if (metadata.artist) setArtist(metadata.artist)
            }}
          />
        </div>

        <div className="space-y-4">
          {result === null ? (
            <p className="rounded-md border border-dashed border-border p-8 text-center text-sm text-muted-foreground">
              Cole a cifra ao lado para ver a prévia.
            </p>
          ) : (
            <>
              <p className="text-sm text-muted-foreground">
                Formato detectado: <strong>{FORMAT_LABELS[result.format]}</strong>
              </p>

              <div className="grid gap-3 sm:grid-cols-2">
                <div className="space-y-1.5">
                  <Label htmlFor="import-title">Título</Label>
                  <Input
                    id="import-title"
                    value={effectiveTitle}
                    onChange={(event) => setTitle(event.target.value)}
                  />
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="import-artist">Artista</Label>
                  <Input
                    id="import-artist"
                    value={effectiveArtist}
                    onChange={(event) => setArtist(event.target.value)}
                  />
                </div>
              </div>

              <dl className="flex flex-wrap gap-x-6 gap-y-1 text-sm text-muted-foreground">
                <div className="flex gap-1">
                  <dt>Tom</dt>
                  <dd className="font-medium text-foreground">{result.document.key ?? '—'}</dd>
                </div>
                <div className="flex gap-1">
                  <dt>BPM</dt>
                  <dd className="font-medium text-foreground">{result.document.bpm ?? '—'}</dd>
                </div>
                <div className="flex gap-1">
                  <dt>Compasso</dt>
                  <dd className="font-medium text-foreground">
                    {result.document.timeSignature ?? '—'}
                  </dd>
                </div>
              </dl>

              <SongWarnings warnings={[...result.warnings.map((message) => ({ message })), ...validation]} />

              <div className="max-h-[24rem] overflow-auto rounded-md border border-border p-4">
                <ChordRenderer song={result.document} />
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
