require("dotenv").config();
const express = require("express");
const cors = require("cors");

const app = express();

// ==================== MIDDLEWARE ====================
app.use(cors({
  origin: "http://localhost:5173",
  credentials: true
}));
app.use(express.json());

// ==================== ROUTES ====================

/**
 * Health Check
 */
app.get("/health", (req, res) => {
  res.json({
    status: "Mock Payment Service Running",
    port: process.env.PORT || 9000
  });
});

/**
 * CREATE PAYMENT (FAKE)
 * POST /payment/create-order
 */
app.post("/payment/create-order", (req, res) => {
  const { amount, orderId } = req.body;

  console.log("📥 Mock payment request:", { amount, orderId });

  if (!amount || amount <= 0) {
    return res.status(400).json({ error: "Invalid amount" });
  }

  // Simulate payment order
  const paymentOrder = {
    paymentOrderId: "mock_order_" + Date.now(),
    amount,
    currency: "INR",
    status: "CREATED"
  };

  res.json(paymentOrder);
});

/**
 * VERIFY PAYMENT (FAKE)
 * POST /payment/verify
 */
app.post("/payment/verify", async (req, res) => {
  const { paymentOrderId, orderId } = req.body;

  console.log("📥 Verifying mock payment:", paymentOrderId);

  // Simulate success
  const success = true;

  if (success) {
    console.log("✅ Mock payment success");

    // 🔥 Call Order Service to mark order PAID
    // (goes through API Gateway)
    try {
      const axios = require("axios");

      await axios.put(
        `http://localhost:8080/orders/${orderId}/mark-paid`
      );
    } catch (err) {
      console.error("⚠️ Failed to mark order paid:", err.message);
    }

    return res.json({
      success: true,
      message: "Payment successful",
      orderId,
      paymentOrderId
    });
  }

  res.status(400).json({
    success: false,
    message: "Payment failed"
  });
});

/**
 * GET PAYMENT STATUS
 */
app.get("/payment/:paymentId", (req, res) => {
  res.json({
    paymentId: req.params.paymentId,
    status: "SUCCESS",
    amount: 1000,
    method: "MOCK"
  });
});

// ==================== START SERVER ====================
const PORT = process.env.PORT || 9000;

app.listen(PORT, () => {
  console.log(`💳 Mock Payment Service running on port ${PORT}`);
});
