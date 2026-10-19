import React, { useState } from 'react';
import HealthStatus from './components/HealthStatus';
import { QuizOnboarding } from './components/QuizOnboarding';
import StudentManager from './components/StudentManager';
import StudentRecords from './components/StudentRecords';

export default function App() {
  const [view, setView] = useState('quiz'); // 'quiz' ou 'profile'

  return (
    <main className="app">
      <header>
        <h1>StudyMatch</h1>
        <p>Formação de grupos equilibrados a partir de competências.</p>
      </header>

      {view === 'quiz' ? (
        <QuizOnboarding
          studentId={1}
          onComplete={() => setView('profile')}
        />
      ) : (
        <div style={{ textAlign: 'center', padding: '40px 20px' }}>
          <h2>Perfil do Estudante</h2>
          <p>As tuas competências foram registadas e calculadas com sucesso!</p>
          <button
            onClick={() => setView('quiz')}
            style={{ padding: '10px 20px', marginTop: '15px', cursor: 'pointer' }}
          >
            Refazer Quiz
          </button>
        </div>
      )}

      <StudentManager />

      <StudentRecords />

      <footer style={{ marginTop: '40px', borderTop: '1px solid #eee', paddingTop: '20px' }}>
        <HealthStatus />
      </footer>
    </main>
  );
}
