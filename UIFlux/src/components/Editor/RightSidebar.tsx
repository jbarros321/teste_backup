import React, { useState, useRef, useEffect } from 'react';
import { useApp } from '../../context/AppContext';
import {
  Sparkles, FileText, CheckCircle, GitBranch, History,
  Send, Key, RefreshCw, Plus
} from 'lucide-react';
import {
  GEMINI_MODELS, GEMINI_KEY_STORAGE, GEMINI_MODEL_STORAGE, DEFAULT_GEMINI_MODEL
} from '../../lib/gemini';

interface ChatMessage {
  sender: 'user' | 'ai';
  text: string;
  timestamp: string;
  suggestionNode?: { label: string; type: 'process' | 'decision'; description: string };
}

export const RightSidebar: React.FC = () => {
  const { 
    activeProject, askAI, rollbackToVersion, addNode, updateNode, addEdge, deleteEdge, spendTokens 
  } = useApp();

  const [activeTab, setActiveTab] = useState<'ia' | 'docs' | 'workflow' | 'versions' | 'audit'>('ia');
  
  // IA State (Google Gemini)
  const [apiKey, setApiKey] = useState(() => localStorage.getItem(GEMINI_KEY_STORAGE) || '');
  const [aiModel, setAiModel] = useState(() => localStorage.getItem(GEMINI_MODEL_STORAGE) || DEFAULT_GEMINI_MODEL);
  const [showKeyInput, setShowKeyInput] = useState(false);
  const [inputMsg, setInputMsg] = useState('');
  const [chatHistory, setChatHistory] = useState<ChatMessage[]>([
    {
      sender: 'ai',
      text: 'Olá! Sou seu Assistente de Processos. Posso ajudar a auditar gargalos operacionais no seu fluxo atual ou gerar a minuta do POP. O que deseja fazer?',
      timestamp: new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
    }
  ]);
  const [aiLoading, setAiLoading] = useState(false);
  const chatEndRef = useRef<HTMLDivElement>(null);

  // Docs sub-tab state
  const [docSubTab, setDocSubTab] = useState<'pop' | 'raci' | 'check'>('pop');

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatHistory, aiLoading]);

  if (!activeProject) return null;

  // Save API key + modelo do Gemini
  const handleSaveKey = () => {
    localStorage.setItem(GEMINI_KEY_STORAGE, apiKey.trim());
    localStorage.setItem(GEMINI_MODEL_STORAGE, aiModel);
    setShowKeyInput(false);
    alert('Chave e modelo do Gemini salvos localmente!');
  };

  // Troca de modelo salva na hora (mesmo sem reabrir o painel de chave)
  const handleModelChange = (value: string) => {
    setAiModel(value);
    localStorage.setItem(GEMINI_MODEL_STORAGE, value);
  };

  // AI Chat Submit
  const handleSendChat = async (textToSend: string) => {
    const query = textToSend.trim();
    if (!query) return;

    // Add user message
    const userMsg: ChatMessage = {
      sender: 'user',
      text: query,
      timestamp: new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
    };
    setChatHistory(prev => [...prev, userMsg]);
    setInputMsg('');
    setAiLoading(true);

    try {
      const response = await askAI(query, apiKey);
      
      // Determine if suggesting to add a node based on responses
      let suggestionNode: ChatMessage['suggestionNode'] = undefined;
      if (query.toLowerCase().includes('gargalo') || query.toLowerCase().includes('analisar')) {
        const hasConfirm = activeProject.nodes.some(n => n.label.toLowerCase().includes('confirm'));
        if (!hasConfirm) {
          suggestionNode = {
            label: 'Confirmar Documentação',
            type: 'decision',
            description: 'Validação e conferência dos documentos e guias do convênio para faturamento.'
          };
        }
      }

      setChatHistory(prev => [...prev, {
        sender: 'ai',
        text: response,
        timestamp: new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }),
        suggestionNode
      }]);
    } catch (err: any) {
      setChatHistory(prev => [...prev, {
        sender: 'ai',
        text: `⚠️ Erro ao processar: ${err.message || 'Erro na requisição. Verifique seus tokens ou conexão.'}`,
        timestamp: new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
      }]);
    } finally {
      setAiLoading(false);
    }
  };

  // Automated Quick Node Insertion
  const handleApplyAISuggestion = (sug: NonNullable<ChatMessage['suggestionNode']>) => {
    if (!spendTokens(5, `Adicionar etapa sugerida pela IA (${sug.label})`)) return;

    // Find a good position
    const nodesSorted = [...activeProject.nodes].sort((a, b) => a.y - b.y);
    const nodeIndexToInsert = nodesSorted.findIndex(n => n.label.toLowerCase().includes('cadast'));
    
    let x = 100;
    let y = 300;
    
    if (nodeIndexToInsert > -1) {
      const reference = nodesSorted[nodeIndexToInsert];
      x = reference.x;
      y = reference.y + 110;
      
      // Push subsequent nodes down
      activeProject.nodes.forEach(n => {
        if (n.y >= y) {
          updateNode(n.id, { y: n.y + 130 });
        }
      });
    }

    const created = addNode(sug.type, sug.label, x, y);
    if (created) {
      setTimeout(() => {
        updateNode(created.id, { description: sug.description, responsible: 'Recepção' });
        
        // Reconnect edges
        if (nodeIndexToInsert > -1) {
          const prevNode = nodesSorted[nodeIndexToInsert];
          // Delete existing edge from prevNode to nextNode if any
          const nextNode = nodesSorted[nodeIndexToInsert + 1];
          if (nextNode) {
            const edgeToDelete = activeProject.edges.find(e => e.source === prevNode.id && e.target === nextNode.id);
            if (edgeToDelete) {
              deleteEdge(edgeToDelete.id);
            }
            // Connect prevNode -> created
            addEdge(prevNode.id, created.id);
            // Connect created -> nextNode
            addEdge(created.id, nextNode.id);
          }
        }
      }, 100);
      
      alert(`Etapa "${sug.label}" adicionada e conectada ao fluxo automaticamente!`);
    }
  };

  // Sub-tabs in Docs
  const renderDocsTab = () => {
    const sequentialNodes = [...activeProject.nodes].sort((a, b) => a.y - b.y);

    return (
      <div style={{ display: 'flex', flexDirection: 'column', height: '100%', gap: '14px' }}>
        {/* Doc Tab selectors */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '4px', background: 'rgba(255,255,255,0.02)', padding: '3px', borderRadius: '6px' }}>
          <button 
            onClick={() => setDocSubTab('pop')}
            style={{
              padding: '6px', fontSize: '11px', fontWeight: 600, border: 'none', borderRadius: '4px', cursor: 'pointer',
              background: docSubTab === 'pop' ? 'rgba(6, 182, 212, 0.15)' : 'transparent',
              color: docSubTab === 'pop' ? 'var(--primary)' : 'var(--text-muted)'
            }}
          >
            POP
          </button>
          <button 
            onClick={() => setDocSubTab('raci')}
            style={{
              padding: '6px', fontSize: '11px', fontWeight: 600, border: 'none', borderRadius: '4px', cursor: 'pointer',
              background: docSubTab === 'raci' ? 'rgba(6, 182, 212, 0.15)' : 'transparent',
              color: docSubTab === 'raci' ? 'var(--primary)' : 'var(--text-muted)'
            }}
          >
            RACI
          </button>
          <button 
            onClick={() => setDocSubTab('check')}
            style={{
              padding: '6px', fontSize: '11px', fontWeight: 600, border: 'none', borderRadius: '4px', cursor: 'pointer',
              background: docSubTab === 'check' ? 'rgba(6, 182, 212, 0.15)' : 'transparent',
              color: docSubTab === 'check' ? 'var(--primary)' : 'var(--text-muted)'
            }}
          >
            Checklists
          </button>
        </div>

        {/* Content area */}
        <div className="glass-panel" style={{ flex: 1, padding: '16px', fontSize: '12px', overflowY: 'auto', background: 'rgba(0,0,0,0.2)', maxHeight: '350px' }}>
          {docSubTab === 'pop' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', lineHeight: '1.4' }}>
              <span style={{ fontSize: '13px', fontWeight: 700, borderBottom: '1px solid var(--border-color)', paddingBottom: '6px' }}>
                Procedimento Operacional Padrão (POP)
              </span>
              <p><strong>Objetivo:</strong> Mapear e padronizar o processo de {activeProject.name}.</p>
              <p><strong>Responsabilidade Geral:</strong> {activeProject.category}.</p>
              
              <div style={{ marginTop: '8px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {sequentialNodes.map((n, i) => (
                  <div key={n.id} style={{ borderLeft: '2px solid rgba(255,255,255,0.08)', paddingLeft: '8px' }}>
                    <span style={{ fontWeight: 600, color: 'var(--primary)' }}>Etapa {i+1}: {n.label}</span>
                    <p style={{ color: 'var(--text-muted)', fontSize: '11px', marginTop: '2px' }}>{n.description || 'Instrução pendente de detalhamento.'}</p>
                    <span style={{ fontSize: '10px', color: 'var(--text-dark)', display: 'block', marginTop: '2px' }}>
                      Executor: {n.responsible || 'Sem cargo atribuído'}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {docSubTab === 'raci' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <span style={{ fontSize: '13px', fontWeight: 700, borderBottom: '1px solid var(--border-color)', paddingBottom: '6px' }}>
                Matriz RACI de Responsabilidades
              </span>
              <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                R: Executor, A: Aprovador final, C: Consultado, I: Informado.
              </p>
              <div style={{ overflowX: 'auto', marginTop: '6px' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '11px' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--primary)' }}>
                      <th style={{ padding: '6px 4px' }}>Etapa (Entregável)</th>
                      <th style={{ padding: '6px 4px' }}>Executor (R)</th>
                      <th style={{ padding: '6px 4px' }}>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {sequentialNodes.map(n => (
                      <tr key={n.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.03)' }}>
                        <td style={{ padding: '8px 4px', fontWeight: 500 }}>{n.label}</td>
                        <td style={{ padding: '8px 4px', color: 'var(--text-muted)' }}>{n.responsible || 'Pendente'}</td>
                        <td style={{ padding: '8px 4px' }}>
                          <span className={`badge ${n.responsible ? 'badge-approved' : 'badge-draft'}`} style={{ fontSize: '9px', padding: '1px 4px' }}>
                            {n.responsible ? 'Configurado' : 'Aviso'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {docSubTab === 'check' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <span style={{ fontSize: '13px', fontWeight: 700, borderBottom: '1px solid var(--border-color)', paddingBottom: '6px' }}>
                Checklist Consolidado de Atividades
              </span>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '6px' }}>
                {sequentialNodes.filter(n => n.checklist && n.checklist.length > 0).map(n => (
                  <div key={n.id}>
                    <span style={{ fontWeight: 600, fontSize: '11px', color: 'var(--primary)' }}>{n.label}</span>
                    <ul style={{ listStyleType: 'none', paddingLeft: '6px', marginTop: '4px', display: 'flex', flexDirection: 'column', gap: '4px' }}>
                      {n.checklist?.map(item => (
                        <li key={item.id} style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '11px', color: item.done ? 'var(--text-muted)' : 'var(--text-main)' }}>
                          <input type="checkbox" checked={item.done} readOnly style={{ accentColor: 'var(--primary)' }} />
                          <span style={{ textDecoration: item.done ? 'line-through' : 'none' }}>{item.text}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                ))}
                {sequentialNodes.filter(n => n.checklist && n.checklist.length > 0).length === 0 && (
                  <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '20px' }}>
                    Nenhuma etapa do fluxo possui itens de checklist de qualidade configurados.
                  </p>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <aside className="glass-panel" style={{
      width: '360px',
      display: 'flex',
      flexDirection: 'column',
      height: 'calc(100vh - 75px)',
      borderRadius: '0',
      borderRight: 'none',
      borderTop: 'none',
      borderBottom: 'none',
      backgroundColor: 'rgba(10, 15, 36, 0.4)'
    }}>
      {/* Sidebar Tabs */}
      <div style={{
        display: 'flex',
        borderBottom: '1px solid var(--border-color)',
        background: 'rgba(0,0,0,0.1)'
      }}>
        {[
          { id: 'ia', label: 'IA', icon: <Sparkles size={14} /> },
          { id: 'docs', label: 'Docs', icon: <FileText size={14} /> },
          { id: 'workflow', label: 'Workflow', icon: <CheckCircle size={14} /> },
          { id: 'versions', label: 'Histórico', icon: <GitBranch size={14} /> },
          { id: 'audit', label: 'Log', icon: <History size={14} /> }
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id as any)}
            style={{
              flex: 1,
              padding: '12px 0',
              background: activeTab === tab.id ? 'rgba(6, 182, 212, 0.08)' : 'transparent',
              border: 'none',
              borderBottom: '2px solid',
              borderBottomColor: activeTab === tab.id ? 'var(--primary)' : 'transparent',
              color: activeTab === tab.id ? 'var(--primary)' : 'var(--text-muted)',
              fontSize: '12px',
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '4px',
              transition: 'all var(--transition-fast)'
            }}
          >
            {tab.icon}
            {tab.label}
          </button>
        ))}
      </div>

      {/* Tab Contents Container */}
      <div style={{ flex: 1, padding: '20px', overflowY: 'auto', display: 'flex', flexDirection: 'column' }}>
        
        {/* IA Tab */}
        {activeTab === 'ia' && (
          <div style={{ display: 'flex', flexDirection: 'column', height: '100%', minHeight: '380px' }}>
            {/* Configuração do Gemini: chave + modelo */}
            <div style={{ marginBottom: '12px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <button
                onClick={() => setShowKeyInput(!showKeyInput)}
                className="glass-btn"
                style={{ fontSize: '11px', padding: '4px 8px', width: '100%', justifyContent: 'center' }}
              >
                <Key size={12} />
                {apiKey ? 'Gemini configurado (chave ativa)' : 'Configurar Gemini API Key'}
              </button>

              {/* Seletor de modelo (sempre visível, em forma de lista) */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '3px' }}>
                <label style={{ fontSize: '10px', color: 'var(--text-dark)', textTransform: 'uppercase', letterSpacing: '0.03em' }}>
                  Modelo do Gemini
                </label>
                <select
                  className="glass-input"
                  value={aiModel}
                  onChange={(e) => handleModelChange(e.target.value)}
                  style={{ fontSize: '11px', padding: '6px 8px', appearance: 'none', background: 'rgba(255,255,255,0.03)' }}
                >
                  {GEMINI_MODELS.map((m) => (
                    <option key={m.id} value={m.id}>{m.label}</option>
                  ))}
                </select>
              </div>

              {showKeyInput && (
                <div className="glass-panel" style={{ padding: '10px', marginTop: '2px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  <div style={{ display: 'flex', gap: '6px' }}>
                    <input
                      type="password"
                      className="glass-input"
                      style={{ flex: 1, fontSize: '11px', padding: '6px' }}
                      placeholder="AIza... (Google AI Studio)"
                      value={apiKey}
                      onChange={(e) => setApiKey(e.target.value)}
                    />
                    <button
                      onClick={handleSaveKey}
                      className="glass-btn glass-btn-primary"
                      style={{ padding: '6px 10px', fontSize: '11px' }}
                    >
                      Salvar
                    </button>
                  </div>
                  <span style={{ fontSize: '10px', color: 'var(--text-dark)', lineHeight: 1.4 }}>
                    Gere sua chave grátis em <strong>aistudio.google.com/app/apikey</strong>. Fica salva só neste navegador.
                  </span>
                </div>
              )}
            </div>

            {/* Chat Box */}
            <div className="glass-panel" style={{
              flex: 1,
              backgroundColor: 'rgba(0,0,0,0.15)',
              borderRadius: '8px',
              padding: '12px',
              overflowY: 'auto',
              maxHeight: '260px',
              display: 'flex',
              flexDirection: 'column',
              gap: '10px',
              marginBottom: '12px'
            }}>
              {chatHistory.map((msg, i) => (
                <div key={i} style={{
                  alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                  maxWidth: '85%',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '4px'
                }}>
                  <div style={{
                    padding: '8px 12px',
                    borderRadius: '8px',
                    fontSize: '12px',
                    lineHeight: '1.4',
                    backgroundColor: msg.sender === 'user' ? 'rgba(139, 92, 246, 0.15)' : 'rgba(255, 255, 255, 0.03)',
                    border: '1px solid',
                    borderColor: msg.sender === 'user' ? 'rgba(139, 92, 246, 0.3)' : 'var(--border-color)',
                    color: 'var(--text-main)',
                    whiteSpace: 'pre-wrap'
                  }}>
                    {msg.text}
                  </div>
                  
                  {/* Action Suggestion button if available */}
                  {msg.suggestionNode && (
                    <div className="glass-panel" style={{ padding: '8px', marginTop: '4px', backgroundColor: 'rgba(6, 182, 212, 0.05)', display: 'flex', flexDirection: 'column', gap: '6px' }}>
                      <span style={{ fontSize: '10px', color: 'var(--primary)', fontWeight: 700 }}>💡 RECOMENDAÇÃO DE FLUXO</span>
                      <span style={{ fontSize: '11px', fontWeight: 600 }}>Adicionar: "{msg.suggestionNode.label}"?</span>
                      <button 
                        onClick={() => handleApplyAISuggestion(msg.suggestionNode!)}
                        className="glass-btn glass-btn-primary" 
                        style={{ padding: '4px 8px', fontSize: '10px', borderRadius: '4px' }}
                      >
                        <Plus size={10} />
                        Aplicar Correção
                      </button>
                    </div>
                  )}

                  <span style={{ fontSize: '9px', color: 'var(--text-dark)', alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start' }}>
                    {msg.timestamp}
                  </span>
                </div>
              ))}

              {aiLoading && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '11px', color: 'var(--text-muted)' }}>
                  <RefreshCw size={12} className="animate-pulse-slow" style={{ animation: 'spin 2s linear infinite' }} />
                  <span>UIFlux IA analisando processo...</span>
                </div>
              )}
              <div ref={chatEndRef} />
            </div>

            {/* Quick Actions Prompts */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '6px', marginBottom: '12px' }}>
              <button 
                onClick={() => handleSendChat('Analisar gargalos operacionais e qualidade')} 
                className="glass-btn" 
                style={{ fontSize: '10px', padding: '6px 8px', justifyContent: 'center' }}
                disabled={aiLoading}
              >
                <Sparkles size={11} color="var(--primary)" />
                Analisar Gargalos (8tk)
              </button>
              <button 
                onClick={() => handleSendChat('Gerar minuta de POP detalhada')} 
                className="glass-btn" 
                style={{ fontSize: '10px', padding: '6px 8px', justifyContent: 'center' }}
                disabled={aiLoading}
              >
                <FileText size={11} color="#10b981" />
                Gerar POP (10tk)
              </button>
            </div>

            {/* Input message form */}
            <form onSubmit={(e) => { e.preventDefault(); handleSendChat(inputMsg); }} style={{ display: 'flex', gap: '8px' }}>
              <input
                type="text"
                className="glass-input"
                style={{ flex: 1, fontSize: '12px' }}
                placeholder="Escreva sua pergunta para a IA..."
                value={inputMsg}
                onChange={(e) => setInputMsg(e.target.value)}
                disabled={aiLoading}
              />
              <button type="submit" className="glass-btn glass-btn-primary" style={{ padding: '8px' }} disabled={aiLoading}>
                <Send size={14} />
              </button>
            </form>
          </div>
        )}

        {/* Docs Tab */}
        {activeTab === 'docs' && renderDocsTab()}

        {/* Workflow Tab */}
        {activeTab === 'workflow' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <span style={{ fontSize: '13px', fontWeight: 700, borderBottom: '1px solid var(--border-color)', paddingBottom: '6px' }}>
              Histórico de Aprovação (Workflow)
            </span>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', position: 'relative', paddingLeft: '16px', borderLeft: '1px solid var(--border-color)' }}>
              
              <div style={{ position: 'relative' }}>
                <div style={{ position: 'absolute', left: '-21px', top: '2px', width: '9px', height: '9px', borderRadius: '50%', background: activeProject.status === 'draft' ? 'var(--primary)' : 'var(--border-hover)' }}></div>
                <strong style={{ fontSize: '12px', display: 'block' }}>Etapa 1: Rascunho Inicial</strong>
                <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Responsável: Criador do fluxo</span>
              </div>

              <div style={{ position: 'relative' }}>
                <div style={{ position: 'absolute', left: '-21px', top: '2px', width: '9px', height: '9px', borderRadius: '50%', background: activeProject.status === 'developing' ? '#60a5fa' : 'var(--border-hover)' }}></div>
                <strong style={{ fontSize: '12px', display: 'block' }}>Etapa 2: Em Desenvolvimento</strong>
                <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Membros colaborando e desenhando o diagrama</span>
              </div>

              <div style={{ position: 'relative' }}>
                <div style={{ position: 'absolute', left: '-21px', top: '2px', width: '9px', height: '9px', borderRadius: '50%', background: activeProject.status === 'review' ? '#fbbf24' : 'var(--border-hover)' }}></div>
                <strong style={{ fontSize: '12px', display: 'block' }}>Etapa 3: Revisão por Pares</strong>
                <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Aguardando validação técnica e clínica</span>
              </div>

              <div style={{ position: 'relative' }}>
                <div style={{ position: 'absolute', left: '-21px', top: '2px', width: '9px', height: '9px', borderRadius: '50%', background: activeProject.status === 'approved' ? '#34d399' : 'var(--border-hover)' }}></div>
                <strong style={{ fontSize: '12px', display: 'block' }}>Etapa 4: Homologado & Aprovado</strong>
                <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Aprovado pelo gestor de Qualidade / PMO</span>
              </div>

              <div style={{ position: 'relative' }}>
                <div style={{ position: 'absolute', left: '-21px', top: '2px', width: '9px', height: '9px', borderRadius: '50%', background: activeProject.status === 'published' ? '#a78bfa' : 'var(--border-hover)' }}></div>
                <strong style={{ fontSize: '12px', display: 'block' }}>Etapa 5: Publicado Corporativo</strong>
                <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Publicado nos manuais institucionais</span>
              </div>
            </div>

            <div className="glass-panel" style={{ padding: '12px', fontSize: '11px', marginTop: '10px' }}>
              <span style={{ fontWeight: 600, color: 'var(--primary)' }}>Alterar Status Atual</span>
              <p style={{ color: 'var(--text-muted)', marginTop: '4px', marginBottom: '8px' }}>
                Utilize o botão de status na barra superior do cabeçalho para progredir no workflow.
              </p>
            </div>
          </div>
        )}

        {/* Versions Tab */}
        {activeTab === 'versions' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: '13px', fontWeight: 700 }}>Histórico de Versões</span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', maxHeight: '350px', overflowY: 'auto' }}>
              {activeProject.versions.map((ver, i) => (
                <div 
                  key={i} 
                  className="glass-panel"
                  style={{
                    padding: '12px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '6px',
                    backgroundColor: 'rgba(255, 255, 255, 0.02)'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <strong style={{ color: 'var(--secondary)', fontSize: '13px' }}>v{ver.version}</strong>
                    <span style={{ fontSize: '10px', color: 'var(--text-dark)' }}>
                      {new Date(ver.date).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })}
                    </span>
                  </div>
                  <p style={{ fontSize: '11px', color: 'var(--text-muted)', lineHeight: '1.3' }}>
                    {ver.description}
                  </p>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid rgba(255,255,255,0.03)', paddingTop: '6px', marginTop: '2px' }}>
                    <span style={{ fontSize: '10px', color: 'var(--text-dark)' }}>Autor: {ver.author}</span>
                    <button
                      onClick={() => rollbackToVersion(ver.version)}
                      className="glass-btn"
                      style={{ padding: '3px 8px', fontSize: '10px', borderRadius: '4px', color: 'var(--primary)', borderColor: 'rgba(6, 182, 212, 0.3)' }}
                    >
                      Restaurar
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Audit Tab */}
        {activeTab === 'audit' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <span style={{ fontSize: '13px', fontWeight: 700 }}>Logs de Auditoria (LGPD / ONA)</span>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', maxHeight: '380px', overflowY: 'auto' }}>
              {activeProject.auditLogs.map((log, i) => (
                <div 
                  key={i} 
                  style={{
                    padding: '8px 10px',
                    borderRadius: '6px',
                    backgroundColor: 'rgba(255, 255, 255, 0.01)',
                    border: '1px solid rgba(255,255,255,0.04)',
                    fontSize: '11px',
                    lineHeight: '1.3'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '2px' }}>
                    <strong style={{ color: 'var(--primary)' }}>{log.action}</strong>
                    <span style={{ fontSize: '9px', color: 'var(--text-dark)' }}>
                      {new Date(log.date).toLocaleTimeString('pt-BR')}
                    </span>
                  </div>
                  <span style={{ color: 'var(--text-muted)', display: 'block' }}>{log.details}</span>
                  <span style={{ color: 'var(--text-dark)', fontSize: '9px', display: 'block', marginTop: '2px', textAlign: 'right' }}>
                    Executor: {log.user}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}

      </div>
    </aside>
  );
};
