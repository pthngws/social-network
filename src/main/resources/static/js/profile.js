$(document).ready(function () {
    const token = localStorage.getItem("token");  // Hoặc lấy token từ nơi bạn lưu trữ

    $("#userForm").submit(function (event) {
        event.preventDefault();
        const updatedUser = {
            firstName: $("#firstname").val(),
            lastName: $("#lastname").val(),
            about: $("#about").val(),
            birthday: $("#birthday").val(),
            gender: $("input[name='gender']:checked").val(),
        };
        fetch("http://localhost:8080/user/profile/update", {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(updatedUser)
        })
        .then(response => response.json())
        .then(data => {
            if (data.status === 200) {
                alert("Thông tin đã được cập nhật!");
            } else {
                console.error("Không thể cập nhật thông tin:", data.message);
            }
        })
        .catch(error => {
            console.error("Lỗi khi cập nhật thông tin:", error);
        });

    })
  });

$(document).ready(function () {
    // Hàm lấy danh sách bạn bè
    function loadFriendsList() {
        $.ajax({
            url: 'http://localhost:8080/friendship/all',
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            success: function (response) {
                if (response.data && response.data.length > 0) {
                    const friendsHtml = response.data.map(friendship => {
                        const user = friendship.user;
                        return `
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <div class="d-flex align-items-center">
                                <img
                                    src="${user.avatar}"
                                    alt="${user.firstName} ${user.lastName}"
                                    class="rounded-circle"
                                    style="width: 50px; height: 50px; margin-right: 15px;"
                                />
                                <div>
                                    <strong>${user.firstName} ${user.lastName}</strong><br />
                                    <small class="text-muted">${user.about || "Không có thông tin"}</small>
                                </div>
                            </div>
                            <button class="btn btn-danger btn-sm remove-friend-btn" data-id="${user.id}">Xóa</button>
                        </div>
                    `;
                    }).join('');
                    $('#friendsList').html(friendsHtml);
                } else {
                    $('#friendsList').html('<p class="text-center text-muted">Bạn chưa có bạn bè nào.</p>');
                }
            },
            error: function () {
                $('#friendsList').html('<p class="text-center text-muted">Đã xảy ra lỗi khi tải danh sách bạn bè.</p>');
            }
        });
    }

    // Gọi hàm loadFriendsList khi mở tab
    $('#friends-tab').on('click', function () {
        loadFriendsList();
    });

    // Xử lý xóa bạn bè
    $(document).on('click', '.remove-friend-btn', function () {
        const friendId = $(this).data('id');
        if (confirm('Bạn có chắc chắn muốn xóa bạn này?')) {
            $.ajax({
                url: 'http://localhost:8080/friendship/cancel',
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                },
                contentType: 'application/json',
                data: JSON.stringify({ receiverId: friendId }),
                success: function (response) {
                    alert(response.message);
                    loadFriendsList(); // Cập nhật lại danh sách sau khi xóa
                },
                error: function () {
                    alert('Đã xảy ra lỗi khi xóa bạn.');
                }
            });
        }
    });
});

