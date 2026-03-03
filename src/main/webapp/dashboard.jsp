<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Ocean View Resort</title>
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
        body { font-family: 'Inter', sans-serif; }
    </style>
</head>
<body class="bg-gray-50 flex h-screen overflow-hidden">

    <!-- Include Sidebar -->
    <jsp:include page="sidebar.jsp" />

    <!-- Main Content Area -->
    <main class="flex-1 flex flex-col">
        <!-- Include Top Header -->
        <jsp:include page="top-header.jsp">
             <jsp:param name="title" value="Dashboard Overview"/>
        </jsp:include>

        <!-- Dynamic Content Body -->
        <div class="flex-1 p-8 overflow-y-auto bg-gray-50/50 relative" id="mainContent">
            <!-- Loading Spinner -->
            <div id="loader" class="hidden absolute inset-0 bg-white/50 backdrop-blur-sm z-50 flex items-center justify-center">
                <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            </div>

            <!-- View Container -->
            <div id="viewContainer" class="max-w-7xl mx-auto">
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
            </div>
        </div>
    </main>

    <!-- Modals Container (Injected globally) -->
    <div id="modalContainer"></div>

    <!-- Scripts -->
    <script>
        window.APP_CONTEXT = '<%= request.getContextPath() %>';
    </script>
    <script src="assets/js/api.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', async () => {
            // Dashboard metrics
            const loader = document.getElementById('loader');
            loader.classList.remove('hidden');
            try {
                const rooms = await fetchAPI('/rooms/');
                if (rooms) document.getElementById('dashTotalRooms').textContent = rooms.length;
            } catch(e) { console.error(e); }
            try {
                const guests = await fetchAPI('/guests/');
                if (guests) document.getElementById('dashActiveGuests').textContent = guests.length;
            } catch(e) { console.error(e); }
            loader.classList.add('hidden');
        });
    </script>
</body>
</html>
