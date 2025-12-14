// src/services/productService.js
import productApi from "../api/productAxios";

// Create a new product
export const createProduct = async (productData) => {
  try {
    const response = await productApi.post("/products", productData);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Get product by ID
export const getProductById = async (id) => {
  try {
    const response = await productApi.get(`/products/${id}`);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Get all products (with pagination)
export const getAllProducts = async (page = 0, size = 10, sortBy = "productId", sortDirection = "ASC") => {
  try {
    const response = await productApi.get("/products", {
      params: { page, size, sortBy, sortDirection }
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Get all products as list (no pagination)
export const getAllProductsList = async () => {
  try {
    const response = await productApi.get("/products/all");
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Update product
export const updateProduct = async (id, productData) => {
  try {
    const response = await productApi.put(`/products/${id}`, productData);
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Delete product
export const deleteProduct = async (id) => {
  try {
    await productApi.delete(`/products/${id}`);
    return true;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Get products by category
export const getProductsByCategory = async (category, page = 0, size = 10) => {
  try {
    const response = await productApi.get(`/products/category/${category}`, {
      params: { page, size }
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Search products by name
export const searchProductsByName = async (name) => {
  try {
    const response = await productApi.get("/products/search", {
      params: { name }
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Get products by price range
export const getProductsByPriceRange = async (minPrice, maxPrice) => {
  try {
    const response = await productApi.get("/products/price-range", {
      params: { minPrice, maxPrice }
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Get in-stock products
export const getInStockProducts = async (page = 0, size = 10) => {
  try {
    const response = await productApi.get("/products/in-stock", {
      params: { page, size }
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Advanced filter/search
export const searchProducts = async (filters = {}) => {
  try {
    const response = await productApi.get("/products/filter", {
      params: {
        name: filters.name || undefined,
        category: filters.category || undefined,
        minPrice: filters.minPrice || undefined,
        maxPrice: filters.maxPrice || undefined,
        inStock: filters.inStock || undefined,
        page: filters.page || 0,
        size: filters.size || 10,
        sortBy: filters.sortBy || "productId"
      }
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Update stock quantity
export const updateStock = async (id, quantity) => {
  try {
    const response = await productApi.patch(`/products/${id}/stock`, null, {
      params: { quantity }
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Reduce stock quantity
export const reduceStock = async (id, quantity) => {
  try {
    const response = await productApi.patch(`/products/${id}/reduce-stock`, null, {
      params: { quantity }
    });
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};