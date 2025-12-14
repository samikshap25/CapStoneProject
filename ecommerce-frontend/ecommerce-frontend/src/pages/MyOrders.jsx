// src/pages/MyOrders.jsx
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { FaArrowLeft, FaBox } from "react-icons/fa";
import { getAllOrders } from "../services/OrderService";

const MyOrders = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const ordersData = await getAllOrders();
      setOrders(ordersData.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString("en-IN", {
      day: "numeric",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });
  };

  const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case "PLACED": return "bg-blue-100 text-blue-800";
      case "SHIPPED": return "bg-purple-100 text-purple-800";
      case "DELIVERED": return "bg-green-100 text-green-800";
      default: return "bg-gray-100 text-gray-800";
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-xl">Loading orders...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-md sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <button onClick={() => navigate("/home")}
                  className="flex items-center gap-2 text-gray-600 hover:text-indigo-600">
            <FaArrowLeft /> Back
          </button>
          <h1 className="text-2xl font-bold text-indigo-600">My Orders</h1>
          <div className="w-20"></div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-6 py-10">
        {orders.length === 0 ? (
          <div className="bg-white rounded-lg shadow-md p-16 text-center">
            <div className="text-6xl mb-4">📦</div>
            <h2 className="text-2xl font-bold mb-4">No Orders Yet</h2>
            <button onClick={() => navigate("/home")}
                    className="bg-indigo-600 text-white px-8 py-3 rounded-lg hover:bg-indigo-700">
              Start Shopping
            </button>
          </div>
        ) : (
          <div className="space-y-6">
            <h2 className="text-xl font-bold">Total Orders: {orders.length}</h2>

            {orders.map((order) => (
              <div key={order.orderId} className="bg-white rounded-lg shadow-md overflow-hidden">
                {/* Order Header */}
                <div className="bg-gray-50 px-6 py-4 border-b flex justify-between">
                  <div>
                    <p className="text-sm text-gray-600">Order ID</p>
                    <p className="font-bold text-lg">#{order.orderId}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm text-gray-600">Placed on</p>
                    <p className="font-semibold">{formatDate(order.createdAt)}</p>
                  </div>
                  <span className={`px-4 py-2 rounded-full font-semibold ${getStatusColor(order.status)}`}>
                    {order.status}
                  </span>
                </div>

                {/* Order Items */}
                <div className="p-6">
                  {order.items?.map((item, index) => (
                    <div key={index} className="flex justify-between py-3 border-b last:border-b-0">
                      <div className="flex items-center gap-3">
                        <FaBox className="text-indigo-600" size={20} />
                        <div>
                          <p className="font-semibold">Product ID: {item.productId}</p>
                          <p className="text-sm text-gray-600">Qty: {item.quantity}</p>
                        </div>
                      </div>
                      <p className="font-bold">₹{item.price.toLocaleString()}</p>
                    </div>
                  ))}
                  
                  {/* Total */}
                  <div className="flex justify-between pt-4 mt-4 border-t">
                    <span className="text-lg font-semibold">Order Total:</span>
                    <span className="text-2xl font-bold text-indigo-600">
                      ₹{order.totalAmount.toLocaleString()}
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyOrders;