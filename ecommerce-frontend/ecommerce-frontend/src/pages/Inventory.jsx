// src/pages/Inventory.jsx
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { FaPlus, FaEdit, FaTrash, FaArrowLeft, FaSearch } from "react-icons/fa";
import {
  getAllProductsList,
  createProduct,
  updateProduct,
  deleteProduct,
  searchProductsByName,
} from "../services/ProductService";

const Inventory = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  
  const [formData, setFormData] = useState({
    productId: null,
    name: "",
    description: "",
    price: "",
    quantity: "",
    category: "",
    imageUrl: "",
  });

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const data = await getAllProductsList();
      setProducts(data);
      setError("");
    } catch (err) {
      setError("Failed to fetch products");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) {
      fetchProducts();
      return;
    }

    try {
      setLoading(true);
      const data = await searchProductsByName(searchQuery);
      setProducts(data);
    } catch (err) {
      setError("Search failed");
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const openAddModal = () => {
    setEditMode(false);
    setFormData({
      productId: null,
      name: "",
      description: "",
      price: "",
      quantity: "",
      category: "",
      imageUrl: "",
    });
    setShowModal(true);
  };

  const openEditModal = (product) => {
    setEditMode(true);
    setFormData({
      productId: product.productId,
      name: product.name,
      description: product.description || "",
      price: product.price,
      quantity: product.quantity,
      category: product.category || "",
      imageUrl: product.imageUrl || "",
    });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const productData = {
      name: formData.name,
      description: formData.description,
      price: parseFloat(formData.price),
      quantity: parseInt(formData.quantity),
      category: formData.category,
      imageUrl: formData.imageUrl || undefined,
    };

    try {
      setLoading(true);
      if (editMode) {
        await updateProduct(formData.productId, productData);
      } else {
        await createProduct(productData);
      }
      
      setShowModal(false);
      fetchProducts();
      alert(`Product ${editMode ? "updated" : "created"} successfully!`);
    } catch (err) {
      alert(err.message || "Operation failed");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this product?")) {
      return;
    }

    try {
      setLoading(true);
      await deleteProduct(id);
      fetchProducts();
      alert("Product deleted successfully!");
    } catch (err) {
      alert("Failed to delete product");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      {/* Header */}
      <div className="max-w-7xl mx-auto mb-8">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate("/home")}
              className="text-indigo-600 hover:text-indigo-800"
            >
              <FaArrowLeft size={24} />
            </button>
            <h1 className="text-4xl font-bold">Product Inventory</h1>
          </div>
          
          <button
            onClick={openAddModal}
            className="bg-indigo-600 text-white px-6 py-3 rounded-lg flex items-center gap-2 hover:bg-indigo-700"
          >
            <FaPlus /> Add Product
          </button>
        </div>

        {/* Search Bar */}
        <form onSubmit={handleSearch} className="mt-6">
          <div className="relative max-w-md">
            <input
              type="text"
              placeholder="Search products..."
              className="w-full border rounded-lg px-4 py-2 pr-10"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <button type="submit" className="absolute right-3 top-2.5">
              <FaSearch className="text-gray-500" />
            </button>
          </div>
        </form>
      </div>

      {/* Error/Loading */}
      {error && (
        <div className="max-w-7xl mx-auto mb-4">
          <p className="text-red-500 bg-red-100 p-4 rounded">{error}</p>
        </div>
      )}

      {loading && (
        <div className="text-center">
          <p className="text-gray-500">Loading...</p>
        </div>
      )}

      {/* Products Table */}
      <div className="max-w-7xl mx-auto bg-white rounded-lg shadow overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-100">
            <tr>
              <th className="px-6 py-3 text-left">ID</th>
              <th className="px-6 py-3 text-left">Image</th>
              <th className="px-6 py-3 text-left">Name</th>
              <th className="px-6 py-3 text-left">Category</th>
              <th className="px-6 py-3 text-left">Price</th>
              <th className="px-6 py-3 text-left">Stock</th>
              <th className="px-6 py-3 text-left">Status</th>
              <th className="px-6 py-3 text-center">Actions</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.productId} className="border-t hover:bg-gray-50">
                <td className="px-6 py-4">{product.productId}</td>
                <td className="px-6 py-4">
                  <img
                    src={product.imageUrl || "https://via.placeholder.com/50"}
                    alt={product.name}
                    className="w-12 h-12 object-cover rounded"
                    onError={(e) => {
                      e.target.src = "https://via.placeholder.com/50";
                    }}
                  />
                </td>
                <td className="px-6 py-4 font-semibold">{product.name}</td>
                <td className="px-6 py-4">{product.category || "N/A"}</td>
                <td className="px-6 py-4">₹{product.price}</td>
                <td className="px-6 py-4">{product.quantity}</td>
                <td className="px-6 py-4">
                  <span
                    className={`px-3 py-1 rounded-full text-sm ${
                      product.inStock
                        ? "bg-green-100 text-green-700"
                        : "bg-red-100 text-red-700"
                    }`}
                  >
                    {product.inStock ? "In Stock" : "Out of Stock"}
                  </span>
                </td>
                <td className="px-6 py-4 text-center">
                  <button
                    onClick={() => openEditModal(product)}
                    className="text-blue-600 hover:text-blue-800 mr-4"
                  >
                    <FaEdit size={18} />
                  </button>
                  <button
                    onClick={() => handleDelete(product.productId)}
                    className="text-red-600 hover:text-red-800"
                  >
                    <FaTrash size={18} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {products.length === 0 && !loading && (
          <p className="text-center py-8 text-gray-500">No products found</p>
        )}
      </div>

      {/* Add/Edit Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 max-w-md w-full mx-4">
            <h2 className="text-2xl font-bold mb-6">
              {editMode ? "Edit Product" : "Add New Product"}
            </h2>

            <form onSubmit={handleSubmit} className="space-y-4">
              <input
                type="text"
                name="name"
                placeholder="Product Name *"
                className="w-full border rounded px-4 py-2"
                value={formData.name}
                onChange={handleInputChange}
                required
              />

              <textarea
                name="description"
                placeholder="Description"
                className="w-full border rounded px-4 py-2"
                value={formData.description}
                onChange={handleInputChange}
                rows="3"
              />

              <input
                type="number"
                name="price"
                placeholder="Price *"
                step="0.01"
                min="0.01"
                className="w-full border rounded px-4 py-2"
                value={formData.price}
                onChange={handleInputChange}
                required
              />

              <input
                type="number"
                name="quantity"
                placeholder="Quantity *"
                min="0"
                className="w-full border rounded px-4 py-2"
                value={formData.quantity}
                onChange={handleInputChange}
                required
              />

              <input
                type="text"
                name="category"
                placeholder="Category"
                className="w-full border rounded px-4 py-2"
                value={formData.category}
                onChange={handleInputChange}
              />

              <input
                type="url"
                name="imageUrl"
                placeholder="Image URL"
                className="w-full border rounded px-4 py-2"
                value={formData.imageUrl}
                onChange={handleInputChange}
              />

              <div className="flex gap-4 mt-6">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="flex-1 bg-gray-300 text-gray-700 py-2 rounded hover:bg-gray-400"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="flex-1 bg-indigo-600 text-white py-2 rounded hover:bg-indigo-700"
                  disabled={loading}
                >
                  {loading ? "Saving..." : editMode ? "Update" : "Create"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Inventory;