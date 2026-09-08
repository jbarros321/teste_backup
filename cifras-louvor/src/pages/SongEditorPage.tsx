import { ArrowLeft, Save } from 'lucide-react'
import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'

import { Alert } from '@/components/ui/alert'
import { Button, buttonVariants } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { ChordRenderer } from '@/features/songs/ChordRenderer'
import { SongWarnings, validateSong } from '@/features/songs/SongValidation'
import { useCategories, useIsDemo, useSaveSong, useSong } from '@/features/songs/songQueries'
import { parseChordPro } from '@/lib/chordpro/parser'

const TEMPLATE = `{title: }
{artist: }
{key: G}
{tempo: 80}
{time: 4/4}

{start_of_verse}
[G]Escreva a letra aqui, com os acordes [C]entre colchetes
{end_of_verse}
`

/**
 * Editor ChordPro com preview ao lado (§32 do escopo).
 *
 * O ChordPro é a fonte da verdade — título, tom e BPM saem das diretivas.
 * Os campos acima do editor são atalhos que escrevem nessas mesmas diretivas.
 */
export default function SongEditorPage() {
  const { slug } = useParams()
  const navigate = useNavigate()
  const isDemo = useIsDemo()
  const isEditing = Boolean(slug)

  const { data: existing, isPending } = useSong(slug ?? '')
  const { data: categories } = useCategories()
  const save = useSaveSong()

  const [chordpro, setChordpro] = useState(TEMPLATE)
  const [artistName, setArtistName] = useState('')
  const [categoryIds, setCategoryIds] = useState<string[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (existing) {
      setChordpro(existing.chordpro || TEMPLATE)
      setArtistName(existing.artist ?? '')
    }
  }, [existing])

  const document = useMemo(() => parseChordPro(chordpro), [chordpro])
  const warnings = useMemo(() => validateSong(document), [document])
  const title = document.title?.trim() ?? ''

  if (isDemo) {
    return (
      <section className="space-y-4 py-8">
        <h1 className="text-2xl font-semibold tracking-tight">Cadastro indisponível</h1>
        <Alert>
          O cadastro de músicas precisa do Supabase configurado. Siga o <code>SETUP.md</code> e
          reinicie o servidor.
        </Alert>
        <Link to="/" className={buttonVariants({ variant: 'outline' })}>
          Voltar
        </Link>
      </section>
    )
  }

  if (isEditing && isPending) {
    return (
      <p className="py-16 text-center text-sm text-muted-foreground" role="status">
        Carregando…
      </p>
    )
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)

    try {
      const song = await save.mutateAsync({
        id: existing?.id,
        chordpro,
        artistName: artistName.trim() || undefined,
        categoryIds,
      })
      navigate(`/musica/${song.slug}`, { replace: true })
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Não foi possível salvar.')
    }
  }

  return (
    <form className="space-y-5 pb-16" onSubmit={handleSubmit}>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Link
          to={existing ? `/musica/${existing.slug}` : '/'}
          className={`${buttonVariants({ variant: 'ghost', size: 'sm' })} -ml-2 text-muted-foreground`}
        >
          <ArrowLeft aria-hidden />
          Voltar
        </Link>

        <Button type="submit" disabled={save.isPending || title.length === 0}>
          <Save aria-hidden />
          {save.isPending ? 'Salvando…' : 'Salvar música'}
        </Button>
      </div>

      <h1 className="text-2xl font-semibold tracking-tight">
        {isEditing ? 'Editar cifra' : 'Nova música'}
      </h1>

      <div className="grid gap-4 sm:grid-cols-2">
        <div className="space-y-1.5">
          <Label htmlFor="artist">Artista</Label>
          <Input
            id="artist"
            value={artistName}
            onChange={(event) => setArtistName(event.target.value)}
            placeholder="Quem gravou ou compôs"
          />
        </div>

        {categories && categories.length > 0 && (
          <div className="space-y-1.5">
            <Label htmlFor="categories">Categorias</Label>
            <select
              id="categories"
              multiple
              className="h-24 w-full rounded-md border border-input bg-transparent px-2 py-1 text-sm"
              value={categoryIds}
              onChange={(event) =>
                setCategoryIds([...event.target.selectedOptions].map((option) => option.value))
              }
            >
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      {error && <Alert variant="destructive">{error}</Alert>}
      <SongWarnings warnings={warnings} />

      <div className="grid gap-4 lg:grid-cols-2">
        <div className="space-y-1.5">
          <Label htmlFor="chordpro">Cifra (ChordPro)</Label>
          <Textarea
            id="chordpro"
            className="h-[28rem] font-mono text-[13px] leading-relaxed"
            spellCheck={false}
            value={chordpro}
            onChange={(event) => setChordpro(event.target.value)}
          />
          <p className="text-xs text-muted-foreground">
            Acordes entre colchetes, imediatamente antes da sílaba:{' '}
            <code>[G]Grande é o Se[Em]nhor</code>. Metadados entre chaves:{' '}
            <code>{'{title: ...}'}</code>, <code>{'{key: G}'}</code>, <code>{'{tempo: 80}'}</code>.
          </p>
        </div>

        <div className="space-y-1.5">
          <p className="text-sm font-medium leading-none">Preview</p>
          <div className="h-[28rem] overflow-auto rounded-md border border-border p-4">
            {document.sections.length > 0 ? (
              <ChordRenderer song={document} />
            ) : (
              <p className="text-sm text-muted-foreground">
                Comece a escrever para ver a cifra aqui.
              </p>
            )}
          </div>
        </div>
      </div>
    </form>
  )
}
