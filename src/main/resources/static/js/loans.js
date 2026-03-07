import { apiRequest } from './api.js';
import { closeModal, openModal, showAlert } from './ui.js';
import { formatCurrency, formatDate, formatDateTime, toDateInput, escapeHtml, isClosedStatus } from './utils.js';

let loansCache = [];

function getLoanId(loan) {
  return loan.id ?? loan.loanId;
}

function asLoanPayload(form) {
  return {
    loanName: form.loanName.value.trim(),
    providerName: form.providerName.value.trim(),
    emiAmount: Number(form.emiAmount.value),
    startDate: form.startDate.value,
    tenureMonths: Number(form.tenureMonths.value)
  };
}

function validateLoanPayload(payload) {
  return payload.loanName
    && payload.providerName
    && payload.startDate
    && Number.isFinite(payload.emiAmount)
    && payload.emiAmount > 0
    && Number.isInteger(payload.tenureMonths)
    && payload.tenureMonths > 0;
}

function renderLoansTable(loans) {
  const tbody = document.getElementById('loansTableBody');
  if (!tbody) return;

  tbody.innerHTML = '';

  if (!loans.length) {
    tbody.innerHTML = '<tr><td colspan="7" class="muted">No loans added yet.</td></tr>';
    return;
  }

  tbody.innerHTML = loans.map((loan) => {
    const loanId = getLoanId(loan);
    const closed = isClosedStatus(loan.status);
    const status = closed ? 'Closed' : (loan.status || 'Active');
    const statusClass = closed ? 'closed' : 'active';

    return `
      <tr>
        <td>${escapeHtml(loan.loanName || '-')}</td>
        <td>${escapeHtml(loan.providerName || '-')}</td>
        <td>${formatCurrency(loan.emiAmount)}</td>
        <td>${formatDate(loan.startDate)}</td>
        <td>${Number(loan.tenureMonths || 0)} months</td>
        <td><span class="chip ${statusClass}">${escapeHtml(status)}</span></td>
        <td>
          <button class="btn btn-light" data-action="view" data-id="${loanId}" type="button">View Details</button>
        </td>
      </tr>
    `;
  }).join('');
}

function fillLoanForm(loan) {
  const form = document.getElementById('loanForm');
  document.getElementById('loanId').value = getLoanId(loan);
  form.loanName.value = loan.loanName || '';
  form.providerName.value = loan.providerName || '';
  form.emiAmount.value = loan.emiAmount || '';
  form.startDate.value = toDateInput(loan.startDate);
  form.tenureMonths.value = loan.tenureMonths || '';
}

function resetLoanForm() {
  const form = document.getElementById('loanForm');
  form.reset();
  document.getElementById('loanId').value = '';
}

async function createLoan(payload) {
  return apiRequest('/api/loans', { method: 'POST', body: JSON.stringify(payload) });
}

