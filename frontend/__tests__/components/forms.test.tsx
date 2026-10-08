import userEvent from "@testing-library/user-event";
import axios from "axios";
import Router from "next/router";
import React from "react";
import { describe, expect, it, vi } from "vitest";

import CommentInput from "../../components/comment/CommentInput";
import TagInput from "../../components/editor/TagInput";
import LoginForm from "../../components/profile/LoginForm";
import RegisterForm from "../../components/profile/RegisterForm";
import SettingsForm from "../../components/profile/SettingsForm";
import { SERVER_BASE_URL } from "../../lib/utils/constant";
import PublishArticleEditor from "../../pages/editor/new";
import UpdateArticleEditor from "../../pages/editor/[pid]";
import { article, currentUser, loginAs, okResponse } from "../../test/fixtures";
import { setRoute } from "../../test/mockRouter";
import { renderWithProviders, screen, waitFor } from "../../test/utils";

const rejectWith = (status: number, data: unknown) =>
  Object.assign(new Error("Request failed"), { isAxiosError: true, response: { status, data } });

describe("LoginForm", () => {
  it("binds fields, submits and stores the session", async () => {
    const user = userEvent.setup();
    vi.mocked(axios.post).mockResolvedValue(okResponse({ user: currentUser }));
    renderWithProviders(<LoginForm />);

    await user.type(screen.getByPlaceholderText("Email"), "jane@example.com");
    await user.type(screen.getByPlaceholderText("Password"), "secret");
    expect(screen.getByPlaceholderText("Email")).toHaveValue("jane@example.com");
    expect(screen.getByPlaceholderText("Password")).toHaveValue("secret");

    await user.click(screen.getByRole("button", { name: "Sign in" }));

    await waitFor(() => expect(Router.push).toHaveBeenCalledWith("/"));
    expect(axios.post).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/users/login`,
      JSON.stringify({ user: { email: "jane@example.com", password: "secret" } }),
      expect.anything()
    );
    expect(JSON.parse(window.localStorage.getItem("user") ?? "{}")).toEqual(currentUser);
  });

  it("shows a backend message when the response has no errors map", async () => {
    const user = userEvent.setup();
    vi.mocked(axios.post).mockRejectedValue(
      rejectWith(422, { message: "invalid email or password" })
    );
    renderWithProviders(<LoginForm />);
    await user.type(screen.getByPlaceholderText("Email"), "x@y.z");
    await user.type(screen.getByPlaceholderText("Password"), "wrong");
    await user.click(screen.getByRole("button", { name: "Sign in" }));

    const item = await screen.findByText("invalid email or password");
    expect(item).toBeInTheDocument();
    expect(item.textContent?.trim()).toBe("invalid email or password");
    expect(Router.push).not.toHaveBeenCalled();
  });

  it("shows API errors", async () => {
    const user = userEvent.setup();
    vi.mocked(axios.post).mockRejectedValue(
      rejectWith(422, { errors: { "email or password": ["is invalid"] } })
    );
    renderWithProviders(<LoginForm />);
    await user.type(screen.getByPlaceholderText("Email"), "x@y.z");
    await user.type(screen.getByPlaceholderText("Password"), "nope");
    await user.click(screen.getByRole("button", { name: "Sign in" }));

    expect(await screen.findByText("email or password is invalid")).toBeInTheDocument();
    expect(Router.push).not.toHaveBeenCalled();
    expect(window.localStorage.getItem("user")).toBeNull();
    expect(screen.getByRole("button", { name: "Sign in" })).toBeEnabled();
  });

  it("logs unexpected failures without crashing", async () => {
    const user = userEvent.setup();
    const consoleError = vi.spyOn(console, "error").mockImplementation(() => undefined);
    vi.mocked(axios.post).mockRejectedValue(new Error("Network Error"));
    renderWithProviders(<LoginForm />);
    await user.click(screen.getByRole("button", { name: "Sign in" }));
    await waitFor(() => expect(consoleError).toHaveBeenCalled());
    expect(screen.getByRole("button", { name: "Sign in" })).toBeEnabled();
  });
});

describe("RegisterForm", () => {
  it("binds fields and registers", async () => {
    const user = userEvent.setup();
    vi.mocked(axios.post).mockResolvedValue(okResponse({ user: currentUser }, 201));
    renderWithProviders(<RegisterForm />);

    await user.type(screen.getByPlaceholderText("Username"), "janedoe");
    await user.type(screen.getByPlaceholderText("Email"), "jane@example.com");
    await user.type(screen.getByPlaceholderText("Password"), "secret");
    await user.click(screen.getByRole("button", { name: "Sign up" }));

    await waitFor(() => expect(Router.push).toHaveBeenCalledWith("/"));
    expect(axios.post).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/users`,
      JSON.stringify({
        user: { username: "janedoe", email: "jane@example.com", password: "secret" },
      }),
      expect.anything()
    );
    expect(window.localStorage.getItem("user")).toContain(currentUser.token);
  });

  it("renders validation errors", async () => {
    const user = userEvent.setup();
    vi.mocked(axios.post).mockRejectedValue(
      rejectWith(422, { errors: { username: ["has already been taken", "is too short"] } })
    );
    renderWithProviders(<RegisterForm />);
    await user.click(screen.getByRole("button", { name: "Sign up" }));
    expect(
      await screen.findByText("username has already been taken, is too short")
    ).toBeInTheDocument();
    expect(Router.push).not.toHaveBeenCalled();
  });
});

