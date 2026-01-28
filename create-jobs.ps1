# PowerShell script to create test job orders

# First, login as admin
$loginBody = @{
    username = "admin"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.data.token

Write-Host "Logged in successfully. Token: $token"

# Create job orders
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$jobs = @(
    @{
        jobOrderRef = "JO-001"
        employerId = 1
        jobTitle = "Mechanical Engineer"
        headcountRequired = 10
        headcountFilled = 0
        salaryMin = 2500.00
        salaryMax = 3500.00
        currency = "USD"
        location = "Dubai"
        country = "UAE"
        contractDurationMonths = 24
        requiredSkills = "AutoCAD, SolidWorks, 3+ years experience"
        description = "Seeking qualified Mechanical Engineers for construction projects"
        status = "OPEN"
    },
    @{
        jobOrderRef = "JO-002"
        employerId = 1
        jobTitle = "Chef"
        headcountRequired = 5
        headcountFilled = 0
        salaryMin = 2000.00
        salaryMax = 3000.00
        currency = "USD"
        location = "Dubai"
        country = "UAE"
        contractDurationMonths = 24
        requiredSkills = "Culinary degree, 5+ years experience"
        description = "Experienced Chef for 5-star hotel"
        status = "OPEN"
    },
    @{
        jobOrderRef = "JO-003"
        employerId = 1
        jobTitle = "Civil Engineer"
        headcountRequired = 8
        headcountFilled = 0
        salaryMin = 3000.00
        salaryMax = 4000.00
        currency = "USD"
        location = "Abu Dhabi"
        country = "UAE"
        contractDurationMonths = 36
        requiredSkills = "AutoCAD, Civil 3D, 4+ years experience"
        description = "Civil Engineers for infrastructure projects"
        status = "OPEN"
    },
    @{
        jobOrderRef = "JO-004"
        employerId = 1
        jobTitle = "Nurse"
        headcountRequired = 15
        headcountFilled = 0
        salaryMin = 1800.00
        salaryMax = 2500.00
        currency = "USD"
        location = "Riyadh"
        country = "Saudi Arabia"
        contractDurationMonths = 24
        requiredSkills = "Nursing degree, valid license, 3+ years experience"
        description = "Registered Nurses for hospital"
        status = "OPEN"
    },
    @{
        jobOrderRef = "JO-005"
        employerId = 1
        jobTitle = "Electrical Engineer"
        headcountRequired = 6
        headcountFilled = 0
        salaryMin = 2800.00
        salaryMax = 3800.00
        currency = "USD"
        location = "Doha"
        country = "Qatar"
        contractDurationMonths = 36
        requiredSkills = "Electrical systems, 4+ years in oil & gas"
        description = "Electrical Engineers for oil & gas sector"
        status = "OPEN"
    }
)

foreach ($job in $jobs) {
    $jobJson = $job | ConvertTo-Json
    
    try {
        # First create with PENDING_APPROVAL status
        $job.status = "PENDING_APPROVAL"
        $jobJson = $job | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/job-orders" -Method Post -Body $jobJson -Headers $headers
        $jobId = $response.data.id
        Write-Host "Created job order: $($job.jobTitle) (ID: $jobId)"
        
        # Now update status to OPEN (admin can do this)
        $updateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/job-orders/$jobId/status?status=OPEN" -Method Patch -Headers $headers
        Write-Host "Updated job order $jobId to OPEN status"
        
    } catch {
        Write-Host "Error creating job order $($job.jobTitle): $_"
    }
}

Write-Host "`nDone! Job orders created successfully."
