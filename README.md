# estimator
Web-application for prediction the end date of the project

To run the app locally just run Spring Boot Application! That's it!

API Description:
- /api/companies (POST) - create test entry (company with developers) to play with
- /api/companies (DELETE) - delete all entries (companies with all developers)
- /api/companies/prediction (POST) - ask about prediction (RequestBody is below)

{
	"backendTasksEstimation": 50,
	"frontendTasksEstimation": 100
}

Note: some entries are already created and you don't need to create them at the beginning
