// dashboard.js — Role-aware Overview for Ocean View Resort

document.addEventListener('DOMContentLoaded', () => {
    const userStr = sessionStorage.getItem('currentUser');
    if (!userStr) { window.location.replace('index.jsp'); return; }

    let user;
    try { user = JSON.parse(userStr); }
    catch (e) { sessionStorage.removeItem('currentUser'); window.location.replace('index.jsp'); return; }

    const authUsername = document.getElementById('authUsername');
    const authRole     = document.getElementById('authRole');
    if (authUsername) authUsername.textContent = user.username || user.name || 'User';
    if (authRole)     authRole.textContent     = user.userType || 'RECEPTIONIST';

    const role = (user.userType || '').toUpperCase();

    if (role !== 'ADMINISTRATOR') {
        const navUsers = document.getElementById('nav-users');
        if (navUsers) navUsers.style.display = 'none';
    }

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            if (window.processLogout) { await window.processLogout(); return; }
            sessionStorage.removeItem('currentUser');
            window.location.replace('index.jsp');
        });
    }

    setInterval(() => {
        const el = document.getElementById('currentTime');
        if (el) el.textContent = new Date().toLocaleTimeString();
    }, 1000);

    if (role === 'ADMINISTRATOR') {
        renderAdminDashboard(user);
    } else {
        renderReceptionDashboard(user);
    }
});

