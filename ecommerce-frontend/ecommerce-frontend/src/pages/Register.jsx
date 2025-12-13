import React, { useState } from "react";
import { Link } from "react-router-dom";

const Register = () => {
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    phone: "",
    password: "",
  });

  const [errors, setErrors] = useState({});
  const [success, setSuccess] = useState("");
  const [apiError, setApiError] = useState("");

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const validate = () => {
    const errs = {};
    if (!formData.name) errs.name = "Name required";
    if (!formData.email) errs.email = "Email required";
    if (!/^\d{10}$/.test(formData.phone)) errs.phone = "Phone must be 10 digits";
    if (formData.password.length < 6)
      errs.password = "Password must be 6+ chars";
    return errs;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) {
      setErrors(errs);
      return;
    }

    try {
      const res = await fetch("http://localhost:8080/api/users/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      if (!res.ok) throw new Error("Registration failed");

      setSuccess("Registration successful!");
      setFormData({ name: "", email: "", phone: "", password: "" });
    } catch (err) {
      setApiError(err.message);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-lg w-full max-w-md">
        <h2 className="text-3xl font-bold text-center mb-6">Register</h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          <input name="name" placeholder="Name" className="w-full p-2 border rounded" onChange={handleChange} value={formData.name} />
          <input name="email" placeholder="Email" className="w-full p-2 border rounded" onChange={handleChange} value={formData.email} />
          <input name="phone" placeholder="Phone" className="w-full p-2 border rounded" onChange={handleChange} value={formData.phone} />
          <input type="password" name="password" placeholder="Password" className="w-full p-2 border rounded" onChange={handleChange} value={formData.password} />

          <button className="w-full bg-indigo-600 text-white py-2 rounded">
            Register
          </button>

          {success && <p className="text-green-600 text-center">{success}</p>}
          {apiError && <p className="text-red-500 text-center">{apiError}</p>}

          <p className="text-sm text-center">
            Already have an account?{" "}
            <Link to="/login" className="text-indigo-600">
              Login
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
};

export default Register;
