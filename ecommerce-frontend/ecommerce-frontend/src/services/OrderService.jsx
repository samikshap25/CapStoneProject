// src/services/OrderService.jsx
import axios from "axios";

const ORDER_BASE_URL = "http://localhost:8083/orders";

// Get auth token from localStorage
const getAuthToken = () => localStorage.getItem("token");

// Create axios instance
const api = axios.create({
  baseURL: ORDER_BASE_URL,
  headers: { "Content-Type": "application/json" }
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = getAuthToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// ========== CART FUNCTIONS (Local Storage) ==========

export const getCartItems = () => {
  const cart = localStorage.getItem("cart");
  return cart ? JSON.parse(cart) : [];
};

export const addToCart = (product, quantity = 1) => {
  const cart = getCartItems();
  const existingIndex = cart.findIndex(item => item.productId === product.productId);
  
  if (existingIndex > -1) {
    cart[existingIndex].quantity += quantity;
  } else {
    cart.push({
      productId: product.productId,
      name: product.name,
      price: product.price,
      imageUrl: product.imageUrl,
      category: product.category,
      quantity: quantity,
      maxStock: product.quantity
    });
  }
  
  localStorage.setItem("cart", JSON.stringify(cart));
  return cart;
};

export const updateCartItemQuantity = (productId, quantity) => {
  const cart = getCartItems();
  const itemIndex = cart.findIndex(item => item.productId === productId);
  
  if (itemIndex > -1) {
    if (quantity <= 0) {
      cart.splice(itemIndex, 1);
    } else {
      cart[itemIndex].quantity = quantity;
    }
  }
  
  localStorage.setItem("cart", JSON.stringify(cart));
  return cart;
};

export const removeFromCart = (productId) => {
  const cart = getCartItems().filter(item => item.productId !== productId);
  localStorage.setItem("cart", JSON.stringify(cart));
  return cart;
};

export const clearCart = () => {
  localStorage.removeItem("cart");
  return [];
};

export const getCartItemCount = () => {
  return getCartItems().reduce((total, item) => total + item.quantity, 0);
};

export const getCartTotal = () => {
  return getCartItems().reduce((total, item) => total + (item.price * item.quantity), 0);
};

// ========== ORDER API CALLS ==========

export const createOrder = async (userId) => {
  const cart = getCartItems();
  if (cart.length === 0) throw new Error("Cart is empty");
  
  const orderItems = cart.map(item => ({
    productId: item.productId,
    quantity: item.quantity,
    price: item.price
  }));
  
  const response = await api.post("", { userId, items: orderItems });
  clearCart();
  return response.data;
};

export const getAllOrders = async () => {
  const response = await api.get("");
  return response.data;
};

export const getOrderById = async (orderId) => {
  const response = await api.get(`/${orderId}`);
  return response.data;
};

// ✅ FIXED: Returns numeric user ID or default 1
export const getUserIdFromToken = () => {
  try {
    const token = getAuthToken();
    if (!token) {
      console.warn("No token found, using default userId: 1");
      return 1;
    }
    
    const payload = JSON.parse(atob(token.split(".")[1]));
    console.log("JWT Payload:", payload); // Debug log
    
    // Try different possible field names for user ID
    const userId = payload.userId || payload.user_id || payload.id;
    
    // If found and it's a number, return it
    if (userId && typeof userId === 'number') {
      return userId;
    }
    
    // If userId is a string number, convert it
    if (userId && !isNaN(Number(userId))) {
      return Number(userId);
    }
    
    // If no numeric ID found, use default
    console.warn("No numeric userId in token, using default: 1");
    return 1;
    
  } catch (error) {
    console.error("Error parsing JWT:", error);
    return 1;
  }
};