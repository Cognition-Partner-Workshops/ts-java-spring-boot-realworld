import { ErrorMap } from "../../components/common/ListErrors";

interface ApiErrorBody {
  errors?: ErrorMap;
  message?: string;
}

export const MESSAGE_KEY = "";

export const toErrorMap = (data: unknown): ErrorMap => {
  if (!data || typeof data !== "object") return {};
  const { errors, message } = data as ApiErrorBody;
  if (errors && Object.keys(errors).length > 0) return errors;
  if (message) return { [MESSAGE_KEY]: [message] };
  return {};
};

export default toErrorMap;
