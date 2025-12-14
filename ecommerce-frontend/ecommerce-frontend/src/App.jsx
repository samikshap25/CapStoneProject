// src/App.jsx
import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";

import Register from "./pages/Register.jsx";
import Login from "./pages/Login.jsx";
import Signup from "./pages/Signup.jsx";
import Home from "./pages/Home.jsx";
import Cart from "./pages/Cart.jsx";
import MyOrders from "./pages/MyOrders.jsx";
import Inventory from "./pages/Inventory.jsx";
import Products from "./pages/Products.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import { isAuthenticated } from "./utils/auth";

const App = () => {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/" element={<Home />} />
      <Route path="/home" element={<Home />} />
      <Route path="/products" element={<Products />} />
      
      {/* Auth Routes */}
      <Route 
        path="/login" 
        element={isAuthenticated() ? <Navigate to="/home" /> : <Login />} 
      />
      <Route 
        path="/signup" 
        element={isAuthenticated() ? <Navigate to="/home" /> : <Signup />} 
      />
      <Route 
        path="/register" 
        element={isAuthenticated() ? <Navigate to="/home" /> : <Register />} 
      />

      {/* Protected Routes - Cart & Orders */}
      <Route
        path="/cart"
        element={
          <ProtectedRoute>
            <Cart />
          </ProtectedRoute>
        }
      />
      <Route
        path="/my-orders"
        element={
          <ProtectedRoute>
            <MyOrders />
          </ProtectedRoute>
        }
      />

      {/* Admin Only Route */}
      <Route
        path="/inventory"
        element={
          <ProtectedRoute adminOnly={true}>
            <Inventory />
          </ProtectedRoute>
        }
      />

      {/* 404 Not Found */}
      <Route 
        path="*" 
        element={
          <div className="min-h-screen flex items-center justify-center bg-gray-100">
            <div className="text-center">
              <h1 className="text-6xl font-bold text-gray-800 mb-4">404</h1>
              <p className="text-xl text-gray-600 mb-6">Page Not Found</p>
              <button
                onClick={() => window.location.href = "/home"}
                className="bg-indigo-600 text-white px-6 py-3 rounded hover:bg-indigo-700"
              >
                Go Home
              </button>
            </div>
          </div>
        } 
      />
    </Routes>
  );
};

export default App;