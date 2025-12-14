// src/pages/Products.jsx
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { FaShoppingCart, FaFilter, FaTimes } from "react-icons/fa";
import {
  searchProducts,
  getProductsByCategory,
  getInStockProducts,
} from "../services/ProductService";

const Products = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [filters, setFilters] = useState({
    name: "",
    category: "",
    minPrice: "",
    maxPrice: "",
    inStock: null,
    page: 0,
    size: 12,
    sortBy: "productId",
  });

  const categories = ["Electronics", "Clothing", "Books", "Home", "Sports"];

  useEffect(() => {
    fetchProducts();
  }, [filters.page]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response = await searchProducts(filters);
      setProducts(response.content);
      setTotalPages(response.totalPages);
      setCurrentPage(response.number);
    } catch (err) {
      console.error("Failed to fetch products", err);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters({
      ...filters,
      [name]: value,
      page: 0, // Reset to first page
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
      size: 12,
      sortBy: "productId",
    });
    setShowFilters(false);
  };

  const handlePageChange = (newPage) => {
    setFilters({ ...filters, page: newPage });
  };

  const handleAddToCart = (product) => {
    console.log("Add to cart:", product);
    alert(`${product.name} added to cart!`);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-md sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <h1
            className="text-3xl font-bold text-indigo-600 cursor-pointer"
            onClick={() => navigate("/home")}
          >
            ShopMate
          </h1>

          <button
            onClick={() => setShowFilters(!showFilters)}
            className="bg-indigo-600 text-white px-4 py-2 rounded flex items-center gap-2"
          >
            <FaFilter /> Filters
          </button>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-6 py-8">
        {/* Filter Panel */}
        {showFilters && (
          <div className="bg-white p-6 rounded-lg shadow-lg mb-6">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-xl font-bold">Filters</h3>
              <button onClick={() => setShowFilters(false)}>
                <FaTimes />
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <input
                type="text"
                name="name"
                placeholder="Search by name..."
                className="border rounded px-4 py-2"
                value={filters.name}
                onChange={handleFilterChange}
              />

              <select
                name="category"
                className="border rounded px-4 py-2"
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

              <select
                name="inStock"
                className="border rounded px-4 py-2"
                value={filters.inStock === null ? "" : filters.inStock}
                onChange={(e) =>
                  setFilters({
                    ...filters,
                    inStock: e.target.value === "" ? null : e.target.value === "true",
                  })
                }
              >
                <option value="">All Products</option>
                <option value="true">In Stock</option>
                <option value="false">Out of Stock</option>
              </select>

              <input
                type="number"
                name="minPrice"
                placeholder="Min Price"
                className="border rounded px-4 py-2"
                value={filters.minPrice}
                onChange={handleFilterChange}
              />

              <input
                type="number"
                name="maxPrice"
                placeholder="Max Price"
                className="border rounded px-4 py-2"
                value={filters.maxPrice}
                onChange={handleFilterChange}
              />

              <select
                name="sortBy"
                className="border rounded px-4 py-2"
                value={filters.sortBy}
                onChange={handleFilterChange}
              >
                <option value="productId">Default</option>
                <option value="name">Name</option>
                <option value="price">Price</option>
                <option value="quantity">Stock</option>
              </select>
            </div>

            <div className="flex gap-4 mt-4">
              <button
                onClick={handleApplyFilters}
                className="bg-indigo-600 text-white px-6 py-2 rounded"
              >
                Apply Filters
              </button>
              <button
                onClick={handleClearFilters}
                className="bg-gray-300 text-gray-700 px-6 py-2 rounded"
              >
                Clear All
              </button>
            </div>
          </div>
        )}

        {/* Loading */}
        {loading && (
          <p className="text-center text-gray-500">Loading products...</p>
        )}

        {/* Products Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {products.map((product) => (
            <div
              key={product.productId}
              className="bg-white rounded-lg shadow hover:shadow-xl transition"
            >
              <img
                src={product.imageUrl || "https://via.placeholder.com/300"}
                alt={product.name}
                className="w-full h-48 object-cover rounded-t-lg"
                onError={(e) => {
                  e.target.src = "https://via.placeholder.com/300";
                }}
              />

              <div className="p-4">
                <h3 className="font-semibold text-lg">{product.name}</h3>
                <p className="text-sm text-gray-500 line-clamp-2 mt-1">
                  {product.description || "No description"}
                </p>
                <p className="text-indigo-600 font-bold text-lg mt-2">
                  ₹{product.price}
                </p>
                <p className="text-sm text-gray-500">
                  {product.inStock ? `Stock: ${product.quantity}` : "Out of Stock"}
                </p>

                <button
                  onClick={() => handleAddToCart(product)}
                  disabled={!product.inStock}
                  className={`w-full mt-3 py-2 rounded flex items-center justify-center gap-2 ${
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

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex justify-center gap-2 mt-8">
            <button
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 0}
              className="px-4 py-2 bg-indigo-600 text-white rounded disabled:bg-gray-300"
            >
              Previous
            </button>

            <span className="px-4 py-2">
              Page {currentPage + 1} of {totalPages}
            </span>

            <button
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages - 1}
              className="px-4 py-2 bg-indigo-600 text-white rounded disabled:bg-gray-300"
            >
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default Products;