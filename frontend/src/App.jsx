import HealthStatus from './components/HealthStatus';

export default function App() {
  return (
    <main className="app">
      <header>
        <h1>StudyMatch</h1>
        <p>Formação de grupos equilibrados a partir de competências.</p>
      </header>
      <HealthStatus />
    </main>
  );
}
