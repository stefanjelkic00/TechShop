import React, { useEffect, useState, useRef } from "react";
import { Container, Row, Col, Card, Button, Spinner, FormControl } from "react-bootstrap";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { getProducts, getCategories, autocompleteSearch, getCartByUserId } from "../services/api";
import { CartPlus } from "react-bootstrap-icons";
import { jwtDecode } from "jwt-decode";

function HomePage() {
  const [products, setProducts] = useState([]);
  const [category, setCategory] = useState("");
  const [sort, setSort] = useState("price_asc");
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeDropdown, setActiveDropdown] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const priceDropdownRef = useRef(null);
  const navRef = useRef(null);
  const navigate = useNavigate();

  const categoryDisplayMap = {
    LAPTOP: "Laptop",
    PHONE: "Phone",
    GAMING_EQUIPMENT: "Gaming oprema",
    SMART_DEVICES: "Pametni uređaji",
  };

  const validateAndProcessProducts = (data) => {
    if (!Array.isArray(data)) return [];
    return data.map((item) => ({
      ...item,
      price: typeof item.price === "number" ? item.price : parseFloat(item.price) || 0,
      imageUrl: item.imageUrl || "https://via.placeholder.com/300",
    })).filter((item) => item.name && typeof item.price === "number");
  };

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const minPriceValue = minPrice ? parseFloat(minPrice) : null;
        const maxPriceValue = maxPrice ? parseFloat(maxPrice) : null;

        console.log("Filtriranje sa:", { searchQuery, category, sort, minPriceValue, maxPriceValue });

        const fetchedProducts = await getProducts(searchQuery, category, sort, minPriceValue, maxPriceValue);
        if (!fetchedProducts || !Array.isArray(fetchedProducts)) {
          console.error("Podaci iz Elasticsearch-a nisu validni niz:", fetchedProducts);
          const dummyProducts = [
            { id: 1, name: "Laptop HP", price: 999.99, category: "LAPTOP", imageUrl: "https://via.placeholder.com/300" },
            { id: 2, name: "Telefon Samsung", price: 599.99, category: "PHONE", imageUrl: "https://via.placeholder.com/300" },
          ];
          const processedProducts = validateAndProcessProducts(dummyProducts);
          setProducts(processedProducts);
        } else {
          const processedProducts = validateAndProcessProducts(fetchedProducts);
          setProducts(processedProducts);
        }

        const fetchedCategories = await getCategories();
        if (fetchedCategories && Array.isArray(fetchedCategories) && fetchedCategories.length > 0) {
          setCategories(fetchedCategories);
        } else {
          setCategories(["LAPTOP", "PHONE", "GAMING_EQUIPMENT", "SMART_DEVICES"]);
        }
      } catch (error) {
        console.error("Greška pri preuzimanju podataka:", error);
        const dummyProducts = [
          { id: 1, name: "Laptop HP", price: 999.99, category: "LAPTOP", imageUrl: "https://via.placeholder.com/300" },
          { id: 2, name: "Telefon Samsung", price: 599.99, category: "PHONE", imageUrl: "https://via.placeholder.com/300" },
        ];
        const processedProducts = validateAndProcessProducts(dummyProducts);
        setProducts(processedProducts);
        setCategories(["LAPTOP", "PHONE", "GAMING_EQUIPMENT", "SMART_DEVICES"]);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [category, sort, minPrice, maxPrice, searchQuery]);

  const handleToggleDropdown = (dropdownId) => {
    setActiveDropdown(activeDropdown === dropdownId ? null : dropdownId);
  };

  const handleSearchChange = async (e) => {
    const query = e.target.value;
    setSearchQuery(query);
    if (query.length > 2) {
      try {
        const results = await autocompleteSearch(query);
        setSuggestions(results);
      } catch (error) {
        console.error("Greška pri dohvatanju sugestija:", error);
        setSuggestions([]);
      }
    } else {
      setSuggestions([]);
    }
  };

  const handleSuggestionClick = (suggestion) => {
    setSearchQuery(suggestion);
    setSuggestions([]);
  };

  const handleCategorySelect = (selectedCategory) => {
    console.log("Izabrana kategorija:", selectedCategory);
    setCategory(selectedCategory);
    setActiveDropdown(null);
  };

  const handleSortSelect = (selectedSort) => {
    console.log("Izabrano sortiranje:", selectedSort);
    setSort(selectedSort);
    setActiveDropdown(null);
  };

  const handlePriceChange = (e, type) => {
    const value = e.target.value;
    if (type === "min") {
      console.log("Nova min cena:", value);
      setMinPrice(value);
    } else {
      console.log("Nova max cena:", value);
      setMaxPrice(value);
    }
  };

  const handleProductClick = (productId) => {
    navigate(`/product/${productId}`);
  };

  const addToCart = async (product) => {
    const token = localStorage.getItem("token");
    if (!token) {
      // Redirektuj na login sa informacijom o pokušaju dodavanja u korpu
      navigate("/login", { state: { fromCart: true } });
      return;
    }

    try {
      const decodedToken = jwtDecode(token);
      const email = decodedToken.sub;
      console.log("Dekodiran email iz tokena:", email);

      // Dohvatamo korisnika preko email-a
      const userResponse = await fetch(`http://localhost:8001/api/users/email/${email}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!userResponse.ok) {
        throw new Error(`Neuspelo dohvatanje korisnika: ${userResponse.status} - ${userResponse.statusText}`);
      }
      const userText = await userResponse.text();
      let userData;
      try {
        userData = JSON.parse(userText);
      } catch (e) {
        console.error("Neispravan JSON odgovor od /api/users/email/:", userText);
        throw new Error("Neispravan odgovor od servera");
      }
      const userId = userData && userData.id ? userData.id : null;

      if (!userId || userId <= 0) {
        throw new Error("Neispravan userId: " + userId);
      }
      console.log("Pronađen userId:", userId);

      // Dohvatamo korpu korisnika
      const cartResponse = await getCartByUserId(userId);
      console.log("Odgovor od getCartByUserId:", cartResponse);
      let cartId = cartResponse.id;

      if (!cartId) {
        // Kreiraj novu korpu ako ne postoji
        const newCartDTO = { userId };
        console.log("Kreiranje nove korpe sa podacima:", newCartDTO);
        const createResponse = await fetch("http://localhost:8001/api/carts", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(newCartDTO),
        });
        if (!createResponse.ok) {
          const errorText = await createResponse.text();
          console.error("Neuspelo kreiranje korpe:", {
            status: createResponse.status,
            statusText: createResponse.statusText,
            body: errorText,
          });
          throw new Error(`Neuspelo kreiranje korpe: ${createResponse.status} - ${errorText}`);
        }
        const newCartText = await createResponse.text();
        let newCart;
        try {
          newCart = JSON.parse(newCartText);
        } catch (e) {
          console.error("Neispravan JSON odgovor od /api/carts:", newCartText);
          throw new Error("Neispravan odgovor od servera prilikom kreiranja korpe");
        }
        cartId = newCart.id;
        console.log("Nova korpa kreirana sa id:", cartId);
      }

      // Kreiramo DTO za dodavanje u korpu
      const cartItemDTO = {
        cartId: cartId,
        productId: product.id,
        quantity: 1,
      };
      console.log("Dodavanje stavke u korpu sa podacima:", cartItemDTO);

      // Šaljemo zahtev za dodavanje stavke u korpu
      const response = await fetch("http://localhost:8001/api/cart-items", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(cartItemDTO),
      });

      if (response.ok) {
        console.log(`Proizvod ${product.name} dodat u korpu!`);
        window.dispatchEvent(new Event("storage")); // Ažuriranje UI-ja
      } else {
        const errorText = await response.text();
        console.error("Greška pri dodavanju u korpu:", {
          status: response.status,
          statusText: response.statusText,
          body: errorText,
        });
        throw new Error(`Greška pri dodavanju u korpu: ${response.status} - ${errorText}`);
      }
    } catch (error) {
      console.error("Greška pri komunikaciji sa backend-om:", error);
      alert("Došlo je do greške prilikom dodavanja u korpu. Proverite konzolu za detalje.");
    }
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (priceDropdownRef.current && !priceDropdownRef.current.contains(event.target)) {
        setActiveDropdown((prev) => (prev === "price" ? null : prev));
      }
      if (navRef.current && !navRef.current.contains(event.target)) {
        setActiveDropdown(null);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <div style={{ background: "linear-gradient(135deg, #1a1a1a 0%, #444 100%)", minHeight: "100vh", padding: "20px", maxWidth: "1500px", margin: "0 auto", position: "relative", zIndex: 1 }}>
      <style>
        {`
          .header-section {
            background: #333;
            padding: 20px 0;
            border-radius: 15px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
            margin-bottom: 20px;
            width: 100%;
            border: 1px solid #444;
            position: relative;
            z-index: 2000;
          }
          .techshop-logo {
            display: block;
            margin: 0 auto 15px auto;
            max-width: 150px;
            height: auto;
            border-radius: 50%;
            object-fit: cover;
          }
          .category-nav {
            display: flex;
            justify-content: center;
            background: transparent;
            position: relative;
            z-index: 2000;
          }
          .nav-item {
            position: relative;
            margin: 0 5px;
          }
          .filter-nav-link {
            color: #fff;
            font-weight: 600;
            font-size: 1.1rem;
            padding: 10px 25px;
            transition: all 0.3s ease;
            background: #444;
            border-radius: 5px;
            text-decoration: none;
            width: 250px;
            text-align: center;
            display: flex;
            justify-content: center;
            align-items: center;
            cursor: pointer;
          }
          .filter-nav-link:hover {
            background: #ff4500;
            color: #fff;
          }
          .custom-dropdown {
            position: absolute;
            background: #fff;
            border: 1px solid #ddd;
            border-radius: 5px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
            min-width: 250px;
            padding: 10px;
            margin: 0;
            top: 100%;
            left: 50%;
            transform: translateX(-50%);
            text-align: left;
            z-index: 4000 !important;
            display: none;
          }
          .custom-dropdown.active {
            display: block;
          }
          .dropdown-item {
            color: #333;
            font-weight: 500;
            padding: 10px 20px;
            transition: all 0.3s ease;
            text-decoration: none;
            display: block;
            width: 100%;
            box-sizing: border-box;
            cursor: pointer;
          }
          .dropdown-item:hover {
            background: #ff4500;
            color: #fff;
            width: 100%;
          }
          .dropdown-toggle::after {
            border: none;
            content: "▼";
            font-size: 0.8rem;
            margin-left: 5px;
          }
          .price-filter-container {
            display: flex;
            gap: 10px;
            padding: 0;
            position: relative;
            z-index: 4000 !important;
          }
          .price-input {
            width: 100px;
            padding: 5px;
            border-radius: 5px;
            border: 1px solid #ddd;
            text-align: center;
            color: #000 !important;
            background: #fff !important;
            outline: none;
          }
          .price-input:focus {
            border-color: #ff4500;
            box-shadow: 0 0 5px rgba(255, 69, 0, 0.5);
          }
          .search-bar {
            display: flex;
            justify-content: center;
            margin-top: 20px;
            width: 100%;
            position: relative;
            z-index: 1500;
          }
          .search-input {
            width: 50%;
            max-width: 500px;
            padding: 12px 20px;
            border: none;
            border-radius: 25px;
            background: #fff !important;
            color: #000 !important;
            font-size: 1.1rem;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
            transition: all 0.3s ease;
            outline: none;
            text-align: center;
            z-index: 1500;
          }
          .search-input:focus {
            box-shadow: 0 6px 15px rgba(255, 69, 0, 0.4);
            transform: scale(1.02);
          }
          .search-input::placeholder {
            color: #888 !important;
            font-style: italic;
            text-align: center;
          }
          .suggestions-dropdown {
            position: absolute;
            top: 100%;
            left: 50%;
            transform: translateX(-50%);
            width: 50%;
            max-width: 500px;
            background: #fff;
            border: 1px solid #ddd;
            border-radius: 5px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
            z-index: 2000;
            max-height: 200px;
            overflow-y: auto;
          }
          .suggestion-item {
            padding: 10px 20px;
            color: #333;
            font-weight: 500;
            cursor: pointer;
            transition: background 0.3s ease;
          }
          .suggestion-item:hover {
            background: #ff4500;
            color: #fff;
          }
          .product-container {
            background: #333;
            border-radius: 10px;
            padding: 5px;
            height: 100%;
            display: flex;
            justify-content: center;
            align-items: center;
            width: 90%;
            margin: 0 auto;
          }
          .brutal-card {
            background: #222;
            border: 2px solid #444;
            border-radius: 10px;
            overflow: hidden;
            transition: transform 0.3s ease, box-shadow 0.3s ease, border-width 0.3s ease;
            width: 200px;
            height: 250px;
          }
          .brutal-card:hover {
            box-shadow: 0 0 15px #ff4500;
            border-width: 4px;
            border-color: #ff4500;
            transform: scale(1.05);
            cursor: pointer;
          }
          .card-image-wrapper {
            height: 150px;
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #444;
            overflow: hidden;
          }
          .card-image-wrapper img {
            height: 100%;
            width: 100%;
            object-fit: cover;
            transition: transform 0.3s ease;
          }
          .brutal-card:hover .card-image-wrapper img {
            transform: scale(1.1);
          }
          .brutal-text {
            font-family: 'Arial Black', sans-serif;
            font-size: 1rem;
            color: #fff;
            text-align: center;
            margin: 2px 0;
            max-height: 40px;
            overflow: hidden;
            overflow-wrap: break-word;
          }
          .brutal-price {
            font-size: 1.1rem;
            color: #ff4500;
            text-shadow: 1px 1px 5px #000;
            margin: 0;
          }
          .cart-button {
            background: #ff4500;
            border: none;
            border-radius: 50%;
            width: 30px;
            height: 30px;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 0;
            margin: 0;
            transition: background 0.3s ease;
          }
          .cart-button:hover {
            background: #e03e00;
          }
          .cart-icon {
            color: #fff;
            font-size: 1rem;
          }
          .card-body {
            padding: 5px;
            display: flex;
            flex-direction: column;
            height: 90px;
          }
          .price-cart-container {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 10px;
            margin-top: auto;
          }
          .product-col {
            display: flex;
            justify-content: center;
            align-items: stretch;
          }
          .debug-row {
            display: flex !important;
            flex-wrap: wrap !important;
            justify-content: center !important;
            margin-right: -15px !important;
            margin-left: -15px !important;
          }
          .debug-col {
            flex: 0 0 25% !important;
            max-width: 25% !important;
            padding-right: 15px !important;
            padding-left: 15px !important;
          }
          .product-list {
            margin-top: 40px;
          }
          @media (max-width: 575.98px) {
            .techshop-logo {
              max-width: 120px;
            }
            .filter-nav-link {
              font-size: 0.9rem;
              padding: 8px 15px;
              width: 200px;
            }
            .custom-dropdown {
              min-width: 200px;
              left: 50%;
              transform: translateX(-50%);
            }
            .brutal-card {
              width: 150px;
              height: 200px;
            }
            .card-image-wrapper {
              height: 120px;
            }
            .card-body {
              height: 70px;
            }
            .debug-col {
              flex: 0 0 100% !important;
              max-width: 100% !important;
            }
            .brutal-text {
              font-size: 0.9rem;
              max-height: 36px;
            }
            .brutal-price {
              font-size: 1rem;
            }
            .cart-button {
              width: 25px;
              height: 25px;
            }
            .cart-icon {
              font-size: 0.9rem;
            }
            .price-cart-container {
              gap: 5px;
            }
            .search-input {
              width: 95%;
              max-width: none;
              padding: 10px 15px;
              font-size: 1rem;
            }
            .search-bar {
              margin-bottom: 30px;
            }
            .price-input {
              width: 80px;
            }
            .product-list {
              margin-top: 30px;
            }
            .suggestions-dropdown {
              width: 95%;
              max-width: none;
            }
          }
        `}
      </style>

      <div className="header-section">
        <img src="/ts5.png" alt="TechShop Logo" className="techshop-logo" />
        <div className="category-nav" ref={navRef}>
          <div className="nav-item">
            <div className="filter-nav-link dropdown-toggle" onClick={() => handleToggleDropdown("category")}>
              {category ? categoryDisplayMap[category] || category : "Sve kategorije"}
            </div>
            <div className={`custom-dropdown ${activeDropdown === "category" ? "active" : ""}`}>
              <div className="dropdown-item" onClick={() => handleCategorySelect("")}>
                Sve kategorije
              </div>
              {categories.map((cat, index) => (
                <div key={index} className="dropdown-item" onClick={() => handleCategorySelect(cat)}>
                  {categoryDisplayMap[cat] || cat}
                </div>
              ))}
            </div>
          </div>

          <div className="nav-item">
            <div className="filter-nav-link dropdown-toggle" onClick={() => handleToggleDropdown("sort")}>
              {sort === "price_asc" ? "Cena: Nisko ka Visokom" : sort === "price_desc" ? "Cena: Visoko ka Niskom" : "Najpopularnije"}
            </div>
            <div className={`custom-dropdown ${activeDropdown === "sort" ? "active" : ""}`}>
              <div className="dropdown-item" onClick={() => handleSortSelect("price_asc")}>
                Cena: Nisko ka Visokom
              </div>
              <div className="dropdown-item" onClick={() => handleSortSelect("price_desc")}>
                Cena: Visoko ka Niskom
              </div>
              <div className="dropdown-item" onClick={() => handleSortSelect("popular")}>
                Najpopularnije
              </div>
            </div>
          </div>

          <div className="nav-item">
            <div className="filter-nav-link dropdown-toggle" onClick={() => handleToggleDropdown("price")}>
              Cena
            </div>
            <div className={`custom-dropdown ${activeDropdown === "price" ? "active" : ""}`}>
              <div className="price-filter-container" ref={priceDropdownRef} onClick={(e) => e.stopPropagation()}>
                <FormControl
                  type="number"
                  placeholder="Min cena"
                  value={minPrice}
                  onChange={(e) => handlePriceChange(e, "min")}
                  onFocus={(e) => e.stopPropagation()}
                  className="price-input"
                  min="0"
                />
                <FormControl
                  type="number"
                  placeholder="Max cena"
                  value={maxPrice}
                  onChange={(e) => handlePriceChange(e, "max")}
                  onFocus={(e) => e.stopPropagation()}
                  className="price-input"
                  min="0"
                />
              </div>
            </div>
          </div>
        </div>

        <div className="search-bar">
          <FormControl
            type="text"
            placeholder="Unesite pojam za pretragu"
            value={searchQuery}
            onChange={handleSearchChange}
            className="search-input"
            style={{ color: "#000", background: "#fff" }}
          />
          {suggestions.length > 0 && (
            <div className="suggestions-dropdown">
              {suggestions.map((suggestion, index) => (
                <div
                  key={index}
                  className="suggestion-item"
                  onClick={() => handleSuggestionClick(suggestion)}
                >
                  {suggestion}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {loading && (
        <div className="text-center text-light">
          <Spinner animation="border" variant="light" />
          <p>Učitavanje ...</p>
        </div>
      )}

      {!loading && products.length === 0 ? (
        <div className="text-center text-muted mt-5">
          <h4 className="text-white">Nema pronađenih proizvoda</h4>
        </div>
      ) : (
        <div className="product-list">
          <Container>
            <Row className="g-5 debug-row">
              {products.map((product) => (
                <Col key={product.id || `product-${Math.random()}`} className="product-col debug-col">
                  <Container className="product-container">
                    <motion.div
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                      transition={{ type: "spring", stiffness: 300 }}
                      onClick={() => handleProductClick(product.id)}
                    >
                      <Card className="brutal-card shadow-lg border-0">
                        <div className="card-image-wrapper">
                          <Card.Img
                            variant="top"
                            src={product.imageUrl && product.imageUrl.startsWith("http") ? product.imageUrl : "https://via.placeholder.com/300"}
                            alt={product.name}
                            className="img-fluid"
                          />
                        </div>
                        <Card.Body className="text-center d-flex flex-column card-body">
                          <Card.Title className="brutal-text">{product.name}</Card.Title>
                          <div className="price-cart-container">
                            <Card.Text className="brutal-price">${product.price ? product.price.toFixed(2) : "N/A"}</Card.Text>
                            <Button
                              className="cart-button"
                              onClick={(e) => {
                                e.stopPropagation();
                                addToCart(product);
                              }}
                            >
                              <CartPlus className="cart-icon" />
                            </Button>
                          </div>
                        </Card.Body>
                      </Card>
                    </motion.div>
                  </Container>
                </Col>
              ))}
            </Row>
          </Container>
        </div>
      )}
    </div>
  );
}

export default HomePage;