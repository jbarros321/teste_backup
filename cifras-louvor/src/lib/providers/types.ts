/**
 * Contrato dos provedores externos (§54 do escopo).
 *
 * A regra que define o que entra aqui: só metadados públicos e conteúdo
 * licenciado ou em domínio público. Letra e cifra protegidas de terceiros
 * NÃO são buscadas automaticamente — elas entram por ação do usuário, pela
 * importação de texto.
 */

/** Metadado de uma gravação, para preencher o cadastro. */
export interface SongMetadata {
  title: string
  artist: string | null
  album: string | null
  /** Ano de lançamento, quando conhecido. */
  year: number | null
  /** Duração em segundos. */
  durationSeconds: number | null
  /** Identificador no provedor, para consultas seguintes. */
  externalId: string
  provider: string
}

export interface MetadataProvider {
  /** Nome exibido na interface. */
  readonly name: string
  /** Atribuição exigida pela licença do provedor, exibida junto dos resultados. */
  readonly attribution: string
  search(query: string, signal?: AbortSignal): Promise<SongMetadata[]>
}
