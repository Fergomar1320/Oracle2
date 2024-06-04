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
    const deadline = document.getElementById('deadline').value;
    const sprint = document.getElementById('sprint').value;

    if (description && deadline && sprint) {
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
    <div className="View h-screen bg-orange-50 p-8">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-3xl font-bold">Backlog</h1>
        <button 
          className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
          onClick={() => setShowModal(true)} 
        >
          Add New Task
        </button>
      </div>
      <div className="overflow-x-auto">
        <table className="min-w-full bg-white border border-red-900">
          <thead>
            <tr className="bg-red-900 text-orange-50">
              <th className="py-2 px-4 border-b border-red-900 text-center">User Name</th>
              <th className="py-2 px-4 border-b border-red-900 text-center">Item ID</th>
              <th className="py-2 px-4 border-b border-red-900 text-center">Item Deadline</th>
              <th className="py-2 px-4 border-b border-red-900 text-center">Item Description</th>
              <th className="py-2 px-4 border-b border-red-900 text-center">Item Creation TS</th>
            </tr>
          </thead>
          <tbody>
            {data.length > 0 ? (
              data.map((item, index) => (
                <tr key={index} className="hover:bg-gray-50">
                  <td className="py-2 px-4 border-b border-red-900 text-center">{item.user.userName}</td>
                  <td className="py-2 px-4 border-b border-red-900 text-center">{item.itemId}</td>
                  <td className="py-2 px-4 border-b border-red-900 text-center">
                    {item.itemDeadline ? new Date(item.itemDeadline).toLocaleString() : 'No Deadline'}
                  </td>
                  <td className="py-2 px-4 border-b border-red-900 text-center">{item.itemDescription}</td>
                  <td className="py-2 px-4 border-b border-red-900 text-center">
                    {new Date(item.itemCreationTs).toLocaleString()}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="5" className="py-2 px-4 text-center border-b border-red-900">No data available</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {showModal && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
          <div className="bg-white p-6 rounded shadow-lg">
            <h2 className="text-2xl mb-4">Add New Task</h2>
            <form id="taskForm">
              <div className="mb-4">
                <label className="block text-gray-700">Description</label>
                <input type="text" id="description" className="border rounded w-full py-2 px-3" />
              </div>
              <div className="mb-4">
                <label className="block text-gray-700">Deadline</label>
                <input type="datetime-local" id="deadline" className="border rounded w-full py-2 px-3" />
              </div>
              <div className="mb-4">
                <label className="block text-gray-700">Sprint</label>
                <input type="text" id="sprint" className="border rounded w-full py-2 px-3" />
              </div>
              <div className="flex justify-end">
                <button 
                  type="button" 
                  className="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded mr-2"
                  onClick={() => setShowModal(false)} 
                >
                  Cancel
                </button>
                <button 
                  type="button" 
                  className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
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
