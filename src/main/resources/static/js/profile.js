$(document).ready(function () {
    const token = localStorage.getItem("token"); // Lấy token từ localStorage

    $("#userForm").submit(function (event) {
        event.preventDefault();

        // Tạo FormData để gửi dữ liệu dạng multipart/form-data
        const formData = new FormData();
        const avatarFile = $("#avatar1")[0].files[0]; // Lấy file từ input

        // Thêm file nếu có
        if (avatarFile) {
            formData.append("avatarFile", avatarFile);
        }

        // Thêm các thông tin khác
        formData.append("firstName", $("#firstname").val());
        formData.append("lastName", $("#lastname").val());
        formData.append("about", $("#about").val());
        formData.append("birthday", $("#birthday").val());
        formData.append("gender", $("input[name='gender']:checked").val());

        // Gửi request với fetch
        fetch("http://localhost:8080/user/profile/update", {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${token}`, // Thêm token vào header
            },
            body: formData, // FormData tự động đặt Content-Type
        })
            .then((response) => response.json())
            .then((data) => {
                if (data.status === 200) {
                    alert("Thông tin đã được cập nhật!");
                    if (avatarFile) {
                        // Hiển thị ảnh mới
                        const imageUrl = URL.createObjectURL(avatarFile);
                        $("#avatarImage").attr("src", imageUrl);
                    }
                } else {
                    alert("Không thể cập nhật thông tin: " + data.message);
                }
            })
            .catch((error) => {
                console.error("Lỗi khi cập nhật thông tin:", error);
            });
    });
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
// Fetch posts from API
fetchPosts();

// Function to fetch posts
function fetchPosts() {
    fetch("http://localhost:8080/myposts", {
        method: "GET",
        headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
    })
        .then((response) => response.json())
        .then((data) => {
            const postsContainer = document.querySelector(".post-container");
            postsContainer.innerHTML = ""; // Clear previous posts

            if (data && data.data) {
                data.data.forEach((post) => {
                    const postElement = createPostElement(post);
                    postsContainer.appendChild(postElement);
                });
            }
        })
        .catch((error) => {
            console.error("Error fetching posts:", error);
        });
}


// Check if token exists
const token = localStorage.getItem("token");
if (!token) {
    window.location.href = "/login.html"; // Redirect to login if no token
}