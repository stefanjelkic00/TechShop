import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Container, Row, Col } from 'react-bootstrap';
import { api } from '../services/api';

const OrdersPage = () => {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const fetchOrders = async () => {
    const token = localStorage.getItem('token');
    console.log('fetchOrders pokrenut sa tokenom:', token);
    if (!token) {
      setError('Niste prijavljeni. Molimo prijavite se.');
      setTimeout(() => navigate('/login'), 2000); // Odloži preusmeravanje
      return;
    }

    try {
      console.log('Slanje GET /api/orders');
      const response = await api.get('/orders');
      console.log('Odgovor od servera:', response.status, response.data);
      
      // Sortiranje porudžbina od najnovijih ka najstarijim (po ID-u)
      const sortedOrders = response.data.sort((a, b) => b.id - a.id);
      setOrders(sortedOrders);
    } catch (error) {
      console.error('Greška u fetchOrders:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message,
      });
      if (error.response) {
        if (error.response.status === 401) {
          setError('Neautorizovan pristup. Molimo prijavite se ponovo.');
          setTimeout(() => {
            localStorage.removeItem('token');
            navigate('/login');
          }, 2000);
        } else if (error.response.status === 403) {
          setError('Nemate dozvolu za pristup porudžbinama.');
        } else {
          setError(`Greška pri dohvatanju porudžbina: ${error.response.data?.message || error.message}`);
        }
      } else {
        setError(`Greška: ${error.message}`);
      }
    }
  };

  const handleOrderClick = (orderId) => {
    console.log('Kliknuta porudžbina sa ID:', orderId);
    if (!orderId || orderId === 'undefined') {
      setError('Neispravan ID porudžbine.');
      console.error('OrderId je neispravan:', orderId);
      return;
    }
    navigate(`/order/${orderId}`);
  };

  useEffect(() => {
    fetchOrders();
  }, [navigate]);

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return '#ff4500';
      case 'COMPLETED':
        return '#4CAF50';
      case 'CANCELLED':
        return '#dc3545';
      default:
        return '#888';
    }
  };

  return (
    <div style={{ padding: '20px', background: 'linear-gradient(135deg, #1a1a1a 0%, #444 100%)', minHeight: '100vh' }}>
      <style>
        {`
          .order-card {
            background: #222;
            border: 2px solid #444;
            border-radius: 10px;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            cursor: pointer;
            color: #fff;
            margin-bottom: 15px;
          }
          .order-card:hover {
            transform: scale(1.03);
            box-shadow: 0 0 15px #ff4500;
            border-color: #ff4500;
          }
          .status-text {
            font-weight: bold;
          }
          .order-details {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 10px;
          }
          .order-id {
            font-size: 1.2rem;
            font-weight: bold;
          }
          .order-price {
            font-size: 1.1rem;
            color: #ff4500;
          }
        `}
      </style>
      <Container>
        <h1 style={{ color: '#fff', marginBottom: '30px' }}>Vaše porudžbine</h1>
        {error && <p style={{ color: '#ff4500' }}>{error}</p>}
        {orders.length === 0 && !error ? (
          <p style={{ color: '#fff' }}>Nemate porudžbina.</p>
        ) : (
          <Row>
            {orders.map((order) => (
              <Col key={order.id} xs={12}>
                <Card className="order-card" onClick={() => handleOrderClick(order.id)}>
                  <div className="order-details">
                    <div>
                      <span className="order-id">Porudžbina #{order.id}</span>
                      <p style={{ margin: 0 }}>
                        Status:{' '}
                        <span className="status-text" style={{ color: getStatusColor(order.orderStatus) }}>
                          {order.orderStatus}
                        </span>
                      </p>
                    </div>
                    <div className="order-price">Ukupno: ${order.totalPrice.toFixed(2)}</div>
                  </div>
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </Container>
    </div>
  );
};

export default OrdersPage;