import axios, { AxiosError, AxiosResponse } from "axios";

import { SERVER_BASE_URL } from "../utils/constant";

const errorResponse = (error: unknown): AxiosResponse => {
  const response = (error as AxiosError).response;
  if (response) return response;
  throw error;
};

const readToken = (): string | undefined => {
  if (typeof window === "undefined") return undefined;
  const raw = window.localStorage.getItem("user");
  if (!raw) return undefined;
  try {
    return JSON.parse(raw)?.token;
  } catch {
    return undefined;
  }
};

const UserAPI = {
  current: async () => {
    const token = readToken();
    try {
      const response = await axios.get(`${SERVER_BASE_URL}/user`, {
        headers: {
          Authorization: `Token ${encodeURIComponent(token ?? "")}`,
        },
      });
      return response;
    } catch (error) {
      return errorResponse(error);
    }
  },
  login: async (email: string, password: string) => {
    try {
      const response = await axios.post(
        `${SERVER_BASE_URL}/users/login`,
        JSON.stringify({ user: { email, password } }),
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      return response;
    } catch (error) {
      return errorResponse(error);
    }
  },
  register: async (username: string, email: string, password: string) => {
    try {
      const response = await axios.post(
        `${SERVER_BASE_URL}/users`,
        JSON.stringify({ user: { username, email, password } }),
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      return response;
    } catch (error) {
      return errorResponse(error);
    }
  },
  save: async (user: Record<string, unknown>) => {
    try {
      const response = await axios.put(
        `${SERVER_BASE_URL}/user`,
        JSON.stringify({ user }),
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      return response;
    } catch (error) {
      return errorResponse(error);
    }
  },
  follow: async (username: string) => {
    const token = readToken();
    try {
      const response = await axios.post(
        `${SERVER_BASE_URL}/profiles/${username}/follow`,
        {},
        {
          headers: {
            Authorization: `Token ${encodeURIComponent(token ?? "")}`,
          },
        }
      );
      return response;
    } catch (error) {
      return errorResponse(error);
    }
  },
  unfollow: async (username: string) => {
    const token = readToken();
    try {
      const response = await axios.delete(
        `${SERVER_BASE_URL}/profiles/${username}/follow`,
        {
          headers: {
            Authorization: `Token ${encodeURIComponent(token ?? "")}`,
          },
        }
      );
      return response;
    } catch (error) {
      return errorResponse(error);
    }
  },
  get: async (username: string) =>
    axios.get(`${SERVER_BASE_URL}/profiles/${username}`),
};

export default UserAPI;
