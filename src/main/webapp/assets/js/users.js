const UsersModule = {

    init: async () => {
        const container = document.getElementById('viewContainer');

        // Guard: administrator only
        const curUserStr = sessionStorage.getItem('currentUser');
        const curUser = curUserStr ? JSON.parse(curUserStr) : {};
        if ((curUser.userType || '').toUpperCase() !== 'ADMINISTRATOR') {
            container.innerHTML = `
                <div class="flex flex-col items-center justify-center py-24 text-center">
                    <i class="fa-solid fa-lock text-5xl text-red-300 mb-4"></i>
                    <h2 class="text-xl font-bold text-red-600">Access Denied</h2>
                    <p class="text-gray-500 mt-1">This section is restricted to Administrators only.</p>
                </div>`;
            return;
        }

        UsersModule.renderLayout(container);
        await UsersModule.loadUsers();
    },

    renderLayout: (container) => {
        container.innerHTML = `
            <div class="space-y-6 animate-fade-in">

                <!-- Header -->
                <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <div>
                        <h2 class="text-2xl font-bold text-gray-900">User Management</h2>
                        <p class="text-sm text-gray-500 mt-1">Manage system users, access, and account status.</p>
                    </div>
                    <button onclick="UsersModule.showAddModal()"
                        class="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-lg text-sm font-semibold shadow-sm transition-all focus:ring-2 focus:ring-blue-500 focus:ring-offset-1 flex items-center gap-2">
                        <i class="fa-solid fa-user-plus"></i> Add Receptionist
                    </button>
                </div>

                <!-- Stats bar -->
                <div class="grid grid-cols-2 sm:grid-cols-3 gap-4">
                    <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex items-center gap-3">
                        <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
                            <i class="fa-solid fa-users text-blue-600"></i>
                        </div>
                        <div>
                            <p class="text-xs text-gray-500">Total Users</p>
                            <p class="text-xl font-bold text-gray-900" id="statTotal">—</p>
                        </div>
                    </div>
                    <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex items-center gap-3">
                        <div class="w-10 h-10 rounded-full bg-emerald-100 flex items-center justify-center">
                            <i class="fa-solid fa-circle-check text-emerald-600"></i>
                        </div>
                        <div>
                            <p class="text-xs text-gray-500">Active</p>
                            <p class="text-xl font-bold text-gray-900" id="statActive">—</p>
                        </div>
                    </div>
                    <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex items-center gap-3">
                        <div class="w-10 h-10 rounded-full bg-rose-100 flex items-center justify-center">
                            <i class="fa-solid fa-circle-xmark text-rose-600"></i>
                        </div>
                        <div>
                            <p class="text-xs text-gray-500">Inactive</p>
                            <p class="text-xl font-bold text-gray-900" id="statInactive">—</p>
                        </div>
                    </div>
                </div>

                <!-- Table -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                    <div class="overflow-x-auto">
                        <table class="w-full text-left text-sm text-gray-600">
                            <thead class="text-xs text-gray-700 uppercase bg-gray-50/80 border-b border-gray-200">
                                <tr>
                                    <th class="px-6 py-4 font-semibold">ID</th>
                                    <th class="px-6 py-4 font-semibold">Name</th>
                                    <th class="px-6 py-4 font-semibold">Username</th>
                                    <th class="px-6 py-4 font-semibold">Role</th>
                                    <th class="px-6 py-4 font-semibold text-center">Status</th>
                                    <th class="px-6 py-4 font-semibold text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody id="usersTableBody" class="divide-y divide-gray-100">
                                <tr><td colspan="6" class="px-6 py-8 text-center">
                                    <div class="animate-pulse flex flex-col items-center">
                                        <div class="h-8 w-8 rounded-full border-4 border-gray-200 border-t-blue-500 animate-spin mb-3"></div>
                                        <div class="text-gray-400 font-medium">Loading users...</div>
                                    </div>
                                </td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- Modal -->
            <div id="userModal" class="hidden fixed inset-0 bg-gray-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4 opacity-0 transition-opacity duration-300">
                <div class="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden transform scale-95 transition-transform duration-300" id="userModalContent"></div>
            </div>
        `;
    },

    loadUsers: async () => {
        if (typeof showLoader === 'function') showLoader(true);
        try {
            const users = await fetchAPI('/users/');
            UsersModule.renderTable(users);
            UsersModule.updateStats(users);
        } catch (error) {
            UsersModule.renderError(error.message);
        } finally {
            if (typeof showLoader === 'function') showLoader(false);
        }
    },

    updateStats: (users) => {
        if (!users) return;
        const active = users.filter(u => u.active === true || u.isActive === true).length;
        const el = (id, val) => { const e = document.getElementById(id); if (e) e.textContent = val; };
        el('statTotal', users.length);
        el('statActive', active);
        el('statInactive', users.length - active);
    },

    renderTable: (users) => {
        const tbody = document.getElementById('usersTableBody');
        if (!users || users.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="px-6 py-12 text-center text-gray-500">
                <i class="fa-solid fa-inbox text-4xl mb-3 text-gray-300 block"></i> No users found.</td></tr>`;
            return;
        }

        const curUserStr = sessionStorage.getItem('currentUser');
        const curUser = curUserStr ? JSON.parse(curUserStr) : {};
        const curUserId = curUser.userId;

        tbody.innerHTML = users.map(u => {
            const uid = u.userId;
            const isActive = u.active === true || u.isActive === true;
            const isSelf = uid === curUserId;
            const roleLabel = (u.userType || 'UNKNOWN');
            const roleBadge = roleLabel === 'ADMINISTRATOR'
                ? 'bg-purple-100 text-purple-700 border border-purple-200'
                : 'bg-blue-100 text-blue-700 border border-blue-200';

            return `
            <tr class="bg-white hover:bg-blue-50/30 transition-colors group">
                <td class="px-6 py-4 font-semibold text-gray-900 bg-gray-50/50 group-hover:bg-transparent">#${uid}</td>
                <td class="px-6 py-4">
                    <div class="font-medium text-gray-900">${u.name || '—'}</div>
                </td>
                <td class="px-6 py-4 text-gray-600 font-mono text-xs">${u.userName || u.username || '—'}</td>
                <td class="px-6 py-4">
                    <span class="px-2.5 py-1 text-xs font-semibold rounded-full ${roleBadge}">
                        ${roleLabel}
                    </span>
                </td>
                <td class="px-6 py-4 text-center">
                    <span class="px-3 py-1 text-xs font-semibold rounded-full ${isActive
                        ? 'bg-emerald-100 text-emerald-700 border border-emerald-200'
                        : 'bg-rose-100 text-rose-700 border border-rose-200'}">
                        ${isActive
                        ? '<i class="fa-solid fa-check-circle mr-1"></i> Active'
                        : '<i class="fa-solid fa-xmark-circle mr-1"></i> Inactive'}
                    </span>
                </td>
                <td class="px-6 py-4 text-right space-x-1 whitespace-nowrap">
                    ${!isSelf ? `
                    <button onclick="UsersModule.toggleStatus(${uid}, ${isActive})"
                        class="p-2 rounded-lg text-gray-500 hover:text-amber-600 hover:bg-amber-50 transition-colors"
                        title="${isActive ? 'Deactivate' : 'Activate'} User">
                        <i class="fa-solid fa-power-off"></i>
                    </button>
                    <button onclick="UsersModule.deleteUser(${uid}, '${u.userName || ''}')"
                        class="p-2 rounded-lg text-gray-500 hover:text-red-600 hover:bg-red-50 transition-colors"
                        title="Delete User">
                        <i class="fa-solid fa-trash-can"></i>
                    </button>
                    ` : `
                    <span class="text-xs text-gray-400 italic pr-2">You</span>
                    `}
                </td>
            </tr>`;
        }).join('');
    },

    renderError: (msg) => {
        const tbody = document.getElementById('usersTableBody');
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="6" class="px-6 py-12 text-center text-red-500 bg-red-50/50">
                <i class="fa-solid fa-triangle-exclamation text-3xl mb-3 block opacity-70"></i>
                <strong>Error:</strong> ${msg}</td></tr>`;
        }
    },

    showAddModal: () => {
        const modal = document.getElementById('userModal');
        const modalContent = document.getElementById('userModalContent');

        modalContent.innerHTML = `
            <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/80">
                <h3 class="text-lg font-bold text-gray-900">
                    <i class="fa-solid fa-user-plus text-blue-600 mr-2"></i>Add New Receptionist
                </h3>
                <button onclick="UsersModule.closeModal()" class="text-gray-400 hover:text-gray-600 transition-colors p-1 rounded-md hover:bg-gray-200">
                    <i class="fa-solid fa-xmark text-lg"></i>
                </button>
            </div>
            <form onsubmit="UsersModule.addUser(event)" class="p-6 space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Full Name <span class="text-red-500">*</span></label>
                    <input type="text" id="newUserName" required placeholder="e.g. Jane Smith"
                        class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all outline-none">
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Username <span class="text-red-500">*</span></label>
                    <input type="text" id="newUserUsername" required placeholder="e.g. jsmith"
                        class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all outline-none">
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Password <span class="text-red-500">*</span></label>
                    <input type="password" id="newUserPassword" required placeholder="Min. 6 characters"
                        class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all outline-none">
                </div>
                <div class="bg-blue-50 border border-blue-200 rounded-lg px-4 py-3 flex items-center gap-2 text-sm text-blue-700">
                    <i class="fa-solid fa-info-circle"></i>
                    <span>New accounts are always created as <strong>Receptionist</strong> and set to <strong>Active</strong>.</span>
                </div>
                <div id="addUserError" class="hidden bg-red-50 border border-red-200 rounded-lg px-4 py-3 text-sm text-red-600"></div>
                <div class="pt-4 flex gap-3 justify-end items-center border-t border-gray-100 mt-2">
                    <button type="button" onclick="UsersModule.closeModal()"
                        class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
                        Cancel
                    </button>
                    <button type="submit" id="addUserBtn"
                        class="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 transition-colors shadow-sm flex items-center gap-2">
                        <i class="fa-solid fa-user-plus"></i> Create Account
                    </button>
                </div>
            </form>
        `;

        modal.classList.remove('hidden');
        setTimeout(() => {
            modal.classList.remove('opacity-0');
            document.getElementById('userModalContent').classList.remove('scale-95');
        }, 10);
    },

    closeModal: () => {
        const modal = document.getElementById('userModal');
        const modalContent = document.getElementById('userModalContent');
        modal.classList.add('opacity-0');
        if (modalContent) modalContent.classList.add('scale-95');
        setTimeout(() => modal.classList.add('hidden'), 300);
    },

    addUser: async (e) => {
        e.preventDefault();

        const name      = document.getElementById('newUserName').value.trim();
        const userName  = document.getElementById('newUserUsername').value.trim();
        const password  = document.getElementById('newUserPassword').value;
        const errEl     = document.getElementById('addUserError');
        const btn       = document.getElementById('addUserBtn');

        if (password.length < 6) {
            errEl.textContent = 'Password must be at least 6 characters.';
            errEl.classList.remove('hidden');
            return;
        }

        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Creating...';
        errEl.classList.add('hidden');

        const payload = {
            name,
            userName,
            password,
            userType: 'RECEPTIONIST'
        };

        try {
            await fetchAPI('/users/register', 'POST', payload);
            UsersModule.closeModal();
            await UsersModule.loadUsers();
        } catch (error) {
            errEl.textContent = error.message || 'Failed to create user.';
            errEl.classList.remove('hidden');
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-user-plus"></i> Create Account';
        }
    },

    toggleStatus: async (id, currentActive) => {
        const action = currentActive ? 'deactivate' : 'activate';
        if (!confirm(`Are you sure you want to ${action} User #${id}?`)) return;

        if (typeof showLoader === 'function') showLoader(true);
        try {
            await fetchAPI(`/users/${id}/status`, 'PUT', { active: !currentActive });
            await UsersModule.loadUsers();
        } catch (e) {
            alert('Status update failed: ' + e.message);
            if (typeof showLoader === 'function') showLoader(false);
        }
    },

    deleteUser: async (id, username) => {
        if (!confirm(`Delete user "${username || '#' + id}"? This cannot be undone.`)) return;

        if (typeof showLoader === 'function') showLoader(true);
        try {
            await fetchAPI(`/users/${id}`, 'DELETE');
            await UsersModule.loadUsers();
        } catch (e) {
            alert('Delete failed: ' + e.message);
            if (typeof showLoader === 'function') showLoader(false);
        }
    }
};
