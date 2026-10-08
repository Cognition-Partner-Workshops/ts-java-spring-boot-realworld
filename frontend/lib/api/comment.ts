import axios, { AxiosError, AxiosResponse } from "axios";

import { SERVER_BASE_URL } from "../utils/constant";

const errorResponse = (error: unknown): AxiosResponse => {
  const response = (error as AxiosError).response;
  if (response) return response;
  throw error;
};

const CommentAPI = {
  create: async (slug: string, comment: { body: string }) => {
    try {
      const response = await axios.post(
        `${SERVER_BASE_URL}/articles/${slug}/comments`,
        JSON.stringify({ comment })
      );
      return response;
    } catch (error) {
      return errorResponse(error);
    }
  },
  delete: async (slug: string, commentId: string) => {
    try {
      const response = await axios.delete(
        `${SERVER_BASE_URL}/articles/${slug}/comments/${commentId}`
      );
      return response;
    } catch (error) {
      return errorResponse(error);
    }
  },

  forArticle: (slug: string) =>
    axios.get(`${SERVER_BASE_URL}/articles/${slug}/comments`),
};

export default CommentAPI;
