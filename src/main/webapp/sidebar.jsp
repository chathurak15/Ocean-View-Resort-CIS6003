<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- Sidebar -->
<aside class="w-64 bg-white/95 border-r border-gray-200 shadow-sm flex-shrink-0 flex flex-col transition-all duration-300">
    <!-- Logo -->
    <div class="h-16 flex items-center px-6 border-b border-gray-100">
        <div class="flex items-center gap-3">
            <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center text-white">
                <i class="fa-solid fa-umbrella-beach"></i>
            </div>
            <span class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-700 to-blue-500">OceanView</span>
        </div>
    </div>

    <!-- Navigation Menu -->
    <nav class="flex-1 p-4 space-y-1 overflow-y-auto" id="sidebarNav">
        
        <a href="dashboard.jsp" id="nav-dashboard" class="flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-lg text-gray-700 hover:text-blue-700 hover:bg-blue-50 transition-colors">
            <i class="fa-solid fa-chart-line w-5 text-center"></i>
            <span>Overview</span>
        </a>

        <!-- Users (Admin Only) -->
        <a href="dashboard.jsp?view=users" id="nav-users" class="flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-lg text-gray-700 hover:text-blue-700 hover:bg-blue-50 transition-colors">
            <i class="fa-solid fa-users w-5 text-center"></i>
            <span>Users Panel</span>
        </a>

        <a href="rooms.jsp" id="nav-rooms" class="flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-lg text-gray-700 hover:text-blue-700 hover:bg-blue-50 transition-colors">
            <i class="fa-solid fa-bed w-5 text-center"></i>
            <span>Rooms</span>
        </a>

        <a href="dashboard.jsp?view=guests" id="nav-guests" class="flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-lg text-gray-700 hover:text-blue-700 hover:bg-blue-50 transition-colors">
            <i class="fa-solid fa-address-card w-5 text-center"></i>
            <span>Guests</span>
        </a>

        <a href="dashboard.jsp?view=reservations" id="nav-reservations" class="flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-lg text-gray-700 hover:text-blue-700 hover:bg-blue-50 transition-colors">
            <i class="fa-solid fa-calendar-check w-5 text-center"></i>
            <span>Reservations</span>
        </a>
        
    </nav>

    <!-- User Profile Area -->
    <div class="p-4 border-t border-gray-100">
        <div class="flex items-center gap-3 mb-4 px-2">
            <div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center border-2 border-white shadow-sm">
                <i class="fa-solid fa-user text-gray-500"></i>
            </div>
            <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-gray-900 truncate" id="authUsername">Loading...</p>
                <p class="text-xs text-gray-500 truncate" id="authRole">Loading role...</p>
            </div>
        </div>
        <button id="logoutBtn" class="w-full flex items-center justify-center gap-2 px-4 py-2 text-sm font-medium text-red-600 bg-red-50 hover:bg-red-100 rounded-lg transition-colors">
            <i class="fa-solid fa-arrow-right-from-bracket"></i>
            <span>Logout</span>
        </button>
    </div>
</aside>
<script>
    // Set active link based on current URL path
    document.addEventListener("DOMContentLoaded", () => {
        const path = window.location.pathname;
        const links = {
            'dashboard.jsp': 'nav-dashboard',
            'rooms.jsp': 'nav-rooms',
            // Simple query logic as fallback for SPAs if used
        };

        const page = path.split('/').pop();
        if(links[page]) {
            const activeLink = document.getElementById(links[page]);
            if (activeLink) {
                 activeLink.classList.remove('text-gray-700');
                 activeLink.classList.add('bg-blue-50', 'text-blue-700');
            }
        }
    });

    // Handle logout gloablly since it's on the sidebar
    document.getElementById('logoutBtn').addEventListener('click', async () => {
        if(window.processLogout) { await window.processLogout(); return; }
        sessionStorage.removeItem('currentUser');
        window.location.replace('index.jsp');
    });

    // Auth profile display
    const userStr = sessionStorage.getItem('currentUser');
    if (!userStr) {
        window.location.replace('index.jsp'); // Must log in
    } else {
        try {
            const user = JSON.parse(userStr);
            document.getElementById('authUsername').textContent = user.username || user.name || 'User';
            document.getElementById('authRole').textContent = user.userType || 'RECEPTIONIST';
            
            // Hide users panel for reception
            if ((user.userType || '').toLowerCase() !== 'administrator') {
                const navUsers = document.getElementById('nav-users');
                if (navUsers) navUsers.style.display = 'none';
            }
        } catch(e) {
            sessionStorage.removeItem('currentUser');
            window.location.replace('index.jsp');
        }
    }
</script>
