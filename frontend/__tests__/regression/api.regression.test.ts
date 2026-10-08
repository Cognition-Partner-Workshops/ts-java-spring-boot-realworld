import axios from "axios";
import { describe, expect, it, vi } from "vitest";

import ArticleAPI from "../../lib/api/article";
import CommentAPI from "../../lib/api/comment";
import TagAPI from "../../lib/api/tag";
import UserAPI from "../../lib/api/user";
import { SERVER_BASE_URL } from "../../lib/utils/constant";
import { okResponse } from "../../test/fixtures";

describe("regression: API client URLs and auth headers", () => {
  it("encodes author/tag filters and applies default paging", async () => {
    vi.mocked(axios.get).mockResolvedValue(okResponse({ articles: [], articlesCount: 0 }));
    await ArticleAPI.byAuthor("jane doe");
    await ArticleAPI.byTag("c++", 2);
    await ArticleAPI.favoritedBy("jane doe", 1);
    await ArticleAPI.all(3);
    await ArticleAPI.feed(0, 5);
    const urls = vi.mocked(axios.get).mock.calls.map((c) => c[0]);
    expect(urls).toEqual([
      `${SERVER_BASE_URL}/articles?author=jane%20doe&limit=5&offset=0`,
      `${SERVER_BASE_URL}/articles?tag=c%2B%2B&limit=10&offset=20`,
      `${SERVER_BASE_URL}/articles?favorited=jane%20doe&limit=10&offset=10`,
      `${SERVER_BASE_URL}/articles?limit=10&offset=30`,
      `${SERVER_BASE_URL}/articles/feed?limit=5&offset=0`,
    ]);
  });

  it("fetches single articles and tags without auth headers", async () => {
    vi.mocked(axios.get).mockResolvedValue(okResponse({}));
    await ArticleAPI.get("some-slug");
    await TagAPI.getAll();
    await CommentAPI.forArticle("some-slug");
    expect(vi.mocked(axios.get).mock.calls).toEqual([
      [`${SERVER_BASE_URL}/articles/some-slug`],
      [`${SERVER_BASE_URL}/tags`],
      [`${SERVER_BASE_URL}/articles/some-slug/comments`],
    ]);
  });

  it("returns the 4xx body for create/update instead of throwing", async () => {
    vi.mocked(axios.post).mockResolvedValue(okResponse({ errors: { title: ["can't be blank"] } }, 422));
    vi.mocked(axios.put).mockResolvedValue(okResponse({ errors: { body: ["can't be blank"] } }, 422));
    const payload = { title: "", description: "", body: "", tagList: [] as string[] };
    const created = await ArticleAPI.create(payload, "t0k/en");
    const updated = await ArticleAPI.update({ ...payload, slug: "s" }, undefined);
    expect(created).toEqual({ status: 422, data: { errors: { title: ["can't be blank"] } } });
    expect(updated).toEqual({ status: 422, data: { errors: { body: ["can't be blank"] } } });
    const postConfig = vi.mocked(axios.post).mock.calls[0][2] as {
      headers: Record<string, string>;
      validateStatus: () => boolean;
    };
    expect(postConfig.headers.Authorization).toBe("Token t0k%2Fen");
    expect(postConfig.validateStatus()).toBe(true);
    const putConfig = vi.mocked(axios.put).mock.calls[0][2] as { headers: Record<string, string> };
    expect(putConfig.headers.Authorization).toBe("Token ");
  });

  it("UserAPI.current sends an empty token when nobody is stored or storage is corrupt", async () => {
    vi.mocked(axios.get).mockResolvedValue(okResponse({ user: {} }));
    await UserAPI.current();
    window.localStorage.setItem("user", "{not json");
    await UserAPI.current();
    for (const call of vi.mocked(axios.get).mock.calls) {
      expect(call[0]).toBe(`${SERVER_BASE_URL}/user`);
      expect((call[1] as { headers: Record<string, string> }).headers.Authorization).toBe("Token ");
    }
  });

  it("UserAPI.current returns a 401 body instead of throwing", async () => {
    vi.mocked(axios.get).mockRejectedValue(
      Object.assign(new Error("401"), { isAxiosError: true, response: { status: 401, data: {} } })
    );
    const response = await UserAPI.current();
    expect(response.status).toBe(401);
  });

  it("follow/unfollow read the token from localStorage", async () => {
    window.localStorage.setItem("user", JSON.stringify({ token: "abc def" }));
    vi.mocked(axios.post).mockResolvedValue(okResponse({ profile: { following: true } }));
    vi.mocked(axios.delete).mockResolvedValue(okResponse({ profile: { following: false } }));
    await UserAPI.follow("janedoe");
    await UserAPI.unfollow("janedoe");
    expect(axios.post).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/profiles/janedoe/follow`,
      {},
      { headers: { Authorization: "Token abc%20def" } }
    );
    expect(axios.delete).toHaveBeenCalledWith(`${SERVER_BASE_URL}/profiles/janedoe/follow`, {
      headers: { Authorization: "Token abc%20def" },
    });
  });

  it("comment create/delete surface the backend error body", async () => {
    const rejection = Object.assign(new Error("422"), {
      isAxiosError: true,
      response: { status: 422, data: { errors: { body: ["can't be empty"] } } },
    });
    vi.mocked(axios.post).mockRejectedValue(rejection);
    vi.mocked(axios.delete).mockRejectedValue(
      Object.assign(new Error("403"), { isAxiosError: true, response: { status: 403, data: {} } })
    );
    const created = await CommentAPI.create("slug", { body: "" });
    const deleted = await CommentAPI.delete("slug", "42");
    expect(created.status).toBe(422);
    expect(created.data.errors.body).toEqual(["can't be empty"]);
    expect(deleted.status).toBe(403);
    expect(axios.delete).toHaveBeenCalledWith(`${SERVER_BASE_URL}/articles/slug/comments/42`);
  });
});
