import React, { useEffect, useState } from "react";
import { api } from "./../../services/api";

const AdminProductsPage = () => {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [newProduct, setNewProduct] = useState({
        name: "",
        description: "",
        price: 0,
        stockQuantity: 0,
        imageUrl: "",
        category: ""
    });

    useEffect(() => {
        const fetchProducts = async () => {
            try {
                const response = await api.get("/products");
                setProducts(response.data);
            } catch (error) {
                console.error("Greška pri dohvatanju proizvoda:", error);
            }
        };

        const fetchCategories = async () => {
            try {
                const response = await api.get("/products/categories");
                setCategories(response.data);
                if (response.data.length > 0) {
                    setNewProduct((prev) => ({ ...prev, category: response.data[0] }));
                }
            } catch (error) {
                console.error("Greška pri dohvatanju kategorija:", error);
            }
        };

        fetchProducts();
        fetchCategories();
    }, []);

    const handleCreate = async () => {
        try {
            const response = await api.post("/products", {
                name: newProduct.name,
                description: newProduct.description,
                price: Number(newProduct.price),
                stockQuantity: Number(newProduct.stockQuantity),
                imageUrl: newProduct.imageUrl,
                category: newProduct.category
            });
            setProducts([...products, response.data]);
            setNewProduct({
                name: "",
                description: "",
                price: 0,
                stockQuantity: 0,
                imageUrl: "",
                category: categories[0] || ""
            });
        } catch (error) {
            console.error("Greška pri kreiranju proizvoda:", error.response?.data || error.message);
        }
    };

    const handleUpdate = async (id, updatedData) => {
        try {
            const updatedProduct = {
                id: id,
                name: updatedData.name || "",
                description: updatedData.description || "",
                price: Number(updatedData.price) || 0,
                stockQuantity: Number(updatedData.stockQuantity) || 0,
                imageUrl: updatedData.imageUrl || "",
                category: updatedData.category || categories[0] || ""
            };
            const response = await api.put(`/products/${id}`, updatedProduct);
            setProducts(products.map((product) => (product.id === id ? { ...product, ...response.data } : product)));
        } catch (error) {
            console.error("Greška pri ažuriranju proizvoda:", error.response?.data || error.message);
        }
    };

    const handleDelete = async (id) => {
        try {
            await api.delete(`/products/${id}`);
            setProducts(products.filter((product) => product.id !== id));
        } catch (error) {
            console.error("Greška pri brisanju proizvoda:", error);
        }
    };

    return (
        <div className="container mt-4">
            <h2>Proizvodi</h2>
            <style>
                {`
                    .form-group {
                        display: inline-block;
                        margin-right: 1rem;
                        margin-bottom: 1rem;
                        text-align: center;
                    }
                    .form-group label {
                        display: block;
                        margin-bottom: 0.5rem;
                        font-weight: 500;
                    }
                    .form-group input, .form-group select {
                        text-align: center;
                        width: 150px;
                    }
                    .table th, .table td {
                        padding: 0.75rem 1.5rem;
                        vertical-align: middle;
                        text-align: center;
                    }
                `}
            </style>

            <div className="mb-4">
                <div className="form-group">
                    <label>Naziv</label>
                    <input
                        type="text"
                        value={newProduct.name}
                        onChange={(e) => setNewProduct({ ...newProduct, name: e.target.value })}
                        placeholder="Naziv"
                        className="form-control"
                    />
                </div>
                <div className="form-group">
                    <label>Opis</label>
                    <input
                        type="text"
                        value={newProduct.description}
                        onChange={(e) => setNewProduct({ ...newProduct, description: e.target.value })}
                        placeholder="Opis"
                        className="form-control"
                    />
                </div>
                <div className="form-group">
                    <label>Cena</label>
                    <input
                        type="number"
                        value={newProduct.price}
                        onChange={(e) => setNewProduct({ ...newProduct, price: e.target.value })}
                        placeholder="Cena"
                        className="form-control"
                    />
                </div>
                <div className="form-group">
                    <label>Količina</label>
                    <input
                        type="number"
                        value={newProduct.stockQuantity}
                        onChange={(e) => setNewProduct({ ...newProduct, stockQuantity: e.target.value })}
                        placeholder="Količina"
                        className="form-control"
                    />
                </div>
                <div className="form-group">
                    <label>URL slike</label>
                    <input
                        type="text"
                        value={newProduct.imageUrl}
                        onChange={(e) => setNewProduct({ ...newProduct, imageUrl: e.target.value })}
                        placeholder="URL slike"
                        className="form-control"
                    />
                </div>
                <div className="form-group">
                    <label>Kategorija</label>
                    <select
                        value={newProduct.category}
                        onChange={(e) => setNewProduct({ ...newProduct, category: e.target.value })}
                        className="form-control"
                    >
                        {categories.map((category) => (
                            <option key={category} value={category}>
                                {category}
                            </option>
                        ))}
                    </select>
                </div>
                <div className="form-group">
                    <label> </label>
                    <button className="btn btn-success" onClick={handleCreate}>
                        Kreiraj
                    </button>
                </div>
            </div>

            <table className="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Naziv</th>
                        <th>Opis</th>
                        <th>Cena</th>
                        <th>Količina</th>
                        <th>Akcije</th>
                    </tr>
                </thead>
                <tbody>
                    {products.map((product) => (
                        <tr key={product.id}>
                            <td>{product.id}</td>
                            <td>{product.name}</td>
                            <td>{product.description || "Nema opisa"}</td>
                            <td>{product.price}</td>
                            <td>{product.stockQuantity}</td>
                            <td>
                                <button
                                    className="btn btn-warning me-2"
                                    onClick={() => {
                                        const newName = prompt("Novi naziv:", product.name);
                                        const newDescription = prompt("Novi opis:", product.description);
                                        const newPrice = prompt("Nova cena:", product.price);
                                        const newStockQuantity = prompt("Nova količina:", product.stockQuantity);
                                        const newImageUrl = prompt("Novi URL slike:", product.imageUrl || "");
                                        const newCategory = prompt("Nova kategorija:", product.category || "");

                                        if (newName && newDescription && newPrice && newStockQuantity) {
                                            handleUpdate(product.id, {
                                                name: newName,
                                                description: newDescription,
                                                price: newPrice,
                                                stockQuantity: newStockQuantity,
                                                imageUrl: newImageUrl,
                                                category: newCategory
                                            });
                                        } else {
                                            alert("Sva obavezna polja moraju biti popunjena!");
                                        }
                                    }}
                                >
                                    Ažuriraj
                                </button>
                                <button className="btn btn-danger" onClick={() => handleDelete(product.id)}>
                                    Obriši
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default AdminProductsPage;