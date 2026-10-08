const storage = async (key: string) => {
  if (typeof window === "undefined") return undefined;
  const value = window.localStorage.getItem(key);
  return value ? JSON.parse(value) : undefined;
};

export default storage;
