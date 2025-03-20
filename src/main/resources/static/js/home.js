
// Fetch posts from API
fetchPosts();

// Function to fetch posts
function fetchPosts() {
    fetch('/posts', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    })
        .then(response => response.json())
        .then(data => {
            const postsContainer = document.querySelector('.post-container');
            postsContainer.innerHTML = ''; // Clear previous posts

            if (data && data.data) {
                data.data.forEach(post => {
                    const postElement = createPostElement(post);
                    postsContainer.appendChild(postElement);
                });
            }
        })
        .catch(error => {
            console.error('Error fetching posts:', error);
        });
}

const token = localStorage.getItem("token");  // Hoặc lấy token từ nơi bạn lưu trữ

if (token) {
    // Gửi request tới API với token
    fetch("/user/profile", {
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
if (!token) {
    window.location.href = "/login.html"; // Redirect to login if no token
}
