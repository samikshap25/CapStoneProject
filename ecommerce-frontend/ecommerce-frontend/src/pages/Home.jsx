import React from "react";
import { useNavigate } from "react-router-dom";
import {
  FaUser,
  FaShoppingCart,
  FaBars,
  FaFacebook,
  FaInstagram,
  FaTwitter,
  FaYoutube,
} from "react-icons/fa";

import logo from "../assets/logo.jpg";
import { isAuthenticated } from "../utils/auth";

const Home = () => {
  // ✅ HOOKS MUST BE INSIDE COMPONENT
  const navigate = useNavigate();

  const products = [
    {
      id: 1,
      name: "Wireless Headphones",
      price: 79.99,
      image:
        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500",
    },
    {
      id: 2,
      name: "Smart Watch",
      price: 199.99,
      image:
        "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500",
    },
    {
      id: 3,
      name: "Laptop Backpack",
      price: 49.99,
      image:
        "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500",
    },
  ];

  const handleAddToCart = (product) => {
    if (!isAuthenticated()) {
      navigate("/login", { state: { from: "/home" } });
      return;
    }
    console.log("Added to cart:", product);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* NAVBAR */}
      <nav className="bg-white shadow-md sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center">
          <h1
            className="text-3xl font-extrabold text-indigo-600 cursor-pointer"
            onClick={() => navigate("/home")}
          >
            ShopMate
          </h1>

          <div className="ml-auto flex items-center gap-6">
            <FaShoppingCart
              size={26}
              className="cursor-pointer"
              onClick={() => navigate("/cart")}
            />
            <FaUser
              size={26}
              className="cursor-pointer"
              onClick={() => navigate("/login")}
            />
            <FaBars size={26} className="md:hidden cursor-pointer" />
          </div>
        </div>
      </nav>

      {/* HERO */}
      <section className="bg-gradient-to-br from-indigo-700 via-purple-700 to-pink-600 text-white py-24 text-center">
        <h1 className="text-5xl font-extrabold mb-6">
          Upgrade Your Shopping Experience
        </h1>
        <p className="text-xl mb-10">
          Premium products • Fast delivery • Secure payments
        </p>
        <button
          onClick={() => navigate("/login")}
          className="bg-white text-indigo-700 px-10 py-3 rounded-full font-semibold"
        >
          Get Started
        </button>
      </section>

      {/* PRODUCTS */}
      <section className="py-20">
        <h2 className="text-4xl font-bold text-center mb-12">
          Featured Products
        </h2>

        <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-10 px-6">
          {products.map((product) => (
            <div
              key={product.id}
              className="bg-white rounded-xl shadow hover:shadow-xl transition"
            >
              <img
                src={product.image}
                alt={product.name}
                className="w-full h-64 object-cover rounded-t-xl"
              />

              <div className="p-6">
                <h3 className="text-xl font-semibold">{product.name}</h3>
                <p className="text-gray-600 mt-2">₹{product.price}</p>

                <button
                  onClick={() => handleAddToCart(product)}
                  className="mt-4 w-full bg-indigo-600 text-white py-2 rounded"
                >
                  <FaShoppingCart className="inline mr-2" />
                  Add to Cart
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* FOOTER */}
      <footer className="bg-gray-900 text-gray-300 py-14">
        <div className="max-w-7xl mx-auto px-6 grid grid-cols-1 md:grid-cols-4 gap-10">
          <div>
            <img src={logo} alt="Logo" className="h-12 mb-4" />
            <p>
              Your trusted store for premium products, fast delivery, and secure
              shopping.
            </p>
          </div>

          <div>
            <h4 className="font-semibold mb-3">Shop</h4>
            <ul className="space-y-2">
              <li>New Arrivals</li>
              <li>Best Sellers</li>
              <li>Offers</li>
            </ul>
          </div>

          <div>
            <h4 className="font-semibold mb-3">Support</h4>
            <ul className="space-y-2">
              <li>FAQ</li>
              <li>Customer Service</li>
            </ul>
          </div>

          <div>
            <h4 className="font-semibold mb-3">Follow Us</h4>
            <div className="flex gap-4 text-xl">
              <FaFacebook />
              <FaInstagram />
              <FaTwitter />
              <FaYoutube />
            </div>
          </div>
        </div>

        <p className="text-center text-gray-500 mt-10">
          © 2025 ShopMate. All rights reserved.
        </p>
      </footer>
    </div>
  );
};

export default Home;
