export const openRazorpay = (order, payment, token, navigate) => {

  const options = {
    key: "RAZORPAY_KEY_ID",
    amount: payment.amount * 100,
    currency: "INR",
    name: "E-Commerce App",
    description: "Order Payment",
    order_id: payment.transactionId,

    handler: async function (response) {

      const verifyPayload = {
        razorpayOrderId: response.razorpay_order_id,
        razorpayPaymentId: response.razorpay_payment_id,
        razorpaySignature: response.razorpay_signature
      };

      await fetch("http://localhost:8083/api/payment/verify", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(verifyPayload)
      });

      navigate("/my-orders");
    },

    prefill: {
      name: "User",
      email: "user@gmail.com",
      contact: "9999999999"
    },

    theme: {
      color: "#3399cc"
    }
  };

  const razor = new window.Razorpay(options);
  razor.open();
};
