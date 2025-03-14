import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

const API_URL = 'http://localhost:8001/api';
const ELASTICSEARCH_BASE_URL = `${API_URL}/elasticsearch/products`;

export const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.defaults.jwtDecode = jwtDecode;

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      const decoded = jwtDecode(token);
      console.log('Dekodiran token u interceptoru:', decoded);
      if (decoded.exp < Date.now() / 1000) {
        console.error('Token je istekao:', decoded.exp);
        localStorage.removeItem('token');
        return Promise.reject(new Error('Token expired'));
      }
    } else {
      console.log('No token found in localStorage - skipping for public routes like Elasticsearch');
    }
    return config;
  },
  (error) => {
    console.error('Interceptor error:', error);
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    console.log('Response received:', response.data);
    return response;
  },
  (error) => {
    console.error('Response error:', {
      message: error.message,
      status: error.response ? error.response.status : 'No response',
      data: error.response ? error.response.data : 'No data',
    });
    return Promise.reject(error);
  }
);

export const loginUser = async ({ email, password }) => {
  try {
    console.log(`Logging in with email=${email}`);
    const response = await api.post(
      '/users/login',
      new URLSearchParams({ email, password }),
      { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    );
    if (response.data && response.data.jwtToken) {
      console.log('Login successful, saving token:', response.data.jwtToken);
      localStorage.setItem('token', response.data.jwtToken);
      window.dispatchEvent(new Event('storage'));
      return response.data;
    } else {
      throw new Error('Invalid response from server during login:', response.data);
    }
  } catch (error) {
    console.error('Error logging in:', error);
    throw error;
  }
};

export const getUserProfile = async () => {
  try {
    const response = await api.get('/users/profile');
    return response.data;
  } catch (error) {
    console.error('Error fetching user profile:', error);
    throw error;
  }
};

export const changePassword = async (data) => {
  try {
    const response = await api.post('/users/change-password', data);
    return response.data;
  } catch (error) {
    console.error('Error changing password:', error);
    throw error;
  }
};

export const registerUser = async (userData) => {
  try {
    const response = await api.post('/users/register', userData);
    return response.data;
  } catch (error) {
    console.error('Error registering user:', error);
    throw error;
  }
};

export const getCategories = async () => {
  try {
    console.log('Fetching categories...');
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/all`);
    if (response.data && Array.isArray(response.data)) {
      const categories = [...new Set(response.data.map((product) => product.category))].filter(Boolean);
      console.log('Categories fetched successfully:', categories);
      return categories.length > 0 ? categories : ['LAPTOP', 'PHONE', 'GAMING_EQUIPMENT', 'SMART_DEVICES'];
    } else {
      console.warn('Nevalidan odgovor od servera za kategorije:', response.data);
      return ['LAPTOP', 'PHONE', 'GAMING_EQUIPMENT', 'SMART_DEVICES'];
    }
  } catch (error) {
    console.error('Error fetching categories:', error);
    return ['LAPTOP', 'PHONE', 'GAMING_EQUIPMENT', 'SMART_DEVICES'];
  }
};

export const getProducts = async (
  searchQuery = '',
  category = '',
  sort = 'price_asc',
  minPrice = null,
  maxPrice = null
) => {
  try {
    const params = new URLSearchParams();
    if (searchQuery) params.append('query', searchQuery);
    if (category) params.append('category', category);
    if (sort) {
      const [sortField, sortDirection] = sort.split('_');
      params.append('sortBy', sortField);
      params.append('sortOrder', sortDirection || 'asc');
    }
    if (minPrice !== null && !isNaN(minPrice)) params.append('minPrice', minPrice);
    if (maxPrice !== null && !isNaN(maxPrice)) params.append('maxPrice', maxPrice);
    console.log('API params for products:', params.toString());

    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/filter`, { params });
    if (response.data && Array.isArray(response.data)) {
      console.log('Products fetched successfully:', response.data);
      return response.data;
    } else {
      console.warn('Nevalidan odgovor od servera za proizvode:', response.data);
      return [];
    }
  } catch (error) {
    console.error('Error fetching products:', error);
    return [];
  }
};

export const autocompleteSearch = async (query) => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/autocomplete`, {
      params: { query },
    });
    return response.data || [];
  } catch (error) {
    console.error('Error fetching autocomplete:', error);
    return [];
  }
};

export const searchProducts = async (query) => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/search`, {
      params: { query },
    });
    return response.data || [];
  } catch (error) {
    console.error('Error in search products:', error);
    return [];
  }
};

