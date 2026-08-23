export type NodeType = 'process' | 'decision' | 'document' | 'subprocess' | 'role' | 'note' | 'organogram';

export interface ChecklistItem {
  id: string;
  text: string;
  done: boolean;
}

export interface FlowNode {
  id: string;
  type: NodeType;
  label: string;
  description: string;
  x: number;
  y: number;
  width?: number;
  height?: number;
  responsible?: string;
  checklist?: ChecklistItem[];
}

export interface FlowEdge {
  id: string;
  source: string;
  target: string;
}

export interface Comment {
  id: string;
  nodeId?: string;
  author: string;
  text: string;
  createdAt: string;
  resolved: boolean;
}

export type ProjectStatus = 'draft' | 'developing' | 'review' | 'approved' | 'published';

export interface ProjectVersion {
  version: string;
  description: string;
  date: string;
  author: string;
  nodes: FlowNode[];
  edges: FlowEdge[];
}

export interface AuditLog {
  action: string;
  user: string;
  date: string;
  details?: string;
}

export interface Project {
  id: string;
  name: string;
  description: string;
  category: string;
  team: string[];
  status: ProjectStatus;
  version: string;
  createdAt: string;
  updatedAt: string;
  nodes: FlowNode[];
  edges: FlowEdge[];
  comments: Comment[];
  versions: ProjectVersion[];
  auditLogs: AuditLog[];
}

export type UserRole = 'admin' | 'gestor' | 'membro';

export interface Company {
  id: string;
  name: string;
  tokens: number;
}

export interface UserSession {
  id?: string;          // id do auth.users (Supabase)
  email: string;
  fullName?: string;
  companyId?: string;   // empresa (tenant) do usuário
  role?: UserRole;
  tokens: number;
  company: string;      // nome da empresa (compatibilidade com a UI atual)
  isLoggedIn: boolean;
}

/** Documento POP gerado a partir de um projeto (histórico). */
export interface GeneratedDocument {
  id: string;
  projectId?: string;
  title: string;
  docType: string;      // ex: 'POP'
  version?: string;
  createdAt: string;
  generatedBy?: string;
}
