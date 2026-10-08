import axios from "axios";
import { describe, expect, it, vi } from "vitest";

import { getPageInfo, getRange } from "../../lib/utils/calculatePagination";
import checkLogin from "../../lib/utils/checkLogin";
import { DEFAULT_PROFILE_IMAGE, SERVER_BASE_URL } from "../../lib/utils/constant";
import editorReducer, { EditorState } from "../../lib/utils/editorReducer";
import fetcher from "../../lib/utils/fetcher";
import { getQuery } from "../../lib/utils/getQuery";
import handleBrokenImage from "../../lib/utils/handleBrokenImage";
import storage from "../../lib/utils/storage";
import { currentUser, loginAs, okResponse } from "../../test/fixtures";

describe("constant", () => {
  it("defaults the API base URL to the local backend", () => {
    expect(SERVER_BASE_URL).toBe("http://localhost:8080");
  });
});

describe("checkLogin", () => {
  it("is true only for a non-empty plain object", () => {
    expect(checkLogin(currentUser)).toBe(true);
    expect(checkLogin({})).toBe(false);
    expect(checkLogin(undefined)).toBe(false);
    expect(checkLogin(null)).toBe(false);
    expect(checkLogin("token")).toBe(false);
    expect(checkLogin([1])).toBe(false);
  });
});

describe("getQuery", () => {
  it("builds limit/offset", () => {
    expect(getQuery(10, 0)).toBe("limit=10&offset=0");
    expect(getQuery(10, 3)).toBe("limit=10&offset=30");
  });
});

describe("calculatePagination", () => {
  it("getRange is inclusive", () => {
    expect(getRange(2, 5)).toEqual([2, 3, 4, 5]);
    expect(getRange(0, 0)).toEqual([0]);
  });

  it("computes a window around the current page", () => {
    const info = getPageInfo({ limit: 20, pageCount: 5, total: 200, page: 5 });
    expect(info.totalPages).toBe(10);
    expect(info.firstPage).toBe(3);
    expect(info.lastPage).toBe(7);
    expect(info.hasPreviousPage).toBe(true);
    expect(info.hasNextPage).toBe(true);
    expect(info.previousPage).toBe(4);
    expect(info.nextPage).toBe(6);
  });

  it("clamps the page to the last page and widens the window at the start", () => {
    const info = getPageInfo({ limit: 20, pageCount: 5, total: 200, page: 42 });
    expect(info.hasNextPage).toBe(false);
    expect(info.lastPage).toBe(10);

    const first = getPageInfo({ limit: 20, pageCount: 5, total: 200, page: 0 });
    expect(first.firstPage).toBe(0);
    expect(first.hasPreviousPage).toBe(false);
    expect(first.lastPage - first.firstPage + 1).toBe(5);
  });

  it("shrinks the window when it is wider than pageCount", () => {
    const info = getPageInfo({ limit: 20, pageCount: 4, total: 200, page: 5 });
    expect(info.lastPage - info.firstPage + 1).toBeLessThanOrEqual(4);
  });
});

describe("editorReducer", () => {
  const state: EditorState = { title: "", description: "", body: "", tagList: [] };

  it("handles every action", () => {
    let next = editorReducer(state, { type: "SET_TITLE", text: "T" });
    next = editorReducer(next, { type: "SET_DESCRIPTION", text: "D" });
    next = editorReducer(next, { type: "SET_BODY", text: "B" });
    next = editorReducer(next, { type: "ADD_TAG", tag: "a" });
    next = editorReducer(next, { type: "ADD_TAG", tag: "b" });
    expect(next).toEqual({ title: "T", description: "D", body: "B", tagList: ["a", "b"] });
    next = editorReducer(next, { type: "REMOVE_TAG", tag: "a" });
    expect(next.tagList).toEqual(["b"]);
  });

  it("throws on unknown actions", () => {
    expect(() =>
      editorReducer(state, { type: "NOPE" } as unknown as Parameters<typeof editorReducer>[1])
    ).toThrow("Unhandled action");
  });
});

describe("storage", () => {
  it("reads and parses JSON from localStorage", async () => {
    loginAs();
    await expect(storage("user")).resolves.toEqual(currentUser);
  });

  it("returns undefined when the key is missing", async () => {
    await expect(storage("user")).resolves.toBeUndefined();
  });
});

describe("fetcher", () => {
  it("sends the stored token as an Authorization header", async () => {
    loginAs();
    vi.mocked(axios.get).mockResolvedValue(okResponse({ ok: true }));
    const result = await fetcher("http://localhost:8080/user");
    expect(result).toEqual({ ok: true });
    expect(axios.get).toHaveBeenCalledWith("http://localhost:8080/user", {
      headers: { Authorization: `Token ${currentUser.token}` },
    });
  });

  it("sends no headers when logged out or storage is corrupt", async () => {
    vi.mocked(axios.get).mockResolvedValue(okResponse({}));
    await fetcher("http://localhost:8080/tags");
    expect(axios.get).toHaveBeenLastCalledWith("http://localhost:8080/tags", {});

    window.localStorage.setItem("user", "{not json");
    await fetcher("http://localhost:8080/tags");
    expect(axios.get).toHaveBeenLastCalledWith("http://localhost:8080/tags", {});
  });
});

describe("handleBrokenImage", () => {
  it("swaps in the default avatar and disables further error handling", () => {
    const img = document.createElement("img");
    handleBrokenImage({ currentTarget: img } as unknown as React.SyntheticEvent<HTMLImageElement>);
    expect(img.getAttribute("src")).toBe(DEFAULT_PROFILE_IMAGE);
    expect(img.onerror).toBeNull();
  });
});

describe("toErrorMap", () => {
  it("prefers the errors map, falls back to message, else empty", async () => {
    const { toErrorMap, MESSAGE_KEY } = await import("../../lib/utils/errors");
    expect(toErrorMap({ errors: { email: ["is invalid"] } })).toEqual({ email: ["is invalid"] });
    expect(toErrorMap({ errors: {}, message: "nope" })).toEqual({ [MESSAGE_KEY]: ["nope"] });
    expect(toErrorMap({ message: "invalid email or password" })).toEqual({
      [MESSAGE_KEY]: ["invalid email or password"],
    });
    expect(toErrorMap(undefined)).toEqual({});
    expect(toErrorMap("oops")).toEqual({});
    expect(toErrorMap({})).toEqual({});
  });
});

describe("invalidateArticles", () => {
  it("matches only article cache keys", async () => {
    const { isArticleKey } = await import("../../lib/utils/invalidateArticles");
    const { SERVER_BASE_URL } = await import("../../lib/utils/constant");
    expect(isArticleKey(`${SERVER_BASE_URL}/articles?offset=0`)).toBe(true);
    expect(isArticleKey(`${SERVER_BASE_URL}/articles/some-slug`)).toBe(true);
    expect(isArticleKey(`${SERVER_BASE_URL}/articles/some-slug/comments`)).toBe(true);
    expect(isArticleKey(`${SERVER_BASE_URL}/tags`)).toBe(false);
    expect(isArticleKey("user")).toBe(false);
    expect(isArticleKey(undefined)).toBe(false);
  });
});
