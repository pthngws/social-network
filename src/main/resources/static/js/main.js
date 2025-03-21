document.addEventListener("DOMContentLoaded", function () {
    checkToken();
    loadInitialNotifications();
    connectWebSocket();

    // Biến để quản lý thông báo nổi
    let currentTimeout = null; // Lưu setTimeout hiện tại
    let notificationQueue = []; // Hàng đợi thông báo
    let isShowingNotification = false; // Trạng thái hiển thị thông báo

    // Lấy danh sách thông báo ban đầu qua Ajax
    function loadInitialNotifications() {
        $.ajax({
            url: "/notify",
            method: "GET",
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                "Content-Type": "application/json",
            },
            success: function (response) {
                if (response.status === 200) {
                    displayNotifications(response.data);
                } else {
                    console.error("Lỗi khi lấy thông báo:", response.message);
                }
            },
            error: function (xhr, status, error) {
                console.error("Lỗi kết nối:", error);
            },
        });
    }

    // Kết nối WebSocket để nhận thông báo thời gian thực
    function connectWebSocket() {
        const token = localStorage.getItem('token');
        if (!token) {
            console.error('No token found');
            return;
        }

        const socket = new SockJS('/ws');
        const stompClient = Stomp.over(socket);

        stompClient.connect({'Authorization': `Bearer ${token}`}, function (frame) {
            console.log('Connected to WebSocket: ' + frame);
            const payload = JSON.parse(atob(token.split('.')[1]));
            const userId = payload.sub; // Điều chỉnh nếu userId nằm ở trường khác trong token

            // Đảm bảo chỉ subscribe một lần
            stompClient.subscribe(`/topic/notifications/${userId}`, function (message) {
                const notification = JSON.parse(message.body);
                console.log('New notification received:', notification); // Debug
                addRealTimeNotification(notification);
            });
        }, function (error) {
            console.error('WebSocket error:', error);
            setTimeout(connectWebSocket, 5000); // Thử kết nối lại sau 5 giây
        });
    }

    // Tính thời gian cách đây
    function timeAgo(dateString) {
        const now = new Date();
        const date = new Date(dateString);
        const diff = Math.floor((now - date) / 1000);

        if (diff < 60) return `${diff} giây trước`;
        else if (diff < 3600) return `${Math.floor(diff / 60)} phút trước`;
        else if (diff < 86400) return `${Math.floor(diff / 3600)} giờ trước`;
        else return `${Math.floor(diff / 86400)} ngày trước`;
    }

    // Hiển thị danh sách thông báo ban đầu
    function displayNotifications(notifications) {
        const notificationsList = $("#notificationsList");
        notificationsList.empty();
        let unreadCount = 0;

        if (!notifications || !Array.isArray(notifications)) return;

        notifications.reverse().forEach(notification => {
            if (notification.isRead === 0) unreadCount++;
            const notificationItem = `
                <div class="dropdown-item" style="padding-left: 20px">
                    <div class="notification-item align-items-center">
                        <p class="m-0 fw-bold">${notification.content}</p>
                        <small class="text-muted">${timeAgo(notification.date)}</small>
                    </div>
                </div>
            `;
            notificationsList.append(notificationItem);
        });

        $("#notificationCount").text(unreadCount);
    }

    // Thêm thông báo thời gian thực
    function addRealTimeNotification(notification) {
        // Thêm vào danh sách thông báo trong dropdown
        const notificationsList = $("#notificationsList");
        const notificationItem = `
            <div class="dropdown-item" style="padding-left: 20px">
                <div class="notification-item align-items-center">
                    <p class="m-0 fw-bold">${notification.content}</p>
                    <small class="text-muted">${timeAgo(notification.date)}</small>
                </div>
            </div>
        `;
        notificationsList.prepend(notificationItem);
        const currentCount = parseInt($("#notificationCount").text()) || 0;
        $("#notificationCount").text(currentCount + 1);

        // Thêm thông báo vào hàng đợi
        notificationQueue.push(notification);
        showNextNotification();
    }

    // Hiển thị thông báo tiếp theo trong hàng đợi
    function showNextNotification() {
        if (isShowingNotification || notificationQueue.length === 0) return;

        isShowingNotification = true;
        const notification = notificationQueue.shift(); // Lấy thông báo đầu tiên trong hàng đợi
        const toast = $("#notificationToast");

        // Hủy timeout cũ nếu có
        if (currentTimeout) {
            clearTimeout(currentTimeout);
        }

        // Hiển thị thông báo
        toast.text(notification.content);
        toast.css("display", "block"); // Đặt display: block để hiện thông báo
        toast.addClass("fade-in-out"); // Áp dụng hiệu ứng fade in/out

        // Ẩn thông báo sau 3 giây
        currentTimeout = setTimeout(() => {
            toast.css("display", "none"); // Đặt display: none sau 3 giây
            isShowingNotification = false;
            showNextNotification(); // Hiển thị thông báo tiếp theo nếu có
        }, 3000); // 3000ms = 3 giây
    }

    // Toggle menu thông báo
    $("#notificationsToggle").click(function (event) {
        event.preventDefault();
        $("#notificationsMenu").toggle();
    });

    // Ẩn menu khi click ra ngoài
    document.addEventListener("click", function (event) {
        const toggle = $("#notificationsToggle")[0];
        const menu = $("#notificationsMenu")[0];
        if (!toggle.contains(event.target) && !menu.contains(event.target)) {
            $("#notificationsMenu").hide();
        }
    });

    // Xử lý lời mời kết bạn
    $.ajax({
        url: '/friendship/requests',
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        success: function (response) {
            if (response.data && response.data.length > 0) {
                const friendRequestsHtml = response.data.map(request => {
                    const timeAgoText = timeAgo(request.requestTimestamp);
                    return `
                        <div class="dropdown-item d-flex justify-content-between align-items-center">
                            <div class="d-flex">
                                <img src="${request.user.avatar}" alt="${request.user.firstName} ${request.user.lastName}"
                                    class="me-2 rounded-circle" style="width: 40px; height: 40px; object-fit: cover;" />
                                <div class="ml-2">
                                    <a href="${request.user.id}" style="color: black; font-weight: bold; text-decoration: none;">
                                        ${request.user.firstName} ${request.user.lastName}
                                    </a><br />
                                    <small class="text-muted">${timeAgoText}</small>
                                </div>
                            </div>
                            <div class="d-flex gap-2">
                                <button class="btn btn-primary btn-sm accept-btn" data-user-id="${request.user.id}">Chấp nhận</button>
                                <button class="btn btn-danger btn-sm reject-btn" data-user-id="${request.user.id}">Từ chối</button>
                            </div>
                        </div>
                    `;
                }).join('');
                $('#friendRequestList').html(friendRequestsHtml);
            } else {
                $('#friendRequestList').html('<p class="text-center text-muted">Không có lời mời nào.</p>');
            }
        },
        error: function () {
            $('#friendRequestList').html('<p class="text-center text-muted">Đã xảy ra lỗi khi tải lời mời.</p>');
        }
    });

    // Xử lý nút Chấp nhận lời mời kết bạn
    $(document).on('click', '.accept-btn', function () {
        const userId = $(this).data('user-id');
        $.ajax({
            url: '/friendship/accept',
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            },
            data: JSON.stringify({ receiverId: userId }),
            success: function () {
                location.reload();
            },
            error: function () {
                alert('Đã xảy ra lỗi khi chấp nhận lời mời!');
            }
        });
    });

    // Xử lý nút Từ chối lời mời kết bạn
    $(document).on('click', '.reject-btn', function () {
        const userId = $(this).data('user-id');
        $.ajax({
            url: '/friendship/cancel',
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            },
            data: JSON.stringify({ receiverId: userId }),
            success: function () {
                location.reload();
            },
            error: function () {
                alert('Đã xảy ra lỗi khi từ chối lời mời!');
            }
        });
    });

    // Xử lý tìm kiếm
    $('#searchButton').click(function () {
        const name = $('#search').val().trim();
        if (!name) {
            alert('Vui lòng nhập tên để tìm kiếm.');
            return;
        }
        window.location.href = `/search/${encodeURIComponent(name)}`;
    });
});

// Đăng xuất
function logout() {
    localStorage.removeItem("token");
    window.location.href = "/";
}

// Kiểm tra token
function checkToken() {
    const token = localStorage.getItem('token');
    if (!token) {
        logout();
    } else {
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.exp * 1000 < Date.now()) {
            logout();
        }
    }
}

// Kiểm tra token định kỳ
setInterval(checkToken, 60 * 1000);