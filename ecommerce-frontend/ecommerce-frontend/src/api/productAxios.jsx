// src/api/productAxios.js
import axios from "axios";

const productApi = axios.create({
  baseURL: "http://localhost:8082/api", // Product Service
});

productApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default productApi;