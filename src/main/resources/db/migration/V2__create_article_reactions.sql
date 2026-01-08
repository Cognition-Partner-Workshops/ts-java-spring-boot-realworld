create table article_reactions (
  article_id varchar(255) not null,
  user_id varchar(255) not null,
  reaction_type varchar(20) not null,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  primary key(article_id, user_id)
);
