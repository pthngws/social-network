
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

// Check if token exists
const token = localStorage.getItem("token");
if (!token) {
    window.location.href = "/login.html"; // Redirect to login if no token
}
