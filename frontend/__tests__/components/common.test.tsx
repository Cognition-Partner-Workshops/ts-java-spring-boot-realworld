import userEvent from "@testing-library/user-event";
import axios from "axios";
import React from "react";
import { describe, expect, it, vi } from "vitest";

import CustomImage from "../../components/common/CustomImage";
import CustomLink from "../../components/common/CustomLink";
import ErrorMessage from "../../components/common/ErrorMessage";
import Footer from "../../components/common/Footer";
import Layout from "../../components/common/Layout";
import ListErrors from "../../components/common/ListErrors";
import LoadingSpinner from "../../components/common/LoadingSpinner";
import Maybe from "../../components/common/Maybe";
import NavLink from "../../components/common/NavLink";
import Navbar from "../../components/common/Navbar";
import Banner from "../../components/home/Banner";
import TabList from "../../components/home/TabList";
import Tags from "../../components/home/Tags";
import EditProfileButton from "../../components/profile/EditProfileButton";
import FollowUserButton from "../../components/profile/FollowUserButton";
import ProfileTab from "../../components/profile/ProfileTab";
import { usePageState } from "../../lib/context/PageContext";
import { DEFAULT_PROFILE_IMAGE } from "../../lib/utils/constant";
import { currentUser, loginAs, okResponse } from "../../test/fixtures";
import { setRoute } from "../../test/mockRouter";
import { fireEvent, renderWithProviders, screen } from "../../test/utils";

const PageProbe = () => <output data-testid="page">{usePageState()}</output>;

describe("CustomLink / NavLink", () => {
  it("renders a single anchor without nesting", () => {
    renderWithProviders(
      <CustomLink href="/article/[pid]" as="/article/slug" className="preview-link">
        <h1>Title</h1>
      </CustomLink>
    );
    const link = screen.getByRole("link");
    expect(link).toHaveAttribute("href", "/article/slug");
    expect(link).toHaveClass("preview-link");
    expect(link.querySelectorAll("a")).toHaveLength(0);
  });

  it("marks the active nav link based on the current path", () => {
    setRoute("/user/login", {}, "/user/login");
    renderWithProviders(
      <>
        <NavLink href="/user/login">Sign in</NavLink>
        <NavLink href="/user/register">Sign up</NavLink>
      </>
    );
    expect(screen.getByText("Sign in")).toHaveClass("nav-link", "active");
    expect(screen.getByText("Sign up")).toHaveClass("nav-link");
    expect(screen.getByText("Sign up")).not.toHaveClass("active");
  });
});

describe("small presentational components", () => {
  it("Maybe renders children only when the test is truthy", () => {
    renderWithProviders(
      <>
        <Maybe test={true}>
          <span>yes</span>
        </Maybe>
        <Maybe test={0}>
          <span>no</span>
        </Maybe>
      </>
    );
    expect(screen.getByText("yes")).toBeInTheDocument();
    expect(screen.queryByText("no")).not.toBeInTheDocument();
  });

  it("ListErrors handles maps, arrays and empty input", () => {
    const { rerender } = renderWithProviders(
      <ListErrors errors={{ email: ["is invalid", "is taken"], body: "can't be blank" }} />
    );
    expect(screen.getByText("email is invalid, is taken")).toBeInTheDocument();
    expect(screen.getByText("body can't be blank")).toBeInTheDocument();
    rerender(<ListErrors errors={null} />);
    expect(screen.queryAllByRole("listitem")).toHaveLength(0);
  });

  it("ErrorMessage, LoadingSpinner, Footer and Banner render", () => {
    renderWithProviders(
      <>
        <ErrorMessage message="Something broke" />
        <LoadingSpinner />
        <Footer />
        <Banner />
      </>
    );
    expect(screen.getByText("Something broke")).toBeInTheDocument();
    expect(document.querySelector(".loading-spinner")).toBeInTheDocument();
    expect(screen.getByText("conduit", { selector: "a" })).toHaveAttribute("href", "/");
    expect(screen.getByText("A place to share your knowledge.")).toBeInTheDocument();
  });

  it("CustomImage lazy-loads and falls back to the default avatar on error", () => {
    renderWithProviders(<CustomImage src={undefined} alt="avatar" className="user-img" />);
    const img = screen.getByAltText("avatar");
    expect(img).toHaveClass("user-img", "lazyload");
    expect(img).toHaveAttribute("data-src", DEFAULT_PROFILE_IMAGE);
    fireEvent.error(img);
    expect(img).toHaveAttribute("src", DEFAULT_PROFILE_IMAGE);
  });
});

