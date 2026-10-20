import { useState } from 'react';
import AdminApp from './AdminApp.jsx';
import { readSession, saveSession } from './auth/session.js';
import Login from './student/Login.jsx';
import Register from './student/Register.jsx';
import StudentApp from './student/StudentApp.jsx';
import Welcome from './student/Welcome.jsx';
import './student/student.css';

/**
 * Ponto de entrada:
 * - sem sessão: boas-vindas, entrar ou criar conta;
 * - administrador: área de gestão (estudantes e cadeiras), com o Pix real;
 * - estudante: quiz obrigatório até o Pix nascer e depois a área do estudante.
 */
export default function App() {
  const [session, setSession] = useState(readSession);
  const [screen, setScreen] = useState('welcome');
  const [justRegistered, setJustRegistered] = useState(false);

  const login = (next, isNew = false) => {
    saveSession(next);
    setJustRegistered(isNew);
    setSession(next);
  };

  const logout = () => {
    saveSession(null);
    setSession(null);
    setScreen('welcome');
  };

  if (session?.role === 'admin') return <AdminApp onLogout={logout} />;

  if (session?.role === 'student') {
    return <StudentApp studentId={session.id} startWithQuiz={justRegistered} onLogout={logout} />;
  }

  return (
    <div className="sx">
      {screen === 'welcome' && (
        <Welcome onStart={() => setScreen('register')} onLogin={() => setScreen('login')} />
      )}
      {screen === 'register' && (
        <Register
          onBack={() => setScreen('welcome')}
          onCreated={(s) => login({ role: 'student', id: s.id }, true)}
        />
      )}
      {screen === 'login' && (
        <Login
          onBack={() => setScreen('welcome')}
          onRegister={() => setScreen('register')}
          onStudent={(s) => login({ role: 'student', id: s.id })}
          onAdmin={() => login({ role: 'admin' })}
        />
      )}
    </div>
  );
}
