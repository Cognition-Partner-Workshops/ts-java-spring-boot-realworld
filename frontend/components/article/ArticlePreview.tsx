import axios from "axios";
import Link from "next/link";
import Router from "next/router";
import React from "react";
import useSWR from "swr";

import CustomLink from "../common/CustomLink";
import CustomImage from "../common/CustomImage";
import { usePageDispatch } from "../../lib/context/PageContext";
import { ArticleType } from "../../lib/types/articleType";
import checkLogin from "../../lib/utils/checkLogin";
import { SERVER_BASE_URL } from "../../lib/utils/constant";
import storage from "../../lib/utils/storage";

const FAVORITED_CLASS = "btn btn-sm btn-primary";
const NOT_FAVORITED_CLASS = "btn btn-sm btn-outline-primary";

interface ArticlePreviewProps {
  article: ArticleType;
}

const ArticlePreview = ({ article }: ArticlePreviewProps) => {
  const setPage = usePageDispatch();

  const [preview, setPreview] = React.useState(article);
  const [hover, setHover] = React.useState(false);
  const [currentIndex, setCurrentIndex] = React.useState(-1);

  const { data: currentUser } = useSWR("user", storage);
  const isLoggedIn = checkLogin(currentUser);

  const toggleFavorite = (current: ArticleType): ArticleType => ({
    ...current,
    favorited: !current.favorited,
    favoritesCount: current.favorited
      ? current.favoritesCount - 1
      : current.favoritesCount + 1,
  });

  const handleClickFavorite = async (slug: string) => {
    if (!isLoggedIn) {
      Router.push(`/user/login`);
      return;
    }

    const optimistic = toggleFavorite(preview);
    setPreview(optimistic);

    try {
      if (preview.favorited) {
        await axios.delete(`${SERVER_BASE_URL}/articles/${slug}/favorite`, {
          headers: {
            Authorization: `Token ${currentUser?.token}`,
          },
        });
      } else {
        await axios.post(
          `${SERVER_BASE_URL}/articles/${slug}/favorite`,
          {},
          {
            headers: {
              Authorization: `Token ${currentUser?.token}`,
            },
          }
        );
      }
    } catch {
      setPreview(preview);
    }
  };

  if (!article) return null;

  return (
    <div className="article-preview" style={{ padding: "1.5rem 0.5rem" }}>
      <div className="article-meta">
        <CustomLink
          href="/profile/[pid]"
          as={`/profile/${preview.author.username}`}
        >
          <CustomImage
            src={preview.author.image}
            alt="author's profile image"
          />
        </CustomLink>

        <div className="info">
          <CustomLink
            href="/profile/[pid]"
            as={`/profile/${preview.author.username}`}
            className="author"
          >
            <span onClick={() => setPage?.(0)}>{preview.author.username}</span>
          </CustomLink>
          <span className="date">
            {new Date(preview.createdAt).toDateString()}
          </span>
        </div>

        <div className="pull-xs-right">
          <button
            className={
              preview.favorited ? FAVORITED_CLASS : NOT_FAVORITED_CLASS
            }
            onClick={() => handleClickFavorite(preview.slug)}
          >
            <i className="ion-heart" /> {preview.favoritesCount}
          </button>
        </div>
      </div>

      <CustomLink
        href="/article/[pid]"
        as={`/article/${preview.slug}`}
        className="preview-link"
      >
        <h1>{preview.title}</h1>
        <p>{preview.description}</p>
        <span>Read more...</span>
      </CustomLink>
      <ul className="tag-list" style={{ maxWidth: "100%" }}>
        {preview.tagList.map((tag, index) => (
          <li
            key={tag}
            className="tag-default tag-pill tag-outline"
            onMouseOver={() => {
              setHover(true);
              setCurrentIndex(index);
            }}
            onMouseLeave={() => {
              setHover(false);
              setCurrentIndex(-1);
            }}
            style={{
              borderColor:
                hover && currentIndex === index ? "#5cb85c" : "initial",
            }}
          >
            <Link
              href={`/?tag=${tag}`}
              style={{
                color: hover && currentIndex === index ? "#5cb85c" : "inherit",
              }}
              onClick={() => setPage?.(0)}
            >
              {tag}
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default ArticlePreview;
