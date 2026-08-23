import React from 'react';
import { useApp } from '../../context/AppContext';

export const PrintReport: React.FC = () => {
  const { activeProject } = useApp();

  if (!activeProject) return null;

  const sequentialNodes = [...activeProject.nodes].sort((a, b) => a.y - b.y);

  return (
    <div className="print-report" style={{ display: 'none' }}>
      {/* Cover/Header Block */}
      <div style={{ borderBottom: '3px solid #06b6d4', paddingBottom: '16px', marginBottom: '24px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <h1 style={{ fontSize: '26px', fontWeight: 700, margin: 0, color: '#111827' }}>
              POP - Procedimento Operacional Padrão
            </h1>
            <h2 style={{ fontSize: '18px', fontWeight: 600, margin: '8px 0 0 0', color: '#4b5563', border: 'none' }}>
              Processo: {activeProject.name}
            </h2>
          </div>
          <div style={{ textAlign: 'right', fontSize: '12px', color: '#6b7280' }}>
            <div><strong>Versão:</strong> {activeProject.version}</div>
            <div><strong>Status:</strong> {activeProject.status.toUpperCase()}</div>
            <div><strong>Atualizado:</strong> {new Date(activeProject.updatedAt).toLocaleDateString('pt-BR')}</div>
          </div>
        </div>
        <p style={{ marginTop: '16px', fontSize: '14px', color: '#374151', fontStyle: 'italic', lineHeight: '1.5' }}>
          <strong>Objetivo do Processo:</strong> {activeProject.description || 'Não detalhado.'}
        </p>
        <div style={{ marginTop: '8px', fontSize: '12px', color: '#4b5563' }}>
          <strong>Categoria:</strong> {activeProject.category} | <strong>Equipe:</strong> {activeProject.team.join(', ')}
        </div>
      </div>

      {/* 1. Schematic SVG Flow Diagram */}
      <div style={{ marginBottom: '30px', pageBreakInside: 'avoid' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700, borderBottom: '1px solid #e5e7eb', paddingBottom: '6px', marginBottom: '12px' }}>
          1. Diagrama de Fluxo do Processo
        </h2>
        <div style={{ 
          display: 'flex', 
          justifyContent: 'center', 
          padding: '16px', 
          border: '1px dashed #d1d5db', 
          borderRadius: '8px',
          backgroundColor: '#f9fafb'
        }}>
          <svg viewBox="0 0 800 120" width="100%" height="auto" style={{ maxHeight: '180px' }}>
            <defs>
              <marker id="print-arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
                <path d="M 0 1.5 L 8 5 L 0 8.5 z" fill="#374151" />
              </marker>
            </defs>

            {/* Horizontal flow line sequence */}
            {sequentialNodes.map((node, i) => {
              const itemWidth = 110;
              const spacing = 120;
              const x = 30 + (i * spacing);
              const y = 30;

              // Connections
              const hasNext = i < sequentialNodes.length - 1;
              const isDecision = node.type === 'decision';

              return (
                <g key={node.id}>
                  {/* Arrow line */}
                  {hasNext && (
                    <line 
                      x1={x + itemWidth} 
                      y1={y + 25} 
                      x2={x + spacing} 
                      y2={y + 25} 
                      stroke="#4b5563" 
                      strokeWidth="2" 
                      markerEnd="url(#print-arrow)" 
                    />
                  )}

                  {/* Node Box */}
                  {isDecision ? (
                    <polygon 
                      points={`${x + itemWidth/2},${y} ${x + itemWidth},${y + 25} ${x + itemWidth/2},${y + 50} ${x},${y + 25}`}
                      fill="#fef3c7"
                      stroke="#d97706"
                      strokeWidth="1.5"
                    />
                  ) : (
                    <rect 
                      x={x} 
                      y={y} 
                      width={itemWidth} 
                      height="50" 
                      rx="4"
                      fill="#eff6ff"
                      stroke="#2563eb"
                      strokeWidth="1.5"
                    />
                  )}

                  {/* Node Label Text */}
                  <text 
                    x={x + itemWidth/2} 
                    y={y + 28} 
                    fontSize="9" 
                    fontWeight="bold" 
                    fill="#111827" 
                    textAnchor="middle"
                  >
                    {node.label.length > 18 ? `${node.label.slice(0, 16)}...` : node.label}
                  </text>
                  
                  {/* Sequence Badge */}
                  <circle cx={x + 6} cy={y + 6} r={7} fill="#374151" />
                  <text x={x + 6} y={y + 9} fontSize="7" fill="#ffffff" fontWeight="bold" textAnchor="middle">
                    {i + 1}
                  </text>
                </g>
              );
            })}
          </svg>
        </div>
      </div>

      {/* 2. Detailed Work Instructions */}
      <div style={{ marginBottom: '30px' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700, borderBottom: '1px solid #e5e7eb', paddingBottom: '6px', marginBottom: '12px' }}>
          2. Detalhamento das Etapas e Instruções de Trabalho
        </h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {sequentialNodes.map((node, i) => (
            <div 
              key={node.id} 
              className="print-step-item"
              style={{ 
                borderLeft: '3px solid #2563eb', 
                paddingLeft: '12px', 
                paddingBottom: '4px',
                borderLeftColor: node.type === 'decision' ? '#d97706' : '#2563eb'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '14px', fontWeight: 700, color: '#111827' }}>
                  {i + 1}. {node.label} <span style={{ fontSize: '11px', color: '#6b7280', fontWeight: 500 }}>({node.type.toUpperCase()})</span>
                </span>
                {node.responsible && (
                  <span style={{ fontSize: '11px', backgroundColor: '#f3f4f6', padding: '2px 6px', borderRadius: '4px', border: '1px solid #e5e7eb', fontWeight: 600 }}>
                    Responsável: {node.responsible}
                  </span>
                )}
              </div>
              <p style={{ margin: '6px 0 0 0', fontSize: '12px', color: '#4b5563', lineHeight: '1.4' }}>
                {node.description || 'Nenhuma instrução de trabalho detalhada para esta etapa.'}
              </p>
              
              {/* Checklist */}
              {node.checklist && node.checklist.length > 0 && (
                <div style={{ marginTop: '8px' }}>
                  <span style={{ fontSize: '11px', fontWeight: 600, color: '#374151', display: 'block', marginBottom: '4px' }}>
                    Checklist de Verificação da Etapa:
                  </span>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                    {node.checklist.map(item => (
                      <div key={item.id} style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '11px', color: '#4b5563' }}>
                        <span style={{ display: 'inline-block', width: '12px', height: '12px', border: '1px solid #9ca3af', borderRadius: '2px', textAlign: 'center', lineHeight: '10px', fontSize: '9px' }}>
                          {item.done ? '✓' : ''}
                        </span>
                        <span>{item.text}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* 3. RACI Matrix */}
      <div style={{ marginBottom: '30px', pageBreakInside: 'avoid' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700, borderBottom: '1px solid #e5e7eb', paddingBottom: '6px', marginBottom: '12px' }}>
          3. Matriz RACI de Responsabilidades
        </h2>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '11px' }}>
          <thead>
            <tr style={{ backgroundColor: '#f3f4f6' }}>
              <th style={{ border: '1px solid #d1d5db', padding: '6px 8px', fontWeight: 'bold' }}>Entregável / Etapa</th>
              <th style={{ border: '1px solid #d1d5db', padding: '6px 8px', fontWeight: 'bold' }}>Executor (R)</th>
              <th style={{ border: '1px solid #d1d5db', padding: '6px 8px', fontWeight: 'bold' }}>Aprovador (A)</th>
              <th style={{ border: '1px solid #d1d5db', padding: '6px 8px', fontWeight: 'bold' }}>Consultado (C)</th>
              <th style={{ border: '1px solid #d1d5db', padding: '6px 8px', fontWeight: 'bold' }}>Informado (I)</th>
            </tr>
          </thead>
          <tbody>
            {sequentialNodes.map(node => (
              <tr key={node.id}>
                <td style={{ border: '1px solid #d1d5db', padding: '6px 8px', fontWeight: 600 }}>{node.label}</td>
                <td style={{ border: '1px solid #d1d5db', padding: '6px 8px' }}>{node.responsible || 'Operador'}</td>
                <td style={{ border: '1px solid #d1d5db', padding: '6px 8px' }}>Gestor da Qualidade</td>
                <td style={{ border: '1px solid #d1d5db', padding: '6px 8px' }}>Diretoria</td>
                <td style={{ border: '1px solid #d1d5db', padding: '6px 8px' }}>Equipes de Apoio</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Footer Audit Block */}
      <div style={{ 
        marginTop: '50px', 
        paddingTop: '12px', 
        borderTop: '1px solid #d1d5db', 
        fontSize: '10px', 
        color: '#9ca3af',
        display: 'flex',
        justifyContent: 'space-between',
        pageBreakInside: 'avoid'
      }}>
        <span>Documento oficial gerado via UIFlux IA.</span>
        <span>Código de Validação: {activeProject.id.toUpperCase()}</span>
      </div>
    </div>
  );
};
