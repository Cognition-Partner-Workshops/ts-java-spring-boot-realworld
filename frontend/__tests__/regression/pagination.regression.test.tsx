import userEvent from "@testing-library/user-event";
import React from "react";
import { mutate } from "swr";
import { describe, expect, it, vi } from "vitest";

import Pagination from "../../components/common/Pagination";
import { getPageInfo, getRange } from "../../lib/utils/calculatePagination";
import { getQuery } from "../../lib/utils/getQuery";
import { usePageState } from "../../lib/context/PageContext";
import { renderWithProviders, screen } from "../../test/utils";

vi.mock("swr", async (importOriginal) => ({
  ...(await importOriginal<typeof import("swr")>()),
  mutate: vi.fn(),
}));

const PageEcho = () => <output data-testid="page">{usePageState()}</output>;

describe("regression: pagination maths", () => {
  it("getPageInfo reports no previous/next on a single page", () => {
    const info = getPageInfo({ limit: 10, pageCount: 5, total: 7, page: 0 });
    expect(info.totalPages).toBe(0);
    expect(info.hasPreviousPage).toBe(false);
    expect(info.hasNextPage).toBe(false);
    // fewer articles than one page: the window collapses and no numbered page is rendered
    expect(getRange(info.firstPage, info.lastPage)).toEqual([]);
  });

  it("getPageInfo clamps an out-of-range page and keeps the window inside bounds", () => {
    const info = getPageInfo({ limit: 10, pageCount: 3, total: 95, page: 42 });
    expect(info.totalPages).toBe(9);
    expect(info.lastPage).toBe(9);
    expect(info.firstPage).toBeGreaterThanOrEqual(1);
    expect(info.lastPage - info.firstPage + 1).toBe(3);
    expect(info.hasNextPage).toBe(false);
    expect(info.hasPreviousPage).toBe(true);
    expect(info.previousPage).toBe(8);
  });

  it("getQuery maps page numbers to offsets", () => {
    expect(getQuery(20, 0)).toBe("limit=20&offset=0");
    expect(getQuery(20, 3)).toBe("limit=20&offset=60");
    expect(getQuery(5, undefined as unknown as number)).toBe("limit=5&offset=0");
  });
});

describe("regression: Pagination controls", () => {
  it("first/previous/next/last controls update the page and revalidate the feed", async () => {
    const user = userEvent.setup();
    const mutateSpy = vi.mocked(mutate);
    mutateSpy.mockClear();
    window.sessionStorage.setItem("offset", "3");
    renderWithProviders(
      <>
        <Pagination
          total={100}
          limit={10}
          pageCount={5}
          currentPage={3}
          lastIndex={9}
          fetchURL="http://api/articles?limit=10&offset=30"
        />
        <PageEcho />
      </>
    );
    expect(screen.getByText("4").closest("li")).toHaveClass("active");

    await user.click(screen.getByText(">"));
    expect(screen.getByTestId("page")).toHaveTextContent("4");
    await user.click(screen.getByText("<"));
    expect(screen.getByTestId("page")).toHaveTextContent("3");
    await user.click(screen.getByText(">>"));
    expect(screen.getByTestId("page")).toHaveTextContent("9");
    await user.click(screen.getByText("<<"));
    expect(screen.getByTestId("page")).toHaveTextContent("0");
    expect(mutateSpy).toHaveBeenCalledTimes(4);
    expect(mutateSpy).toHaveBeenCalledWith("http://api/articles?limit=10&offset=30");
  });

  it("hides the previous control on the first page and the next control on the last", () => {
    const { unmount } = renderWithProviders(
      <Pagination total={29} limit={10} pageCount={5} currentPage={0} lastIndex={2} fetchURL="k" />
    );
    expect(screen.queryByText("<")).not.toBeInTheDocument();
    expect(screen.getByText(">")).toBeInTheDocument();
    unmount();
    renderWithProviders(
      <Pagination total={29} limit={10} pageCount={5} currentPage={2} lastIndex={2} fetchURL="k" />
    );
    expect(screen.getByText("<")).toBeInTheDocument();
    expect(screen.queryByText(">")).not.toBeInTheDocument();
  });
});
