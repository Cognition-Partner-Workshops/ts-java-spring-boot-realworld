import userEvent from "@testing-library/user-event";
import axios from "axios";
import Router from "next/router";
import React from "react";
import { describe, expect, it, vi } from "vitest";

import ArticleActions from "../../components/article/ArticleActions";
import ArticleList from "../../components/article/ArticleList";
import ArticleMeta from "../../components/article/ArticleMeta";
import ArticlePreview from "../../components/article/ArticlePreview";
import Comment from "../../components/comment/Comment";
import CommentList from "../../components/comment/CommentList";
import DeleteButton from "../../components/comment/DeleteButton";
import { SERVER_BASE_URL } from "../../lib/utils/constant";
import { article, comment, currentUser, loginAs, okResponse } from "../../test/fixtures";
import { setRoute } from "../../test/mockRouter";
import { renderWithProviders, screen, waitFor } from "../../test/utils";

describe("ArticlePreview", () => {
  it("renders the article summary with links", () => {
    renderWithProviders(<ArticlePreview article={article} />);
    expect(screen.getByRole("heading", { name: article.title })).toBeInTheDocument();
    expect(screen.getByText(article.description)).toBeInTheDocument();
    expect(screen.getByText("Read more...").closest("a")).toHaveAttribute(
      "href",
      `/article/${article.slug}`
    );
    expect(screen.getByText(article.author.username).closest("a")).toHaveAttribute(
      "href",
      `/profile/${article.author.username}`
    );
    expect(screen.getByText("dragons")).toHaveAttribute("href", "/?tag=dragons");
    expect(screen.getByRole("button", { name: /3/ })).toHaveClass("btn-outline-primary");
    expect(document.querySelectorAll("a a")).toHaveLength(0);
  });

  it("redirects guests to login when favoriting", async () => {
    const user = userEvent.setup();
    renderWithProviders(<ArticlePreview article={article} />);
    await user.click(screen.getByRole("button", { name: /3/ }));
    expect(Router.push).toHaveBeenCalledWith("/user/login");
    expect(axios.post).not.toHaveBeenCalled();
  });

  it("optimistically favorites and unfavorites for logged-in users", async () => {
    const user = userEvent.setup();
    loginAs();
    vi.mocked(axios.post).mockResolvedValue(okResponse({}));
    vi.mocked(axios.delete).mockResolvedValue(okResponse({}));
    renderWithProviders(<ArticlePreview article={article} />);
    await screen.findByText(article.author.username);
    await waitFor(() => expect(screen.getByRole("button", { name: /3/ })).toBeInTheDocument());

    await user.click(screen.getByRole("button", { name: /3/ }));
    expect(await screen.findByRole("button", { name: /4/ })).toHaveClass("btn-primary");
    await waitFor(() =>
      expect(axios.post).toHaveBeenCalledWith(
        `${SERVER_BASE_URL}/articles/${article.slug}/favorite`,
        {},
        { headers: { Authorization: `Token ${currentUser.token}` } }
      )
    );

    await user.click(screen.getByRole("button", { name: /4/ }));
    expect(await screen.findByRole("button", { name: /3/ })).toHaveClass("btn-outline-primary");
    await waitFor(() => expect(axios.delete).toHaveBeenCalled());
  });

  it("rolls back the optimistic update when the request fails", async () => {
    const user = userEvent.setup();
    loginAs();
    vi.mocked(axios.post).mockRejectedValue(new Error("nope"));
    renderWithProviders(<ArticlePreview article={article} />);
    await screen.findByText(article.author.username);
    await waitFor(() => expect(screen.getByRole("button", { name: /3/ })).toBeInTheDocument());
    await user.click(screen.getByRole("button", { name: /3/ }));
    await waitFor(() => expect(axios.post).toHaveBeenCalled());
    expect(await screen.findByRole("button", { name: /3/ })).toBeInTheDocument();
  });
});

describe("ArticleMeta / ArticleActions", () => {
  it("hides edit/delete for other users", async () => {
    loginAs({ ...currentUser, username: "someone-else" });
    renderWithProviders(<ArticleMeta article={article} />);
    expect(await screen.findByText(article.author.username)).toBeInTheDocument();
    expect(screen.queryByText(/Delete Article/)).not.toBeInTheDocument();
  });

  it("lets the author edit and delete", async () => {
    const user = userEvent.setup();
    loginAs();
    setRoute("/article/[pid]", { pid: article.slug });
    vi.mocked(axios.delete).mockResolvedValue(okResponse({}));
    vi.spyOn(window, "confirm").mockReturnValue(true);
    renderWithProviders(<ArticleActions article={article} />);

    expect(await screen.findByText(/Edit Article/)).toBeInTheDocument();
    expect(screen.getByText(/Edit Article/).closest("a")).toHaveAttribute(
      "href",
      `/editor/${article.slug}`
    );
    await user.click(screen.getByRole("button", { name: /Delete Article/ }));
    await waitFor(() =>
      expect(axios.delete).toHaveBeenCalledWith(`${SERVER_BASE_URL}/articles/${article.slug}`, {
        headers: { Authorization: `Token ${currentUser.token}` },
      })
    );
    await waitFor(() => expect(Router.push).toHaveBeenCalledWith("/"));
  });

  it("does nothing when the delete confirmation is declined", async () => {
    const user = userEvent.setup();
    loginAs();
    setRoute("/article/[pid]", { pid: article.slug });
    vi.spyOn(window, "confirm").mockReturnValue(false);
    renderWithProviders(<ArticleActions article={article} />);
    await user.click(await screen.findByRole("button", { name: /Delete Article/ }));
    expect(axios.delete).not.toHaveBeenCalled();
  });
});

