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

// Interceptor za zahteve
api.interceptors.request.use(
  async (config) => {
    const token = localStorage.getItem('token');
    console.log('Slanje zahteva za:', config.url, 'sa tokenom:', token);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      const decoded = jwtDecode(token);
      console.log('Dekodiran token u interceptoru:', decoded);
      if (decoded.exp < Date.now() / 1000) {
        console.log('Token je istekao, pokušavam refresh...');
        try {
          await refreshToken();
          const newToken = localStorage.getItem('token');
          if (newToken) {
            config.headers.Authorization = `Bearer ${newToken}`;
            console.log('Novi token nakon refresh-a:', newToken);
          } else {
            throw new Error('Failed to refresh token');
          }
        } catch (refreshError) {
          console.error('Refresh failed:', refreshError);
          localStorage.removeItem('token');
          setTimeout(() => {
            if (window.location.pathname !== '/login') {
              window.location.href = '/login';
            }
          }, 2000); // Odlažem preusmeravanje za 2 sekunde radi logova
          return Promise.reject(new Error('Token refresh failed'));
        }
      }
    } else if (config.url.startsWith('/carts') || config.url.startsWith('/orders') || config.url.startsWith('/cart-items') || config.url.startsWith('/users/profile')) {
      console.log('Nema tokena za zaštićeni endpoint, čekam 2 sekunde pre preusmeravanja');
      setTimeout(() => {
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
      }, 2000);
      return Promise.reject(new Error('No token available'));
    } else {
      console.log('Nema tokena u localStorage - preskačem za javne rute poput Elasticsearch-a');
    }
    return config;
  },
  (error) => {
    console.error('Interceptor error:', error);
    return Promise.reject(error);
  }
);

// Interceptor za odgovore
api.interceptors.response.use(
  (response) => {
    console.log('Uspešan odgovor za:', response.config.url, 'Status:', response.status, 'Data:', response.data);
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    console.error('Greška u odgovoru za:', originalRequest.url, {
      message: error.message,
      status: error.response ? error.response.status : 'No response',
      data: error.response ? error.response.data : 'No data',
    });

    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        await refreshToken();
        const newToken = localStorage.getItem('token');
        if (newToken) {
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          console.log('Ponovni zahtev sa novim tokenom:', newToken);
          return api(originalRequest); // Ponovi zahtev sa novim tokenom
        } else {
          throw new Error('No new token after refresh');
        }
      } catch (refreshError) {
        console.error('Refresh failed:', refreshError);
        localStorage.removeItem('token');
        setTimeout(() => {
          if (window.location.pathname !== '/login') {
            window.location.href = '/login';
          }
        }, 2000);
        return Promise.reject(new Error('Token refresh failed'));
      }
    } else if (error.response && error.response.status === 403) {
      console.warn('403 Forbidden - Nemate dozvolu, čekam 2 sekunde pre preusmeravanja');
      setTimeout(() => {
        localStorage.removeItem('token');
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
      }, 2000);
      return Promise.reject(new Error('Forbidden access'));
    }
    return Promise.reject(error);
  }
);

