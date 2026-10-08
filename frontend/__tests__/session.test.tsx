import userEvent from "@testing-library/user-event";
import axios from "axios";
import Router from "next/router";
import React from "react";
import useSWR from "swr";
import { describe, expect, it, vi } from "vitest";

import LoginForm from "../components/profile/LoginForm";
import Navbar from "../components/common/Navbar";
import checkLogin from "../lib/utils/checkLogin";
import storage from "../lib/utils/storage";
import Settings from "../pages/user/settings";
import { currentUser, loginAs, okResponse } from "../test/fixtures";
import { renderWithProviders, screen, waitFor } from "../test/utils";

const SessionProbe = () => {
  const { data } = useSWR("user", storage);
  return <output data-testid="session">{checkLogin(data) ? data.username : "anonymous"}</output>;
};

describe("login / session behaviour", () => {
  it("useSWR('user') resolves the stored user from localStorage", async () => {
    loginAs();
    renderWithProviders(<SessionProbe />);
    expect(await screen.findByText(currentUser.username)).toBeInTheDocument();
  });

  it("reports anonymous when nothing is stored", async () => {
    renderWithProviders(<SessionProbe />);
    expect(await screen.findByText("anonymous")).toBeInTheDocument();
  });

  it("logging in stores the token and updates every subscriber", async () => {
    const user = userEvent.setup();
    vi.mocked(axios.post).mockResolvedValue(okResponse({ user: currentUser }));
    renderWithProviders(
      <>
        <Navbar />
        <LoginForm />
        <SessionProbe />
      </>
    );
    expect(await screen.findByText("anonymous")).toBeInTheDocument();

    await user.type(screen.getByPlaceholderText("Email"), currentUser.email);
    await user.type(screen.getByPlaceholderText("Password"), "secret");
    await user.click(screen.getByRole("button", { name: "Sign in" }));

    expect(await screen.findByTestId("session")).toHaveTextContent(currentUser.username);
    expect(JSON.parse(window.localStorage.getItem("user") ?? "{}").token).toBe(currentUser.token);
    expect(await screen.findByText("New Post")).toBeInTheDocument();
    expect(Router.push).toHaveBeenCalledWith("/");
  });

  it("logging out clears storage, the SWR cache and redirects home", async () => {
    const user = userEvent.setup();
    loginAs();
    renderWithProviders(
      <>
        <Settings />
        <SessionProbe />
      </>
    );
    expect(await screen.findByText(currentUser.username)).toBeInTheDocument();
    expect(await screen.findByDisplayValue(currentUser.email)).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Or click here to logout." }));

    await waitFor(() => expect(window.localStorage.getItem("user")).toBeNull());
    expect(await screen.findByText("anonymous")).toBeInTheDocument();
    expect(Router.push).toHaveBeenCalledWith("/");
  });

  it("the settings page redirects anonymous visitors home", async () => {
    renderWithProviders(<Settings />);
    await waitFor(() => expect(Router.push).toHaveBeenCalledWith("/"));
  });
});
