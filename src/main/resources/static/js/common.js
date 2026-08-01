/* ==========================================================
   XO EVENT MANAGEMENT - COMMON JS UTILITIES
   ========================================================== */

const API_BASE = '/api';

// -------------------- Auth Storage Helpers --------------------
function saveAuth(data) {
  localStorage.setItem('xo_token', data.accessToken);
  localStorage.setItem('xo_user', JSON.stringify({
    id: data.userId, name: data.name, email: data.email, role: data.role
  }));
}

function getToken() { return localStorage.getItem('xo_token'); }

function getCurrentUser() {
  const raw = localStorage.getItem('xo_user');
  return raw ? JSON.parse(raw) : null;
}

function isLoggedIn() { return !!getToken(); }

function logout() {
  localStorage.removeItem('xo_token');
  localStorage.removeItem('xo_user');
  window.location.href = 'index.html';
}

// -------------------- API Fetch Wrapper --------------------
async function apiRequest(endpoint, method = 'GET', body = null, auth = true) {
  const headers = { 'Content-Type': 'application/json' };
  if (auth && getToken()) {
    headers['Authorization'] = 'Bearer ' + getToken();
  }

  const options = { method, headers };
  if (body) options.body = JSON.stringify(body);

  const response = await fetch(API_BASE + endpoint, options);
  const contentType = response.headers.get('content-type');
  const data = contentType && contentType.includes('application/json') ? await response.json() : null;

  if (!response.ok) {
    const message = (data && data.message) ? data.message : 'Something went wrong. Please try again.';
    throw new Error(message);
  }
  return data;
}

// -------------------- Navbar Renderer --------------------
function renderNavbar(activePage = '') {
  const navEl = document.getElementById('xo-navbar');
  if (!navEl) return;

  const user = getCurrentUser();
  let links = `
    <a href="index.html">Home</a>
    <a href="events.html">Events</a>
  `;

  if (user) {
    links += `<a href="my-bookings.html">My Bookings</a>`;
    if (user.role === 'ORGANIZER') {
      links += `<a href="organizer-dashboard.html">My Events</a>`;
    }
    if (user.role === 'ADMIN') {
      links += `<a href="admin-dashboard.html">Admin Panel</a>`;
    }
    links += `<span style="color:#fff;font-weight:600;">Hi, ${user.name.split(' ')[0]}</span>`;
    links += `<button class="btn btn-outline" onclick="logout()">Logout</button>`;
  } else {
    links += `<a href="login.html">Login</a>`;
    links += `<a href="register.html" class="btn btn-primary">Sign Up</a>`;
  }

  navEl.innerHTML = `
    <a href="index.html" class="brand">XO <span>Events</span></a>
    <div class="navbar-links">${links}</div>
  `;
}

// -------------------- Helpers --------------------
function formatDate(dateStr) {
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { day: 'numeric', month: 'short', year: 'numeric' });
}

function formatCurrency(amount) {
  return '₹' + Number(amount).toLocaleString('en-IN');
}

function statusBadge(status) {
  const map = {
    UPCOMING: 'badge-upcoming',
    ONGOING: 'badge-ongoing',
    COMPLETED: 'badge-completed',
    CANCELLED: 'badge-cancelled'
  };
  return `<span class="badge ${map[status] || ''}">${status}</span>`;
}

function requireAuth(allowedRoles = []) {
  const user = getCurrentUser();
  if (!user) {
    window.location.href = 'login.html';
    return null;
  }
  if (allowedRoles.length && !allowedRoles.includes(user.role)) {
    alert('You are not authorized to view this page.');
    window.location.href = 'index.html';
    return null;
  }
  return user;
}

document.addEventListener('DOMContentLoaded', () => renderNavbar());
