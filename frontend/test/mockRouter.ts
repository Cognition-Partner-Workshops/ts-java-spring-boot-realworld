import { vi } from "vitest";

type Query = Record<string, string | string[] | undefined>;

export const mockRouter = {
  pathname: "/",
  asPath: "/",
  route: "/",
  basePath: "",
  query: {} as Query,
  isReady: true,
  isFallback: false,
  isPreview: false,
  isLocaleDomain: false,
  push: vi.fn(() => Promise.resolve(true)),
  replace: vi.fn(() => Promise.resolve(true)),
  prefetch: vi.fn(() => Promise.resolve()),
  back: vi.fn(),
  reload: vi.fn(),
  beforePopState: vi.fn(),
  events: { on: vi.fn(), off: vi.fn(), emit: vi.fn() },
};

export const setRoute = (pathname: string, query: Query = {}, asPath?: string) => {
  mockRouter.pathname = pathname;
  mockRouter.route = pathname;
  mockRouter.query = query;
  mockRouter.asPath = asPath ?? pathname;
};

export const resetRouter = () => {
  setRoute("/", {});
  mockRouter.push.mockClear();
  mockRouter.replace.mockClear();
};
