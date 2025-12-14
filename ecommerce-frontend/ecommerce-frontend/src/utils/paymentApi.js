export const createPayment = async (paymentData, token) => {
  const res = await fetch("http://localhost:8083/api/payment", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify(paymentData)
  });
  return res.json();
};
