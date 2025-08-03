// Global variables
let currentSection = 'home';
let currentSubSection = {};

// Initialize the app
document.addEventListener('DOMContentLoaded', function() {
    // Initialize form event listeners
    setupEventListeners();
    
    // Load initial data
    loadHomeStats();
    loadAllRequests();
});

function setupEventListeners() {
    // Donor registration form
    document.getElementById('donorForm').addEventListener('submit', function(e) {
        e.preventDefault();
        registerDonor();
    });
    
    // Receiver registration form
    document.getElementById('receiverForm').addEventListener('submit', function(e) {
        e.preventDefault();
        registerReceiver();
    });
    
    // Blood request form
    document.getElementById('requestForm').addEventListener('submit', function(e) {
        e.preventDefault();
        postBloodRequest();
    });
}

// Navigation functions
function showSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });
    
    // Show selected section
    document.getElementById(sectionId).classList.add('active');
    
    // Update navigation buttons
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    currentSection = sectionId;
    
    // Load section-specific data
    if (sectionId === 'home') {
        loadHomeStats();
    } else if (sectionId === 'requests') {
        loadAllRequests();
    } else if (sectionId === 'donor' && !currentSubSection.donor) {
        showSubSection('donor', 'register');
    } else if (sectionId === 'receiver' && !currentSubSection.receiver) {
        showSubSection('receiver', 'register');
    }
}

function showSubSection(section, subSection) {
    // Hide all sub-sections for this section
    document.querySelectorAll(`#${section} .sub-section`).forEach(sub => {
        sub.classList.remove('active');
    });
    
    // Show selected sub-section
    document.getElementById(`${section}-${subSection}`).classList.add('active');
    
    // Update section navigation buttons
    document.querySelectorAll(`#${section} .section-btn`).forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    currentSubSection[section] = subSection;
    
    // Load sub-section specific data
    if (section === 'donor' && subSection === 'donate') {
        loadAllRequests();
    }
}

// API functions
async function apiCall(endpoint, method = 'GET', data = null) {
    try {
        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            }
        };
        
        if (data && method === 'POST') {
            options.body = new URLSearchParams(data).toString();
        }
        
        const response = await fetch(`http://localhost:8080/api/${endpoint}`, options);
        return await response.json();
        
    } catch (error) {
        console.error('API Error:', error);
        showNotification('Connection error. Please make sure the server is running.', 'error');
        return null;
    }
}

// Registration functions
async function registerDonor() {
    const formData = {
        name: document.getElementById('donorName').value,
        email: document.getElementById('donorEmail').value,
        mobile: document.getElementById('donorMobile').value,
        bloodType: document.getElementById('donorBloodType').value,
        location: document.getElementById('donorLocation').value,
        gender: document.getElementById('donorGender').value,
        lastDonated: document.getElementById('lastDonated').value || 'NEVER'
    };
    
    if (!validateDonorForm(formData)) {
        return;
    }
    
    const result = await apiCall('register-donor', 'POST', formData);
    
    if (result && result.success) {
        showNotification(`✅ ${result.message} Your ID: ${result.userId}`, 'success');
        document.getElementById('donorForm').reset();
        loadHomeStats();
    } else if (result) {
        showNotification(`❌ ${result.message}`, 'error');
    }
}

async function registerReceiver() {
    const formData = {
        name: document.getElementById('receiverName').value,
        email: document.getElementById('receiverEmail').value,
        mobile: document.getElementById('receiverMobile').value,
        location: document.getElementById('receiverLocation').value,
        gender: document.getElementById('receiverGender').value
    };
    
    if (!validateReceiverForm(formData)) {
        return;
    }
    
    const result = await apiCall('register-receiver', 'POST', formData);
    
    if (result && result.success) {
        showNotification(`✅ ${result.message} Your ID: ${result.userId}`, 'success');
        document.getElementById('receiverForm').reset();
        loadHomeStats();
    } else if (result) {
        showNotification(`❌ ${result.message}`, 'error');
    }
}

