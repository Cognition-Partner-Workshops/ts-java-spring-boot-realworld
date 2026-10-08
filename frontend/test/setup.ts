import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { defaultConfig } from "swr/_internal";
import { afterEach, beforeEach, vi } from "vitest";

import { resetRouter } from "./mockRouter";

vi.mock("next/router", async () => {
  const { mockRouter } = await import("./mockRouter");
  return {
    __esModule: true,
    default: mockRouter,
    useRouter: () => mockRouter,
    Router: mockRouter,
  };
});

vi.mock("axios", () => {
  const instance = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    isAxiosError: (value: unknown) =>
      !!value && typeof value === "object" && "isAxiosError" in value,
  };
  return { __esModule: true, default: instance, ...instance };
});

beforeEach(() => {
  window.localStorage.clear();
  window.sessionStorage.clear();
  resetRouter();
  (defaultConfig.cache as unknown as Map<string, unknown>).clear();
});

afterEach(async () => {
  cleanup();
  vi.resetAllMocks();
  // let SWR's deduping timers settle so in-flight keys don't leak into the next test
  await new Promise((resolve) => setTimeout(resolve, 0));
});
