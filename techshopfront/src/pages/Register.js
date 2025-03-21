import React from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import { Button } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { registerUser } from "../services/api";
import { motion } from "framer-motion";
import { FaUserPlus } from "react-icons/fa";

function Register() {
  const navigate = useNavigate();

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
          minHeight: "400px",
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
          <FaUserPlus style={{ marginRight: "10px", fontSize: "18px" }} /> Registracija
        </h6>

        <Formik
          initialValues={{ firstName: "", lastName: "", email: "", password: "" }}
          validationSchema={Yup.object({
            firstName: Yup.string().required("Ime je obavezno"),
            lastName: Yup.string().required("Prezime je obavezno"),
            email: Yup.string()
              .email("Neispravan email")
              .required("Email je obavezan"),
            password: Yup.string()
              .min(6, "Lozinka mora imati najmanje 6 karaktera")
              .required("Lozinka je obavezna"),
          })}
          onSubmit={async (values, { setSubmitting }) => {
            try {
              await registerUser(values);
              toast.success("Registracija je započeta! Proverite vaš email za potvrdu.");
              navigate("/login");
            } catch (error) {
              toast.error(error.response?.data?.message || "Greška pri registraciji. Pokušajte ponovo.");
            }
            setSubmitting(false);
          }}
        >
          {(props) => (
            <Form className="d-flex flex-column align-items-center w-100">
              <div className="w-75 text-center" style={{ marginBottom: "20px" }}>
                <Field
                  type="text"
                  name="firstName"
                  className="form-control"
                  placeholder="Unesite vaše ime"
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
                  name="firstName"
                  component="div"
                  className="alert alert-danger text-center py-1 mt-2"
                  style={{ borderRadius: "8px", fontSize: "0.9rem" }}
                />
              </div>

              <div className="w-75 text-center" style={{ marginBottom: "20px" }}>
                <Field
                  type="text"
                  name="lastName"
                  className="form-control"
                  placeholder="Unesite vaše prezime"
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
                  name="lastName"
                  component="div"
                  className="alert alert-danger text-center py-1 mt-2"
                  style={{ borderRadius: "8px", fontSize: "0.9rem" }}
                />
              </div>

              <div className="w-75 text-center" style={{ marginBottom: "20px" }}>
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
                  style={{ borderRadius: "8px", fontSize: "0.9rem" }}
                />
              </div>

              <div className="w-75 text-center" style={{ marginBottom: "20px" }}>
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
                  style={{ borderRadius: "8px", fontSize: "0.9rem" }}
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
                    padding: "10px",
                    backgroundColor: "#ff4500",
                    border: "none",
                    color: "white",
                    fontWeight: "bold",
                    fontSize: "14px",
                  }}
                >
                  {props.isSubmitting ? "Registracija..." : "Registruj se"}
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
          <span>Već imate nalog?</span>
          <Link
            to="/login"
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
            Prijavite se
          </Link>
        </div>
      </motion.div>
    </div>
  );
}

export default Register;