async function postBloodRequest() {
    const formData = {
        userId: document.getElementById('requestUserId').value,
        bloodType: document.getElementById('requestBloodType').value,
        hospitalArea: document.getElementById('hospitalArea').value,
        unitsNeeded: document.getElementById('unitsNeeded').value,
        seriousness: document.getElementById('seriousness').value
    };
    
    if (!validateRequestForm(formData)) {
        return;
    }
    
    const result = await apiCall('post-request', 'POST', formData);
    
    if (result && result.success) {
        showNotification(`✅ ${result.message} Request ID: ${result.requestId}`, 'success');
        document.getElementById('requestForm').reset();
        loadAllRequests();
        loadHomeStats();
    } else if (result) {
        showNotification(`❌ ${result.message}`, 'error');
    }
}

// Profile and data loading functions
async function loadDonorProfile() {
    const userId = document.getElementById('donorIdLookup').value.trim();
    
    if (!userId) {
        showNotification('Please enter your Donor ID', 'error');
        return;
    }
    
    const result = await apiCall(`donor-details?userId=${encodeURIComponent(userId)}`);
    
    if (result && result.success) {
        displayDonorProfile(result);
    } else if (result) {
        showNotification(`❌ ${result.message}`, 'error');
        document.getElementById('donorProfileData').innerHTML = '';
    }
}

function displayDonorProfile(data) {
    const profileDiv = document.getElementById('donorProfileData');
    const eligibilityStatus = data.eligible ? '✅ Eligible to donate' : '⏳ Not eligible yet';
    const eligibilityClass = data.eligible ? 'text-success' : 'text-warning';
    
    profileDiv.innerHTML = `
        <h4>👤 ${data.name}</h4>
        <div class="profile-details">
            <p><strong>📧 Email:</strong> ${data.email}</p>
            <p><strong>📱 Mobile:</strong> ${data.mobile}</p>
            <p><strong>🩺 Blood Type:</strong> <span class="blood-type">${data.bloodType}</span></p>
            <p><strong>📍 Location:</strong> ${data.location}</p>
            <p><strong>🚻 Gender:</strong> ${data.gender}</p>
            <p><strong>📅 Last Donated:</strong> ${data.lastDonated}</p>
            <p><strong>🩸 Donation Status:</strong> <span class="${eligibilityClass}">${eligibilityStatus}</span></p>
        </div>
    `;
}

async function loadMyRequests() {
    const userId = document.getElementById('myRequestsUserId').value.trim();
    
    if (!userId) {
        showNotification('Please enter your Receiver ID', 'error');
        return;
    }
    
    const requests = await apiCall(`my-requests?userId=${encodeURIComponent(userId)}`);
    
    if (requests) {
        displayRequests(requests, 'myRequestsData');
    }
}

async function loadAllRequests() {
    const requests = await apiCall('get-requests');
    
    if (requests) {
        displayRequests(requests, 'allRequests');
        displayRequests(requests, 'openRequests'); // Also update donor section
    }
}