describe("SettingsForm", () => {
  it("prefills from the current user and submits an update", async () => {
    const user = userEvent.setup();
    loginAs();
    const updated = { ...currentUser, bio: "Updated bio" };
    vi.mocked(axios.put).mockResolvedValue(okResponse({ user: updated }));
    renderWithProviders(<SettingsForm />);

    expect(await screen.findByDisplayValue(currentUser.username)).toBeInTheDocument();
    expect(screen.getByDisplayValue(currentUser.email)).toBeInTheDocument();
    expect(screen.getByDisplayValue(currentUser.image)).toBeInTheDocument();

    const bio = screen.getByPlaceholderText("Short bio about you");
    await user.clear(bio);
    await user.type(bio, "Updated bio");
    await user.click(screen.getByRole("button", { name: "Update Settings" }));

    await waitFor(() => expect(Router.push).toHaveBeenCalledWith("/"));
    const [url, body, config] = vi.mocked(axios.put).mock.calls[0];
    expect(url).toBe(`${SERVER_BASE_URL}/user`);
    const parsed = JSON.parse(body as string);
    expect(parsed.user.bio).toBe("Updated bio");
    expect(parsed.user.username).toBe(currentUser.username);
    expect(parsed.user).not.toHaveProperty("password");
    expect(config?.headers).toMatchObject({ Authorization: `Token ${currentUser.token}` });
    expect(JSON.parse(window.localStorage.getItem("user") ?? "{}").bio).toBe("Updated bio");
  });

  it("includes a new password only when provided and shows errors", async () => {
    const user = userEvent.setup();
    loginAs();
    vi.mocked(axios.put).mockResolvedValue(
      okResponse({ errors: { password: ["is too short"] } }, 422)
    );
    renderWithProviders(<SettingsForm />);
    await screen.findByDisplayValue(currentUser.username);
    await user.type(screen.getByPlaceholderText("New Password"), "123");
    await user.click(screen.getByRole("button", { name: "Update Settings" }));

    expect(await screen.findByText("password is too short")).toBeInTheDocument();
    const body = JSON.parse(vi.mocked(axios.put).mock.calls[0][1] as string);
    expect(body.user.password).toBe("123");
    expect(Router.push).not.toHaveBeenCalled();
  });
});

