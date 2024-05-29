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
    <div className='View h-screen bg-orange-50'>
      <div className='choose-view flex space-x-5 h-full p-8'>
        <Button className="backlog bg-gray-600 text-orange-50 rounded-3xl hover:bg-red-900 w-1/2" to="/backlog">
          <h1 className="text-5xl">Backlog</h1>
        </Button>
        <Button className="actual-sprint bg-gray-600 text-orange-50 rounded-3xl hover:bg-purple-900 w-1/2" to="/actual-sprint">
          <h1 className="text-5xl">Actual Sprint</h1>
        </Button>
      </div>
    </div>
  );
}

function Button({ children, className, to }) {
  const navigate = useNavigate();
  return (
    <button className={`Button ${className}`} onClick={() => navigate(to)}>
      {children}
    </button>
  );
}

export default App;

