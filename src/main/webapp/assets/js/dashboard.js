// dashboard.js
document.addEventListener('DOMContentLoaded', () => {
    // Auth Check on load
    const userStr = sessionStorage.getItem('currentUser');
    if (!userStr) {
        window.location.replace('index.jsp');
        return;
    }
    
    // Valid object parsing
    let user;
    try {
        user = JSON.parse(userStr);
    } catch(e) {
        sessionStorage.removeItem('currentUser');
        window.location.replace('index.jsp');
        return;
    }

    const authUsername = document.getElementById('authUsername');
    const authRole = document.getElementById('authRole');
    
    if(authUsername) authUsername.textContent = user.username || user.name || 'User';
    if(authRole) authRole.textContent = user.userType || 'RECEPTIONIST';

    // Role specific UI updates
    // Assuming user returns 'Admin' or 'Reception'
    const role = (user.userType || '').toLowerCase();
    
    if (role !== 'ADMINISTRATOR') {
        const navUsers = document.getElementById('nav-users');
        if (navUsers) navUsers.style.display = 'none'; // Hide users nav for Reception
    }

    // Logout handling
    document.getElementById('logoutBtn').addEventListener('click', async () => {
        if(window.processLogout) { await window.processLogout(); return; }
        sessionStorage.removeItem('currentUser');
        window.location.replace('index.jsp');
    });

    // Navigation setup
    const navButtons = document.querySelectorAll('.nav-btn');
    const viewContainer = document.getElementById('viewContainer');
    const pageTitle = document.getElementById('pageTitle');

    navButtons.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            
            // Remove active classes
            document.querySelectorAll('.nav-btn').forEach(b => {
                b.classList.remove('bg-blue-50', 'text-blue-700');
                b.classList.add('text-gray-700');
            });

            // Add active class
            btn.classList.add('bg-blue-50', 'text-blue-700');
            btn.classList.remove('text-gray-700');

            const viewName = btn.getAttribute('data-view');
            pageTitle.textContent = btn.querySelector('span').textContent;
            
            loadView(viewName, role);
        });
    });

    // Time update loop
    setInterval(() => {
        const timeEl = document.getElementById('currentTime');
        if(timeEl) {
            timeEl.textContent = new Date().toLocaleTimeString();
        }
    }, 1000);

    // Initial load view
    loadView('dashboard', role);
});

// View Injection & Loader Helper
function showLoader(show) {
    const loader = document.getElementById('loader');
    if (loader) {
        if (show) loader.classList.remove('hidden');
        else loader.classList.add('hidden');
    }
}

async function loadView(view, role) {
    const container = document.getElementById('viewContainer');
    showLoader(true);
    
    try {
        // Simplified template injecting (No framework)
        let html = '';
        
        switch(view) {
            case 'dashboard':
                html = `
                    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 flex flex-col justify-center">
                            <h3 class="text-gray-500 text-sm font-medium mb-1">Total Rooms</h3>
                            <span class="text-3xl font-bold text-gray-900" id="dashTotalRooms">-</span>
                        </div>
                        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 flex flex-col justify-center">
                            <h3 class="text-gray-500 text-sm font-medium mb-1">Active Guests</h3>
                            <span class="text-3xl font-bold text-gray-900" id="dashActiveGuests">-</span>
                        </div>
                    </div>
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-8 text-center">
                        <h2 class="text-2xl font-bold text-gray-800 mb-2">Welcome to Ocean View Resort Administration!</h2>
                        <p class="text-gray-500">Navigate using the sidebar to manage your hotel entities.</p>
                        <i class="fa-solid fa-umbrella-beach text-blue-100 text-9xl mt-8"></i>
                    </div>
                `;
                break;
            case 'users':
                if (role !== 'ADMINISTRATOR') {
                     html = `<div class="p-8 text-center text-red-500">Access Denied.</div>`;
                     break;
                }
                html = `
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                        <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center bg-gray-50">
                            <h3 class="text-lg font-medium text-gray-900">System Users</h3>
                        </div>
                        <div class="p-0 overflow-x-auto">
                            <table class="w-full text-left text-sm text-gray-500">
                                <thead class="text-xs text-gray-700 uppercase bg-gray-100">
                                    <tr>
                                        <th class="px-6 py-3">ID</th>
                                        <th class="px-6 py-3">Username</th>
                                        <th class="px-6 py-3">Action</th>
                                    </tr>
                                </thead>
                                <tbody id="usersTableBody">
                                    <tr><td colspan="3" class="text-center py-4">Loading users...</td></tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                `;
                break;
            // Additional module cases built later
            default:
                html = `<div class="bg-white rounded-xl shadow-sm border border-gray-100 p-8 text-center text-gray-500">
                            <i class="fa-solid fa-person-digging text-4xl mb-4"></i><br>Module rendering under construction...
                        </div>`;
        }

        container.innerHTML = html;
        
        // Post rendering actions -> Fetch actual data and populate tables
        if (view === 'dashboard') {
             // Mockup fetch request for total rooms / guests
             try {
                const rooms = await fetchAPI('/rooms');
                if(rooms) document.getElementById('dashTotalRooms').textContent = rooms.length;
             } catch(e) {}
             try {
                const guests = await fetchAPI('/guests');
                if(guests) document.getElementById('dashActiveGuests').textContent = guests.length;
             } catch(e) {}
        }

        if (view === 'users') {
            try {
                const users = await fetchAPI('/users');
                const tb = document.getElementById('usersTableBody');
                if (users && users.length > 0) {
                    tb.innerHTML = users.map(u => `
                        <tr class="bg-white border-b hover:bg-gray-50">
                            <td class="px-6 py-4">${u.id || u.userId || '?'}</td>
                            <td class="px-6 py-4 font-medium text-gray-900">${u.username || 'System Admin'}</td>
                            <td class="px-6 py-4">
                               <span class="px-2 py-1 bg-green-100 text-green-800 rounded-lg text-xs font-medium cursor-pointer">Active</span>
                            </td>
                        </tr>
                    `).join('');
                } else {
                    tb.innerHTML = `<tr><td colspan="3" class="text-center py-4">No users found.</td></tr>`;
                }
            } catch (e) {
                document.getElementById('usersTableBody').innerHTML = `<tr><td colspan="3" class="text-center py-4 text-red-500">Error loading users.</td></tr>`;
            }
        }
        
    } catch (error) {
        console.error('Error loading view:', error);
        container.innerHTML = `<div class="p-4 text-red-500 text-center">Failed to load interface component.</div>`;
    } finally {
        showLoader(false);
    }
}
