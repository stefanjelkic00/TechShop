import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Container, Row, Col, Card, Button } from "react-bootstrap";
import { motion } from "framer-motion";
import { CartPlus } from "react-bootstrap-icons";
import { getProductsWithDiscount, getProductById, addOrUpdateCartItem, getCartByUserId, getUserByEmail } from "../services/api";
import { jwtDecode } from "jwt-decode";

function ProductPage() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  // Funkcija za obradu proizvoda (slična HomePage)
  const validateAndProcessProduct = (data) => {
    if (!data) return null;

    if (data.originalPrice !== undefined && data.discountedPrice !== undefined) {
      const originalPrice = typeof data.originalPrice === "number" ? data.originalPrice : parseFloat(data.originalPrice) || 0;
      const discountedPrice = typeof data.discountedPrice === "number" ? data.discountedPrice : parseFloat(data.discountedPrice) || originalPrice;
      const stockQuantity = typeof data.stockQuantity === "number" ? data.stockQuantity : parseInt(data.stockQuantity) || 0; // Dodajemo stockQuantity
      console.log(`Obrada proizvoda ${data.name}: originalPrice=${originalPrice}, discountedPrice=${discountedPrice}, stockQuantity=${stockQuantity}`);
      return {
        ...data,
        originalPrice,
        discountedPrice,
        stockQuantity, // Dodajemo stockQuantity u procesirani objekat
        imageUrl: data.imageUrl || "https://via.placeholder.com/400",
      };
    }
    const price = typeof data.price === "number" ? data.price : parseFloat(data.price) || 0;
    const stockQuantity = typeof data.stockQuantity === "number" ? data.stockQuantity : parseInt(data.stockQuantity) || 0; // Dodajemo stockQuantity
    console.log(`Obrada proizvoda ${data.name}: price=${price}, stockQuantity=${stockQuantity}`);
    return {
      ...data,
      originalPrice: price,
      discountedPrice: price,
      stockQuantity, // Dodajemo stockQuantity u procesirani objekat
      imageUrl: data.imageUrl || "https://via.placeholder.com/400",
    };
  };

  // Funkcija za izračunavanje procenta popusta (identična HomePage)
  const getDiscountPercentage = (originalPrice, discountedPrice) => {
    if (!originalPrice || !discountedPrice || originalPrice === 0 || discountedPrice >= originalPrice) return 0;
    const discount = ((originalPrice - discountedPrice) / originalPrice) * 100;
    const roundedDiscount = Math.round(discount);
    console.log(`Popust za originalPrice=${originalPrice}, discountedPrice=${discountedPrice}: ${roundedDiscount}%`);
    return roundedDiscount;
  };

  useEffect(() => {
    const fetchProduct = async () => {
      setLoading(true);
      try {
        const token = localStorage.getItem("token");
        let fetchedProduct;

        if (token) {
          const decodedToken = jwtDecode(token);
          const email = decodedToken.sub;
          const userData = await getUserByEmail(email);
          const userId = userData && userData.id ? userData.id : null;

          if (!userId) throw new Error("Neispravan userId");

          const productsWithDiscount = await getProductsWithDiscount(userId, "", "", "", null, null);
          fetchedProduct = productsWithDiscount.find((p) => p.id === parseInt(id));

          if (!fetchedProduct) {
            fetchedProduct = await getProductById(id);
          }
        } else {
          fetchedProduct = await getProductById(id);
        }

        if (!fetchedProduct) throw new Error("Proizvod nije pronađen.");

        const processedProduct = validateAndProcessProduct(fetchedProduct);
        setProduct(processedProduct);
      } catch (err) {
        setError("Proizvod nije pronađen ili je došlo do greške.");
        console.error("Greška pri dohvatanju proizvoda:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [id]);

  const addToCart = async () => {
    // Proveravamo da li je proizvod na stanju pre dodavanja u korpu
    if (product.stockQuantity <= 0) {
      alert("Ovaj proizvod trenutno nije na stanju.");
      return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
      navigate("/login", { state: { fromCart: true } });
      return;
    }

    try {
      const decodedToken = jwtDecode(token);
      const email = decodedToken.sub;
      const userData = await getUserByEmail(email);
      const userId = userData && userData.id ? userData.id : null;

      if (!userId || userId <= 0) throw new Error("Neispravan userId: " + userId);

      const cartResponse = await getCartByUserId(userId);
      let cartId = cartResponse.id;

      if (!cartId) {
        const newCartDTO = { userId };
        const createResponse = await fetch("http://localhost:8001/api/carts", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(newCartDTO),
        });
        if (!createResponse.ok) throw new Error(`Neuspelo kreiranje korpe: ${createResponse.status}`);
        const newCart = await createResponse.json();
        cartId = newCart.id;
      }

      const cartItemDTO = {
        cartId: cartId,
        productId: parseInt(product.id),
        quantity: 1,
        price: parseFloat(product.discountedPrice),
      };

      const response = await addOrUpdateCartItem(cartItemDTO);
      if (response) {
        console.log(`Proizvod ${product.name} dodat u korpu!`);
        navigate("/cart");
      } else {
        throw new Error("Neuspelo dodavanje u korpu.");
      }
    } catch (error) {
      console.error("Greška pri dodavanju u korpu:", error);
      alert(`Došlo je do greške prilikom dodavanja u korpu: ${error.message}`);
    }
  };

  if (loading) {
    return (
      <div className="text-center text-light" style={{ padding: "20px" }}>
        <h4>Učitavanje...</h4>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="text-center text-danger" style={{ padding: "20px" }}>
        <h4>{error || "Proizvod nije pronađen."}</h4>
      </div>
    );
  }

  const discountPercentage = getDiscountPercentage(product.originalPrice, product.discountedPrice);
  const hasDiscount = discountPercentage > 0 && product.discountedPrice < product.originalPrice;
  const isOutOfStock = product.stockQuantity <= 0; // Proveravamo da li je proizvod na stanju

  return (
    <div style={{ background: "linear-gradient(135deg, #1a1a1a 0%, #444 100%)", minHeight: "100vh", padding: "20px", maxWidth: "1500px", margin: "0 auto" }}>
      <style>
        {`
          .product-card {
            background: #222;
            border: 2px solid #444;
            border-radius: 10px;
            overflow: hidden;
            width: 100%;
            max-width: 800px;
            margin: 0 auto;
          }
          .product-image-wrapper {
            height: 400px;
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #444;
            overflow: hidden;
            position: relative;
          }
          .product-image-wrapper img {
            height: 100%;
            width: 100%;
            object-fit: cover;
          }
          .product-details {
            padding: 20px;
            color: #fff;
          }
          .product-title {
            font-family: 'Arial Black', sans-serif;
            font-size: 1.5rem;
            color: #ff4500;
            text-align: center;
            margin-bottom: 10px;
          }
          .product-description {
            font-size: 1rem;
            color: #ccc;
            margin-bottom: 15px;
            text-align: center;
          }
          .product-price-container {
            text-align: center;
            margin-bottom: 20px;
          }
          .original-price {
            text-decoration: line-through;
            color: #888;
            font-size: 1.2rem;
            margin-right: 10px;
          }
          .discounted-price {
            font-size: 1.5rem;
            color: #ff4500;
            text-shadow: 1px 1px 5px #000;
          }
          .add-to-cart-btn {
            background: #ff4500;
            border: none;
            border-radius: 8px;
            padding: 10px 20px;
            font-weight: bold;
            color: #fff;
            transition: background 0.3s ease;
            width: 200px;
            margin: 0 auto;
            display: block;
          }
          .add-to-cart-btn:hover {
            background: #e03e00;
          }
          .add-to-cart-btn:disabled {
            background: #666; /* Siva boja za onemogućeno dugme */
            cursor: not-allowed;
          }
          .discount-circle {
            width: 50px;
            height: 50px;
            background: #ff4500;
            border-radius: 50%;
            color: #fff;
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 0.9rem;
            font-weight: bold;
            position: absolute;
            top: 10px;
            right: 10px;
            z-index: 10;
            text-align: center;
            line-height: 1;
          }
          @media (max-width: 575.98px) {
            .product-image-wrapper {
              height: 300px;
            }
            .product-title {
              font-size: 1.2rem;
            }
            .product-description {
              font-size: 0.9rem;
            }
            .original-price {
              font-size: 1rem;
            }
            .discounted-price {
              font-size: 1.2rem;
            }
            .add-to-cart-btn {
              width: 150px;
              padding: 8px 15px;
            }
            .discount-circle {
              width: 40px;
              height: 40px;
              font-size: 0.7rem;
            }
          }
        `}
      </style>
      <Container>
        <Row className="justify-content-center">
          <Col>
            <motion.div
              initial={{ opacity: 0, y: 50 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5 }}
            >
              <Card className="product-card shadow-lg">
                <div className="product-image-wrapper">
                  <Card.Img
                    variant="top"
                    src={product.imageUrl || "https://via.placeholder.com/400"}
                    alt={product.name}
                    className="img-fluid"
                  />
                  {hasDiscount && !isOutOfStock && (
                    <div className="discount-circle">
                      {discountPercentage}% OFF
                    </div>
                  )}
                </div>
                <Card.Body className="product-details">
                  <Card.Title className="product-title">{product.name}</Card.Title>
                  <Card.Text className="product-description">
                    {product.description || "Nema opisa za ovaj proizvod."}
                  </Card.Text>
                  <div className="product-price-container">
                    {hasDiscount && (
                      <span className="original-price">${product.originalPrice.toFixed(2)}</span>
                    )}
                    <span className="discounted-price">
                      ${product.discountedPrice.toFixed(2)}
                    </span>
                  </div>
                  <Button
                    className="add-to-cart-btn"
                    onClick={addToCart}
                    disabled={isOutOfStock} // Onemogućavamo dugme ako nema na stanju
                  >
                    {isOutOfStock ? (
                      "Nema na stanju" // Tekst unutar dugmeta ako nema na stanju
                    ) : (
                      <>
                        <CartPlus style={{ marginRight: "5px" }} /> Dodaj u korpu
                      </>
                    )}
                  </Button>
                </Card.Body>
              </Card>
            </motion.div>
          </Col>
        </Row>
      </Container>
    </div>
  );
}

export default ProductPage;