describe("ArticleList", () => {
  it("loads the global feed and shows previews", async () => {
    vi.mocked(axios.get).mockResolvedValue(
      okResponse({ articles: [article], articlesCount: 1 })
    );
    renderWithProviders(<ArticleList />);
    expect(await screen.findByText(article.title)).toBeInTheDocument();
    expect(axios.get).toHaveBeenCalledWith(`${SERVER_BASE_URL}/articles?offset=0`, {});
    expect(document.querySelector(".pagination")).not.toBeInTheDocument();
  });

  it("shows pagination for large feeds", async () => {
    vi.mocked(axios.get).mockResolvedValue(
      okResponse({ articles: [article], articlesCount: 100 })
    );
    renderWithProviders(<ArticleList />);
    expect(await screen.findByText(article.title)).toBeInTheDocument();
    await waitFor(() => expect(document.querySelector(".pagination")).toBeInTheDocument());
  });

  it("uses the author / favorited / tag / feed endpoints", async () => {
    vi.mocked(axios.get).mockResolvedValue(okResponse({ articles: [], articlesCount: 0 }));

    setRoute("/profile/[pid]", { pid: "jane" }, "/profile/jane");
    const { unmount } = renderWithProviders(<ArticleList />);
    expect(await screen.findByText("No articles are here... yet.")).toBeInTheDocument();
    expect(axios.get).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/articles?author=jane&offset=0`,
      {}
    );
    unmount();

    setRoute("/profile/[pid]", { pid: "jane", favorite: "true" }, "/profile/jane?favorite=true");
    const second = renderWithProviders(<ArticleList />);
    await waitFor(() =>
      expect(axios.get).toHaveBeenCalledWith(
        `${SERVER_BASE_URL}/articles?favorited=jane&offset=0`,
        {}
      )
    );
    second.unmount();

    setRoute("/", { tag: "dragons" }, "/?tag=dragons");
    const third = renderWithProviders(<ArticleList />);
    await waitFor(() =>
      expect(axios.get).toHaveBeenCalledWith(
        `${SERVER_BASE_URL}/articles?tag=dragons&offset=0`,
        {}
      )
    );
    third.unmount();

    setRoute("/", { follow: "jane" }, "/?follow=jane");
    renderWithProviders(<ArticleList />);
    await waitFor(() =>
      expect(axios.get).toHaveBeenCalledWith(`${SERVER_BASE_URL}/articles/feed?offset=0`, {})
    );
  });

  it("renders an error state", async () => {
    vi.mocked(axios.get).mockRejectedValue(new Error("down"));
    renderWithProviders(<ArticleList />);
    expect(await screen.findByText("Cannot load recent articles...")).toBeInTheDocument();
  });
});

describe("comments", () => {
  it("CommentList renders fetched comments with the input", async () => {
    setRoute("/article/[pid]", { pid: article.slug });
    vi.mocked(axios.get).mockResolvedValue(okResponse({ comments: [comment] }));
    renderWithProviders(<CommentList />);
    expect(await screen.findByText(comment.body)).toBeInTheDocument();
    expect(screen.getByText("Sign in")).toBeInTheDocument();
    expect(axios.get).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/articles/${article.slug}/comments`,
      {}
    );
  });

  it("CommentList renders an error state", async () => {
    setRoute("/article/[pid]", { pid: article.slug });
    vi.mocked(axios.get).mockRejectedValue(new Error("down"));
    renderWithProviders(<CommentList />);
    expect(
      await screen.findByText("Cannot load comments related to this article...")
    ).toBeInTheDocument();
  });

  it("Comment shows the delete control only to its author", async () => {
    loginAs();
    const { unmount } = renderWithProviders(<Comment comment={comment} />);
    expect(await screen.findByLabelText("Delete comment")).toBeInTheDocument();
    unmount();

    loginAs({ ...currentUser, username: "stranger" });
    renderWithProviders(<Comment comment={comment} />);
    expect(await screen.findByText(comment.body)).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByLabelText("Delete comment")).not.toBeInTheDocument());
  });

  it("DeleteButton deletes with the auth header", async () => {
    const user = userEvent.setup();
    loginAs();
    setRoute("/article/[pid]", { pid: article.slug });
    vi.mocked(axios.delete).mockResolvedValue(okResponse({}));
    renderWithProviders(<DeleteButton commentId={comment.id} />);
    await waitFor(() => expect(screen.getByLabelText("Delete comment")).toBeInTheDocument());
    await new Promise((resolve) => setTimeout(resolve, 10));
    await user.click(screen.getByLabelText("Delete comment"));
    await waitFor(() =>
      expect(axios.delete).toHaveBeenCalledWith(
        `${SERVER_BASE_URL}/articles/${article.slug}/comments/${comment.id}`,
        { headers: { Authorization: `Token ${currentUser.token}` } }
      )
    );
  });
});
