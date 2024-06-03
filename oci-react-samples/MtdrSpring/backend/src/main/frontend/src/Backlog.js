import React, { useState, useEffect } from 'react';

const Backlog = () => {
  const [data, setData] = useState([]);

  useEffect(() => {
    fetch('/todolist')
      .then(response => {
        console.log(response);
        if (!response.ok) {
          throw new Error('Network response was not ok' + response);
        }
        return response.json();
      })
      .then(data => {
        console.log('Data fetched:', data); 
        setData(data);
      })
      .catch(error => console.error('Error fetching the data:', error));
  }, []);

  return (
    <div className="View h-screen bg-orange-50 p-8">
      <h1 className="text-3xl font-bold mb-4">Backlog</h1>
      <div className="overflow-x-auto">
        <table className="min-w-full bg-white border border-red-900">
          <thead>
            <tr className="bg-red-900 text-orange-50">
              <th className="py-2 px-4 border-b border-red-900">User Name</th>
              <th className="py-2 px-4 border-b border-red-900">Item ID</th>
              <th className="py-2 px-4 border-b border-red-900">Item Deadline</th>
              <th className="py-2 px-4 border-b border-red-900">Item Description</th>
              <th className="py-2 px-4 border-b border-red-900">Item Status</th>
              <th className="py-2 px-4 border-b border-red-900">Item Creation TS</th>
              <th className="py-2 px-4 border-b border-red-900">Status</th>
            </tr>
          </thead>
          <tbody>
            {data.length > 0 ? (
              data.map((item, index) => (
                <tr key={index} className="hover:bg-gray-50">
                  <td className="py-2 px-4 border-b border-red-900">{item.user.userName}</td>
                  <td className="py-2 px-4 border-b border-red-900">{item.itemId}</td>
                  <td className="py-2 px-4 border-b border-red-900">
                    {item.itemDeadline ? new Date(item.itemDeadline).toLocaleString() : 'No Deadline'}
                  </td>
                  <td className="py-2 px-4 border-b border-red-900">{item.itemDescription}</td>
                  <td className="py-2 px-4 border-b border-red-900">{item.itemStatus}</td>
                  <td className="py-2 px-4 border-b border-red-900">
                    {new Date(item.itemCreationTs).toLocaleString()}
                  </td>
                  <td className="py-2 px-4 border-b border-red-900">{item.status}</td>
                    <select className="border border-red-900 p-1 rounded">
                    <option value="pendiente">Pendiente</option>
                    <option value="hecho">Hecho</option>
                    <option value="en_progreso">En Progreso</option>
                  </select>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="7" className="py-2 px-4 text-center border-b border-red-900">No data available</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
export default Backlog;
