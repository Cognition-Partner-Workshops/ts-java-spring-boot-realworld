import { sleep } from 'k6';
import { THRESHOLDS } from './config.js';
import { registerUser, getArticles, getTags, getArticlesFeed, createArticle, getComments } from './helpers.js';

export const options = {
  stages: [
    { duration: '1m', target: 50 },
    { duration: '2m', target: 100 },
    { duration: '3m', target: 200 },
    { duration: '5m', target: 200 },
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_failed: THRESHOLDS.HTTP_ERRORS,
    http_req_duration: THRESHOLDS.RESPONSE_TIME.LOAD.p95,
  },
};

export default function () {
  // Register a new user for authenticated operations
  const registerResponse = registerUser();

  const scenario = Math.random();

  if (scenario < 0.3) {
    // Read operations - articles
    getArticles(10, 0);
  } else if (scenario < 0.5) {
    // Read operations - tags
    getTags();
  } else if (scenario < 0.7) {
    // Authenticated read - feed
    getArticlesFeed(10, 0);
  } else if (scenario < 0.85) {
    // Write operation - create article
    const articleResponse = createArticle();
    try {
      const body = JSON.parse(articleResponse.body);
      if (body.article && body.article.slug) {
        getComments(body.article.slug);
      }
    } catch (e) {
      // Ignore parse errors
    }
  } else {
    // Mixed operations
    getArticles(20, 0);
    getTags();
  }

  sleep(0.1);
}
