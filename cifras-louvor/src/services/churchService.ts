/** Igrejas e membros — o eixo do multi-tenant. */
import { requireSupabase } from '@/lib/supabase'
import type { Church, ChurchMember, ChurchRole, Profile } from '@/types/database'

export interface Membership {
  church: Church
  role: ChurchRole
}

export interface MemberWithProfile extends ChurchMember {
  profile: Pick<Profile, 'id' | 'name' | 'email' | 'avatar_url'> | null
}

export const churchService = {
  /** Igrejas de que o usuário logado participa. */
  async listMine(): Promise<Membership[]> {
    const { data, error } = await requireSupabase()
      .from('church_members')
      .select('role, church:churches(*)')
      .order('created_at', { ascending: true })

    if (error) throw new Error(error.message)

    return (data ?? [])
      .filter((row) => row.church !== null)
      .map((row) => ({ role: row.role as ChurchRole, church: row.church as unknown as Church }))
  },

  /** Cria a igreja e já entra como administrador (RPC, para não esbarrar no RLS). */
  async create(name: string): Promise<Church> {
    const { data, error } = await requireSupabase().rpc('create_church', { p_name: name })
    if (error) throw new Error(error.message)
    return data as Church
  },

  /** Entra numa igreja existente pelo código de convite. */
  async joinByInviteCode(inviteCode: string): Promise<Church> {
    const { data, error } = await requireSupabase().rpc('join_church', {
      p_invite_code: inviteCode,
    })
    if (error) throw new Error(translateJoinError(error.message))
    return data as Church
  },

  async listMembers(churchId: string): Promise<MemberWithProfile[]> {
    const { data, error } = await requireSupabase()
      .from('church_members')
      .select('*, profile:profiles(id, name, email, avatar_url)')
      .eq('church_id', churchId)
      .order('created_at', { ascending: true })

    if (error) throw new Error(error.message)
    return (data ?? []) as unknown as MemberWithProfile[]
  },

  async updateRole(memberId: string, role: ChurchRole): Promise<void> {
    const { error } = await requireSupabase()
      .from('church_members')
      .update({ role })
      .eq('id', memberId)
    if (error) throw new Error(error.message)
  },
}

function translateJoinError(message: string): string {
  if (/código de convite inválido/i.test(message)) return 'Código de convite inválido.'
  return message
}
