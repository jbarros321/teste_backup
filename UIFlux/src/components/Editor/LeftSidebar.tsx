import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { Plus, ArrowUp, ArrowDown, Trash2, Edit2, Check, Sparkles } from 'lucide-react';

export const LeftSidebar: React.FC = () => {
  const { activeProject, addNode, addEdge, syncStepList, deleteNode } = useApp();
  const [newStepText, setNewStepText] = useState('');
  const [editingNodeId, setEditingNodeId] = useState<string | null>(null);
  const [editLabel, setEditLabel] = useState('');

  if (!activeProject) return null;

  // Filter nodes that are part of the main sequential flow (we sort by Y coordinate to get the logical list order)
  const sequentialNodes = [...activeProject.nodes].sort((a, b) => a.y - b.y);

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && newStepText.trim()) {
      e.preventDefault();
      
      const label = newStepText.trim();
      const lastNode = sequentialNodes[sequentialNodes.length - 1];
      
      // Calculate coordinates (align linear flowchart below last node)
      const x = lastNode ? lastNode.x : 100;
      const y = lastNode ? lastNode.y + 130 : 100;
      
      // 1. Cria Node
      // 2. Cria ID
      // 3. Cria posição
      const type = label.toLowerCase().includes('?') || label.toLowerCase().includes('se ') ? 'decision' : 'process';
      const created = addNode(type, label, x, y);
      
      // 4. Liga ao anterior
      if (lastNode && created) {
        // Wait a small timeout to let state update, or connect directly
        setTimeout(() => {
          addEdge(lastNode.id, created.id);
        }, 50);
      }

      setNewStepText('');
    }
  };

  const handleMoveUp = (index: number) => {
    if (index === 0) return;
    const items = [...sequentialNodes];
    // Swap items
    const temp = items[index];
    items[index] = items[index - 1];
    items[index - 1] = temp;
    
    // Sincronizar nova lista de labels
    syncStepList(items.map(n => n.label));
  };

  const handleMoveDown = (index: number) => {
    if (index === sequentialNodes.length - 1) return;
    const items = [...sequentialNodes];
    // Swap items
    const temp = items[index];
    items[index] = items[index + 1];
    items[index + 1] = temp;
    
    // Sincronizar nova lista de labels
    syncStepList(items.map(n => n.label));
  };

  const startEditing = (nodeId: string, currentLabel: string) => {
    setEditingNodeId(nodeId);
    setEditLabel(currentLabel);
  };

  const saveEdit = (nodeId: string) => {
    if (!editLabel.trim()) return;
    
    // Reorganizar através do sync com a lista atualizada
    const updated = sequentialNodes.map(n => {
      if (n.id === nodeId) {
        return { ...n, label: editLabel.trim() };
      }
      return n;
    });

    syncStepList(updated.map(n => n.label));
    setEditingNodeId(null);
  };

  return (
    <aside className="glass-panel" style={{
      width: '320px',
      display: 'flex',
      flexDirection: 'column',
      height: 'calc(100vh - 75px)',
      borderRadius: '0',
      borderLeft: 'none',
      borderTop: 'none',
      borderBottom: 'none',
      backgroundColor: 'rgba(10, 15, 36, 0.4)',
      overflow: 'hidden'
    }}>
      {/* Quick Creator Header */}
      <div style={{
        padding: '20px',
        borderBottom: '1px solid var(--border-color)',
        display: 'flex',
        flexDirection: 'column',
        gap: '12px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Sparkles size={16} color="var(--primary)" className="animate-pulse-slow" />
          <h2 style={{ fontSize: '14px', fontWeight: 700, letterSpacing: '0.02em', textTransform: 'uppercase', color: 'var(--primary)' }}>
            Fluxo Inteligente
          </h2>
        </div>
        <p style={{ fontSize: '11px', color: 'var(--text-muted)', lineHeight: '1.4' }}>
          Digite o nome da etapa e dê <strong>ENTER</strong> para gerar e conectar automaticamente o nó na tela.
        </p>

        <div style={{ position: 'relative' }}>
          <input
            type="text"
            className="glass-input"
            style={{ width: '100%', paddingRight: '36px', fontSize: '13px' }}
            placeholder="Ex: Receber Pedido..."
            value={newStepText}
            onChange={(e) => setNewStepText(e.target.value)}
            onKeyDown={handleKeyPress}
          />
          <Plus size={16} style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-dark)' }} />
        </div>
      </div>

      {/* Index List Container */}
      <div style={{
        flex: 1,
        overflowY: 'auto',
        padding: '16px',
        display: 'flex',
        flexDirection: 'column',
        gap: '10px'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
          <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
            Etapas do Processo ({sequentialNodes.length})
          </span>
        </div>

        {sequentialNodes.map((node, index) => (
          <div 
            key={node.id} 
            className="glass-panel"
            style={{
              padding: '10px 12px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              gap: '10px',
              backgroundColor: 'rgba(255, 255, 255, 0.02)',
              borderLeft: '3px solid var(--primary)',
              borderRadius: '6px',
              borderColor: node.type === 'decision' ? 'var(--warning)' : 'var(--primary)'
            }}
          >
            {/* Left side info */}
            <div style={{ flex: 1, minWidth: 0 }}>
              {editingNodeId === node.id ? (
                <div style={{ display: 'flex', gap: '4px', alignItems: 'center' }}>
                  <input
                    type="text"
                    className="glass-input"
                    style={{ fontSize: '12px', padding: '4px 8px', width: '100%' }}
                    value={editLabel}
                    onChange={(e) => setEditLabel(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && saveEdit(node.id)}
                    autoFocus
                  />
                  <button 
                    onClick={() => saveEdit(node.id)}
                    style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: 'var(--success)' }}
                  >
                    <Check size={14} />
                  </button>
                </div>
              ) : (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-dark)', fontFamily: 'var(--font-mono)' }}>
                    {(index + 1).toString().padStart(2, '0')}
                  </span>
                  <span style={{ 
                    fontSize: '13px', 
                    fontWeight: 500, 
                    color: 'var(--text-main)',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                    display: 'block'
                  }}>
                    {node.label}
                  </span>
                </div>
              )}
            </div>

            {/* Reorder & Action Controls */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <button 
                onClick={() => handleMoveUp(index)}
                disabled={index === 0}
                style={{ 
                  border: 'none', 
                  background: 'transparent', 
                  cursor: index === 0 ? 'not-allowed' : 'pointer',
                  color: index === 0 ? 'var(--text-dark)' : 'var(--text-muted)'
                }}
                title="Subir Etapa"
              >
                <ArrowUp size={13} />
              </button>
              <button 
                onClick={() => handleMoveDown(index)}
                disabled={index === sequentialNodes.length - 1}
                style={{ 
                  border: 'none', 
                  background: 'transparent', 
                  cursor: index === sequentialNodes.length - 1 ? 'not-allowed' : 'pointer',
                  color: index === sequentialNodes.length - 1 ? 'var(--text-dark)' : 'var(--text-muted)'
                }}
                title="Descer Etapa"
              >
                <ArrowDown size={13} />
              </button>
              <button 
                onClick={() => startEditing(node.id, node.label)}
                style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: 'var(--text-muted)' }}
                title="Renomear Etapa"
              >
                <Edit2 size={12} />
              </button>
              <button 
                onClick={() => deleteNode(node.id)}
                style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: 'rgba(239, 68, 68, 0.6)' }}
                title="Deletar Etapa"
              >
                <Trash2 size={12} />
              </button>
            </div>
          </div>
        ))}
      </div>
    </aside>
  );
};
