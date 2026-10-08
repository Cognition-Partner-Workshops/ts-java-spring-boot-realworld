import React from "react";

import PageContext from "./PageContext";
import PageCountContext from "./PageCountContext";

interface Props {
  children: React.ReactNode;
}

const ContextProvider = ({ children }: Props) => (
  <PageContext>
    <PageCountContext>{children}</PageCountContext>
  </PageContext>
);

export default ContextProvider;
