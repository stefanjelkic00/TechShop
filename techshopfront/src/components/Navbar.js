import React, { useEffect, useState } from "react";
import { Navbar as BootstrapNavbar, Nav, Container } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";
import { FaShoppingCart, FaClipboardList } from "react-icons/fa";
import { jwtDecode } from "jwt-decode";

function NavigationBar() {
  const [user, setUser] = useState(null);
  const [showCartDropdown, setShowCartDropdown] = useState(false);
  const navigate = useNavigate();
  const [cartItems, setCartItems] = useState([]);

  const loadUserFromToken = () => {
    const token = localStorage.getItem("token");
    if (token) {
      try {
        const decodedToken = jwtDecode(token);
        console.log("Dekodirani token:", decodedToken);
        setUser({
          firstName: decodedToken.firstName,
          lastName: decodedToken.lastName,
          customerType: decodedToken.customerType,
        });
      } catch (error) {
        console.error("Greška pri dekodiranju tokena:", error);
      }
    }
  };

  useEffect(() => {
    loadUserFromToken();

    const handleStorageChange = () => {
      loadUserFromToken();
    };
    window.addEventListener("storage", handleStorageChange);

    return () => {
      window.removeEventListener("storage", handleStorageChange);
    };
  }, []);

  const handleLogout = (e) => {
    e.preventDefault();
    localStorage.removeItem("token");
    setUser(null);
    navigate("/");
  };

  const buttonStyle = {
    fontSize: "14px",
    color: "#fff",
    fontWeight: "normal",
    textDecoration: "none",
    padding: "6px 12px",
    borderRadius: "8px",
    userSelect: "none",
    display: "flex",
    alignItems: "center",
    width: "120px",
    justifyContent: "center",
    position: "relative",
  };

  const profileButtonStyle = {
    ...buttonStyle,
    width: "auto",
    maxWidth: "200px",
    padding: "6px 12px",
    whiteSpace: "nowrap",
    overflow: "hidden",
    textOverflow: "ellipsis",
  };

  return (
    <BootstrapNavbar
      style={{
        background: "linear-gradient(to right, #FF5A1F 0%, #FF8C42 25%, #000000 50%, #333399 75%, #1E3A8A 100%)",
        height: "80px",
        width: "100%",
      }}
      variant="dark"
      expand="lg"
    >
      <style>
        {`
          .nav-button:hover {
            background-color: #ff4500 !important;
          }
          .nav-button {
            transition: background-color 0.3s ease;
          }
          .user-nav {
            display: flex;
            flex-direction: row;
            align-items: "center",
            gap: 10px;
          }
          .cart-dropdown {
            position: absolute;
            top: 60px;
            right: 10px;
            background-color: #ffffff !important; /* Osiguravamo belu pozadinu */
            color: #000;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
            padding: 10px;
            min-width: 220px;
            z-index: 1050; /* Već je dovoljno visoko za HomePage */
          }
          .cart-dropdown-item {
            padding: 5px 0;
            border-bottom: 1px solid #eee;
            color: #000; /* Osiguravamo da tekst bude crn za kontrast */
          }
          .cart-dropdown-item:last-child {
            border-bottom: none;
          }
          .navbar-container {
            overflow: hidden;
          }
        `}
      </style>

      <Container
        className="navbar-container"
        style={{
          padding: "0 20px",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          height: "100%",
          maxWidth: "100%",
        }}
      >
        <Nav className="d-flex align-items-center" style={{ height: "100%" }}>
          <BootstrapNavbar.Brand as={Link} to="/" className="d-flex align-items-center" style={{ height: "100%" }}>
            <img
              src="/techshop2.png"
              alt="Tech Shop Logo"
              style={{
                height: "80px",
                width: "80px",
                marginRight: "15px",
                objectFit: "cover",
                borderRadius: "50%",
                verticalAlign: "middle",
              }}
            />
          </BootstrapNavbar.Brand>
        </Nav>

        <div style={{ flex: "1" }}></div>

        {user ? (
          <Nav className="d-flex align-items-center user-nav" style={{ marginRight: "20px" }}>
            <Nav.Link as={Link} to="/profile" className="nav-button" style={profileButtonStyle}>
              {user.firstName} {user.lastName} | {user.customerType}
            </Nav.Link>

            <Nav.Link as={Link} to="/orders" className="nav-button" style={buttonStyle}>
              <FaClipboardList size={22} style={{ marginRight: "5px", verticalAlign: "middle" }} />
              Porudžbine
            </Nav.Link>

            <div
              style={{ position: "relative" }}
              onMouseEnter={() => setShowCartDropdown(true)}
              onMouseLeave={() => setShowCartDropdown(false)}
            >
              <Nav.Link as={Link} to="/cart" className="nav-button" style={buttonStyle}>
                <FaShoppingCart size={22} style={{ verticalAlign: "middle", color: "#fff" }} />
              </Nav.Link>
              {showCartDropdown && (
                <div className="cart-dropdown">
                  {cartItems.length > 0 ? (
                    <>
                      {cartItems.map((item) => (
                        <div key={item.id} className="cart-dropdown-item">
                          {item.name} - {item.price} RSD
                        </div>
                      ))}
                      <Link to="/cart" className="btn btn-primary w-100 mt-2">
                        Nastavi
                      </Link>
                    </>
                  ) : (
                    <div>Nema stavki u korpi</div>
                  )}
                </div>
              )}
            </div>

            <Nav.Link onClick={handleLogout} className="nav-button" style={buttonStyle}>
              Odjava
            </Nav.Link>
          </Nav>
        ) : (
          <Nav className="d-flex align-items-center user-nav" style={{ marginRight: "20px" }}>
            <div
              style={{ position: "relative" }}
              onMouseEnter={() => setShowCartDropdown(true)}
              onMouseLeave={() => setShowCartDropdown(false)}
            >
              <Nav.Link as={Link} to="/cart" className="nav-button" style={buttonStyle}>
                <FaShoppingCart size={22} style={{ verticalAlign: "middle", color: "#fff" }} />
              </Nav.Link>
              {showCartDropdown && (
                <div className="cart-dropdown">
                  {cartItems.length > 0 ? (
                    <>
                      {cartItems.map((item) => (
                        <div key={item.id} className="cart-dropdown-item">
                          {item.name} - {item.price} RSD
                        </div>
                      ))}
                      <Link to="/cart" className="btn btn-primary w-100 mt-2">
                        Nastavi
                      </Link>
                    </>
                  ) : (
                    <div>Nema stavki u korpi</div>
                  )}
                </div>
              )}
            </div>

            <Nav.Link as={Link} to="/login" className="nav-button" style={buttonStyle}>
              Prijava
            </Nav.Link>
          </Nav>
        )}
      </Container>
    </BootstrapNavbar>
  );
}

export default NavigationBar;