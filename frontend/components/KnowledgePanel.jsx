import { useState, useEffect } from 'react';
import { Upload, Trash2, X, FileText } from 'lucide-react';
import { uploadKnowledge, getKnowledgeList, deleteKnowledge } from '../services/api';
import './KnowledgePanel.css';

export default function KnowledgePanel({ onClose }) {
  const [docs, setDocs] = useState([]);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadDocs();
  }, []);

  const loadDocs = async () => {
    try {
      const list = await getKnowledgeList();
      setDocs(list);
    } catch (e) {
      console.error('Failed to load knowledge', e);
    }
  };

  const handleUpload = async () => {
    if (!title.trim() || !content.trim()) return;
    setLoading(true);
    try {
      await uploadKnowledge(title, content);
      setTitle('');
      setContent('');
      await loadDocs();
    } catch (e) {
      console.error('Upload failed', e);
    }
    setLoading(false);
  };

  const handleDelete = async (id) => {
    try {
      await deleteKnowledge(id);
      setDocs(docs.filter((d) => d.id !== id));
    } catch (e) {
      console.error('Delete failed', e);
    }
  };

  return (
    <div className="knowledge-panel">
      <div className="kp-header">
        <h2>Knowledge Base</h2>
        <button className="close-btn" onClick={onClose}><X size={18} /></button>
      </div>

      <div className="kp-upload">
        <input
          type="text"
          placeholder="Document title..."
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <textarea
          placeholder="Document content..."
          value={content}
          onChange={(e) => setContent(e.target.value)}
          rows={4}
        />
        <button onClick={handleUpload} disabled={loading || !title || !content}>
          <Upload size={14} /> Upload
        </button>
      </div>

      <div className="kp-list">
        {docs.map((doc) => (
          <div key={doc.id} className="kp-item">
            <FileText size={14} />
            <div className="kp-info">
              <div className="kp-title">{doc.title}</div>
              <div className="kp-meta">
                {doc.vectorized ? 'Indexed' : 'Pending'} · {doc.chunkCount} chunks
              </div>
            </div>
            <button className="delete-btn" onClick={() => handleDelete(doc.id)}>
              <Trash2 size={12} />
            </button>
          </div>
        ))}
        {docs.length === 0 && (
          <div className="kp-empty">No documents uploaded yet</div>
        )}
      </div>
    </div>
  );
}
