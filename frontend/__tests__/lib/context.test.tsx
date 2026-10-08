import { act, renderHook } from "@testing-library/react";
import React from "react";
import { describe, expect, it } from "vitest";

import ContextProvider from "../../lib/context";
import PageContextProvider, {
  usePageDispatch,
  usePageState,
} from "../../lib/context/PageContext";
import PageCountContextProvider, {
  usePageCountDispatch,
  usePageCountState,
} from "../../lib/context/PageCountContext";
import useIsMounted from "../../lib/hooks/useIsMounted";
import useSessionStorage from "../../lib/hooks/useSessionStorage";
import useViewport from "../../lib/hooks/useViewport";

describe("PageContext", () => {
  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <PageContextProvider>{children}</PageContextProvider>
  );

  it("starts at page 0 and persists updates to sessionStorage", () => {
    const { result } = renderHook(
      () => ({ page: usePageState(), setPage: usePageDispatch() }),
      { wrapper }
    );
    expect(result.current.page).toBe(0);

    act(() => result.current.setPage?.(3));
    expect(result.current.page).toBe(3);
    expect(window.sessionStorage.getItem("offset")).toBe("3");

    act(() => result.current.setPage?.((previous) => previous + 1));
    expect(result.current.page).toBe(4);
  });

  it("rehydrates from sessionStorage", () => {
    window.sessionStorage.setItem("offset", "7");
    const { result } = renderHook(() => usePageState(), { wrapper });
    expect(result.current).toBe(7);
  });

  it("returns undefined outside a provider", () => {
    const { result } = renderHook(() => ({ page: usePageState(), set: usePageDispatch() }));
    expect(result.current.page).toBeUndefined();
    expect(result.current.set).toBeUndefined();
  });
});

describe("PageCountContext", () => {
  it("starts at 1 and can be updated", () => {
    const { result } = renderHook(
      () => ({ count: usePageCountState(), setCount: usePageCountDispatch() }),
      {
        wrapper: ({ children }) => (
          <PageCountContextProvider>{children}</PageCountContextProvider>
        ),
      }
    );
    expect(result.current.count).toBe(1);
    act(() => result.current.setCount?.(42));
    expect(result.current.count).toBe(42);
  });
});

describe("ContextProvider", () => {
  it("composes both providers", () => {
    const { result } = renderHook(
      () => ({ page: usePageState(), count: usePageCountState() }),
      { wrapper: ({ children }) => <ContextProvider>{children}</ContextProvider> }
    );
    expect(result.current).toEqual({ page: 0, count: 1 });
  });
});

describe("hooks", () => {
  it("useSessionStorage falls back to the initial value and stores JSON", () => {
    const { result } = renderHook(() => useSessionStorage("thing", { a: 1 }));
    expect(result.current[0]).toEqual({ a: 1 });
    act(() => result.current[1]({ a: 2 }));
    expect(JSON.parse(window.sessionStorage.getItem("thing") ?? "")).toEqual({ a: 2 });
  });

  it("useIsMounted is false on first render and true after mounting", () => {
    const { result, rerender } = renderHook(() => useIsMounted());
    expect(result.current).toBe(false);
    rerender();
    expect(result.current).toBe(true);
  });

  it("useViewport tracks window size", () => {
    const { result } = renderHook(() => useViewport());
    expect(result.current.vw).toBe(window.innerWidth);
    act(() => {
      Object.defineProperty(window, "innerWidth", { value: 500, configurable: true });
      window.dispatchEvent(new Event("resize"));
    });
    expect(result.current.vw).toBe(500);
  });
});
