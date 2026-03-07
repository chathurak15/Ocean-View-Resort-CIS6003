const ReservationsModule = {

    _selectedRooms: new Map(),
    _guestId: null,
    _guestName: '',

    init: async () => {
        const curUserStr = sessionStorage.getItem('currentUser');
        const curUser    = curUserStr ? JSON.parse(curUserStr) : {};
        const isAdmin    = (curUser.userType || '').toUpperCase() === 'ADMINISTRATOR';

        const container = document.getElementById('viewContainer');
        ReservationsModule.renderLayout(container, isAdmin);
        await ReservationsModule.loadReservations();
    },

    renderLayout: (container, isAdmin) => {
        const reportTab = isAdmin ? `
            <button id="tabReport" onclick="ReservationsModule.switchTab('report')"
                class="px-4 py-2 text-sm font-medium rounded-lg text-gray-600 hover:text-blue-700 hover:bg-blue-50 transition-colors">
                <i class="fa-solid fa-chart-bar mr-1"></i> Report
            </button>` : '';

        container.innerHTML = `
            <div class="space-y-6 animate-fade-in">

                <!-- Header -->
                <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <div>
                        <h2 class="text-2xl font-bold text-gray-900">Reservations</h2>
                        <p class="text-sm text-gray-500 mt-1">Manage guest reservations, status and billing.</p>
                    </div>
                    <div class="flex gap-2 flex-wrap">
                        <div class="flex gap-1 bg-gray-100 p-1 rounded-lg">
                            <button id="tabList" onclick="ReservationsModule.switchTab('list')"
                                class="px-4 py-2 text-sm font-medium rounded-lg bg-white shadow-sm text-blue-700 transition-colors">
                                <i class="fa-solid fa-list mr-1"></i> All
                            </button>
                            ${reportTab}
                        </div>
                        <button onclick="ReservationsModule.openNewReservationWizard()"
                            class="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-lg text-sm font-semibold shadow-sm transition-all flex items-center gap-2">
                            <i class="fa-solid fa-plus"></i> New Reservation
                        </button>
                    </div>
                </div>

                <!-- Table View -->
                <div id="viewList">
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                        <div class="overflow-x-auto">
                            <table class="w-full text-left text-sm text-gray-600">
                                <thead class="text-xs text-gray-700 uppercase bg-gray-50/80 border-b border-gray-200">
                                    <tr>
                                        <th class="px-5 py-4 font-semibold">Ref No.</th>
                                        <th class="px-5 py-4 font-semibold">Guest</th>
                                        <th class="px-5 py-4 font-semibold">Check In</th>
                                        <th class="px-5 py-4 font-semibold">Check Out</th>
                                        <th class="px-5 py-4 font-semibold">Rooms</th>
                                        <th class="px-5 py-4 font-semibold text-center">Status</th>
                                        <th class="px-5 py-4 font-semibold text-right">Total</th>
                                        <th class="px-5 py-4 font-semibold text-right">Actions</th>
                                    </tr>
                                </thead>
                                <tbody id="reservationsTableBody" class="divide-y divide-gray-100">
                                    <tr><td colspan="8" class="px-6 py-8 text-center">
                                        <div class="flex flex-col items-center">
                                            <div class="h-8 w-8 rounded-full border-4 border-gray-200 border-t-blue-500 animate-spin mb-3"></div>
                                            <div class="text-gray-400 font-medium">Loading...</div>
                                        </div>
                                    </td></tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Report View (admin only) -->
                <div id="viewReport" class="hidden space-y-5">
                    <div class="bg-white p-5 rounded-xl shadow-sm border border-gray-100">
                        <h3 class="text-sm font-semibold text-gray-700 uppercase tracking-wider mb-3">Date Range Report</h3>
                        <div class="flex flex-wrap gap-3 items-end">
                            <div>
                                <label class="block text-xs font-medium text-gray-600 mb-1">From</label>
                                <input type="date" id="reportFrom" class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none">
                            </div>
                            <div>
                                <label class="block text-xs font-medium text-gray-600 mb-1">To</label>
                                <input type="date" id="reportTo" class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none">
                            </div>
                            <button onclick="ReservationsModule.generateReport()"
                                class="bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-2 rounded-lg text-sm font-semibold transition-colors flex items-center gap-2">
                                <i class="fa-solid fa-magnifying-glass-chart"></i> Generate Report
                            </button>
                        </div>
                    </div>
                    <!-- Summary Stats -->
                    <div id="reportStats" class="hidden grid grid-cols-2 sm:grid-cols-4 gap-4"></div>
                    <!-- Export Buttons -->
                    <div id="reportExportBar" class="hidden flex gap-2 justify-end">
                        <button onclick="ReservationsModule.exportCSV()"
                            class="flex items-center gap-2 px-4 py-2 text-sm font-medium text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg hover:bg-emerald-100 transition-colors">
                            <i class="fa-solid fa-file-csv"></i> Export CSV
                        </button>
                        <button onclick="ReservationsModule.exportReportPDF()"
                            class="flex items-center gap-2 px-4 py-2 text-sm font-medium text-rose-700 bg-rose-50 border border-rose-200 rounded-lg hover:bg-rose-100 transition-colors">
                            <i class="fa-solid fa-file-pdf"></i> Export PDF
                        </button>
                    </div>
                    <!-- Report Table -->
                    <div id="reportTableWrap" class="hidden bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                        <div class="overflow-x-auto">
                            <table class="w-full text-left text-sm text-gray-600">
                                <thead class="text-xs text-gray-700 uppercase bg-gray-50/80 border-b border-gray-200">
                                    <tr>
                                        <th class="px-5 py-4 font-semibold">Ref No.</th>
                                        <th class="px-5 py-4 font-semibold">Guest</th>
                                        <th class="px-5 py-4 font-semibold">Check In</th>
                                        <th class="px-5 py-4 font-semibold">Check Out</th>
                                        <th class="px-5 py-4 font-semibold">Rooms</th>
                                        <th class="px-5 py-4 font-semibold text-center">Status</th>
                                        <th class="px-5 py-4 font-semibold text-right">Total</th>
                                        <th class="px-5 py-4 font-semibold text-right">View</th>
                                    </tr>
                                </thead>
                                <tbody id="reportTableBody" class="divide-y divide-gray-100"></tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Overlay Modal (full wizard) -->
            <div id="resModal" class="hidden fixed inset-0 bg-gray-900/70 backdrop-blur-sm z-50 flex items-center justify-center p-4 opacity-0 transition-opacity duration-300">
                <div id="resModalContent" class="bg-white rounded-2xl shadow-2xl w-full max-w-3xl max-h-[92vh] overflow-y-auto transform scale-95 transition-transform duration-300"></div>
            </div>
        `;
    },

    switchTab: (tab) => {
        const listEl   = document.getElementById('viewList');
        const reportEl = document.getElementById('viewReport');
        const btnList  = document.getElementById('tabList');
        const btnReport= document.getElementById('tabReport');

        const activeClass   = 'bg-white shadow-sm text-blue-700';
        const inactiveClass = 'text-gray-600 hover:text-blue-700 hover:bg-blue-50';

        if (tab === 'list') {
            listEl.classList.remove('hidden');
            reportEl.classList.add('hidden');
            btnList.className = `px-4 py-2 text-sm font-medium rounded-lg ${activeClass} transition-colors`;
            if (btnReport) btnReport.className = `px-4 py-2 text-sm font-medium rounded-lg ${inactiveClass} transition-colors`;
        } else {
            listEl.classList.add('hidden');
            reportEl.classList.remove('hidden');
            btnList.className = `px-4 py-2 text-sm font-medium rounded-lg ${inactiveClass} transition-colors`;
            if (btnReport) btnReport.className = `px-4 py-2 text-sm font-medium rounded-lg ${activeClass} transition-colors`;
        }
    },

    loadReservations: async () => {
        showLoader(true);
        try {
            const list = await fetchAPI('/reservations/');
            ReservationsModule.renderTable(list, 'reservationsTableBody', true);
        } catch (e) {
            const tb = document.getElementById('reservationsTableBody');
            if (tb) tb.innerHTML = `<tr><td colspan="8" class="px-6 py-10 text-center text-red-500">
                <i class="fa-solid fa-triangle-exclamation text-3xl mb-2 block"></i> ${e.message}</td></tr>`;
        } finally {
            showLoader(false);
        }
    },

    renderTable: (list, tbodyId, showActions) => {
        const tbody = document.getElementById(tbodyId);
        if (!list || list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="px-6 py-12 text-center text-gray-400">
                <i class="fa-solid fa-calendar-xmark text-4xl mb-3 block opacity-40"></i>
                No reservations found.</td></tr>`;
            return;
        }

        tbody.innerHTML = list.map(r => {
            const statusCfg = {
                CONFIRMED: { cls: 'bg-blue-100 text-blue-700 border border-blue-200',  icon: 'fa-clock' },
                COMPLETED: { cls: 'bg-emerald-100 text-emerald-700 border border-emerald-200', icon: 'fa-check-circle' },
                CANCELLED: { cls: 'bg-rose-100 text-rose-700 border border-rose-200',   icon: 'fa-xmark-circle' },
            };
            const s    = statusCfg[r.status] || statusCfg.CONFIRMED;
            const rooms= (r.rooms || []).map(rm => rm.roomName).join(', ') || '—';
            const fmt  = d => d ? new Date(d).toLocaleDateString('en-GB', {day:'2-digit',month:'short',year:'numeric'}) : '—';
            const rno  = r.reservationNo;

            const viewBtn = `
                <button onclick="ReservationsModule.viewReservation('${rno}')"
                    class="p-1.5 rounded-lg text-gray-400 hover:text-blue-600 hover:bg-blue-50 transition-colors" title="View Details">
                    <i class="fa-solid fa-eye"></i>
                </button>`;

            const actionBtns = showActions && r.status === 'CONFIRMED' ? `
                ${viewBtn}
                <button onclick="ReservationsModule.changeStatus('${rno}','complete')"
                    class="p-1.5 rounded-lg text-gray-400 hover:text-emerald-600 hover:bg-emerald-50 transition-colors" title="Mark Completed">
                    <i class="fa-solid fa-circle-check"></i>
                </button>
                <button onclick="ReservationsModule.changeStatus('${rno}','cancel')"
                    class="p-1.5 rounded-lg text-gray-400 hover:text-rose-600 hover:bg-rose-50 transition-colors" title="Cancel">
                    <i class="fa-solid fa-ban"></i>
                </button>` : viewBtn;

            return `
            <tr class="bg-white hover:bg-blue-50/20 transition-colors group">
                <td class="px-5 py-4 font-mono text-xs font-semibold text-blue-600">${rno}</td>
                <td class="px-5 py-4 font-medium text-gray-900">${r.guestName || '—'}</td>
                <td class="px-5 py-4 text-xs text-gray-600">${fmt(r.checkInDate)}</td>
                <td class="px-5 py-4 text-xs text-gray-600">${fmt(r.checkOutDate)}</td>
                <td class="px-5 py-4 text-xs text-gray-500 max-w-[140px] truncate" title="${rooms}">${rooms}</td>
                <td class="px-5 py-4 text-center">
                    <span class="px-2.5 py-1 text-xs font-semibold rounded-full ${s.cls}">
                        <i class="fa-solid ${s.icon} mr-1"></i>${r.status}
                    </span>
                </td>
                <td class="px-5 py-4 text-right font-semibold text-emerald-600">LKR ${parseFloat(r.totalAmount || 0).toFixed(2)}</td>
                <td class="px-5 py-4 text-right space-x-1 whitespace-nowrap">${actionBtns}</td>
            </tr>`;
        }).join('');
    },

    changeStatus: async (reservationNo, action) => {
        const label = action === 'cancel' ? 'Cancel' : 'Complete';
        if (!confirm(`${label} reservation ${reservationNo}?`)) return;
        showLoader(true);
        try {
            await fetchAPI(`/reservations/${reservationNo}/${action}`, 'PUT');
            if (typeof showToast === 'function') showToast(`Reservation ${reservationNo} ${label.toLowerCase()}ed successfully`, 'success');
            await ReservationsModule.loadReservations();
        } catch (e) {
            if (typeof showToast === 'function') showToast(`${label} failed: ` + e.message, 'error');
            showLoader(false);
        }
    },

    _reportData: [],

    generateReport: async () => {
        const from = document.getElementById('reportFrom').value;
        const to   = document.getElementById('reportTo').value;
        if (!from || !to) { 
            if (typeof showToast === 'function') showToast('Please select both From and To dates.', 'error');
            return; 
        }
        if (from > to)    { 
            if (typeof showToast === 'function') showToast('From date must be before To date.', 'error');
            return; 
        }

        showLoader(true);
        try {
            const list = await fetchAPI(`/reservations/?from=${from}&to=${to}`);
            ReservationsModule._reportData = list;
            ReservationsModule._reportFrom = from;
            ReservationsModule._reportTo   = to;

            const statsEl   = document.getElementById('reportStats');
            const revenue = list.filter(r => r.status === 'COMPLETED')
                                  .reduce((s, r) => s + parseFloat(r.totalAmount || 0), 0);
            const confirmed = list.filter(r => r.status === 'CONFIRMED').length;
            const completed = list.filter(r => r.status === 'COMPLETED').length;
            const cancelled = list.filter(r => r.status === 'CANCELLED').length;

            statsEl.innerHTML = [
                { label: 'Total Reservations', val: list.length,                icon: 'fa-calendar-check', col: 'blue'   },
                { label: 'Confirmed',           val: confirmed,                  icon: 'fa-clock',          col: 'indigo' },
                { label: 'Completed',           val: completed,                  icon: 'fa-check-circle',   col: 'emerald'},
                { label: 'Revenue (Completed)', val: `LKR ${revenue.toFixed(2)}`,   icon: 'fa-money-bill-wave',    col: 'amber'  },
            ].map(st => `
                <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full bg-${st.col}-100 flex items-center justify-center">
                        <i class="fa-solid ${st.icon} text-${st.col}-600"></i>
                    </div>
                    <div>
                        <p class="text-xs text-gray-500">${st.label}</p>
                        <p class="text-xl font-bold text-gray-900">${st.val}</p>
                    </div>
                </div>`).join('');
            statsEl.classList.remove('hidden');

            // Export bar
            document.getElementById('reportExportBar').classList.remove('hidden');

            // Table
            ReservationsModule.renderTable(list, 'reportTableBody', false);
            document.getElementById('reportTableWrap').classList.remove('hidden');
        } catch (e) {
            if (typeof showToast === 'function') showToast('Report failed: ' + e.message, 'error');
        } finally {
            showLoader(false);
        }
    },

    exportCSV: () => {
        const list = ReservationsModule._reportData;
        if (!list || !list.length) { 
            if (typeof showToast === 'function') showToast('Generate a report first.', 'error');
            else alert('Generate a report first.'); 
            return; 
        }

        const headers = ['Ref No', 'Guest', 'Check In', 'Check Out', 'Rooms', 'Status', 'Total (USD)'];
        const fmt = d => d ? new Date(d).toLocaleDateString('en-GB') : '';
        const escape = v => `"${String(v).replace(/"/g, '""')}"`;

        const rows = list.map(r => [
            r.reservationNo,
            r.guestName || '',
            fmt(r.checkInDate),
            fmt(r.checkOutDate),
            (r.rooms || []).map(rm => rm.roomName).join('; '),
            r.status,
            parseFloat(r.totalAmount || 0).toFixed(2)
        ].map(escape).join(','));

        const csv   = [headers.map(escape).join(','), ...rows].join('\n');
        const blob  = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        const url   = URL.createObjectURL(blob);
        const link  = document.createElement('a');
        link.href   = url;
        link.download = `reservations_report_${ReservationsModule._reportFrom}_${ReservationsModule._reportTo}.csv`;
        link.click();
        URL.revokeObjectURL(url);
    },

    exportReportPDF: () => {
        const list = ReservationsModule._reportData;
        if (!list || !list.length) { 
            if (typeof showToast === 'function') showToast('Generate a report first.', 'error');
            else alert('Generate a report first.'); 
            return; 
        }

        const from = ReservationsModule._reportFrom;
        const to   = ReservationsModule._reportTo;
        const revenue = list.filter(r => r.status === 'COMPLETED')
                            .reduce((s, r) => s + parseFloat(r.totalAmount || 0), 0);
        const fmt = d => d ? new Date(d).toLocaleDateString('en-GB', {day:'2-digit',month:'short',year:'numeric'}) : '—';

        const rows = list.map(r => `
            <tr>
                <td>${r.reservationNo}</td>
                <td>${r.guestName || '—'}</td>
                <td>${fmt(r.checkInDate)}</td>
                <td>${fmt(r.checkOutDate)}</td>
                <td>${(r.rooms || []).map(rm => rm.roomName).join(', ') || '—'}</td>
                <td><span class="status-${(r.status || '').toLowerCase()}">${r.status}</span></td>
                <td style="text-align:right">LKR ${parseFloat(r.totalAmount || 0).toFixed(2)}</td>
            </tr>`).join('');

        const html = `<!DOCTYPE html><html><head><meta charset="UTF-8">
            <title>Reservation Report</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { font-family: 'Arial', sans-serif; font-size: 12px; color: #111; padding: 32px; }
                .header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 28px; border-bottom: 3px solid #1d4ed8; padding-bottom: 16px; }
                .resort-name { font-size: 24px; font-weight: 900; color: #1d4ed8; letter-spacing: -0.5px; }
                .resort-sub { font-size: 10px; color: #6b7280; margin-top: 2px; }
                .report-title { font-size: 11px; color: #374151; text-align: right; }
                .stats { display: grid; grid-template-columns: repeat(4,1fr); gap: 12px; margin-bottom: 24px; }
                .stat { background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px; }
                .stat-label { font-size: 9px; color: #6b7280; text-transform: uppercase; letter-spacing: .5px; }
                .stat-val { font-size: 20px; font-weight: 800; color: #111827; margin-top: 2px; }
                .stat-val.revenue { color: #059669; }
                table { width: 100%; border-collapse: collapse; }
                th { background: #1e40af; color: white; padding: 8px 10px; text-align: left; font-size: 10px; text-transform: uppercase; letter-spacing: .5px; }
                td { padding: 7px 10px; border-bottom: 1px solid #e5e7eb; font-size: 11px; }
                tr:nth-child(even) td { background: #f8fafc; }
                .status-confirmed { color: #1d4ed8; font-weight: 600; }
                .status-completed { color: #059669; font-weight: 600; }
                .status-cancelled { color: #dc2626; font-weight: 600; }
                .footer { margin-top: 24px; text-align: center; font-size: 10px; color: #9ca3af; border-top: 1px solid #e5e7eb; padding-top: 12px; }
                @media print { body { padding: 16px; } }
            </style></head><body>
            <div class="header">
                <div><div class="resort-name">Ocean View Resort</div><div class="resort-sub">Reservation Management System</div></div>
                <div class="report-title">
                    <strong>RESERVATION REPORT</strong><br>
                    Period: ${fmt(from)} — ${fmt(to)}<br>
                    Generated: ${new Date().toLocaleString('en-GB')}
                </div>
            </div>
            <div class="stats">
                <div class="stat"><div class="stat-label">Total</div><div class="stat-val">${list.length}</div></div>
                <div class="stat"><div class="stat-label">Confirmed</div><div class="stat-val">${list.filter(r=>r.status==='CONFIRMED').length}</div></div>
                <div class="stat"><div class="stat-label">Completed</div><div class="stat-val">${list.filter(r=>r.status==='COMPLETED').length}</div></div>
                <div class="stat"><div class="stat-label">Revenue (Completed)</div><div class="stat-val revenue">LKR ${revenue.toFixed(2)}</div></div>
            </div>
            <table><thead><tr><th>Ref No.</th><th>Guest</th><th>Check In</th><th>Check Out</th><th>Rooms</th><th>Status</th><th>Total</th></tr></thead>
            <tbody>${rows}</tbody></table>
            <div class="footer">Ocean View Resort · Confidential Report · ${new Date().getFullYear()}</div>
            <script>window.onload = () => window.print();<\/script></body></html>`;

        const win = window.open('', '_blank', 'width=900,height=700');
        win.document.write(html);
        win.document.close();
    },

    viewReservation: async (reservationNo) => {
        const modal        = document.getElementById('resModal');
        const modalContent = document.getElementById('resModalContent');

        modalContent.innerHTML = `
            <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/80 sticky top-0">
                <h3 class="text-lg font-bold text-gray-900"><i class="fa-solid fa-eye text-blue-600 mr-2"></i>Reservation Details</h3>
                <button onclick="ReservationsModule.closeModal()" class="text-gray-400 hover:text-gray-600 p-1 rounded-md hover:bg-gray-200"><i class="fa-solid fa-xmark text-lg"></i></button>
            </div>
            <div class="p-6 flex flex-col items-center justify-center py-12">
                <div class="animate-spin h-8 w-8 border-4 border-gray-200 border-t-blue-500 rounded-full"></div>
                <p class="text-gray-400 mt-3 text-sm">Loading reservation...</p>
            </div>`;

        modal.classList.remove('hidden');
        setTimeout(() => { modal.classList.remove('opacity-0'); modalContent.classList.remove('scale-95'); }, 10);

        try {
            const r   = await fetchAPI(`/reservations/${reservationNo}`);
            const fmt = d => d ? new Date(d).toLocaleDateString('en-GB', {day:'2-digit',month:'short',year:'numeric'}) : '—';
            const nights = r.checkInDate && r.checkOutDate
                ? Math.round((new Date(r.checkOutDate) - new Date(r.checkInDate)) / 86400000) : 0;

            const statusCfg = {
                CONFIRMED: 'bg-blue-100 text-blue-700 border border-blue-200',
                COMPLETED: 'bg-emerald-100 text-emerald-700 border border-emerald-200',
                CANCELLED: 'bg-rose-100 text-rose-700 border border-rose-200',
            };

            const roomRows = (r.rooms || []).map(rm => {
                const baseLine = (parseFloat(rm.ratePerNight) || 0) * nights;
                const actualLine = parseFloat(rm.lineTotal) || 0;
                let markupText = '';
                
                if (actualLine > baseLine && baseLine > 0) {
                    const diff = actualLine - baseLine;
                    const percent = Math.round((diff / baseLine) * 100);
                    markupText = `<div class="text-[10px] text-amber-600 mt-0.5 whitespace-nowrap">+${percent}% Room Type Markup</div>`;
                }

                return `
                <tr class="border-b border-gray-100">
                    <td class="py-2 text-gray-800 font-medium">${rm.roomName}</td>
                    <td class="py-2 text-center text-gray-500">${nights} night${nights!==1?'s':''}</td>
                    <td class="py-2 text-right text-gray-600">LKR ${parseFloat(rm.ratePerNight||0).toFixed(2)}</td>
                    <td class="py-2 text-right font-semibold text-gray-800">
                        LKR ${actualLine.toFixed(2)}
                        ${markupText}
                    </td>
                </tr>`;
            }).join('');

            modalContent.innerHTML = `
                <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/80 sticky top-0">
                    <h3 class="text-lg font-bold text-gray-900"><i class="fa-solid fa-file-lines text-blue-600 mr-2"></i>Reservation Details</h3>
                    <button onclick="ReservationsModule.closeModal()" class="text-gray-400 hover:text-gray-600 p-1 rounded-md hover:bg-gray-200"><i class="fa-solid fa-xmark text-lg"></i></button>
                </div>
                <div class="p-6 space-y-5">
                    <!-- Ref + Status -->
                    <div class="flex justify-between items-start">
                        <div>
                            <p class="text-xs text-gray-400 uppercase tracking-wider">Reference Number</p>
                            <p class="text-2xl font-black font-mono text-blue-600 mt-1">${r.reservationNo}</p>
                        </div>
                        <span class="px-3 py-1.5 text-sm font-semibold rounded-full ${statusCfg[r.status] || statusCfg.CONFIRMED}">${r.status}</span>
                    </div>

                    <!-- Guest + Dates grid -->
                    <div class="grid grid-cols-2 gap-4">
                        <div class="bg-gray-50 rounded-xl p-4">
                            <p class="text-xs text-gray-400 uppercase tracking-wider mb-1"><i class="fa-solid fa-user mr-1"></i>Guest</p>
                            <p class="font-semibold text-gray-900">${r.guestName || '—'}</p>
                            <p class="text-xs text-gray-400 mt-0.5">ID #${r.guestId || '—'}</p>
                        </div>
                        <div class="bg-gray-50 rounded-xl p-4">
                            <p class="text-xs text-gray-400 uppercase tracking-wider mb-1"><i class="fa-solid fa-moon mr-1"></i>Stay Duration</p>
                            <p class="font-semibold text-gray-900">${nights} night${nights!==1?'s':''}</p>
                            <p class="text-xs text-gray-400 mt-0.5">${fmt(r.checkInDate)} → ${fmt(r.checkOutDate)}</p>
                        </div>
                    </div>

                    <!-- Room Breakdown -->
                    <div class="bg-white border border-gray-200 rounded-xl overflow-hidden">
                        <div class="px-4 py-3 bg-gray-50 border-b border-gray-200">
                            <p class="text-xs font-semibold text-gray-700 uppercase tracking-wider"><i class="fa-solid fa-bed mr-1"></i>Room Breakdown</p>
                        </div>
                        <div class="px-4">
                            <table class="w-full text-sm">
                                <thead><tr class="text-xs text-gray-400 uppercase">
                                    <th class="py-2 text-left">Room</th>
                                    <th class="py-2 text-center">Nights</th>
                                    <th class="py-2 text-right">Rate/Night</th>
                                    <th class="py-2 text-right">Subtotal</th>
                                </tr></thead>
                                <tbody>${roomRows || '<tr><td colspan="4" class="py-4 text-center text-gray-400">No rooms.</td></tr>'}</tbody>
                            </table>
                        </div>
                        <div class="px-4 py-3 border-t border-gray-200 bg-gray-50 flex justify-between items-center">
                            <span class="text-sm font-semibold text-gray-700">Total Amount</span>
                            <span class="text-xl font-black text-emerald-600">LKR ${parseFloat(r.totalAmount||0).toFixed(2)}</span>
                        </div>
                    </div>

                    <!-- Created -->
                    <p class="text-xs text-gray-400 text-right">Created: ${r.createdAt ? new Date(r.createdAt).toLocaleString('en-GB') : '—'}</p>
                </div>
                <div class="px-6 py-4 border-t border-gray-100 flex justify-between items-center bg-white sticky bottom-0">
                    <button onclick="ReservationsModule.closeModal()" class="px-4 py-2 text-sm text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">Close</button>
                    <button onclick="ReservationsModule.downloadBill(${JSON.stringify(r).replace(/"/g,'&quot;')})"
                        class="px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-lg shadow-sm flex items-center gap-2 transition-colors">
                        <i class="fa-solid fa-file-invoice"></i> Download Bill
                    </button>
                </div>`;
        } catch (e) {
            modalContent.innerHTML += `<div class="p-6 text-red-500 text-center">${e.message}</div>`;
        }
    },

    downloadBill: (r) => {
        const fmt = d => d ? new Date(d).toLocaleDateString('en-GB', {day:'2-digit',month:'short',year:'numeric'}) : '—';
        const nights = r.checkInDate && r.checkOutDate
            ? Math.round((new Date(r.checkOutDate) - new Date(r.checkInDate)) / 86400000) : 0;

        const roomRows = (r.rooms || []).map(rm => {
            const baseLine = (parseFloat(rm.ratePerNight) || 0) * nights;
            const actualLine = parseFloat(rm.lineTotal) || 0;
            let markupText = '';
            
            if (actualLine > baseLine && baseLine > 0) {
                const percent = Math.round(((actualLine - baseLine) / baseLine) * 100);
                markupText = `<br><span style="font-size:9px;color:#d97706">+${percent}% Room Type Markup</span>`;
            }

            return `
            <tr>
                <td>${rm.roomName}</td>
                <td style="text-align:center">${nights}</td>
                <td style="text-align:right">LKR ${parseFloat(rm.ratePerNight||0).toFixed(2)}</td>
                <td style="text-align:right">
                    LKR ${actualLine.toFixed(2)}
                    ${markupText}
                </td>
            </tr>`;
        }).join('');

        const statusColor = { CONFIRMED:'#1d4ed8', COMPLETED:'#059669', CANCELLED:'#dc2626' };
        const sColor = statusColor[r.status] || '#1d4ed8';

        const html = `<!DOCTYPE html><html><head><meta charset="UTF-8">
            <title>Bill - ${r.reservationNo}</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { font-family: Arial, sans-serif; font-size: 13px; color: #111; background: #fff; }
                .bill { max-width: 680px; margin: 0 auto; padding: 40px 48px; }
                .top { display: flex; justify-content: space-between; align-items: flex-start; padding-bottom: 20px; border-bottom: 3px solid #1d4ed8; margin-bottom: 28px; }
                .resort { font-size: 28px; font-weight: 900; color: #1d4ed8; }
                .resort-sub { font-size: 11px; color: #6b7280; margin-top: 3px; }
                .bill-label { font-size: 22px; font-weight: 700; color: #111; text-align: right; }
                .bill-no { font-size: 12px; color: #6b7280; text-align: right; margin-top: 4px; }
                .section { margin-bottom: 20px; }
                .section-title { font-size: 10px; color: #6b7280; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; }
                .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
                .info-box { background: #f9fafb; border-radius: 8px; padding: 12px 16px; }
                .info-box .label { font-size: 10px; color: #9ca3af; text-transform: uppercase; letter-spacing: .5px; }
                .info-box .val { font-size: 14px; font-weight: 700; color: #111827; margin-top: 2px; }
                .info-box .sub { font-size: 11px; color: #6b7280; margin-top: 2px; }
                table { width: 100%; border-collapse: collapse; }
                thead th { background: #1e40af; color: white; padding: 10px 14px; font-size: 11px; text-align: left; }
                thead th:last-child, thead th:nth-child(3), thead th:nth-child(2) { text-align: right; }
                thead th:nth-child(2) { text-align: center; }
                tbody td { padding: 10px 14px; border-bottom: 1px solid #e5e7eb; font-size: 12px; }
                tbody td:nth-child(2) { text-align: center; }
                tbody td:nth-child(3), tbody td:last-child { text-align: right; }
                .total-row { background: #f0fdf4; }
                .total-row td { font-size: 16px; font-weight: 800; color: #059669; padding: 14px; }
                .status-badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 700;
                    color: ${sColor}; background: ${sColor}18; border: 1px solid ${sColor}44; }
                .footer { margin-top: 40px; text-align: center; font-size: 11px; color: #9ca3af; border-top: 1px solid #e5e7eb; padding-top: 16px; }
                .watermark { color: ${sColor}; font-size: 11px; font-weight: 700; }
                @media print {
                    .no-print { display: none !important; }
                    body { background: white; }
                }
            </style></head>
            <body>
            <div class="bill">
                <div class="top">
                    <div><div class="resort">Ocean View Resort</div><div class="resort-sub">Premium Coastal Experience · Reservations & Billing</div></div>
                    <div><div class="bill-label">INVOICE</div><div class="bill-no">${r.reservationNo}</div></div>
                </div>

                <div class="section">
                    <div class="section-title">Booking Information</div>
                    <div class="info-grid">
                        <div class="info-box">
                            <div class="label">Guest</div>
                            <div class="val">${r.guestName || '—'}</div>
                            <div class="sub">Guest ID #${r.guestId || '—'}</div>
                        </div>
                        <div class="info-box">
                            <div class="label">Status</div>
                            <div class="val"><span class="status-badge">${r.status}</span></div>
                        </div>
                        <div class="info-box">
                            <div class="label">Check-In</div>
                            <div class="val">${fmt(r.checkInDate)}</div>
                        </div>
                        <div class="info-box">
                            <div class="label">Check-Out</div>
                            <div class="val">${fmt(r.checkOutDate)}</div>
                            <div class="sub">${nights} night${nights!==1?'s':''}</div>
                        </div>
                    </div>
                </div>

                <div class="section">
                    <div class="section-title">Room Charges</div>
                    <table>
                        <thead><tr><th>Room</th><th>Nights</th><th>Rate/Night</th><th>Subtotal</th></tr></thead>
                        <tbody>
                            ${roomRows}
                            <tr class="total-row"><td colspan="3" style="text-align:right;font-size:13px">Total Amount</td><td style="text-align:right">LKR ${parseFloat(r.totalAmount||0).toFixed(2)}</td></tr>
                        </tbody>
                    </table>
                </div>

                <div style="margin-top:20px;text-align:right;">
                    <div class="watermark">● ${r.status}</div>
                </div>

                <div class="footer">
                    <p>Thank you for choosing Ocean View Resort</p>
                    <p style="margin-top:4px">This is a computer-generated invoice · Generated on ${new Date().toLocaleString('en-GB')}</p>
                </div>

                <div class="no-print" style="text-align:center;margin-top:24px">
                    <button onclick="window.print()" style="background:#1d4ed8;color:white;border:none;padding:12px 28px;border-radius:8px;font-size:14px;font-weight:700;cursor:pointer;">
                        🖨️ Print / Save as PDF
                    </button>
                </div>
            </div>
            </body></html>`;

        const win = window.open('', '_blank', 'width=750,height=900');
        win.document.write(html);
        win.document.close();
    },

    openNewReservationWizard: () => {
        ReservationsModule._selectedRooms = new Map();
        ReservationsModule._guestId       = null;
        ReservationsModule._guestName     = '';

        const modal        = document.getElementById('resModal');
        const modalContent = document.getElementById('resModalContent');

        modalContent.innerHTML = `
            <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/80 sticky top-0 z-10">
                <h3 class="text-lg font-bold text-gray-900"><i class="fa-solid fa-calendar-plus text-blue-600 mr-2"></i>New Reservation</h3>
                <button onclick="ReservationsModule.closeModal()" class="text-gray-400 hover:text-gray-600 p-1 rounded-md hover:bg-gray-200"><i class="fa-solid fa-xmark text-lg"></i></button>
            </div>

            <div class="p-6 space-y-6">

                <!-- STEP 1: Dates -->
                <div class="space-y-3">
                    <h4 class="text-sm font-semibold text-gray-800 flex items-center gap-2">
                        <span class="w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-xs font-bold">1</span>
                        Select Dates
                    </h4>
                    <div class="grid grid-cols-2 gap-4 pl-8">
                        <div>
                            <label class="block text-xs font-medium text-gray-600 mb-1">Check In <span class="text-red-500">*</span></label>
                            <input type="date" id="wCheckIn" onchange="ReservationsModule.onDatesChange()"
                                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 focus:bg-white transition-all">
                        </div>
                        <div>
                            <label class="block text-xs font-medium text-gray-600 mb-1">Check Out <span class="text-red-500">*</span></label>
                            <input type="date" id="wCheckOut" onchange="ReservationsModule.onDatesChange()"
                                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 focus:bg-white transition-all">
                        </div>
                    </div>
                    <!-- nights display -->
                    <div id="nightsDisplay" class="hidden pl-8 text-xs text-blue-600 font-semibold"></div>
                </div>

                <!-- Available Rooms -->
                <div class="space-y-3">
                    <h4 class="text-sm font-semibold text-gray-800 flex items-center gap-2">
                        <span class="w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-xs font-bold">2</span>
                        Select Room(s)
                        <span id="roomsSelectedBadge" class="hidden ml-1 px-2 py-0.5 bg-blue-100 text-blue-700 text-xs rounded-full font-bold"></span>
                    </h4>
                    <div id="availableRoomsArea" class="pl-8 text-sm text-gray-400 italic">
                        Pick dates first to see available rooms.
                    </div>
                </div>

                <!-- Guest -->
                <div class="space-y-3">
                    <h4 class="text-sm font-semibold text-gray-800 flex items-center gap-2">
                        <span class="w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-xs font-bold">3</span>
                        Guest
                    </h4>
                    <div class="pl-8 space-y-3">
                        <!-- Search -->
                        <div class="flex gap-2">
                            <div class="flex-1">
                                <select id="guestSearchType" class="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 mr-2">
                                    <option value="phone">Phone</option>
                                    <option value="nic">NIC</option>
                                </select>
                            </div>
                            <div class="flex-[3] flex gap-2">
                                <input type="text" id="guestSearchVal" placeholder="Search existing guest..."
                                    class="flex-1 px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 focus:bg-white">
                                <button onclick="ReservationsModule.searchGuest()"
                                    class="bg-gray-700 hover:bg-gray-800 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors whitespace-nowrap">
                                    <i class="fa-solid fa-search mr-1"></i> Search
                                </button>
                            </div>
                        </div>
                        <!-- Guest result / inline form -->
                        <div id="guestArea">
                            <p class="text-xs text-gray-400 italic">Search for an existing guest, or register a new guest below.</p>
                        </div>

                        <!-- New guest form (initially hidden) -->
                        <div id="newGuestForm" class="hidden bg-blue-50 border border-blue-200 rounded-xl p-4 space-y-3">
                            <p class="text-xs font-semibold text-blue-700"><i class="fa-solid fa-user-plus mr-1"></i> New Guest Details</p>
                            <div class="grid grid-cols-2 gap-3">
                                <div><label class="block text-xs font-medium text-gray-600 mb-1">Full Name *</label>
                                    <input type="text" id="ngName" placeholder="Jane Doe"
                                        class="w-full px-3 py-1.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-400 outline-none bg-white"></div>
                                <div><label class="block text-xs font-medium text-gray-600 mb-1">Email *</label>
                                    <input type="email" id="ngEmail" placeholder="jane@email.com"
                                        class="w-full px-3 py-1.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-400 outline-none bg-white"></div>
                                <div><label class="block text-xs font-medium text-gray-600 mb-1">Phone *</label>
                                    <input type="text" id="ngPhone" placeholder="0789362885"
                                        class="w-full px-3 py-1.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-400 outline-none bg-white"></div>
                                <div><label class="block text-xs font-medium text-gray-600 mb-1">NIC *</label>
                                    <input type="text" id="ngNic" placeholder="200401500020"
                                        class="w-full px-3 py-1.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-400 outline-none bg-white"></div>
                                <div class="col-span-2"><label class="block text-xs font-medium text-gray-600 mb-1">Address</label>
                                    <input type="text" id="ngAddress" placeholder="12 Beach Road, Galle"
                                        class="w-full px-3 py-1.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-400 outline-none bg-white"></div>
                            </div>
                            <!-- Explicit Add Guest button -->
                            <div class="flex gap-2 items-center pt-1">
                                <button id="addGuestBtn" onclick="ReservationsModule.addNewGuest()"
                                    class="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition-colors flex items-center justify-center gap-2">
                                    <i class="fa-solid fa-user-plus"></i> Add Guest
                                </button>
                                <button onclick="ReservationsModule.toggleNewGuestForm()"
                                    class="px-4 py-2 text-sm text-gray-500 hover:text-gray-700 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
                                    Cancel
                                </button>
                            </div>
                            <div id="newGuestError" class="hidden text-xs text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2"></div>
                        </div>

                        <button onclick="ReservationsModule.toggleNewGuestForm()"
                            id="toggleNewGuestBtn"
                            class="text-xs text-blue-600 hover:text-blue-800 underline font-medium transition-colors">
                            + Register new guest instead
                        </button>
                    </div>
                </div>

                <!-- Price Summary -->
                <div id="priceSummary" class="hidden pl-8 bg-gradient-to-br from-gray-50 to-blue-50 border border-gray-200 rounded-xl p-4 space-y-2">
                    <h4 class="text-sm font-semibold text-gray-700 mb-2"><i class="fa-solid fa-file-invoice-dollar text-blue-500 mr-1"></i> Price Summary</h4>
                    <div id="priceLines" class="space-y-1 text-sm"></div>
                    <div class="border-t border-gray-300 pt-2 flex justify-between font-bold text-base text-gray-900">
                        <span>Estimated Total</span>
                        <span id="priceTotal" class="text-emerald-600"></span>
                    </div>
                    <p class="text-xs text-gray-400">* Final price calculated server-side including type surcharges.</p>
                </div>

                <!-- Error -->
                <div id="wizardError" class="hidden bg-red-50 border border-red-200 rounded-lg px-4 py-3 text-sm text-red-600 pl-8"></div>

            </div>

            <!-- Footer -->
            <div class="px-6 py-4 border-t border-gray-100 flex justify-between items-center bg-white sticky bottom-0">
                <button onclick="ReservationsModule.closeModal()" class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
                    Cancel
                </button>
                <button onclick="ReservationsModule.submitReservation()" id="submitResBtn"
                    class="px-6 py-2.5 text-sm font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors shadow-sm flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed">
                    <i class="fa-solid fa-calendar-check"></i> Confirm Reservation
                </button>
            </div>
        `;

        // Set min dates to today
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('wCheckIn').min  = today;
        document.getElementById('wCheckOut').min = today;

        modal.classList.remove('hidden');
        setTimeout(() => {
            modal.classList.remove('opacity-0');
            modalContent.classList.remove('scale-95');
        }, 10);
    },

    closeModal: () => {
        const modal        = document.getElementById('resModal');
        const modalContent = document.getElementById('resModalContent');
        modal.classList.add('opacity-0');
        if (modalContent) modalContent.classList.add('scale-95');
        setTimeout(() => modal.classList.add('hidden'), 300);
    },

    // Dates changed — fetch available rooms
    onDatesChange: async () => {
        const checkIn  = document.getElementById('wCheckIn').value;
        const checkOut = document.getElementById('wCheckOut').value;
        const nightsEl = document.getElementById('nightsDisplay');
        const roomArea = document.getElementById('availableRoomsArea');

        if (!checkIn || !checkOut || checkIn >= checkOut) {
            nightsEl.classList.add('hidden');
            roomArea.innerHTML = `<p class="text-xs text-rose-500">Check-out must be after check-in.</p>`;
            return;
        }

        // Show nights
        const nights = Math.round((new Date(checkOut) - new Date(checkIn)) / 86400000);
        nightsEl.textContent = `${nights} night${nights !== 1 ? 's' : ''}`;
        nightsEl.classList.remove('hidden');
        ReservationsModule._nights = nights;

        roomArea.innerHTML = `<div class="flex items-center gap-2 text-gray-400 text-sm"><div class="animate-spin h-4 w-4 border-2 border-gray-300 border-t-blue-500 rounded-full"></div> Fetching available rooms...</div>`;
        ReservationsModule._selectedRooms.clear();
        ReservationsModule.updatePriceSummary();

        try {
            const rooms = await fetchAPI(`/rooms/available?checkIn=${checkIn}&checkOut=${checkOut}`);
            ReservationsModule.renderAvailableRooms(rooms);
        } catch (e) {
            roomArea.innerHTML = `<p class="text-xs text-rose-500">Failed to load rooms: ${e.message}</p>`;
        }
    },

    renderAvailableRooms: (rooms) => {
        const area = document.getElementById('availableRoomsArea');
        if (!rooms || rooms.length === 0) {
            area.innerHTML = `<p class="text-sm text-amber-600"><i class="fa-solid fa-triangle-exclamation mr-1"></i> No rooms available for these dates.</p>`;
            return;
        }
        area.innerHTML = `
            <p class="text-xs text-gray-500 mb-2">${rooms.length} room(s) available — click to select (multi-select allowed):</p>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-2" id="roomGrid">
            ${rooms.map(r => `
                <div id="roomCard_${r.roomId}"
                    onclick="ReservationsModule.toggleRoom(${r.roomId})"
                    data-id="${r.roomId}"
                    data-name="${r.roomName}"
                    data-price="${r.roomPrice}"
                    data-type="${r.roomType || 'STANDARD'}"
                    class="cursor-pointer border-2 border-gray-200 rounded-xl p-3 hover:border-blue-400 hover:bg-blue-50/50 transition-all select-none">
                    <div class="flex justify-between items-start">
                        <div>
                            <div class="font-semibold text-gray-900 text-sm">${r.roomName}</div>
                            <div class="text-xs text-gray-400 uppercase tracking-wide mt-0.5">${r.roomType || 'Standard'}</div>
                        </div>
                        <div class="text-right">
                            <div class="font-bold text-emerald-600 text-sm">LKR ${parseFloat(r.roomPrice).toFixed(2)}</div>
                            <div class="text-xs text-gray-400">/night</div>
                        </div>
                    </div>
                    <div id="roomCheck_${r.roomId}" class="hidden mt-2 text-blue-600 text-xs font-semibold flex items-center gap-1">
                        <i class="fa-solid fa-check-circle"></i> Selected
                    </div>
                </div>
            `).join('')}
            </div>`;
    },

    toggleRoom: (roomId) => {
        const card  = document.getElementById(`roomCard_${roomId}`);
        const check = document.getElementById(`roomCheck_${roomId}`);

        if (ReservationsModule._selectedRooms.has(roomId)) {
            ReservationsModule._selectedRooms.delete(roomId);
            card.classList.remove('border-blue-500', 'bg-blue-50');
            card.classList.add('border-gray-200');
            check.classList.add('hidden');
        } else {
            const el = card;
            ReservationsModule._selectedRooms.set(roomId, {
                roomId,
                roomName:  el.dataset.name,
                roomPrice: parseFloat(el.dataset.price),
                roomType:  el.dataset.type,
            });
            card.classList.add('border-blue-500', 'bg-blue-50');
            card.classList.remove('border-gray-200');
            check.classList.remove('hidden');
        }

        // Update badge
        const badge = document.getElementById('roomsSelectedBadge');
        const count = ReservationsModule._selectedRooms.size;
        if (count > 0) {
            badge.textContent = `${count} selected`;
            badge.classList.remove('hidden');
        } else {
            badge.classList.add('hidden');
        }

        ReservationsModule.updatePriceSummary();
    },

    updatePriceSummary: () => {
        const nights  = ReservationsModule._nights || 0;
        const rooms   = [...ReservationsModule._selectedRooms.values()];
        const summaryEl = document.getElementById('priceSummary');

        if (rooms.length === 0 || nights === 0) {
            if (summaryEl) summaryEl.classList.add('hidden');
            return;
        }

        let total = 0;
        const lines = rooms.map(r => {
            let rate = r.roomPrice;
            let markupPercent = 0;
            
            // Apply backend logic locally for accurate pre-calculation summary
            if (r.roomType === 'DELUXE') markupPercent = 10;
            else if (r.roomType === 'FAMILY_SUITE') markupPercent = 20;
            
            const multiplier = 1 + (markupPercent / 100);
            const line = rate * multiplier * nights;
            total += line;
            
            let markupLabel = markupPercent > 0 ? `<span class="text-[10px] text-amber-600 block ml-auto">+${markupPercent}% type markup</span>` : '';

            return `<div class="flex flex-col text-gray-600 mb-2">
                <div class="flex justify-between">
                    <span>${r.roomName} × ${nights} night${nights !== 1 ? 's' : ''}</span>
                    <span>LKR ${line.toFixed(2)}</span>
                </div>
                ${markupLabel}
            </div>`;
        });

        document.getElementById('priceLines').innerHTML = lines.join('');
        document.getElementById('priceTotal').textContent = `LKR ${total.toFixed(2)}`;
        summaryEl.classList.remove('hidden');
    },

    // Guest Search
    searchGuest: async () => {
        const type = document.getElementById('guestSearchType').value;
        const val  = document.getElementById('guestSearchVal').value.trim();
        if (!val) { 
            if (typeof showToast === 'function') showToast('Enter a search value.', 'error');
            return; 
        }

        const area = document.getElementById('guestArea');
        area.innerHTML = `<div class="flex items-center gap-2 text-gray-400 text-xs"><div class="animate-spin h-3 w-3 border-2 border-gray-300 border-t-blue-500 rounded-full"></div> Searching...</div>`;

        try {
            const results = await fetchAPI(`/guests/search?${type}=${encodeURIComponent(val)}`);
            if (results && results.length > 0) {
                const g = results[0];
                ReservationsModule._guestId   = g.guestId;
                ReservationsModule._guestName = g.name;
                area.innerHTML = `
                    <div class="flex items-center gap-3 bg-emerald-50 border border-emerald-200 rounded-xl p-3">
                        <div class="w-9 h-9 rounded-full bg-emerald-200 flex items-center justify-center text-emerald-700 font-bold">
                            ${(g.name || '?')[0].toUpperCase()}
                        </div>
                        <div class="flex-1">
                            <div class="font-semibold text-gray-900 text-sm">${g.name}</div>
                            <div class="text-xs text-gray-500">${g.email || ''} · ${g.phoneNumber || ''}</div>
                        </div>
                        <i class="fa-solid fa-circle-check text-emerald-500 text-lg"></i>
                    </div>`;
                // Collapse new guest form
                document.getElementById('newGuestForm').classList.add('hidden');
                document.getElementById('toggleNewGuestBtn').textContent = '+ Register new guest instead';
            } else {
                ReservationsModule._guestId = null;
                area.innerHTML = `<p class="text-xs text-amber-600"><i class="fa-solid fa-triangle-exclamation mr-1"></i> Guest not found. Register them below.</p>`;
                ReservationsModule.showNewGuestForm();
            }
        } catch (e) {
            ReservationsModule._guestId = null;
            area.innerHTML = `<p class="text-xs text-amber-600"><i class="fa-solid fa-triangle-exclamation mr-1"></i> Not found — fill in new guest details below.</p>`;
            ReservationsModule.showNewGuestForm();
        }
    },

    showNewGuestForm: () => {
        document.getElementById('newGuestForm').classList.remove('hidden');
        document.getElementById('toggleNewGuestBtn').textContent = '– Hide new guest form';
    },

    toggleNewGuestForm: () => {
        const form = document.getElementById('newGuestForm');
        const btn  = document.getElementById('toggleNewGuestBtn');
        const hidden = form.classList.toggle('hidden');
        btn.textContent = hidden ? '+ Register new guest instead' : '– Hide new guest form';
    },

    addNewGuest: async () => {
        const ngName  = document.getElementById('ngName').value.trim();
        const ngEmail = document.getElementById('ngEmail').value.trim();
        const ngPhone = document.getElementById('ngPhone').value.trim();
        const ngNic   = document.getElementById('ngNic').value.trim();
        const ngAddr  = document.getElementById('ngAddress').value.trim();
        const errEl   = document.getElementById('newGuestError');
        const btn     = document.getElementById('addGuestBtn');

        errEl.classList.add('hidden');

        // Validate
        if (!ngName || !ngEmail || !ngPhone || !ngNic) {
            errEl.textContent = 'Name, Email, Phone and NIC are required.';
            errEl.classList.remove('hidden');
            return;
        }

        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Creating...';

        try {
            const created = await fetchAPI('/guests/', 'POST', {
                name: ngName, email: ngEmail, phoneNumber: ngPhone, nic: ngNic, address: ngAddr
            });

            // Store the confirmed guestId
            ReservationsModule._guestId   = created.guestId;
            ReservationsModule._guestName = created.name;

            // Hide the form, show confirmed guest card
            document.getElementById('newGuestForm').classList.add('hidden');
            document.getElementById('toggleNewGuestBtn').textContent = '+ Register new guest instead';
            
            if (typeof showToast === 'function') showToast('Guest registered successfully', 'success');

            document.getElementById('guestArea').innerHTML = `
                <div class="flex items-center gap-3 bg-emerald-50 border border-emerald-200 rounded-xl p-3">
                    <div class="w-9 h-9 rounded-full bg-emerald-200 flex items-center justify-center text-emerald-700 font-bold text-sm">
                        ${(created.name || '?')[0].toUpperCase()}
                    </div>
                    <div class="flex-1">
                        <div class="font-semibold text-gray-900 text-sm">${created.name}</div>
                        <div class="text-xs text-gray-500">${created.email || ''} &bull; ID #${created.guestId}</div>
                    </div>
                    <div class="flex flex-col items-end gap-1">
                        <i class="fa-solid fa-circle-check text-emerald-500 text-lg"></i>
                        <span class="text-xs text-emerald-600 font-medium">Guest Registered</span>
                    </div>
                </div>`;

        } catch (e) {
            errEl.textContent = 'Failed to register guest: ' + (e.message || 'Server error. Check guest details.');
            errEl.classList.remove('hidden');
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-user-plus"></i> Add Guest';
        }
    },

    submitReservation: async () => {
        const errEl  = document.getElementById('wizardError');
        const btn    = document.getElementById('submitResBtn');
        errEl.classList.add('hidden');

        const checkIn  = document.getElementById('wCheckIn').value;
        const checkOut = document.getElementById('wCheckOut').value;

        if (!checkIn || !checkOut) { errEl.textContent = 'Please select check-in and check-out dates.'; errEl.classList.remove('hidden'); return; }
        if (checkIn >= checkOut)   { errEl.textContent = 'Check-out must be after check-in.'; errEl.classList.remove('hidden'); return; }
        if (ReservationsModule._selectedRooms.size === 0) { errEl.textContent = 'Please select at least one room.'; errEl.classList.remove('hidden'); return; }

        let guestId = ReservationsModule._guestId;

        if (!guestId) {
            const formVisible = !document.getElementById('newGuestForm').classList.contains('hidden');
            errEl.textContent = formVisible
                ? 'Please click \"Add Guest\" first to register the new guest, then confirm the reservation.'
                : 'Please search for a guest or register a new guest using the form below.';
            errEl.classList.remove('hidden');
            return;
        }

        const payload = {
            guestId,
            checkInDate:  checkIn,
            checkOutDate: checkOut,
            status:       'CONFIRMED',
            roomIds:      [...ReservationsModule._selectedRooms.keys()]
        };

        try {
            btn.disabled = true;
            btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Confirming...';
            const result = await fetchAPI('/reservations/', 'POST', payload);
            ReservationsModule.closeModal();
            await ReservationsModule.loadReservations();
            if (typeof showToast === 'function') showToast(`Reservation ${result.reservationNo} confirmed! Total: LKR ${parseFloat(result.totalAmount || 0).toFixed(2)}`, 'success');
        } catch (e) {
            errEl.textContent = 'Reservation failed: ' + e.message;
            errEl.classList.remove('hidden');
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-calendar-check"></i> Confirm Reservation';
        }
    },

    // Global showToast from api.js is used
};
