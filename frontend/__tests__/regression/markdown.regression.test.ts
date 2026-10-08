import { describe, expect, it } from "vitest";

import renderMarkdown from "../../lib/utils/renderMarkdown";

describe("regression: markdown rendering and sanitising", () => {
  it("renders GFM tables, fenced code and lists", () => {
    const html = renderMarkdown(
      "| a | b |\n|---|---|\n| 1 | 2 |\n\n```js\nconst x = 1;\n```\n\n- one\n- two"
    );
    expect(html).toContain("<table>");
    expect(html).toContain("<td>1</td>");
    expect(html).toMatch(/<pre><code class="language-js">const x = 1;/);
    expect(html).toContain("<li>one</li>");
  });

  it("keeps safe links and images but drops embedded frames and objects", () => {
    const html = renderMarkdown(
      '[site](https://example.com) ![alt](https://example.com/a.png)\n\n<iframe src="https://evil"></iframe><object data="x"></object><embed src="x">'
    );
    expect(html).toContain('<a href="https://example.com">site</a>');
    expect(html).toContain('<img src="https://example.com/a.png" alt="alt">');
    expect(html).not.toContain("<iframe");
    expect(html).not.toContain("<embed");
    expect(html).not.toContain("<object");
  });

  it("neutralises javascript: in images and data: SVG payloads", () => {
    const html = renderMarkdown(
      '<img src="javascript:alert(1)"><a href="data:text/html,<script>alert(1)</script>">x</a>'
    );
    expect(html).not.toContain("javascript:");
    expect(html).not.toContain("<script");
  });

  it("escapes raw HTML entities inside inline code", () => {
    const html = renderMarkdown("`<b>not bold</b>`");
    expect(html).toContain("<code>&lt;b&gt;not bold&lt;/b&gt;</code>");
    expect(html).not.toContain("<b>");
  });

  it("treats null, undefined and whitespace-only input as empty", () => {
    expect(renderMarkdown(null)).toBe("");
    expect(renderMarkdown(undefined)).toBe("");
    expect(renderMarkdown("   \n  ").trim()).toBe("");
  });
});
