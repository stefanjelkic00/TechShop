import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, getCartItemsByCartId, getProductById, getProductsWithDiscount, updateCartItemQuantity, deleteCartItem, jwtDecode, checkout } from "../services/api";

const CartPage = () => {
  const [cartItems, setCartItems] = useState([]);
  const [products, setProducts] = useState({});
  const [error, setError] = useState(null);
  const [totalPrice, setTotalPrice] = useState(0);
  const [cartId, setCartId] = useState(null);
  const [address, setAddress] = useState({
    street: "",
    city: "",
    postalCode: "",
    country: "",
  });

  const navigate = useNavigate();

  const fetchCartItems = async () => {
    const token = localStorage.getItem("token");
    if (!token) {
      setError("Niste prijavljeni. Molimo prijavite se.");
      navigate("/login");
      return;
    }

    try {
      const decodedToken = jwtDecode(token);
      console.log("Dekodiran token:", decodedToken);
      if (!decodedToken || !decodedToken.sub) {
        setError("Neispravan token. Molimo ponovo se prijavite.");
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      const email = decodedToken.sub;
      console.log("Dekodiran email iz tokena:", email);

      const userResponse = await api.get(`/users/email/${email}`);
      const userData = userResponse.data;
      const userId = userData?.id;
      if (!userId) {
        throw new Error("Nije pronađen userId u odgovoru");
      }
      console.log("Pronađen userId:", userId);

      const cartResponse = await api.get(`/carts/user/${userId}`);
      const cartData = cartResponse.data;
      const cartId = cartData?.id;
      if (!cartId) {
        setCartItems([]);
        setTotalPrice(0);
        setCartId(null);
        setError("Korpa nije pronađena za ovog korisnika. Dodajte proizvode u korpu.");
        return;
      }
      setCartId(cartId);
      console.log("Pronađen cartId:", cartId);

      const itemsResponse = await getCartItemsByCartId(cartId);
      const items = itemsResponse || [];
      console.log("Struktura cartItems:", items);
      if (items.length === 0) {
        setError("Korpa je trenutno prazna. Dodajte proizvode pre nastavka.");
      }
      setCartItems(items);

      // Dohvatanje proizvoda sa popustima
      const productsWithDiscount = await getProductsWithDiscount(userId, "", "", "", null, null);
      const productDetails = {};
      let total = 0;
      for (const item of items) {
        if (item.productId) {
          try {
            // Pronađi proizvod sa popustom ako postoji, inače koristi getProductById
            const discountedProduct = productsWithDiscount.find((p) => p.id === item.productId);
            const product = discountedProduct || (await getProductById(item.productId));
            productDetails[item.productId] = product;
            const priceToUse = product.discountedPrice !== undefined ? product.discountedPrice : product.price || 0;
            total += priceToUse * (item.quantity || 0);
          } catch (error) {
            console.error(`Greška pri dohvatanju proizvoda sa ID ${item.productId}:`, error);
            productDetails[item.productId] = { name: "Nepoznat proizvod", price: 0, imageUrl: "", originalPrice: 0, discountedPrice: 0 };
          }
        }
      }

      setProducts(productDetails);
      setTotalPrice(total);
    } catch (error) {
      console.error("Greška pri dohvatanju korpe:", error);
      setError(`Neuspelo dohvatanje korpe: ${error.response?.data?.message || error.message}`);
    }
  };

  const handleAddQuantity = async (item) => {
    try {
      if (!item.id) {
        throw new Error("Item nema ispravnog id-a");
      }
      const newQuantity = item.quantity + 1;
      await updateCartItemQuantity(item.id, newQuantity);
      await fetchCartItems();
    } catch (error) {
      console.error("Greška pri dodavanju količine:", error);
      setError("Neuspelo dodavanje količine: " + (error.response?.data?.message || error.message));
    }
  };

  const handleRemoveQuantity = async (item) => {
    try {
      if (!item.id) {
        throw new Error("Item nema ispravnog id-a");
      }
      if (item.quantity <= 1) {
        await handleRemoveItem(item);
        return;
      }
      const newQuantity = item.quantity - 1;
      await updateCartItemQuantity(item.id, newQuantity);
      await fetchCartItems();
    } catch (error) {
      console.error("Greška pri smanjenju količine:", error);
      setError("Neuspelo smanjenje količine: " + (error.response?.data?.message || error.message));
    }
  };

  const handleRemoveItem = async (item) => {
    try {
      if (!item.id) {
        throw new Error("Item nema ispravnog id-a");
      }
      await deleteCartItem(item.id);
      await fetchCartItems();
    } catch (error) {
      console.error("Greška pri uklanjanju stavke:", error);
      setError("Neuspelo uklanjanje stavke: " + (error.response?.data?.message || error.message));
    }
  };

  const handleOrder = async () => {
    const token = localStorage.getItem("token");
    if (!token) {
      setError("Niste prijavljeni. Molimo prijavite se.");
      navigate("/login");
      return;
    }

    if (cartItems.length === 0) {
      setError("Korpa je prazna. Dodajte proizvode pre naručivanja.");
      return;
    }

    try {
      const decodedToken = jwtDecode(token);
      const email = decodedToken.sub;
      console.log("Šaljem zahtev sa tokenom:", token);
      const userResponse = await api.get(`/users/email/${email}`);
      const userData = userResponse.data;
      const userId = userData?.id;

      if (!userId) {
        throw new Error("Nije pronađen userId u odgovoru");
      }

      if (!address.street || !address.city || !address.postalCode || !address.country) {
        setError("Molimo popunite sva polja adrese.");
        return;
      }

      console.log("Šaljem zahtev za checkout sa podacima:", { userId, address });
      await checkout(userId, address, navigate);
    } catch (error) {
      console.error("Detalji greške:", error.response || error);
      if (error.message === "Token expired") {
        setError("Vaša sesija je istekla. Molimo prijavite se ponovo.");
        navigate("/login");
      } else if (error.response?.status === 403) {
        setError("Nemate ovlašćenje za ovu akciju. Proverite da li ste prijavljeni.");
      } else if (error.response?.status === 400 && error.response?.data?.includes("Korpa nije pronađena")) {
        setError("Korpa nije pronađena. Dodajte proizvode u korpu pre naručivanja.");
        navigate("/products");
      } else if (error.response?.status === 400) {
        setError(`Greška: ${error.response.data}`);
      } else {
        setError(`Greška pri kreiranju porudžbine: ${error.response?.data?.message || error.message}`);
      }
    }
  };

  const handleAddressChange = (e) => {
    setAddress({ ...address, [e.target.name]: e.target.value });
  };

  useEffect(() => {
    fetchCartItems();
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h1>Vaša korpa</h1>
      {error && <p style={{ color: "red" }}>{error}</p>}
      {cartItems.length === 0 && !error ? (
        <p>
          Korpa je prazna. <a href="/products" style={{ color: "blue", textDecoration: "underline" }}>Dodajte proizvode</a>.
        </p>
      ) : (
        <>
          <ul style={{ listStyleType: "none", padding: 0 }}>
            {cartItems.map((item) => {
              const product = products[item.productId];
              const priceToUse = product?.discountedPrice !== undefined ? product.discountedPrice : product?.price || 0;
              const originalPrice = product?.originalPrice !== undefined ? product.originalPrice : product?.price || 0;
              const discountPercentage = originalPrice > 0 && priceToUse < originalPrice ? Math.round(((originalPrice - priceToUse) / originalPrice) * 100) : 0;
              const hasDiscount = discountPercentage > 0;
              const itemTotal = priceToUse * (item.quantity || 0);
              return (
                <li
                  key={item.id}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    marginBottom: "20px",
                    borderBottom: "1px solid #ccc",
                    paddingBottom: "10px",
                    position: "relative",
                  }}
                >
                  {product && product.imageUrl ? (
                    <div style={{ position: "relative", marginRight: "20px" }}>
                      <img
                        src={product.imageUrl}
                        alt={product.name}
                        style={{ width: "100px", height: "100px" }}
                      />
                      {hasDiscount && (
                        <div
                          style={{
                            width: "40px",
                            height: "40px",
                            background: "#ff4500",
                            borderRadius: "50%",
                            color: "#fff",
                            display: "flex",
                            justifyContent: "center",
                            alignItems: "center",
                            fontSize: "0.7rem",
                            fontWeight: "bold",
                            position: "absolute",
                            top: "0",
                            right: "0",
                            zIndex: "10",
                            textAlign: "center",
                            lineHeight: "1",
                          }}
                        >
                          {discountPercentage}% OFF
                        </div>
                      )}
                    </div>
                  ) : (
                    <div
                      style={{
                        width: "100px",
                        height: "100px",
                        backgroundColor: "#eee",
                        marginRight: "20px",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                    >
                      Nema slike
                    </div>
                  )}
                  <div style={{ flex: 1 }}>
                    <h3>{product?.name || "Nepoznat proizvod"}</h3>
                    <div style={{ marginBottom: "10px" }}>
                      {hasDiscount ? (
                        <>
                          <span style={{ textDecoration: "line-through", color: "#888", fontSize: "0.9rem", marginRight: "5px" }}>
                            ${originalPrice.toFixed(2)}
                          </span>
                          <span style={{ fontSize: "1.1rem", color: "#ff4500", textShadow: "1px 1px 5px #000" }}>
                            ${priceToUse.toFixed(2)}
                          </span>
                        </>
                      ) : (
                        <span style={{ fontSize: "1.1rem", color: "#ff4500", textShadow: "1px 1px 5px #000" }}>
                          ${priceToUse.toFixed(2)}
                        </span>
                      )}
                    </div>
                    <p>Količina: {item.quantity}</p>
                    <div style={{ margin: "10px 0" }}>
                      <button
                        onClick={() => handleAddQuantity(item)}
                        style={{
                          padding: "5px 10px",
                          marginRight: "10px",
                          backgroundColor: "#4CAF50",
                          color: "white",
                          border: "none",
                          borderRadius: "4px",
                          cursor: "pointer",
                        }}
                      >
                        Dodaj
                      </button>
                      <button
                        onClick={() => handleRemoveQuantity(item)}
                        style={{
                          padding: "5px 10px",
                          marginRight: "10px",
                          backgroundColor: "#f44336",
                          color: "white",
                          border: "none",
                          borderRadius: "4px",
                          cursor: "pointer",
                        }}
                      >
                        Ukloni
                      </button>
                      <button
                        onClick={() => handleRemoveItem(item)}
                        style={{
                          padding: "5px 10px",
                          backgroundColor: "#ff9800",
                          color: "white",
                          border: "none",
                          borderRadius: "4px",
                          cursor: "pointer",
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
          </ul>
          <div style={{ marginTop: "20px", fontSize: "1.2em", fontWeight: "bold" }}>
            Ukupna cena: ${totalPrice.toFixed(2)}
          </div>

          <div style={{ marginTop: "20px" }}>
            <h3>Unesite adresu za dostavu</h3>
            <div>
              <label>Ulica: </label>
              <input
                type="text"
                name="street"
                value={address.street}
                onChange={handleAddressChange}
                placeholder="Unesite ulicu"
                style={{ display: "block", margin: "10px 0", padding: "5px", width: "300px" }}
              />
            </div>
            <div>
              <label>Grad: </label>
              <input
                type="text"
                name="city"
                value={address.city}
                onChange={handleAddressChange}
                placeholder="Unesite grad"
                style={{ display: "block", margin: "10px 0", padding: "5px", width: "300px" }}
              />
            </div>
            <div>
              <label>Poštanski broj: </label>
              <input
                type="text"
                name="postalCode"
                value={address.postalCode}
                onChange={handleAddressChange}
                placeholder="Unesite poštanski broj"
                style={{ display: "block", margin: "10px 0", padding: "5px", width: "300px" }}
              />
            </div>
            <div>
              <label>Država: </label>
              <input
                type="text"
                name="country"
                value={address.country}
                onChange={handleAddressChange}
                placeholder="Unesite državu"
                style={{ display: "block", margin: "10px 0", padding: "5px", width: "300px" }}
              />
            </div>
            <button
              onClick={handleOrder}
              style={{
                marginTop: "20px",
                padding: "10px 20px",
                backgroundColor: "#4CAF50",
                color: "white",
                border: "none",
                borderRadius: "4px",
                cursor: "pointer",
              }}
            >
              Poruči
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default CartPage;