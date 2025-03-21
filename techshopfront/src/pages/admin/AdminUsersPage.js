import React, { useEffect, useState } from "react";
import { api } from "./../../services/api";

const AdminUsersPage = () => {
  const [users, setUsers] = useState([]);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const response = await api.get("/users");
        setUsers(response.data);
      } catch (error) {
        console.error("Greška pri dohvatanju korisnika:", error);
      }
    };
    fetchUsers();
  }, []);

  const handleDelete = async (id) => {
    try {
      await api.delete(`/users/${id}`);
      setUsers(users.filter((user) => user.id !== id));
    } catch (error) {
      console.error("Greška pri brisanju korisnika:", error);
    }
  };

  const handleUpdate = async (id, updatedData) => {
    try {
      await api.put(`/users/${id}`, updatedData);
      setUsers(users.map((user) => (user.id === id ? { ...user, ...updatedData } : user)));
    } catch (error) {
      console.error("Greška pri ažuriranju korisnika:", error);
    }
  };

  return (
    <div className="container mt-4">
      <h2>Korisnici</h2>
      <style>
        {`
          .table th, .table td {
            padding: 0.75rem 1.5rem; /* Širi razmak između kolona u tabeli */
            vertical-align: middle; /* Centriranje sadržaja u ćelijama */
            text-align: center; /* Centriranje teksta u tabeli */
          }
        `}
      </style>

      <table className="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Ime</th>
            <th>Prezime</th>
            <th>Email</th>
            <th>Tip korisnika</th> {/* Dodato polje za customerType */}
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id}>
              <td>{user.id}</td>
              <td>{user.firstName}</td>
              <td>{user.lastName}</td>
              <td>{user.email}</td>
              <td>{user.customerType || "Nema tipa"}</td> {/* Dodato customerType sa podrazumevanom vrednošću */}
              <td>
                <button
                  className="btn btn-warning me-2"
                  onClick={() =>
                    handleUpdate(user.id, {
                      firstName: prompt("Novo ime:", user.firstName),
                      lastName: prompt("Novo prezime:", user.lastName),
                      email: prompt("Novi email:", user.email),
                      customerType: prompt("Novi tip korisnika (npr. REGULAR, VIP):", user.customerType), // Dodato ažuriranje customerType
                    })
                  }
                >
                  Ažuriraj
                </button>
                <button className="btn btn-danger" onClick={() => handleDelete(user.id)}>
                  Obriši
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default AdminUsersPage;