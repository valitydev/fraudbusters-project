import http from 'k6/http';
import { integer, text } from './env.js';

const REFRESH_MARGIN_MS = 30 * 1000;

let cached;

function keycloakSettings() {
  const url = text('FB_AUTH_URL', '');
  if (!url) return undefined;
  return {
    tokenUrl: `${url.replace(/\/$/, '')}/realms/${text('FB_AUTH_REALM', 'fraudbusters-realm')}/protocol/openid-connect/token`,
    client: text('FB_AUTH_CLIENT', 'fraudbusters-app'),
    username: text('FB_AUTH_USER', ''),
    password: text('FB_AUTH_PASSWORD', ''),
    timeout: `${integer('REQUEST_TIMEOUT_MS', 30000, 1)}ms`,
  };
}

function requestToken(settings, form) {
  const response = http.post(settings.tokenUrl, { client_id: settings.client, ...form }, {
    timeout: settings.timeout, tags: { stage: 'auth_token', name: 'auth_token' },
  });
  if (response.status !== 200) {
    throw new Error(`Keycloak token request failed: HTTP ${response.status} from ${settings.tokenUrl}`);
  }
  const body = response.json();
  return {
    accessToken: body.access_token,
    refreshToken: body.refresh_token,
    expiresAt: Date.now() + body.expires_in * 1000,
    refreshExpiresAt: Date.now() + (body.refresh_expires_in || 0) * 1000,
  };
}

function freshToken(settings) {
  const now = Date.now();
  if (cached && cached.expiresAt - REFRESH_MARGIN_MS > now) return cached;
  if (cached && cached.refreshToken && cached.refreshExpiresAt - REFRESH_MARGIN_MS > now) {
    cached = requestToken(settings, { grant_type: 'refresh_token', refresh_token: cached.refreshToken });
    return cached;
  }
  if (!settings.username || !settings.password) {
    throw new Error('FB_AUTH_URL is set, but FB_AUTH_USER or FB_AUTH_PASSWORD is empty');
  }
  cached = requestToken(settings, { grant_type: 'password', username: settings.username, password: settings.password });
  return cached;
}

export function authorizationHeaders() {
  const staticToken = text('FB_TOKEN', '');
  if (staticToken) return { Authorization: `Bearer ${staticToken}` };
  const settings = keycloakSettings();
  return settings ? { Authorization: `Bearer ${freshToken(settings).accessToken}` } : {};
}
