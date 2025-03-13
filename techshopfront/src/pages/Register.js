import React from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import { Button } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import axios from "axios";
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
        className="card shadow-lg border-0 p-4" // Povećan padding sa p-3 na p-4 za veći unutrašnji razmak
        style={{
          maxWidth: "500px",
          width: "100%",
          minHeight: "600px", // Podesio na 600px da se uklopi sa više polja
          borderRadius: "15px",
          backgroundColor: "rgba(51, 51, 51, 0.95)",
          boxShadow: "0 8px 20px rgba(0, 0, 0, 0.5)",
          zIndex: 1,
          margin: "0 auto",
          marginTop: "80px",
          display: "flex",
          flexDirection: "column",
          justifyContent: "center", // Centriranje sadržaja vertikalno
          alignItems: "center",
          padding: "20px", // Dodat unutrašnji padding umesto samo paddingTop
        }}
        initial={{ opacity: 0, y: 50 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <img
          src="/ts5.png"
          alt="Tech Shop Logo"
          style={{
            width: "100px",
            height: "100px",
            marginBottom: "20px", // Održan razmak od 20px ispod loga
            objectFit: "cover",
            borderRadius: "50%",
          }}
        />
        <h6
          className="text-center mb-3" // Smanjen razmak ispod naslova na mb-3
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
            firstName: Yup.string()
              .required("Ime je obavezno")
              .min(2, "Mora imati najmanje 2 karaktera"),
            lastName: Yup.string()
              .required("Prezime je obavezno")
              .min(2, "Mora imati najmanje 2 karaktera"),
            email: Yup.string()
              .email("Neispravan email format")
              .required("Email je obavezan"),
            password: Yup.string()
              .min(6, "Lozinka mora imati najmanje 6 karaktera")
              .required("Lozinka je obavezna"),
          })}
          onSubmit={async (values, { setSubmitting, resetForm }) => {
            try {
              const response = await axios.post(
                "http://localhost:8001/api/users/register",
                values
              );

              if (response.status === 200 || response.status === 201) {
                toast.success("Registracija uspešna! Sada se možete prijaviti.");
                resetForm();
                navigate("/login");
              } else {
                throw new Error("Neočekivan odgovor od servera");
              }
            } catch (error) {
              toast.error("Registracija nije uspela. Pokušajte ponovo.");
            }
            setSubmitting(false);
          }}
        >
          {({ isSubmitting }) => (
            <Form className="d-flex flex-column align-items-center w-100">
              <div className="w-75 text-center" style={{ marginBottom: "15px" }}>
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
                    width: "100%",
                    padding: "10px",
                    fontSize: "14px",
                  }}
                />
                <ErrorMessage name="firstName" component="div" className="alert alert-danger text-center py-1 mt-2" />
              </div>

              <div className="w-75 text-center" style={{ marginBottom: "15px" }}>
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
                    width: "100%",
                    padding: "10px",
                    fontSize: "14px",
                  }}
                />
                <ErrorMessage name="lastName" component="div" className="alert alert-danger text-center py-1 mt-2" />
              </div>

              <div className="w-75 text-center" style={{ marginBottom: "15px" }}>
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
                    width: "100%",
                    padding: "10px",
                    fontSize: "14px",
                  }}
                />
                <ErrorMessage name="email" component="div" className="alert alert-danger text-center py-1 mt-2" />
              </div>

              <div className="w-75 text-center" style={{ marginBottom: "15px" }}>
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
                    width: "100%",
                    padding: "10px",
                    fontSize: "14px",
                  }}
                />
                <ErrorMessage name="password" component="div" className="alert alert-danger text-center py-1 mt-2" />
              </div>

              <motion.div
                whileHover={{ scale: 1.05 }}
                transition={{ duration: 0.3 }}
                style={{
                  display: "flex",
                  justifyContent: "center",
                  width: "50%",
                  margin: "0 auto",
                  marginBottom: "20px", // Održan razmak od 20px do donjeg dela
                }}
              >
                <Button
                  type="submit"
                  variant="primary"
                  className="w-100"
                  disabled={isSubmitting}
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
                  {isSubmitting ? "Registracija..." : "Registruj se"}
                </Button>
              </motion.div>
            </Form>
          )}
        </Formik>
        <div
          className="text-center mt-3" // Promenjeno sa mt-auto na mt-3 za ravnomerni razmak
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