describe("CommentInput", () => {
  it("asks anonymous users to sign in", () => {
    setRoute("/article/[pid]", { pid: article.slug });
    renderWithProviders(<CommentInput />);
    expect(screen.getByText("Sign in")).toHaveAttribute("href", "/user/login");
    expect(screen.getByText("sign up")).toHaveAttribute("href", "/user/register");
    expect(screen.queryByPlaceholderText("Write a comment...")).not.toBeInTheDocument();
  });

  it("posts a comment with the auth header and clears the field", async () => {
    const user = userEvent.setup();
    loginAs();
    setRoute("/article/[pid]", { pid: article.slug });
    vi.mocked(axios.post).mockResolvedValue(okResponse({ comment: {} }, 201));
    renderWithProviders(<CommentInput />);

    const textarea = await screen.findByPlaceholderText("Write a comment...");
    await user.type(textarea, "Nice one");
    expect(textarea).toHaveValue("Nice one");
    await user.click(screen.getByRole("button", { name: "Post Comment" }));

    await waitFor(() => expect(textarea).toHaveValue(""));
    expect(axios.post).toHaveBeenCalledWith(
      `${SERVER_BASE_URL}/articles/${article.slug}/comments`,
      JSON.stringify({ comment: { body: "Nice one" } }),
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Token ${encodeURIComponent(currentUser.token)}`,
        },
      }
    );
  });
});

describe("TagInput", () => {
  it("adds tags on Enter / comma / blur and removes them", async () => {
    const user = userEvent.setup();
    const addTag = vi.fn();
    const removeTag = vi.fn();
    renderWithProviders(
      <TagInput tagList={["react", "next"]} addTag={addTag} removeTag={removeTag} />
    );
    const input = screen.getByPlaceholderText("Enter tags");

    await user.type(input, "vitest{Enter}");
    expect(addTag).toHaveBeenCalledWith("vitest");
    expect(input).toHaveValue("");

    await user.type(input, "swr,");
    expect(addTag).toHaveBeenCalledWith("swr");

    await user.type(input, "blurred");
    await user.tab();
    expect(addTag).toHaveBeenCalledWith("blurred");

    await user.click(screen.getByRole("button", { name: "Remove tag react" }));
    expect(removeTag).toHaveBeenCalledWith("react");
    expect(addTag).toHaveBeenCalledTimes(3);
  });
});

describe("Article editor", () => {
  it("publishes a new article from the form state", async () => {
    const user = userEvent.setup();
    loginAs();
    vi.mocked(axios.post).mockResolvedValue(okResponse({ article }, 201));
    renderWithProviders(<PublishArticleEditor />);

    await user.type(screen.getByPlaceholderText("Article Title"), "My title");
    await user.type(screen.getByPlaceholderText("What's this article about?"), "About");
    await user.type(screen.getByPlaceholderText("Write your article (in markdown)"), "Body");
    await user.type(screen.getByPlaceholderText("Enter tags"), "tag1{Enter}");
    expect(screen.getByText("tag1")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Publish Article" }));
    await waitFor(() => expect(Router.push).toHaveBeenCalledWith("/"));

    const [url, body, config] = vi.mocked(axios.post).mock.calls[0];
    expect(url).toBe(`${SERVER_BASE_URL}/articles`);
    expect(JSON.parse(body as string)).toEqual({
      article: { title: "My title", description: "About", body: "Body", tagList: ["tag1"] },
    });
    expect(config?.headers).toMatchObject({
      Authorization: `Token ${encodeURIComponent(currentUser.token)}`,
    });
  });

  it("shows server-side validation errors on publish", async () => {
    const user = userEvent.setup();
    loginAs();
    vi.mocked(axios.post).mockResolvedValue(
      okResponse({ errors: { title: ["can't be blank"] } }, 422)
    );
    renderWithProviders(<PublishArticleEditor />);
    await user.click(screen.getByRole("button", { name: "Publish Article" }));
    expect(await screen.findByText("title can't be blank")).toBeInTheDocument();
    expect(Router.push).not.toHaveBeenCalled();
  });

  it("prefills and updates an existing article", async () => {
    const user = userEvent.setup();
    loginAs();
    setRoute("/editor/[pid]", { pid: article.slug });
    vi.mocked(axios.put).mockResolvedValue(okResponse({ article }));
    renderWithProviders(<UpdateArticleEditor article={article} />);

    expect(screen.getByDisplayValue(article.title)).toBeInTheDocument();
    expect(screen.getByDisplayValue(article.description)).toBeInTheDocument();
    expect(screen.getByText("dragons")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Remove tag dragons" }));
    expect(screen.queryByText("dragons")).not.toBeInTheDocument();

    const title = screen.getByPlaceholderText("Article Title");
    await user.clear(title);
    await user.type(title, "New title");
    await user.click(screen.getByRole("button", { name: "Update Article" }));

    await waitFor(() => expect(Router.push).toHaveBeenCalledWith("/"));
    const [url, body] = vi.mocked(axios.put).mock.calls[0];
    expect(url).toBe(`${SERVER_BASE_URL}/articles/${article.slug}`);
    expect(JSON.parse(body as string).article).toMatchObject({
      title: "New title",
      tagList: ["training"],
    });
  });

  it("shows errors when the update is rejected", async () => {
    const user = userEvent.setup();
    loginAs();
    setRoute("/editor/[pid]", { pid: article.slug });
    vi.mocked(axios.put).mockResolvedValue(okResponse({ errors: { body: ["can't be blank"] } }, 422));
    renderWithProviders(<UpdateArticleEditor article={article} />);
    await user.click(screen.getByRole("button", { name: "Update Article" }));
    expect(await screen.findByText("body can't be blank")).toBeInTheDocument();
  });
});