async function renderAdminDashboard(user) {
    const container = document.getElementById('viewContainer');
    const firstName = (user.username || user.name || 'Admin').split(' ')[0];
    const hour = new Date().getHours();
    const greeting = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';

    container.innerHTML = `
    <style>
      @keyframes fadeUp   { from { opacity:0; transform:translateY(18px); } to { opacity:1; transform:translateY(0); } }
      @keyframes countUp  { from { opacity:0; transform:scale(.8); } to { opacity:1; transform:scale(1); } }
      @keyframes shimmer  { 0%,100%{opacity:.6} 50%{opacity:1} }
      .dash-card          { animation: fadeUp .45s ease both; }
      .dash-card:nth-child(1){ animation-delay:.05s }
      .dash-card:nth-child(2){ animation-delay:.12s }
      .dash-card:nth-child(3){ animation-delay:.19s }
      .dash-card:nth-child(4){ animation-delay:.26s }
      .stat-num           { animation: countUp .5s ease both .3s; display:inline-block; }
      .skeleton           { animation: shimmer 1.4s infinite; background:#e5e7eb; border-radius:6px; }
      .ring-chart         { position:relative; display:inline-flex; align-items:center; justify-content:center; }
      .bar-fill           { transition: width 1s cubic-bezier(.4,0,.2,1); }
      .quick-link         { transition: all .2s; }
      .quick-link:hover   { transform:translateY(-3px); box-shadow:0 8px 24px rgba(0,0,0,.1); }
      .activity-dot       { width:8px; height:8px; border-radius:50%; flex-shrink:0; margin-top:5px; }
    </style>

    <!-- Welcome Banner -->
    <div class="relative overflow-hidden rounded-2xl mb-7 bg-gradient-to-br from-slate-800 via-blue-900 to-indigo-900 text-white p-7 shadow-xl">
      <div class="absolute inset-0 opacity-10"
           style="background-image:radial-gradient(circle at 70% 50%, #38bdf8 0%, transparent 65%),
                                   radial-gradient(circle at 10% 80%, #818cf8 0%, transparent 55%)"></div>
      <div class="relative flex items-center justify-between flex-wrap gap-4">
        <div>
          <p class="text-blue-200 text-sm font-medium mb-1 uppercase tracking-widest">Administration Panel</p>
          <h1 class="text-3xl font-extrabold tracking-tight">${greeting}, ${firstName} 👋</h1>
          <p class="text-blue-200 mt-2 text-sm" id="adminBannerDate">—</p>
        </div>
        <div class="flex-shrink-0 bg-white/10 rounded-2xl px-6 py-4 text-center backdrop-blur-sm border border-white/20">
          <div class="text-3xl font-black tracking-tight" id="adminBannerTime">—</div>
          <div class="text-xs text-blue-200 mt-1 uppercase tracking-wider">Local Time</div>
        </div>
      </div>
    </div>

    <!-- KPI Cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-5 mb-7" id="kpiGrid">
      ${adminKPISkeletons()}
    </div>

    <!-- Charts Row -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-5 mb-7">

      <!-- Reservation Breakdown -->
      <div class="lg:col-span-2 bg-white rounded-2xl border border-gray-100 shadow-sm p-6 dash-card">
        <div class="flex items-center justify-between mb-5">
          <div>
            <h3 class="text-base font-bold text-gray-800">Reservation Breakdown</h3>
            <p class="text-xs text-gray-400 mt-0.5">Status distribution across all reservations</p>
          </div>
          <span class="text-xs bg-indigo-50 text-indigo-600 font-semibold px-3 py-1 rounded-full">Live Data</span>
        </div>
        <div id="reservationChart" class="space-y-3">
          <div class="skeleton h-8 w-full"></div>
          <div class="skeleton h-8 w-4/5"></div>
          <div class="skeleton h-8 w-3/5"></div>
          <div class="skeleton h-8 w-2/5"></div>
        </div>
      </div>

      <!-- Room Status Ring -->
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 dash-card flex flex-col">
        <div class="mb-5">
          <h3 class="text-base font-bold text-gray-800">Room Occupancy</h3>
          <p class="text-xs text-gray-400 mt-0.5">Available vs Unavailable</p>
        </div>
        <div class="flex-1 flex flex-col items-center justify-center gap-4" id="roomRing">
          <div class="skeleton rounded-full" style="width:120px;height:120px"></div>
          <div class="skeleton h-4 w-24"></div>
        </div>
      </div>

    </div>

    <!-- Bottom Row -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-5 mb-7">

      <!-- Revenue Summary -->
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 dash-card">
        <h3 class="text-base font-bold text-gray-800 mb-1">Revenue Summary</h3>
        <p class="text-xs text-gray-400 mb-5">Completed reservations only</p>
        <div id="revenueSummary" class="space-y-3">
          <div class="skeleton h-12 w-full"></div>
          <div class="skeleton h-4 w-3/4"></div>
          <div class="skeleton h-4 w-1/2"></div>
        </div>
      </div>

      <!-- Quick Navigation -->
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 dash-card">
        <h3 class="text-base font-bold text-gray-800 mb-1">Quick Navigation</h3>
        <p class="text-xs text-gray-400 mb-5">Jump to any section</p>
        <div class="grid grid-cols-2 gap-3">
          <a href="users.jsp"        class="quick-link flex flex-col items-center gap-2 p-4 rounded-xl bg-violet-50 border border-violet-100 text-violet-700 text-xs font-semibold text-center">
            <i class="fa-solid fa-users text-xl"></i>Users Panel
          </a>
          <a href="rooms.jsp"        class="quick-link flex flex-col items-center gap-2 p-4 rounded-xl bg-blue-50 border border-blue-100 text-blue-700 text-xs font-semibold text-center">
            <i class="fa-solid fa-bed text-xl"></i>Rooms
          </a>
          <a href="guests.jsp"       class="quick-link flex flex-col items-center gap-2 p-4 rounded-xl bg-emerald-50 border border-emerald-100 text-emerald-700 text-xs font-semibold text-center">
            <i class="fa-solid fa-address-card text-xl"></i>Guests
          </a>
          <a href="reservations.jsp" class="quick-link flex flex-col items-center gap-2 p-4 rounded-xl bg-amber-50 border border-amber-100 text-amber-700 text-xs font-semibold text-center">
            <i class="fa-solid fa-calendar-check text-xl"></i>Reservations
          </a>
        </div>
      </div>

      <!-- Recent Reservations -->
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 dash-card">
        <div class="flex items-center justify-between mb-5">
          <div>
            <h3 class="text-base font-bold text-gray-800">Recent Reservations</h3>
            <p class="text-xs text-gray-400 mt-0.5">Latest 5 bookings</p>
          </div>
          <a href="reservations.jsp" class="text-xs text-blue-600 font-semibold hover:underline">View all →</a>
        </div>
        <div id="recentReservations" class="space-y-2">
          ${Array(4).fill('<div class="skeleton h-10 w-full rounded-lg"></div>').join('')}
        </div>
      </div>

    </div>
    `;

    const bannerTime = document.getElementById('adminBannerTime');
    const bannerDate = document.getElementById('adminBannerDate');
    const updateBanner = () => {
        const now = new Date();
        if (bannerTime) bannerTime.textContent = now.toLocaleTimeString();
        if (bannerDate) bannerDate.textContent = now.toLocaleDateString('en-US', { weekday:'long', year:'numeric', month:'long', day:'numeric' });
    };
    updateBanner();
    setInterval(updateBanner, 1000);

    const [rooms, guests, reservations] = await Promise.all([
        fetchAPI('/rooms/').catch(() => []),
        fetchAPI('/guests/').catch(() => []),
        fetchAPI('/reservations/').catch(() => []),
    ]);

    const localDateToISO = (d) => {
        if (!d) return '';
        if (typeof d === 'string') return d.slice(0, 10);
        if (Array.isArray(d)) return `${d[0]}-${String(d[1]).padStart(2,'0')}-${String(d[2]).padStart(2,'0')}`;
        return '';
    };

    const completedRes   = (reservations || []).filter(r => r.status === 'COMPLETED');
    const confirmedRes   = (reservations || []).filter(r => r.status === 'CONFIRMED');
    const revenue        = completedRes.reduce((s, r) => s + parseFloat(r.totalAmount || 0), 0);
    const availableRooms = (rooms || []).filter(r => r.available === true).length;

    const kpiGrid = document.getElementById('kpiGrid');
    if (kpiGrid) kpiGrid.innerHTML = [
        { label:'Total Rooms',       value: (rooms     || []).length,  icon:'fa-bed',            grad:'from-blue-500 to-cyan-400',    sub:`${availableRooms} available` },
        { label:'Registered Guests', value: (guests    || []).length,  icon:'fa-address-card',   grad:'from-emerald-500 to-teal-400', sub:'all time' },
        { label:'Confirmed Bookings',value: confirmedRes.length,       icon:'fa-calendar-check', grad:'from-amber-500 to-yellow-400', sub:'active reservations' },
        { label:'Total Revenue',     value:`LKR ${revenue.toFixed(2)}`,icon:'fa-money-bill-wave', grad:'from-violet-500 to-purple-400',sub:'from completed stays' },
    ].map((k,i) => `
      <div class="dash-card bg-white rounded-2xl border border-gray-100 shadow-sm p-5 overflow-hidden relative" style="animation-delay:${.05+i*.07}s">
        <div class="absolute -right-4 -top-4 w-20 h-20 rounded-full bg-gradient-to-br ${k.grad} opacity-10"></div>
        <div class="w-11 h-11 rounded-xl bg-gradient-to-br ${k.grad} flex items-center justify-center text-white mb-4 shadow-sm">
          <i class="fa-solid ${k.icon} text-base"></i>
        </div>
        <div class="text-2xl font-extrabold text-gray-900 stat-num">${k.value}</div>
        <div class="text-sm font-medium text-gray-600 mt-0.5">${k.label}</div>
        <div class="text-xs text-gray-400 mt-1">${k.sub}</div>
      </div>
    `).join('');

    const statuses = ['CONFIRMED','COMPLETED','CANCELLED','PENDING'];
    const colors   = { CONFIRMED:'bg-indigo-500', COMPLETED:'bg-emerald-500', CANCELLED:'bg-rose-400', PENDING:'bg-amber-400' };
    const total    = (reservations || []).length || 1;
    const chartEl  = document.getElementById('reservationChart');
    if (chartEl) {
        chartEl.innerHTML = statuses.map(s => {
            const count = (reservations || []).filter(r => r.status === s).length;
            const pct   = Math.round(count / total * 100);
            return `
              <div class="flex items-center gap-3">
                <span class="text-xs font-semibold text-gray-500 w-24 flex-shrink-0">${s}</span>
                <div class="flex-1 bg-gray-100 rounded-full h-6 overflow-hidden">
                  <div class="${colors[s] || 'bg-gray-400'} bar-fill h-6 rounded-full flex items-center justify-end pr-2" style="width:0%" data-target="${pct}%">
                    <span class="text-white text-xs font-bold">${count}</span>
                  </div>
                </div>
                <span class="text-xs font-bold text-gray-500 w-10 text-right">${pct}%</span>
              </div>`;
        }).join('');
        setTimeout(() => {
            chartEl.querySelectorAll('.bar-fill').forEach(b => b.style.width = b.dataset.target);
        }, 100);
    }

    const totalRooms  = (rooms || []).length || 1;
    const unavail     = totalRooms - availableRooms;
    const availPct    = Math.round(availableRooms / totalRooms * 100);
    const R           = 52, C = 2 * Math.PI * R;
    const dash        = (availableRooms / totalRooms) * C;
    const ringEl      = document.getElementById('roomRing');
    if (ringEl) ringEl.innerHTML = `
      <svg width="140" height="140" viewBox="0 0 140 140">
        <circle cx="70" cy="70" r="${R}" fill="none" stroke="#f3f4f6" stroke-width="14"/>
        <circle cx="70" cy="70" r="${R}" fill="none" stroke="#10b981" stroke-width="14"
                stroke-dasharray="${dash} ${C}" stroke-dashoffset="${C/4}"
                stroke-linecap="round" style="transition:stroke-dasharray 1s ease"/>
        <text x="70" y="66" text-anchor="middle" font-size="20" font-weight="800" fill="#111827">${availPct}%</text>
        <text x="70" y="83" text-anchor="middle" font-size="9" fill="#6b7280">Available</text>
      </svg>
      <div class="flex gap-4 text-xs font-semibold">
        <span class="flex items-center gap-1.5"><span class="w-3 h-3 rounded-full bg-emerald-500"></span>${availableRooms} Free</span>
        <span class="flex items-center gap-1.5"><span class="w-3 h-3 rounded-full bg-gray-200"></span>${unavail} Busy</span>
      </div>`;

    const avgRev  = completedRes.length ? revenue / completedRes.length : 0;
    const revEl   = document.getElementById('revenueSummary');
    if (revEl) revEl.innerHTML = `
      <div class="bg-gradient-to-br from-violet-50 to-purple-50 rounded-xl p-4 border border-violet-100">
        <div class="text-xs text-violet-500 font-semibold uppercase tracking-wider mb-1">Total Earned</div>
        <div class="text-3xl font-extrabold text-violet-700">LKR ${revenue.toFixed(2)}</div>
      </div>
      <div class="grid grid-cols-2 gap-3 mt-3">
        <div class="bg-gray-50 rounded-xl p-3 border border-gray-100">
          <div class="text-xs text-gray-400 font-medium">Avg per Stay</div>
          <div class="text-base font-bold text-gray-800 mt-0.5">LKR ${avgRev.toFixed(2)}</div>
        </div>
        <div class="bg-gray-50 rounded-xl p-3 border border-gray-100">
          <div class="text-xs text-gray-400 font-medium">Completed Stays</div>
          <div class="text-base font-bold text-gray-800 mt-0.5">${completedRes.length}</div>
        </div>
      </div>`;

    const statusMeta = {
        CONFIRMED:{ cls:'bg-indigo-100 text-indigo-700', lbl:'Confirmed' },
        COMPLETED:{ cls:'bg-emerald-100 text-emerald-700', lbl:'Completed' },
        CANCELLED:{ cls:'bg-rose-100 text-rose-700', lbl:'Cancelled' },
        PENDING  :{ cls:'bg-amber-100 text-amber-700', lbl:'Pending' },
    };
    const recent  = [...(reservations || [])].slice(-5).reverse();
    const recEl   = document.getElementById('recentReservations');
    if (recEl) recEl.innerHTML = recent.length ? recent.map(r => {
        const sm = statusMeta[r.status] || { cls:'bg-gray-100 text-gray-600', lbl: r.status };
        return `
          <div class="flex items-center justify-between p-3 rounded-xl bg-gray-50 border border-gray-100 hover:bg-blue-50/50 transition-colors">
            <div>
              <span class="text-xs font-bold text-gray-700">${r.reservationNo || '#' + r.reservationId}</span>
              <span class="text-xs text-gray-400 ml-2">${r.guestName || 'Guest'}</span>
            </div>
            <span class="text-xs font-semibold px-2 py-0.5 rounded-full ${sm.cls}">${sm.lbl}</span>
          </div>`;
    }).join('') : '<div class="text-center text-gray-400 text-sm py-4">No reservations yet</div>';
}

