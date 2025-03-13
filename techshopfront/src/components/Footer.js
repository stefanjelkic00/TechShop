import React from "react";
import { Container, Row, Col } from "react-bootstrap";
import { Link } from "react-router-dom";

function Footer() {
  return (
    <footer
      style={{
        background: "linear-gradient(to right, #FF5A1F 0%, #FF8C42 20%, #000000 40%, #000000 70%, #333399 80%, #1E3A8A 100%)", // Crna dominira od 40% do 70%
        color: "white",
        padding: "15px 0",
        fontSize: "12px",
        marginTop: "auto",
        width: "100%",
        borderTop: "1px solid rgba(255, 255, 255, 0.2)",
        overflowX: "hidden",
      }}
    >
      <Container style={{ padding: "0 20px" }}>
        <Row className="align-items-center" style={{ display: "flex", justifyContent: "space-between" }}>
          {/* Leva trećina - O Nama */}
          <Col xs={12} sm={4} style={{ flex: "0 0 33.33%", maxWidth: "33.33%" }} className="mb-3 mb-sm-0 text-center">
            <h6
              style={{
                fontWeight: "700",
                marginBottom: "8px",
                textTransform: "uppercase",
                letterSpacing: "0.5px",
                fontSize: "14px",
                textAlign: "center",
              }}
            >
              O Nama
            </h6>
            <p style={{ fontSize: "12px", lineHeight: "1.4", margin: 0, textAlign: "center" }}>
              TechShop - Najbolje cene i brza dostava.
            </p>
          </Col>

          {/* Centralna trećina - Linkovi (centrirano) */}
          <Col xs={12} sm={4} style={{ flex: "0 0 33.33%", maxWidth: "33.33%" }} className="mb-3 mb-sm-0 text-center">
            <h6
              style={{
                fontWeight: "700",
                marginBottom: "8px",
                textTransform: "uppercase",
                letterSpacing: "0.5px",
                fontSize: "14px",
                textAlign: "center",
              }}
            >
              Linkovi
            </h6>
            <div style={{ display: "flex", justifyContent: "center", gap: "15px", flexWrap: "wrap" }}>
              {["Početna", "Korpa", "Porudžbine", "Profil"].map((item) => (
                <Link
                  key={item}
                  to={`/${item === "Početna" ? "" : item.toLowerCase()}`}
                  style={{
                    color: "white",
                    textDecoration: "none",
                    fontSize: "12px",
                    transition: "color 0.3s ease",
                  }}
                  onMouseEnter={(e) => (e.target.style.color = "#ffffffcc")}
                  onMouseLeave={(e) => (e.target.style.color = "white")}
                >
                  {item}
                </Link>
              ))}
            </div>
          </Col>

          {/* Desna trećina - Kontakt & Info */}
          <Col xs={12} sm={4} style={{ flex: "0 0 33.33%", maxWidth: "33.33%" }} className="mb-3 mb-sm-0 text-center">
            <h6
              style={{
                fontWeight: "700",
                marginBottom: "8px",
                textTransform: "uppercase",
                letterSpacing: "0.5px",
                fontSize: "14px",
                textAlign: "center",
              }}
            >
              Kontakt & Info
            </h6>
            <div style={{ fontSize: "12px", textAlign: "center" }}>
              <p style={{ margin: "0 0 4px" }}>
                Email: <a href="mailto:support@techshop.com" style={{ color: "white", textDecoration: "underline" }}>support@techshop.com</a>
              </p>
              <p style={{ margin: "0 0 4px" }}>Tel: +381 11 1234 567</p>
              <p style={{ margin: "0 0 4px" }}>Radno vreme: Pon-Pet 9-17h</p>
              <p style={{ margin: "0 0 4px" }}>Adresa: Beograd, Srbija</p>
              <div style={{ marginTop: "8px", display: "flex", gap: "10px", justifyContent: "center" }}>
                <a href="https://facebook.com" target="_blank" rel="noopener noreferrer" style={{ color: "white", transition: "color 0.3s ease" }} onMouseEnter={(e) => (e.target.style.color = "#ffffffcc")} onMouseLeave={(e) => (e.target.style.color = "white")}>
                  <i className="bi bi-facebook" style={{ fontSize: "14px" }}></i>
                </a>
                <a href="https://instagram.com" target="_blank" rel="noopener noreferrer" style={{ color: "white", transition: "color 0.3s ease" }} onMouseEnter={(e) => (e.target.style.color = "#ffffffcc")} onMouseLeave={(e) => (e.target.style.color = "white")}>
                  <i className="bi bi-instagram" style={{ fontSize: "14px" }}></i>
                </a>
                <a href="https://twitter.com" target="_blank" rel="noopener noreferrer" style={{ color: "white", transition: "color 0.3s ease" }} onMouseEnter={(e) => (e.target.style.color = "#ffffffcc")} onMouseLeave={(e) => (e.target.style.color = "white")}>
                  <i className="bi bi-twitter" style={{ fontSize: "14px" }}></i>
                </a>
              </div>
            </div>
          </Col>
        </Row>
        <hr style={{ borderTop: "1px solid rgba(255, 255, 255, 0.3)", margin: "10px 0" }} />
        <Row>
          <Col className="text-center">
            <p style={{ margin: 0, fontSize: "11px", opacity: "0.8" }}>
              © {new Date().getFullYear()} TechShop. Sva prava zadržana.
            </p>
          </Col>
        </Row>
      </Container>
    </footer>
  );
}

export default Footer;