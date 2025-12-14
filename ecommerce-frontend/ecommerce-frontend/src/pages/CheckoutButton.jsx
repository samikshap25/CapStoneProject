const CheckoutButton = ({ cart, user, token }) => {

  const navigate = useNavigate();

  const handleCheckout = async () => {

    // 1️⃣ Create Order
    const order = await createOrder({
      userId: user.id,
      items: cart.items
    }, token);

    // 2️⃣ Create Payment
    const payment = await createPayment({
      orderId: order.orderId,
      userId: user.id,
      amount: order.totalAmount
    }, token);

    // 3️⃣ Open Razorpay
    openRazorpay(order, payment, token, navigate);
  };

  return (
    <button onClick={handleCheckout} className="checkout-btn">
      Pay Now
    </button>
  );
};
