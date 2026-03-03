// auth.js

// If user is already authenticated, redirect away from login page
function redirectIfLoggedIn() {
    const stored = sessionStorage.getItem('currentUser');
    if (!stored) return;
    try {
        const user = JSON.parse(stored);
        if (user && user.active === true) {
            // Active session found — send them straight to the dashboard
            window.location.replace('dashboard.jsp');
        } else if (user && user.active === false) {
            // Session exists but account was deactivated — clear stale data
            sessionStorage.removeItem('currentUser');
        }
    } catch (e) {
        // Corrupt storage entry — clear it
        sessionStorage.removeItem('currentUser');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // Block the login page for already authenticated users
    redirectIfLoggedIn();
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
                // Fetch all users to check active status pre-flight
                const base = (window.APP_CONTEXT || '') + '/api';
                const usersRes = await fetch(base + '/users/');
                if (usersRes.ok) {
                    const usersList = await usersRes.json();
                    
                    // Case-insensitive user find
                    const userMatch = usersList.find(u => u.userName.toLowerCase() === username.toLowerCase());
                    
                    if (userMatch && userMatch.active === false) {
                        errorMessage.innerHTML = `
                            <strong>Account Deactivated</strong><br>
                            Your account has been deactivated. Please contact the administrator for assistance.`;
                        errorMessage.classList.remove('hidden');
                        loginBtn.classList.remove('opacity-75', 'cursor-not-allowed');
                        loadingIcon.classList.add('hidden');
                        return;
                    }
                }
            } catch (e) {
                // ignore, let login failure happen naturally
                console.warn("Preflight user check failed", e);
            }
            
            try {
                const userData = await loginWithSession(username, password);

                // As a fallback 
                if (userData.active === false) {
                    errorMessage.innerHTML = `
                        <strong>Account Deactivated</strong><br>
                        Your account has been deactivated. Please contact the administrator for assistance.`;
                    errorMessage.classList.remove('hidden');
                    return;
                }

                sessionStorage.setItem('currentUser', JSON.stringify(userData));
                window.location.replace('dashboard.jsp');
            } catch (error) {
                console.error('Login error:', error);
                errorMessage.textContent = 'Invalid Username or Password'; // Mask technical msg
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

