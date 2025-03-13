import axios from "axios";

const API_URL = "http://localhost:8001/api";
const ELASTICSEARCH_BASE_URL = `${API_URL}/elasticsearch/products`;

export const api = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Interceptor za request (dodavanje tokena)
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log("Token added to request:", token);
    } else {
      console.log("No token found in localStorage - skipping for public routes like Elasticsearch");
    }
    return config;
  },
  (error) => {
    console.error("Interceptor error:", error);
    return Promise.reject(error);
  }
);

// Interceptor za odgovore
api.interceptors.response.use(
  (response) => {
    console.log("Response received:", response.data);
    return response;
  },
  (error) => {
    console.error("Response error:", {
      message: error.message,
      status: error.response ? error.response.status : "No response",
      data: error.response ? error.response.data : "No data",
    });
    return Promise.reject(error);
  }
);

// Login korisnika
export const loginUser = async ({ email, password }) => {
  try {
    console.log(`Logging in with email=${email}`);
    const response = await api.post(
      "/users/login",
      new URLSearchParams({ email, password }),
      { headers: { "Content-Type": "application/x-www-form-urlencoded" } }
    );

    if (response.data && response.data.jwtToken) {
      console.log("Login successful, saving token:", response.data.jwtToken);
      localStorage.setItem("token", response.data.jwtToken);
      window.dispatchEvent(new Event("storage"));
      return response.data;
    } else {
      throw new Error("Invalid response from server during login:", response.data);
    }
  } catch (error) {
    console.error("Error logging in:", error);
    throw error;
  }
};

// Dohvatanje profila korisnika
export const getUserProfile = async () => {
  try {
    const response = await api.get("/users/profile");
    return response.data;
  } catch (error) {
    console.error("Error fetching user profile:", error);
    throw error;
  }
};

// Promena lozinke
export const changePassword = async (data) => {
  try {
    const response = await api.post("/users/change-password", data);
    return response.data;
  } catch (error) {
    console.error("Error changing password:", error);
    throw error;
  }
};

// Registracija korisnika
export const registerUser = async (userData) => {
  try {
    const response = await api.post("/users/register", userData);
    return response.data;
  } catch (error) {
    console.error("Error registering user:", error);
    throw error;
  }
};

// Dohvatanje kategorija
export const getCategories = async () => {
  try {
    console.log("Fetching categories...");
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/all`);
    if (response.data && Array.isArray(response.data)) {
      const categories = [...new Set(response.data.map((product) => product.category))].filter(Boolean);
      console.log("Categories fetched successfully:", categories);
      return categories.length > 0 ? categories : ["LAPTOP", "PHONE", "GAMING_EQUIPMENT", "SMART_DEVICES"];
    } else {
      console.warn("Nevalidan odgovor od servera za kategorije:", response.data);
      return ["LAPTOP", "PHONE", "GAMING_EQUIPMENT", "SMART_DEVICES"];
    }
  } catch (error) {
    console.error("Error fetching categories:", error);
    return ["LAPTOP", "PHONE", "GAMING_EQUIPMENT", "SMART_DEVICES"];
  }
};

// Dohvatanje proizvoda sa filterima
export const getProducts = async (
  searchQuery = "",
  category = "",
  sort = "price_asc",
  minPrice = null,
  maxPrice = null
) => {
  try {
    const params = new URLSearchParams();
    if (searchQuery) params.append("query", searchQuery);
    if (category) params.append("category", category);
    if (sort) {
      const [sortField, sortDirection] = sort.split("_");
      params.append("sortBy", sortField);
      params.append("sortOrder", sortDirection || "asc");
    }
    if (minPrice !== null && !isNaN(minPrice)) params.append("minPrice", minPrice);
    if (maxPrice !== null && !isNaN(maxPrice)) params.append("maxPrice", maxPrice);
    console.log("API params for products:", params.toString());

    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/filter`, { params });
    if (response.data && Array.isArray(response.data)) {
      console.log("Products fetched successfully:", response.data);
      return response.data;
    } else {
      console.warn("Nevalidan odgovor od servera za proizvode:", response.data);
      return [];
    }
  } catch (error) {
    console.error("Error fetching products:", error);
    return [];
  }
};

// Autocomplete pretraga
export const autocompleteSearch = async (query) => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/autocomplete`, {
      params: { query },
    });
    return response.data || [];
  } catch (error) {
    console.error("Error fetching autocomplete:", error);
    return [];
  }
};

// Full-text pretraga
export const searchProducts = async (query) => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/search`, {
      params: { query },
    });
    return response.data || [];
  } catch (error) {
    console.error("Error in search products:", error);
    return [];
  }
};

// Fuzzy pretraga
export const fuzzySearch = async (query) => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/search-fuzzy`, {
      params: { query },
    });
    return response.data || [];
  } catch (error) {
    console.error("Error in fuzzy search:", error);
    return [];
  }
};

// Normalizovana pretraga
export const searchWithNormalization = async (query) => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/search-normalized`, {
      params: { query },
    });
    return response.data || [];
  } catch (error) {
    console.error("Error in normalized search:", error);
    return [];
  }
};

// Pretraga i sortiranje
export const searchAndSort = async (query, sortBy = "price", sortOrder = "asc") => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/search-sort`, {
      params: { query, sortBy, sortOrder },
    });
    return response.data || [];
  } catch (error) {
    console.error("Error sorting search results:", error);
    return [];
  }
};

export default api;