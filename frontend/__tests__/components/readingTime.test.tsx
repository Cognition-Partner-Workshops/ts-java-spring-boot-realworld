import React from "react";
import { describe, expect, it } from "vitest";

import ArticleMeta from "../../components/article/ArticleMeta";
import ArticlePreview from "../../components/article/ArticlePreview";
import ReadingTimeBadge from "../../components/article/ReadingTimeBadge";
import { article } from "../../test/fixtures";
import { renderWithProviders, screen } from "../../test/utils";

describe("ReadingTimeBadge", () => {
  it("renders the estimated reading time", () => {
    renderWithProviders(<ReadingTimeBadge minutes={4} />);

    expect(screen.getByTestId("reading-time-badge")).toHaveTextContent("4 min read");
    expect(screen.getByTestId("reading-time-badge")).toHaveAttribute(
      "title",
      "Estimated reading time"
    );
  });

  it.each([undefined, 0, Number.NaN])("renders nothing for invalid minutes: %s", (minutes) => {
    const { container } = renderWithProviders(
      <ReadingTimeBadge minutes={minutes} />
    );

    expect(container).toBeEmptyDOMElement();
  });
});

describe("article reading-time badges", () => {
  it("shows the reading time in an article preview", () => {
    renderWithProviders(
      <ArticlePreview article={{ ...article, readingTimeMinutes: 4 }} />
    );

    expect(screen.getByTestId("reading-time-badge")).toHaveTextContent("4 min read");
  });

  it("shows the reading time in article metadata", () => {
    renderWithProviders(<ArticleMeta article={{ ...article, readingTimeMinutes: 4 }} />);

    expect(screen.getByTestId("reading-time-badge")).toHaveTextContent("4 min read");
  });

  it("omits badges when the article has no reading time", () => {
    const articleWithoutReadingTime = { ...article, readingTimeMinutes: undefined };
    const { rerender } = renderWithProviders(
      <ArticlePreview article={articleWithoutReadingTime} />
    );
    expect(screen.queryByTestId("reading-time-badge")).not.toBeInTheDocument();

    rerender(
      <ArticleMeta article={articleWithoutReadingTime} />
    );
    expect(screen.queryByTestId("reading-time-badge")).not.toBeInTheDocument();
  });
});
