import { describe, expect, it } from 'vitest'

import { translateAuthError } from './authService'

describe('translateAuthError', () => {
  it('explica o limite de e-mails, que não é problema de senha', () => {
    const message = translateAuthError('email rate limit exceeded')
    expect(message).toContain('limite de e-mails')
    expect(message).toContain('Confirm email')
  })

  it('usa o tamanho mínimo que o servidor informou', () => {
    expect(translateAuthError('Password should be at least 6 characters.')).toBe(
      'A senha precisa ter pelo menos 6 caracteres.',
    )
    expect(translateAuthError('Password should be at least 10 characters.')).toBe(
      'A senha precisa ter pelo menos 10 caracteres.',
    )
  })

  it('traduz senha fraca e credenciais inválidas', () => {
    expect(translateAuthError('Password is known to be weak')).toContain('fraca')
    expect(translateAuthError('Invalid login credentials')).toBe('E-mail ou senha incorretos.')
  })

  it('avisa quando o provedor não está habilitado', () => {
    expect(translateAuthError('Unsupported provider: provider is not enabled')).toContain(
      'não está habilitado',
    )
  })

  it('devolve a mensagem original quando não conhece o erro', () => {
    expect(translateAuthError('algo muito específico')).toBe('algo muito específico')
  })
})
