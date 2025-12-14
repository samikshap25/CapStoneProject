// src/pages/Home.jsx
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  FaUser,
  FaShoppingCart,
  FaBars,
  FaFacebook,
  FaInstagram,
  FaTwitter,
  FaYoutube,
  FaSearch,
  FaTimes,
  FaClipboardList,
} from "react-icons/fa";

import logo from "../assets/logo.jpg";
import { isAuthenticated, getUserRole } from "../utils/auth";
import { getAllProductsList } from "../services/ProductService";
import { addToCart, getCartItemCount } from "../services/OrderService";

const Home = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showFilters, setShowFilters] = useState(false);
  const [userRole, setUserRole] = useState(null);
  const [cartCount, setCartCount] = useState(0);

  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Filter state
  const [filters, setFilters] = useState({
    name: "",
    category: "",
    minPrice: "",
    maxPrice: "",
    inStock: null,
    page: 0,
    size: 9,
    sortBy: "productId",
  });

  const categories = ["Electronics", "Clothing", "Books", "Home", "Sports"];

  useEffect(() => {
    if (isAuthenticated()) {
      const role = getUserRole();
      setUserRole(role);
    }
    fetchProducts();
    updateCartCount();
  }, [filters.page]);

  const updateCartCount = () => {
    setCartCount(getCartItemCount());
  };

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response = await getAllProductsList();
      
      const startIndex = filters.page * filters.size;
      const endIndex = startIndex + filters.size;
      const paginatedProducts = response.slice(startIndex, endIndex);
      
      setProducts(paginatedProducts);
      setTotalPages(Math.ceil(response.length / filters.size));
      setCurrentPage(filters.page);
      setError("");
    } catch (err) {
      setError("Failed to load products");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters({
      ...filters,
      [name]: value,
      page: 0,
    });
  };

  const handleApplyFilters = () => {
    fetchProducts();
    setShowFilters(false);
  };

  const handleClearFilters = () => {
    setFilters({
      name: "",
      category: "",
      minPrice: "",
      maxPrice: "",
      inStock: null,
      page: 0,
      size: 9,
      sortBy: "productId",
    });
    setShowFilters(false);
  };

  const handlePageChange = (newPage) => {
    setFilters({ ...filters, page: newPage });
    window.scrollTo({ top: 600, behavior: "smooth" });
  };

  const handleAddToCart = (product) => {
    if (!isAuthenticated()) {
      navigate("/login", { state: { from: "/home" } });
      return;
    }
    
    addToCart(product, 1);
    updateCartCount();
    
    // Show success notification
    const notification = document.createElement("div");
    notification.className = "fixed top-20 right-6 bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg z-50";
    notification.textContent = `✓ ${product.name} added to cart!`;
    document.body.appendChild(notification);
    
    setTimeout(() => {
      notification.remove();
    }, 3000);
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("cart");
    setUserRole(null);
    setCartCount(0);
    navigate("/login");
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
            <button
              onClick={() => setShowFilters(!showFilters)}
              className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
            >
              <FaSearch /> Search
            </button>

            {/* My Orders Icon */}
            {isAuthenticated() && (
              <div className="relative group">
                <FaClipboardList
                  size={26}
                  className="cursor-pointer hover:text-indigo-600"
                  onClick={() => navigate("/my-orders")}
                />
                <span className="absolute -bottom-8 left-1/2 transform -translate-x-1/2 bg-gray-800 text-white text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition whitespace-nowrap">
                  My Orders
                </span>
              </div>
            )}

            {/* Cart Icon with Badge */}
            <div 
              className="relative cursor-pointer" 
              onClick={() => navigate("/cart")}
            >
              <FaShoppingCart 
                size={26} 
                className="hover:text-indigo-600"
              />
              {cartCount > 0 && (
                <span className="absolute -top-2 -right-2 bg-red-600 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
                  {cartCount > 99 ? "99+" : cartCount}
                </span>
              )}
            </div>

            {isAuthenticated() ? (
              <>
                <div className="relative group">
                  <FaUser
                    size={26}
                    className="cursor-pointer hover:text-indigo-600"
                  />
                  <div className="absolute right-0 mt-2 w-48 bg-white shadow-lg rounded hidden group-hover:block">
                    <button
                      onClick={() => navigate("/profile")}
                      className="block w-full text-left px-4 py-2 hover:bg-gray-100"
                    >
                      Profile
                    </button>
                    {userRole === "ADMIN" && (
                      <button
                        onClick={() => navigate("/inventory")}
                        className="block w-full text-left px-4 py-2 hover:bg-gray-100"
                      >
                        Inventory Management
                      </button>
                    )}
                    <button
                      onClick={handleLogout}
                      className="block w-full text-left px-4 py-2 hover:bg-gray-100 text-red-600"
                    >
                      Logout
                    </button>
                  </div>
                </div>
              </>
            ) : (
              <button
                onClick={() => navigate("/login")}
                className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
              >
                Login
              </button>
            )}

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
          onClick={() => setShowFilters(true)}
          className="bg-white text-indigo-700 px-10 py-3 rounded-full font-semibold hover:shadow-lg transition flex items-center gap-2 mx-auto"
        >
          <FaSearch /> Search Products
        </button>
      </section>

      {/* SEARCH & FILTER PANEL */}
      {showFilters && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b px-6 py-4 flex justify-between items-center">
              <h3 className="text-2xl font-bold text-indigo-600">
                Search & Filter Products
              </h3>
              <button
                onClick={() => setShowFilters(false)}
                className="text-gray-500 hover:text-gray-700"
              >
                <FaTimes size={24} />
              </button>
            </div>

            <div className="p-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                <div>
                  <label className="block text-sm font-medium mb-2">
                    Product Name
                  </label>
                  <input
                    type="text"
                    name="name"
                    placeholder="Search by name..."
                    className="w-full border rounded px-4 py-2"
                    value={filters.name}
                    onChange={handleFilterChange}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium mb-2">
                    Category
                  </label>
                  <select
                    name="category"
                    className="w-full border rounded px-4 py-2"
                    value={filters.category}
                    onChange={handleFilterChange}
                  >
                    <option value="">All Categories</option>
                    {categories.map((cat) => (
                      <option key={cat} value={cat}>
                        {cat}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium mb-2">
                    Min Price (₹)
                  </label>
                  <input
                    type="number"
                    name="minPrice"
                    placeholder="Min Price"
                    className="w-full border rounded px-4 py-2"
                    value={filters.minPrice}
                    onChange={handleFilterChange}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium mb-2">
                    Max Price (₹)
                  </label>
                  <input
                    type="number"
                    name="maxPrice"
                    placeholder="Max Price"
                    className="w-full border rounded px-4 py-2"
                    value={filters.maxPrice}
                    onChange={handleFilterChange}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium mb-2">
                    Availability
                  </label>
                  <select
                    name="inStock"
                    className="w-full border rounded px-4 py-2"
                    value={filters.inStock === null ? "" : filters.inStock}
                    onChange={(e) =>
                      setFilters({
                        ...filters,
                        inStock:
                          e.target.value === ""
                            ? null
                            : e.target.value === "true",
                        page: 0,
                      })
                    }
                  >
                    <option value="">All Products</option>
                    <option value="true">In Stock Only</option>
                    <option value="false">Out of Stock</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium mb-2">
                    Sort By
                  </label>
                  <select
                    name="sortBy"
                    className="w-full border rounded px-4 py-2"
                    value={filters.sortBy}
                    onChange={handleFilterChange}
                  >
                    <option value="productId">Default</option>
                    <option value="name">Name (A-Z)</option>
                    <option value="price">Price (Low to High)</option>
                    <option value="quantity">Stock Quantity</option>
                  </select>
                </div>
              </div>

              <div className="flex gap-4">
                <button
                  onClick={handleApplyFilters}
                  className="flex-1 bg-indigo-600 text-white px-6 py-3 rounded hover:bg-indigo-700 font-semibold"
                >
                  Apply Filters
                </button>
                <button
                  onClick={handleClearFilters}
                  className="flex-1 bg-gray-300 text-gray-700 px-6 py-3 rounded hover:bg-gray-400 font-semibold"
                >
                  Clear All
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* PRODUCTS SECTION */}
      <section className="py-20">
        <h2 className="text-4xl font-bold text-center mb-12">
          Featured Products
        </h2>

        {loading && (
          <p className="text-center text-gray-500 text-xl">Loading products...</p>
        )}

        {error && <p className="text-center text-red-500 text-xl">{error}</p>}

        {!loading && products.length === 0 && (
          <div className="text-center">
            <p className="text-gray-500 text-xl mb-4">No products found</p>
            <button
              onClick={handleClearFilters}
              className="text-indigo-600 underline"
            >
              Clear filters and try again
            </button>
          </div>
        )}

        <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-10 px-6">
          {products.map((product) => (
            <div
              key={product.productId}
              className="bg-white rounded-xl shadow hover:shadow-xl transition"
            >
              <img
                src={product.imageUrl || "https://via.placeholder.com/400"}
                alt={product.name}
                className="w-full h-64 object-cover rounded-t-xl"
                onError={(e) => {
                  e.target.src = "https://via.placeholder.com/400";
                }}
              />

              <div className="p-6">
                <div className="flex items-start justify-between mb-2">
                  <h3 className="text-xl font-semibold flex-1">{product.name}</h3>
                  <span className="bg-indigo-100 text-indigo-600 text-xs px-2 py-1 rounded">
                    {product.category}
                  </span>
                </div>
                
                <p className="text-gray-600 text-sm mt-2 line-clamp-2">
                  {product.description || "No description available"}
                </p>
                
                <p className="text-2xl font-bold text-indigo-600 mt-3">
                  ₹{product.price.toLocaleString()}
                </p>
                
                <p className="text-sm text-gray-500 mt-1">
                  {product.inStock ? (
                    <span className="text-green-600">
                      ✓ In Stock ({product.quantity} available)
                    </span>
                  ) : (
                    <span className="text-red-600">✗ Out of Stock</span>
                  )}
                </p>

                <button
                  onClick={() => handleAddToCart(product)}
                  disabled={!product.inStock}
                  className={`mt-4 w-full py-3 rounded flex items-center justify-center gap-2 font-semibold ${
                    product.inStock
                      ? "bg-indigo-600 text-white hover:bg-indigo-700"
                      : "bg-gray-300 text-gray-500 cursor-not-allowed"
                  }`}
                >
                  <FaShoppingCart />
                  {product.inStock ? "Add to Cart" : "Out of Stock"}
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* PAGINATION */}
        {totalPages > 1 && (
          <div className="flex justify-center items-center gap-4 mt-12">
            <button
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 0}
              className="px-6 py-2 bg-indigo-600 text-white rounded disabled:bg-gray-300 disabled:cursor-not-allowed hover:bg-indigo-700"
            >
              Previous
            </button>

            <div className="flex gap-2">
              {[...Array(totalPages)].map((_, index) => (
                <button
                  key={index}
                  onClick={() => handlePageChange(index)}
                  className={`px-4 py-2 rounded ${
                    currentPage === index
                      ? "bg-indigo-600 text-white"
                      : "bg-gray-200 hover:bg-gray-300"
                  }`}
                >
                  {index + 1}
                </button>
              ))}
            </div>

            <button
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages - 1}
              className="px-6 py-2 bg-indigo-600 text-white rounded disabled:bg-gray-300 disabled:cursor-not-allowed hover:bg-indigo-700"
            >
              Next
            </button>
          </div>
        )}
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
              <li className="cursor-pointer hover:text-white">New Arrivals</li>
              <li className="cursor-pointer hover:text-white">Best Sellers</li>
              <li className="cursor-pointer hover:text-white">Offers</li>
            </ul>
          </div>

          <div>
            <h4 className="font-semibold mb-3">Support</h4>
            <ul className="space-y-2">
              <li className="cursor-pointer hover:text-white">FAQ</li>
              <li className="cursor-pointer hover:text-white">Customer Service</li>
            </ul>
          </div>

          <div>
            <h4 className="font-semibold mb-3">Follow Us</h4>
            <div className="flex gap-4 text-xl">
              <FaFacebook className="cursor-pointer hover:text-white" />
              <FaInstagram className="cursor-pointer hover:text-white" />
              <FaTwitter className="cursor-pointer hover:text-white" />
              <FaYoutube className="cursor-pointer hover:text-white" />
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