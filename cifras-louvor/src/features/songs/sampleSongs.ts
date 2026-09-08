/**
 * Acervo de domínio público.
 *
 * Serve de duas formas: é o que a demonstração mostra sem Supabase, e é o que
 * a igreja pode importar de uma vez para começar com o acervo já povoado.
 *
 * São hinos cujo texto está em domínio público, com as cifras escritas por
 * nós. Nada vem de base de terceiros. Ainda assim, CONFIRA a letra com o seu
 * hinário antes de usar no culto — grafias e traduções variam entre edições.
 */
import { parseChordPro } from '@/lib/chordpro/parser'
import type { SongDocument } from '@/lib/chordpro/types'

export interface SampleSong {
  slug: string
  category: string
  document: SongDocument
}

const SOURCES: Array<{ slug: string; category: string; chordpro: string }> = [
  {
    slug: 'sublime-graca',
    category: 'Adoração',
    chordpro: `{title: Sublime Graça}
{artist: John Newton (domínio público)}
{key: G}
{tempo: 72}
{time: 3/4}

{start_of_intro}
[G] [C] [G] [D]
{end_of_intro}

{start_of_verse: Verso 1}
Su[G]blime gra[G7]ça do Se[C]nhor
Que um [G]infeliz sal[Em]vou
Perdido [G]eu andava ao [D]léu
E [G]Ele me [D]resga[G]tou
{end_of_verse}

{start_of_verse: Verso 2}
Foi [G]Sua graça [G7]que ensi[C]nou
Meu [G]coração a [Em]crer
E [G]paz a minha [D]alma achou
Na [G]hora de [D]sofr[G]er
{end_of_verse}

{start_of_chorus}
# Refrão cantado a duas vozes
Bem [C]longe eu já [G]cheguei
E [Em]cedo estou [D]aqui
Foi [C]Sua graça [G]que guardou
E [D]graça me [G]guiará
{end_of_chorus}
`,
  },
  {
    slug: 'santo-santo-santo',
    category: 'Adoração',
    chordpro: `{title: Santo, Santo, Santo}
{artist: Reginald Heber (domínio público)}
{key: D}
{tempo: 84}
{time: 4/4}

{start_of_verse: Verso 1}
[D]Santo! San[A]to! San[D]to! Deus onipo[G]tente!
[D]Cedo de ma[A]nhã can[D]taremos [A]Teu lou[D]vor
[D]Santo! San[A]to! San[D]to! Justo e cle[Bm]mente
[G]Deus em [D]três Pes[A]soas, ben[G]dita Trin[D]dade
{end_of_verse}

{start_of_verse: Verso 2}
[D]Santo! San[A]to! San[D]to! Todos os re[G]midos
[D]Junto aos se[A]rafins pros[D]tram-se [A]ante [D]Ti
[D]De alegria [A]cheios e [D]agrade[Bm]cidos
[G]Nós Te [D]adora[A]mos, ó [G]Senhor Je[D]sus
{end_of_verse}
`,
  },
  {
    slug: 'castelo-forte',
    category: 'Celebração',
    chordpro: `{title: Castelo Forte}
{artist: Martinho Lutero (domínio público)}
{key: Em}
{tempo: 96}
{time: 4/4}

{start_of_intro}
[Em] [D] [G] [Bm]
{end_of_intro}

{start_of_verse: Verso 1}
Cas[Em]telo forte é [D]nosso Deus
[G]Espada e bom es[Bm]cudo
Com [Em]Seu poder de[D]fende os Seus
Em [Am]todo transe [B7]agudo
{end_of_verse}

{start_of_chorus}
Com [G]fúria pertinaz
Per[D]segue Satanás
Com [Em]ares de furor
E [C]cheio de rancor
E com as[Am]túcia [B7]quer [Em]perder-nos
{end_of_chorus}
`,
  },
]

export const SAMPLE_SONGS: SampleSong[] = SOURCES.map(({ slug, category, chordpro }) => ({
  slug,
  category,
  document: parseChordPro(chordpro),
}))

export function findSampleSong(slug: string): SampleSong | undefined {
  return SAMPLE_SONGS.find((song) => song.slug === slug)
}

/** Os mesmos hinos, em ChordPro cru, para gravar no banco da igreja. */
export const PUBLIC_DOMAIN_LIBRARY = SOURCES
