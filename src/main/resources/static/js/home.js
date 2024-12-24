document.getElementById('postMedia').addEventListener('change', function(event) {
    const files = event.target.files;
    const uploadContainer = document.getElementById('mediaUploadContainer');

    Array.from(files).forEach(file => {
        const reader = new FileReader();
        reader.onload = function(e) {
            const mediaItem = document.createElement('div');
            mediaItem.classList.add('media-item');

            if (file.type.startsWith('image/')) {
                const img = document.createElement('img');
                img.src = e.target.result;
                mediaItem.appendChild(img);
            } else if (file.type.startsWith('video/')) {
                const video = document.createElement('video');
                video.src = e.target.result;
                video.controls = true;
                mediaItem.appendChild(video);
            }

            // Add remove button
            const removeBtn = document.createElement('button');
            removeBtn.classList.add('remove-btn');
            removeBtn.innerHTML = '&times;';
            removeBtn.onclick = function() {
                mediaItem.remove();
            };
            mediaItem.appendChild(removeBtn);

            uploadContainer.appendChild(mediaItem);
        };
        reader.readAsDataURL(file);
    });
});

let currentPostId = null;

// Fetch posts from API
fetchPosts();

// Function to fetch posts
function fetchPosts() {
    fetch('http://localhost:8080/posts', {
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

// Function to create post element dynamically
function createPostElement(post) {
    const postElement = document.createElement('div');
    postElement.classList.add('post-box');

    // Tính toán thời gian chênh lệch
    const postDate = new Date(post.timestamp);
    const currentDate = new Date();
    const timeDiff = currentDate - postDate;

    const secondsAgo = Math.floor(timeDiff / 1000); // Số giây đã trôi qua
    const minutesAgo = Math.floor(secondsAgo / 60); // Số phút đã trôi qua
    const hoursAgo = Math.floor(minutesAgo / 60); // Số giờ đã trôi qua

    // Hiển thị thời gian theo định dạng phù hợp
    let timeDisplay;
    if (hoursAgo < 1) {
        if (minutesAgo < 1) {
            timeDisplay = `${secondsAgo} giây trước`;
        } else {
            timeDisplay = `${minutesAgo} phút trước`;
        }
    } else if (hoursAgo < 24) {
        timeDisplay = `${hoursAgo} giờ trước`;
    } else {
        timeDisplay = postDate.toLocaleString(); // Nếu quá 24 giờ, hiển thị ngày giờ đầy đủ
    }

    postElement.innerHTML = `
        <div class="post-header d-flex align-items-center mb-3">
            <img src="${post.imageUrl}" alt="User" class="me-2">
            <div>
                <strong>${post.authorName}</strong>
                <br>
                <small class="text-muted">${timeDisplay}</small>
            </div>
        </div>
        <div class="post-content">
            <h2>${post.content}</h2>
        </div>
        <hr>
        <div class="reaction-icons d-flex justify-content-between">
            <i class="bi bi-heart likeBtn" data-post-id="${post.id}"> Thích (${post.likedByCount})</i>
            <i class="bi bi-chat"> Bình luận (${post.commentCount})</i>
        </div>
    `;

    // Thêm sự kiện click vào nút "like"
    const likeBtn = postElement.querySelector('.likeBtn');
    likeBtn.addEventListener('click', function () {
        const postId = likeBtn.getAttribute('data-post-id');

        // Gửi yêu cầu POST để like bài viết
        fetch(`http://localhost:8080/like/${postId}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            }
        })
            .then(response => {
                if (response.ok) {
                    // Cập nhật số lượt thích
                    const likeCount = likeBtn.innerText.match(/\d+/)[0];
                    likeBtn.innerText = `Thích (${Number(likeCount) + 1})`;
                } else {
                    console.error('Like failed');
                }
            })
            .catch(error => {
                console.error('Error while liking post:', error);
            });
    });

    return postElement;
}

// Function to submit a new post with media
// Function to submit a new post with media
function submitPost() {
    const content = document.getElementById('postContent').value;
    const mediaFiles = document.getElementById('postMedia').files;

    const mediaArray = [];

    // Đính kèm từng file media
    for (let i = 0; i < mediaFiles.length; i++) {
        const file = mediaFiles[i];
        const reader = new FileReader();

        reader.onload = function (e) {
            const mediaItem = {
                url: e.target.result,
                type: file.type.startsWith('image/') ? 'image' : 'video',
            };
            mediaArray.push(mediaItem);
        };

        reader.readAsDataURL(file);
    }

    // Đợi cho đến khi các file đã được đọc
    Promise.all(Array.from(mediaFiles).map(file => {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.onerror = reject;
            reader.readAsDataURL(file);
        });
    }))
        .then(() => {
            const postData = {
                content: content,
                media: mediaArray
            };

            fetch('http://localhost:8080/post', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(postData)
            })
                .then(response => response.json())
                .then(data => {
                    if (data.status === 200) {
                        console.log('Post created successfully:', data);
                        fetchPosts(); // Fetch posts after successful submission
                        const createPostModal = bootstrap.Modal.getInstance(document.getElementById('createPostModal'));
                        createPostModal.hide();
                    } else {
                        console.error('Failed to create post:', data.message);
                    }
                })
                .catch(error => console.error('Error submitting post:', error));
        })
        .catch(error => {
            console.error('Error reading files:', error);
        });
}



// Handle new post submission
document.getElementById('postSubmitBtn').addEventListener('click', function() {
    const content = document.getElementById('postContent').value;
    const postData = {
        content: content,
        media: [] // Add media support here if needed
    };

    fetch('http://localhost:8080/post', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(postData)
    })
        .then(response => response.json())
        .then(data => {
            fetchPosts(); // Re-fetch posts after creation
            const createPostModal = bootstrap.Modal.getInstance(document.getElementById('createPostModal'));
            createPostModal.hide();
        })
        .catch(error => {
            console.error('Error creating post:', error);
        });
});

// Check if token exists
const token = localStorage.getItem("token");
if (!token) {
    window.location.href = "/login.html"; // Redirect to login if no token
}

// Handle logout
const logoutBtn = document.getElementById("logoutBtn");
if (logoutBtn) {
    logoutBtn.addEventListener("click", function () {
        localStorage.removeItem("token");
        window.location.href = "/login.html";
    });
}