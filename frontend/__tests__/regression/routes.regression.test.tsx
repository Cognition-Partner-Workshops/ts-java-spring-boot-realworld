import type { AppProps } from "next/app";
import axios from "axios";
import React from "react";
import { describe, expect, it, vi } from "vitest";

import MyApp from "../../pages/_app";
import Home from "../../pages/index";
import Login from "../../pages/user/login";
import Register from "../../pages/user/register";
import { article, currentUser, loginAs, okResponse } from "../../test/fixtures";
import { renderWithProviders, screen } from "../../test/utils";

const appProps = (Component: React.ComponentType, pageProps = {}) =>
  ({ Component, pageProps, router: {} } as unknown as AppProps);

describe("regression: route smoke through _app", () => {
  it("wraps pages in the layout with navbar and footer for guests", async () => {
    vi.mocked(axios.get).mockImplementation(async (url: string) => {
      if (url.endsWith("/tags")) return okResponse({ tags: ["java"] });
      return okResponse({ articles: [article], articlesCount: 1 });
    });
    renderWithProviders(<MyApp {...appProps(Home)} />);
    expect(await screen.findByText(article.title)).toBeInTheDocument();
    expect(screen.getAllByText("conduit").length).toBeGreaterThan(0);
    expect(screen.getByText("Sign in")).toBeInTheDocument();
    expect(screen.getByText("Sign up")).toBeInTheDocument();
    expect(screen.queryByText(/New Post/)).not.toBeInTheDocument();
    expect(screen.getByText("Global Feed")).toBeInTheDocument();
    expect(screen.queryByText("Your Feed")).not.toBeInTheDocument();
  });

  it("shows the authenticated navbar and personal feed tab for logged-in users", async () => {
    loginAs();
    vi.mocked(axios.get).mockResolvedValue(okResponse({ articles: [], articlesCount: 0, tags: [] }));
    renderWithProviders(<MyApp {...appProps(Home)} />);
    expect(await screen.findByText(/New Post/)).toBeInTheDocument();
    expect(screen.getByText(/Settings/)).toBeInTheDocument();
    expect(screen.getByText(currentUser.username)).toBeInTheDocument();
    expect(await screen.findByText("Your Feed")).toBeInTheDocument();
  });

  it("login and register routes render headings and cross-links", () => {
    const { unmount } = renderWithProviders(<MyApp {...appProps(Login)} />);
    expect(screen.getByRole("heading", { level: 1, name: "Sign in" })).toBeInTheDocument();
    expect(screen.getByText("Need an account?").closest("a")).toHaveAttribute("href", "/user/register");
    unmount();
    renderWithProviders(<MyApp {...appProps(Register)} />);
    expect(screen.getByRole("heading", { level: 1, name: "Sign Up" })).toBeInTheDocument();
    expect(screen.getByText("Have an account?").closest("a")).toHaveAttribute("href", "/user/login");
  });
});
