import { describe, expect, it } from "vitest";

import renderMarkdown from "../../lib/utils/renderMarkdown";

describe("renderMarkdown", () => {
  it("renders basic markdown to HTML", () => {
    const html = renderMarkdown("# Title\n\nSome **bold** text");
    expect(html).toContain("<h1>Title</h1>");
    expect(html).toContain("<strong>bold</strong>");
  });

  it("normalises escaped newlines coming from the API", () => {
    const html = renderMarkdown("line one\\n\\nline two");
    expect(html).toContain("<p>line one</p>");
    expect(html).toContain("<p>line two</p>");
  });

  it("returns an empty string for empty input", () => {
    expect(renderMarkdown("")).toBe("");
    expect(renderMarkdown(null)).toBe("");
    expect(renderMarkdown(undefined)).toBe("");
  });

  it("strips <script> tags", () => {
    const html = renderMarkdown('Hello <script>alert("xss")</script> world');
    expect(html).not.toContain("<script");
    expect(html).not.toContain("alert(");
    expect(html).toContain("Hello");
    expect(html).toContain("world");
  });

  it("strips inline event handlers such as onerror", () => {
    const html = renderMarkdown('<img src="x" onerror="alert(1)">');
    expect(html).not.toMatch(/onerror/i);
    expect(html).not.toContain("alert(1)");
  });

  it("strips javascript: URLs from markdown links", () => {
    const html = renderMarkdown("[click me](javascript:alert(1))");
    expect(html).not.toMatch(/javascript:/i);
    expect(html).toContain("click me");
  });

  it("strips dangerous elements but keeps safe formatting", () => {
    const html = renderMarkdown(
      '<iframe src="https://evil.example"></iframe><p onclick="steal()">safe <em>text</em></p>'
    );
    expect(html).not.toContain("<iframe");
    expect(html).not.toContain("onclick");
    expect(html).toContain("<em>text</em>");
  });
});
