import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { ArrowLeft, Coins, Download, Save, GitBranch, ChevronDown } from 'lucide-react';
import type { ProjectStatus } from '../../types';

export const Header: React.FC = () => {
  const { 
    activeProject, selectProject, updateProjectStatus, user, saveVersion 
  } = useApp();

  const [showStatusMenu, setShowStatusMenu] = useState(false);
  const [showExportMenu, setShowExportMenu] = useState(false);
  const [showVersionModal, setShowVersionModal] = useState(false);
  const [versionNote, setVersionNote] = useState('');

  if (!activeProject) return null;

  const statusOptions: { value: ProjectStatus; label: string; desc: string; color: string }[] = [
    { value: 'draft', label: 'Rascunho', desc: 'Edição inicial livre', color: 'var(--text-muted)' },
    { value: 'developing', label: 'Em Desenvolvimento', desc: 'Trabalho ativo da equipe', color: '#60a5fa' },
    { value: 'review', label: 'Em Revisão', desc: 'Aguardando validações', color: '#fbbf24' },
    { value: 'approved', label: 'Aprovado', desc: 'Revisado e validado', color: '#34d399' },
    { value: 'published', label: 'Publicado', desc: 'Disponível para a corporação', color: '#a78bfa' }
  ];

  const currentStatus = statusOptions.find(o => o.value === activeProject.status) || statusOptions[0];

  // Helper function to download file in browser
  const downloadFile = (filename: string, content: string, contentType: string) => {
    const blob = new Blob([content], { type: contentType });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  const handleExportJSON = () => {
    const dataStr = JSON.stringify(activeProject, null, 2);
    downloadFile(`${activeProject.name.toLowerCase().replace(/\s+/g, '_')}_workflow.json`, dataStr, 'application/json');
    setShowExportMenu(false);
  };

  const handleExportMarkdown = () => {
    // Generate full POP text
    let doc = `# POP - Procedimento Operacional Padrão\n\n`;
    doc += `## PROCESSO: ${activeProject.name}\n`;
    doc += `**Objetivo**: ${activeProject.description}\n`;
    doc += `**Categoria**: ${activeProject.category} | **Versão**: v${activeProject.version}\n`;
    doc += `**Atualizado em**: ${new Date(activeProject.updatedAt).toLocaleDateString('pt-BR')}\n\n`;
    
    doc += `### 1. FLUXO DAS ETAPAS OPERACIONAIS\n\n`;
    activeProject.nodes.forEach((node, i) => {
      doc += `#### Etapa ${i + 1}: ${node.label} (${node.type.toUpperCase()})\n`;
      doc += `- **Descrição**: ${node.description || 'Não detalhada.'}\n`;
      doc += `- **Responsável**: ${node.responsible || 'Não definido'}\n`;
      if (node.checklist && node.checklist.length > 0) {
        doc += `- **Sub-tarefas (Checklist)**:\n`;
        node.checklist.forEach(item => {
          doc += `  - [${item.done ? 'x' : ' '}] ${item.text}\n`;
        });
      }
      doc += `\n`;
    });

    doc += `---\n\n`;
    doc += `### 2. MATRIZ RACI (RESPONSABILIDADES)\n\n`;
    doc += `| Etapa | Responsável (R) | Aprovador (A) | Consultado (C) | Informado (I) |\n`;
    doc += `| :--- | :--- | :--- | :--- | :--- |\n`;
    activeProject.nodes.forEach(node => {
      doc += `| ${node.label} | ${node.responsible || 'Sem responsável'} | Gestor Qualidade | Diretoria | Equipe Operacional |\n`;
    });

    doc += `\n\n*Documento gerado automaticamente por UIFlux. Status atual: ${activeProject.status.toUpperCase()}*`;

    downloadFile(`${activeProject.name.toLowerCase().replace(/\s+/g, '_')}_pop.md`, doc, 'text/markdown');
    setShowExportMenu(false);
  };

  const handleExportSVG = () => {
    // Basic SVG export
    let svgContent = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 800" width="100%" height="100%" style="background:#0a0f24; font-family:sans-serif;">`;
    
    // Draw edges first
    activeProject.edges.forEach(edge => {
      const sourceNode = activeProject.nodes.find(n => n.id === edge.source);
      const targetNode = activeProject.nodes.find(n => n.id === edge.target);
      if (sourceNode && targetNode) {
        svgContent += `<line x1="${sourceNode.x + 85}" y1="${sourceNode.y + 35}" x2="${targetNode.x + 85}" y2="${targetNode.y + 35}" stroke="#06b6d4" stroke-width="2" marker-end="url(#arrow)" />`;
      }
    });

    // Arrow marker Definition
    svgContent += `<defs><marker id="arrow" viewBox="0 0 10 10" refX="6" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse"><path d="M 0 0 L 10 5 L 0 10 z" fill="#06b6d4"/></marker></defs>`;

    // Draw nodes
    activeProject.nodes.forEach(node => {
      const isDecision = node.type === 'decision';
      const color = isDecision ? '#f59e0b' : '#06b6d4';
      
      if (isDecision) {
        // Draw diamond
        const cx = node.x + 85;
        const cy = node.y + 35;
        svgContent += `<polygon points="${cx},${cy - 35} ${cx + 85},${cy} ${cx},${cy + 35} ${cx - 85},${cy}" fill="#111827" stroke="${color}" stroke-width="2" />`;
      } else {
        // Draw rounded rect
        svgContent += `<rect x="${node.x}" y="${node.y}" width="170" height="70" rx="8" fill="#111827" stroke="${color}" stroke-width="2" />`;
      }
      
      // Node text
      svgContent += `<text x="${node.x + 85}" y="${node.y + 40}" fill="#fff" font-size="11" font-weight="bold" text-anchor="middle">${node.label}</text>`;
    });

    svgContent += `</svg>`;
    downloadFile(`${activeProject.name.toLowerCase().replace(/\s+/g, '_')}_diagrama.svg`, svgContent, 'image/svg+xml');
    setShowExportMenu(false);
  };

  const handlePrint = () => {
    window.print();
    setShowExportMenu(false);
  };

  const handleSaveVersion = (e: React.FormEvent) => {
    e.preventDefault();
    if (!versionNote.trim()) return;

    saveVersion(versionNote);
    setVersionNote('');
    setShowVersionModal(false);
  };

  return (
    <header style={{
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      padding: '16px 24px',
      background: 'rgba(10, 15, 36, 0.8)',
      backdropFilter: 'blur(10px)',
      borderBottom: '1px solid var(--border-color)',
      position: 'relative',
      zIndex: 50
    }}>
      {/* Back & Title */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <button 
          onClick={() => selectProject(null)} 
          className="glass-btn" 
          style={{ padding: '8px', borderRadius: '8px' }}
        >
          <ArrowLeft size={16} />
        </button>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <h1 style={{ fontSize: '18px', fontWeight: 700 }}>{activeProject.name}</h1>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)', background: 'rgba(255,255,255,0.05)', padding: '2px 8px', borderRadius: '4px' }}>
              v{activeProject.version}
            </span>
          </div>
          <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
            Categoria: {activeProject.category}
          </span>
        </div>
      </div>

      {/* Middle Operations & Flow details */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        {/* Token Balance */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--text-muted)', borderRight: '1px solid var(--border-color)', paddingRight: '16px' }}>
          <Coins size={14} color="var(--primary)" />
          <span>Saldo: <strong style={{ color: 'var(--text-main)' }}>{user.tokens}</strong></span>
        </div>

        {/* Workflow State Dropdown */}
        <div style={{ position: 'relative' }}>
          <button 
            onClick={() => setShowStatusMenu(!showStatusMenu)}
            className="glass-btn"
            style={{ 
              borderRadius: '8px', 
              fontSize: '13px', 
              padding: '6px 12px',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              borderLeftWidth: '3px',
              borderLeftColor: currentStatus.color
            }}
          >
            Workflow: <strong style={{ color: currentStatus.color }}>{currentStatus.label}</strong>
            <ChevronDown size={14} />
          </button>

          {showStatusMenu && (
            <>
              <div 
                style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, zIndex: 10 }} 
                onClick={() => setShowStatusMenu(false)}
              />
              <div className="glass-panel" style={{
                position: 'absolute',
                top: '100%',
                right: 0,
                marginTop: '8px',
                width: '260px',
                padding: '8px',
                zIndex: 20,
                backgroundColor: '#0a0f24',
                display: 'flex',
                flexDirection: 'column',
                gap: '4px'
              }}>
                <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-muted)', padding: '6px 8px', textTransform: 'uppercase' }}>
                  Aprovação do Processo
                </span>
                {statusOptions.map(option => (
                  <button
                    key={option.value}
                    onClick={() => {
                      updateProjectStatus(option.value);
                      setShowStatusMenu(false);
                    }}
                    style={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'flex-start',
                      padding: '8px 12px',
                      borderRadius: '6px',
                      background: activeProject.status === option.value ? 'rgba(255,255,255,0.03)' : 'transparent',
                      border: 'none',
                      textAlign: 'left',
                      cursor: 'pointer',
                      width: '100%'
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.05)'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = activeProject.status === option.value ? 'rgba(255,255,255,0.03)' : 'transparent'}
                  >
                    <span style={{ fontSize: '13px', fontWeight: 600, color: option.color }}>
                      {option.label}
                    </span>
                    <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                      {option.desc}
                    </span>
                  </button>
                ))}
              </div>
            </>
          )}
        </div>

        {/* Save Version Button */}
        <button 
          onClick={() => setShowVersionModal(true)}
          className="glass-btn" 
          style={{ fontSize: '13px', padding: '6px 12px', display: 'flex', alignItems: 'center', gap: '6px' }}
          title="Congelar e Criar Nova Versão (Custa 5 tokens)"
        >
          <GitBranch size={14} color="var(--secondary)" />
          Versão
        </button>

        {/* Export Dropdown */}
        <div style={{ position: 'relative' }}>
          <button 
            onClick={() => setShowExportMenu(!showExportMenu)}
            className="glass-btn glass-btn-primary"
            style={{ fontSize: '13px', padding: '6px 14px', display: 'flex', alignItems: 'center', gap: '6px' }}
          >
            <Download size={14} />
            Exportar
            <ChevronDown size={12} />
          </button>

          {showExportMenu && (
            <>
              <div 
                style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, zIndex: 10 }} 
                onClick={() => setShowExportMenu(false)}
              />
              <div className="glass-panel" style={{
                position: 'absolute',
                top: '100%',
                right: 0,
                marginTop: '8px',
                width: '180px',
                padding: '6px',
                zIndex: 20,
                backgroundColor: '#0a0f24',
                display: 'flex',
                flexDirection: 'column',
                gap: '2px'
              }}>
                <button
                  onClick={handleExportMarkdown}
                  style={{
                    padding: '8px 12px',
                    borderRadius: '6px',
                    background: 'transparent',
                    border: 'none',
                    textAlign: 'left',
                    color: 'var(--text-main)',
                    fontSize: '13px',
                    cursor: 'pointer',
                    width: '100%'
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.05)'}
                  onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                >
                  Documento POP (.md)
                </button>
                <button
                  onClick={handleExportSVG}
                  style={{
                    padding: '8px 12px',
                    borderRadius: '6px',
                    background: 'transparent',
                    border: 'none',
                    textAlign: 'left',
                    color: 'var(--text-main)',
                    fontSize: '13px',
                    cursor: 'pointer',
                    width: '100%'
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.05)'}
                  onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                >
                  Diagrama Vetorial (.svg)
                </button>
                <button
                  onClick={handleExportJSON}
                  style={{
                    padding: '8px 12px',
                    borderRadius: '6px',
                    background: 'transparent',
                    border: 'none',
                    textAlign: 'left',
                    color: 'var(--text-main)',
                    fontSize: '13px',
                    cursor: 'pointer',
                    width: '100%'
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.05)'}
                  onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                >
                  Dados Backup (.json)
                </button>
                <button
                  onClick={handlePrint}
                  style={{
                    padding: '8px 12px',
                    borderRadius: '6px',
                    background: 'transparent',
                    border: 'none',
                    textAlign: 'left',
                    color: 'var(--text-main)',
                    fontSize: '13px',
                    cursor: 'pointer',
                    width: '100%'
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.05)'}
                  onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                >
                  Imprimir / PDF
                </button>
              </div>
            </>
          )}
        </div>
      </div>

      {/* Modal Version Note */}
      {showVersionModal && (
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
        }} onClick={() => setShowVersionModal(false)}>
          <div className="glass-panel animate-fade-in" style={{
            width: '100%',
            maxWidth: '440px',
            padding: '24px',
            backgroundColor: '#0a0f24',
          }} onClick={(e) => e.stopPropagation()}>
            <h2 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Save size={18} color="var(--secondary)" />
              Registrar Nova Versão
            </h2>
            <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '16px', lineHeight: '1.4' }}>
              Esta ação congelará a estrutura de etapas atual, salvando como uma nova versão histórica para fins de backup e rollback. Custo: <strong>5 tokens</strong>.
            </p>

            <form onSubmit={handleSaveVersion} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Nota de Alteração (Changelog)</label>
                <input 
                  type="text" 
                  className="glass-input" 
                  placeholder="Ex: Adicionado validação de guias e triagem clínica"
                  value={versionNote}
                  onChange={(e) => setVersionNote(e.target.value)}
                  required 
                  autoFocus
                />
              </div>

              <div style={{ display: 'flex', gap: '10px', marginTop: '10px', justifyContent: 'flex-end' }}>
                <button 
                  type="button" 
                  className="glass-btn" 
                  onClick={() => setShowVersionModal(false)}
                >
                  Cancelar
                </button>
                <button 
                  type="submit" 
                  className="glass-btn glass-btn-primary"
                  style={{ background: 'linear-gradient(135deg, var(--secondary), #a78bfa)' }}
                >
                  Debitar 5 Tokens & Salvar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </header>
  );
};
