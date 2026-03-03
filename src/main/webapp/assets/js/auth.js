// auth.js
document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const errorMessage = document.getElementById('errorMessage');
    const loginBtn = document.getElementById('loginBtn');
    const loadingIcon = document.getElementById('loadingIcon');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;

            loginBtn.classList.add('opacity-75', 'cursor-not-allowed');
            loadingIcon.classList.remove('hidden');
            errorMessage.classList.add('hidden');
            try {
                const userData = await loginWithSession(username, password);
                sessionStorage.setItem('currentUser', JSON.stringify(userData));

                window.location.replace('dashboard.jsp');
            } catch (error) {
                console.error('Login error:', error);
                errorMessage.textContent = error.message || 'Invalid username or password.';
                errorMessage.classList.remove('hidden');
            } finally {
                loginBtn.classList.remove('opacity-75', 'cursor-not-allowed');
                loadingIcon.classList.add('hidden');
            }
        });
    }
});

// Session based login (Servlet friendly)
async function loginWithSession(username, password) {
    const base = (window.APP_CONTEXT || '') + '/api';
    const res = await fetch(base + '/users/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ userName: username, password })
    });

    if (!res.ok) {
        let msg = `HTTP ${res.status}`;
        try {
            const data = await res.json();
            msg = data.error || data.message || msg;
        } catch (e) {
            const text = await res.text();
            if (text) msg = text;
        }
        throw new Error(msg);
    }

    return await res.json();
}

// Guard (session-based)
async function requireAuth() {
    const base = (window.APP_CONTEXT || '') + '/api';
    const res = await fetch(base + '/auth/me', { credentials: 'include' });

    if (!res.ok) {
        window.location.replace('index.jsp');
        return null;
    }
    return await res.json();
}

async function processLogout() {
    const base = (window.APP_CONTEXT || '') + '/api';
    await fetch(base + '/users/logout', { method: 'POST', credentials: 'include' }).catch(() => {});
    sessionStorage.removeItem('currentUser');
    window.location.replace('index.jsp');
}

