import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import type { Project } from '../types';
import { 
  Plus, Search, Sparkles, Building, Coins, LogOut, 
  Trash2, Folder, Users, GitBranch 
} from 'lucide-react';

export const Dashboard: React.FC = () => {
  const { 
    user, logout, addTokens, projects, selectProject, createProject, deleteProject 
  } = useApp();

  const [searchTerm, setSearchTerm] = useState('');
  const [activeCategory, setActiveCategory] = useState('Todos');
  const [showModal, setShowModal] = useState(false);
  
  // Form State
  const [projName, setProjName] = useState('');
  const [projDesc, setProjDesc] = useState('');
  const [projCat, setProjCat] = useState('Qualidade & Processos');
  const [projTemplate, setProjTemplate] = useState<'flowchart' | 'organogram'>('flowchart');

  // Categories list
  const categories = ['Todos', 'Saúde & Hospitais', 'Qualidade & Processos', 'Tecnologia', 'Financeiro'];

  // Filtered projects
  const filteredProjects = projects.filter(p => {
    const matchesSearch = p.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
                          p.description.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCat = activeCategory === 'Todos' || p.category === activeCategory;
    return matchesSearch && matchesCat;
  });

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!projName.trim()) return;

    createProject(projName, projDesc, projCat, projTemplate);
    
    // Reset form
    setProjName('');
    setProjDesc('');
    setProjCat('Qualidade & Processos');
    setProjTemplate('flowchart');
    setShowModal(false);
  };

  const getStatusBadge = (status: Project['status']) => {
    switch (status) {
      case 'draft': return <span className="badge badge-draft">Rascunho</span>;
      case 'developing': return <span className="badge badge-dev">Em Desenv.</span>;
      case 'review': return <span className="badge badge-review">Revisão</span>;
      case 'approved': return <span className="badge badge-approved">Aprovado</span>;
      case 'published': return <span className="badge badge-published">Publicado</span>;
      default: return null;
    }
  };

  return (
    <div style={{ minHeight: '100vh', padding: '30px 40px' }}>
      {/* Top Header */}
      <header style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '40px',
        borderBottom: '1px solid var(--border-color)',
        paddingBottom: '20px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{
            width: '36px',
            height: '36px',
            borderRadius: '8px',
            background: 'linear-gradient(135deg, var(--primary), var(--secondary))',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <Sparkles size={18} color="#fff" />
          </div>
          <div>
            <h1 style={{ fontSize: '22px', fontWeight: 700, letterSpacing: '-0.02em' }}>UIFlux Workspace</h1>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--text-muted)' }}>
              <Building size={12} />
              <span>{user.company}</span>
            </div>
          </div>
        </div>

        {/* User tokens & logout */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          {/* Token Indicator */}
          <div className="glass-panel" style={{
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            padding: '8px 16px',
            borderRadius: '30px',
            background: 'rgba(6, 182, 212, 0.05)',
            borderColor: 'rgba(6, 182, 212, 0.2)'
          }}>
            <Coins size={16} color="var(--primary)" />
            <span style={{ fontSize: '13px', fontWeight: 600 }}>
              <span className="text-gradient-cyan">{user.tokens}</span> Tokens
            </span>
            <button 
              onClick={() => addTokens(100)} 
              className="glass-btn glass-btn-primary" 
              style={{ padding: '4px 10px', fontSize: '11px', borderRadius: '20px' }}
            >
              Recarregar +100
            </button>
          </div>

          {/* User Profile */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{
              width: '34px',
              height: '34px',
              borderRadius: '50%',
              background: 'rgba(255, 255, 255, 0.05)',
              border: '1px solid var(--border-color)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '13px',
              fontWeight: 600
            }}>
              {user.email ? user.email.slice(0, 2).toUpperCase() : 'UI'}
            </div>
            <div style={{ display: 'none', flexDirection: 'column' }} className="md:flex">
              <span style={{ fontSize: '13px', fontWeight: 500 }}>{user.email}</span>
            </div>
            <button 
              onClick={logout} 
              className="glass-btn" 
              style={{ padding: '8px', borderRadius: '50%', color: 'var(--error)', borderColor: 'rgba(239, 68, 68, 0.2)' }}
              title="Sair"
            >
              <LogOut size={14} />
            </button>
          </div>
        </div>
      </header>

      {/* Overview Analytics Cards */}
      <section style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
        gap: '20px',
        marginBottom: '40px'
      }}>
        <div className="glass-panel" style={{ padding: '20px' }}>
          <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Projetos Criados</span>
          <div style={{ fontSize: '28px', fontWeight: 700, marginTop: '8px', color: '#fff' }}>
            {projects.length}
          </div>
        </div>
        <div className="glass-panel" style={{ padding: '20px' }}>
          <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Membros na Equipe</span>
          <div style={{ fontSize: '28px', fontWeight: 700, marginTop: '8px', color: 'var(--primary)' }}>
            {Array.from(new Set(projects.flatMap(p => p.team))).length}
          </div>
        </div>
        <div className="glass-panel" style={{ padding: '20px' }}>
          <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Versões Registradas</span>
          <div style={{ fontSize: '28px', fontWeight: 700, marginTop: '8px', color: 'var(--secondary)' }}>
            {projects.reduce((acc, p) => acc + p.versions.length, 0)}
          </div>
        </div>
        <div className="glass-panel" style={{ padding: '20px' }}>
          <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Ações de Auditoria</span>
          <div style={{ fontSize: '28px', fontWeight: 700, marginTop: '8px', color: 'var(--success)' }}>
            {projects.reduce((acc, p) => acc + p.auditLogs.length, 0)}
          </div>
        </div>
      </section>

      {/* Main Workspace Area */}
      <main className="glass-panel" style={{ padding: '24px', minHeight: '400px' }}>
        {/* Toolbar */}
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '16px',
          marginBottom: '24px'
        }}>
          {/* Search bar */}
          <div style={{ position: 'relative', width: '100%', maxWidth: '320px' }}>
            <Search size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            <input 
              type="text" 
              className="glass-input" 
              style={{ width: '100%', paddingLeft: '40px' }} 
              placeholder="Buscar projetos..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          {/* New Project Button */}
          <button 
            onClick={() => setShowModal(true)} 
            className="glass-btn glass-btn-primary"
            style={{ padding: '10px 20px', borderRadius: '8px' }}
          >
            <Plus size={16} />
            Novo Projeto
          </button>
        </div>

        {/* Categories Tab selector */}
        <div style={{
          display: 'flex',
          gap: '8px',
          overflowX: 'auto',
          paddingBottom: '12px',
          marginBottom: '24px',
          borderBottom: '1px solid rgba(255, 255, 255, 0.05)'
        }}>
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setActiveCategory(cat)}
              className="glass-btn"
              style={{
                borderRadius: '20px',
                padding: '6px 16px',
                fontSize: '13px',
                backgroundColor: activeCategory === cat ? 'rgba(6, 182, 212, 0.15)' : 'transparent',
                borderColor: activeCategory === cat ? 'var(--primary)' : 'var(--border-color)',
                color: activeCategory === cat ? 'var(--primary)' : 'var(--text-muted)'
              }}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Projects Grid */}
        {filteredProjects.length === 0 ? (
          <div style={{
            textAlign: 'center',
            padding: '80px 20px',
            color: 'var(--text-muted)'
          }}>
            <Folder size={48} style={{ opacity: 0.3, marginBottom: '16px', color: 'var(--text-muted)' }} />
            <h3 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-main)' }}>Nenhum projeto encontrado</h3>
            <p style={{ fontSize: '13px', marginTop: '6px' }}>
              Tente redefinir sua busca ou crie um novo fluxo inteligente.
            </p>
          </div>
        ) : (
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
            gap: '20px'
          }}>
            {filteredProjects.map((project) => (
              <div 
                key={project.id} 
                className="glass-panel glass-panel-hover" 
                style={{
                  padding: '24px',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  minHeight: '200px',
                  cursor: 'pointer',
                  position: 'relative'
                }}
                onClick={() => selectProject(project.id)}
              >
                {/* Upper info */}
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                    <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--primary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                      {project.category}
                    </span>
                    {getStatusBadge(project.status)}
                  </div>
                  <h3 style={{ fontSize: '17px', fontWeight: 700, color: '#fff', marginBottom: '8px' }}>
                    {project.name}
                  </h3>
                  <p style={{ 
                    fontSize: '13px', 
                    color: 'var(--text-muted)', 
                    lineHeight: '1.4',
                    display: '-webkit-box',
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden',
                    marginBottom: '16px'
                  }}>
                    {project.description}
                  </p>
                </div>

                {/* Bottom stats bar */}
                <div style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  paddingTop: '16px',
                  borderTop: '1px solid rgba(255,255,255,0.05)',
                  fontSize: '12px',
                  color: 'var(--text-muted)'
                }}>
                  <div style={{ display: 'flex', gap: '14px' }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }} title="Versão Atual">
                      <GitBranch size={13} />
                      v{project.version}
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }} title="Membros da equipe">
                      <Users size={13} />
                      {project.team.length}
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }} title="Quantidade de Etapas/Nós">
                      <Folder size={13} />
                      {project.nodes.length}
                    </span>
                  </div>

                  {/* Actions (Delete only if not clinical sample to maintain template) */}
                  <button 
                    onClick={(e) => {
                      e.stopPropagation();
                      if (confirm(`Tem certeza de que deseja excluir o projeto "${project.name}"?`)) {
                        deleteProject(project.id);
                      }
                    }}
                    className="glass-btn" 
                    style={{ 
                      padding: '6px', 
                      borderRadius: '4px',
                      color: 'var(--text-dark)', 
                      borderColor: 'transparent',
                      background: 'transparent'
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.color = 'var(--error)'}
                    onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-dark)'}
                    title="Excluir Projeto"
                  >
                    <Trash2 size={13} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Modal Criar Projeto */}
      {showModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(3, 7, 18, 0.8)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '20px'
        }} onClick={() => setShowModal(false)}>
          <div className="glass-panel animate-fade-in" style={{
            width: '100%',
            maxWidth: '520px',
            padding: '30px',
            backgroundColor: '#0a0f24',
            position: 'relative'
          }} onClick={(e) => e.stopPropagation()}>
            <h2 style={{ fontSize: '20px', fontWeight: 700, marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Plus size={20} color="var(--primary)" />
              Novo Projeto de Processo
            </h2>

            <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Nome do Projeto</label>
                <input 
                  type="text" 
                  className="glass-input" 
                  placeholder="Ex: Admissão de Pacientes ou Organograma Operacional"
                  value={projName}
                  onChange={(e) => setProjName(e.target.value)}
                  required 
                />
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Descrição do Objetivo</label>
                <textarea 
                  className="glass-input" 
                  style={{ minHeight: '80px', resize: 'vertical' }}
                  placeholder="Qual problema este processo visa mapear ou resolver?"
                  value={projDesc}
                  onChange={(e) => setProjDesc(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Categoria</label>
                <select 
                  className="glass-input"
                  value={projCat}
                  onChange={(e) => setProjCat(e.target.value)}
                  style={{ appearance: 'none', background: 'rgba(255,255,255,0.03)' }}
                >
                  <option value="Saúde & Hospitais">Saúde & Hospitais</option>
                  <option value="Qualidade & Processos">Qualidade & Processos</option>
                  <option value="Tecnologia">Tecnologia</option>
                  <option value="Financeiro">Financeiro</option>
                </select>
              </div>

              {/* Template Choice Cards */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Estrutura & Custo de Criação</label>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  {/* Option 1: Flowchart */}
                  <div 
                    onClick={() => setProjTemplate('flowchart')}
                    style={{
                      border: '1px solid',
                      borderColor: projTemplate === 'flowchart' ? 'var(--primary)' : 'var(--border-color)',
                      backgroundColor: projTemplate === 'flowchart' ? 'rgba(6, 182, 212, 0.05)' : 'transparent',
                      padding: '16px',
                      borderRadius: '8px',
                      cursor: 'pointer',
                      transition: 'all var(--transition-fast)'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                      <span style={{ fontWeight: 600, fontSize: '14px' }}>Fluxograma</span>
                      <span style={{ fontSize: '11px', color: 'var(--primary)', fontWeight: 600 }}>15 Tokens</span>
                    </div>
                    <p style={{ fontSize: '11px', color: 'var(--text-muted)', lineHeight: '1.3' }}>
                      Mapeamento linear sequencial ideal para processos operacionais.
                    </p>
                  </div>

                  {/* Option 2: Organogram */}
                  <div 
                    onClick={() => setProjTemplate('organogram')}
                    style={{
                      border: '1px solid',
                      borderColor: projTemplate === 'organogram' ? 'var(--secondary)' : 'var(--border-color)',
                      backgroundColor: projTemplate === 'organogram' ? 'rgba(139, 92, 246, 0.05)' : 'transparent',
                      padding: '16px',
                      borderRadius: '8px',
                      cursor: 'pointer',
                      transition: 'all var(--transition-fast)'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                      <span style={{ fontWeight: 600, fontSize: '14px' }}>Organograma</span>
                      <span style={{ fontSize: '11px', color: 'var(--secondary)', fontWeight: 600 }}>20 Tokens</span>
                    </div>
                    <p style={{ fontSize: '11px', color: 'var(--text-muted)', lineHeight: '1.3' }}>
                      Mapeamento de relações hierárquicas e times da empresa.
                    </p>
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div style={{ display: 'flex', gap: '10px', marginTop: '10px', justifyContent: 'flex-end' }}>
                <button 
                  type="button" 
                  className="glass-btn" 
                  onClick={() => setShowModal(false)}
                >
                  Cancelar
                </button>
                <button 
                  type="submit" 
                  className="glass-btn glass-btn-primary"
                >
                  Confirmar e Debitar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
