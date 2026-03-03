<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>User Management - Ocean View Resort</title>

  <script src="https://cdn.tailwindcss.com"></script>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
    body { font-family: 'Inter', sans-serif; }
  </style>
</head>

<body class="bg-gray-50 flex h-screen overflow-hidden" data-page="users">

  <jsp:include page="sidebar.jsp" />

  <main class="flex-1 flex flex-col">
    <jsp:include page="top-header.jsp">
      <jsp:param name="title" value="User Management"/>
    </jsp:include>

    <div class="flex-1 p-8 overflow-y-auto bg-gray-50/50 relative">
      <!-- Loader -->
      <div id="loader" class="hidden absolute inset-0 bg-white/50 backdrop-blur-sm z-50 flex items-center justify-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>

      <div id="viewContainer" class="max-w-7xl mx-auto"></div>
    </div>
  </main>

  <!-- Modal Container -->
  <div id="modalContainer"></div>

  <script>
    window.APP_CONTEXT = '<%= request.getContextPath() %>';
    window.showLoader = function(show) {
      const loader = document.getElementById('loader');
      if (!loader) return;
      loader.classList.toggle('hidden', !show);
    };
  </script>
  <script src="assets/js/api.js"></script>
  <script src="assets/js/users.js"></script>
  <script>
    document.addEventListener('DOMContentLoaded', () => {
      UsersModule.init();
    });
  </script>

</body>
</html>
