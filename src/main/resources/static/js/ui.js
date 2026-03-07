import { qs, qsa } from './utils.js';

let alertTimeout;

export function showAlert(message, type = 'info') {
  const container = qs('#alertContainer');
  if (!container) return;

  clearTimeout(alertTimeout);
  container.innerHTML = `<div class="alert ${type}">${message}</div>`;
  alertTimeout = setTimeout(() => {
    container.innerHTML = '';
  }, 3500);
}

export function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove('hidden');
}

export function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.add('hidden');
}

function setupModals() {
  qsa('[data-close-modal]').forEach((button) => {
    button.addEventListener('click', () => {
      closeModal(button.dataset.closeModal);
    });
  });

  qsa('.modal').forEach((modal) => {
    modal.addEventListener('click', (event) => {
      if (event.target === modal) modal.classList.add('hidden');
    });
  });
}

function setupSidebarToggle() {
  const toggle = qs('#sidebarToggle');
  if (!toggle) return;

  toggle.addEventListener('click', () => {
    document.body.classList.toggle('sidebar-open');
  });

  qsa('.nav-item').forEach((item) => {
    item.addEventListener('click', () => {
      document.body.classList.remove('sidebar-open');
    });
  });
}

export function bindAppNavigation(onChange) {
  const navItems = qsa('.nav-item[data-view]');
  const views = {
    loans: document.getElementById('loansSection'),
    calendar: document.getElementById('calendarSection')
  };

  navItems.forEach((button) => {
    button.addEventListener('click', async () => {
      const selected = button.dataset.view;
      navItems.forEach((item) => item.classList.remove('active'));
      button.classList.add('active');

      Object.keys(views).forEach((view) => {
        views[view].classList.toggle('hidden', view !== selected);
      });

      if (typeof onChange === 'function') await onChange(selected);
    });
  });
}

export function initUi() {
  setupModals();
  setupSidebarToggle();
}
