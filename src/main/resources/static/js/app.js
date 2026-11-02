const $ = id => document.getElementById(id);
const esc = s => String(s ?? '').replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));
const PRI = { NORMAL: 'secondary', HIGHER: 'warning', CRITICAL: 'danger' };
const STEPS = ['SUBMITTED', 'VERIFIED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED'];

async function api(url, opts = {}) {
    const r = await fetch(url, opts);
    let data = null;
    try { data = await r.json(); } catch (e) { }
    if (!r.ok) {
        const err = new Error((data && data.error) || 'Request failed (' + r.status + ')');
        err.status = r.status;
        throw err;
    }
    return data;
}

async function initNav() {
    const me = await api('/api/auth/me');
    let links = `<a class="nav-link" href="index.html">Report Issue</a>
               <a class="nav-link" href="track.html">Track Complaint</a>`;
    if (me.loggedIn) {
        links += `<a class="nav-link" href="my.html">My Complaints</a>`;
        if (me.role === 'ADMIN') links += `<a class="nav-link" href="admin.html">Admin Dashboard</a>`;
        links += `<span class="navbar-text text-white-50 mx-2">Hi, ${esc(me.name)}</span>
              <a class="nav-link" href="#" onclick="logout();return false;">Logout</a>`;
    } else {
        links += `<a class="nav-link" href="login.html">Login</a>
              <a class="nav-link" href="register.html">Register</a>`;
    }
    $('nav').innerHTML = `<nav class="navbar navbar-expand-md navbar-dark bg-dark">
    <div class="container">
      <a class="navbar-brand fw-bold" href="index.html">🏙️ CivicFix</a>
      <div class="navbar-nav ms-auto flex-row flex-wrap align-items-center gap-2">${links}</div>
    </div></nav>`;
    return me;
}

async function logout() {
    await api('/api/auth/logout', { method: 'POST' });
    location.href = 'login.html';
}