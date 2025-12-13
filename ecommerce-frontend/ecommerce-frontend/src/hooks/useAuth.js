// src/hooks/useAuth.js
import { useEffect, useState } from "react";
import { getToken } from "../utils/auth";

export const useAuth = () => {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const token = getToken();
    if (!token) return;

    fetch("http://localhost:8081/api/auth/me", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then(res => res.json())
      .then(data => setUser(data))
      .catch(() => setUser(null));
  }, []);

  return user;
};
