import React from "react";

interface MaybeProps {
  test: unknown;
  children: React.ReactNode;
}

const Maybe = ({ test, children }: MaybeProps) => <>{test ? children : null}</>;

export default Maybe;
