// src/pages/Cart.jsx
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { FaTrash, FaPlus, FaMinus, FaArrowLeft } from "react-icons/fa";
import {
  getCartItems,
  updateCartItemQuantity,
  removeFromCart,
  clearCart,
  getCartTotal,
  createOrder,
  getUserIdFromToken
} from "../services/OrderService";

const Cart = () => {
  const navigate = useNavigate();
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadCart();
  }, []);

  const loadCart = () => setCartItems(getCartItems());

  const handleQuantityChange = (productId, newQuantity, maxStock) => {
    if (newQuantity < 1 || newQuantity > maxStock) return;
    updateCartItemQuantity(productId, newQuantity);
    loadCart();
  };

  const handleRemove = (productId) => {
    removeFromCart(productId);
    loadCart();
  };

  const handleCheckout = async () => {
    try {
      setLoading(true);
      
      // ✅ Get userId from token (returns numeric value or default 1)
      const userId = getUserIdFromToken();
      console.log("Creating order for userId:", userId);
      
      await createOrder(userId);
      alert("✓ Order placed successfully!");
      navigate("/my-orders");
    } catch (error) {
      console.error("Order creation error:", error);
      alert("Failed to place order: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  const subtotal = getCartTotal();
  const tax = subtotal * 0.18;
  const shipping = cartItems.length > 0 ? 50 : 0;
  const total = subtotal + tax + shipping;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-md sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <button onClick={() => navigate("/home")}
                  className="flex items-center gap-2 text-gray-600 hover:text-indigo-600">
            <FaArrowLeft /> Back
          </button>
          <h1 className="text-2xl font-bold text-indigo-600">Shopping Cart</h1>
          <div className="w-20"></div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-6 py-10">
        {cartItems.length === 0 ? (
          <div className="bg-white rounded-lg shadow-md p-16 text-center">
            <div className="text-6xl mb-4">🛒</div>
            <h2 className="text-2xl font-bold mb-4">Your Cart is Empty</h2>
            <button onClick={() => navigate("/home")}
                    className="bg-indigo-600 text-white px-8 py-3 rounded-lg hover:bg-indigo-700">
              Continue Shopping
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Cart Items */}
            <div className="lg:col-span-2 space-y-4">
              {cartItems.map((item) => (
                <div key={item.productId} className="bg-white rounded-lg shadow p-6 flex items-center gap-6">
                  <img src={item.imageUrl || "https://via.placeholder.com/150"}
                       alt={item.name}
                       className="w-24 h-24 object-cover rounded" />
                  
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold">{item.name}</h3>
                    <p className="text-sm text-gray-500">{item.category}</p>
                    <p className="text-xl font-bold text-indigo-600">₹{item.price.toLocaleString()}</p>
                  </div>

                  {/* Quantity Controls */}
                  <div className="flex items-center gap-2">
                    <button onClick={() => handleQuantityChange(item.productId, item.quantity - 1, item.maxStock)}
                            className="p-2 bg-gray-200 rounded hover:bg-gray-300">
                      <FaMinus size={12} />
                    </button>
                    <span className="w-12 text-center font-bold">{item.quantity}</span>
                    <button onClick={() => handleQuantityChange(item.productId, item.quantity + 1, item.maxStock)}
                            className="p-2 bg-gray-200 rounded hover:bg-gray-300">
                      <FaPlus size={12} />
                    </button>
                  </div>

                  <div className="text-right min-w-[100px]">
                    <p className="text-sm text-gray-500">Total</p>
                    <p className="text-lg font-bold">₹{(item.price * item.quantity).toLocaleString()}</p>
                  </div>

                  <button onClick={() => handleRemove(item.productId)}
                          className="text-red-600 hover:text-red-700">
                    <FaTrash size={18} />
                  </button>
                </div>
              ))}
            </div>

            {/* Order Summary */}
            <div className="lg:col-span-1">
              <div className="bg-white rounded-lg shadow p-6 sticky top-24">
                <h2 className="text-xl font-bold mb-6">Order Summary</h2>
                
                <div className="space-y-3 mb-6">
                  <div className="flex justify-between">
                    <span>Subtotal</span>
                    <span>₹{subtotal.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Tax (18%)</span>
                    <span>₹{tax.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Shipping</span>
                    <span>₹{shipping}</span>
                  </div>
                  <div className="border-t pt-3 flex justify-between text-xl font-bold">
                    <span>Total</span>
                    <span className="text-indigo-600">₹{total.toFixed(2)}</span>
                  </div>
                </div>

                <button onClick={handleCheckout}
                        disabled={loading}
                        className="w-full bg-indigo-600 text-white py-4 rounded-lg hover:bg-indigo-700 font-semibold disabled:bg-gray-400">
                  {loading ? "Processing..." : "Proceed to Checkout"}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Cart;