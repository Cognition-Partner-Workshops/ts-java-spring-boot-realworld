import userEvent from "@testing-library/user-event";
import axios from "axios";
import Router from "next/router";
import React from "react";
import { describe, expect, it, vi } from "vitest";

import LoginForm from "../../components/profile/LoginForm";
import RegisterForm from "../../components/profile/RegisterForm";
import SettingsForm from "../../components/profile/SettingsForm";
import { SERVER_BASE_URL } from "../../lib/utils/constant";
import { currentUser, loginAs, okResponse } from "../../test/fixtures";
import { renderWithProviders, screen, waitFor } from "../../test/utils";

const rejectWith = (status: number, data: unknown) =>
  Object.assign(new Error("Request failed"), {
    isAxiosError: true,
    response: { status, data },
  });

describe("regression: RegisterForm error handling", () => {
  it("renders duplicate email/username field errors from the backend envelope", async () => {
    const user = userEvent.setup();
    vi.mocked(axios.post).mockRejectedValue(
      rejectWith(422, {
        errors: { email: ["duplicated email"], username: ["duplicated username"] },
      })
    );
    renderWithProviders(<RegisterForm />);
    await user.type(screen.getByPlaceholderText("Username"), "johndoe");
    await user.type(screen.getByPlaceholderText("Email"), "john@example.com");
    await user.type(screen.getByPlaceholderText("Password"), "password123");
    await user.click(screen.getByRole("button", { name: "Sign up" }));

    expect(await screen.findByText("email duplicated email")).toBeInTheDocument();
    expect(screen.getByText("username duplicated username")).toBeInTheDocument();
    expect(Router.push).not.toHaveBeenCalled();
    expect(window.localStorage.getItem("user")).toBeNull();
    expect(axios.post).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/users`,
      JSON.stringify({
        user: { username: "johndoe", email: "john@example.com", password: "password123" },
      }),
      expect.objectContaining({ headers: { "Content-Type": "application/json" } })
    );
  });

  it("falls back to a bare message and re-enables the button", async () => {
    const user = userEvent.setup();
    vi.mocked(axios.post).mockRejectedValue(rejectWith(500, { message: "boom" }));
    renderWithProviders(<RegisterForm />);
    await user.click(screen.getByRole("button", { name: "Sign up" }));
    expect(await screen.findByText("boom")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Sign up" })).toBeEnabled();
  });

  it("logs network failures without crashing", async () => {
    const user = userEvent.setup();
    const consoleError = vi.spyOn(console, "error").mockImplementation(() => undefined);
    vi.mocked(axios.post).mockRejectedValue(new Error("Network Error"));
    renderWithProviders(<RegisterForm />);
    await user.click(screen.getByRole("button", { name: "Sign up" }));
    await waitFor(() => expect(consoleError).toHaveBeenCalled());
    expect(screen.getByRole("button", { name: "Sign up" })).toBeEnabled();
    expect(Router.push).not.toHaveBeenCalled();
  });
});

describe("regression: LoginForm", () => {
  it("renders an empty error list when the response carries no error details", async () => {
    const user = userEvent.setup();
    vi.mocked(axios.post).mockRejectedValue(rejectWith(422, {}));
    renderWithProviders(<LoginForm />);
    await user.type(screen.getByPlaceholderText("Email"), "x@y.z");
    await user.type(screen.getByPlaceholderText("Password"), "nope");
    await user.click(screen.getByRole("button", { name: "Sign in" }));
    await waitFor(() => expect(screen.getByRole("button", { name: "Sign in" })).toBeEnabled());
    expect(document.querySelectorAll(".error-messages li")).toHaveLength(0);
    expect(Router.push).not.toHaveBeenCalled();
  });

  it("does not store a session when the backend answers 200 without a user", async () => {
    const user = userEvent.setup();
    vi.mocked(axios.post).mockResolvedValue(okResponse({}));
    renderWithProviders(<LoginForm />);
    await user.click(screen.getByRole("button", { name: "Sign in" }));
    await waitFor(() => expect(screen.getByRole("button", { name: "Sign in" })).toBeEnabled());
    expect(window.localStorage.getItem("user")).toBeNull();
    expect(Router.push).not.toHaveBeenCalled();
  });
});

describe("regression: SettingsForm", () => {
  it("stays blank for anonymous visitors", async () => {
    renderWithProviders(<SettingsForm />);
    await waitFor(() => expect(screen.getByPlaceholderText("Username")).toHaveValue(""));
    expect(screen.getByPlaceholderText("Email")).toHaveValue("");
  });

  it("prefills nullable bio/image as empty strings", async () => {
    loginAs({ ...currentUser, bio: null, image: null });
    renderWithProviders(<SettingsForm />);
    expect(await screen.findByDisplayValue(currentUser.username)).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Short bio about you")).toHaveValue("");
    expect(screen.getByPlaceholderText("URL of profile picture")).toHaveValue("");
  });

  it("shows errors.body when the backend nests them and keeps the session", async () => {
    const user = userEvent.setup();
    loginAs();
    vi.mocked(axios.put).mockResolvedValue({
      ...okResponse({ errors: { body: { username: ["can't be blank"] } } }, 422),
    });
    renderWithProviders(<SettingsForm />);
    expect(await screen.findByDisplayValue(currentUser.username)).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Update Settings" }));
    expect(await screen.findByText("username can't be blank")).toBeInTheDocument();
    expect(JSON.parse(window.localStorage.getItem("user") ?? "{}")).toEqual(currentUser);
    expect(Router.push).not.toHaveBeenCalled();
  });

  it("sends the token and the edited fields, then stores the updated user", async () => {
    const user = userEvent.setup();
    loginAs();
    const updated = { ...currentUser, bio: "new bio" };
    vi.mocked(axios.put).mockResolvedValue(okResponse({ user: updated }));
    renderWithProviders(<SettingsForm />);
    expect(await screen.findByDisplayValue(currentUser.username)).toBeInTheDocument();
    const bio = screen.getByPlaceholderText("Short bio about you");
    await user.clear(bio);
    await user.type(bio, "new bio");
    await user.click(screen.getByRole("button", { name: "Update Settings" }));

    await waitFor(() => expect(Router.push).toHaveBeenCalledWith("/"));
    const [url, body, config] = vi.mocked(axios.put).mock.calls[0];
    expect(url).toBe(`${SERVER_BASE_URL}/user`);
    const sent = JSON.parse(body as string).user;
    expect(sent.bio).toBe("new bio");
    expect(sent).not.toHaveProperty("password");
    expect((config as { headers: Record<string, string> }).headers.Authorization).toBe(
      `Token ${currentUser.token}`
    );
    expect(JSON.parse(window.localStorage.getItem("user") ?? "{}")).toEqual(updated);
  });
});
