import { check, group } from 'k6';
import http from 'k6/http';
import { BASE_URL, getAuthHeaders, randomString, TEST_USER } from './config.js';

let authToken = null;

export function registerUser() {
  return group('Register User', () => {
    const uniqueId = randomString(8);
    const payload = JSON.stringify({
      user: {
        email: `perf_${uniqueId}@example.com`,
        username: `perf_${uniqueId}`,
        password: 'perftest123',
      },
    });

    const response = http.post(`${BASE_URL}/users`, payload, {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Register User' },
    });

    check(response, {
      'Registration successful': (r) => r.status === 200 || r.status === 201,
      'Has token': (r) => {
        try {
          const body = JSON.parse(r.body);
          if (body.user && body.user.token) {
            authToken = body.user.token;
            return true;
          }
          return false;
        } catch {
          return false;
        }
      },
    });

    return response;
  });
}

export function login() {
  return group('Login', () => {
    const payload = JSON.stringify({
      user: {
        email: TEST_USER.email,
        password: TEST_USER.password,
      },
    });

    const response = http.post(`${BASE_URL}/users/login`, payload, {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Login' },
    });

    check(response, {
      'Login successful': (r) => r.status === 200,
      'Has token': (r) => {
        try {
          const body = JSON.parse(r.body);
          if (body.user && body.user.token) {
            authToken = body.user.token;
            return true;
          }
          return false;
        } catch {
          return false;
        }
      },
    });

    return response;
  });
}

export function getToken() {
  return authToken;
}

export function setToken(token) {
  authToken = token;
}

export function getCurrentUser() {
  return group('Get Current User', () => {
    const response = http.get(`${BASE_URL}/user`, {
      headers: getAuthHeaders(authToken),
      tags: { name: 'Get Current User' },
    });

    check(response, {
      'Get user successful': (r) => r.status === 200,
      'Has user data': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.user && body.user.email;
        } catch {
          return false;
        }
      },
    });

    return response;
  });
}

export function getArticles(limit = 10, offset = 0) {
  return group('Get Articles', () => {
    const response = http.get(`${BASE_URL}/articles?limit=${limit}&offset=${offset}`, {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Get Articles' },
    });

    check(response, {
      'Get articles successful': (r) => r.status === 200,
      'Has articles array': (r) => {
        try {
          const body = JSON.parse(r.body);
          return Array.isArray(body.articles);
        } catch {
          return false;
        }
      },
    });

    return response;
  });
}

export function getArticlesFeed(limit = 10, offset = 0) {
  return group('Get Articles Feed', () => {
    const response = http.get(`${BASE_URL}/articles/feed?limit=${limit}&offset=${offset}`, {
      headers: getAuthHeaders(authToken),
      tags: { name: 'Get Articles Feed' },
    });

    check(response, {
      'Get feed successful': (r) => r.status === 200,
      'Has articles array': (r) => {
        try {
          const body = JSON.parse(r.body);
          return Array.isArray(body.articles);
        } catch {
          return false;
        }
      },
    });

    return response;
  });
}

export function createArticle() {
  return group('Create Article', () => {
    const uniqueId = randomString(8);
    const payload = JSON.stringify({
      article: {
        title: `Performance Test Article ${uniqueId}`,
        description: 'This is a performance test article',
        body: `This article was created during performance testing at ${new Date().toISOString()}`,
        tagList: ['performance', 'test'],
      },
    });

    const response = http.post(`${BASE_URL}/articles`, payload, {
      headers: getAuthHeaders(authToken),
      tags: { name: 'Create Article' },
    });

    check(response, {
      'Create article successful': (r) => r.status === 200 || r.status === 201,
      'Has article slug': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.article && body.article.slug;
        } catch {
          return false;
        }
      },
    });

    return response;
  });
}

export function getArticle(slug) {
  return group('Get Article', () => {
    const response = http.get(`${BASE_URL}/articles/${slug}`, {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Get Article' },
    });

    check(response, {
      'Get article successful': (r) => r.status === 200,
      'Has article data': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.article && body.article.slug;
        } catch {
          return false;
        }
      },
    });

    return response;
  });
}

export function getTags() {
  return group('Get Tags', () => {
    const response = http.get(`${BASE_URL}/tags`, {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Get Tags' },
    });

    check(response, {
      'Get tags successful': (r) => r.status === 200,
      'Has tags array': (r) => {
        try {
          const body = JSON.parse(r.body);
          return Array.isArray(body.tags);
        } catch {
          return false;
        }
      },
    });

    return response;
  });
}

export function getProfile(username) {
  return group('Get Profile', () => {
    const response = http.get(`${BASE_URL}/profiles/${username}`, {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Get Profile' },
    });

    check(response, {
      'Get profile successful': (r) => r.status === 200,
      'Has profile data': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.profile && body.profile.username;
        } catch {
          return false;
        }
      },
    });

    return response;
  });
}

export function addComment(slug) {
  return group('Add Comment', () => {
    const payload = JSON.stringify({
      comment: {
        body: `Performance test comment at ${new Date().toISOString()}`,
      },
    });

    const response = http.post(`${BASE_URL}/articles/${slug}/comments`, payload, {
      headers: getAuthHeaders(authToken),
      tags: { name: 'Add Comment' },
    });

    check(response, {
      'Add comment successful': (r) => r.status === 200 || r.status === 201,
      'Has comment data': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.comment && body.comment.id;
        } catch {
          return false;
        }
      },
    });

    return response;
  });
}

export function getComments(slug) {
  return group('Get Comments', () => {
    const response = http.get(`${BASE_URL}/articles/${slug}/comments`, {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Get Comments' },
    });

    check(response, {
      'Get comments successful': (r) => r.status === 200,
      'Has comments array': (r) => {
        try {
          const body = JSON.parse(r.body);
          return Array.isArray(body.comments);
        } catch {
          return false;
        }
      },
    });

    return response;
  });
}

export function favoriteArticle(slug) {
  return group('Favorite Article', () => {
    const response = http.post(`${BASE_URL}/articles/${slug}/favorite`, null, {
      headers: getAuthHeaders(authToken),
      tags: { name: 'Favorite Article' },
    });

    check(response, {
      'Favorite successful': (r) => r.status === 200,
      'Article favorited': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.article && body.article.favorited === true;
        } catch {
          return false;
        }
      },
    });

    return response;
  });
}
