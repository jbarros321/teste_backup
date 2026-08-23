import React, { createContext, useContext, useState, useEffect, useRef } from 'react';
import type { Project, FlowNode, FlowEdge, Comment, ProjectStatus, UserSession, AuditLog } from '../types';
import { supabase, isSupabaseConfigured } from '../lib/supabase';
import { callGemini, getGeminiKey, getGeminiModel } from '../lib/gemini';

// Gera um UUID válido (necessário porque a coluna projects.id é uuid no Supabase)
const uuid = (): string =>
  (typeof crypto !== 'undefined' && 'randomUUID' in crypto)
    ? crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(16).slice(2)}`;

// -------- Conversão entre o Project (front) e a linha do Supabase --------
const rowToProject = (r: any): Project => ({
  id: r.id,
  name: r.name,
  description: r.description ?? '',
  category: r.category ?? '',
  team: r.team ?? [],
  status: r.status,
  version: r.version,
  createdAt: r.created_at,
  updatedAt: r.updated_at,
  nodes: r.nodes ?? [],
  edges: r.edges ?? [],
  comments: r.comments ?? [],
  versions: r.versions ?? [],
  auditLogs: r.audit_logs ?? [],
});

const projectToRow = (p: Project, companyId?: string, userId?: string) => ({
  id: p.id,
  company_id: companyId,
  name: p.name,
  description: p.description,
  category: p.category,
  status: p.status,
  version: p.version,
  team: p.team,
  nodes: p.nodes,
  edges: p.edges,
  comments: p.comments,
  versions: p.versions,
  audit_logs: p.auditLogs,
  created_by: userId ?? null,
  updated_at: p.updatedAt,
});

interface AppContextType {
  currentScreen: 'login' | 'dashboard' | 'editor';
  setScreen: (screen: 'login' | 'dashboard' | 'editor') => void;
  user: UserSession;
  supabaseReady: boolean;
  authLoading: boolean;
  // Autenticação real (Supabase)
  signIn: (email: string, password: string) => Promise<void>;
  signUp: (email: string, password: string, companyName: string, fullName: string) => Promise<void>;
  // Login "demo"/offline (usado quando o Supabase não está configurado, e pelos botões SSO)
  login: (email: string) => void;
  logout: () => void;
  addTokens: (amount: number) => void;
  projects: Project[];
  activeProjectId: string | null;
  activeProject: Project | null;
  selectProject: (id: string | null) => void;
  createProject: (name: string, description: string, category: string, template: 'flowchart' | 'organogram') => void;
  updateProjectStatus: (status: ProjectStatus) => void;
  deleteProject: (id: string) => void;
  
  // Node Operations
  addNode: (type: FlowNode['type'], label: string, x: number, y: number) => FlowNode;
  updateNode: (nodeId: string, updates: Partial<FlowNode>) => void;
  deleteNode: (nodeId: string) => void;
  
  // Edge Operations
  addEdge: (source: string, target: string) => void;
  deleteEdge: (edgeId: string) => void;
  
  // Comment Operations
  addComment: (text: string, nodeId?: string) => void;
  resolveComment: (commentId: string) => void;
  
  // Versions
  saveVersion: (description: string) => void;
  rollbackToVersion: (versionName: string) => void;
  
  // AI and Tokens Helper
  spendTokens: (amount: number, actionName: string) => boolean;
  askAI: (promptText: string, customApiKey?: string) => Promise<string>;
  
  // Step List Sync (Flow Intelligent)
  syncStepList: (steps: string[]) => void;

  // Analisa um texto livre e devolve as etapas do fluxo (IA quando houver chave; senão heurística local)
  generateFlowSteps: (text: string) => Promise<{ steps: string[]; usedAI: boolean }>;
}

// Quebra um texto livre em etapas (fallback local, sem IA)
const parseStepsLocally = (raw: string): string[] => {
  const text = raw.trim();
  if (!text) return [];

  // 1) tenta por linhas; se vier um bloco único, quebra por frases e conectores comuns
  let steps = text.split(/\n+/).map(s => s.trim()).filter(Boolean);
  if (steps.length <= 1) {
    steps = text
      .split(/(?:[.;:]+|\b(?:em seguida|logo em seguida|na sequ[eê]ncia|depois disso|depois|ent[aã]o|ap[oó]s|posteriormente|por fim|por [uú]ltimo)\b[,:]?)/i)
      .map(s => s.trim())
      .filter(Boolean);
  }
  // 2) limpa numeração/marcadores iniciais
  steps = steps
    .map(s => s.replace(/^\s*(\d+|[a-zA-Z])[).\-\]]+\s*/, '').replace(/^[-*•]\s*/, '').trim())
    .filter(s => s.length > 1);

  return steps;
};

const AppContext = createContext<AppContextType | undefined>(undefined);

// Initial Sample Project
const createSampleProject = (): Project => {
  const initialNodes: FlowNode[] = [
    {
      id: 'node-1',
      type: 'process',
      label: 'Receber Paciente',
      description: 'Acolhimento inicial na recepção do hospital ou clínica, triando a queixa principal.',
      x: 100,
      y: 100,
      responsible: 'Recepcionista',
      checklist: [
        { id: 'c1', text: 'Confirmar nome completo e CPF', done: true },
        { id: 'c2', text: 'Entregar termo de consentimento', done: false }
      ]
    },
    {
      id: 'node-2',
      type: 'process',
      label: 'Cadastrar Paciente',
      description: 'Inserção de dados cadastrais no prontuário eletrônico e conferência de convênio médico.',
      x: 100,
      y: 250,
      responsible: 'Recepcionista',
      checklist: [
        { id: 'c3', text: 'Verificar validade da carteira do convênio', done: true },
        { id: 'c4', text: 'Coletar assinatura digital', done: true }
      ]
    },
    {
      id: 'node-3',
      type: 'decision',
      label: 'Possui Convênio?',
      description: 'Verificação da cobertura e aceitação do convênio apresentado pelo paciente.',
      x: 100,
      y: 400,
      responsible: 'Faturamento',
      checklist: []
    },
    {
      id: 'node-4',
      type: 'process',
      label: 'Autorizar Procedimentos',
      description: 'Solicitação de liberação eletrônica via portal de convênio parceiro.',
      x: 350,
      y: 400,
      responsible: 'Faturamento',
      checklist: [
        { id: 'c5', text: 'Emitir guia de SADT', done: false }
      ]
    },
    {
      id: 'node-5',
      type: 'process',
      label: 'Emitir Cobrança Particular',
      description: 'Apresentação de valores particulares para consulta e exames imediatos.',
      x: -150,
      y: 400,
      responsible: 'Financeiro',
      checklist: [
        { id: 'c6', text: 'Registrar pagamento via cartão ou Pix', done: false }
      ]
    },
    {
      id: 'node-6',
      type: 'subprocess',
      label: 'Triagem Clínica',
      description: 'Aferição de sinais vitais, classificação de risco através do protocolo de Manchester.',
      x: 100,
      y: 580,
      responsible: 'Enfermeiro Triagem',
      checklist: [
        { id: 'c7', text: 'Aferir Pressão Arterial', done: true },
        { id: 'c8', text: 'Medir Saturação e Temperatura', done: true },
        { id: 'c9', text: 'Aplicar pulseira de cor Manchester', done: false }
      ]
    }
  ];

  const initialEdges: FlowEdge[] = [
    { id: 'edge-1', source: 'node-1', target: 'node-2' },
    { id: 'edge-2', source: 'node-2', target: 'node-3' },
    { id: 'edge-3', source: 'node-3', target: 'node-4' }, // Sim / Convênio
    { id: 'edge-4', source: 'node-3', target: 'node-5' }, // Não / Particular
    { id: 'edge-5', source: 'node-4', target: 'node-6' },
    { id: 'edge-6', source: 'node-5', target: 'node-6' }
  ];

  const initialComments: Comment[] = [
    {
      id: 'comm-1',
      nodeId: 'node-6',
      author: 'Dr. Roberto Santos',
      text: 'O protocolo de Manchester é obrigatório para cumprimento da norma da ONA.',
      createdAt: new Date(Date.now() - 3600000 * 2).toISOString(),
      resolved: false
    }
  ];

  const initialAudit: AuditLog[] = [
    { action: 'Criação do Projeto', user: 'adm@uiflux.com', date: new Date(Date.now() - 86400000).toISOString(), details: 'Projeto inicial de admissão criado com template de Fluxograma.' },
    { action: 'Criação de Nós', user: 'adm@uiflux.com', date: new Date(Date.now() - 86400000 + 1000).toISOString(), details: 'Configurado fluxo base com 6 etapas e 1 decisão.' }
  ];

  return {
    id: 'proj-sample-1',
    name: 'Admissão de Pacientes (Hospital da Colina)',
    description: 'Fluxo oficial de recepção, triagem de Manchester e encaminhamento clínico para o pronto-atendimento.',
    category: 'Saúde & Hospitais',
    team: ['adm@uiflux.com', 'roberto.santos@colina.com', 'marcia.triagem@colina.com'],
    status: 'developing',
    version: '1.0.0',
    createdAt: new Date(Date.now() - 86400000).toISOString(),
    updatedAt: new Date().toISOString(),
    nodes: initialNodes,
    edges: initialEdges,
    comments: initialComments,
    versions: [
      {
        version: '1.0.0',
        description: 'Primeira versão do fluxo com processo básico de recepção e triagem.',
        date: new Date(Date.now() - 3600000 * 12).toISOString(),
        author: 'adm@uiflux.com',
        nodes: JSON.parse(JSON.stringify(initialNodes)),
        edges: JSON.parse(JSON.stringify(initialEdges))
      }
    ],
    auditLogs: initialAudit
  };
};

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentScreen, setScreen] = useState<'login' | 'dashboard' | 'editor'>('login');
  const [user, setUser] = useState<UserSession>(() => {
    const saved = localStorage.getItem('uiflux_user');
    return saved ? JSON.parse(saved) : { email: '', tokens: 150, company: 'Hospital da Colina', isLoggedIn: false };
  });
  
  const [projects, setProjects] = useState<Project[]>(() => {
    // Com Supabase ativo, começa vazio e carrega do banco após o login.
    if (isSupabaseConfigured) return [];
    const saved = localStorage.getItem('uiflux_projects');
    return saved ? JSON.parse(saved) : [createSampleProject()];
  });

  const [activeProjectId, setActiveProjectId] = useState<string | null>(() => {
    return isSupabaseConfigured ? null : localStorage.getItem('uiflux_active_project_id');
  });

  const [authLoading, setAuthLoading] = useState<boolean>(isSupabaseConfigured);

  // Controle de sincronização com o Supabase (evita gravações redundantes/loops)
  const syncedProjectsRef = useRef<Record<string, string>>({}); // id -> updatedAt já gravado
  const syncedTokensRef = useRef<number | null>(null);

  const activeProject = projects.find(p => p.id === activeProjectId) || null;

  // Carrega os projetos da empresa a partir do Supabase
  const loadProjectsFromSupabase = async (companyId?: string) => {
    if (!companyId) return;
    const { data, error } = await supabase
      .from('projects')
      .select('*')
      .eq('company_id', companyId)
      .order('updated_at', { ascending: false });

    if (error) {
      console.error('[UIFlux] Erro ao carregar projetos:', error.message);
      return;
    }
    const mapped = (data ?? []).map(rowToProject);
    // Marca todos como já sincronizados para não regravá-los à toa
    mapped.forEach(p => { syncedProjectsRef.current[p.id] = p.updatedAt; });
    setProjects(mapped);
  };

  // -------------------------------------------------------------------------
  // Autenticação via Supabase
  // -------------------------------------------------------------------------

  // Carrega o perfil + empresa do usuário logado e monta a sessão da UI.
  const hydrateSessionFromSupabase = async (authUser: { id: string; email?: string }) => {
    const { data: profile } = await supabase
      .from('profiles')
      .select('id, email, full_name, role, company_id, companies ( id, name, tokens )')
      .eq('id', authUser.id)
      .single();

    const company = (profile?.companies as unknown as { id: string; name: string; tokens: number } | null) || null;

    const session: UserSession = {
      id: authUser.id,
      email: profile?.email || authUser.email || '',
      fullName: profile?.full_name || '',
      companyId: profile?.company_id,
      role: profile?.role || 'membro',
      tokens: company?.tokens ?? 150,
      company: company?.name || 'Minha Empresa',
      isLoggedIn: true,
    };
    setUser(session);
    syncedTokensRef.current = session.tokens; // evita regravar tokens logo após carregar
    return session;
  };

  // Restaura sessão existente ao abrir o app + escuta mudanças de auth.
  useEffect(() => {
    if (!isSupabaseConfigured) {
      setAuthLoading(false);
      return;
    }

    let active = true;

    supabase.auth.getSession().then(async ({ data }) => {
      if (!active) return;
      if (data.session?.user) {
        const s = await hydrateSessionFromSupabase(data.session.user);
        await loadProjectsFromSupabase(s.companyId);
        setScreen('dashboard');
      }
      setAuthLoading(false);
    });

    const { data: sub } = supabase.auth.onAuthStateChange((_event, session) => {
      if (!active) return;
      if (!session?.user) {
        setUser({ email: '', tokens: 150, company: '', isLoggedIn: false });
        setProjects([]);
        syncedProjectsRef.current = {};
        setActiveProjectId(null);
        setScreen('login');
      }
    });

    return () => {
      active = false;
      sub.subscription.unsubscribe();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const signIn = async (email: string, password: string): Promise<void> => {
    if (!isSupabaseConfigured) {
      // Modo offline: aceita e entra como demo.
      login(email);
      return;
    }
    const { data, error } = await supabase.auth.signInWithPassword({ email, password });
    if (error) throw new Error(error.message);
    if (data.user) {
      const s = await hydrateSessionFromSupabase(data.user);
      await loadProjectsFromSupabase(s.companyId);
      setScreen('dashboard');
    }
  };

  const signUp = async (email: string, password: string, companyName: string, fullName: string): Promise<void> => {
    if (!isSupabaseConfigured) {
      login(email);
      return;
    }
    const { data, error } = await supabase.auth.signUp({
      email,
      password,
      options: { data: { company_name: companyName, full_name: fullName } },
    });
    if (error) throw new Error(error.message);
    // Se a confirmação por email estiver desativada, já vem uma sessão ativa.
    if (data.session?.user) {
      const s = await hydrateSessionFromSupabase(data.session.user);
      await loadProjectsFromSupabase(s.companyId);
      setScreen('dashboard');
    } else {
      throw new Error('Conta criada! Confirme seu email para entrar (verifique sua caixa de entrada).');
    }
  };

  // Persistência offline (só quando o Supabase NÃO está configurado)
  useEffect(() => {
    if (isSupabaseConfigured) return;
    localStorage.setItem('uiflux_user', JSON.stringify(user));
  }, [user]);

  useEffect(() => {
    if (isSupabaseConfigured) return;
    localStorage.setItem('uiflux_projects', JSON.stringify(projects));
  }, [projects]);

  useEffect(() => {
    if (isSupabaseConfigured) return;
    if (activeProjectId) {
      localStorage.setItem('uiflux_active_project_id', activeProjectId);
    } else {
      localStorage.removeItem('uiflux_active_project_id');
    }
  }, [activeProjectId]);

  // -------------------------------------------------------------------------
  // Sincronização automática com o Supabase (com debounce)
  // -------------------------------------------------------------------------

  // Salva no banco qualquer projeto cujo updatedAt mudou desde a última gravação.
  useEffect(() => {
    if (!isSupabaseConfigured || !user.isLoggedIn || !user.companyId) return;

    const pending = projects.filter(p => syncedProjectsRef.current[p.id] !== p.updatedAt);
    if (pending.length === 0) return;

    const t = setTimeout(async () => {
      for (const p of pending) {
        const { error } = await supabase
          .from('projects')
          .upsert(projectToRow(p, user.companyId, user.id));
        if (error) {
          console.error('[UIFlux] Erro ao salvar projeto:', error.message);
        } else {
          syncedProjectsRef.current[p.id] = p.updatedAt;
        }
      }
    }, 700);

    return () => clearTimeout(t);
  }, [projects, user.isLoggedIn, user.companyId, user.id]);

  // Salva o saldo de tokens da empresa quando ele muda.
  useEffect(() => {
    if (!isSupabaseConfigured || !user.isLoggedIn || !user.companyId) return;
    if (syncedTokensRef.current === null || syncedTokensRef.current === user.tokens) return;

    const t = setTimeout(async () => {
      const { error } = await supabase
        .from('companies')
        .update({ tokens: user.tokens })
        .eq('id', user.companyId);
      if (!error) syncedTokensRef.current = user.tokens;
    }, 500);

    return () => clearTimeout(t);
  }, [user.tokens, user.isLoggedIn, user.companyId]);

  const login = (email: string) => {
    setUser(prev => ({
      ...prev,
      email,
      isLoggedIn: true,
      company: email.includes('@') ? email.split('@')[1].split('.')[0].toUpperCase() : 'UIFlux Corp'
    }));
    setScreen('dashboard');
  };

  const logout = () => {
    if (isSupabaseConfigured) {
      supabase.auth.signOut();
    }
    setUser({ email: '', tokens: 150, company: '', isLoggedIn: false });
    setActiveProjectId(null);
    setScreen('login');
  };

  const addTokens = (amount: number) => {
    setUser(prev => ({ ...prev, tokens: prev.tokens + amount }));
  };

  const spendTokens = (amount: number, actionName: string): boolean => {
    if (user.tokens < amount) {
      alert(`Saldo insuficiente! Esta ação custa ${amount} tokens. Você possui ${user.tokens}.`);
      return false;
    }
    
    setUser(prev => ({ ...prev, tokens: prev.tokens - amount }));
    
    if (activeProjectId) {
      const log: AuditLog = {
        action: 'Consumo de Tokens',
        user: user.email || 'Usuário',
        date: new Date().toISOString(),
        details: `Gastou ${amount} tokens com a ação: "${actionName}"`
      };
      
      setProjects(prev => prev.map(p => {
        if (p.id === activeProjectId) {
          return {
            ...p,
            auditLogs: [log, ...p.auditLogs],
            updatedAt: new Date().toISOString()
          };
        }
        return p;
      }));
    }
    return true;
  };

  const selectProject = (id: string | null) => {
    setActiveProjectId(id);
    if (id) {
      setScreen('editor');
    } else {
      setScreen('dashboard');
    }
  };

  const createProject = (name: string, description: string, category: string, template: 'flowchart' | 'organogram') => {
    if (!spendTokens(template === 'flowchart' ? 15 : 20, `Criar Projeto (${template === 'flowchart' ? 'Fluxograma' : 'Organograma'})`)) {
      return;
    }

    const newProjId = uuid();
    const defaultNodes: FlowNode[] = [];
    const defaultEdges: FlowEdge[] = [];
    
    if (template === 'flowchart') {
      defaultNodes.push(
        { id: 'node-start', type: 'process', label: 'Etapa Inicial', description: 'Início do fluxo. Descreva a primeira ação.', x: 100, y: 100, responsible: 'Operador' }
      );
    } else {
      // Organogram default nodes
      defaultNodes.push(
        { id: 'node-ceo', type: 'organogram', label: 'Diretor Geral (CEO)', description: 'Liderança executiva principal.', x: 150, y: 50, responsible: 'Diretoria' },
        { id: 'node-dir-1', type: 'organogram', label: 'Diretor Operacional', description: 'Responsável pelas operações.', x: -50, y: 220, responsible: 'Operacional' },
        { id: 'node-dir-2', type: 'organogram', label: 'Diretor Financeiro', description: 'Responsável pelo financeiro.', x: 350, y: 220, responsible: 'Financeiro' }
      );
      defaultEdges.push(
        { id: 'edge-org-1', source: 'node-ceo', target: 'node-dir-1' },
        { id: 'edge-org-2', source: 'node-ceo', target: 'node-dir-2' }
      );
    }

    const newProject: Project = {
      id: newProjId,
      name,
      description,
      category,
      team: [user.email],
      status: 'draft',
      version: '1.0.0',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      nodes: defaultNodes,
      edges: defaultEdges,
      comments: [],
      versions: [
        {
          version: '1.0.0',
          description: 'Inicialização do projeto.',
          date: new Date().toISOString(),
          author: user.email,
          nodes: JSON.parse(JSON.stringify(defaultNodes)),
          edges: JSON.parse(JSON.stringify(defaultEdges))
        }
      ],
      auditLogs: [
        {
          action: 'Criação do Projeto',
          user: user.email,
          date: new Date().toISOString(),
          details: `Projeto criado usando o template: ${template === 'flowchart' ? 'Fluxograma' : 'Organograma'}.`
        }
      ]
    };

    setProjects(prev => [newProject, ...prev]);
    setActiveProjectId(newProjId);
    setScreen('editor');
  };

  const updateProjectStatus = (status: ProjectStatus) => {
    if (!activeProjectId) return;
    
    const log: AuditLog = {
      action: 'Alteração de Status',
      user: user.email,
      date: new Date().toISOString(),
      details: `Status alterado para: ${status.toUpperCase()}`
    };

    setProjects(prev => prev.map(p => {
      if (p.id === activeProjectId) {
        return {
          ...p,
          status,
          auditLogs: [log, ...p.auditLogs],
          updatedAt: new Date().toISOString()
        };
      }
      return p;
    }));
  };

  const deleteProject = (id: string) => {
    setProjects(prev => prev.filter(p => p.id !== id));
    delete syncedProjectsRef.current[id];
    if (isSupabaseConfigured) {
      supabase.from('projects').delete().eq('id', id).then(({ error }) => {
        if (error) console.error('[UIFlux] Erro ao excluir projeto:', error.message);
      });
    }
    if (activeProjectId === id) {
      setActiveProjectId(null);
      setScreen('dashboard');
    }
  };

  // Node Operations
  const addNode = (type: FlowNode['type'], label: string, x: number, y: number): FlowNode => {
    const newNode: FlowNode = {
      id: `node-${Date.now()}`,
      type,
      label,
      description: `Descrição da etapa: ${label}`,
      x,
      y,
      responsible: '',
      checklist: []
    };

    if (!activeProjectId) return newNode;

    const log: AuditLog = {
      action: 'Adição de Nó',
      user: user.email,
      date: new Date().toISOString(),
      details: `Nó do tipo "${type}" criado: "${label}"`
    };

    setProjects(prev => prev.map(p => {
      if (p.id === activeProjectId) {
        return {
          ...p,
          nodes: [...p.nodes, newNode],
          auditLogs: [log, ...p.auditLogs],
          updatedAt: new Date().toISOString()
        };
      }
      return p;
    }));

    return newNode;
  };

  const updateNode = (nodeId: string, updates: Partial<FlowNode>) => {
    if (!activeProjectId) return;

    setProjects(prev => prev.map(p => {
      if (p.id === activeProjectId) {
        const updatedNodes = p.nodes.map(n => {
          if (n.id === nodeId) {
            return { ...n, ...updates };
          }
          return n;
        });

        return {
          ...p,
          nodes: updatedNodes,
          updatedAt: new Date().toISOString()
        };
      }
      return p;
    }));
  };

  const deleteNode = (nodeId: string) => {
    if (!activeProjectId) return;

    const log: AuditLog = {
      action: 'Exclusão de Nó',
      user: user.email,
      date: new Date().toISOString(),
      details: `Nó ID ${nodeId} e suas conexões associadas foram removidos.`
    };

    setProjects(prev => prev.map(p => {
      if (p.id === activeProjectId) {
        return {
          ...p,
          nodes: p.nodes.filter(n => n.id !== nodeId),
          edges: p.edges.filter(e => e.source !== nodeId && e.target !== nodeId),
          auditLogs: [log, ...p.auditLogs],
          updatedAt: new Date().toISOString()
        };
      }
      return p;
    }));
  };

  // Edge Operations
  const addEdge = (source: string, target: string) => {
    if (!activeProjectId || source === target) return;

    // Evitar conexões duplicadas
    const exists = activeProject?.edges.some(e => e.source === source && e.target === target);
    if (exists) return;

    const newEdge: FlowEdge = {
      id: `edge-${Date.now()}`,
      source,
      target
    };

    const log: AuditLog = {
      action: 'Conexão Criada',
      user: user.email,
      date: new Date().toISOString(),
      details: `Criada conexão entre nó ${source} e nó ${target}`
    };

    setProjects(prev => prev.map(p => {
      if (p.id === activeProjectId) {
        return {
          ...p,
          edges: [...p.edges, newEdge],
          auditLogs: [log, ...p.auditLogs],
          updatedAt: new Date().toISOString()
        };
      }
      return p;
    }));
  };

  const deleteEdge = (edgeId: string) => {
    if (!activeProjectId) return;

    const log: AuditLog = {
      action: 'Conexão Removida',
      user: user.email,
      date: new Date().toISOString(),
      details: `Conexão ID ${edgeId} foi removida.`
    };

    setProjects(prev => prev.map(p => {
      if (p.id === activeProjectId) {
        return {
          ...p,
          edges: p.edges.filter(e => e.id !== edgeId),
          auditLogs: [log, ...p.auditLogs],
          updatedAt: new Date().toISOString()
        };
      }
      return p;
    }));
  };

  // Comment Operations
  const addComment = (text: string, nodeId?: string) => {
    if (!activeProjectId) return;

    const newComment: Comment = {
      id: `comm-${Date.now()}`,
      nodeId,
      author: user.email,
      text,
      createdAt: new Date().toISOString(),
      resolved: false
    };

    const log: AuditLog = {
      action: 'Comentário Adicionado',
      user: user.email,
      date: new Date().toISOString(),
      details: `Novo comentário adicionado ${nodeId ? `ao nó ${nodeId}` : 'geral'}: "${text.slice(0, 30)}..."`
    };

    setProjects(prev => prev.map(p => {
      if (p.id === activeProjectId) {
        return {
          ...p,
          comments: [...p.comments, newComment],
          auditLogs: [log, ...p.auditLogs],
          updatedAt: new Date().toISOString()
        };
      }
      return p;
    }));
  };

  const resolveComment = (commentId: string) => {
    if (!activeProjectId) return;

    setProjects(prev => prev.map(p => {
      if (p.id === activeProjectId) {
        return {
          ...p,
          comments: p.comments.map(c => c.id === commentId ? { ...c, resolved: true } : c),
          updatedAt: new Date().toISOString()
        };
      }
      return p;
    }));
  };

  // Sincronização Bidirecional / Painel Esquerdo (Lista de passos em tempo real - Flow Intelligent)
  const syncStepList = (steps: string[]) => {
    if (!activeProjectId) return;

    const currentProject = activeProject;
    if (!currentProject) return;

    // Mapear nós existentes
    const existingNodes = [...currentProject.nodes];
    const newNodes: FlowNode[] = [];
    const newEdges: FlowEdge[] = [];
    
    // Layout em GRADE (colunas): desce até ROWS_PER_COL e depois vai para o lado.
    const ROWS_PER_COL = 6;
    const COL_W = 300;
    const ROW_H = 150;
    const X0 = 80;
    const Y0 = 80;

    const stepNodeIds: string[] = []; // ordem dos nós da sequência (para conectar)
    let placed = 0;

    steps.forEach((stepLabel) => {
      // Remove numeração/marcadores iniciais ("1.", "a)", "-", "•"...)
      const cleanLabel = stepLabel
        .replace(/^\s*(\d+|[a-zA-Z])[).\-\]]+\s*/, '')
        .replace(/^[-*•]\s*/, '')
        .trim();
      if (!cleanLabel) return;

      const col = Math.floor(placed / ROWS_PER_COL);
      const row = placed % ROWS_PER_COL;
      const posX = X0 + col * COL_W;
      const posY = Y0 + row * ROW_H;
      placed++;

      const found = existingNodes.find(n => n.label.toLowerCase() === cleanLabel.toLowerCase());

      if (found) {
        newNodes.push({ ...found, x: posX, y: posY });
        stepNodeIds.push(found.id);
        const idx = existingNodes.indexOf(found);
        if (idx > -1) existingNodes.splice(idx, 1);
      } else {
        const id = `node-sync-${Date.now()}-${placed}`;
        newNodes.push({
          id,
          type: /\?|^se\s|caso|decis/i.test(cleanLabel) ? 'decision' : 'process',
          label: cleanLabel,
          description: `Etapa: ${cleanLabel}`,
          x: posX,
          y: posY,
          responsible: 'Operador',
          checklist: []
        });
        stepNodeIds.push(id);
      }
    });

    // Nós que sobraram (não citados na lista) mantêm suas posições originais
    newNodes.push(...existingNodes);

    // Conecta APENAS os nós da sequência, em ordem
    for (let i = 0; i < stepNodeIds.length - 1; i++) {
      newEdges.push({
        id: `edge-sync-${i}-${Date.now()}`,
        source: stepNodeIds[i],
        target: stepNodeIds[i + 1]
      });
    }

    // Mantém conexões antigas ainda válidas (sem duplicar)
    currentProject.edges.forEach(e => {
      const sourceExists = newNodes.some(n => n.id === e.source);
      const targetExists = newNodes.some(n => n.id === e.target);
      const isDuplicate = newEdges.some(ne => ne.source === e.source && ne.target === e.target);
      if (sourceExists && targetExists && !isDuplicate) {
        newEdges.push(e);
      }
    });

    setProjects(prev => prev.map(p => {
      if (p.id === activeProjectId) {
        return {
          ...p,
          nodes: newNodes,
          edges: newEdges,
          updatedAt: new Date().toISOString()
        };
      }
      return p;
    }));
  };

  // Analisa o texto com IA (Gemini, se houver chave) e devolve as etapas do fluxo.
  const generateFlowSteps = async (text: string): Promise<{ steps: string[]; usedAI: boolean }> => {
    const key = getGeminiKey();
    const model = getGeminiModel();

    if (key) {
      // Ação de IA → consome créditos
      if (!spendTokens(10, 'Gerar fluxo a partir de texto (IA Gemini)')) {
        throw new Error('Tokens insuficientes');
      }
      try {
        const system =
          'Você transforma a descrição de um processo em uma lista ORDENADA e enxuta de etapas para um fluxograma. ' +
          'Regras: cada etapa deve ser curta (2 a 6 palavras), em verbo no infinitivo/imperativo; ' +
          'pontos de decisão devem ser escritos como PERGUNTA terminando com "?"; ' +
          'não numere; não repita etapas. ' +
          'Responda APENAS um JSON válido no formato {"steps": ["...", "..."]} sem nenhum texto extra.';

        const content = await callGemini(key, model, system, text, true);
        const parsed = JSON.parse(content || '{}');
        const steps: string[] = Array.isArray(parsed.steps)
          ? parsed.steps.filter((s: any) => typeof s === 'string' && s.trim()).map((s: string) => s.trim())
          : [];

        if (steps.length > 0) return { steps, usedAI: true };
        // Se a IA não retornou nada útil, cai para a heurística local
        return { steps: parseStepsLocally(text), usedAI: false };
      } catch (err) {
        console.error('[UIFlux] Erro na IA (Gemini) ao gerar fluxo, usando heurística local:', err);
        return { steps: parseStepsLocally(text), usedAI: false };
      }
    }

    // Sem chave de IA → heurística local (grátis)
    return { steps: parseStepsLocally(text), usedAI: false };
  };

  // Versionamento
  const saveVersion = (description: string) => {
    if (!activeProjectId || !activeProject) return;

    // Gastar token
    if (!spendTokens(5, `Criar Versão (${activeProject.version})`)) return;

    // Incrementar minor version
    const parts = activeProject.version.split('.');
    const nextMinor = parseInt(parts[2]) + 1;
    const nextVersion = `${parts[0]}.${parts[1]}.${nextMinor}`;

    const newVersionObj = {
      version: nextVersion,
      description,
      date: new Date().toISOString(),
      author: user.email,
      nodes: JSON.parse(JSON.stringify(activeProject.nodes)),
      edges: JSON.parse(JSON.stringify(activeProject.edges))
    };

    const log: AuditLog = {
      action: 'Nova Versão Salva',
      user: user.email,
      date: new Date().toISOString(),
      details: `Salva versão ${nextVersion} com a nota: "${description}"`
    };

    setProjects(prev => prev.map(p => {
      if (p.id === activeProjectId) {
        return {
          ...p,
          version: nextVersion,
          versions: [newVersionObj, ...p.versions],
          auditLogs: [log, ...p.auditLogs],
          updatedAt: new Date().toISOString()
        };
      }
      return p;
    }));

    alert(`Versão ${nextVersion} criada com sucesso!`);
  };

  const rollbackToVersion = (versionName: string) => {
    if (!activeProjectId || !activeProject) return;

    const target = activeProject.versions.find(v => v.version === versionName);
    if (!target) {
      alert(`Versão ${versionName} não localizada.`);
      return;
    }

    if (!confirm(`Deseja mesmo reverter as etapas e posições do Canvas para o estado da Versão ${versionName}? As alterações não salvas serão perdidas.`)) {
      return;
    }

    const log: AuditLog = {
      action: 'Rollback Efetuado',
      user: user.email,
      date: new Date().toISOString(),
      details: `Restaurado para o estado da Versão ${versionName}`
    };

    setProjects(prev => prev.map(p => {
      if (p.id === activeProjectId) {
        return {
          ...p,
          nodes: JSON.parse(JSON.stringify(target.nodes)),
          edges: JSON.parse(JSON.stringify(target.edges)),
          auditLogs: [log, ...p.auditLogs],
          updatedAt: new Date().toISOString()
        };
      }
      return p;
    }));

    alert(`Rollback concluído para a Versão ${versionName}.`);
  };

  // Integração real com o Google Gemini (ou simulador offline quando não há chave)
  const askAI = async (promptText: string, customApiKey?: string): Promise<string> => {
    if (!spendTokens(8, 'Análise de Processo com IA')) {
      throw new Error('Tokens insuficientes');
    }

    const key = (customApiKey || getGeminiKey()).trim();
    const model = getGeminiModel();

    // Se houver uma chave do Gemini, chamar de verdade
    if (key) {
      try {
        const flowText = activeProject?.nodes.map((n, i) => `${i + 1}. [${n.type.toUpperCase()}] ${n.label} (Resp: ${n.responsible || 'Sem responsável'})`).join('\n') || '';

        const system = `Você é um analista especialista em qualidade, PMO e modelagem de processos BPMN/POP da plataforma UIFlux.
