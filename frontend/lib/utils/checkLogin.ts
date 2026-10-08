const checkLogin = (currentUser: unknown): boolean =>
  !!currentUser &&
  typeof currentUser === "object" &&
  (currentUser as object).constructor === Object &&
  Object.keys(currentUser as object).length !== 0;

export default checkLogin;
