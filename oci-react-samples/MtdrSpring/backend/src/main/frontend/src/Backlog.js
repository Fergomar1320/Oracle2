import React, { useState, useEffect } from 'react';

const Backlog = () => {
  const [data, setData] = useState([]);
  const [showModal, setShowModal] = useState(false); 

  useEffect(() => {
    fetch('/todolist')
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok' + response);
        }
        return response.json();
      })
      .then(data => {
        setData(data);
      })
      .catch(error => console.error('Error fetching the data:', error));
  }, []);

  const addTask = () => {
    const description = document.getElementById('description').value;
    const deadline = document.getElementById('deadline').value ? document.getElementById('deadline').value : null;
    const sprint = document.getElementById('sprint').value ? document.getElementById('sprint').value : null;

    if (description) {
      const newItem = {
        user: { userName: 'New User' },
        itemId: 'New ID',
        itemDeadline: deadline,
        itemDescription: description,
        itemCreationTs: new Date().toISOString(),
      };
      setData([...data, newItem]);
      document.getElementById('taskForm').reset();
      setShowModal(false);
    } else {
      alert('Please fill out all fields.');
    }
  };

  return (
    <div className="View h-screen" style={{ backgroundColor: 'rgb(253, 246, 240)', color: 'rgb(59, 53, 49)', padding: '3rem' }}>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Backlog</h1>
        <button 
          className="font-bold py-2 px-4 rounded"
          style={{ backgroundColor: 'rgb(59, 53, 49)', color: 'rgb(253, 246, 240)' }}
          onClick={() => setShowModal(true)} 
        >
          Add New Task
        </button>
      </div>
      <div className="overflow-x-auto">
        <table className="min-w-full bg-white border" style={{ borderColor: 'rgb(198, 60, 41)' }}>
          <thead>
            <tr style={{ backgroundColor: 'rgb(198, 60, 41)', color: 'rgb(253, 246, 240)' }}>
              <th className="py-2 px-4 border-b text-center">User Name</th>
              <th className="py-2 px-4 border-b text-center">Item ID</th>
              <th className="py-2 px-4 border-b text-center">Item Deadline</th>
              <th className="py-2 px-4 border-b text-center">Item Description</th>
              <th className="py-2 px-4 border-b text-center">Item Status</th>
            </tr>
          </thead>
          <tbody>
            {data.length > 0 ? (
              data.map((item, index) => (
                <tr key={index} className="hover:bg-gray-50" style={{ backgroundColor: 'rgb(253, 246, 240)', color: 'rgb(59, 53, 49)' }}>
                  <td className="py-2 px-4 border-b" style={{ borderColor: 'rgb(198, 60, 41)' }}>{item.user.userName}</td>
                  <td className="py-2 px-4 border-b" style={{ borderColor: 'rgb(198, 60, 41)' }}>{item.itemId}</td>
                  <td className="py-2 px-4 border-b" style={{ borderColor: 'rgb(198, 60, 41)' }}>
                    {item.itemDeadline ? new Date(item.itemDeadline).toLocaleString() : 'No Deadline'}
                  </td>
                  <td className="py-2 px-4 border-b" style={{ borderColor: 'rgb(198, 60, 41)' }}>{item.itemDescription}</td>
                  <td className="py-2 px-4 border-b" style={{ borderColor: 'rgb(198, 60, 41)' }}>
                    {new Date(item.itemCreationTs).toLocaleString()}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="6" className="py-2 px-4 text-center border-b" style={{ borderColor: 'rgb(198, 60, 41)' }}>No data available</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {showModal && (
        <div className="fixed inset-0 flex items-center justify-center" style={{ backgroundColor: 'rgba(0, 0, 0, 0.5)' }}>
          <div className="bg-white p-6 rounded shadow-lg">
            <h2 className="text-2xl mb-4" style={{ color: 'rgb(59, 53, 49)' }}>Add New Task</h2>
            <form id="taskForm">
              <div className="mb-4">
                <label className="block" style={{ color: 'rgb(59, 53, 49)' }}>Description</label>
                <input type="text" id="description" className="border rounded w-full py-2 px-3" style={{ borderColor: 'rgb(198, 60, 41)' }} />
              </div>
              <div className="mb-4">
                <label className="block" style={{ color: 'rgb(59, 53, 49)' }}>Deadline</label>
                <input type="datetime-local" id="deadline" className="border rounded w-full py-2 px-3" style={{ borderColor: 'rgb(198, 60, 41)' }} />
              </div>
              <div className="mb-4">
                <label className="block" style={{ color: 'rgb(59, 53, 49)' }}>Sprint</label>
                <input type="text" id="sprint" className="border rounded w-full py-2 px-3" style={{ borderColor: 'rgb(198, 60, 41)' }} />
              </div>
              <div className="flex justify-end">
                <button 
                  type="button" 
                  className="font-bold py-2 px-4 rounded mr-2"
                  style={{ backgroundColor: 'rgb(198, 60, 41)', color: 'rgb(253, 246, 240)' }}
                  onClick={() => setShowModal(false)} 
                >
                  Cancel
                </button>
                <button 
                  type="button" 
                  className="font-bold py-2 px-4 rounded"
                  style={{ backgroundColor: 'rgb(59, 53, 49)', color: 'rgb(253, 246, 240)' }}
                  onClick={addTask} 
                >
                  Add Task
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Backlog;
