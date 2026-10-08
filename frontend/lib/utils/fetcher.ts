import axios, { AxiosRequestConfig } from "axios";

const updateOptions = (): AxiosRequestConfig => {
  if (typeof window === "undefined") return {};

  const rawUser = window.localStorage.getItem("user");
  if (!rawUser) return {};

  try {
    const user = JSON.parse(rawUser);
    if (user?.token) {
      return {
        headers: {
          Authorization: `Token ${user.token}`,
        },
      };
    }
  } catch {
    return {};
  }
  return {};
};

export default async function fetcher<T = unknown>(url: string): Promise<T> {
  const { data } = await axios.get<T>(url, updateOptions());
  return data;
}