function adminKPISkeletons() {
    return Array(4).fill(`
      <div class="dash-card bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <div class="skeleton rounded-xl w-11 h-11 mb-4"></div>
        <div class="skeleton h-7 w-20 mb-2"></div>
        <div class="skeleton h-4 w-28 mb-1"></div>
        <div class="skeleton h-3 w-20"></div>
      </div>`).join('');
}

async function renderReceptionDashboard(user) {
    const container = document.getElementById('viewContainer');
    const firstName = (user.username || user.name || 'Receptionist').split(' ')[0];
    const hour = new Date().getHours();
    const greeting = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';
    const todayISO  = new Date().toISOString().slice(0, 10);

    container.innerHTML = `
    <style>
      @keyframes fadeUp  { from { opacity:0; transform:translateY(18px); } to { opacity:1; transform:translateY(0); } }
      @keyframes shimmer { 0%,100%{opacity:.6} 50%{opacity:1} }
      .dash-card         { animation: fadeUp .45s ease both; }
      .dash-card:nth-child(1){ animation-delay:.05s }
      .dash-card:nth-child(2){ animation-delay:.12s }
      .dash-card:nth-child(3){ animation-delay:.19s }
      .dash-card:nth-child(4){ animation-delay:.26s }
      .skeleton          { animation: shimmer 1.4s infinite; background:#e5e7eb; border-radius:6px; }
      .action-card       { transition: all .2s ease; cursor:pointer; }
      .action-card:hover { transform:translateY(-3px); box-shadow:0 12px 28px rgba(0,0,0,.10); }
      .checkin-row:hover { background:#f0fdf4; }
      .checkout-row:hover{ background:#fff7ed; }
      .tab-btn           { transition: all .18s; }
      .tab-btn.active    { background:white; box-shadow:0 1px 4px rgba(0,0,0,.10); color:#1d4ed8; }
    </style>

    <!-- Welcome Banner -->
    <div class="relative overflow-hidden rounded-2xl mb-7 bg-gradient-to-br from-teal-700 via-emerald-800 to-cyan-900 text-white p-7 shadow-xl">
      <div class="absolute inset-0 opacity-10"
           style="background-image:radial-gradient(circle at 75% 50%, #6ee7b7 0%, transparent 60%),
                                   radial-gradient(circle at 15% 80%, #34d399 0%, transparent 50%)"></div>
      <div class="relative flex items-start justify-between flex-wrap gap-4">
        <div>
          <p class="text-emerald-200 text-sm font-medium mb-1 uppercase tracking-widest">Reception Desk</p>
          <h1 class="text-3xl font-extrabold tracking-tight">${greeting}, ${firstName}! </h1>
          <p class="text-emerald-200 mt-2 text-sm" id="recBannerDate">—</p>
          <div class="mt-4 flex flex-wrap gap-3">
            <a href="reservations.jsp" class="flex items-center gap-2 bg-white/15 hover:bg-white/25 transition-colors backdrop-blur-sm border border-white/20 rounded-xl px-4 py-2 text-sm font-semibold">
              <i class="fa-solid fa-plus-circle"></i> New Reservation
            </a>
            <a href="guests.jsp" class="flex items-center gap-2 bg-white/15 hover:bg-white/25 transition-colors backdrop-blur-sm border border-white/20 rounded-xl px-4 py-2 text-sm font-semibold">
              <i class="fa-solid fa-magnifying-glass"></i> Find Guest
            </a>
          </div>
        </div>
        <div class="flex-shrink-0 bg-white/10 rounded-2xl px-6 py-4 text-center backdrop-blur-sm border border-white/20">
          <div class="text-3xl font-black tracking-tight" id="recBannerTime">—</div>
          <div class="text-xs text-emerald-200 mt-1 uppercase tracking-wider">Local Time</div>
        </div>
      </div>
    </div>

    <!-- KPI Cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-5 mb-7" id="recKpiGrid">
      ${recKPISkeletons()}
    </div>

    <!-- Main Work Area -->
    <div class="grid grid-cols-1 lg:grid-cols-5 gap-5 mb-7">

      <!-- Today's Check-in / Check-out Tabs -->
      <div class="lg:col-span-3 bg-white rounded-2xl border border-gray-100 shadow-sm p-6 dash-card">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h3 class="text-base font-bold text-gray-800">Today's Schedule</h3>
            <p class="text-xs text-gray-400 mt-0.5" id="todayLabel">—</p>
          </div>
          <div class="flex bg-gray-100 rounded-xl p-1 gap-1" id="scheduleTabs">
            <button class="tab-btn active text-xs font-semibold px-3 py-1.5 rounded-lg" data-tab="checkins">Check-ins</button>
            <button class="tab-btn text-xs font-semibold px-3 py-1.5 rounded-lg text-gray-500" data-tab="checkouts">Check-outs</button>
          </div>
        </div>
        <div id="checkinsPanel"  class="tab-panel space-y-2 max-h-64 overflow-y-auto pr-1"></div>
        <div id="checkoutsPanel" class="tab-panel space-y-2 max-h-64 overflow-y-auto pr-1 hidden"></div>
      </div>

      <!-- Available Rooms Quick View -->
      <div class="lg:col-span-2 bg-white rounded-2xl border border-gray-100 shadow-sm p-6 dash-card flex flex-col">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h3 class="text-base font-bold text-gray-800">Available Rooms</h3>
            <p class="text-xs text-gray-400 mt-0.5">Ready to book now</p>
          </div>
          <a href="rooms.jsp" class="text-xs text-teal-600 font-semibold hover:underline">All rooms →</a>
        </div>
        <div id="availRoomsList" class="flex-1 space-y-2 max-h-64 overflow-y-auto pr-1">
          ${Array(3).fill('<div class="skeleton h-12 w-full rounded-xl"></div>').join('')}
        </div>
      </div>

    </div>

    <!-- Bottom: Quick Actions + Recent Guests -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-5">

      <!-- Quick Actions -->
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 dash-card">
        <h3 class="text-base font-bold text-gray-800 mb-1">Quick Actions</h3>
        <p class="text-xs text-gray-400 mb-5">Common reception tasks</p>
        <div class="grid grid-cols-2 gap-3">
          <a href="reservations.jsp" class="action-card flex items-center gap-3 p-4 rounded-xl bg-gradient-to-br from-teal-500 to-emerald-600 text-white shadow-sm">
            <div class="w-9 h-9 bg-white/25 rounded-lg flex items-center justify-center flex-shrink-0">
              <i class="fa-solid fa-calendar-plus text-base"></i>
            </div>
            <div><div class="font-bold text-sm">New Booking</div><div class="text-xs text-white/75">Create reservation</div></div>
          </a>
          <a href="guests.jsp" class="action-card flex items-center gap-3 p-4 rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 text-white shadow-sm">
            <div class="w-9 h-9 bg-white/25 rounded-lg flex items-center justify-center flex-shrink-0">
              <i class="fa-solid fa-user-plus text-base"></i>
            </div>
            <div><div class="font-bold text-sm">Add Guest</div><div class="text-xs text-white/75">Register new guest</div></div>
          </a>
          <a href="rooms.jsp" class="action-card flex items-center gap-3 p-4 rounded-xl bg-gradient-to-br from-amber-500 to-orange-500 text-white shadow-sm">
            <div class="w-9 h-9 bg-white/25 rounded-lg flex items-center justify-center flex-shrink-0">
              <i class="fa-solid fa-bed text-base"></i>
            </div>
            <div><div class="font-bold text-sm">Room Status</div><div class="text-xs text-white/75">View availability</div></div>
          </a>
          <a href="reservations.jsp" class="action-card flex items-center gap-3 p-4 rounded-xl bg-gradient-to-br from-violet-500 to-purple-600 text-white shadow-sm">
            <div class="w-9 h-9 bg-white/25 rounded-lg flex items-center justify-center flex-shrink-0">
              <i class="fa-solid fa-file-invoice text-base"></i>
            </div>
            <div><div class="font-bold text-sm">Generate Bill</div><div class="text-xs text-white/75">Print reservation</div></div>
          </a>
        </div>
      </div>

      <!-- Recent Guests -->
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 dash-card">
        <div class="flex items-center justify-between mb-5">
          <div>
            <h3 class="text-base font-bold text-gray-800">Recent Guests</h3>
            <p class="text-xs text-gray-400 mt-0.5">Latest registered guests</p>
          </div>
          <a href="guests.jsp" class="text-xs text-teal-600 font-semibold hover:underline">View all →</a>
        </div>
        <div id="recentGuests" class="space-y-2.5">
          ${Array(4).fill('<div class="skeleton h-11 w-full rounded-xl"></div>').join('')}
        </div>
      </div>

    </div>
    `;

    const bTime = document.getElementById('recBannerTime');
    const bDate = document.getElementById('recBannerDate');
    const todayLbl = document.getElementById('todayLabel');
    const updateRec = () => {
        const now = new Date();
        if (bTime) bTime.textContent = now.toLocaleTimeString();
        const ds = now.toLocaleDateString('en-US', { weekday:'long', year:'numeric', month:'long', day:'numeric' });
        if (bDate) bDate.textContent = ds;
        if (todayLbl) todayLbl.textContent = ds;
    };
    updateRec();
    setInterval(updateRec, 1000);

    document.querySelectorAll('#scheduleTabs .tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('#scheduleTabs .tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            const t = btn.dataset.tab;
            document.getElementById('checkinsPanel').classList.toggle('hidden',  t !== 'checkins');
            document.getElementById('checkoutsPanel').classList.toggle('hidden', t !== 'checkouts');
        });
    });

    const [rooms, guests, reservations] = await Promise.all([
        fetchAPI('/rooms/').catch(() => []),
        fetchAPI('/guests/').catch(() => []),
        fetchAPI('/reservations/').catch(() => []),
    ]);

    const localDateToISO = (d) => {
        if (!d) return '';
        if (typeof d === 'string') return d.slice(0, 10);
        if (Array.isArray(d)) return `${d[0]}-${String(d[1]).padStart(2,'0')}-${String(d[2]).padStart(2,'0')}`;
        return '';
    };

    const availRooms     = (rooms || []).filter(r => r.available === true);
    const todayCheckIns  = (reservations || []).filter(r => localDateToISO(r.checkInDate)  === todayISO && r.status === 'CONFIRMED');
    const todayCheckOuts = (reservations || []).filter(r => localDateToISO(r.checkOutDate) === todayISO && r.status === 'CONFIRMED');
    const activeRes      = (reservations || []).filter(r => r.status === 'CONFIRMED');

    const kpiGrid = document.getElementById('recKpiGrid');
    if (kpiGrid) kpiGrid.innerHTML = [
        { label:"Today's Check-ins",    value: todayCheckIns.length,  icon:'fa-arrow-right-to-bracket', grad:'from-teal-500 to-emerald-400',   sub:'expected arrivals' },
        { label:"Today's Check-outs",   value: todayCheckOuts.length, icon:'fa-arrow-right-from-bracket',grad:'from-amber-500 to-orange-400',   sub:'departures today' },
        { label:'Available Rooms',      value: availRooms.length,     icon:'fa-bed',                    grad:'from-blue-500 to-cyan-400',       sub:`of ${(rooms||[]).length} total` },
        { label:'Active Reservations',  value: activeRes.length,      icon:'fa-calendar-check',         grad:'from-violet-500 to-purple-400',   sub:'confirmed bookings' },
    ].map((k,i) => `
      <div class="dash-card bg-white rounded-2xl border border-gray-100 shadow-sm p-5 overflow-hidden relative" style="animation-delay:${.05+i*.07}s">
        <div class="absolute -right-4 -top-4 w-20 h-20 rounded-full bg-gradient-to-br ${k.grad} opacity-10"></div>
        <div class="w-11 h-11 rounded-xl bg-gradient-to-br ${k.grad} flex items-center justify-center text-white mb-4 shadow-sm">
          <i class="fa-solid ${k.icon} text-base"></i>
        </div>
        <div class="text-2xl font-extrabold text-gray-900">${k.value}</div>
        <div class="text-sm font-medium text-gray-600 mt-0.5">${k.label}</div>
        <div class="text-xs text-gray-400 mt-1">${k.sub}</div>
      </div>
    `).join('');

    const ciEl = document.getElementById('checkinsPanel');
    if (ciEl) ciEl.innerHTML = todayCheckIns.length ? todayCheckIns.map(r => `
      <div class="checkin-row flex items-center justify-between p-3 rounded-xl border border-gray-100 transition-colors">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 rounded-full bg-emerald-100 flex items-center justify-center flex-shrink-0">
            <i class="fa-solid fa-arrow-right-to-bracket text-emerald-600 text-xs"></i>
          </div>
          <div>
            <div class="text-xs font-bold text-gray-800">${r.reservationNo || '#'+r.reservationId}</div>
            <div class="text-xs text-gray-400">${r.guestName || 'Guest'}</div>
          </div>
        </div>
        <div class="text-right">
          <div class="text-xs font-semibold text-gray-600">${(r.rooms||[]).map(rm=>rm.roomName).join(', ') || '—'}</div>
          <div class="text-xs text-emerald-600 font-bold">Arriving Today</div>
        </div>
      </div>`) .join('') :
      `<div class="text-center text-gray-400 text-sm py-8 flex flex-col items-center gap-2">
         <i class="fa-solid fa-moon text-3xl text-gray-200"></i>No check-ins scheduled for today
       </div>`;

    const coEl = document.getElementById('checkoutsPanel');
    if (coEl) coEl.innerHTML = todayCheckOuts.length ? todayCheckOuts.map(r => `
      <div class="checkout-row flex items-center justify-between p-3 rounded-xl border border-gray-100 transition-colors">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 rounded-full bg-amber-100 flex items-center justify-center flex-shrink-0">
            <i class="fa-solid fa-arrow-right-from-bracket text-amber-600 text-xs"></i>
          </div>
          <div>
            <div class="text-xs font-bold text-gray-800">${r.reservationNo || '#'+r.reservationId}</div>
            <div class="text-xs text-gray-400">${r.guestName || 'Guest'}</div>
          </div>
        </div>
        <div class="text-right">
          <div class="text-xs font-semibold text-gray-600">${(r.rooms||[]).map(rm=>rm.roomName).join(', ') || '—'}</div>
          <div class="text-xs text-amber-600 font-bold">Departing Today</div>
        </div>
      </div>`).join('') :
      `<div class="text-center text-gray-400 text-sm py-8 flex flex-col items-center gap-2">
         <i class="fa-solid fa-sun text-3xl text-gray-200"></i>No check-outs scheduled for today
       </div>`;

    const roomColors = { STANDARD:'bg-blue-50 border-blue-100 text-blue-600', DELUXE:'bg-violet-50 border-violet-100 text-violet-600', FAMILY_SUITE:'bg-amber-50 border-amber-100 text-amber-600' };
    const arEl = document.getElementById('availRoomsList');
    if (arEl) arEl.innerHTML = availRooms.length ? availRooms.slice(0, 8).map(r => {
        const typeCls = roomColors[r.roomType] || 'bg-gray-50 border-gray-100 text-gray-600';
        return `
          <div class="flex items-center justify-between p-3 rounded-xl border border-gray-100 bg-gray-50 hover:bg-emerald-50/50 transition-colors">
            <div class="flex items-center gap-2.5">
              <div class="w-7 h-7 rounded-lg bg-emerald-100 flex items-center justify-center flex-shrink-0">
                <i class="fa-solid fa-bed text-emerald-600 text-xs"></i>
              </div>
              <div>
                <div class="text-xs font-bold text-gray-800">${r.roomName || r.roomNumber || 'Room'}</div>
                <div class="text-xs text-gray-400">LKR ${parseFloat(r.roomPrice||0).toFixed(2)}/night</div>
              </div>
            </div>
            <span class="text-xs font-semibold px-2 py-0.5 rounded-full border ${typeCls}">${(r.roomType||'Standard').replace('_',' ')}</span>
          </div>`;
    }).join('') + (availRooms.length > 8 ? `<p class="text-center text-xs text-gray-400 pt-1">+${availRooms.length-8} more rooms available</p>` : '')
    : `<div class="text-center text-gray-400 text-sm py-6 flex flex-col items-center gap-2">
         <i class="fa-solid fa-door-closed text-3xl text-gray-200"></i>All rooms are currently occupied
       </div>`;

    const avatarColors = ['bg-teal-100 text-teal-700','bg-blue-100 text-blue-700','bg-violet-100 text-violet-700','bg-amber-100 text-amber-700','bg-rose-100 text-rose-700'];
    const recent = [...(guests || [])].slice(-5).reverse();
    const rgEl = document.getElementById('recentGuests');
    if (rgEl) rgEl.innerHTML = recent.length ? recent.map((g, i) => {
        const initial  = (g.name || g.guestName || 'G')[0].toUpperCase();
        const colorCls = avatarColors[i % avatarColors.length];
        return `
          <div class="flex items-center gap-3 p-2.5 rounded-xl hover:bg-gray-50 transition-colors">
            <div class="w-9 h-9 rounded-full ${colorCls} flex items-center justify-center font-bold text-sm flex-shrink-0">${initial}</div>
            <div class="flex-1 min-w-0">
              <div class="text-sm font-semibold text-gray-800 truncate">${g.name || g.guestName || 'Guest'}</div>
              <div class="text-xs text-gray-400 truncate">${g.email || g.phoneNumber || 'No contact'}</div>
            </div>
            <a href="guests.jsp" class="text-xs text-teal-600 font-semibold hover:underline">View</a>
          </div>`;
    }).join('') : '<div class="text-center text-gray-400 text-sm py-4">No guests registered yet</div>';
}

function recKPISkeletons() {
    return Array(4).fill(`
      <div class="dash-card bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <div class="skeleton rounded-xl w-11 h-11 mb-4"></div>
        <div class="skeleton h-7 w-12 mb-2"></div>
        <div class="skeleton h-4 w-28 mb-1"></div>
        <div class="skeleton h-3 w-20"></div>
      </div>`).join('');
}
