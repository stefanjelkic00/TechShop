import React, { useEffect, useState } from "react";
import { api } from "../services/api";
import { toast } from "react-toastify";
import { motion } from "framer-motion";
import { FaUserCircle, FaLock } from "react-icons/fa";
import { jwtDecode } from "jwt-decode";

const UserProfile = () => {
  const [userData, setUserData] = useState(null);
  const [showChangePassword, setShowChangePassword] = useState(false);

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

  if (!userData) return <p className="text-center mt-5">Učitavanje podataka...</p>;

  return (
    <div
      className="d-flex align-items-start justify-content-center min-vh-100"
      style={{
        backgroundColor: "#1c1c1c",
        width: "100%",
        height: "100vh",
        margin: 0,
        padding: 0,
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
              paddingLeft: "5px", // Pomeranje desno za malu količinu
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
            <p className="mb-2" style={{ color: "#fff" }}><strong>Ime:</strong> {userData.firstName}</p>
            <p className="mb-2" style={{ color: "#fff" }}><strong>Prezime:</strong> {userData.lastName}</p>
            <p className="mb-2" style={{ color: "#fff" }}><strong>Korisničko ime:</strong> {userData.email}</p>
            <p className="mb-2" style={{ color: "#fff" }}><strong>Uloga:</strong> {userData.role}</p>
            {userData.customerType === "VIP" && (
              <p className="mb-2" style={{ color: "#fff" }}><strong>Tip korisnika:</strong> {userData.customerType}</p>
            )}
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
      </motion.div>
    </div>
  );
};

export default UserProfile;