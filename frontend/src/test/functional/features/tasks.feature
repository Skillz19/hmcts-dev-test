Feature: Task management journeys

    Scenario: Task list page loads
        When I go to '/tasks'
        Then the page heading should be 'Tasks'
        Then the page should include 'Create New Task'

    Scenario: Create task from UI
        When I go to '/tasks/new'
        Then the page heading should be 'Create New Task'
        When I fill 'Title' with 'Functional Task'
        When I fill 'Description' with 'Created by functional test'
        When I select 'Status' as 'PENDING'
        When I fill 'Due Date' with '2027-12-20'
        When I click 'Create Task'
        Then the page URL should be '/tasks'
        When I go to '/tasks?sortBy=id&direction=desc&size=100'
        Then the page should include 'Functional Task'


    Scenario: View task details and return to list
        When I go to '/tasks'
        When I click 'View'
        Then the page heading should be 'Task Details'
        When I click 'Back to List'
        Then the page heading should be 'Tasks'
