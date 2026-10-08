import { mutate } from "swr";

import { SERVER_BASE_URL } from "./constant";

const ARTICLES_PREFIX = `${SERVER_BASE_URL}/articles`;

export const isArticleKey = (key: unknown): boolean =>
  typeof key === "string" && key.startsWith(ARTICLES_PREFIX);

const invalidateArticles = () => mutate(isArticleKey, undefined, { revalidate: true });

export default invalidateArticles;
