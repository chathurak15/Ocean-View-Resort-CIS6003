<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Help Guide - Ocean View Resort</title>
    <meta name="description" content="Help and documentation for Ocean View Resort management system.">
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap');
        body { font-family: 'Inter', sans-serif; }
        ::-webkit-scrollbar       { width: 5px; height: 5px; }
        ::-webkit-scrollbar-track  { background: transparent; }
        ::-webkit-scrollbar-thumb  { background: #d1d5db; border-radius: 10px; }
        ::-webkit-scrollbar-thumb:hover { background: #9ca3af; }
    </style>
</head>
<body class="bg-gray-50 flex h-screen overflow-hidden" data-page="help">

    <!-- Sidebar -->
    <jsp:include page="sidebar.jsp" />

    <!-- Main Content -->
    <main class="flex-1 flex flex-col min-w-0">
        <!-- Scrollable help body -->
        <div class="flex-1 overflow-y-auto bg-gray-50/50 relative" id="mainContent">
            <!-- Global Loader -->
            <div id="loader" class="hidden absolute inset-0 bg-white/60 backdrop-blur-sm z-50 flex items-center justify-center">
                <div class="flex flex-col items-center gap-3">
                    <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600"></div>
                    <span class="text-sm text-gray-500 font-medium">Loading help...</span>
                </div>
            </div>

            <div id="viewContainer" class="max-w-4xl mx-auto p-7"></div>
        </div>
    </main>

    <!-- Modal Container -->
    <div id="modalContainer"></div>

    <!-- Scripts -->
    <script>
        window.APP_CONTEXT = '<%= request.getContextPath() %>';
        window.showLoader = function(show) {
            const loader = document.getElementById('loader');
            if (loader) loader.classList.toggle('hidden', !show);
        };
    </script>
    <script src="assets/js/api.js"></script>
    <script src="assets/js/help.js"></script>

</body>
</html>
