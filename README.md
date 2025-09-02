# 🩸 Saviour Blood Donation Web App

A modern, web-based blood donation management system that connects blood donors with receivers in need. This application facilitates seamless blood donation processes with real-time matching, eligibility tracking, and request management.

## 🌟 Features

### For Donors 🩸
- **Easy Registration**: Simple signup process with OTP verification
- **Eligibility Tracking**: Automatic donation eligibility calculation based on gender-specific cooldown periods
- **Donation History**: Track your donation history and last donation date.
- **Smart Matching**: Get matched with compatible blood requests in your area
- **Real-time Updates**: Receive instant notifications about donation opportunities

### For Receivers 🏥
- **Quick Request Posting**: Post urgent blood requests with detailed requirements
- **Donor Discovery**: Find eligible donors based on blood type and location compatibility
- **Request Tracking**: Monitor the status of your blood requests in real-time
- **Priority Levels**: Set urgency levels (LOW, MODERATE, HIGH) for critical situations

### System Features ⚙️
- **Blood Compatibility Matching**: Accurate blood type compatibility checking
- **Gender-based Cooldown**: 120 days for males, 180 days for females
- **OTP Verification**: Secure registration process
- **Real-time Web Interface**: Modern, responsive web UI
- **Request Management**: Automatic request fulfillment tracking

## 🚀 Technology Stack

- **Backend**: Java 8+ with built-in HTTP server
- **Frontend**: HTML5, CSS3, JavaScript (ES6+)
- **Architecture**: RESTful API design
- **Data Storage**: In-memory data structures (easily extensible to databases)
- **Server**: Java HttpServer for lightweight deployment

## 📋 Prerequisites

- Java 8 or higher
- Modern web browser (Chrome, Firefox, Safari, Edge)
- Port 8080 available on your system

## 🛠️ Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/durveshali49/Saviour.git
   cd Saviour
   ```

2. **Compile the application**
   ```bash
   javac BloodDonationWebApp.java
   ```

3. **Run the web server**
   ```bash
   java BloodDonationWebApp
   ```

4. **Access the application**
   Open your browser and navigate to: `http://localhost:8080`

## 🎯 How to Use

### For Donors:
1. **Register**: Fill out the donor registration form with your details
2. **Verify**: Complete OTP verification via your mobile number
3. **Browse Requests**: View open blood requests in your area.
4. **Donate**: Record your donations to help those in need

### For Receivers:
1. **Register**: Sign up as a blood receiver
2. **Post Request**: Create a blood request with specific requirements
3. **Find Donors**: Search for compatible donors in your location.
4. **Track Status**: Monitor your request until fulfillment

## 🩺 Blood Compatibility Matrix

| Donor → Receiver | O- | O+ | A- | A+ | B- | B+ | AB- | AB+ |
|------------------|----|----|----|----|----|----|-----|-----|
| **O-**           | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅  | ✅  |
| **O+**           | ❌ | ✅ | ❌ | ✅ | ❌ | ✅ | ❌  | ✅  |
| **A-**           | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ✅  | ✅  |
| **A+**           | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌  | ✅  |
| **B-**           | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅  | ✅  |
| **B+**           | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌  | ✅  |
| **AB-**          | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅  | ✅  |
| **AB+**          | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌  | ✅  |

## 📁 Project Structure

```
Blood1/
├── BloodDonationWebApp.java    # Main application with web server
├── index.html                  # Frontend HTML interface
├── styles.css                  # Styling and responsive design
├── script.js                   # Frontend JavaScript functionality
├── .gitignore                  # Git ignore configuration
└── README.md                   # Project documentation
```

## 🔧 API Endpoints

### Donor Endpoints
- `POST /api/register-donor` - Register a new donor
- `GET /api/donor-details?userId={id}` - Get donor information
- `POST /api/record-donation` - Record a blood donation

### Receiver Endpoints  
- `POST /api/register-receiver` - Register a new receiver
- `POST /api/post-request` - Post a blood request
- `GET /api/my-requests?userId={id}` - Get user's requests

### General Endpoints
- `GET /api/open-requests` - Get all open blood requests
- `GET /api/find-donors?requestId={id}` - Find donors for a request

## 📊 Donation Eligibility Rules

- **Male Donors**: Can donate every 120 days (4 months)
- **Female Donors**: Can donate every 180 days (6 months)
- **Age Requirements**: 18-65 years (implemented in validation)
- **Health Status**: Basic validation through registration process

## 🔒 Security Features

- **OTP Verification**: Mobile number verification during registration
- **Input Validation**: Comprehensive validation for all user inputs
- **CORS Headers**: Proper cross-origin resource sharing configuration
- **Error Handling**: Graceful error handling and user feedback

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📈 Future Enhancements

- [ ] Database integration (MySQL/PostgreSQL)
- [ ] Real SMS OTP integration
- [ ] Email notifications
- [ ] Mobile app development
- [ ] Admin dashboard
- [ ] Analytics and reporting
- [ ] Geolocation-based matching
- [ ] Social media integration
- [ ] Multi-language support

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Shaik Durveshali** - *Initial work* - [durveshali49](https://github.com/durveshali49)

## 🙏 Acknowledgments

- Thanks to all blood donors who save lives every day
- Inspired by the need for efficient blood donation management
- Built with the goal of connecting donors and receivers seamlessly

## 📞 Support

For support, email durveshali49@gmail.com or create an issue in this repository.

---


**⚡ Quick Start**: Clone → Compile → Run → Visit `localhost:8080` → Start saving lives! 🩸❤️





