async function updateLoan(id, payload) {
  return apiRequest(`/api/loans/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
}

async function removeLoan(id) {
  return apiRequest(`/api/loans/${id}`, { method: 'DELETE' });
}

async function closeLoan(id, closedDate) {
  return apiRequest(`/api/loans/${id}/close`, {
    method: 'PATCH',
    body: JSON.stringify({ closedDate })
  });
}

async function fetchLoanById(loanId) {
  return apiRequest(`/api/loans/${loanId}`);
}

async function loadLoans() {
  const response = await apiRequest('/api/loans');
  loansCache = Array.isArray(response) ? response : (response?.content || []);
  renderLoansTable(loansCache);
}

function openCreateModal() {
  resetLoanForm();
  document.getElementById('loanModalTitle').textContent = 'Add Loan';
  openModal('loanModal');
}

function openEditModal(loan) {
  document.getElementById('loanModalTitle').textContent = 'Edit Loan';
  fillLoanForm(loan);
  openModal('loanModal');
}

function openCloseModal(loanId) {
  document.getElementById('closeLoanId').value = loanId;
  document.getElementById('closedDate').value = toDateInput(new Date());
  openModal('closeLoanModal');
}

function bindLoanForms(refreshFn) {
  const loanForm = document.getElementById('loanForm');
  const closeForm = document.getElementById('closeLoanForm');

  if (loanForm && !loanForm.dataset.bound) {
    loanForm.dataset.bound = 'true';
    loanForm.addEventListener('submit', async (event) => {
      event.preventDefault();
      const form = event.currentTarget;
      const loanId = document.getElementById('loanId').value;
      const payload = asLoanPayload(form);

      if (!validateLoanPayload(payload)) {
        showAlert('Please fill valid values for all loan fields.', 'error');
        return;
      }

      const submitBtn = document.getElementById('loanSubmitBtn');
      submitBtn.disabled = true;

      try {
        if (loanId) {
          await updateLoan(loanId, payload);
          showAlert('Loan updated successfully.', 'success');
        } else {
          await createLoan(payload);
          showAlert('Loan created successfully.', 'success');
        }
        closeModal('loanModal');
        await refreshFn();
      } catch (error) {
        showAlert(error.message, 'error');
      } finally {
        submitBtn.disabled = false;
      }
    });
  }

  if (closeForm && !closeForm.dataset.bound) {
    closeForm.dataset.bound = 'true';
    closeForm.addEventListener('submit', async (event) => {
      event.preventDefault();
      const loanId = document.getElementById('closeLoanId').value;
      const closedDate = document.getElementById('closedDate').value;
      if (!closedDate) {
        showAlert('Please choose a close date.', 'error');
        return;
      }

      const submitBtn = document.getElementById('closeLoanSubmitBtn');
      submitBtn.disabled = true;

      try {
        await closeLoan(loanId, closedDate);
        closeModal('closeLoanModal');
        showAlert('Loan closed successfully.', 'success');
        await refreshFn();
      } catch (error) {
        showAlert(error.message, 'error');
      } finally {
        submitBtn.disabled = false;
      }
    });
  }
}

function renderDetailsItem(id, value) {
  const node = document.getElementById(id);
  if (node) {
    node.textContent = value;
  }
}

function renderLoanDetails(loan) {
  const closed = isClosedStatus(loan.status);
  renderDetailsItem('detailLoanName', loan.loanName || '-');
  renderDetailsItem('detailProviderName', loan.providerName || '-');
  renderDetailsItem('detailEmiAmount', formatCurrency(loan.emiAmount));
  renderDetailsItem('detailStartDate', formatDate(loan.startDate));
  renderDetailsItem('detailTenureMonths', `${Number(loan.tenureMonths || 0)} months`);
  renderDetailsItem('detailEndDate', formatDate(loan.endDate));
  renderDetailsItem('detailClosedDate', formatDate(loan.closedDate));
  renderDetailsItem('detailStatus', closed ? 'Closed' : (loan.status || 'Active'));
  renderDetailsItem('detailCreatedAt', formatDateTime(loan.createdAt));
  renderDetailsItem('detailUpdatedAt', formatDateTime(loan.updatedAt));

  const closeBtn = document.getElementById('detailCloseBtn');
  if (closeBtn) {
    closeBtn.disabled = closed;
  }
}

function navigateToDetails(loanId) {
  window.location.href = `loan-details.html?loanId=${encodeURIComponent(loanId)}`;
}

export function initLoans() {
  const addButton = document.getElementById('addLoanBtn');
  if (addButton) {
    addButton.addEventListener('click', openCreateModal);
  }

  const table = document.getElementById('loansTableBody');
  if (table) {
    table.addEventListener('click', (event) => {
      const button = event.target.closest('button[data-action]');
      if (!button) return;
      if (button.dataset.action === 'view') {
        navigateToDetails(button.dataset.id);
      }
    });
  }

  bindLoanForms(loadLoans);

  return {
    load: async () => {
      try {
        await loadLoans();
      } catch (error) {
        showAlert(error.message, 'error');
      }
    }
  };
}

export function initLoanDetailsPage() {
  const params = new URLSearchParams(window.location.search);
  const loanId = params.get('loanId');

  if (!loanId) {
    showAlert('Loan not found.', 'error');
    return;
  }

  async function loadDetails() {
    const loan = await fetchLoanById(loanId);
    renderLoanDetails(loan);
    document.getElementById('loanId').value = getLoanId(loan);
  }

  bindLoanForms(loadDetails);

  document.getElementById('backToLoansBtn').addEventListener('click', () => {
    window.location.href = 'app.html';
  });

  document.getElementById('detailEditBtn').addEventListener('click', async () => {
    try {
      const loan = await fetchLoanById(loanId);
      openEditModal(loan);
    } catch (error) {
      showAlert(error.message, 'error');
    }
  });

  document.getElementById('detailCloseBtn').addEventListener('click', () => {
    openCloseModal(loanId);
  });

  document.getElementById('detailDeleteBtn').addEventListener('click', async () => {
    if (!window.confirm('Delete this loan permanently?')) return;
    try {
      await removeLoan(loanId);
      showAlert('Loan deleted successfully.', 'success');
      setTimeout(() => {
        window.location.href = 'app.html';
      }, 450);
    } catch (error) {
      showAlert(error.message, 'error');
    }
  });

  loadDetails().catch((error) => showAlert(error.message, 'error'));
}
