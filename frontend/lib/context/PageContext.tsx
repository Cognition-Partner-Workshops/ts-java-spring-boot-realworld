import React from "react";

import useSessionStorage from "../hooks/useSessionStorage";

export type PageDispatch = (value: number | ((previous: number) => number)) => void;

interface Props {
  children: React.ReactNode;
}

const PageStateContext = React.createContext<number | undefined>(undefined);

const PageDispatchContext = React.createContext<PageDispatch | undefined>(
  undefined
);

const PageContextProvider = ({ children }: Props) => {
  const [page, setPage] = useSessionStorage<number>("offset", 0);
  return (
    <PageDispatchContext.Provider value={setPage}>
      <PageStateContext.Provider value={page}>
        {children}
      </PageStateContext.Provider>
    </PageDispatchContext.Provider>
  );
};

export const usePageState = () => {
  const state = React.useContext(PageStateContext);
  return state;
};

export const usePageDispatch = () => {
  const dispatch = React.useContext(PageDispatchContext);
  return dispatch;
};

export default PageContextProvider;
