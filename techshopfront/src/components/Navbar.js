import React, { useEffect, useState } from "react";
import { Navbar as BootstrapNavbar, Nav, Container } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";
import { FaShoppingCart, FaClipboardList } from "react-icons/fa";
import { jwtDecode } from "jwt-decode";

function NavigationBar() {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();
  const [cartItems, setCartItems] = useState([]);
  const [isAdmin, setIsAdmin] = useState(false);

  const loadUserFromToken = () => {
    const token = localStorage.getItem("token");
    if (token) {
      try {
        const decodedToken = jwtDecode(token);
        console.log("Dekodirani token:", decodedToken);
        const userData = {
          id: decodedToken.id,
          firstName: decodedToken.firstName,
          lastName: decodedToken.lastName,
          customerType: decodedToken.customerType,
          roles: decodedToken.roles || [],
        };
        setUser(userData);
        // IZMENA: Proveravamo "ROLE_ADMIN" umesto "ADMIN"
        const adminStatus = Array.isArray(decodedToken.roles) && decodedToken.roles.includes("ROLE_ADMIN");
        setIsAdmin(adminStatus);
        console.log("isAdmin set to:", adminStatus, "Roles:", decodedToken.roles || "not found");
        return decodedToken.id;
      } catch (error) {
        console.error("Greška pri dekodiranju tokena:", error);
        return null;
      }
    }
    return null;
  };

  const fetchCartItems = async (userId) => {
    if (!userId) return;
    try {
      const cartResponse = await fetch(`http://localhost:8080/api/carts/user/${userId}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      if (!cartResponse.ok) {
        throw new Error("Failed to fetch cart");
      }
      const cart = await cartResponse.json();
      setCartItems(cart.cartItems || []);
    } catch (error) {
      console.error("Greška pri dohvatanju korpe:", error);
      setCartItems([]);
    }
  };

  useEffect(() => {
    const userId = loadUserFromToken();
    if (userId) {
      fetchCartItems(userId);
    }

    const handleStorageChange = () => {
      const newUserId = loadUserFromToken();
      if (newUserId) {
        fetchCartItems(newUserId);
      } else {
        setCartItems([]);
      }
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
    setCartItems([]);
    setIsAdmin(false);
    navigate("/");

    // Emituj događaj za osvežavanje proizvoda na HomePage
    window.dispatchEvent(new CustomEvent("logout"));
  };

  const buttonStyle = {
    fontSize: "14px",
    color: "#fff",
    fontWeight: "normal",
    textDecoration: "none",
    padding: "4px 8px",
    borderRadius: "8px",
    userSelect: "none",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    width: "120px",
    minWidth: "120px",
    minHeight: "30px",
    backgroundColor: "rgba(0, 0, 0, 0.3)",
    boxShadow: "0 2px 4px rgba(0, 0, 0, 0.2)",
    overflow: "hidden",
  };

  const profileButtonStyle = {
    ...buttonStyle,
    width: "auto",
    maxWidth: "200px",
    padding: "4px 8px",
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
            margin-left: 5px;
            width: 120px;
            min-width: 120px;
            min-height: 30px;
            text-align: center;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: "hidden";
          }
          .nav-button.with-icon {
            display: flex;
            align-items: center;
            justify-content: center;
            flex-wrap: nowrap;
          }
          .nav-button.with-icon svg {
            margin-right: 3px;
            flex-shrink: 0;
          }
          .user-nav {
            display: flex;
            flex-direction: row;
            align-items: center;
            gap: 10px;
          }
          .admin-nav {
            display: flex;
            flex-direction: row;
            align-items: center;
            gap: 10px;
          }
          .navbar-container {
            overflow: visible;
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
          <>
            {isAdmin && (
              <Nav className="d-flex align-items-center admin-nav" style={{ marginRight: "20px" }}>
                <Nav.Link as={Link} to="/admin/users" className="nav-button" style={buttonStyle}>
                  Korisnici
                </Nav.Link>
                <Nav.Link as={Link} to="/admin/products" className="nav-button" style={buttonStyle}>
                  Proizvodi
                </Nav.Link>
                <Nav.Link as={Link} to="/admin/orders" className="nav-button" style={buttonStyle}>
                  Porudžbine Kor.
                </Nav.Link>
              </Nav>
            )}
            <Nav className="d-flex align-items-center user-nav" style={{ marginRight: "20px" }}>
              <Nav.Link as={Link} to="/profile" className="nav-button" style={profileButtonStyle}>
                {user.firstName} {user.lastName} | {user.customerType}
              </Nav.Link>

              <Nav.Link as={Link} to="/orders" className="nav-button with-icon" style={buttonStyle}>
                <FaClipboardList size={18} style={{ marginRight: "3px", verticalAlign: "middle" }} />
                Porudžbine
              </Nav.Link>

              <Nav.Link as={Link} to="/cart" className="nav-button with-icon" style={buttonStyle}>
                <FaShoppingCart size={18} style={{ marginRight: "3px", verticalAlign: "middle", color: "#fff" }} />
              </Nav.Link>

              <Nav.Link onClick={handleLogout} className="nav-button" style={buttonStyle}>
                Odjava
              </Nav.Link>
            </Nav>
          </>
        ) : (
          <Nav className="d-flex align-items-center user-nav" style={{ marginRight: "20px" }}>
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