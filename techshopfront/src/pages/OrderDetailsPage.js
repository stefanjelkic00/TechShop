import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api, jwtDecode } from '../services/api';
import { Container, Card, Form, Button } from 'react-bootstrap';

const OrderDetailsPage = () => {
  const { orderId } = useParams();
  const [order, setOrder] = useState(null);
  const [error, setError] = useState(null);
  const [address, setAddress] = useState({ street: '', city: '', postalCode: '', country: '' });
  const navigate = useNavigate();

  // Funkcija za određivanje popusta na osnovu broja prethodnih porudžbina
  const determineDiscount = (orderCount) => {
    if (orderCount >= 4) {
      return 0.30; // VIP - 30% popusta (5. porudžbina i dalje)
    } else if (orderCount >= 2) {
      return 0.20; // PLATINUM - 20% popusta (3. i 4. porudžbina)
    } else if (orderCount >= 1) {
      return 0.10; // PREMIUM - 10% popusta (2. porudžbina)
    } else {
      return 0.0; // REGULAR - nema popusta (1. porudžbina)
    }
  };

  const fetchOrderDetails = async () => {
    const token = localStorage.getItem('token');
    console.log('Pokrećem fetch za orderId:', orderId, 'sa tokenom:', token);
    if (!token) {
      setError('Niste prijavljeni. Molimo prijavite se.');
      navigate('/login');
      return;
    }

    try {
      // Dohvatanje userId iz tokena
      const decodedToken = jwtDecode(token);
      if (!decodedToken || !decodedToken.sub) {
        setError('Neispravan token. Molimo ponovo se prijavite.');
        localStorage.removeItem('token');
        navigate('/login');
        return;
      }

      const email = decodedToken.sub;
      const userResponse = await api.get(`/users/email/${email}`);
      const userData = userResponse.data;
      const userId = userData?.id;
      if (!userId) {
        throw new Error('Nije pronađen userId u odgovoru');
      }
      console.log('Pronađen userId:', userId);

      // Dohvatanje svih porudžbina korisnika
      const allOrdersResponse = await api.get(`/orders`);
      const allOrders = allOrdersResponse.data;
      console.log('Sve porudžbine:', allOrders);

      // Filtriranje porudžbina za trenutnog korisnika
      const userOrders = allOrders.filter((order) => order.userId === userId);
      console.log('Porudžbine trenutnog korisnika:', userOrders);

      // Dohvatanje detalja trenutne porudžbine
      const response = await api.get(`/orders/${orderId}`);
      console.log('Detalji porudžbine sa servera:', response.data);
      let currentOrder = response.data;
      
      // Provera da li trenutna porudžbina pripada korisniku
      if (currentOrder.userId !== userId) {
        setError('Nemate dozvolu za pristup ovoj porudžbini.');
        return;
      }

      // Sortiranje porudžbina po datumu kreiranja (rastuće)
      const sortedOrders = userOrders.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));

      // Pronalaženje broja porudžbina pre trenutne porudžbine
      const currentOrderDate = new Date(currentOrder.createdAt);
      const previousOrdersCount = sortedOrders.filter(
        (order) => new Date(order.createdAt) < currentOrderDate && order.id !== currentOrder.id
      ).length;
      console.log('Broj porudžbina pre trenutne porudžbine:', previousOrdersCount);

      // Postavljanje popusta na osnovu broja prethodnih porudžbina
      currentOrder.appliedDiscount = determineDiscount(previousOrdersCount);
      console.log('Primenjeni popust za ovu porudžbinu:', currentOrder.appliedDiscount);

      // Računanje ukupne cene sa popustom
      const originalTotalPrice = currentOrder.orderItems.reduce(
        (total, item) => total + (item.price / item.quantity) * item.quantity,
        0
      );
      const discountMultiplier = 1 - currentOrder.appliedDiscount;
      currentOrder.totalPriceWithDiscount = originalTotalPrice * discountMultiplier;
      console.log('Originalna ukupna cena:', originalTotalPrice);
      console.log('Ukupna cena sa popustom:', currentOrder.totalPriceWithDiscount);

      setOrder(currentOrder);

      // Postavljanje adrese
      if (currentOrder.address) {
        setAddress({
          street: currentOrder.address.street || '',
          city: currentOrder.address.city || '',
          postalCode: currentOrder.address.postalCode || '',
          country: currentOrder.address.country || '',
        });
      }
    } catch (error) {
      console.error('Greška u fetchOrderDetails:', error.response?.data || error.message);
      if (error.response) {
        if (error.response.status === 403) {
          setError('Nemate dozvolu za pristup ovoj porudžbini.');
        } else if (error.response.status === 404) {
          setError('Porudžbina nije pronađena.');
        } else {
          setError(`Greška: ${error.response.data || error.message}`);
        }
      } else {
        setError(`Greška: ${error.message}`);
      }
    }
  };

  const handleAddressChange = (e) => {
    setAddress({ ...address, [e.target.name]: e.target.value });
  };

  const handleUpdateAddress = async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      setError('Niste prijavljeni. Molimo prijavite se.');
      navigate('/login');
      return;
    }

    try {
      const response = await api.post(`/addresses`, address);
      const newAddress = response.data;

      await api.put(`/orders/${orderId}`, {
        totalPrice: order.totalPrice,
        orderStatus: order.orderStatus,
        addressId: newAddress.id,
      });
      alert('Adresa uspešno ažurirana!');
      fetchOrderDetails();
    } catch (error) {
      setError(`Greška pri ažuriranju adrese: ${error.message}`);
      console.error('Greška pri ažuriranju adrese:', error.response?.data || error.message);
    }
  };

  // Funkcija za formatiranje datuma
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleString('sr-RS', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  useEffect(() => {
    fetchOrderDetails();
  }, [orderId, navigate]);

  if (error) {
    return (
      <div style={{ padding: '20px', background: 'linear-gradient(135deg, #1a1a1a 0%, #444 100%)', minHeight: '100vh' }}>
        <Container>
          <h1 style={{ color: '#fff' }}>Greška</h1>
          <p style={{ color: '#ff4500' }}>{error}</p>
          {error.includes('Niste prijavljeni') && (
            <Button
              onClick={() => navigate('/login')}
              style={{ background: '#ff4500', border: 'none', borderRadius: '5px', padding: '10px 20px' }}
            >
              Prijavi se ponovo
            </Button>
          )}
        </Container>
      </div>
    );
  }

  if (!order) return (
    <div style={{ padding: '20px', background: 'linear-gradient(135deg, #1a1a1a 0%, #444 100%)', minHeight: '100vh' }}>
      <Container>
        <p style={{ color: '#fff' }}>Učitavanje...</p>
      </Container>
    </div>
  );

  return (
    <div style={{ padding: '20px', background: 'linear-gradient(135deg, #1a1a1a 0%, #444 100%)', minHeight: '100vh' }}>
      <style>
        {`
          .order-details-card {
            background: #222;
            border: 2px solid #444;
            border-radius: 10px;
            color: #fff;
            padding: 20px;
          }
          .order-item {
            padding: 10px;
            border-bottom: 1px solid #444;
          }
          .address-form input {
            background: #333;
            color: #fff;
            border: 1px solid #444;
            border-radius: 5px;
            margin-bottom: 10px;
          }
          .address-form input::placeholder {
            color: #888;
          }
          .save-address-btn {
            background: #ff4500;
            border: none;
            border-radius: 5px;
            padding: 10px 20px;
            transition: background 0.3s ease;
          }
          .save-address-btn:hover {
            background: #e03e00;
          }
        `}
      </style>
      <Container>
        <h1 style={{ color: '#fff', marginBottom: '20px' }}>Detalji porudžbine #{order.id}</h1>
        <Card className="order-details-card">
          <p>
            Datum porudžbine: <span style={{ color: '#ff4500' }}>{order.createdAt ? formatDate(order.createdAt) : 'Nije dostupno'}</span>
          </p>
          <p>
            Ukupna cena:{' '}
            {order.appliedDiscount > 0 ? (
              <>
                <span style={{ textDecoration: 'line-through', color: '#888', fontSize: '0.9rem', marginRight: '5px' }}>
                  ${order.totalPrice.toFixed(2)}
                </span>
                <span style={{ fontSize: '1.1rem', color: '#ff4500', textShadow: '1px 1px 5px #000' }}>
                  ${order.totalPriceWithDiscount.toFixed(2)}
                </span>
              </>
            ) : (
              <span style={{ fontSize: '1.1rem', color: '#ff4500', textShadow: '1px 1px 5px #000' }}>
                ${order.totalPrice.toFixed(2)}
              </span>
            )}
          </p>
          <p>
            Status: <span style={{ color: '#ff4500' }}>{order.orderStatus}</span>
          </p>
          <p>
            Primenjeni popust: <span style={{ color: '#ff4500' }}>{(order.appliedDiscount * 100).toFixed(0)}%</span>
          </p>
          <h3 style={{ color: '#fff' }}>Stavke porudžbine</h3>
          <ul style={{ listStyleType: 'none', padding: 0 }}>
            {order.orderItems && order.orderItems.length > 0 ? (
              order.orderItems.map((item) => {
                const originalPricePerUnit = item.price / item.quantity; // Puna cena po jedinici
                const discountMultiplier = 1 - order.appliedDiscount; // npr. ako je popust 0.10, množi se sa 0.90
                const discountedPricePerUnit = originalPricePerUnit * discountMultiplier;
                const discountPercentage = (order.appliedDiscount * 100).toFixed(0); // Procentualni popust
                const hasDiscount = discountPercentage > 0;
                const itemTotal = discountedPricePerUnit * item.quantity;

                return (
                  <li key={item.id} className="order-item">
                    {item.product.name} - Količina: {item.quantity} - Cena po jedinici:{' '}
                    {hasDiscount ? (
                      <>
                        <span style={{ textDecoration: 'line-through', color: '#888', fontSize: '0.9rem', marginRight: '5px' }}>
                          ${originalPricePerUnit.toFixed(2)}
                        </span>
                        <span style={{ fontSize: '1.1rem', color: '#ff4500', textShadow: '1px 1px 5px #000' }}>
                          ${discountedPricePerUnit.toFixed(2)}
                        </span>
                        <span style={{ marginLeft: '10px', color: '#ff4500', fontSize: '0.8rem' }}>
                          ({discountPercentage}% OFF)
                        </span>
                      </>
                    ) : (
                      <span style={{ fontSize: '1.1rem', color: '#ff4500', textShadow: '1px 1px 5px #000' }}>
                        ${originalPricePerUnit.toFixed(2)}
                      </span>
                    )}
                    <br />
                    Ukupno za stavku: ${itemTotal.toFixed(2)}
                  </li>
                );
              })
            ) : (
              <li>Nema stavki za ovu porudžbinu.</li>
            )}
          </ul>
          <h3 style={{ color: '#fff' }}>Adresa za dostavu</h3>
          {order.address ? (
            <p>
              {order.address.street}, {order.address.city}, {order.address.postalCode}, {order.address.country}
            </p>
          ) : (
            <Form className="address-form">
              <Form.Control
                name="street"
                placeholder="Ulica"
                value={address.street}
                onChange={handleAddressChange}
                style={{ display: 'block', margin: '10px 0' }}
              />
              <Form.Control
                name="city"
                placeholder="Grad"
                value={address.city}
                onChange={handleAddressChange}
                style={{ display: 'block', margin: '10px 0' }}
              />
              <Form.Control
                name="postalCode"
                placeholder="Poštanski broj"
                value={address.postalCode}
                onChange={handleAddressChange}
                style={{ display: 'block', margin: '10px 0' }}
              />
              <Form.Control
                name="country"
                placeholder="Država"
                value={address.country}
                onChange={handleAddressChange}
                style={{ display: 'block', margin: '10px 0' }}
              />
              <Button className="save-address-btn" onClick={handleUpdateAddress}>
                Sačuvaj adresu
              </Button>
            </Form>
          )}
        </Card>
      </Container>
    </div>
  );
};

export default OrderDetailsPage;