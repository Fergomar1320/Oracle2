import React from 'react';
import Backlog from './Backlog';
import ActualSprint from './ActualSprint';
import { BrowserRouter as Router, Routes, Route, useNavigate } from 'react-router-dom';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<View />} />
        <Route path="/backlog" element={<Backlog />} />
        <Route path="/actual-sprint" element={<ActualSprint />} />
      </Routes>
    </Router>
  );
}

function View() {
  return (
    <div className='View h-screen' style={{ backgroundColor: 'rgb(253, 246, 240)' }}>
      <div className='choose-view flex space-x-5 h-full p-8'>
        <Button 
          className="backlog rounded-3xl w-1/2" 
          to="/backlog" 
          buttonStyle={{ backgroundColor: 'rgb(59, 53, 49)' }}
          hoverStyle={{ backgroundColor: 'rgb(198, 60, 41)' }}
        >
          <h1 className="text-5xl" style={{ color: 'rgb(253, 246, 240)' }}>Backlog</h1>
        </Button>
        <Button 
          className="actual-sprint rounded-3xl w-1/2" 
          to="/actual-sprint" 
          buttonStyle={{ backgroundColor: 'rgb(59, 53, 49)' }}
          hoverStyle={{ backgroundColor: 'rgb(198, 60, 41)' }}
        >
          <h1 className="text-5xl" style={{ color: 'rgb(253, 246, 240)' }}>Actual Sprint</h1>
        </Button>
      </div>
    </div>
  );
}

function Button({ children, className, to, buttonStyle, hoverStyle }) {
  const navigate = useNavigate();
  return (
    <button 
      className={`Button ${className}`} 
      onClick={() => navigate(to)}
      style={{ backgroundColor: buttonStyle.backgroundColor }}
      onMouseOver={(e) => e.currentTarget.style.backgroundColor = hoverStyle.backgroundColor}
      onMouseOut={(e) => e.currentTarget.style.backgroundColor = buttonStyle.backgroundColor}
    >
      {children}
    </button>
  );
}

export default App;
