import { useEffect, useRef } from 'react';
import { User, Bot } from 'lucide-react';
import './MessageList.css';

export default function MessageList({ messages, streamingText }) {
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, streamingText]);

  return (
    <div className="message-list">
      {messages.length === 0 && !streamingText && (
        <div className="empty-state">
          <Bot size={40} />
          <h3>MindBridge</h3>
          <p>Start a conversation with your AI counselor</p>
        </div>
      )}
      {messages.map((msg, i) => (
        <div key={i} className={`message-row ${msg.role === 'USER' ? 'user' : 'assistant'}`}>
          <div className="message-avatar">
            {msg.role === 'USER' ? <User size={16} /> : <Bot size={16} />}
          </div>
          <div className="message-bubble">
            <div className="message-content">{msg.content}</div>
          </div>
        </div>
      ))}
      {streamingText && (
        <div className="message-row assistant">
          <div className="message-avatar"><Bot size={16} /></div>
          <div className="message-bubble streaming">
            <div className="message-content">{streamingText}<span className="cursor">|</span></div>
          </div>
        </div>
      )}
      <div ref={bottomRef} />
    </div>
  );
}
