import { apiRequest } from './api.js';
import { openModal, showAlert } from './ui.js';
import { formatCurrency, monthName, escapeHtml } from './utils.js';

let selectedYear = new Date().getFullYear();

function normalizeYearPayload(payload) {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.months)) return payload.months;
  if (payload && typeof payload === 'object') {
    return Object.keys(payload).map((key) => ({
      month: Number(key),
      totalEmi: Number(payload[key] || 0)
    }));
  }
  return [];
}

function monthTotalMap(entries) {
  const map = {};
  entries.forEach((entry) => {
    const month = Number(entry.month || entry.monthNumber);
    if (month >= 1 && month <= 12) {
      map[month] = Number(entry.totalEmiAmount ?? entry.totalEmi ?? entry.total ?? 0);
    }
  });
  return map;
}

function renderYearGrid(totalByMonth) {
  const grid = document.getElementById('calendarGrid');
  grid.innerHTML = '';

  for (let month = 1; month <= 12; month += 1) {
    const total = totalByMonth[month] || 0;
    const tile = document.createElement('button');
    tile.type = 'button';
    tile.className = 'month-tile';
    tile.dataset.month = String(month);
    tile.innerHTML = `
      <div class="month-name">${monthName(month)}</div>
      <div class="month-amount">${formatCurrency(total)}</div>
    `;
    grid.appendChild(tile);
  }
}

async function loadYearCalendar() {
  const payload = await apiRequest(`/api/calendar/${selectedYear}`);
  const entries = normalizeYearPayload(payload);
  const totalByMonth = monthTotalMap(entries);
  renderYearGrid(totalByMonth);
  document.getElementById('calendarYear').textContent = String(selectedYear);
}

function normalizeMonthBreakdown(payload) {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.loans)) return payload.loans;
  return [];
}

function renderMonthModal(month, rows) {
  document.getElementById('monthModalTitle').textContent = `${monthName(month)} ${selectedYear}`;

  const tbody = document.getElementById('monthBreakdownBody');
  if (!rows.length) {
    tbody.innerHTML = '<tr><td colspan="2" class="muted">No EMI records for this month.</td></tr>';
    openModal('monthModal');
    return;
  }

  tbody.innerHTML = rows.map((row) => `
    <tr>
      <td>${escapeHtml(row.loanName || row.name || '-')}</td>
      <td>${formatCurrency(row.emiAmount ?? row.amount ?? 0)}</td>
    </tr>
  `).join('');

  openModal('monthModal');
}

async function loadMonthBreakdown(month) {
  const payload = await apiRequest(`/api/calendar/${selectedYear}/${month}`);
  const rows = normalizeMonthBreakdown(payload);
  renderMonthModal(month, rows);
}

async function safeLoadYear() {
  try {
    await loadYearCalendar();
  } catch (error) {
    showAlert(error.message, 'error');
  }
}

function bindCalendarEvents() {
  document.getElementById('prevYearBtn').addEventListener('click', async () => {
    selectedYear -= 1;
    await safeLoadYear();
  });

  document.getElementById('nextYearBtn').addEventListener('click', async () => {
    selectedYear += 1;
    await safeLoadYear();
  });

  document.getElementById('calendarGrid').addEventListener('click', async (event) => {
    const tile = event.target.closest('.month-tile');
    if (!tile) return;

    const month = Number(tile.dataset.month);
    if (!month) return;

    try {
      await loadMonthBreakdown(month);
    } catch (error) {
      showAlert(error.message, 'error');
    }
  });
}

export function initCalendar() {
  bindCalendarEvents();
  document.getElementById('calendarYear').textContent = String(selectedYear);

  return {
    loadYear: safeLoadYear
  };
}
