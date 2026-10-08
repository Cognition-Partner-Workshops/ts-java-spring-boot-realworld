import axios from "axios";
import React from "react";
import { describe, expect, it, vi } from "vitest";

import { SERVER_BASE_URL } from "../../lib/utils/constant";
import ArticlePage from "../../pages/article/[pid]";
import UpdateArticleEditor from "../../pages/editor/[pid]";
import PublishArticleEditor from "../../pages/editor/new";
import Home from "../../pages/index";
import Profile from "../../pages/profile/[pid]";
import Login from "../../pages/user/login";
import Register from "../../pages/user/register";
import Settings from "../../pages/user/settings";
import { article, author, comment, currentUser, loginAs, okResponse } from "../../test/fixtures";
import { setRoute } from "../../test/mockRouter";
import { renderWithProviders, screen, waitFor } from "../../test/utils";

const mockBackend = () => {
  vi.mocked(axios.get).mockImplementation(async (url: string) => {
    if (url.endsWith("/tags")) return okResponse({ tags: ["dragons", "training"] });
    if (url.includes("/comments")) return okResponse({ comments: [comment] });
    if (url.includes("/profiles/")) return okResponse({ profile: author });
    if (url.match(/\/articles\/[^/?]+$/)) return okResponse({ article });
    if (url.includes("/articles")) return okResponse({ articles: [article], articlesCount: 1 });
    return okResponse({});
  });
};

describe("page smoke tests", () => {
  it("home renders the feed and popular tags", async () => {
    mockBackend();
    renderWithProviders(<Home />);
    expect(screen.getByText("A place to share your knowledge.")).toBeInTheDocument();
    expect(await screen.findByText(article.title)).toBeInTheDocument();
    expect(await screen.findByText("Popular Tags")).toBeInTheDocument();
    expect((await screen.findAllByText("training")).length).toBeGreaterThan(0);
    expect(screen.getByText("Global Feed")).toBeInTheDocument();
  });

  it("article page renders sanitized markdown, meta and comments", async () => {
    mockBackend();
    setRoute("/article/[pid]", { pid: article.slug });
    const unsafeArticle = {
      ...article,
      body: '# Heading\n\n<img src="x" onerror="alert(1)"><script>alert(2)</script>**safe**',
    };
    renderWithProviders(<ArticlePage article={unsafeArticle} />);

    expect(screen.getByRole("heading", { level: 1, name: article.title })).toBeInTheDocument();
    expect(screen.getByRole("heading", { level: 1, name: "Heading" })).toBeInTheDocument();
    expect(screen.getByText("safe")).toBeInTheDocument();
    expect(document.querySelector("script")).toBeNull();
    expect(document.querySelector("[onerror]")).toBeNull();
    expect(await screen.findByText(comment.body)).toBeInTheDocument();
    expect(screen.getAllByText("dragons").length).toBeGreaterThan(0);
  });

  it("article page getInitialProps fetches the article by slug", async () => {
    mockBackend();
    const props = await ArticlePage.getInitialProps({ query: { pid: article.slug } });
    expect(props).toEqual({ article });
    expect(axios.get).toHaveBeenCalledWith(`${SERVER_BASE_URL}/articles/${article.slug}`);
  });

  it("profile page renders the profile, tabs and articles", async () => {
    mockBackend();
    loginAs({ ...currentUser, username: "viewer" });
    setRoute("/profile/[pid]", { pid: author.username }, `/profile/${author.username}`);
    renderWithProviders(<Profile initialProfile={{ profile: author }} />);

    expect(screen.getByRole("heading", { level: 4, name: author.username })).toBeInTheDocument();
    expect(screen.getByText(author.bio)).toBeInTheDocument();
    expect(screen.getByText("My Articles")).toBeInTheDocument();
    expect(await screen.findByText(article.title)).toBeInTheDocument();
    expect(await screen.findByRole("button", { name: /Follow janedoe/ })).toBeInTheDocument();
    expect(screen.queryByText(/Edit Profile Settings/)).not.toBeInTheDocument();
  });

  it("profile page shows the edit button for the owner and loads via getInitialProps", async () => {
    mockBackend();
    loginAs();
    setRoute("/profile/[pid]", { pid: author.username }, `/profile/${author.username}`);
    renderWithProviders(<Profile initialProfile={{ profile: author }} />);
    expect(await screen.findByText(/Edit Profile Settings/)).toBeInTheDocument();

    const props = await Profile.getInitialProps({ query: { pid: author.username } });
    expect(props.initialProfile).toEqual({ profile: author });
  });

  it("editor pages render", async () => {
    mockBackend();
    loginAs();
    const { unmount } = renderWithProviders(<PublishArticleEditor />);
    expect(screen.getByRole("button", { name: "Publish Article" })).toBeInTheDocument();
    unmount();

    setRoute("/editor/[pid]", { pid: article.slug });
    renderWithProviders(<UpdateArticleEditor article={article} />);
    expect(screen.getByDisplayValue(article.title)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Update Article" })).toBeInTheDocument();

    const props = await UpdateArticleEditor.getInitialProps({ query: { pid: article.slug } });
    expect(props).toEqual({ article });
  });

  it("settings page renders the form for a logged-in user", async () => {
    loginAs();
    renderWithProviders(<Settings />);
    expect(screen.getByRole("heading", { name: "Your Settings" })).toBeInTheDocument();
    expect(await screen.findByDisplayValue(currentUser.username)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Or click here to logout." })).toBeInTheDocument();
  });

  it("login and register pages render their forms", async () => {
    const { unmount } = renderWithProviders(<Login />);
    expect(screen.getByRole("heading", { name: "Sign in" })).toBeInTheDocument();
    expect(screen.getByText("Need an account?")).toHaveAttribute("href", "/user/register");
    expect(screen.getByRole("button", { name: "Sign in" })).toBeInTheDocument();
    unmount();

    renderWithProviders(<Register />);
    expect(screen.getByRole("heading", { name: "Sign Up" })).toBeInTheDocument();
    expect(screen.getByText("Have an account?")).toHaveAttribute("href", "/user/login");
    expect(screen.getByRole("button", { name: "Sign up" })).toBeInTheDocument();
    await waitFor(() => expect(screen.getByPlaceholderText("Username")).toBeInTheDocument());
  });
});
