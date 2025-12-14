export const createOrder = async (orderData, token) => {
  const res = await fetch("http://localhost:8082/orders", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify(orderData)
  });
  return res.json();
};
