import React, { useEffect, useState } from 'react';
import { getCartItemsByCartId, getProductById, jwtDecode, addOrUpdateCartItem, updateCartItemQuantity, deleteCartItem } from '../services/api';

const CartPage = () => {
  const [cartItems, setCartItems] = useState([]);
  const [products, setProducts] = useState({});
  const [error, setError] = useState(null);
  const [totalPrice, setTotalPrice] = useState(0);
  const [cartId, setCartId] = useState(null);

  const fetchCartItems = async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      setError('Niste prijavljeni. Molimo prijavite se.');
      return;
    }

    try {
      const decodedToken = jwtDecode(token);
      console.log('Dekodiran token:', decodedToken);
      if (!decodedToken || !decodedToken.sub) {
        setError('Neispravan token. Molimo ponovo se prijavite.');
        return;
      }

      const email = decodedToken.sub;
      console.log('Dekodiran email iz tokena:', email);

      const userResponse = await fetch(`http://localhost:8001/api/users/email/${email}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!userResponse.ok) {
        throw new Error(`Neuspelo dohvatanje korisnika: ${userResponse.status} - ${await userResponse.text()}`);
      }
      const userData = await userResponse.json();
      const userId = userData?.id;
      if (!userId) {
        throw new Error('Nije pronađen userId u odgovoru');
      }
      console.log('Pronađen userId:', userId);

      const cartResponse = await fetch(`http://localhost:8001/api/carts/user/${userId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!cartResponse.ok) {
        const errorText = await cartResponse.text();
        throw new Error(`Neuspelo dohvatanje korpe: ${cartResponse.status} - ${errorText}`);
      }
      const cartData = await cartResponse.json();
      const cartId = cartData?.id;
      if (!cartId) {
        setCartItems([]);
        setTotalPrice(0);
        setCartId(null);
        setError('Korpa nije pronađena za ovog korisnika.');
        return;
      }
      setCartId(cartId);
      console.log('Pronađen cartId:', cartId);

      const itemsResponse = await getCartItemsByCartId(cartId);
      const items = itemsResponse || [];
      console.log('Struktura cartItems:', items);
      if (items.length === 0) {
        setError('Korpa je prazna ili podaci nisu ispravno dohvaćeni. Proverite back-end.');
      }
      setCartItems(items);

      const productDetails = {};
      let total = 0;
      for (const item of items) {
        if (item.productId) {
          try {
            const product = await getProductById(item.productId);
            productDetails[item.productId] = product;
            total += (product.price || 0) * (item.quantity || 0);
          } catch (error) {
            console.error(`Greška pri dohvatanju proizvoda sa ID ${item.productId}:`, error);
            productDetails[item.productId] = { name: 'Nepoznat proizvod', price: 0, imageUrl: '' };
          }
        }
      }

      setProducts(productDetails);
      setTotalPrice(total);
    } catch (error) {
      console.error('Greška pri dohvatanju korpe:', error);
      setError(`Neuspelo dohvatanje korpe: ${error.message}`);
    }
  };

  const handleAddQuantity = async (item) => {
    try {
      if (!item.id) {
        throw new Error('Item nema ispravnog id-a');
      }
      const newQuantity = item.quantity + 1;
      await updateCartItemQuantity(item.id, newQuantity);
      await fetchCartItems();
    } catch (error) {
      console.error('Greška pri dodavanju količine:', error);
      setError('Neuspelo dodavanje količine: ' + (error.response?.data || error.message));
    }
  };

  const handleRemoveQuantity = async (item) => {
    try {
      if (!item.id) {
        throw new Error('Item nema ispravnog id-a');
      }
      if (item.quantity <= 1) {
        await handleRemoveItem(item);
        return;
      }
      const newQuantity = item.quantity - 1;
      await updateCartItemQuantity(item.id, newQuantity);
      await fetchCartItems();
    } catch (error) {
      console.error('Greška pri smanjenju količine:', error);
      setError('Neuspelo smanjenje količine: ' + (error.response?.data || error.message));
    }
  };

  const handleRemoveItem = async (item) => {
    try {
      if (!item.id) {
        throw new Error('Item nema ispravnog id-a');
      }
      await deleteCartItem(item.id);
      await fetchCartItems();
    } catch (error) {
      console.error('Greška pri uklanjanju stavke:', error);
      setError('Neuspelo uklanjanje stavke: ' + (error.response?.data || error.message));
    }
  };

  useEffect(() => {
    fetchCartItems();
  }, []);

  return (
    <div style={{ padding: '20px' }}>
      <h1>Vaša korpa</h1>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {cartItems.length === 0 && !error ? (
        <p>Korpa je prazna.</p>
      ) : (
        <ul style={{ listStyleType: 'none', padding: 0 }}>
          {cartItems.map((item) => {
            const product = products[item.productId];
            const itemTotal = (product?.price || 0) * (item.quantity || 0);
            return (
              <li
                key={item.id} // Sada koristimo item.id
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  marginBottom: '20px',
                  borderBottom: '1px solid #ccc',
                  paddingBottom: '10px',
                }}
              >
                {product && product.imageUrl ? (
                  <img
                    src={product.imageUrl}
                    alt={product.name}
                    style={{ width: '100px', height: '100px', marginRight: '20px' }}
                  />
                ) : (
                  <div
                    style={{
                      width: '100px',
                      height: '100px',
                      backgroundColor: '#eee',
                      marginRight: '20px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    Nema slike
                  </div>
                )}
                <div style={{ flex: 1 }}>
                  <h3>{product?.name || 'Nepoznat proizvod'}</h3>
                  <p>Cena po komadu: ${product?.price?.toFixed(2) || 'N/A'}</p>
                  <p>Količina: {item.quantity}</p>
                  <div style={{ margin: '10px 0' }}>
                    <button
                      onClick={() => handleAddQuantity(item)}
                      style={{
                        padding: '5px 10px',
                        marginRight: '10px',
                        backgroundColor: '#4CAF50',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer',
                      }}
                    >
                      Dodaj
                    </button>
                    <button
                      onClick={() => handleRemoveQuantity(item)}
                      style={{
                        padding: '5px 10px',
                        marginRight: '10px',
                        backgroundColor: '#f44336',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer',
                      }}
                    >
                      Ukloni
                    </button>
                    <button
                      onClick={() => handleRemoveItem(item)}
                      style={{
                        padding: '5px 10px',
                        backgroundColor: '#ff9800',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer',
                      }}
                    >
                      Ukloni stavku
                    </button>
                  </div>
                  <p>Ukupno za stavku: ${itemTotal.toFixed(2)}</p>
                </div>
              </li>
            );
          })}
          <div style={{ marginTop: '20px', fontSize: '1.2em', fontWeight: 'bold' }}>
            Ukupna cena: ${totalPrice.toFixed(2)}
          </div>
        </ul>
      )}
    </div>
  );
};

export default CartPage;