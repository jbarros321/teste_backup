import React from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { Login } from './components/Login';
import { Dashboard } from './components/Dashboard';
import { Header } from './components/Editor/Header';
import { LeftSidebar } from './components/Editor/LeftSidebar';
import { Canvas } from './components/Editor/Canvas';
import { RightSidebar } from './components/Editor/RightSidebar';
import { PrintReport } from './components/Editor/PrintReport';

const UIFluxApp: React.FC = () => {
  const { currentScreen, authLoading } = useApp();

  if (authLoading) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
        Carregando sessão…
      </div>
    );
  }

  switch (currentScreen) {
    case 'login':
      return <Login />;
    case 'dashboard':
      return <Dashboard />;
    case 'editor':
      return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
          <Header />
          <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
            <LeftSidebar />
            <Canvas />
            <RightSidebar />
          </div>
          <PrintReport />
        </div>
      );
    default:
      return <Login />;
  }
};

function App() {
  return (
    <AppProvider>
      <UIFluxApp />
    </AppProvider>
  );
}

export default App;
