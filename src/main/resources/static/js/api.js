export const TOKEN_KEY = 'token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

function normalizeErrorMessage(payload, fallback) {
  if (!payload) return fallback;
  if (typeof payload === 'string') return payload;
  if (payload.message) return payload.message;
  if (Array.isArray(payload.errors) && payload.errors.length) return payload.errors.join(', ');
  return fallback;
}

export async function apiRequest(path, options = {}, config = {}) {
  const { auth = true } = config;
  const token = getToken();

  if (auth && !token) {
    localStorage.removeItem(TOKEN_KEY);
    if (!window.location.pathname.endsWith('login.html')) {
      window.location.href = 'login.html';
    }
    throw new Error('Your session has expired. Please login again.');
  }

  const headers = {
    Accept: 'application/json',
    ...(options.headers || {})
  };

  const hasBody = options.body !== undefined && options.body !== null;
  const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData;
  if (hasBody && !isFormData && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }

  if (auth && token) {
    headers.Authorization = `Bearer ${token}`;
  }

  let response;
  try {
    response = await fetch(path, { ...options, headers });
  } catch {
    throw new Error('Network error. Please check your connection and try again.');
  }

  const contentType = response.headers.get('content-type') || '';
  const payload = contentType.includes('application/json')
    ? await response.json().catch(() => null)
    : await response.text().catch(() => '');

  if (!response.ok) {
    if (response.status === 401 && auth) {
      localStorage.removeItem(TOKEN_KEY);
      if (!window.location.pathname.endsWith('login.html')) {
        window.location.href = 'login.html';
      }
    }

    const fallback = response.status >= 500
      ? 'Server error. Please try again in a moment.'
      : 'Request failed. Please verify your input and retry.';
    throw new Error(normalizeErrorMessage(payload, fallback));
  }

  return payload;
}
