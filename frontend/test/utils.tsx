import { render, RenderOptions } from "@testing-library/react";
import React from "react";
import { SWRConfig } from "swr";

import ContextProvider from "../lib/context";

const Providers = ({ children }: { children: React.ReactNode }) => (
  <SWRConfig
    value={{
      dedupingInterval: 0,
      focusThrottleInterval: 0,
      revalidateOnFocus: false,
      shouldRetryOnError: false,
    }}
  >
    <ContextProvider>{children}</ContextProvider>
  </SWRConfig>
);

export const renderWithProviders = (
  ui: React.ReactElement,
  options?: Omit<RenderOptions, "wrapper">
) => render(ui, { wrapper: Providers, ...options });

export * from "@testing-library/react";
