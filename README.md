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

Application is deployed on Heroku server and available under this link -  https://calm-spire-41022.herokuapp.com/
Please consider that we use free Heroku plan and sometimes it has to run, so, please, if you see just white screen - wait a minute while application is up again.

Instruction how to use endpoints:
- Use CURL for better experience with API
- Open the console on your computer (cmd, terminal, whatever)
- Put the following requests inside and run it (result will be delivered in the console as well)
  * curl -X POST https://calm-spire-41022.herokuapp.com/api/companies (for creating a new entry)
  * curl -X DELETE https://calm-spire-41022.herokuapp.com/api/companies (for deleting all entries)
  * curl -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"backendTasksEstimation": 50,"frontendTasksEstimation":100}' https://calm-spire-41022.herokuapp.com/api/companies/prediction (for prediction)
  