// Funkcija za login
export const loginUser = async ({ email, password }) => {
  try {
    console.log(`Prijavljivanje sa email=${email}`);
    const response = await api.post(
      '/users/login',
      new URLSearchParams({ email, password }),
      { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    );
    if (response.data && response.data.jwtToken) {
      console.log('Uspešna prijava, čuvam token:', response.data.jwtToken);
      localStorage.setItem('token', response.data.jwtToken);
      window.dispatchEvent(new Event('storage'));
      return response.data;
    } else {
      throw new Error('Nevažeći odgovor od servera tokom prijave:', response.data);
    }
  } catch (error) {
    console.error('Greška pri prijavi:', error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za dohvatanje profila korisnika
export const getUserProfile = async () => {
  try {
    const response = await api.get('/users/profile');
    console.log('Profil korisnika dohvaćen:', response.data);
    return response.data;
  } catch (error) {
    console.error('Greška pri dohvatanju profila korisnika:', error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za promenu lozinke
export const changePassword = async (data) => {
  try {
    const response = await api.post('/users/change-password', data);
    console.log('Lozinka uspešno promenjena:', response.data);
    return response.data;
  } catch (error) {
    console.error('Greška pri promeni lozinke:', error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za registraciju korisnika
export const registerUser = async (userData) => {
  try {
    const response = await api.post('/users/register', userData);
    console.log('Korisnik uspešno registrovan:', response.data);
    return response.data;
  } catch (error) {
    console.error('Greška pri registraciji korisnika:', error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za dohvatanje kategorija
export const getCategories = async () => {
  try {
    console.log('Dohvatam kategorije...');
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/all`);
    if (response.data && Array.isArray(response.data)) {
      const categories = [...new Set(response.data.map((product) => product.category))].filter(Boolean);
      console.log('Kategorije uspešno dohvaćene:', categories);
      return categories.length > 0 ? categories : ['LAPTOP', 'PHONE', 'GAMING_EQUIPMENT', 'SMART_DEVICES'];
    } else {
      console.warn('Nevalidan odgovor od servera za kategorije:', response.data);
      return ['LAPTOP', 'PHONE', 'GAMING_EQUIPMENT', 'SMART_DEVICES'];
    }
  } catch (error) {
    console.error('Greška pri dohvatanju kategorija:', error.response?.data || error.message);
    return ['LAPTOP', 'PHONE', 'GAMING_EQUIPMENT', 'SMART_DEVICES'];
  }
};

// Funkcija za dohvatanje proizvoda
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
    console.log('API parametri za proizvode:', params.toString());

    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/filter`, { params });
    if (response.data && Array.isArray(response.data)) {
      console.log('Proizvodi uspešno dohvaćeni:', response.data);
      return response.data;
    } else {
      console.warn('Nevalidan odgovor od servera za proizvode:', response.data);
      return [];
    }
  } catch (error) {
    console.error('Greška pri dohvatanju proizvoda:', error.response?.data || error.message);
    return [];
  }
};

// Funkcija za dohvatanje proizvoda sa popustom
export const getProductsWithDiscount = async (
  userId,
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
    console.log('API parametri za proizvode sa popustom:', params.toString());

    const response = await api.get(`/products/discounted/${userId}`, { params });
    if (response.data && Array.isArray(response.data)) {
      console.log('Proizvodi sa popustom uspešno dohvaćeni:', response.data);
      return response.data;
    } else {
      console.warn('Nevalidan odgovor od servera za popustovane proizvode:', response.data);
      return [];
    }
  } catch (error) {
    console.error('Greška pri dohvatanju proizvoda sa popustom:', error.response?.data || error.message);
    return [];
  }
};

// Funkcija za autocomplete pretragu
export const autocompleteSearch = async (query) => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/autocomplete`, {
      params: { query },
    });
    console.log('Autocomplete rezultati:', response.data);
    return response.data || [];
  } catch (error) {
    console.error('Greška pri autocomplete pretrazi:', error.response?.data || error.message);
    return [];
  }
};

// Funkcija za pretragu proizvoda
export const searchProducts = async (query) => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/search`, {
      params: { query },
    });
    console.log('Rezultati pretrage:', response.data);
    return response.data || [];
  } catch (error) {
    console.error('Greška pri pretrazi proizvoda:', error.response?.data || error.message);
    return [];
  }
};

// Funkcija za fuzzy pretragu
export const fuzzySearch = async (query) => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/search-fuzzy`, {
      params: { query },
    });
    console.log('Fuzzy pretraga rezultati:', response.data);
    return response.data || [];
  } catch (error) {
    console.error('Greška pri fuzzy pretrazi:', error.response?.data || error.message);
    return [];
  }
};

// Funkcija za pretragu sa normalizacijom
export const searchWithNormalization = async (query) => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/search-normalized`, {
      params: { query },
    });
    console.log('Normalizovana pretraga rezultati:', response.data);
    return response.data || [];
  } catch (error) {
    console.error('Greška pri normalizovanoj pretrazi:', error.response?.data || error.message);
    return [];
  }
};

// Funkcija za pretragu i sortiranje
export const searchAndSort = async (query, sortBy = 'price', sortOrder = 'asc') => {
  try {
    const response = await api.get(`${ELASTICSEARCH_BASE_URL}/search-sort`, {
      params: { query, sortBy, sortOrder },
    });
    console.log('Sortirani rezultati pretrage:', response.data);
    return response.data || [];
  } catch (error) {
    console.error('Greška pri sortiranju rezultata pretrage:', error.response?.data || error.message);
    return [];
  }
};

// Funkcija za dohvatanje korisnika po emailu
export const getUserByEmail = async (email) => {
  try {
    const response = await api.get(`/users/email/${email}`);
    console.log('Korisnik dohvaćen po emailu:', response.data);
    return response.data;
  } catch (error) {
    console.error('Greška pri dohvatanju korisnika po emailu:', error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za dohvatanje korpe po userId
export const getCartByUserId = async (userId) => {
  try {
    console.log(`Dohvatam korpu za userId=${userId}`);
    const response = await api.get(`/carts/user/${userId}`);
    let cart = response.data;

    if (typeof cart === 'string') {
      console.error('Neispravan JSON odgovor od /api/carts/user/:', cart);
      const idMatch = cart.match(/"id":(\d+)/);
      cart = idMatch ? { id: parseInt(idMatch[1]), cartItems: [] } : { id: null, cartItems: [] };
    } else if (!cart || typeof cart !== 'object') {
      cart = { id: null, cartItems: [] };
    }

    console.log(`Korpa dohvaćena za userId=${userId}:`, cart);
    return cart;
  } catch (error) {
    console.error('Greška pri dohvatanju korpe po userId:', error.response?.data || error.message);
    return { id: null, cartItems: [] };
  }
};

// Funkcija za dohvatanje stavki korpe po cartId
export const getCartItemsByCartId = async (cartId) => {
  try {
    console.log(`Dohvatam stavke korpe za cartId=${cartId}`);
    const response = await api.get(`/cart-items/cart/${cartId}`);
    const items = response.data || [];
    if (!items.length) {
      console.warn(`Nema stavki korpe za cartId=${cartId}`);
    }
    console.log(`Stavke korpe dohvaćene za cartId=${cartId}:`, items);
    return items;
  } catch (error) {
    console.error('Greška pri dohvatanju stavki korpe po cartId:', error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za dohvatanje proizvoda po ID-ju
export const getProductById = async (productId) => {
  try {
    const response = await api.get(`/products/${productId}`);
    console.log(`Proizvod dohvaćen za ID ${productId}:`, response.data);
    return response.data;
  } catch (error) {
    console.error(`Greška pri dohvatanju proizvoda sa ID ${productId}:`, error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za dohvatanje stavki korpe korisnika
export const getUserCartItems = async (userId) => {
  try {
    const cart = await getCartByUserId(userId);
    if (!cart.id) {
      console.warn(`Nema korpe za userId=${userId}`);
      return [];
    }
    const cartItems = await getCartItemsByCartId(cart.id);
    console.log(`Stavke korpe korisnika za userId=${userId}:`, cartItems);
    return cartItems;
  } catch (error) {
    console.error('Greška pri dohvatanju stavki korpe korisnika:', error.response?.data || error.message);
    return [];
  }
};

// Funkcija za dodavanje ili ažuriranje stavke korpe
export const addOrUpdateCartItem = async (cartItemDTO) => {
  try {
    console.log(`Dodajem/ažuriram stavku korpe:`, cartItemDTO);
    const response = await api.post(`/cart-items`, cartItemDTO);
    console.log(`Stavka korpe dodana/ažurirana:`, response.data);
    return response.data;
  } catch (error) {
    console.error('Greška pri dodavanju/ažuriranju stavke korpe:', error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za ažuriranje količine stavke korpe
export const updateCartItemQuantity = async (cartItemId, quantity) => {
  try {
    if (!cartItemId) {
      throw new Error('cartItemId je undefined');
    }
    console.log(`Ažuriram stavku korpe ${cartItemId} sa količinom ${quantity}`);
    const response = await api.put(`/cart-items/${cartItemId}`, { id: cartItemId, quantity });
    console.log(`Stavka korpe ažurirana:`, response.data);
    return response.data;
  } catch (error) {
    console.error('Greška pri ažuriranju količine stavke korpe:', error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za brisanje stavke korpe
export const deleteCartItem = async (cartItemId) => {
  try {
    if (!cartItemId) {
      throw new Error('cartItemId je undefined');
    }
    console.log(`Brišem stavku korpe ${cartItemId}`);
    const response = await api.delete(`/cart-items/${cartItemId}`);
    console.log(`Stavka korpe obrisana:`, response.status);
    return response.status;
  } catch (error) {
    console.error('Greška pri brisanju stavke korpe:', error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za dohvatanje svih porudžbina (za admina)
export const getAllOrders = async () => {
  try {
    console.log('Dohvatam sve porudžbine (admin)...');
    const response = await api.get('/orders/all');
    console.log('Sve porudžbine uspešno dohvaćene:', response.data);
    return response.data || [];
  } catch (error) {
    console.error('Greška pri dohvatanju svih porudžbina:', error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za dohvatanje porudžbina korisnika
export const getUserOrders = async () => {
  try {
    console.log('Dohvatam porudžbine korisnika...');
    const response = await api.get('/orders');
    console.log('Porudžbine korisnika uspešno dohvaćene:', response.data);
    return response.data || [];
  } catch (error) {
    console.error('Greška pri dohvatanju porudžbina korisnika:', error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za ažuriranje statusa porudžbine (za admina)
export const updateOrderStatus = async (orderId, orderUpdateDTO) => {
  try {
    console.log(`Ažuriram status porudžbine ${orderId}:`, orderUpdateDTO);
    const response = await api.put(`/orders/${orderId}`, orderUpdateDTO);
    console.log(`Status porudžbine ${orderId} uspešno ažuriran:`, response.data);
    return response.data;
  } catch (error) {
    console.error('Greška pri ažuriranju statusa porudžbine:', error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za brisanje porudžbine (za admina)
export const deleteOrder = async (orderId) => {
  try {
    console.log(`Brišem porudžbinu ${orderId}`);
    const response = await api.delete(`/orders/${orderId}`);
    console.log(`Porudžbina ${orderId} uspešno obrisana`);

    // Proveri da li odgovor sadrži novi token
    if (response.data && response.data.jwtToken) {
      localStorage.setItem('token', response.data.jwtToken);
      window.dispatchEvent(new Event('storage')); // Obavesti ostale komponente o promeni
      console.log('Token ažuriran nakon brisanja porudžbine:', response.data.jwtToken);
    }

    return response.status;
  } catch (error) {
    console.error('Greška pri brisanju porudžbine:', error.response?.data || error.message);
    throw error;
  }
};

// Funkcija za osvežavanje tokena
export const refreshToken = async () => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('Nema tokena za osvežavanje');
    }
    console.log('Osvežavam token:', token);
    const response = await api.post('/refresh-token', {}, {
      headers: { Authorization: `Bearer ${token}` }
    });
    if (response.data && response.data.jwtToken) {
      localStorage.setItem('token', response.data.jwtToken);
      window.dispatchEvent(new Event('storage'));
      console.log('Token uspešno osvežen sa customerType:', response.data.customerType);
      return response.data;
    } else {
      throw new Error('Nevažeći odgovor servera prilikom osvežavanja tokena');
    }
  } catch (error) {
    console.error('Greška prilikom osvežavanja tokena:', error.response?.data || error.message);
    localStorage.removeItem('token');
    setTimeout(() => {
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }, 2000);
    throw error;
  }
};

// Funkcija za checkout
export const checkout = async (userId, address, navigate) => {
  try {
    const response = await api.post(`/orders/user/${userId}/checkout`, address);
    const { order, jwtToken } = response.data;

    if (jwtToken) {
      localStorage.setItem('token', jwtToken);
      window.dispatchEvent(new Event('storage'));
      console.log('Ažuriran customerType nakon checkout-a:', response.data.customerType);
    } else {
      console.warn('Token nije vraćen u odgovoru za checkout');
    }

    alert(`Porudžbina #${order.id} je uspešno kreirana!`);
    navigate('/');
    return order;
  } catch (error) {
    console.error('Detalji greške pri checkout-u:', error.response?.data || error.message);
    throw error;
  }
};

export { jwtDecode };
export default api;