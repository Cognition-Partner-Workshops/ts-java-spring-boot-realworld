import userEvent from "@testing-library/user-event";
import React from "react";
import { describe, expect, it } from "vitest";

import ContextProvider from "../../lib/context";
import { usePageDispatch, usePageState } from "../../lib/context/PageContext";
import {
  usePageCountDispatch,
  usePageCountState,
} from "../../lib/context/PageCountContext";
import useSessionStorage from "../../lib/hooks/useSessionStorage";
import { render, screen } from "../../test/utils";

const PageProbe = () => {
  const page = usePageState();
  const setPage = usePageDispatch();
  const count = usePageCountState();
  const setCount = usePageCountDispatch();
  return (
    <>
      <output data-testid="page">{String(page)}</output>
      <output data-testid="count">{String(count)}</output>
      <button onClick={() => setPage?.((previous) => previous + 2)}>bump</button>
      <button onClick={() => setPage?.(0)}>reset</button>
      <button onClick={() => setCount?.((previous) => previous * 10)}>count</button>
    </>
  );
};

describe("regression: page/pageCount context state", () => {
  it("supports functional updates and persists the page offset", async () => {
    const user = userEvent.setup();
    render(
      <ContextProvider>
        <PageProbe />
      </ContextProvider>
    );
    expect(screen.getByTestId("page")).toHaveTextContent("0");
    await user.click(screen.getByText("bump"));
    await user.click(screen.getByText("bump"));
    expect(screen.getByTestId("page")).toHaveTextContent("4");
    expect(window.sessionStorage.getItem("offset")).toBe("4");
    await user.click(screen.getByText("reset"));
    expect(window.sessionStorage.getItem("offset")).toBe("0");
    await user.click(screen.getByText("count"));
    expect(screen.getByTestId("count")).toHaveTextContent("10");
  });

  it("rehydrates the persisted offset for a new provider tree", () => {
    window.sessionStorage.setItem("offset", "7");
    render(
      <ContextProvider>
        <PageProbe />
      </ContextProvider>
    );
    expect(screen.getByTestId("page")).toHaveTextContent("7");
    expect(screen.getByTestId("count")).toHaveTextContent("1");
  });

  it("dispatchers are undefined outside the providers so consumers no-op", async () => {
    const user = userEvent.setup();
    render(<PageProbe />);
    expect(screen.getByTestId("page")).toHaveTextContent("undefined");
    expect(screen.getByTestId("count")).toHaveTextContent("undefined");
    await user.click(screen.getByText("bump"));
    await user.click(screen.getByText("count"));
    expect(window.sessionStorage.getItem("offset")).toBeNull();
  });

  it("useSessionStorage stores objects as JSON and reads them back", async () => {
    const user = userEvent.setup();
    const Probe = () => {
      const [value, setValue] = useSessionStorage<{ n: number }>("probe", { n: 1 });
      return <button onClick={() => setValue({ n: value.n + 1 })}>{value.n}</button>;
    };
    render(<Probe />);
    await user.click(screen.getByText("1"));
    expect(screen.getByText("2")).toBeInTheDocument();
    expect(window.sessionStorage.getItem("probe")).toBe(JSON.stringify({ n: 2 }));
  });
});
