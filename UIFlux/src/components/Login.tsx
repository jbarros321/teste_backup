import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { Mail, Lock, ArrowRight, Sparkles, Building2, User, AlertCircle, Info } from 'lucide-react';

type Mode = 'signin' | 'signup';

export const Login: React.FC = () => {
  const { signIn, signUp, login, supabaseReady } = useApp();
  const [mode, setMode] = useState<Mode>('signin');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [companyName, setCompanyName] = useState('');
  const [fullName, setFullName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setInfo(null);

    if (!email || !password) {
      setError('Preencha email e senha.');
      return;
    }
    if (mode === 'signup' && !companyName.trim()) {
      setError('Informe o nome da sua empresa.');
      return;
    }

    setLoading(true);
    try {
      if (mode === 'signin') {
        await signIn(email, password);
      } else {
        await signUp(email, password, companyName.trim(), fullName.trim());
      }
    } catch (err: any) {
      // signUp lança uma "mensagem de sucesso" quando exige confirmação de email
      const msg = err?.message || 'Ocorreu um erro. Tente novamente.';
      if (msg.toLowerCase().includes('confirme')) {
        setInfo(msg);
        setMode('signin');
      } else {
        setError(traduzErro(msg));
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '20px',
      position: 'relative',
      overflow: 'hidden'
    }}>
      {/* Esferas decorativas */}
      <div style={{
        position: 'absolute', top: '15%', left: '20%', width: '300px', height: '300px',
        borderRadius: '50%', background: 'radial-gradient(circle, rgba(6,182,212,0.15) 0%, transparent 70%)',
        filter: 'blur(40px)', zIndex: 0
      }} />
      <div style={{
        position: 'absolute', bottom: '15%', right: '20%', width: '350px', height: '350px',
        borderRadius: '50%', background: 'radial-gradient(circle, rgba(139,92,246,0.12) 0%, transparent 70%)',
        filter: 'blur(50px)', zIndex: 0
      }} />

      <div className="glass-panel animate-fade-in" style={{
        width: '100%', maxWidth: '440px', padding: '40px', zIndex: 1, position: 'relative'
      }}>
        {/* Logo */}
        <div style={{ textAlign: 'center', marginBottom: '28px' }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
            <div style={{
              width: '40px', height: '40px', borderRadius: '10px',
              background: 'linear-gradient(135deg, var(--primary), var(--secondary))',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              boxShadow: '0 4px 12px rgba(6, 182, 212, 0.4)'
            }}>
              <Sparkles size={20} color="#fff" />
            </div>
            <span style={{
              fontFamily: 'var(--font-sans)', fontWeight: 800, fontSize: '28px', letterSpacing: '-0.02em',
              background: 'linear-gradient(135deg, #fff 40%, var(--primary) 100%)',
              WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent'
            }}>UIFlux</span>
          </div>
          <p style={{ color: 'var(--text-muted)', fontSize: '14px' }}>
            {mode === 'signin' ? 'Entre na sua conta' : 'Crie a conta da sua empresa'}
          </p>
        </div>

        {/* Aviso de modo offline (Supabase não configurado) */}
        {!supabaseReady && (
          <div style={{
            display: 'flex', gap: '8px', alignItems: 'flex-start', padding: '10px 12px', marginBottom: '18px',
            background: 'rgba(245, 158, 11, 0.1)', border: '1px solid rgba(245, 158, 11, 0.3)',
            borderRadius: '8px', fontSize: '12px', color: '#fbbf24', lineHeight: 1.4
          }}>
            <Info size={16} style={{ flexShrink: 0, marginTop: '1px' }} />
            <span>Banco Supabase ainda não configurado. Rodando em <strong>modo demonstração</strong> (dados locais). Qualquer email/senha entra.</span>
          </div>
        )}

        {error && (
          <div style={{
            display: 'flex', gap: '8px', alignItems: 'center', padding: '10px 12px', marginBottom: '18px',
            background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.3)',
            borderRadius: '8px', fontSize: '13px', color: '#f87171'
          }}>
            <AlertCircle size={16} style={{ flexShrink: 0 }} />
            <span>{error}</span>
          </div>
        )}

        {info && (
          <div style={{
            display: 'flex', gap: '8px', alignItems: 'center', padding: '10px 12px', marginBottom: '18px',
            background: 'rgba(16, 185, 129, 0.1)', border: '1px solid rgba(16, 185, 129, 0.3)',
            borderRadius: '8px', fontSize: '13px', color: '#34d399'
          }}>
            <Info size={16} style={{ flexShrink: 0 }} />
            <span>{info}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
          {mode === 'signup' && (
            <>
              <Field label="Nome da empresa" icon={<Building2 size={16} />}>
                <input className="glass-input" style={{ width: '100%', paddingLeft: '40px' }}
                  placeholder="Ex: Hospital da Colina" value={companyName}
                  onChange={(e) => setCompanyName(e.target.value)} required />
              </Field>
              <Field label="Seu nome" icon={<User size={16} />}>
                <input className="glass-input" style={{ width: '100%', paddingLeft: '40px' }}
                  placeholder="Seu nome completo" value={fullName}
                  onChange={(e) => setFullName(e.target.value)} />
              </Field>
            </>
          )}

          <Field label="Email corporativo" icon={<Mail size={16} />}>
            <input type="email" className="glass-input" style={{ width: '100%', paddingLeft: '40px' }}
              placeholder="seu.nome@empresa.com" value={email}
              onChange={(e) => setEmail(e.target.value)} required />
          </Field>

          <Field label="Senha" icon={<Lock size={16} />}>
            <input type="password" className="glass-input" style={{ width: '100%', paddingLeft: '40px' }}
              placeholder={mode === 'signup' ? 'Mínimo 6 caracteres' : '••••••••'} value={password}
              onChange={(e) => setPassword(e.target.value)} required minLength={6} />
          </Field>

          <button type="submit" className="glass-btn glass-btn-primary"
            style={{ justifyContent: 'center', height: '44px', width: '100%', marginTop: '4px' }} disabled={loading}>
            {loading
              ? (mode === 'signin' ? 'Entrando...' : 'Criando conta...')
              : (mode === 'signin' ? 'Entrar na plataforma' : 'Criar conta')}
            {!loading && <ArrowRight size={16} />}
          </button>
        </form>

        {/* Alternar entre entrar / criar conta */}
        <div style={{ textAlign: 'center', marginTop: '20px', fontSize: '13px', color: 'var(--text-muted)' }}>
          {mode === 'signin' ? (
            <>Não tem conta?{' '}
              <button onClick={() => { setMode('signup'); setError(null); setInfo(null); }}
                style={linkBtn}>Criar conta da empresa</button>
            </>
          ) : (
            <>Já tem conta?{' '}
              <button onClick={() => { setMode('signin'); setError(null); setInfo(null); }}
                style={linkBtn}>Entrar</button>
            </>
          )}
        </div>

        {/* Entrada demo rápida (só no modo offline) */}
        {!supabaseReady && (
          <button type="button" className="glass-btn"
            style={{ justifyContent: 'center', width: '100%', marginTop: '16px', fontSize: '13px' }}
            onClick={() => login('demo@uiflux.com')}>
            Entrar como demonstração
          </button>
        )}
      </div>
    </div>
  );
};

const Field: React.FC<{ label: string; icon: React.ReactNode; children: React.ReactNode }> = ({ label, icon, children }) => (
  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
    <label style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text-muted)' }}>{label}</label>
    <div style={{ position: 'relative' }}>
      <span style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-dark)', display: 'flex' }}>
        {icon}
      </span>
      {children}
    </div>
  </div>
);

const linkBtn: React.CSSProperties = {
  background: 'none', border: 'none', color: 'var(--primary)', cursor: 'pointer',
  fontWeight: 600, fontSize: '13px', padding: 0
};

// Traduz mensagens de erro comuns do Supabase para PT-BR
function traduzErro(msg: string): string {
  const m = msg.toLowerCase();
  if (m.includes('invalid login credentials')) return 'Email ou senha incorretos.';
  if (m.includes('user already registered')) return 'Este email já está cadastrado. Faça login.';
  if (m.includes('password should be at least')) return 'A senha deve ter pelo menos 6 caracteres.';
  if (m.includes('email not confirmed')) return 'Confirme seu email antes de entrar (verifique sua caixa de entrada).';
  if (m.includes('unable to validate email')) return 'Email inválido.';
  return msg;
}