Sua tarefa é analisar o fluxo de processos atual e o pedido do usuário.
Seja direto, profissional e estruturado. Indique gargalos, sugestões de novas etapas de controle ou automações.
Sempre forneça uma resposta construtiva no formato Markdown.`;

        const userText = `FLUXO ATUAL:\n${flowText}\n\nMENSAGEM DO USUÁRIO:\n${promptText}`;

        const out = await callGemini(key, model, system, userText, false);
        if (out.trim()) return out;
        throw new Error('Resposta vazia do Gemini');
      } catch (err: any) {
        console.error('Gemini Error, falling back to simulated engine:', err);
        return `*(Erro na conexão Gemini: ${err.message || err}. Respondendo em modo de contingência offline...)*\n\n` + runOfflineAISimulator(promptText);
      }
    } else {
      // Simulação Offline Inteligente
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve(runOfflineAISimulator(promptText));
        }, 1500);
      });
    }
  };

  const runOfflineAISimulator = (promptText: string): string => {
    const text = promptText.toLowerCase();
    
    // Heurísticas de acordo com os nós atuais
    const nodesCount = activeProject?.nodes.length || 0;
    const hasManchester = activeProject?.nodes.some(n => n.label.toLowerCase().includes('manchester') || n.label.toLowerCase().includes('triagem'));
    const hasCadastrar = activeProject?.nodes.some(n => n.label.toLowerCase().includes('cadastrar') || n.label.toLowerCase().includes('cadastro'));
    const hasConfirmar = activeProject?.nodes.some(n => n.label.toLowerCase().includes('confirmar') || n.label.toLowerCase().includes('valida'));

    if (text.includes('analisar') || text.includes('gargalo') || text.includes('sugestão') || text.includes('melhorar')) {
      let output = `### Análise de Inteligência Artificial (UIFlux Agent)\n\n`;
      output += `Analisei seu fluxo **"${activeProject?.name}"** contendo Atualmente **${nodesCount} etapas**. Identifiquei pontos de melhoria e potenciais gargalos regulatórios:\n\n`;
      
      if (hasCadastrar && !hasConfirmar) {
        output += `⚠️ **Gargalo de Identificação**: Detectei que existe a etapa de **Cadastrar Paciente**, mas não há uma etapa explícita de **Confirmação Cadastral / Validação de Documentos** antes do envio para a triagem. \n\n`;
        output += `💡 **Recomendação**: Adicionar um nó de Validação imediatamente após o Cadastro para evitar fraudes ou preenchimento incorreto de dados de convênio. Deseja adicionar o nó **"Confirmar Documentação"** automaticamente? (Clique no botão acima da IA para aplicar).\n\n`;
      }
      
      if (hasManchester && !activeProject?.nodes.some(n => n.responsible === 'Enfermeiro Triagem')) {
        output += `⚠️ **Responsabilidade Omissa**: A etapa de **Triagem Clínica** está sem papel responsável definido ou está incoerente. Para certificações de qualidade (como ONA/JCI), a triagem Manchester deve ser assinada por um profissional de Enfermagem habilitado.\n\n`;
      }

      output += `📊 **Conformidade Geral**: O fluxo atende a 75% das diretrizes básicas. Recomendo também adicionar um nó final de **Ficha de Atendimento Finalizado** para fins de auditoria no prontuário eletrônico.`;
      return output;
    }

    if (text.includes('pop') || text.includes('documentar')) {
      return `### Minuta para Documentação POP (Procedimento Operacional Padrão)
      
Gerado automaticamente com base nos seus nós do editor visual:
      
1. **Objetivo**: Padronizar as ações operacionais do fluxo de ${activeProject?.name}.
2. **Área Responsável**: Diretoria, Faturamento, Corpo Clínico e Recepção.
3. **Descrição das Etapas**:
${activeProject?.nodes.map((n, i) => `   * **Etapa ${i+1} - ${n.label}**: ${n.description} *(Responsável: ${n.responsible || 'Operador'})*`).join('\n')}

*Para publicar este documento e obter assinaturas eletrônicas dos validadores, avance o status do workflow no painel direito.*`;
    }

    // Resposta padrão amigável
    return `### Olá! Sou o Assistente de Processos da UIFlux.
    
Estou analisando seu fluxo em tempo real. Aqui estão comandos que você pode digitar ou pedir para eu executar:
* **"Analisar gargalos"**: Rodar a auditoria lógica do seu fluxo e apontar melhorias de certificação.
* **"Gerar minuta de POP"**: Criar um rascunho estendido com objetivos e descritivos para cada atividade.
* **"Sugerir checklists"**: Recomendar itens de checklist e sub-tarefas operacionais para os nós selecionados.

*Gostaria que eu fizesse alguma dessas tarefas agora?*`;
  };

  return (
    <AppContext.Provider value={{
      currentScreen,
      setScreen,
      user,
      supabaseReady: isSupabaseConfigured,
      authLoading,
      signIn,
      signUp,
      login,
      logout,
      addTokens,
      projects,
      activeProjectId,
      activeProject,
      selectProject,
      createProject,
      updateProjectStatus,
      deleteProject,
      addNode,
      updateNode,
      deleteNode,
      addEdge,
      deleteEdge,
      addComment,
      resolveComment,
      saveVersion,
      rollbackToVersion,
      spendTokens,
      askAI,
      syncStepList,
      generateFlowSteps
    }}>
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) throw new Error('useApp deve ser usado sob um AppProvider');
  return context;
};
