import React from "react";
import { mutate } from "swr";

import { getRange, getPageInfo } from "../../lib/utils/calculatePagination";
import { usePageDispatch, usePageState } from "../../lib/context/PageContext";
import Maybe from "./Maybe";

interface PaginationProps {
  total: number;
  limit: number;
  pageCount: number;
  currentPage: number;
  lastIndex: number;
  fetchURL: string;
}

const Pagination = ({
  total,
  limit,
  pageCount,
  currentPage,
  lastIndex,
  fetchURL,
}: PaginationProps) => {
  const page = usePageState() ?? 0;
  const setPage = usePageDispatch();

  const { firstPage, lastPage, hasPreviousPage, hasNextPage } = getPageInfo({
    limit,
    pageCount,
    total,
    page: currentPage,
  });
  const pages = total > 0 ? getRange(firstPage, lastPage) : [];

  const goTo = React.useCallback(
    (e: React.MouseEvent<HTMLLIElement, MouseEvent>, index: number) => {
      e.preventDefault();
      setPage?.(index);
      mutate(fetchURL);
    },
    [setPage, fetchURL]
  );

  return (
    <nav>
      <ul className="pagination">
        <li className="page-item" onClick={(e) => goTo(e, 0)}>
          <a className="page-link">{`<<`}</a>
        </li>
        <Maybe test={hasPreviousPage}>
          <li className="page-item" onClick={(e) => goTo(e, page - 1)}>
            <a className="page-link">{`<`}</a>
          </li>
        </Maybe>

        {pages.map((pageNumber) => {
          const isCurrent = !currentPage
            ? pageNumber === 0
            : pageNumber === currentPage;
          return (
            <li
              key={pageNumber.toString()}
              className={isCurrent ? "page-item active" : "page-item"}
              onClick={(e) => goTo(e, pageNumber)}
            >
              <a className="page-link">{pageNumber + 1}</a>
            </li>
          );
        })}
        <Maybe test={hasNextPage}>
          <li className="page-item" onClick={(e) => goTo(e, page + 1)}>
            <a className="page-link">{`>`}</a>
          </li>
        </Maybe>
        <li className="page-item" onClick={(e) => goTo(e, lastIndex)}>
          <a className="page-link">{`>>`}</a>
        </li>
      </ul>
    </nav>
  );
};

export default Pagination;
