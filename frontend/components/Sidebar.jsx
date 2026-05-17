import { useState } from 'react';
import { Plus, Trash2, MessageSquare, BookOpen, Menu, LogOut, Shield } from 'lucide-react';
import { getRole } from '../services/api';
import './Sidebar.css';

export default function Sidebar({ sessions, currentSession, onSelectSession, onCreateSession, onDeleteSession, onShowKnowledge, onLogout }) {
  const [title, setTitle] = useState('');
  const [collapsed, setCollapsed] = useState(false);

  const handleCreate = () => {
    if (title.trim()) {
      onCreateSession(title.trim());
      setTitle('');
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') handleCreate();
  };

  return (
    <aside className={`sidebar ${collapsed ? 'collapsed' : ''}`}>
      <div className="sidebar-header">
        <button className="collapse-btn" onClick={() => setCollapsed(!collapsed)}>
          <Menu size={18} />
        </button>
        {!collapsed && <h1 className="logo">MindBridge</h1>}
      </div>

      {!collapsed && (
        <>
          <div className="new-session">
            <input
              type="text"
              placeholder="New session..."
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              onKeyDown={handleKeyDown}
            />
            <button onClick={handleCreate} title="Create session">
              <Plus size={16} />
            </button>
          </div>

          <div className="session-list">
            {sessions.map((s) => (
              <div
                key={s.id}
                className={`session-item ${currentSession?.id === s.id ? 'active' : ''}`}
                onClick={() => onSelectSession(s)}
              >
                <MessageSquare size={14} />
                <span className="session-title">{s.title || `Session ${s.id}`}</span>
                <button
                  className="delete-btn"
                  onClick={(e) => { e.stopPropagation(); onDeleteSession(s.id); }}
                >
                  <Trash2 size={12} />
                </button>
              </div>
            ))}
          </div>

          <div className="sidebar-footer">
            <div className="user-role">
              <Shield size={12} />
              <span>{getRole()}</span>
            </div>
            <button className="knowledge-btn" onClick={onShowKnowledge}>
              <BookOpen size={16} />
              <span>Knowledge Base</span>
            </button>
            <button className="logout-btn" onClick={onLogout}>
              <LogOut size={16} />
              <span>Logout</span>
            </button>
          </div>
        </>
      )}
    </aside>
  );
}
