﻿import { apiRequest, TOKEN_KEY, getToken } from './api.js';
import { showAlert } from './ui.js';

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function requireAuth() {
  if (!getToken()) {
    window.location.replace('login.html');
  }
}

export async function login(phoneNumber, password) {
  return apiRequest('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ phoneNumber, password })
  }, { auth: false });
}

export async function registerUser(payload) {
  return apiRequest('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload)
  }, { auth: false });
}

export async function loadCurrentUser() {
  try {
    return await apiRequest('/api/users/me');
  } catch {
    return null;
  }
}

export function logout() {
  clearToken();
  window.location.replace('login.html');
}

export function bindAuthGuard() {
  if (getToken()) {
    window.location.replace('app.html');
  }
}

function initPasswordToggles(root) {
  if (!root) return;
  root.querySelectorAll('[data-toggle-password]').forEach((button) => {
    button.addEventListener('click', () => {
      const targetId = button.dataset.target;
      const input = document.getElementById(targetId);
      if (!input) return;

      const isHidden = input.type === 'password';
      input.type = isHidden ? 'text' : 'password';
      button.textContent = isHidden ? 'Hide' : 'Show';
    });
  });
}

export function handleLoginPage() {
  bindAuthGuard();

  const form = document.getElementById('loginForm');
  const button = document.getElementById('loginBtn');
  initPasswordToggles(form);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const phoneNumber = form.phoneNumber.value.trim();
    const password = form.password.value;

    if (!phoneNumber || !password) {
      showAlert('Please enter both phone number and password.', 'error');
      return;
    }

    button.disabled = true;
    try {
      const result = await login(phoneNumber, password);
      if (!result || !result.accessToken) {
        throw new Error('Login failed. Invalid server response.');
      }
      setToken(result.accessToken);
      window.location.replace('app.html');
    } catch (error) {
      showAlert(error.message, 'error');
    } finally {
      button.disabled = false;
    }
  });
}

export function handleRegisterPage() {
  bindAuthGuard();

  const form = document.getElementById('registerForm');
  const button = document.getElementById('registerBtn');
  initPasswordToggles(form);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const payload = {
      name: form.name.value.trim(),
      phoneNumber: form.phoneNumber.value.trim(),
      password: form.password.value
    };

    if (!payload.name || !payload.phoneNumber || !payload.password) {
      showAlert('Please fill all fields before continuing.', 'error');
      return;
    }

    button.disabled = true;
    try {
      await registerUser(payload);
      showAlert('Registration successful. Redirecting to login...', 'success');
      setTimeout(() => window.location.replace('login.html'), 700);
    } catch (error) {
      showAlert(error.message, 'error');
    } finally {
      button.disabled = false;
    }
  });
}
