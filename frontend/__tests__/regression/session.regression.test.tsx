import userEvent from "@testing-library/user-event";
import axios from "axios";
import Router from "next/router";
import React from "react";
import useSWR from "swr";
import { describe, expect, it, vi } from "vitest";

import ArticlePreview from "../../components/article/ArticlePreview";
import ProfileTab from "../../components/profile/ProfileTab";
import { usePageState } from "../../lib/context/PageContext";
import { SERVER_BASE_URL } from "../../lib/utils/constant";
import fetcher from "../../lib/utils/fetcher";
import storage from "../../lib/utils/storage";
import Profile from "../../pages/profile/[pid]";
import { article, author, currentUser, loginAs, okResponse } from "../../test/fixtures";
import { setRoute } from "../../test/mockRouter";
import { renderWithProviders, screen, waitFor } from "../../test/utils";

const PageEcho = () => <output data-testid="page">{usePageState()}</output>;

describe("regression: fetcher auth header", () => {
  it("adds the stored token, and omits it when the stored user has no token", async () => {
    vi.mocked(axios.get).mockResolvedValue(okResponse({ ok: true }));
    loginAs();
    await fetcher("http://api/x");
    expect(axios.get).toHaveBeenLastCalledWith("http://api/x", {
      headers: { Authorization: `Token ${currentUser.token}` },
    });
    window.localStorage.setItem("user", JSON.stringify({ username: "no-token" }));
    await fetcher("http://api/y");
    expect(axios.get).toHaveBeenLastCalledWith("http://api/y", {});
  });
});

describe("regression: session-aware article preview", () => {
  it("favoriting as a logged-in user sends the token and unfavoriting deletes", async () => {
    const user = userEvent.setup();
    loginAs();
    vi.mocked(axios.post).mockResolvedValue(okResponse({}));
    vi.mocked(axios.delete).mockResolvedValue(okResponse({}));
    renderWithProviders(<ArticlePreview article={{ ...article, favorited: true }} />);
    const button = await screen.findByRole("button");
    await waitFor(() => expect(button).toHaveClass("btn-primary"));
    await user.click(button);
    expect(await screen.findByText("2")).toBeInTheDocument();
    expect(axios.delete).toHaveBeenCalledWith(`${SERVER_BASE_URL}/articles/${article.slug}/favorite`, {
      headers: { Authorization: `Token ${currentUser.token}` },
    });
    await user.click(screen.getByRole("button"));
    expect(await screen.findByText("3")).toBeInTheDocument();
    expect(axios.post).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/articles/${article.slug}/favorite`,
      {},
      { headers: { Authorization: `Token ${currentUser.token}` } }
    );
    expect(Router.push).not.toHaveBeenCalled();
  });

  it("tag and author links reset the page offset and tags highlight on hover", async () => {
    const user = userEvent.setup();
    window.sessionStorage.setItem("offset", "5");
    renderWithProviders(
      <>
        <ArticlePreview article={article} />
        <PageEcho />
      </>
    );
    expect(screen.getByTestId("page")).toHaveTextContent("5");
    const tag = screen.getByText("dragons");
    expect(tag.closest("a")).toHaveAttribute("href", "/?tag=dragons");
    await user.hover(tag);
    expect(tag.closest("li")?.style.borderColor).toBe("rgb(92, 184, 92)");
    await user.unhover(tag);
    expect(tag.closest("li")?.style.borderColor).toBe("initial");
    await user.click(tag);
    expect(screen.getByTestId("page")).toHaveTextContent("0");

    window.sessionStorage.setItem("offset", "5");
    await user.click(screen.getByText(author.username));
    expect(window.sessionStorage.getItem("offset")).toBe("0");
    expect(screen.getByText(new Date(article.createdAt).toDateString())).toBeInTheDocument();
  });
});

describe("regression: profile page follow state", () => {
  const mockBackend = (following: { value: boolean }) => {
    vi.mocked(axios.get).mockImplementation(async (url: string) => {
      if (url.includes("/profiles/"))
        return okResponse({ profile: { ...author, following: following.value } });
      return okResponse({ articles: [], articlesCount: 0 });
    });
  };

  it("lets another logged-in user follow and unfollow the author", async () => {
    const user = userEvent.setup();
    const following = { value: false };
    mockBackend(following);
    loginAs({ ...currentUser, username: "bobsmith" });
    vi.mocked(axios.post).mockImplementation(async () => {
      following.value = true;
      return okResponse({ profile: { ...author, following: true } });
    });
    vi.mocked(axios.delete).mockImplementation(async () => {
      following.value = false;
      return okResponse({ profile: { ...author, following: false } });
    });
    setRoute("/profile/[pid]", { pid: author.username });
    renderWithProviders(<Profile initialProfile={{ profile: author }} />);

    await user.click(await screen.findByRole("button", { name: `Follow ${author.username}` }));
    expect(await screen.findByRole("button", { name: `Unfollow ${author.username}` })).toBeInTheDocument();
    expect(axios.post).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/profiles/${author.username}/follow`,
      {},
      expect.anything()
    );
    await user.click(screen.getByRole("button", { name: `Unfollow ${author.username}` }));
    expect(await screen.findByRole("button", { name: `Follow ${author.username}` })).toBeInTheDocument();
    expect(axios.delete).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/profiles/${author.username}/follow`,
      expect.anything()
    );
    expect(screen.queryByText(/Edit Profile Settings/)).not.toBeInTheDocument();
  });

  it("hides follow controls for anonymous visitors and shows the bio", async () => {
    mockBackend({ value: false });
    setRoute("/profile/[pid]", { pid: author.username });
    renderWithProviders(<Profile initialProfile={{ profile: author }} />);
    expect(await screen.findByRole("heading", { level: 4, name: author.username })).toBeInTheDocument();
    expect(screen.getByText(author.bio)).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /Follow/ })).not.toBeInTheDocument();
  });

  it("shows an error state when the profile cannot be loaded", async () => {
    vi.mocked(axios.get).mockRejectedValue(new Error("boom"));
    setRoute("/profile/[pid]", { pid: "ghost" });
    renderWithProviders(<Profile initialProfile={{ profile: author }} />);
    expect(await screen.findByText("Can't load profile")).toBeInTheDocument();
  });

  it("ProfileTab resets the page offset when switching tabs", async () => {
    const user = userEvent.setup();
    window.sessionStorage.setItem("offset", "2");
    setRoute("/profile/[pid]", { pid: "jane doe" }, "/profile/jane%20doe");
    renderWithProviders(
      <>
        <ProfileTab profile={{ username: "jane doe" }} />
        <PageEcho />
      </>
    );
    expect(screen.getByText("My Articles").closest("a")).toHaveAttribute("href", "/profile/jane%20doe");
    expect(screen.getByText("Favorited Articles").closest("a")).toHaveAttribute(
      "href",
      "/profile/jane%20doe?favorite=true"
    );
    await user.click(screen.getByText("Favorited Articles"));
    expect(screen.getByTestId("page")).toHaveTextContent("0");
  });
});

describe("regression: storage helper", () => {
  it("resolves the stored user through SWR and tolerates removal", async () => {
    loginAs();
    const Probe = () => {
      const { data } = useSWR("user", storage);
      return <output>{data ? data.username : "none"}</output>;
    };
    renderWithProviders(<Probe />);
    expect(await screen.findByText(currentUser.username)).toBeInTheDocument();
    expect(await storage("missing")).toBeUndefined();
  });
});
