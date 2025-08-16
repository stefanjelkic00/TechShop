TechShop is a modern online technology store developed as a full-stack application.
Technologies used are Spring Boot, React, MySQL and ElasticSearch.
Elastic Search:
Running elasticsearch regularly:

Elasticsearch file must be installed
Navigate to this path: \ElasticSearch\elasticsearch-8.17.2/bin
Click on elasticsearch.bat and startup will begin
Wait (2-3 min) for it to fully start, then we can proceed to the backend part of the application
System will be available at: http://localhost:9200
Docker:

Backend:
Java 17                         -Version used
Spring Boot                     -Framework for developing REST APIs
Spring Security + JWT           -User authentication and authorization
Spring Data JPA                 -ORM layer for database operations
Hibernate                       -Maps Java objects to relational databases
MySQL                           -Relational database
Lombok                          -Getters, setters etc. - for cleaner and smaller code
Elasticsearch                   -Advanced product search and filtering
Spring Mail (Gmail SMTP)        -Sending email notifications to users on activities
Swagger UI                      -API testing
Maven                           -Project and dependency management
Logging                         -Logic for activity tracking
Running backend regularly:

Ensure MySQL and ElasticSearch are running
After that, the app can be started via TechShopApplication startup file
Application will be available at: http://localhost:8001
Docker:

Frontend:
React                           -Library for user interface development
Axios                           -HTTP client for backend communication
Bootstrap CSS                   -Framework for responsive design
React Bootstrap                 -React components based on Bootstrap
Formik                          -Manages forms and validation in React
Yup Library                     -Library for form validation (used with Formik)
JWT Decode                      -Decoding JWT tokens on client side
React Toastify                  -Displaying notifications and alerts
Running frontend regularly:

Open terminal in techshopfront folder
Install all dependencies: npm install
Start application: npm start
Application will be available at: http://localhost:3000
Docker:

Database:
It's best to manually add products within the app due to MySQL and Elastic Search database synchronization.
As for users, manually register customers and manually enter admin since there is no admin registration.
MySQL will be available at: http://localhost:3306

Application functionalities:

HomePage:
When the app starts, it takes us to the home page (HomePage).
The nav bar will only have a login button, while within the home page at the top there will be a section for multiple filtering by product category,
filtering by price display from lowest to highest and vice versa, and filtering by minimum and maximum price.
Below the filters, which are synchronized with each other, there is a search field that is also synchronized with the filters.
The search has functional case-insensitive functionality, contains the text we entered, and offers product suggestions when we enter a few characters.
Regarding products and their display, they consist of image, name and price. Price can have discounts (10%, 20%, 30%) depending on user type.
When clicking on a product field, it takes us to that product's individual page where the product description will also be displayed.
When something is discounted, there will be a label within the image in a circle showing the discount. The price will be crossed out and the discounted price will be shown next to it.
If a product is currently out of stock but its field is still there, it will say where the price is located that it's out of stock and cannot be added to cart.
The product field also has a cart button that adds it to cart when clicked.

Cart:
When a user logs in and adds an item to cart, they will see the product image, name and discount if applicable.
They will also have displayed quantity with options to add more quantity, remove quantity and total removal.
Below all products, it will display their total price and below that a form for entering the address.
When the user enters all that, they can click the order button below which creates the given order.

Orders:
When a user clicks on the orders button, it takes them to view all orders.
There will be displayed ID, order status and total price.
Each of them acts as a button that takes us to more detailed information about the order we click on.
Within the detailed order, it shows ID, date, total price, status, discount, items and order address.

Profile:
Within this page, we will have a detailed view of user data.
Below we will have an information button about user type and how user type changes when ordering.
REGULAR: 0 orders - No additional discount.
PREMIUM: 1-2 orders - 10% discount on items.
PLATINUM: 3-4 orders - 20% discount on items.
VIP: 5 or more orders - 30% discount on items.
Below everything will be a password change button.

UsersPage: (Admin)
All other previously mentioned pages can be accessed by both Customer and Admin.
UsersPage can only be accessed by admin.
It contains a display of all users and user data with fields for updating and deleting them.

ProductsPage: (Admin)
Administrative page with detailed display of all products.
Within this page we have the ability to add a new product.
We also have the ability to edit or delete existing products.

UserOrdersPage: (Admin)
Administrative page displaying all orders.
Within this page, admin has the ability to change order status (PENDING, SHIPPED, DELIVERED, CANCELLED).
We have a button that takes us to detailed order view.
We also have a button for complete order deletion.
