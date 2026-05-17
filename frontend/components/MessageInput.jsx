import { useState } from 'react';
import { Send, Loader } from 'lucide-react';
import './MessageInput.css';

export default function MessageInput({ onSend, disabled }) {
  const [input, setInput] = useState('');

  const handleSend = () => {
    if (input.trim() && !disabled) {
      onSend(input.trim());
      setInput('');
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="message-input">
      <textarea
        placeholder="Type your message here... (Enter to send, Shift+Enter for new line)"
        value={input}
        onChange={(e) => setInput(e.target.value)}
        onKeyDown={handleKeyDown}
        rows={2}
        disabled={disabled}
      />
      <button onClick={handleSend} disabled={disabled || !input.trim()}>
        {disabled ? <Loader size={16} className="spin" /> : <Send size={16} />}
      </button>
    </div>
  );
}