function displayRequests(requests, containerId) {
    const container = document.getElementById(containerId);
    
    if (!requests || requests.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="icon">📭</div>
                <p>No blood requests found</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = requests.map(request => {
        const urgencyClass = `request-${request.seriousness.toLowerCase()}`;
        const urgentIndicator = request.seriousness === 'HIGH' ? 'urgent-indicator' : '';
        
        return `
            <div class="request-item ${urgencyClass} ${urgentIndicator}">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                    <h4>Request ID: ${request.id}</h4>
                    <span class="status-badge status-${request.status.toLowerCase()}">${request.status}</span>
                </div>
                <p><strong>🩺 Blood Type:</strong> <span class="blood-type">${request.bloodType}</span></p>
                <p><strong>🏥 Hospital Area:</strong> ${request.hospitalArea}</p>
                <p><strong>🩺 Units Needed:</strong> ${request.unitsNeeded}</p>
                <p><strong>⚠️ Urgency:</strong> ${request.seriousness}</p>
            </div>
        `;
    }).join('');
}

async function recordDonation() {
    const donorId = document.getElementById('donorIdDonate').value.trim();
    const requestId = document.getElementById('requestIdDonate').value.trim();
    
    if (!donorId || !requestId) {
        showNotification('Please enter both Donor ID and Request ID', 'error');
        return;
    }
    
    const result = await apiCall('record-donation', 'POST', {
        donorId: donorId,
        requestId: requestId
    });
    
    if (result && result.success) {
        showNotification(`✅ ${result.message}`, 'success');
        document.getElementById('donorIdDonate').value = '';
        document.getElementById('requestIdDonate').value = '';
        loadAllRequests();
        loadHomeStats();
    } else if (result) {
        showNotification(`❌ ${result.message}`, 'error');
    }
}

async function loadHomeStats() {
    // For now, we'll show static stats since we don't have endpoints for these
    // In a real implementation, you'd have endpoints to get these statistics
    document.getElementById('totalDonors').textContent = '0';
    document.getElementById('totalRequests').textContent = '0';
    document.getElementById('totalDonations').textContent = '0';
}

// Validation functions
function validateDonorForm(data) {
    if (!data.name || !data.name.match(/^[a-zA-Z\s]+$/)) {
        showNotification('Please enter a valid name (letters and spaces only)', 'error');
        return false;
    }
    
    if (!data.email || !data.email.match(/^[a-z][a-z0-9]*@gmail\.com$/)) {
        showNotification('Please enter a valid Gmail address (lowercase)', 'error');
        return false;
    }
    
    if (!data.mobile || !data.mobile.match(/^[6-9]\d{9}$/)) {
        showNotification('Please enter a valid 10-digit mobile number starting with 6-9', 'error');
        return false;
    }
    
    if (!data.bloodType) {
        showNotification('Please select a blood type', 'error');
        return false;
    }
    
    if (!data.location.trim()) {
        showNotification('Please enter your location', 'error');
        return false;
    }
    
    if (!data.gender) {
        showNotification('Please select your gender', 'error');
        return false;
    }
    
    return true;
}

function validateReceiverForm(data) {
    if (!data.name || !data.name.match(/^[a-zA-Z\s]+$/)) {
        showNotification('Please enter a valid name (letters and spaces only)', 'error');
        return false;
    }
    
    if (!data.email || !data.email.match(/^[a-z][a-z0-9]*@gmail\.com$/)) {
        showNotification('Please enter a valid Gmail address (lowercase)', 'error');
        return false;
    }
    
    if (!data.mobile || !data.mobile.match(/^[6-9]\d{9}$/)) {
        showNotification('Please enter a valid 10-digit mobile number starting with 6-9', 'error');
        return false;
    }
    
    if (!data.location.trim()) {
        showNotification('Please enter your location', 'error');
        return false;
    }
    
    if (!data.gender) {
        showNotification('Please select your gender', 'error');
        return false;
    }
    
    return true;
}

function validateRequestForm(data) {
    if (!data.userId.trim()) {
        showNotification('Please enter your Receiver ID', 'error');
        return false;
    }
    
    if (!data.bloodType) {
        showNotification('Please select the required blood type', 'error');
        return false;
    }
    
    if (!data.hospitalArea.trim()) {
        showNotification('Please enter the hospital area', 'error');
        return false;
    }
    
    const units = parseInt(data.unitsNeeded);
    if (!units || units < 1 || units > 10) {
        showNotification('Units needed must be between 1 and 10', 'error');
        return false;
    }
    
    if (!data.seriousness) {
        showNotification('Please select the urgency level', 'error');
        return false;
    }
    
    return true;
}

// Utility functions
function showNotification(message, type = 'info') {
    const notification = document.getElementById('notification');
    notification.textContent = message;
    notification.className = `notification ${type}`;
    notification.classList.add('show');
    
    setTimeout(() => {
        notification.classList.remove('show');
    }, 5000);
}

// Initialize the home section as active
document.addEventListener('DOMContentLoaded', function() {
    showSection('home');
});
