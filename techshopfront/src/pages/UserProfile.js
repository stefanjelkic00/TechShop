import React, { useEffect, useState } from "react";
import { api } from "../services/api";
import { toast } from "react-toastify";
import { motion, AnimatePresence } from "framer-motion";
import { FaUserCircle, FaLock, FaInfoCircle, FaTimes } from "react-icons/fa";
import { jwtDecode } from "jwt-decode";

const UserProfile = () => {
  const [userData, setUserData] = useState(null);
  const [showChangePassword, setShowChangePassword] = useState(false);
  const [showCustomerTypeModal, setShowCustomerTypeModal] = useState(false); // Stanje za modal

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const token = localStorage.getItem("token");
        if (!token) {
          toast.error("Niste ulogovani.");
          return;
        }

        const decodedToken = jwtDecode(token);
        const userEmail = decodedToken.sub;

        if (!userEmail) {
          toast.error("Email korisnika nije pronađen u tokenu.");
          return;
        }

        const response = await api.get(`/users/email/${userEmail}`);
        setUserData(response.data);
      } catch (error) {
        toast.error("Greška prilikom učitavanja profila.");
      }
    };

    fetchProfile();
  }, []);

  const handleChangePassword = async (e) => {
    e.preventDefault();
    const currentPassword = document.getElementById("currentPassword").value;
    const newPassword = document.getElementById("newPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    if (newPassword !== confirmPassword) {
      toast.error("Nove lozinke se ne poklapaju.");
      return;
    }

    try {
      const token = localStorage.getItem("token");
      const decodedToken = jwtDecode(token);
      const userEmail = decodedToken.sub;

      await api.post(`/users/change-password`, {
        email: userEmail,
        currentPassword,
        newPassword,
      });

      toast.success("Lozinka uspešno promenjena!");
      setShowChangePassword(false);
    } catch (error) {
      toast.error("Greška prilikom promene lozinke.");
    }
  };

  // Funkcija za mapiranje popusta na osnovu customerType
  const getDiscountText = (customerType) => {
    switch (customerType) {
      case "REGULAR":
        return "Nema dodatnog popusta na artikle";
      case "PREMIUM":
        return "10% popusta na artikle";
      case "PLATINUM":
        return "20% popusta na artikle";
      case "VIP":
        return "30% popusta na artikle";
      default:
        return "Nepoznat tip korisnika";
    }
  };

  // Funkcija za objašnjenje customerType
  const getCustomerTypeExplanation = (customerType) => {
    return (
      <div>
        <h3 style={{ color: "#ff4500", fontSize: "18px", marginBottom: "15px" }}>
          Informacije o tipu korisnika
        </h3>
        <p style={{ color: "#fff", marginBottom: "10px" }}>
          Vaš trenutni tip korisnika je: <strong>{customerType}</strong> ({getDiscountText(customerType)}).
        </p>
        <p style={{ color: "#fff", marginBottom: "10px" }}>
          Tip korisnika se menja na osnovu broja porudžbina koje ste napravili:
        </p>
        <ul style={{ color: "#fff", paddingLeft: "20px", marginBottom: "15px" }}>
          <li>
            <strong>REGULAR</strong>: 0 porudžbina - Nema dodatnog popusta.
          </li>
          <li>
            <strong>PREMIUM</strong>: 1-2 porudžbine - 10% popusta na artikle.
          </li>
          <li>
            <strong>PLATINUM</strong>: 3-4 porudžbine - 20% popusta na artikle.
          </li>
          <li>
            <strong>VIP</strong>: 5 ili više porudžbina - 30% popusta na artikle.
          </li>
        </ul>
        <p style={{ color: "#fff", marginBottom: "10px" }}>
          Da biste unapredili svoj tip korisnika, nastavite da kupujete! Što više porudžbina napravite, veći popust ćete ostvariti.
        </p>
      </div>
    );
  };

  if (!userData) return <p className="text-center mt-5">Učitavanje podataka...</p>;

  return (
    <div
      style={{
        backgroundColor: "#1c1c1c",
        width: "100%",
        minHeight: "100vh",
        margin: 0,
        padding: 0,
        position: "relative",
      }}
    >
      <style>
        {`
          body, html {
            margin: 0;
            padding: 0;
            height: 100%;
            background-color: #1c1c1c;
            overflow-x: hidden;
          }
          #root {
            height: 100%;
            background-color: #1c1c1c;
          }
        `}
      </style>

      {/* Sadržaj profila koji će biti zamućen kada je modal aktivan */}
      <motion.div
        className="d-flex align-items-start justify-content-center min-vh-100"
        style={{
          filter: showCustomerTypeModal ? "blur(5px)" : "none",
          transition: "filter 0.3s ease",
        }}
      >
        <motion.div
          className="card shadow-lg border-0 p-4"
          style={{
            maxWidth: "500px",
            width: "100%",
            minHeight: "400px",
            borderRadius: "15px",
            backgroundColor: "rgba(51, 51, 51, 0.95)",
            boxShadow: "0 8px 20px rgba(0, 0, 0, 0.5)",
            zIndex: 1,
            margin: "0 auto",
            marginTop: "60px",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            padding: "20px",
          }}
          initial={{ opacity: 0, y: 50 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
        >
          <div className="text-center w-100">
            <h2
              className="mb-3"
              style={{
                fontWeight: "700",
                textTransform: "uppercase",
                letterSpacing: "0.5px",
                fontSize: "16px",
                color: "#ff4500",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                paddingLeft: "5px",
              }}
            >
              <FaUserCircle style={{ marginRight: "10px", fontSize: "18px" }} /> Profil korisnika
            </h2>
            <div
              className="profile-box p-3"
              style={{
                backgroundColor: "transparent",
                width: "100%",
                textAlign: "center",
              }}
            >
              <p className="mb-2" style={{ color: "#fff" }}>
                <strong>Ime:</strong> {userData.firstName}
              </p>
              <p className="mb-2" style={{ color: "#fff" }}>
                <strong>Prezime:</strong> {userData.lastName}
              </p>
              <p className="mb-2" style={{ color: "#fff" }}>
                <strong>Korisničko ime:</strong> {userData.email}
              </p>
              <p className="mb-2" style={{ color: "#fff" }}>
                <strong>Uloga:</strong> {userData.role}
              </p>
              {/* Prikazivanje customerType i popusta sa dugmetom "Saznaj više" */}
              <p className="mb-2" style={{ color: "#fff" }}>
                <strong>Tip korisnika:</strong> {userData.customerType} ({getDiscountText(userData.customerType)}){" "}
                <motion.button
                  className="btn btn-info btn-sm"
                  onClick={() => setShowCustomerTypeModal(true)}
                  whileHover={{ scale: 1.05 }}
                  transition={{ duration: 0.3 }}
                  style={{
                    backgroundColor: "#ff4500",
                    border: "none",
                    color: "#fff",
                    padding: "5px 10px",
                    borderRadius: "5px",
                    fontSize: "12px",
                    marginLeft: "10px",
                  }}
                >
                  <FaInfoCircle style={{ marginRight: "5px" }} /> Saznaj više
                </motion.button>
              </p>
              <motion.button
                className="btn btn-profile mt-3 w-50 mx-auto d-block"
                whileHover={{ scale: 1.05 }}
                transition={{ duration: 0.3 }}
                onClick={() => setShowChangePassword(!showChangePassword)}
              >
                {showChangePassword ? "Zatvori" : "Promena lozinke"}
              </motion.button>
            </div>
          </div>

          {showChangePassword && (
            <div className="text-center w-100 mt-4">
              <h3
                className="mb-3"
                style={{
                  fontWeight: "700",
                  textTransform: "uppercase",
                  letterSpacing: "0.5px",
                  fontSize: "16px",
                  color: "#ff4500",
                  display: "flex",
                  justifyContent: "center",
                  alignItems: "center",
                }}
              >
                <FaLock style={{ marginRight: "10px", fontSize: "18px" }} /> Promena lozinke
              </h3>
              <form
                onSubmit={handleChangePassword}
                className="password-box p-3"
                style={{
                  backgroundColor: "transparent",
                  width: "100%",
                  textAlign: "center",
                }}
              >
                <div className="text-center mb-3" style={{ maxWidth: "300px", margin: "0 auto" }}>
                  <input
                    type="password"
                    className="form-control mb-2"
                    id="currentPassword"
                    placeholder="Trenutna lozinka"
                    required
                    style={{
                      borderRadius: "8px",
                      border: "1px solid #555",
                      backgroundColor: "#fff",
                      color: "#333",
                      padding: "8px",
                      fontSize: "14px",
                      width: "100%",
                    }}
                  />
                </div>
                <div className="text-center mb-3" style={{ maxWidth: "300px", margin: "0 auto" }}>
                  <input
                    type="password"
                    className="form-control mb-2"
                    id="newPassword"
                    placeholder="Nova lozinka"
                    required
                    style={{
                      borderRadius: "8px",
                      border: "1px solid #555",
                      backgroundColor: "#fff",
                      color: "#333",
                      padding: "8px",
                      fontSize: "14px",
                      width: "100%",
                    }}
                  />
                </div>
                <div className="text-center mb-3" style={{ maxWidth: "300px", margin: "0 auto" }}>
                  <input
                    type="password"
                    className="form-control mb-2"
                    id="confirmPassword"
                    placeholder="Potvrdi novu lozinku"
                    required
                    style={{
                      borderRadius: "8px",
                      border: "1px solid #555",
                      backgroundColor: "#fff",
                      color: "#333",
                      padding: "8px",
                      fontSize: "14px",
                      width: "100%",
                    }}
                  />
                </div>
                <motion.button
                  type="submit"
                  className="btn btn-profile w-50 mx-auto d-block"
                  whileHover={{ scale: 1.05 }}
                  transition={{ duration: 0.3 }}
                >
                  Sačuvaj
                </motion.button>
              </form>
            </div>
          )}
        </motion.div>
      </motion.div>

      {/* Modal za objašnjenje customerType - izdvojen iz zamućenog sloja */}
      <AnimatePresence>
        {showCustomerTypeModal && (
          <motion.div
            className="modal-overlay"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.3 }}
            style={{
              position: "fixed",
              top: 0,
              left: 0,
              width: "100%",
              height: "100%",
              backgroundColor: "rgba(0, 0, 0, 0.7)", // Tamna pozadina za dodatni kontrast
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              zIndex: 1000,
            }}
            onClick={() => setShowCustomerTypeModal(false)} // Zatvaranje modala klikom na pozadinu
          >
            <motion.div
              className="modal-content"
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.8, opacity: 0 }}
              transition={{ duration: 0.3 }}
              style={{
                backgroundColor: "rgba(51, 51, 51, 0.95)",
                borderRadius: "15px",
                padding: "20px",
                maxWidth: "500px",
                width: "90%",
                maxHeight: "80vh",
                overflowY: "auto",
                position: "relative",
                boxShadow: "0 8px 20px rgba(0, 0, 0, 0.5)",
              }}
              onClick={(e) => e.stopPropagation()} // Sprečava zatvaranje modala klikom unutar njega
            >
              <motion.button
                className="btn btn-close"
                onClick={() => setShowCustomerTypeModal(false)}
                whileHover={{ scale: 1.1 }}
                style={{
                  position: "absolute",
                  top: "10px",
                  right: "10px",
                  background: "none",
                  border: "none",
                  color: "#ff4500",
                  fontSize: "20px",
                  cursor: "pointer",
                }}
              >
                <FaTimes />
              </motion.button>
              {getCustomerTypeExplanation(userData.customerType)}
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      <style>{`
        .profile-box {
          box-shadow: none;
        }

        .password-box {
          box-shadow: none;
        }

        .btn-profile {
          background-color: #ff4500;
          color: white;
          border: none;
          padding: 10px;
          border-radius: 8px;
          font-size: 14px;
          font-weight: bold;
        }

        .btn-profile:hover {
          background-color: #e03e00;
        }

        .form-control:focus {
          border-color: #ff4500;
          box-shadow: 0 0 5px rgba(255, 69, 0, 0.5);
        }
      `}</style>
    </div>
  );
};

export default UserProfile;