import axios from "axios";
import { describe, expect, it, vi } from "vitest";

import ArticleAPI from "../../lib/api/article";
import CommentAPI from "../../lib/api/comment";
import TagAPI from "../../lib/api/tag";
import UserAPI from "../../lib/api/user";
import { SERVER_BASE_URL } from "../../lib/utils/constant";
import { currentUser, loginAs, okResponse } from "../../test/fixtures";

const jsonHeaders = { "Content-Type": "application/json" };

const axiosError = (status: number, data: unknown) =>
  Object.assign(new Error(`Request failed with status code ${status}`), {
    isAxiosError: true,
    response: { status, data },
  });

describe("ArticleAPI", () => {
  it("lists, filters and pages articles", async () => {
    vi.mocked(axios.get).mockResolvedValue(okResponse({ articles: [] }));
    await ArticleAPI.all(2);
    expect(axios.get).toHaveBeenLastCalledWith(`${SERVER_BASE_URL}/articles?limit=10&offset=20`);
    await ArticleAPI.byAuthor("jane doe", 1);
    expect(axios.get).toHaveBeenLastCalledWith(
      `${SERVER_BASE_URL}/articles?author=jane%20doe&limit=5&offset=5`
    );
    await ArticleAPI.byTag("c++");
    expect(axios.get).toHaveBeenLastCalledWith(
      `${SERVER_BASE_URL}/articles?tag=c%2B%2B&limit=10&offset=0`
    );
    await ArticleAPI.favoritedBy("jane", 0);
    expect(axios.get).toHaveBeenLastCalledWith(
      `${SERVER_BASE_URL}/articles?favorited=jane&limit=10&offset=0`
    );
    await ArticleAPI.feed(1);
    expect(axios.get).toHaveBeenLastCalledWith(`${SERVER_BASE_URL}/articles/feed?limit=10&offset=10`);
    await ArticleAPI.get("slug-1");
    expect(axios.get).toHaveBeenLastCalledWith(`${SERVER_BASE_URL}/articles/slug-1`);
  });

  it("sends the token when deleting", async () => {
    vi.mocked(axios.delete).mockResolvedValue(okResponse({}));
    await ArticleAPI.delete("slug-1", "tok");
    expect(axios.delete).toHaveBeenCalledWith(`${SERVER_BASE_URL}/articles/slug-1`, {
      headers: { Authorization: "Token tok" },
    });
  });

  it("favorites and unfavorites", async () => {
    vi.mocked(axios.post).mockResolvedValue(okResponse({}));
    vi.mocked(axios.delete).mockResolvedValue(okResponse({}));
    await ArticleAPI.favorite("slug-1");
    expect(axios.post).toHaveBeenCalledWith(`${SERVER_BASE_URL}/articles/slug-1/favorite`);
    await ArticleAPI.unfavorite("slug-1");
    expect(axios.delete).toHaveBeenCalledWith(`${SERVER_BASE_URL}/articles/slug-1/favorite`);
  });

  it("creates with a JSON body, auth header and tolerant status handling", async () => {
    vi.mocked(axios.post).mockResolvedValue(okResponse({ article: { slug: "x" } }, 201));
    const payload = { title: "T", description: "D", body: "B", tagList: ["a"] };
    const result = await ArticleAPI.create(payload, "tok");
    expect(result).toEqual({ data: { article: { slug: "x" } }, status: 201 });
    const [url, body, config] = vi.mocked(axios.post).mock.calls[0];
    expect(url).toBe(`${SERVER_BASE_URL}/articles`);
    expect(JSON.parse(body as string)).toEqual({ article: payload });
    expect(config?.headers).toEqual({ ...jsonHeaders, Authorization: "Token tok" });
    expect(config?.validateStatus?.(422)).toBe(true);
  });

  it("updates using the article slug", async () => {
    vi.mocked(axios.put).mockResolvedValue(okResponse({ errors: { title: ["can't be blank"] } }, 422));
    const result = await ArticleAPI.update(
      { title: "", description: "", body: "", tagList: [], slug: "slug-1" },
      undefined
    );
    expect(result.status).toBe(422);
    expect(axios.put).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/articles/slug-1`,
      expect.any(String),
      expect.objectContaining({ headers: { ...jsonHeaders, Authorization: "Token " } })
    );
  });
});

describe("UserAPI", () => {
  it("logs in with a JSON body and returns the response", async () => {
    vi.mocked(axios.post).mockResolvedValue(okResponse({ user: currentUser }));
    const response = await UserAPI.login("jane@example.com", "secret");
    expect(response.data.user).toEqual(currentUser);
    expect(axios.post).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/users/login`,
      JSON.stringify({ user: { email: "jane@example.com", password: "secret" } }),
      { headers: jsonHeaders }
    );
  });

  it("returns the error response body on 4xx instead of throwing", async () => {
    vi.mocked(axios.post).mockRejectedValue(
      axiosError(422, { errors: { "email or password": ["is invalid"] } })
    );
    const response = await UserAPI.login("x", "y");
    expect(response.status).toBe(422);
    expect(response.data.errors).toEqual({ "email or password": ["is invalid"] });
  });

  it("re-throws network errors that have no response", async () => {
    vi.mocked(axios.post).mockRejectedValue(new Error("Network Error"));
    await expect(UserAPI.register("u", "e", "p")).rejects.toThrow("Network Error");
  });

  it("registers with the expected payload", async () => {
    vi.mocked(axios.post).mockResolvedValue(okResponse({ user: currentUser }, 201));
    await UserAPI.register("janedoe", "jane@example.com", "secret");
    expect(axios.post).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/users`,
      JSON.stringify({ user: { username: "janedoe", email: "jane@example.com", password: "secret" } }),
      { headers: jsonHeaders }
    );
  });

  it("reads the token from storage for the current user", async () => {
    loginAs();
    vi.mocked(axios.get).mockResolvedValue(okResponse({ user: currentUser }));
    await UserAPI.current();
    expect(axios.get).toHaveBeenCalledWith(`${SERVER_BASE_URL}/user`, {
      headers: { Authorization: `Token ${encodeURIComponent(currentUser.token)}` },
    });
  });

  it("tolerates a corrupt or missing stored user", async () => {
    window.localStorage.setItem("user", "{broken");
    vi.mocked(axios.get).mockResolvedValue(okResponse({}));
    await UserAPI.current();
    expect(axios.get).toHaveBeenLastCalledWith(`${SERVER_BASE_URL}/user`, {
      headers: { Authorization: "Token " },
    });
  });

  it("saves, follows, unfollows and fetches profiles", async () => {
    loginAs();
    vi.mocked(axios.put).mockResolvedValue(okResponse({}));
    vi.mocked(axios.post).mockResolvedValue(okResponse({}));
    vi.mocked(axios.delete).mockResolvedValue(okResponse({}));
    vi.mocked(axios.get).mockResolvedValue(okResponse({}));

    await UserAPI.save({ bio: "new" });
    expect(axios.put).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/user`,
      JSON.stringify({ user: { bio: "new" } }),
      { headers: jsonHeaders }
    );

    await UserAPI.follow("bob");
    expect(axios.post).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/profiles/bob/follow`,
      {},
      { headers: { Authorization: `Token ${currentUser.token}` } }
    );

    await UserAPI.unfollow("bob");
    expect(axios.delete).toHaveBeenCalledWith(`${SERVER_BASE_URL}/profiles/bob/follow`, {
      headers: { Authorization: `Token ${currentUser.token}` },
    });

    await UserAPI.get("bob");
    expect(axios.get).toHaveBeenCalledWith(`${SERVER_BASE_URL}/profiles/bob`);
  });

  it("propagates error responses for save/follow/unfollow", async () => {
    vi.mocked(axios.put).mockRejectedValue(axiosError(401, { message: "unauthorized" }));
    vi.mocked(axios.post).mockRejectedValue(axiosError(404, {}));
    vi.mocked(axios.delete).mockRejectedValue(axiosError(404, {}));
    expect((await UserAPI.save({})).status).toBe(401);
    expect((await UserAPI.follow("x")).status).toBe(404);
    expect((await UserAPI.unfollow("x")).status).toBe(404);
  });
});

describe("CommentAPI", () => {
  it("creates, deletes and lists comments", async () => {
    vi.mocked(axios.post).mockResolvedValue(okResponse({}));
    vi.mocked(axios.delete).mockResolvedValue(okResponse({}));
    vi.mocked(axios.get).mockResolvedValue(okResponse({ comments: [] }));

    await CommentAPI.create("slug-1", { body: "hi" });
    expect(axios.post).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/articles/slug-1/comments`,
      JSON.stringify({ comment: { body: "hi" } })
    );
    await CommentAPI.delete("slug-1", "c1");
    expect(axios.delete).toHaveBeenCalledWith(`${SERVER_BASE_URL}/articles/slug-1/comments/c1`);
    await CommentAPI.forArticle("slug-1");
    expect(axios.get).toHaveBeenCalledWith(`${SERVER_BASE_URL}/articles/slug-1/comments`);
  });

  it("returns error responses and rethrows unknown errors", async () => {
    vi.mocked(axios.post).mockRejectedValue(axiosError(403, { errors: {} }));
    expect((await CommentAPI.create("s", { body: "" })).status).toBe(403);
    vi.mocked(axios.delete).mockRejectedValue(new Error("boom"));
    await expect(CommentAPI.delete("s", "c")).rejects.toThrow("boom");
  });
});

describe("TagAPI", () => {
  it("fetches all tags", async () => {
    vi.mocked(axios.get).mockResolvedValue(okResponse({ tags: ["a"] }));
    const response = await TagAPI.getAll();
    expect(response.data.tags).toEqual(["a"]);
    expect(axios.get).toHaveBeenCalledWith(`${SERVER_BASE_URL}/tags`);
  });
});