describe("Navbar", () => {
  it("shows guest links when logged out", async () => {
    renderWithProviders(<Navbar />);
    expect(await screen.findByText("Sign in")).toHaveAttribute("href", "/user/login");
    expect(screen.getByText("Sign up")).toHaveAttribute("href", "/user/register");
    expect(screen.queryByText("New Post")).not.toBeInTheDocument();
  });

  it("shows user links when logged in and resets the page on Home click", async () => {
    const user = userEvent.setup();
    loginAs();
    window.sessionStorage.setItem("offset", "4");
    renderWithProviders(
      <>
        <Navbar />
        <PageProbe />
      </>
    );
    expect(await screen.findByText(currentUser.username)).toBeInTheDocument();
    expect(screen.getByText("New Post").closest("a")).toHaveAttribute("href", "/editor/new");
    expect(screen.getByText("Settings").closest("a")).toHaveAttribute("href", "/user/settings");
    expect(screen.queryByText("Sign in")).not.toBeInTheDocument();

    expect(screen.getByTestId("page")).toHaveTextContent("4");
    await user.click(screen.getByText("Home"));
    expect(screen.getByTestId("page")).toHaveTextContent("0");
  });

  it("Layout wraps children between the navbar and footer", async () => {
    renderWithProviders(
      <Layout>
        <main>content</main>
      </Layout>
    );
    expect(screen.getByText("content")).toBeInTheDocument();
    expect(await screen.findByText("Sign in")).toBeInTheDocument();
    expect(document.querySelector("footer")).toBeInTheDocument();
  });
});

describe("TabList", () => {
  it("shows only the global feed for guests, plus the active tag", async () => {
    setRoute("/", { tag: "dragons" }, "/?tag=dragons");
    renderWithProviders(<TabList />);
    expect(await screen.findByText("Global Feed")).toBeInTheDocument();
    expect(screen.queryByText("Your Feed")).not.toBeInTheDocument();
    expect(screen.getByText("dragons").closest("a")).toHaveClass("nav-link", "active");
  });

  it("shows the personal feed for logged-in users", async () => {
    loginAs();
    renderWithProviders(<TabList />);
    expect(await screen.findByText("Your Feed")).toHaveAttribute(
      "href",
      `/?follow=${currentUser.username}`
    );
    expect(screen.getByText("Global Feed")).toBeInTheDocument();
  });
});

describe("Tags", () => {
  it("renders popular tags from the API", async () => {
    vi.mocked(axios.get).mockResolvedValue(okResponse({ tags: ["react", "swr"] }));
    renderWithProviders(<Tags />);
    expect(await screen.findByText("react")).toBeInTheDocument();
    expect(screen.getByText("swr").closest("a")).toHaveAttribute("href", "/?tag=swr");
  });

  it("shows an error message when tags fail to load", async () => {
    vi.mocked(axios.get).mockRejectedValue(new Error("down"));
    renderWithProviders(<Tags />);
    expect(await screen.findByText("Cannot load popular tags...")).toBeInTheDocument();
  });
});

describe("profile buttons", () => {
  it("FollowUserButton toggles between follow and unfollow", async () => {
    const user = userEvent.setup();
    const follow = vi.fn();
    const unfollow = vi.fn();
    const { rerender } = renderWithProviders(
      <FollowUserButton
        isUser={false}
        following={false}
        username="bob"
        follow={follow}
        unfollow={unfollow}
      />
    );
    await user.click(screen.getByRole("button", { name: /Follow bob/ }));
    expect(follow).toHaveBeenCalledWith("bob");

    rerender(
      <FollowUserButton
        isUser={false}
        following={true}
        username="bob"
        follow={follow}
        unfollow={unfollow}
      />
    );
    await user.click(screen.getByRole("button", { name: /Unfollow bob/ }));
    expect(unfollow).toHaveBeenCalledWith("bob");

    rerender(
      <FollowUserButton isUser={true} following={false} username="me" follow={follow} unfollow={unfollow} />
    );
    expect(screen.queryByRole("button")).not.toBeInTheDocument();
  });

  it("EditProfileButton only renders for the owner", () => {
    const { rerender } = renderWithProviders(<EditProfileButton isUser={true} />);
    expect(screen.getByText(/Edit Profile Settings/).closest("a")).toHaveAttribute(
      "href",
      "/user/settings"
    );
    rerender(<EditProfileButton isUser={false} />);
    expect(screen.queryByText(/Edit Profile Settings/)).not.toBeInTheDocument();
  });

  it("ProfileTab links to the author's and favorited articles", () => {
    setRoute("/profile/[pid]", { pid: "jane doe" }, "/profile/jane%20doe");
    renderWithProviders(<ProfileTab profile={{ username: "jane doe" }} />);
    expect(screen.getByText("My Articles").closest("a")).toHaveAttribute(
      "href",
      "/profile/jane%20doe"
    );
    expect(screen.getByText("My Articles").closest("a")).toHaveClass("active");
    expect(screen.getByText("Favorited Articles").closest("a")).toHaveAttribute(
      "href",
      "/profile/jane%20doe?favorite=true"
    );
  });
});
