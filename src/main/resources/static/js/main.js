document.addEventListener("DOMContentLoaded", function () {
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
            url: 'http://localhost:8080/friendship/requests',
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            success: function (response) {
                if (response.data && response.data.length > 0) {
                    const friendRequestsHtml = response.data.map(request => {
                        const timeAgo = calculateTimeAgo(request.requestTimestamp);

                        return `
                    <a class="dropdown-item d-flex justify-content-between" href="#">
                        <div class="d-flex">
                            <img
                                src="${request.user.avatar}"
                                alt="${request.user.firstName} ${request.user.lastName}"
                                class="rounded-circle"
                                style="width: 40px; height: 40px"
                            />
                            <div class="ml-2">
                                <strong>${request.user.firstName} ${request.user.lastName}</strong><br />
                                <small class="text-muted">${timeAgo}</small>
                            </div>
                        </div>
                        <div>
                            <button class="btn btn-primary btn-sm accept-btn" data-user-id="${request.user.id}">Chấp nhận</button>
                            <button class="btn btn-danger btn-sm reject-btn" data-user-id="${request.user.id}">Từ chối</button>
                        </div>
                    </a>
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
                url: 'http://localhost:8080/friendship/accept',
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    'Content-Type': 'application/json'
                },
                data: JSON.stringify({ receiverId: userId }), // Gửi ID của người gửi lời mời
                success: function (response) {
                    alert('Lời mời kết bạn đã được chấp nhận!');
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
                url: 'http://localhost:8080/friendship/cancel',
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    'Content-Type': 'application/json'
                },
                data: JSON.stringify({ receiverId: userId }), // Gửi ID của người gửi lời mời
                success: function (response) {
                    alert('Lời mời kết bạn đã bị từ chối!');
                    location.reload(); // Tải lại danh sách sau khi từ chối
                },
                error: function () {
                    alert('Đã xảy ra lỗi khi từ chối lời mời!');
                }
            });
        });
    });



    const token = localStorage.getItem("token");  // Hoặc lấy token từ nơi bạn lưu trữ

    if (token) {
        // Gửi request tới API với token
        fetch("http://localhost:8080/user/profile", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,  // Thêm token vào header
                "Content-Type": "application/json"
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.status === 200) {
                    const user = data.data;
                    $("#avatar-mini").attr("src", user.avatar);
                    $("#avatar").attr("src", user.avatar);  // Gán đường dẫn ảnh
                    $("#name").text(user.firstName + " " + user.lastName);  // Gán tên
                    $("#main-avatar").attr("src", user.avatar);
                    $("#main-name").text(user.firstName + " " + user.lastName);  // Gán tên
                    $("#main-about").text(user.about);
                    // Gán dữ liệu vào form
                    $("#firstname").val(user.firstName);
                    $("#lastname").val(user.lastName);
                    $("#about").val(user.about);
                    $("#birthday").val(user.birthday.split("T")[0]); // Lấy phần ngày từ birthday
                    $(`input[name='gender'][value='${user.gender}']`).prop("checked", true); // Chọn giới tính
                    // Gán avatar
                    $("#avatarImage").attr("src", user.avatar);
                } else {
                    console.error("Không thể lấy thông tin người dùng:", data.message);
                }
            })
            .catch(error => {
                console.error("Lỗi khi lấy thông tin người dùng:", error);
            });
    } else {
        console.error("Không tìm thấy token!");
    }
    const toggles = {
      groupsToggle: "groupsMenu",
      messagesToggle: "messagesMenu",
      notificationsToggle: "notificationsMenu",
    };

    // Thêm sự kiện cho từng toggle
    Object.keys(toggles).forEach((toggleId) => {
      const toggle = document.getElementById(toggleId);
      const menu = document.getElementById(toggles[toggleId]);

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
        window.location.href = `http://localhost:8080/search/${encodeURIComponent(name)}`;
    });
});
function logout() {
    // Xóa token JWT khỏi localStorage
    localStorage.removeItem("token");

    // Chuyển hướng người dùng về trang đăng nhập
    window.location.href = "/login";
}