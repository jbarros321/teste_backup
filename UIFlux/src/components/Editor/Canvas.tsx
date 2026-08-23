import React, { useState, useRef } from 'react';
import { useApp } from '../../context/AppContext';
import type { FlowNode, NodeType } from '../../types';
import {
  ZoomIn, ZoomOut, Maximize, Link, Trash2,
  Copy, Layers, User, CheckSquare, X, Wand2, MousePointerClick
} from 'lucide-react';

export const Canvas: React.FC = () => {
  const {
    activeProject, addNode, updateNode, deleteNode, addEdge, deleteEdge, syncStepList, generateFlowSteps
  } = useApp();

  const canvasRef = useRef<HTMLDivElement>(null);
  
  // Viewport State
  const [zoom, setZoom] = useState(1);
  const [pan, setPan] = useState({ x: 0, y: 0 });
  const [isPanning, setIsPanning] = useState(false);
  const [panStart, setPanStart] = useState({ x: 0, y: 0 });

  // Dragging Node State
  const [draggedNodeId, setDraggedNodeId] = useState<string | null>(null);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });

  // Connection Builder State
  const [connectingSourceId, setConnectingSourceId] = useState<string | null>(null);

  // Selected / Editing Node State (for Detail Drawer)
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
  const [showDrawer, setShowDrawer] = useState(false);

  // Checklist input
  const [newCheckItem, setNewCheckItem] = useState('');

  // Criação inline de etapa direto no canvas (duplo-clique → digitar → Enter)
  const [creator, setCreator] = useState<{ x: number; y: number } | null>(null);
  const [creatorText, setCreatorText] = useState('');
  const [lastCreatedId, setLastCreatedId] = useState<string | null>(null);

  // Gerador de fluxo a partir de texto
  const [showTextGen, setShowTextGen] = useState(false);
  const [genText, setGenText] = useState('');
  const [generating, setGenerating] = useState(false);

  if (!activeProject) return null;

  const selectedNode = activeProject.nodes.find(n => n.id === selectedNodeId) || null;

  // Zoom handlers
  const handleZoomIn = () => setZoom(prev => Math.min(prev + 0.1, 1.8));
  const handleZoomOut = () => setZoom(prev => Math.max(prev - 0.1, 0.5));
  const handleCenter = () => {
    setZoom(1);
    setPan({ x: 0, y: 0 });
  };

  // Canvas Panning Handlers
  const handleMouseDown = (e: React.MouseEvent) => {
    // If clicking on node or link button, don't pan
    if ((e.target as HTMLElement).closest('.flow-node-card') || (e.target as HTMLElement).closest('.canvas-btn')) {
      return;
    }
    setIsPanning(true);
    setPanStart({ x: e.clientX - pan.x, y: e.clientY - pan.y });
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (isPanning) {
      setPan({
        x: e.clientX - panStart.x,
        y: e.clientY - panStart.y
      });
    } else if (draggedNodeId) {
      // Calculate node coordinate based on mouse position and zoom
      const canvasBounding = canvasRef.current?.getBoundingClientRect();
      if (!canvasBounding) return;
      
      const mouseXInCanvas = e.clientX - canvasBounding.left - pan.x;
      const mouseYInCanvas = e.clientY - canvasBounding.top - pan.y;

      const newX = Math.round((mouseXInCanvas / zoom) - dragOffset.x);
      const newY = Math.round((mouseYInCanvas / zoom) - dragOffset.y);

      updateNode(draggedNodeId, { x: newX, y: newY });
    }
  };

  const handleMouseUp = () => {
    setIsPanning(false);
    setDraggedNodeId(null);
  };

  // Node Dragging Handlers
  const handleNodeDragStart = (e: React.MouseEvent, node: FlowNode) => {
    e.stopPropagation();
    setDraggedNodeId(node.id);
    
    // Calculate drag offset (mouse offset from node origin)
    const canvasBounding = canvasRef.current?.getBoundingClientRect();
    if (!canvasBounding) return;

    const mouseXInCanvas = e.clientX - canvasBounding.left - pan.x;
    const mouseYInCanvas = e.clientY - canvasBounding.top - pan.y;

    setDragOffset({
      x: (mouseXInCanvas / zoom) - node.x,
      y: (mouseYInCanvas / zoom) - node.y
    });
  };

  // Converte coordenadas da tela para coordenadas do canvas (considerando pan/zoom)
  const screenToCanvas = (clientX: number, clientY: number) => {
    const b = canvasRef.current?.getBoundingClientRect();
    if (!b) return { x: 100, y: 100 };
    return {
      x: Math.round((clientX - b.left - pan.x) / zoom),
      y: Math.round((clientY - b.top - pan.y) / zoom),
    };
  };

  // Duplo-clique em área vazia abre o criador inline de etapa
  const handleCanvasDoubleClick = (e: React.MouseEvent) => {
    const el = e.target as HTMLElement;
    if (el.closest('.flow-node-card') || el.closest('.canvas-btn') || el.closest('.glass-panel')) return;
    const pos = screenToCanvas(e.clientX, e.clientY);
    setCreator(pos);
    setCreatorText('');
    setLastCreatedId(null);
  };

  // Salva a etapa digitada, contorna como nó e prepara o próximo passo (criação em cadeia)
  const commitCreator = () => {
    const label = creatorText.trim();
    if (!label || !creator) {
      setCreator(null);
      return;
    }
    const type: NodeType = /\?|^se\s|caso|decis/i.test(label) ? 'decision' : 'process';
    const created = addNode(type, label, creator.x, creator.y);
    if (created) {
      if (lastCreatedId) {
        const src = lastCreatedId;
        setTimeout(() => addEdge(src, created.id), 30);
      }
      setLastCreatedId(created.id);
      // desce o campo para o próximo passo e mantém o foco
      setCreator({ x: creator.x, y: creator.y + 130 });
      setCreatorText('');
    }
  };

  // Gera um fluxo inteiro a partir de um texto — a IA analisa o texto antes (quando há chave)
  const handleGenerateFromText = async () => {
    const raw = genText.trim();
    if (!raw || generating) return;

    setGenerating(true);
    try {
      const { steps, usedAI } = await generateFlowSteps(raw);
      if (steps.length === 0) {
        alert('Não consegui identificar etapas nesse texto. Tente descrever uma etapa por linha.');
        return;
      }
      syncStepList(steps);
      setShowTextGen(false);
      setGenText('');
      if (!usedAI) {
        // Sem chave de IA: foi montado pela análise local
        console.info('[UIFlux] Fluxo gerado por análise local (sem IA).');
      }
    } catch (err: any) {
      alert(err?.message || 'Erro ao gerar o fluxo.');
    } finally {
      setGenerating(false);
    }
  };

  // Connecting Logic
  const handleConnectStart = (nodeId: string) => {
    setConnectingSourceId(nodeId);
  };

  const handleConnectTarget = (targetId: string) => {
    if (connectingSourceId && connectingSourceId !== targetId) {
      addEdge(connectingSourceId, targetId);
    }
    setConnectingSourceId(null);
  };

  // Duplicate node
  const handleDuplicateNode = (node: FlowNode) => {
    const label = `${node.label} (Cópia)`;
    const created = addNode(node.type, label, node.x + 50, node.y + 50);
    if (created && node.responsible) {
      setTimeout(() => {
        updateNode(created.id, {
          description: node.description,
          responsible: node.responsible,
          checklist: node.checklist ? [...node.checklist] : []
        });
      }, 50);
    }
  };

  // Checklist Actions
  const handleAddChecklist = () => {
    if (!newCheckItem.trim() || !selectedNode) return;
    const items = selectedNode.checklist || [];
    const updated = [
      ...items,
      { id: `check-${Date.now()}`, text: newCheckItem.trim(), done: false }
    ];
    updateNode(selectedNode.id, { checklist: updated });
    setNewCheckItem('');
  };

  const handleToggleCheck = (itemId: string) => {
    if (!selectedNode) return;
    const updated = (selectedNode.checklist || []).map(item => 
      item.id === itemId ? { ...item, done: !item.done } : item
    );
    updateNode(selectedNode.id, { checklist: updated });
  };

  const handleDeleteCheck = (itemId: string) => {
    if (!selectedNode) return;
    const updated = (selectedNode.checklist || []).filter(item => item.id !== itemId);
    updateNode(selectedNode.id, { checklist: updated });
  };

  // Node UI rendering helper
  const getNodeColor = (type: NodeType) => {
    switch (type) {
      case 'process': return { border: 'var(--primary)', glow: 'rgba(6, 182, 212, 0.15)' };
      case 'decision': return { border: 'var(--warning)', glow: 'rgba(245, 158, 11, 0.15)' };
      case 'document': return { border: '#10b981', glow: 'rgba(16, 185, 129, 0.15)' };
      case 'subprocess': return { border: 'var(--secondary)', glow: 'rgba(139, 92, 246, 0.15)' };
      case 'organogram': return { border: '#3b82f6', glow: 'rgba(59, 130, 246, 0.15)' };
      case 'note': return { border: '#eab308', glow: 'rgba(234, 179, 8, 0.15)' };
      default: return { border: 'rgba(255,255,255,0.2)', glow: 'transparent' };
    }
  };

  return (
    <div style={{ flex: 1, display: 'flex', position: 'relative', overflow: 'hidden' }}>
      {/* Canvas Viewport */}
      <div 
        ref={canvasRef}
        className="flow-canvas"
        style={{
          flex: 1,
          height: 'calc(100vh - 75px)',
          cursor: isPanning ? 'grabbing' : 'default',
          position: 'relative',
          overflow: 'hidden',
          backgroundColor: 'var(--bg-darker)'
        }}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onDoubleClick={handleCanvasDoubleClick}
      >
        {/* Inner Canvas that transforms (Pan & Zoom) */}
        <div style={{
          transform: `translate(${pan.x}px, ${pan.y}px) scale(${zoom})`,
          transformOrigin: '0 0',
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          pointerEvents: 'none' // Elements inside will restore pointerEvents
        }}>
          {/* SVG Connections Layer */}
          <svg style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '4000px',
            height: '4000px',
            zIndex: 1
          }}>
            <defs>
              <marker 
                id="arrowhead" 
                viewBox="0 0 10 10" 
                refX="8" 
                refY="5" 
                markerWidth="6" 
                markerHeight="6" 
                orient="auto-start-reverse"
              >
                <path d="M 0 1.5 L 8 5 L 0 8.5 z" fill="rgba(255, 255, 255, 0.35)" />
              </marker>
            </defs>

            {/* Connecting line helper preview */}
            {connectingSourceId && (() => {
              const srcNode = activeProject.nodes.find(n => n.id === connectingSourceId);
              if (!srcNode) return null;
              return (
                <line 
                  x1={srcNode.x + 85} 
                  y1={srcNode.y + 40} 
                  x2={srcNode.x + 85} // Dynamic line is difficult since mouse is relative, we just show indicator
                  y2={srcNode.y + 100}
                  stroke="var(--primary)" 
                  strokeWidth="2" 
                  strokeDasharray="4"
                />
              );
            })()}

            {/* Render Edges */}
            {activeProject.edges.map((edge) => {
              const src = activeProject.nodes.find(n => n.id === edge.source);
              const tgt = activeProject.nodes.find(n => n.id === edge.target);
              if (!src || !tgt) return null;

              // Calculate start and end coordinates
              const x1 = src.x + 85;
              const y1 = src.y + 40;
              const x2 = tgt.x + 85;
              const y2 = tgt.y + 40;

              // Draw path (cubic bezier or simple lines)
              const dx = Math.abs(x2 - x1);
              const dy = Math.abs(y2 - y1);
              let pathStr = `M ${x1} ${y1} L ${x2} ${y2}`;
              
              if (dy > dx && y2 > y1) {
                // S-curve vertical
                pathStr = `M ${x1} ${y1} C ${x1} ${y1 + dy/2}, ${x2} ${y2 - dy/2}, ${x2} ${y2}`;
              } else if (dx > dy) {
                // S-curve horizontal
                pathStr = `M ${x1} ${y1} C ${x1 + dx/2} ${y1}, ${x2 - dx/2} ${y2}, ${x2} ${y2}`;
              }

              return (
                <g key={edge.id} style={{ pointerEvents: 'auto' }}>
                  {/* Invisible thick line for easier clicking to delete connection */}
                  <path 
                    d={pathStr} 
                    stroke="transparent" 
                    strokeWidth="10" 
                    style={{ cursor: 'pointer' }}
                    onClick={() => {
                      if (confirm('Deseja excluir esta conexão?')) {
                        deleteEdge(edge.id);
                      }
                    }}
                  />
                  {/* Visible line */}
                  <path 
                    d={pathStr} 
                    stroke="rgba(255, 255, 255, 0.2)" 
                    strokeWidth="2.5" 
                    fill="none"
                    markerEnd="url(#arrowhead)" 
                    style={{ transition: 'stroke var(--transition-fast)' }}
                    onMouseEnter={(e) => e.currentTarget.style.stroke = 'var(--primary)'}
                    onMouseLeave={(e) => e.currentTarget.style.stroke = 'rgba(255, 255, 255, 0.2)'}
                  />
                </g>
              );
            })}
          </svg>

          {/* Nodes Layer */}
          <div style={{ zIndex: 5, position: 'relative' }}>
            {activeProject.nodes.map((node) => {
              const colors = getNodeColor(node.type);
              const isSelected = selectedNodeId === node.id;
              const isConnectingSource = connectingSourceId === node.id;

              return (
                <div
                  key={node.id}
                  className="flow-node-card node-shadow"
                  style={{
                    position: 'absolute',
                    left: `${node.x}px`,
                    top: `${node.y}px`,
                    width: '180px',
                    minHeight: '80px',
                    pointerEvents: 'auto',
                    cursor: draggedNodeId === node.id ? 'grabbing' : 'grab',
                    border: '1px solid',
                    borderColor: isSelected ? 'var(--primary)' : isConnectingSource ? 'var(--secondary)' : colors.border,
                    borderRadius: node.type === 'subprocess' ? '12px' : '8px',
                    boxShadow: isSelected ? `0 0 15px ${colors.glow}` : 'none',
                    backgroundColor: node.type === 'note' ? '#fef08a' : '#0a0f24',
                    color: node.type === 'note' ? '#854d0e' : 'var(--text-main)',
                    padding: '12px',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                    userSelect: 'none',
                    transform: node.type === 'note' ? 'rotate(-2deg)' : 'none'
                  }}
                  onMouseDown={(e) => handleNodeDragStart(e, node)}
                  onDoubleClick={() => {
                    setSelectedNodeId(node.id);
                    setShowDrawer(true);
                  }}
                >
                  {/* Node Header */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '6px' }}>
                    <span style={{ 
                      fontSize: '9px', 
                      textTransform: 'uppercase', 
                      fontWeight: 700, 
                      color: node.type === 'note' ? '#a16207' : colors.border,
                      letterSpacing: '0.05em'
                    }}>
                      {node.type}
                    </span>
                    
                    {/* Inline connector target click */}
                    {connectingSourceId && !isConnectingSource && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleConnectTarget(node.id);
                        }}
                        className="canvas-btn"
                        style={{
                          background: 'var(--secondary)',
                          border: 'none',
                          color: '#fff',
                          padding: '2px 6px',
                          borderRadius: '4px',
                          fontSize: '9px',
                          fontWeight: 600,
                          cursor: 'pointer'
                        }}
                      >
                        Ligar Aqui
                      </button>
                    )}
                  </div>

                  {/* Title / Label */}
                  <h4 style={{ 
                    fontSize: '13px', 
                    fontWeight: 700, 
                    lineHeight: '1.3',
                    marginBottom: '4px',
                    wordBreak: 'break-word'
                  }}>
                    {node.label}
                  </h4>

                  {/* Responsible / Assignee Badge */}
                  {node.responsible && (
                    <div style={{ 
                      display: 'flex', 
                      alignItems: 'center', 
                      gap: '4px', 
                      fontSize: '10px', 
                      color: node.type === 'note' ? '#a16207' : 'var(--text-muted)',
                      marginTop: '4px'
                    }}>
                      <User size={10} />
                      <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {node.responsible}
                      </span>
                    </div>
                  )}

                  {/* Node action shortcuts */}
                  <div className="node-hover-actions" style={{
                    display: 'flex',
                    gap: '4px',
                    justifyContent: 'flex-end',
                    marginTop: '8px',
                    borderTop: node.type === 'note' ? '1px solid rgba(161, 98, 7, 0.15)' : '1px solid rgba(255,255,255,0.05)',
                    paddingTop: '6px'
                  }}>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleConnectStart(node.id);
                      }}
                      className="canvas-btn"
                      style={{ background: 'transparent', border: 'none', color: node.type === 'note' ? '#a16207' : 'var(--text-muted)', cursor: 'pointer', padding: '2px' }}
                      title="Criar Conexão"
                    >
                      <Link size={11} />
                    </button>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDuplicateNode(node);
                      }}
                      className="canvas-btn"
                      style={{ background: 'transparent', border: 'none', color: node.type === 'note' ? '#a16207' : 'var(--text-muted)', cursor: 'pointer', padding: '2px' }}
                      title="Duplicar Nó"
                    >
                      <Copy size={11} />
                    </button>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        if (confirm('Deseja excluir este nó?')) {
                          deleteNode(node.id);
                          if (selectedNodeId === node.id) {
                            setShowDrawer(false);
                          }
                        }
                      }}
                      className="canvas-btn"
                      style={{ background: 'transparent', border: 'none', color: node.type === 'note' ? '#a16207' : 'rgba(239, 68, 68, 0.6)', cursor: 'pointer', padding: '2px' }}
                      title="Excluir Nó"
                    >
                      <Trash2 size={11} />
                    </button>
                  </div>
                </div>
              );
            })}

            {/* Criador inline de etapa (duplo-clique no canvas) */}
            {creator && (
              <div style={{
                position: 'absolute',
                left: `${creator.x}px`,
                top: `${creator.y}px`,
                width: '180px',
                zIndex: 60,
                pointerEvents: 'auto'
              }}>
                <input
                  autoFocus
                  type="text"
                  className="glass-input"
                  value={creatorText}
                  onChange={(e) => setCreatorText(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') { e.preventDefault(); commitCreator(); }
                    else if (e.key === 'Escape') { setCreator(null); setCreatorText(''); }
                  }}
                  onBlur={() => { if (!creatorText.trim()) { setCreator(null); } }}
                  placeholder={`Passo ${activeProject.nodes.length + 1}… (Enter)`}
                  style={{
                    width: '100%',
                    fontSize: '13px',
                    fontWeight: 600,
                    padding: '12px',
                    border: '1px dashed var(--primary)',
                    background: '#0a0f24',
                    boxShadow: '0 0 15px rgba(6, 182, 212, 0.25)'
                  }}
                />
                <span style={{ fontSize: '9px', color: 'var(--text-dark)', display: 'block', marginTop: '2px' }}>
                  Enter para salvar • Esc para cancelar
                </span>
              </div>
            )}
          </div>
        </div>

        {/* Dica quando o canvas está vazio */}
        {activeProject.nodes.length === 0 && !creator && (
          <div style={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            textAlign: 'center',
            color: 'var(--text-dark)',
            pointerEvents: 'none',
            zIndex: 2
          }}>
            <MousePointerClick size={32} style={{ opacity: 0.4, marginBottom: '10px' }} />
            <p style={{ fontSize: '14px', fontWeight: 600 }}>Dê um duplo-clique para criar o Passo 1</p>
            <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '4px' }}>
              ou use “Gerar Fluxo por Texto” na paleta ao lado
            </p>
          </div>
        )}

        {/* Floating Canvas Controls (Zoom In, Zoom Out, Fit) */}
        <div className="glass-panel" style={{
          position: 'absolute',
          bottom: '20px',
          left: '20px',
          display: 'flex',
          gap: '8px',
          padding: '6px',
          borderRadius: '8px',
          zIndex: 10
        }}>
          <button onClick={handleZoomIn} className="glass-btn" style={{ padding: '6px', borderRadius: '4px' }} title="Aumentar Zoom">
            <ZoomIn size={14} />
          </button>
          <span style={{ fontSize: '11px', display: 'flex', alignItems: 'center', minWidth: '35px', justifyContent: 'center', fontWeight: 600 }}>
            {Math.round(zoom * 100)}%
          </span>
          <button onClick={handleZoomOut} className="glass-btn" style={{ padding: '6px', borderRadius: '4px' }} title="Diminuir Zoom">
            <ZoomOut size={14} />
          </button>
          <button onClick={handleCenter} className="glass-btn" style={{ padding: '6px', borderRadius: '4px' }} title="Centralizar Visualização">
            <Maximize size={14} />
          </button>
        </div>

        {/* Floating Node Spawner Box */}
        <div className="glass-panel" style={{
          position: 'absolute',
          top: '20px',
          left: '20px',
          display: 'flex',
          flexDirection: 'column',
          gap: '6px',
          padding: '10px',
          borderRadius: '8px',
          zIndex: 10
        }}>
          <span style={{ fontSize: '10px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '2px' }}>
            Inserir Nó
          </span>
          <button
            onClick={() => setShowTextGen(true)}
            className="glass-btn glass-btn-primary"
            style={{ fontSize: '11px', padding: '6px 8px', justifyContent: 'flex-start', marginBottom: '4px' }}
          >
            <Wand2 size={12} />
            Gerar Fluxo por Texto
          </button>
          <button
            onClick={() => addNode('process', 'Nova Atividade', 100 - pan.x, 100 - pan.y)}
            className="glass-btn" 
            style={{ fontSize: '11px', padding: '4px 8px', justifyContent: 'flex-start' }}
          >
            <Layers size={12} color="var(--primary)" />
            Processo
          </button>
          <button 
            onClick={() => addNode('decision', 'Decisão?', 100 - pan.x, 100 - pan.y)}
            className="glass-btn" 
            style={{ fontSize: '11px', padding: '4px 8px', justifyContent: 'flex-start' }}
          >
            <Layers size={12} color="var(--warning)" />
            Decisão
          </button>
          <button 
            onClick={() => addNode('document', 'Documento POP', 100 - pan.x, 100 - pan.y)}
            className="glass-btn" 
            style={{ fontSize: '11px', padding: '4px 8px', justifyContent: 'flex-start' }}
          >
            <Layers size={12} color="#10b981" />
            Documento
          </button>
          <button 
            onClick={() => addNode('subprocess', 'Subprocesso', 100 - pan.x, 100 - pan.y)}
            className="glass-btn" 
            style={{ fontSize: '11px', padding: '4px 8px', justifyContent: 'flex-start' }}
          >
            <Layers size={12} color="var(--secondary)" />
            Subprocesso
          </button>
          <button 
            onClick={() => addNode('note', 'Anotação', 100 - pan.x, 100 - pan.y)}
            className="glass-btn" 
            style={{ fontSize: '11px', padding: '4px 8px', justifyContent: 'flex-start' }}
          >
            <Layers size={12} color="#eab308" />
            Post-it Note
          </button>
        </div>
      </div>

      {/* Node detail drawer */}
      {showDrawer && selectedNode && (
        <div className="glass-panel animate-fade-in" style={{
          position: 'absolute',
          top: '10px',
          right: '10px',
          bottom: '10px',
          width: '340px',
          zIndex: 40,
          backgroundColor: '#0a0f24',
          borderRadius: '12px',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden'
        }}>
          {/* Header */}
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '16px 20px',
            borderBottom: '1px solid var(--border-color)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Layers size={16} color="var(--primary)" />
              <h3 style={{ fontSize: '15px', fontWeight: 700 }}>Editar Elemento</h3>
            </div>
            <button 
              onClick={() => setShowDrawer(false)}
              style={{ border: 'none', background: 'transparent', color: 'var(--text-muted)', cursor: 'pointer' }}
            >
              <X size={16} />
            </button>
          </div>

          {/* Drawer Body Scroll */}
          <div style={{ flex: 1, overflowY: 'auto', padding: '20px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
            
            {/* Label input */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Nome da Etapa</label>
              <input
                type="text"
                className="glass-input"
                value={selectedNode.label}
                onChange={(e) => updateNode(selectedNode.id, { label: e.target.value })}
              />
            </div>

            {/* Type selector */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Tipo do Nó</label>
              <select
                className="glass-input"
                value={selectedNode.type}
                onChange={(e) => updateNode(selectedNode.id, { type: e.target.value as NodeType })}
                style={{ appearance: 'none', background: 'rgba(255,255,255,0.03)' }}
              >
                <option value="process">Processo / Atividade</option>
                <option value="decision">Decisão / Bifurcação</option>
                <option value="document">Documento / Formulário</option>
                <option value="subprocess">Subprocesso</option>
                <option value="organogram">Organograma Card</option>
                <option value="note">Anotação / Post-it</option>
              </select>
            </div>

            {/* Responsible */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Cargo / Responsável (RACI)</label>
              <input
                type="text"
                className="glass-input"
                value={selectedNode.responsible || ''}
                onChange={(e) => updateNode(selectedNode.id, { responsible: e.target.value })}
                placeholder="Ex: Recepção, Faturamento"
              />
            </div>

            {/* Description */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Instrução de Trabalho (Descrição)</label>
              <textarea
                className="glass-input"
                style={{ minHeight: '80px', resize: 'vertical' }}
                value={selectedNode.description || ''}
                onChange={(e) => updateNode(selectedNode.id, { description: e.target.value })}
                placeholder="Detalhes operacionais específicos desta etapa..."
              />
            </div>

            {/* Checklist Editor */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <label style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <CheckSquare size={13} />
                Sub-tarefas (Checklist de Qualidade)
              </label>

              {/* Add checklist input */}
              <div style={{ display: 'flex', gap: '6px' }}>
                <input 
                  type="text" 
                  className="glass-input" 
                  style={{ flex: 1, fontSize: '12px', padding: '6px 10px' }} 
                  placeholder="Nova sub-tarefa..."
                  value={newCheckItem}
                  onChange={(e) => setNewCheckItem(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleAddChecklist()}
                />
                <button 
                  onClick={handleAddChecklist} 
                  className="glass-btn glass-btn-primary" 
                  style={{ padding: '6px 12px' }}
                >
                  Add
                </button>
              </div>

              {/* Checklist list */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', marginTop: '4px' }}>
                {(selectedNode.checklist || []).map(item => (
                  <div 
                    key={item.id} 
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      padding: '6px 8px',
                      borderRadius: '4px',
                      backgroundColor: 'rgba(255,255,255,0.02)',
                      border: '1px solid rgba(255,255,255,0.05)'
                    }}
                  >
                    <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '12px', flex: 1, minWidth: 0 }}>
                      <input 
                        type="checkbox" 
                        checked={item.done} 
                        onChange={() => handleToggleCheck(item.id)}
                        style={{ accentColor: 'var(--primary)' }}
                      />
                      <span style={{ 
                        textDecoration: item.done ? 'line-through' : 'none',
                        color: item.done ? 'var(--text-muted)' : 'var(--text-main)',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap'
                      }}>
                        {item.text}
                      </span>
                    </label>
                    <button 
                      onClick={() => handleDeleteCheck(item.id)}
                      style={{ border: 'none', background: 'transparent', color: 'rgba(239, 68, 68, 0.6)', cursor: 'pointer' }}
                    >
                      <X size={12} />
                    </button>
                  </div>
                ))}
              </div>
            </div>

          </div>
        </div>
      )}

      {/* Modal: Gerar Fluxo por Texto */}
      {showTextGen && (
        <div
          onClick={() => setShowTextGen(false)}
          style={{
            position: 'absolute', inset: 0, zIndex: 80,
            background: 'rgba(2, 6, 20, 0.6)', backdropFilter: 'blur(2px)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px'
          }}
        >
          <div
            className="glass-panel animate-fade-in"
            onClick={(e) => e.stopPropagation()}
            style={{
              width: '100%', maxWidth: '520px', padding: '24px',
              backgroundColor: '#0a0f24', borderRadius: '12px',
              display: 'flex', flexDirection: 'column', gap: '16px'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Wand2 size={18} color="var(--primary)" />
                <h3 style={{ fontSize: '16px', fontWeight: 700 }}>Gerar Fluxo por Texto</h3>
              </div>
              <button onClick={() => setShowTextGen(false)} style={{ border: 'none', background: 'transparent', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={18} />
              </button>
            </div>

            <p style={{ fontSize: '12px', color: 'var(--text-muted)', lineHeight: 1.5 }}>
              Descreva o processo com suas palavras (pode ser um texto corrido) ou liste as etapas.
              A <strong>IA analisa o texto</strong>, extrai as etapas na ordem certa e monta o fluxo já
              organizado em colunas. Perguntas (com “?”) viram nós de decisão.
            </p>
            <p style={{ fontSize: '11px', color: 'var(--text-dark)', marginTop: '-6px' }}>
              💡 A análise por IA usa sua chave do Gemini (aba IA) e consome créditos. Sem chave, a montagem é feita localmente (grátis).
            </p>

            <textarea
              autoFocus
              className="glass-input"
              value={genText}
              onChange={(e) => setGenText(e.target.value)}
              placeholder={'Ex:\n1. Receber pedido do cliente\n2. Conferir estoque\n3. Estoque disponível?\n4. Emitir nota fiscal\n5. Despachar mercadoria'}
              style={{ minHeight: '180px', resize: 'vertical', fontSize: '13px', lineHeight: 1.5, fontFamily: 'var(--font-mono)' }}
            />

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '10px' }}>
              <span style={{ fontSize: '11px', color: 'var(--text-dark)' }}>
                {genText.trim() ? `${genText.split(/\n+/).filter(l => l.trim()).length} linha(s) detectada(s)` : 'Nenhuma etapa ainda'}
              </span>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button onClick={() => setShowTextGen(false)} className="glass-btn" style={{ padding: '8px 14px', fontSize: '13px' }} disabled={generating}>
                  Cancelar
                </button>
                <button onClick={handleGenerateFromText} className="glass-btn glass-btn-primary" style={{ padding: '8px 16px', fontSize: '13px' }} disabled={!genText.trim() || generating}>
                  <Wand2 size={14} />
                  {generating ? 'Analisando…' : 'Gerar Fluxo'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