export const fuzzySearch = async (query) => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/search-fuzzy`, {
      params: { query },
    });
    return response.data || [];
  } catch (error) {
    console.error('Error in fuzzy search:', error);
    return [];
  }
};

export const searchWithNormalization = async (query) => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/search-normalized`, {
      params: { query },
    });
    return response.data || [];
  } catch (error) {
    console.error('Error in normalized search:', error);
    return [];
  }
};

export const searchAndSort = async (query, sortBy = 'price', sortOrder = 'asc') => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/search-sort`, {
      params: { query, sortBy, sortOrder },
    });
    return response.data || [];
  } catch (error) {
    console.error('Error sorting search results:', error);
    return [];
  }
};

export const getUserByEmail = async (email) => {
  try {
    const response = await api.get(`/users/email/${email}`);
    console.log('User fetched by email:', response.data);
    return response.data;
  } catch (error) {
    console.error('Error fetching user by email:', error);
    throw error;
  }
};

export const getCartByUserId = async (userId) => {
  try {
    console.log(`Fetching cart for userId=${userId}`);
    const response = await api.get(`/carts/user/${userId}`);
    let cart = response.data;

    if (typeof cart === 'string') {
      console.error('Neispravan JSON odgovor od /api/carts/user/:', cart);
      const idMatch = cart.match(/"id":(\d+)/);
      cart = idMatch ? { id: parseInt(idMatch[1]), cartItems: [] } : { id: null, cartItems: [] };
    } else if (!cart || typeof cart !== 'object') {
      cart = { id: null, cartItems: [] };
    }

    console.log(`Cart fetched for userId=${userId}:`, cart);
    return cart;
  } catch (error) {
    console.error('Error fetching cart by user ID:', error);
    return { id: null, cartItems: [] };
  }
};

export const getCartItemsByCartId = async (cartId) => {
  try {
    console.log(`Fetching cart items for cartId=${cartId}`);
    const response = await api.get(`/cart-items/cart/${cartId}`);
    const items = response.data || [];
    if (!items.length) {
      console.warn(`No cart items found for cartId=${cartId}`);
    }
    console.log(`Cart items fetched for cartId=${cartId}:`, items);
    return items;
  } catch (error) {
    console.error('Error fetching cart items by cart ID:', error);
    throw error;
  }
};

export const getProductById = async (productId) => {
  try {
    const response = await api.get(`/products/${productId}`);
    console.log(`Product fetched for ID ${productId}:`, response.data);
    return response.data;
  } catch (error) {
    console.error(`Error fetching product with ID ${productId}:`, error);
    throw error;
  }
};

export const getUserCartItems = async (userId) => {
  try {
    const cart = await getCartByUserId(userId);
    if (!cart.id) {
      console.warn(`No cart found for userId=${userId}`);
      return [];
    }
    const cartItems = await getCartItemsByCartId(cart.id);
    return cartItems;
  } catch (error) {
    console.error('Error fetching user cart items:', error);
    return [];
  }
};

export const addOrUpdateCartItem = async (cartItemDTO) => {
  try {
    console.log(`Adding/Updating cart item:`, cartItemDTO);
    const response = await api.post(`/cart-items`, cartItemDTO);
    console.log(`Cart item added/updated:`, response.data);
    return response.data;
  } catch (error) {
    console.error('Error adding/updating cart item:', error);
    throw error;
  }
};

export const updateCartItemQuantity = async (cartItemId, quantity) => {
  try {
    if (!cartItemId) {
      throw new Error('cartItemId je undefined');
    }
    console.log(`Updating cart item ${cartItemId} with quantity ${quantity}`);
    const response = await api.put(`/cart-items/${cartItemId}`, { id: cartItemId, quantity });
    console.log(`Cart item updated:`, response.data);
    return response.data;
  } catch (error) {
    console.error('Error updating cart item quantity:', error.response?.data || error.message);
    throw error;
  }
};

export const deleteCartItem = async (cartItemId) => {
  try {
    if (!cartItemId) {
      throw new Error('cartItemId je undefined');
    }
    console.log(`Deleting cart item ${cartItemId}`);
    const response = await api.delete(`/cart-items/${cartItemId}`);
    console.log(`Cart item deleted:`, response.status);
    return response.status;
  } catch (error) {
    console.error('Error deleting cart item:', error.response?.data || error.message);
    throw error;
  }
};

export { jwtDecode };
export default api;