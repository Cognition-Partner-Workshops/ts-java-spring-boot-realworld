import userEvent from "@testing-library/user-event";
import React from "react";
import { mutate } from "swr";
import { describe, expect, it, vi } from "vitest";

import Pagination from "../../components/common/Pagination";
import { usePageState } from "../../lib/context/PageContext";
import { renderWithProviders, screen } from "../../test/utils";

vi.mock("swr", async (importOriginal) => {
  const actual = await importOriginal<typeof import("swr")>();
  return { ...actual, mutate: vi.fn(actual.mutate) };
});

const PageProbe = () => <output data-testid="page">{usePageState()}</output>;

const ConnectedPagination = ({ total }: { total: number }) => (
  <Pagination
    total={total}
    limit={20}
    pageCount={5}
    currentPage={usePageState() ?? 0}
    lastIndex={9}
    fetchURL="http://localhost:8080/articles?offset=0"
  />
);

const renderPagination = (currentPage = 0, total = 200) => {
  window.sessionStorage.setItem("offset", String(currentPage));
  return renderWithProviders(
    <>
      <ConnectedPagination total={total} />
      <PageProbe />
    </>
  );
};

describe("Pagination", () => {
  it("renders a window of page numbers with the active page highlighted", () => {
    renderPagination(0);
    expect(screen.getByText("1").closest("li")).toHaveClass("page-item", "active");
    expect(screen.getByText("5")).toBeInTheDocument();
    expect(screen.queryByText("6")).not.toBeInTheDocument();
    expect(screen.queryByText("<")).not.toBeInTheDocument();
    expect(screen.getByText(">")).toBeInTheDocument();
    expect(screen.getByText("<<")).toBeInTheDocument();
    expect(screen.getByText(">>")).toBeInTheDocument();
  });

  it("highlights the current page in the middle of the range", () => {
    renderPagination(5);
    expect(screen.getByText("6").closest("li")).toHaveClass("active");
    expect(screen.getByText("4")).toBeInTheDocument();
    expect(screen.getByText("8")).toBeInTheDocument();
    expect(screen.queryByText("1")).not.toBeInTheDocument();
    expect(screen.queryByText("9")).not.toBeInTheDocument();
    expect(screen.getByText("<")).toBeInTheDocument();
    expect(screen.getByText(">")).toBeInTheDocument();
  });

  it("renders no numbered pages when there are no articles", () => {
    renderPagination(0, 0);
    expect(screen.queryByText("1")).not.toBeInTheDocument();
  });

  it("updates the page context and revalidates the feed on click", async () => {
    const user = userEvent.setup();
    renderPagination(0);
    await user.click(screen.getByText("3"));
    expect(screen.getByTestId("page")).toHaveTextContent("2");
    expect(screen.getByText("3").closest("li")).toHaveClass("active");
    expect(window.sessionStorage.getItem("offset")).toBe("2");
    expect(mutate).toHaveBeenCalledWith("http://localhost:8080/articles?offset=0");

    await user.click(screen.getByText(">"));
    expect(screen.getByTestId("page")).toHaveTextContent("3");

    await user.click(screen.getByText("<"));
    expect(screen.getByTestId("page")).toHaveTextContent("2");

    await user.click(screen.getByText(">>"));
    expect(screen.getByTestId("page")).toHaveTextContent("9");

    await user.click(screen.getByText("<<"));
    expect(screen.getByTestId("page")).toHaveTextContent("0");
  });
});
