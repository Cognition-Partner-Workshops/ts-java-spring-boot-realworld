import { ArticleType, Author } from "../lib/types/articleType";
import { CommentType } from "../lib/types/commentType";

export const author: Author = {
  username: "janedoe",
  bio: "I work at statefarm",
  image: "https://example.com/jane.png",
  following: false,
};

export const currentUser = {
  email: "jane@example.com",
  username: "janedoe",
  bio: "I work at statefarm",
  image: "https://example.com/jane.png",
  token: "jwt-token-123",
};

export const article: ArticleType = {
  slug: "how-to-train-your-dragon",
  title: "How to train your dragon",
  description: "Ever wonder how?",
  body: "# Heading\n\nIt takes a **Jacobian**.",
  tagList: ["dragons", "training"],
  createdAt: Date.UTC(2024, 0, 15),
  updatedAt: Date.UTC(2024, 0, 16),
  favorited: false,
  favoritesCount: 3,
  author,
};

export const comment: CommentType = {
  id: "comment-1",
  body: "Great article!",
  slug: article.slug,
  createdAt: Date.UTC(2024, 1, 1),
  updatedAt: Date.UTC(2024, 1, 1),
  author,
};

export const loginAs = (user: Record<string, unknown> = currentUser) => {
  window.localStorage.setItem("user", JSON.stringify(user));
};

export const okResponse = <T,>(data: T, status = 200) => ({
  data,
  status,
  statusText: "OK",
  headers: {},
  config: {},
});
