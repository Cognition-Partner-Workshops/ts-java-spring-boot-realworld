-- ===========================================
-- User Service Tables
-- ===========================================
CREATE TABLE IF NOT EXISTS users (
  id VARCHAR(255) PRIMARY KEY,
  username VARCHAR(255) UNIQUE,
  password VARCHAR(255),
  email VARCHAR(255) UNIQUE,
  bio TEXT,
  image VARCHAR(511)
);

CREATE TABLE IF NOT EXISTS follows (
  user_id VARCHAR(255) NOT NULL,
  follow_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (user_id, follow_id)
);

-- ===========================================
-- Article Service Tables
-- ===========================================
CREATE TABLE IF NOT EXISTS articles (
  id VARCHAR(255) PRIMARY KEY,
  user_id VARCHAR(255),
  slug VARCHAR(255) UNIQUE,
  title VARCHAR(255),
  description TEXT,
  body TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tags (
  id VARCHAR(255) PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS article_tags (
  article_id VARCHAR(255) NOT NULL,
  tag_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (article_id, tag_id)
);

CREATE TABLE IF NOT EXISTS article_favorites (
  article_id VARCHAR(255) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (article_id, user_id)
);

-- ===========================================
-- Comment Service Tables
-- ===========================================
CREATE TABLE IF NOT EXISTS comments (
  id VARCHAR(255) PRIMARY KEY,
  body TEXT,
  article_id VARCHAR(255),
  user_id VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================
-- Indexes for better query performance
-- ===========================================
CREATE INDEX IF NOT EXISTS idx_articles_user_id ON articles(user_id);
CREATE INDEX IF NOT EXISTS idx_articles_slug ON articles(slug);
CREATE INDEX IF NOT EXISTS idx_articles_created_at ON articles(created_at);
CREATE INDEX IF NOT EXISTS idx_article_tags_article_id ON article_tags(article_id);
CREATE INDEX IF NOT EXISTS idx_article_tags_tag_id ON article_tags(tag_id);
CREATE INDEX IF NOT EXISTS idx_article_favorites_article_id ON article_favorites(article_id);
CREATE INDEX IF NOT EXISTS idx_article_favorites_user_id ON article_favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_article_id ON comments(article_id);
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_follows_user_id ON follows(user_id);
CREATE INDEX IF NOT EXISTS idx_follows_follow_id ON follows(follow_id);
