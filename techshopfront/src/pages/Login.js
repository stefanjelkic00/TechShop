import React, { useEffect, useState } from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import { Button } from "react-bootstrap";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { loginUser } from "../services/api";
import { motion } from "framer-motion";
import { FaSignInAlt } from "react-icons/fa";

function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const fromCart = location.state?.fromCart;

  // Stanje za poruku i grešku iz query parametara
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  // Provera URL parametara za poruku ili grešku nakon verifikacije
  useEffect(() => {
    const queryParams = new URLSearchParams(location.search);
    const successMessage = queryParams.get("message");
    const errorMessage = queryParams.get("error");

    if (successMessage) {
      setMessage(successMessage);
      toast.success(successMessage);
    }
    if (errorMessage) {
      setError(errorMessage);
      toast.error(errorMessage);
    }
  }, [location.search]);

  return (
    <div
      className="d-flex align-items-center justify-content-center min-vh-100"
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
        className="card shadow-lg border-0 p-3"
        style={{
          maxWidth: "500px",
          width: "100%",
          minHeight: "300px",
          borderRadius: "15px",
          backgroundColor: "rgba(51, 51, 51, 0.95)",
          boxShadow: "0 8px 20px rgba(0, 0, 0, 0.5)",
          zIndex: 1,
          margin: "0 auto",
          marginTop: "60px",
          display: "flex",
          flexDirection: "column",
          justifyContent: "flex-start",
          alignItems: "center",
          paddingTop: "20px",
        }}
        initial={{ opacity: 0, y: 50 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <h6
          className="text-center mb-4"
          style={{
            fontWeight: "700",
            textTransform: "uppercase",
            letterSpacing: "0.5px",
            fontSize: "16px",
            color: "#ff4500",
          }}
        >
          <FaSignInAlt style={{ marginRight: "10px", fontSize: "18px" }} /> Prijava
        </h6>

        {fromCart && (
          <div
            className="text-center mb-3"
            style={{
              borderRadius: "8px",
              fontSize: "0.9rem",
              width: "80%",
              backgroundColor: "#333",
              color: "#ff4500",
              padding: "10px",
              border: "1px solid #444",
            }}
          >
            Morate biti prijavljeni kako biste dodali artikal u korpu i izvršili porudžbinu.
          </div>
        )}

        {/* Prikaz poruke o uspešnoj verifikaciji */}
        {message && (
          <div
            className="text-center mb-3"
            style={{
              borderRadius: "8px",
              fontSize: "0.9rem",
              width: "80%",
              backgroundColor: "#28a745",
              color: "white",
              padding: "10px",
              border: "1px solid #218838",
            }}
          >
            {message}
          </div>
        )}

        {/* Prikaz greške iz verifikacije */}
        {error && (
          <div
            className="text-center mb-3"
            style={{
              borderRadius: "8px",
              fontSize: "0.9rem",
              width: "80%",
              backgroundColor: "#dc3545",
              color: "white",
              padding: "10px",
              border: "1px solid #c82333",
            }}
          >
            {error}
          </div>
        )}

        <Formik
          initialValues={{ email: "", password: "" }}
          validationSchema={Yup.object({
            email: Yup.string()
              .email("Neispravan email")
              .required("Email je obavezan"),
            password: Yup.string().required("Lozinka je obavezna"),
          })}
          onSubmit={async (values, { setSubmitting }) => {
            try {
              await loginUser({ email: values.email, password: values.password });
              toast.success("Uspešno ste se prijavili!");
              navigate(fromCart ? "/" : "/");
            } catch (error) {
              if (error.response?.data?.error === "Registracija nije potvrđena. Proverite vaš mejl za potvrdu.") {
                toast.error("Registracija nije potvrđena. Proverite vaš mejl za potvrdu.");
              } else {
                toast.error("Neispravni kredencijali. Pokušajte ponovo.");
              }
            }
            setSubmitting(false);
          }}
        >
          {(props) => (
            <Form className="d-flex flex-column align-items-center w-100">
              <div
                className="w-75 text-center"
                style={{ marginBottom: "20px" }}
              >
                <Field
                  type="email"
                  name="email"
                  className="form-control"
                  placeholder="Unesite vaš email"
                  style={{
                    borderRadius: "8px",
                    border: "1px solid #555",
                    backgroundColor: "#fff",
                    color: "#333",
                    transition: "border-color 0.3s ease",
                    width: "100%",
                    padding: "10px",
                    fontSize: "14px",
                  }}
                />
                <ErrorMessage
                  name="email"
                  component="div"
                  className="alert alert-danger text-center py-1 mt-2"
                  style={{
                    borderRadius: "8px",
                    fontSize: "0.9rem",
                  }}
                />
              </div>

              <div
                className="w-75 text-center"
                style={{ marginBottom: "20px" }}
              >
                <Field
                  type="password"
                  name="password"
                  className="form-control"
                  placeholder="Unesite vašu lozinku"
                  style={{
                    borderRadius: "8px",
                    border: "1px solid #555",
                    backgroundColor: "#fff",
                    color: "#333",
                    transition: "border-color 0.3s ease",
                    width: "100%",
                    padding: "10px",
                    fontSize: "14px",
                  }}
                />
                <ErrorMessage
                  name="password"
                  component="div"
                  className="alert alert-danger text-center py-1 mt-2"
                  style={{
                    borderRadius: "8px",
                    fontSize: "0.9rem",
                  }}
                />
              </div>

              <motion.div
                whileHover={{ scale: 1.05 }}
                transition={{ duration: 0.3 }}
                style={{
                  display: "flex",
                  justifyContent: "center",
                  width: "50%",
                  margin: "0 auto",
                  marginBottom: "20px",
                }}
              >
                <Button
                  type="submit"
                  variant="primary"
                  className="w-100"
                  disabled={props.isSubmitting}
                  style={{
                    borderRadius: "8px",
                    padding: "10px", // Ispravljeno: uklonjen "Ascending," i postavljena validna vrednost
                    backgroundColor: "#ff4500",
                    border: "none",
                    color: "white",
                    fontWeight: "bold",
                    fontSize: "14px",
                  }}
                >
                  {props.isSubmitting ? "Prijavljivanje..." : "Prijava"}
                </Button>
              </motion.div>
            </Form>
          )}
        </Formik>
        <div
          className="text-center mt-auto"
          style={{
            display: "flex",
            justifyContent: "center",
            gap: "5px",
            fontSize: "12px",
            color: "#fff",
          }}
        >
          <span>Nemate nalog?</span>
          <Link
            to="/register"
            style={{
              color: "#ff4500",
              textDecoration: "none",
              fontWeight: "bold",
              transition: "color 0.3s ease",
              fontSize: "12px",
            }}
            onMouseEnter={(e) => (e.target.style.color = "#e03e00")}
            onMouseLeave={(e) => (e.target.style.color = "#ff4500")}
          >
            Registrujte se
          </Link>
        </div>
      </motion.div>
    </div>
  );
}

export default Login;