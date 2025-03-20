document.addEventListener("DOMContentLoaded", function () {
    checkToken();
    $.ajax({
        url: "/notify",
        method: "GET",
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            "Content-Type": "application/json",
        },
        success: function (response) {
            if (response.status === 200) {
                displayNotifications(response.data); // Hiển thị thông báo
            } else {
                console.error("Lỗi khi lấy thông báo:", response.message);
            }
        },
        error: function (xhr, status, error) {
            console.error("Lỗi kết nối:", error);
        },
    });
    function timeAgo(dateString) {
        const now = new Date();
        const date = new Date(dateString);
        const diff = Math.floor((now - date) / 1000); // Tính chênh lệch thời gian (giây)

        if (diff < 60) {
            return `${diff} giây trước`;
        } else if (diff < 3600) {
            const minutes = Math.floor(diff / 60);
            return `${minutes} phút trước`;
        } else if (diff < 86400) {
            const hours = Math.floor(diff / 3600);
            return `${hours} giờ trước`;
        } else {
            const days = Math.floor(diff / 86400);
            return `${days} ngày trước`;
        }
    }

// Hàm hiển thị thông báo
    function displayNotifications(notifications) {
        const notificationsList = $("#notificationsList");
        notificationsList.empty();

        notifications.reverse().forEach(notification => {
            const notificationItem = `
      <div class="dropdown-item" style="padding-left: 20px">
        <div class="notification-item align-items-center">
    <div >
        <p class="m-0 fw-bold">${notification.content}</p>
        <small class="text-muted">${timeAgo(notification.date)}</small>
    </div>
</div>

      </div>
    `;
            notificationsList.append(notificationItem);
        });
    }
    $(document).ready(function () {
        // Hàm tính thời gian gửi kết bạn
        function calculateTimeAgo(timestamp) {
            const now = new Date();
            const requestTime = new Date(timestamp);
            const diffInSeconds = Math.floor((now - requestTime) / 1000);

            if (diffInSeconds < 60) {
                return 'Vừa xong';
            } else if (diffInSeconds < 3600) {
                return `${Math.floor(diffInSeconds / 60)} phút trước`;
            } else if (diffInSeconds < 86400) {
                return `${Math.floor(diffInSeconds / 3600)} giờ trước`;
            } else {
                return `${Math.floor(diffInSeconds / 86400)} ngày trước`;
            }
        }

        // Gửi yêu cầu API để lấy danh sách lời mời kết bạn
        $.ajax({
            url: '/friendship/requests',
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            success: function (response) {
                if (response.data && response.data.length > 0) {
                    const friendRequestsHtml = response.data.map(request => {
                        const timeAgo = calculateTimeAgo(request.requestTimestamp);

                        return `
                    <div class="dropdown-item d-flex justify-content-between align-items-center">
    <!-- Avatar and User Info -->
    <div class="d-flex">
        <img
            src="${request.user.avatar}"
            alt="${request.user.firstName} ${request.user.lastName}"
            class="me-2 rounded-circle" style="width: 40px; height: 40px; object-fit: cover;"
        />
        <div class="ml-2">
            <a href="${request.user.id}" style="color: black; font-weight: bold; text-decoration: none;">
                ${request.user.firstName} ${request.user.lastName}
            </a>
            <br />
            <small class="text-muted">${timeAgo}</small>
        </div>
    </div>

    <!-- Accept/Reject Buttons -->
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

        // Lắng nghe sự kiện click nút Chấp nhận
        $(document).on('click', '.accept-btn', function () {
            const userId = $(this).data('user-id'); // Lấy ID của người gửi lời mời

            $.ajax({
                url: '/friendship/accept',
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    'Content-Type': 'application/json'
                },
                data: JSON.stringify({ receiverId: userId }), // Gửi ID của người gửi lời mời
                success: function (response) {
                    location.reload(); // Tải lại danh sách sau khi chấp nhận
                },
                error: function () {
                    alert('Đã xảy ra lỗi khi chấp nhận lời mời!');
                }
            });
        });

        // Lắng nghe sự kiện click nút Từ chối
        $(document).on('click', '.reject-btn', function () {
            const userId = $(this).data('user-id'); // Lấy ID của người gửi lời mời

            $.ajax({
                url: '/friendship/cancel',
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    'Content-Type': 'application/json'
                },
                data: JSON.stringify({ receiverId: userId }), // Gửi ID của người gửi lời mời
                success: function (response) {
                    location.reload(); // Tải lại danh sách sau khi từ chối
                },
                error: function () {
                    alert('Đã xảy ra lỗi khi từ chối lời mời!');
                }
            });
        });
    });


    const toggles = {
      groupsToggle: "groupsMenu",
      notificationsToggle: "notificationsMenu",
    };

    Object.keys(toggles).forEach((toggleId) => {
        const toggle = document.getElementById(toggleId);
        const menu = document.getElementById(toggles[toggleId]);

        // Kiểm tra nếu toggle và menu đều tồn tại trong DOM
        if (toggle && menu) {
            toggle.addEventListener("click", function (event) {
                event.preventDefault();

                // Ẩn tất cả các menu khác
                Object.keys(toggles).forEach((otherToggleId) => {
                    const otherMenu = document.getElementById(toggles[otherToggleId]);
                    if (otherMenu !== menu) {
                        otherMenu.style.display = "none";
                    }
                });

                // Hiển thị hoặc ẩn menu hiện tại
                menu.style.display =
                    menu.style.display === "block" ? "none" : "block";
            });
        }
    });


    // Ẩn tất cả menu khi click ra ngoài
    document.addEventListener("click", function (event) {
      Object.keys(toggles).forEach((toggleId) => {
        const toggle = document.getElementById(toggleId);
        const menu = document.getElementById(toggles[toggleId]);

        if (
          !toggle.contains(event.target) &&
          !menu.contains(event.target)
        ) {
          menu.style.display = "none";
        }
      });
    });
  });


$(document).ready(function () {
    $('#searchButton').click(function () {
        const name = $('#search').val().trim();

        if (!name) {
            alert('Vui lòng nhập tên để tìm kiếm.');
            return;
        }

        // Chuyển hướng sang trang kết quả với từ khóa trong URL
        window.location.href = `/search/${encodeURIComponent(name)}`;
    });
});
function logout() {
    // Xóa token JWT khỏi localStorage
    localStorage.removeItem("token");

    // Chuyển hướng người dùng về trang đăng nhập
    window.location.href = "/";
}
function checkToken() {
    const token = localStorage.getItem('token');
    if (!token) {
        // Token không tồn tại
        logout();
    } else {
        const payload = JSON.parse(atob(token.split('.')[1])); // Decode payload
        const isExpired = payload.exp * 1000 < Date.now();
        if (isExpired) {
            // Token đã hết hạn
            logout();
        }
    }
}

// Gọi hàm checkToken định kỳ (ví dụ 5 phút/lần)
setInterval(checkToken,  60 * 1000);
