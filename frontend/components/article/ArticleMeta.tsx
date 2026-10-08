import React from "react";

import ArticleActions from "./ArticleActions";
import CustomImage from "../common/CustomImage";
import CustomLink from "../common/CustomLink";
import { ArticleType } from "../../lib/types/articleType";

interface ArticleMetaProps {
  article: ArticleType;
}

const ArticleMeta = ({ article }: ArticleMetaProps) => {
  if (!article) return null;

  return (
    <div className="article-meta">
      <CustomLink
        href="/profile/[pid]"
        as={`/profile/${encodeURIComponent(article.author?.username)}`}
      >
        <CustomImage src={article.author?.image} alt="author-profile-image" />
      </CustomLink>

      <div className="info">
        <CustomLink
          href="/profile/[pid]"
          as={`/profile/${encodeURIComponent(article.author?.username)}`}
          className="author"
        >
          {article.author?.username}
        </CustomLink>
        <span className="date">
          {new Date(article.createdAt).toDateString()}
        </span>
      </div>

      <ArticleActions article={article} />
    </div>
  );
};

export default ArticleMeta;
