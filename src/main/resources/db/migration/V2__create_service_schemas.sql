-- ===========================================
-- V2: Create separate schemas for microservices
-- This migration prepares the database for future
-- microservices decomposition by creating separate
-- schemas for each service domain.
-- ===========================================

-- Create schemas for each service domain
-- Note: PostgreSQL uses schemas, H2 in PostgreSQL mode also supports this
CREATE SCHEMA IF NOT EXISTS user_service;
CREATE SCHEMA IF NOT EXISTS article_service;
CREATE SCHEMA IF NOT EXISTS comment_service;

-- ===========================================
-- User Service Schema Tables
-- ===========================================
CREATE TABLE IF NOT EXISTS user_service.users (
  id VARCHAR(255) PRIMARY KEY,
  username VARCHAR(255) UNIQUE,
  password VARCHAR(255),
  email VARCHAR(255) UNIQUE,
  bio TEXT,
  image VARCHAR(511),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_service.follows (
  user_id VARCHAR(255) NOT NULL,
  follow_id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, follow_id)
);

-- Indexes for user_service
CREATE INDEX IF NOT EXISTS idx_user_service_users_username ON user_service.users(username);
CREATE INDEX IF NOT EXISTS idx_user_service_users_email ON user_service.users(email);
CREATE INDEX IF NOT EXISTS idx_user_service_follows_user_id ON user_service.follows(user_id);
CREATE INDEX IF NOT EXISTS idx_user_service_follows_follow_id ON user_service.follows(follow_id);

-- ===========================================
-- Article Service Schema Tables
-- ===========================================
CREATE TABLE IF NOT EXISTS article_service.articles (
  id VARCHAR(255) PRIMARY KEY,
  user_id VARCHAR(255) NOT NULL,
  slug VARCHAR(255) UNIQUE,
  title VARCHAR(255),
  description TEXT,
  body TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS article_service.tags (
  id VARCHAR(255) PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS article_service.article_tags (
  article_id VARCHAR(255) NOT NULL,
  tag_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (article_id, tag_id)
);

CREATE TABLE IF NOT EXISTS article_service.article_favorites (
  article_id VARCHAR(255) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (article_id, user_id)
);

-- Indexes for article_service
CREATE INDEX IF NOT EXISTS idx_article_service_articles_user_id ON article_service.articles(user_id);
CREATE INDEX IF NOT EXISTS idx_article_service_articles_slug ON article_service.articles(slug);
CREATE INDEX IF NOT EXISTS idx_article_service_articles_created_at ON article_service.articles(created_at);
CREATE INDEX IF NOT EXISTS idx_article_service_article_tags_article_id ON article_service.article_tags(article_id);
CREATE INDEX IF NOT EXISTS idx_article_service_article_tags_tag_id ON article_service.article_tags(tag_id);
CREATE INDEX IF NOT EXISTS idx_article_service_article_favorites_article_id ON article_service.article_favorites(article_id);
CREATE INDEX IF NOT EXISTS idx_article_service_article_favorites_user_id ON article_service.article_favorites(user_id);

-- ===========================================
-- Comment Service Schema Tables
-- ===========================================
CREATE TABLE IF NOT EXISTS comment_service.comments (
  id VARCHAR(255) PRIMARY KEY,
  body TEXT,
  article_id VARCHAR(255) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for comment_service
CREATE INDEX IF NOT EXISTS idx_comment_service_comments_article_id ON comment_service.comments(article_id);
CREATE INDEX IF NOT EXISTS idx_comment_service_comments_user_id ON comment_service.comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comment_service_comments_created_at ON comment_service.comments(created_at);
