import DOMPurify from "isomorphic-dompurify";
import { marked } from "marked";

marked.use({ async: false, gfm: true, breaks: false });

export const renderMarkdown = (source: string | null | undefined): string => {
  if (!source) return "";
  const html = marked.parse(source.replace(/\\n/g, "\n")) as string;
  return DOMPurify.sanitize(html, { USE_PROFILES: { html: true } });
};

export default renderMarkdown;
