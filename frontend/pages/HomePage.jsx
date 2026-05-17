import { useState, useEffect, useCallback } from 'react';
import Sidebar from '../components/Sidebar';
import MessageList from '../components/MessageList';
import MessageInput from '../components/MessageInput';
import KnowledgePanel from '../components/KnowledgePanel';
import {
  getSessions, createSession, deleteSession, getMessages, streamChat,
} from '../services/api';
import './HomePage.css';

export default function HomePage({ onLogout }) {
  const [sessions, setSessions] = useState([]);
  const [currentSession, setCurrentSession] = useState(null);
  const [messages, setMessages] = useState([]);
  const [streamingText, setStreamingText] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [showKnowledge, setShowKnowledge] = useState(false);

  useEffect(() => {
    loadSessions();
  }, []);

  useEffect(() => {
    if (currentSession) {
      loadMessages(currentSession.id);
      setStreamingText('');
    }
  }, [currentSession]);

  const loadSessions = async () => {
    try {
      const list = await getSessions();
      setSessions(list);
      if (!currentSession && list.length > 0) {
        setCurrentSession(list[0]);
      }
    } catch (e) {
      console.error('Failed to load sessions', e);
    }
  };

  const loadMessages = async (sessionId) => {
    try {
      const msgs = await getMessages(sessionId);
      setMessages(msgs);
    } catch (e) {
      console.error('Failed to load messages', e);
    }
  };

  const handleCreateSession = useCallback(async (title) => {
    try {
      const session = await createSession(title, 'deepseek');
      setSessions((prev) => [session, ...prev]);
      setCurrentSession(session);
      setMessages([]);
    } catch (e) {
      console.error('Failed to create session', e);
    }
  }, []);

  const handleDeleteSession = useCallback(async (id) => {
    try {
      await deleteSession(id);
      setSessions((prev) => prev.filter((s) => s.id !== id));
      if (currentSession?.id === id) {
        const remaining = sessions.filter((s) => s.id !== id);
        setCurrentSession(remaining[0] || null);
        setMessages([]);
      }
    } catch (e) {
      console.error('Failed to delete session', e);
    }
  }, [currentSession, sessions]);

  const handleSend = useCallback((content) => {
    if (!currentSession) return;

    const userMsg = { role: 'USER', content };
    setMessages((prev) => [...prev, userMsg]);
    setIsStreaming(true);
    setStreamingText('');

    let fullResponse = '';
    streamChat(currentSession.id, content, 'deepseek', {
      onToken: (token) => {
        fullResponse += token;
        setStreamingText(fullResponse);
      },
      onDone: () => {
        const assistantMsg = { role: 'ASSISTANT', content: fullResponse };
        setMessages((prev) => [...prev, assistantMsg]);
        setStreamingText('');
        setIsStreaming(false);
      },
      onError: (err) => {
        console.error('Stream error', err);
        setStreamingText('');
        setIsStreaming(false);
      },
    });
  }, [currentSession]);

  return (
    <div className="home-page">
      <Sidebar
        sessions={sessions}
        currentSession={currentSession}
        onSelectSession={setCurrentSession}
        onCreateSession={handleCreateSession}
        onDeleteSession={handleDeleteSession}
        onShowKnowledge={() => setShowKnowledge(!showKnowledge)}
        onLogout={onLogout}
      />
      <div className="main-panel">
        <MessageList messages={messages} streamingText={streamingText} />
        <MessageInput onSend={handleSend} disabled={isStreaming || !currentSession} />
      </div>
      {showKnowledge && (
        <KnowledgePanel onClose={() => setShowKnowledge(false)} />
      )}
    </div>
  );
}
