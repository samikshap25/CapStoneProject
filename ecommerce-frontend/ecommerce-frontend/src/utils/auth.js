// src/utils/auth.js

/**
 * Check if user is authenticated
 */
export const isAuthenticated = () => {
  const token = localStorage.getItem("token");
  return !!token;
};

/**
 * Get the JWT token from localStorage
 */
export const getToken = () => {
  return localStorage.getItem("token");
};

/**
 * Decode JWT token to extract user information
 */
const decodeToken = (token) => {
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error("Failed to decode token:", error);
    return null;
  }
};

/**
 * Get user role from JWT token
 * Returns: 'ADMIN', 'USER', or null
 */
export const getUserRole = () => {
  const token = getToken();
  if (!token) return null;

  const decoded = decodeToken(token);
  // Extract role from token
  return decoded?.role || "USER";
};

/**
 * Get username from JWT token
 */
export const getUsername = () => {
  const token = getToken();
  if (!token) return null;

  const decoded = decodeToken(token);
  return decoded?.sub || null;
};

/**
 * Check if user is admin
 */
export const isAdmin = () => {
  const role = getUserRole();
  return role === "ADMIN";
};

/**
 * Logout user
 */
export const logout = () => {
  localStorage.removeItem("token");
  window.location.href = "/login";
};

/**
 * Check if token is expired
 */
export const isTokenExpired = () => {
  const token = getToken();
  if (!token) return true;

  const decoded = decodeToken(token);
  if (!decoded || !decoded.exp) return true;

  const currentTime = Date.now() / 1000;
  return decoded.exp < currentTime;
};