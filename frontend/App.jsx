import { useState } from 'react';
import { isLoggedIn, logout } from './services/api';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import './App.css';

export default function App() {
  const [loggedIn, setLoggedIn] = useState(isLoggedIn());

  const handleLogin = () => setLoggedIn(true);

  const handleLogout = () => {
    logout();
    setLoggedIn(false);
  };

  if (!loggedIn) {
    return <LoginPage onLogin={handleLogin} />;
  }

  return <HomePage onLogout={handleLogout} />;
}
