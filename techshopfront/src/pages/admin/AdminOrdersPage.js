import React, { useEffect, useState } from "react";
import { api, getAllOrders, updateOrderStatus, deleteOrder } from "../../services/api";
import { jwtDecode } from "jwt-decode";
import { useNavigate } from "react-router-dom";

const AdminOrdersPage = () => {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const navigate = useNavigate();

  const fetchOrders = async () => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("Niste prijavljeni. Molimo prijavite se.");
        navigate("/login");
        return;
      }

      const decodedToken = jwtDecode(token);
      const roles = decodedToken.roles || [];
      if (!roles.includes("ROLE_ADMIN")) {
        setError("Nemate dozvolu za ovu stranicu. Samo admini mogu pristupiti.");
        navigate("/");
        return;
      }

      const response = await getAllOrders();
      setOrders(response || []);
    } catch (error) {
      console.error("Greška pri dohvatanju porudžbina:", error);
      setError("Greška pri dohvatanju porudžbina: " + (error.response?.data?.message || error.message));
    }
  };

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      const orderToUpdate = orders.find((order) => order.id === orderId);
      if (!orderToUpdate) return;

      const orderUpdateDTO = {
        totalPrice: orderToUpdate.totalPrice,
        orderStatus: newStatus,
      };

      const updatedOrder = await updateOrderStatus(orderId, orderUpdateDTO);
      setOrders(
        orders.map((order) =>
          order.id === orderId ? { ...order, orderStatus: updatedOrder.orderStatus } : order
        )
      );

      // Opcionalno: Dodaj poruku korisniku na frontendu
      if (newStatus === "CANCELLED") {
        alert(`Porudžbina #${orderId} je otkazana i mejl je poslat korisniku.`);
      }
    } catch (error) {
      console.error("Greška pri promeni statusa porudžbine:", error);
      setError("Greška pri promeni statusa: " + (error.response?.data?.message || error.message));
    }
  };

  const handleDelete = async (orderId) => {
    try {
      await deleteOrder(orderId);
      setOrders(orders.filter((order) => order.id !== orderId));
    } catch (error) {
      console.error("Greška pri brisanju porudžbine:", error);
      setError("Greška pri brisanju porudžbine: " + (error.response?.data?.message || error.message));
    }
  };

  const handleViewDetails = (order) => {
    setSelectedOrder(order);
  };

  const handleCloseDetails = () => {
    setSelectedOrder(null);
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  return (
    <div className="container mt-4">
      <h2>Administracija porudžbina</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}

      {!selectedOrder ? (
        <table className="table table-striped">
          <thead>
            <tr>
              <th>ID Porudžbine</th>
              <th>ID Korisnika</th>
              <th>Ukupna cena</th>
              <th>Status</th>
              <th>Adresa</th>
              <th>Akcije</th>
            </tr>
          </thead>
          <tbody>
            {orders.length === 0 ? (
              <tr>
                <td colSpan="6">Nema porudžbina za prikaz.</td>
              </tr>
            ) : (
              orders.map((order) => (
                <tr key={order.id}>
                  <td>{order.id}</td>
                  <td>{order.userId}</td>
                  <td>{order.totalPrice ? `$${order.totalPrice.toFixed(2)}` : "Nije definisano"}</td>
                  <td>
                    <select
                      value={order.orderStatus}
                      onChange={(e) => handleStatusChange(order.id, e.target.value)}
                      className="form-select"
                    >
                      <option value="PENDING">PENDING</option>
                      <option value="SHIPPED">SHIPPED</option>
                      <option value="DELIVERED">DELIVERED</option>
                      <option value="CANCELLED">CANCELLED</option>
                    </select>
                  </td>
                  <td>
                    {order.address
                      ? `${order.address.street}, ${order.address.city}, ${order.address.postalCode}, ${order.address.country}`
                      : "Nije definisano"}
                  </td>
                  <td>
                    <button
                      className="btn btn-primary me-2"
                      onClick={() => handleViewDetails(order)}
                    >
                      Detalji
                    </button>
                    <button
                      className="btn btn-danger"
                      onClick={() => handleDelete(order.id)}
                    >
                      Obriši
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      ) : (
        <div>
          <h3>Detalji porudžbine #{selectedOrder.id}</h3>
          <button className="btn btn-secondary mb-3" onClick={handleCloseDetails}>
            Nazad na listu
          </button>
          <div className="card">
            <div className="card-body">
              <p><strong>ID Korisnika:</strong> {selectedOrder.userId}</p>
              <p><strong>Ukupna cena:</strong> {selectedOrder.totalPrice ? `$${selectedOrder.totalPrice.toFixed(2)}` : "Nije definisano"}</p>
              <p><strong>Status:</strong> {selectedOrder.orderStatus}</p>
              <p>
                <strong>Adresa:</strong>{" "}
                {selectedOrder.address
                  ? `${selectedOrder.address.street}, ${selectedOrder.address.city}, ${selectedOrder.address.postalCode}, ${selectedOrder.address.country}`
                  : "Nije definisano"}
              </p>
              <h5>Stavke porudžbine:</h5>
              {selectedOrder.orderItems && selectedOrder.orderItems.length > 0 ? (
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID Proizvoda</th>
                      <th>Količina</th>
                      <th>Cena</th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedOrder.orderItems.map((item) => (
                      <tr key={item.id}>
                        <td>{item.product?.id || "Nije definisano"}</td>
                        <td>{item.quantity}</td>
                        <td>{item.price ? `$${item.price.toFixed(2)}` : "Nije definisano"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <p>Nema stavki u ovoj porudžbini.</p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminOrdersPage;