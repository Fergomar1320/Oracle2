import React, { useState, useEffect } from 'react';

const Backlog = () => {
  const [data, setData] = useState([]);

  useEffect(() => {
    fetch('/todolist')
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
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
    <div>
      <h1>Backlog</h1>
      <table>
        <thead>
          <tr>
            <th>User Name</th>
            <th>Item ID</th>
            <th>Item Deadline</th>
            <th>Item Description</th>
            <th>Item Status</th>
            <th>Item Creation TS</th>
          </tr>
        </thead>
        <tbody>
          {data.length > 0 ? (
            data.map((item, index) => (
              <tr key={index}>
                <td>{item.user.userName}</td>
                <td>{item.itemId}</td>
                <td>{item.itemDeadline ? new Date(item.itemDeadline).toLocaleString() : 'No Deadline'}</td>
                <td>{item.itemDescription}</td>
                <td>{item.itemStatus}</td>
                <td>{new Date(item.itemCreationTs).toLocaleString()}</td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="6">No data available</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default Backlog;
