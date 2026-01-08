import { sleep } from 'k6';
import { THRESHOLDS } from './config.js';
import { registerUser, getArticles, getTags, getProfile, setToken } from './helpers.js';

export const options = {
  vus: 5,
  duration: '1m',
  thresholds: {
    http_req_failed: THRESHOLDS.HTTP_ERRORS,
    http_req_duration: THRESHOLDS.RESPONSE_TIME.SMOKE.p95,
  },
};

export default function () {
  // Register a new user for each iteration
  const registerResponse = registerUser();
  sleep(0.5);

  // Get articles (public endpoint)
  getArticles(10, 0);
  sleep(0.5);

  // Get tags (public endpoint)
  getTags();
  sleep(0.5);

  // Get profile (public endpoint)
  try {
    const body = JSON.parse(registerResponse.body);
    if (body.user && body.user.username) {
      getProfile(body.user.username);
    }
  } catch (e) {
    // Ignore parse errors
  }
  sleep(0.5);
}
