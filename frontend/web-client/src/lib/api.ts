import axios from "axios";
import { loadAuth } from "./storage";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080"
});

api.interceptors.request.use((config) => {
  const auth = loadAuth();
  if (auth?.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

export default api;
