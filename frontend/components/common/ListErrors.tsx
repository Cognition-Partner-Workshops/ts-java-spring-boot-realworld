import React from "react";

export type ErrorMap = Record<string, string | string[]> | null | undefined;

interface ListErrorsProps {
  errors: ErrorMap;
}

const ListErrors = ({ errors }: ListErrorsProps) => (
  <ul className="error-messages">
    {Object.keys(errors ?? {}).map((key) => {
      const value = errors![key];
      return (
        <li key={key}>
          {key} {Array.isArray(value) ? value.join(", ") : value}
        </li>
      );
    })}
  </ul>
);

export default ListErrors;
